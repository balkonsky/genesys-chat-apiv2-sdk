package net.balkonsky.genesyschatapiv2sdk.httpclient;

import net.balkonsky.genesyschatapiv2sdk.model.HttpResponse;

import java.io.File;

public class TransportClientImpl implements TransportClient {
    @Override
    public HttpResponse get(String url) {
        //TODO
        return null;
    }

    @Override
    public HttpResponse post(String url, String body, String secureKey, String contentType) {
        //TODO
        return null;
    }

    @Override
    public HttpResponse post(String url, String secureKey, File file) {
        //TODO
        return null;
    }
}
