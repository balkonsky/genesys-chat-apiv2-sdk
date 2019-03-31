package net.balkonsky.genesyschatapiv2sdk.client;

import java.io.File;
import java.util.Map;

public interface ChatAPIv2 {
    void openConnection();

    void closeConnection();

    void openSession(Map<String, Object> userData, String nickname, String subject);

    void closeSession();

    void sendMessage(String text);

    void startUserTyping(String text);

    void stopUserTyping(String text);

    void requestNotifications(String transcriptposition);

    void sendFile(File file);

    void fileGetLimits();

    void sendCustomNotice(String text);
}
