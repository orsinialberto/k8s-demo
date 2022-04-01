package com.example.demo.backend.elasticsearch;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class ElasticsearchClient {

    private final RestClient restClient;
    @Value("${elasticsearch.index-name}")
    private String indexName;

    public ElasticsearchClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    public void index(final String body) throws IOException {

        requireNonNull(body, "body must be not null");

        final HttpEntity entity = new NStringEntity(body, ContentType.APPLICATION_JSON);

        final Request request = new Request(HttpMethod.PUT.toString(), "/_bulk");
        request.setEntity(entity);

        restClient.performRequest(request);
    }

    public void deleteIndex() throws IOException {

        final Request request = new Request(HttpMethod.DELETE.toString(), "/" + indexName);
        restClient.performRequest(request);
    }

    public void createIndex() throws IOException {

        final Request request = new Request(HttpMethod.PUT.toString(), "/" + indexName);
        restClient.performRequest(request);
    }
}
