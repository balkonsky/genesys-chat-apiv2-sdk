package net.balkonsky.genesyschatapiv2sdk.httpclient;

import lombok.extern.slf4j.Slf4j;
import net.balkonsky.genesyschatapiv2sdk.utils.Config;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
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
import java.util.Optional;

@Slf4j
public class HttpTransportClientImpl implements HttpTransportClient {
    private static RequestConfig requestConfig;
    private static CookieStore cookieStore;
    private static SSLConnectionSocketFactory sslsf;

    public HttpTransportClientImpl() {
        try {
            if (requestConfig == null) {
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
                log.debug("creating config for Apache Http Client");
                requestConfig = RequestConfig.custom()
                        .setConnectTimeout(Config.instance().getConnectTimeout())
                        .setConnectionRequestTimeout(Config.instance().getConnectionRequestTimeout())
                        .setSocketTimeout(Config.instance().getSocketTimeout())
                        .build();
                SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
                sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), (hostName, sslSession) -> true);
            }
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
        CloseableHttpClient closeableHttpClient;
        if (!Config.instance().isHttpProxyEnabled()) {
            closeableHttpClient = HttpClientBuilder
                    .create()
                    .setSSLSocketFactory(sslsf)
                    .setDefaultCookieStore(cookieStore)
                    .build();
        } else {
            HttpHost proxyHost = new HttpHost(Config.instance().getHttProxyHost());
            closeableHttpClient = HttpClientBuilder
                    .create()
                    .setSSLSocketFactory(sslsf)
                    .setDefaultCookieStore(cookieStore)
                    .setProxy(proxyHost)
                    .build();
        }

        StringBuilder uri = new StringBuilder();
        uri.append(Config.instance().getHttpHost()).append(url);
        log.info("URL request {}", uri);
        try {
            HttpPost httpPost = new HttpPost(uri.toString());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addTextBody("operation", "fileGetLimits", ContentType.DEFAULT_BINARY);
            builder.addTextBody("secureKey", secureKey, ContentType.DEFAULT_BINARY);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return getResponseContent(closeableHttpResponse, httpEntity);
        } catch (Exception e) {
            log.error("error:", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> post(String url, String secureKey, File file) {
        log.debug("creating Apache Http Client...");
        CloseableHttpClient closeableHttpClient;
        if (!Config.instance().isHttpProxyEnabled()) {
            closeableHttpClient = HttpClientBuilder
                    .create()
                    .setSSLSocketFactory(sslsf)
                    .setDefaultCookieStore(cookieStore)
                    .build();
        } else {
            HttpHost proxyHost = new HttpHost(Config.instance().getHttProxyHost());
            closeableHttpClient = HttpClientBuilder
                    .create()
                    .setSSLSocketFactory(sslsf)
                    .setDefaultCookieStore(cookieStore)
                    .setProxy(proxyHost)
                    .build();
        }

        String uri = Config.instance().getHttpHost() + url;
        log.info("URL request {}", uri);
        try {
            HttpPost httpPost = new HttpPost(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(Charset.forName("UTF-8"));
            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
            builder.addTextBody("secureKey", secureKey, ContentType.DEFAULT_BINARY);
            builder.addTextBody("operation", "fileUpload", ContentType.DEFAULT_BINARY);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return getResponseContent(closeableHttpResponse, httpEntity);
        } catch (Exception e) {
            log.error("error:", e);
        }
        return Optional.empty();
    }

    private Optional<String> getResponseContent(CloseableHttpResponse closeableHttpResponse, HttpEntity httpEntity) {
        StringBuilder response = new StringBuilder();
        try {
            if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {
                if (httpEntity != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        response.append("\n");
                    }
                    log.info("\n"
                            + "-----------------------------------------------------------------"
                            + "\n"
                            + "received a successful response: {} ", closeableHttpResponse.getStatusLine()
                            + "\n"
                            + "response: "
                            + response
                            + "-----------------------------------------------------------------");
                }
            } else {
                if (httpEntity != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        response.append("\n");
                    }
                }
                log.error("\n"
                        + "-----------------------------------------------------------------"
                        + "\n"
                        + "received an unsuccessful response from the server: {} ", closeableHttpResponse.getStatusLine()
                        + "\n"
                        + "response: "
                        + response
                        + "-----------------------------------------------------------------");
            }
        } catch (Exception e) {
            log.error("error with get http response content", e);
        }
        return Optional.of(response.toString());
    }
}
