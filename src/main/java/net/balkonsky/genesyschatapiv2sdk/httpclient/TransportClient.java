package net.balkonsky.genesyschatapiv2sdk.httpclient;

import net.balkonsky.genesyschatapiv2sdk.model.HttpResponse;

import java.io.File;

public interface TransportClient {
    HttpResponse get(String url);

    HttpResponse post(String url, String body, String secureKey, String contentType);

    HttpResponse post(String url, String secureKey, File file);
}
