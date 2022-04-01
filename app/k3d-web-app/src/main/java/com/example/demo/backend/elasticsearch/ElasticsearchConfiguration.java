package com.example.demo.backend.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ElasticsearchConfiguration {

    private static final String CONNECTION_MANAGER_NAME = "ElasticsearchClient kube-elastic connection manager";
    private static final String IO_DISPATCHER_NAME_FORMAT = "ElasticsearchClient kube-elastic I/O dispatcher %d";

    @Value("${elasticsearch.url}")
    private String url;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.username}")
    private String userName;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.key-store-file}")
    private String keyStoreFile;

    @Value("${elasticsearch.cert-file-password}")
    private String certFilePassword;


    /**
     * Elasticsearch admin client for elasticsearch cluster.
     *
     * @return ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {

        final RestClient restClient = RestClient
                .builder(new HttpHost(url, port, "https"))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000)
                )
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setConnectionManager(connectionManager())
                        .setThreadFactory(threadFactory -> new Thread(threadFactory, CONNECTION_MANAGER_NAME))
                        .setDefaultCredentialsProvider(credentialsProvider())
                )
                .build();

        return new ElasticsearchClient(restClient);
    }

    /**
     * A connection manager for elasticsearch cluster security.
     *
     * @return PoolingNHttpClientConnectionManager
     */
    private PoolingNHttpClientConnectionManager connectionManager() {

        try {
            final AtomicInteger ioDispatcherNumber = new AtomicInteger(1);

            final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setSoKeepAlive(true)
                    .build();

            final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(
                    ioReactorConfig,
                    threadFactory -> new Thread(
                            threadFactory,
                            String.format(IO_DISPATCHER_NAME_FORMAT, ioDispatcherNumber.getAndIncrement())
                    )
            );

            final KeyStore keyStore = KeyStore.getInstance("pkcs12");

            try (final InputStream is = Files.newInputStream(Paths.get(keyStoreFile))) {
                keyStore.load(is, certFilePassword.toCharArray());
            }

            final SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(keyStore, null)
                    .loadKeyMaterial(keyStore, certFilePassword.toCharArray())
                    .build();

            final SSLIOSessionStrategy sslStrategy = new SSLIOSessionStrategy(
                    sslcontext,
                    null,
                    null,
                    new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault())
            );

            final Registry<SchemeIOSessionStrategy> ioSessionFactoryRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                    .register("http", NoopIOSessionStrategy.INSTANCE)
                    .register("https", sslStrategy)
                    .build();

            return new PoolingNHttpClientConnectionManager(ioReactor, ioSessionFactoryRegistry);

        } catch (final KeyStoreException | NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | CertificateException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private BasicCredentialsProvider credentialsProvider() {

        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        return credentialsProvider;
    }
}
