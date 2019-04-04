package net.balkonsky.genesyschatapiv2sdk.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void getCometdServerHostTest() {
        String cometdServerHost = Config.instance().getCometdServerHost();
        assertEquals(cometdServerHost,"https://pre-mapp-cimv2.yota.ru/genesys/cometd");
    }

    @Test
    public void isHttpProxyEnabledTest() {
        Boolean httpProxyEnabled = Config.instance().isHttpProxyEnabled();
        assertEquals(httpProxyEnabled,false);
    }

    @Test
    public void getHttProxyHostTest() {
        String httProxyHost = Config.instance().getHttProxyHost();
        assertEquals(httProxyHost,"https://pre-mapp-cimv2.yota.ru");
    }

    @Test
    public void getConnectTimeoutTest() {
        Integer connectTimeout = Config.instance().getConnectTimeout();
        Long truevalue = 100000L;
        assertEquals(connectTimeout,truevalue);
    }

    @Test
    public void getConnectionRequestTimeoutTest() {
        Integer connectionRequestTimeout = Config.instance().getConnectionRequestTimeout();
        Long truevalue = 100000L;
        assertEquals(connectionRequestTimeout,truevalue);
    }

    @Test
    public void getSocketTimeoutTest() {
        Integer socketTimeout = Config.instance().getSocketTimeout();
        Long truevalue = 5000L;
        assertEquals(socketTimeout,truevalue);
    }

    @Test
    public void getHttpHostTest() {
        String httpHost = Config.instance().getHttpHost();
        assertEquals(httpHost,"https://pre-mapp-cimv2.yota.ru");
    }

    @Test
    public void getCometdChannelTest() {
        String cometdChannel = Config.instance().getCometdChannel();
        assertEquals(cometdChannel,"/service/chatV2/mobile-chat");
    }

    @Test
    public void getCometdTransportTest(){
        String cometdTransport = Config.instance().getCometdTransport();
        assertEquals(cometdTransport,"longpolling");

    }
}