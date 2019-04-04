package net.balkonsky.genesyschatapiv2sdk.httpclient;

import net.balkonsky.genesyschatapiv2sdk.model.CometConnectResponse;
import net.balkonsky.genesyschatapiv2sdk.model.HttpResponse;

import java.io.File;
import java.util.Optional;

public interface HttpTransportClient {
    Optional<StringBuilder> get(String url);

    Optional<StringBuilder> post(String url, String secureKey);

    Optional<StringBuilder> post(String url, String secureKey, File file);
}
