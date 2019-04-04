package net.balkonsky.genesyschatapiv2sdk.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void getCometdServerHostTest() {
        String cometdServerHost = Config.instance().getCometdServerHost();
        assertEquals(cometdServerHost,"http://10.2.136.250:8091/genesys/cometd");
    }

    @Test
    public void isHttpProxyEnabledTest() {
        Boolean httpProxyEnabled = Config.instance().isHttpProxyEnabled();
        assertEquals(httpProxyEnabled,false);
    }

    @Test
    public void getHttProxyHostTest() {
        String httProxyHost = Config.instance().getHttProxyHost();
        assertEquals(httProxyHost,"https://pre-site-cimv2.yota.ru");
    }

    @Test
    public void getConnectTimeoutTest() {
        Integer connectTimeout = Config.instance().getConnectTimeout();
        Integer truevalue = 1000;
        assertEquals(connectTimeout,truevalue);
    }

    @Test
    public void getConnectionRequestTimeoutTest() {
        Integer connectionRequestTimeout = Config.instance().getConnectionRequestTimeout();
        Integer truevalue = 1000;
        assertEquals(connectionRequestTimeout,truevalue);
    }

    @Test
    public void getSocketTimeoutTest() {
        Integer socketTimeout = Config.instance().getSocketTimeout();
        Integer truevalue = 5000;
        assertEquals(socketTimeout,truevalue);
    }

    @Test
    public void getHttpHostTest() {
        String httpHost = Config.instance().getHttpHost();
        assertEquals(httpHost,"https://pre-site-cimv2.yota.ru");
    }

    @Test
    public void getCometdChannelTest() {
        String cometdChannel = Config.instance().getCometdChannel();
        assertEquals(cometdChannel,"/service/chatV2/web-chat");
    }

    @Test
    public void getCometdTransportTest(){
        String cometdTransport = Config.instance().getCometdTransport();
        assertEquals(cometdTransport,"websocket");

    }
}