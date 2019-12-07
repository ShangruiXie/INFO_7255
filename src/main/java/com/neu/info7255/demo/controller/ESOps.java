package com.neu.info7255.demo.controller;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class ESOps {
    private RestClient client;

    private void initConnection(){
        client = RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http")).build();
    }

    private void closeConnection() throws IOException {
        client.close();
        client = null;
    }



    public String putCreateDoc(String req) throws IOException {
        initConnection();

        JSONObject reqJSON = new JSONObject(req);
        String objectId = reqJSON.getString("objectId");

        Request request = new Request("PUT", "/orders/_doc/"+objectId);
        request.setJsonEntity(req);
        Response response = client.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        closeConnection();
        return responseBody;
    }

    public String getDoc(String id) throws IOException {
        initConnection();
        Request request = new Request("GET", "/orders/_doc/" + id);
        Response response = client.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        closeConnection();
        return responseBody;
    }




}