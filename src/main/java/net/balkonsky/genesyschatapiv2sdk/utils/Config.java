package net.balkonsky.genesyschatapiv2sdk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Config {
    private static Properties properties;
    private static Config config;
    private static final String filename = "/app.properties";

    private Config() {
    }

    public static Config instance() {
        if (config == null) {
            config = new Config();
            properties = new Properties();
        }
        return config;
    }

    public String getCometdServerHost() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            String cometdServerHost = properties.getProperty("CometdServerHost");
            log.debug("return CometdServerHost = {} from config file", cometdServerHost);
            return cometdServerHost;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("CometdServerHost is null");
    }

    public Boolean isHttpProxyEnabled() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            Boolean isHttpProxyEnabled = Boolean.valueOf(properties.getProperty("HttpProxyEnabled"));
            log.debug("return HttpProxyEnabled = {} from config file", isHttpProxyEnabled);
            return isHttpProxyEnabled;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("HttpProxyEnabled is null");
    }

    public String getHttProxyHost() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            String httProxyHost = properties.getProperty("HttProxyHost");
            log.debug("return HttProxyHost = {} from config file", httProxyHost);
            return httProxyHost;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("HttProxyHost is null");
    }

    public Integer getConnectTimeout() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            Integer connectTimeout = Integer.valueOf(properties.getProperty("ConnectTimeout"));
            log.debug("return ConnectTimeout = {} from config file", connectTimeout);
            return connectTimeout;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("ConnectTimeout is null");
    }

    public Integer getConnectionRequestTimeout() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            Integer connectionRequestTimeout = Integer.valueOf(properties.getProperty("ConnectionRequestTimeout"));
            log.debug("return ConnectionRequestTimeout = {} from config file", connectionRequestTimeout);
            return connectionRequestTimeout;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("ConnectionRequestTimeout is null");
    }

    public Integer getSocketTimeout() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            Integer socketTimeout = Integer.valueOf(properties.getProperty("SocketTimeout"));
            log.debug("return SocketTimeout = {} from config file", socketTimeout);
            return socketTimeout;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("SocketTimeout is null");
    }

    public String getHttpHost() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            String httpHost = properties.getProperty("HttpHost");
            log.debug("return HttpHost = {} from config file", httpHost);
            return httpHost;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("HttpHost is null");
    }

    public String getCometdChannel() {
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            String cometdChannel = properties.getProperty("CometdChannel");
            log.debug("return CometdChannel = {} from config file", cometdChannel);
            return cometdChannel;
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("CometdChannel is null");
    }

    public String getCometdTransport(){
        try (final InputStream fis = this.getClass().getResourceAsStream(filename)) {
            properties.load(fis);
            String cometdTransport = properties.getProperty("CometdTransport");
            log.debug("return CometdTransport = {} from config file", cometdTransport);
            return cometdTransport.toLowerCase();
        } catch (Exception e) {
            log.error("error,", e);
        }
        throw new NullPointerException("CometdChannel is null");
    }


}
