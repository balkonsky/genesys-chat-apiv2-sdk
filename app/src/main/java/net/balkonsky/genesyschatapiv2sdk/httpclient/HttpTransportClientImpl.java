package net.balkonsky.genesyschatapiv2sdk.httpclient;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class HttpTransportClientImpl implements HttpTransportClient {
    private RequestConfig requestConfig;
    private CookieStore cookieStore;
    private SSLConnectionSocketFactory sslsf;
    private CloseableHttpClient closeableHttpClient;

    private String hostname;


    public HttpTransportClientImpl(
            @NonNull String hostname,
            @NonNull Integer socketTimeout,
            @NonNull Integer connectionRequestTimeout,
            @NonNull Integer connectTimeout) {
        try {
            this.hostname = hostname;
            log.debug("creating config for Apache Http Client");
            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setSocketTimeout(socketTimeout)
                    .build();
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), (hostName, sslSession) -> true);
        } catch (Exception e) {
            log.error("error with creating apache http client", e);
        }
    }

    @Override
    public Optional<String> get(String url) {
        //TODO
        return Optional.empty();
    }

    @Override
    public Optional<String> post(String url, String secureKey) {

        closeableHttpClient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(sslsf)
                .setDefaultCookieStore(cookieStore)
                .build();

        StringBuilder uri = new StringBuilder();
        uri.append(hostname).append(url);
        log.info("URL request {}", uri);
        try {
            HttpPost httpPost = new HttpPost(uri.toString());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addTextBody("secureKey", secureKey, ContentType.DEFAULT_BINARY);
            builder.addTextBody("operation", "fileGetLimits", ContentType.DEFAULT_BINARY);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            return getResponseContent(closeableHttpResponse);
        } catch (Exception e) {
            log.error("error:", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> post(String url, String secureKey, File file) {
        log.debug("creating Apache Http Client...");
        closeableHttpClient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(sslsf)
                .setDefaultCookieStore(cookieStore)
                .build();

        StringBuilder uri = new StringBuilder();
        uri.append(hostname).append(url);
        log.info("URL request {}", uri);
        try {
            HttpPost httpPost = new HttpPost(uri.toString());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(StandardCharsets.UTF_8);
            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
            builder.addTextBody("secureKey", secureKey, ContentType.DEFAULT_BINARY);
            builder.addTextBody("operation", "fileUpload", ContentType.DEFAULT_BINARY);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            return getResponseContent(closeableHttpResponse);
        } catch (Exception e) {
            log.error("error:", e);
        }
        return Optional.empty();
    }

    private Optional<String> getResponseContent(CloseableHttpResponse closeableHttpResponse) {
        StringBuilder response = new StringBuilder();
        try {
            if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = closeableHttpResponse.getEntity();
                if (httpEntity != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        response.append("\n");
                    }

                    log.info(new StringBuilder()
                            .append( "\n")
                            .append("-----------------------------------------------------------------")
                            .append("\n")
                            .append("received a successful response: {} ")
                            .append(closeableHttpResponse.getStatusLine())
                            .append("\n")
                            .append("response: ")
                            .append(response)
                            .append("-----------------------------------------------------------------").toString());
                }
            } else {
                HttpEntity httpEntity = closeableHttpResponse.getEntity();
                if (httpEntity != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        response.append("\n");
                    }
                }
                log.error(new StringBuilder()
                        .append("\n")
                        .append("-----------------------------------------------------------------")
                        .append("\n")
                        .append("received an unsuccessful response from the server: {} ")
                        .append(closeableHttpResponse.getStatusLine())
                        .append("\n")
                        .append("response: ")
                        .append(response)
                        .append("-----------------------------------------------------------------").toString());
            }
        } catch (Exception e) {
            log.error("error with get http response content", e);
        }
        return Optional.of(response.toString());
    }
}
