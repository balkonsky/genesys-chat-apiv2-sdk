package net.balkonsky.genesyschatapiv2sdk.httpclient;

import java.io.File;
import java.util.Optional;

public interface HttpTransportClient {
    Optional<String> get(String url);

    Optional<String> post(String url, String secureKey);

    Optional<String> post(String url, String secureKey, File file);
}
