package net.balkonsky.genesyschatapiv2sdk.genesysclient;

import net.balkonsky.genesyschatapiv2sdk.model.CometConnectResponse;
import net.balkonsky.genesyschatapiv2sdk.model.FileSendResponse;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.FileGetLimitsEvent;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.FileSendEvent;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public interface ChatAPIv2Client {
    void openConnection();

    void closeConnection();

    void openSession(Map<String, Object> userData, String nickname, String subject);

    void closeSession();

    void sendMessage(String text);

    void startUserTyping(String text);

    void stopUserTyping(String text);

    void requestNotifications(String transcriptposition);

    Optional<FileSendResponse> sendFile(File file);

    Optional<CometConnectResponse.Data> fileGetLimits();

    void sendCustomNotice(String text);
}
