package com.example.demo.backend.elasticsearch;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.http.HttpMethod;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class ElasticsearchClient {

    private final RestClient restClient;

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
}
