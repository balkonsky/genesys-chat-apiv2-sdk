package net.balkonsky.genesyschatapiv2sdk.genesysclient.impl;

import com.google.gson.Gson;
import lombok.NonNull;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.ChatAPIv2Client;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.CometdTransport;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.EventManager;
import net.balkonsky.genesyschatapiv2sdk.httpclient.HttpTransportClient;
import net.balkonsky.genesyschatapiv2sdk.model.ChatParticipantType;
import net.balkonsky.genesyschatapiv2sdk.model.ChatState;
import net.balkonsky.genesyschatapiv2sdk.model.CometConnectResponse;
import net.balkonsky.genesyschatapiv2sdk.model.FileSendResponse;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.*;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.LongPollingTransport;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.File;
import java.util.*;

/**
 * Main object to interact with Genesys Mobile Services Chat APIv2
 * @author Maksim Avramenko
 * @version 0.9.1
 */

@Slf4j
public class ChatAPIv2ClientImpl implements ChatAPIv2Client {
    /**Bayeux chat listener interface field*/
    private ChatListener chatListener;
    /**Event manager to notify listeners filed*/
    private EventManager eventManager;
    /**Bayeux client filed*/
    private BayeuxClient client;
    /**GSON field to parse response in {@link ChatListener} */
    private Gson gson;

    /**Http transport field to send files in chat apiv2*/
    private HttpTransportClient transportClient;
    /**Cometd server host field*/
    private String cometdServerHost;
    /**Cometd channel field*/
    private String cometdChannel;
    /**Cometd connect to server timeout field*/
    private Long cometdConnectTimeout;
    /**Cometd transport type field*/
    private CometdTransport cometdTransport;
    /**SecureKey field*/
    private String secureKey;

    /**
     * Default constructor
     * @param eventManager to notify listeners of chat events
     * @param cometdServerHost hostname of cometd server
     * @param cometdChannel cometd channel name
     * @param cometdConnectTimeout timeout for connect to cometd server
     * @param cometdTransport enum type of cometd transport {@link CometdTransport}
     * @param transportClient http transport client {@link HttpTransportClient}
     */
    public ChatAPIv2ClientImpl(
            @NonNull EventManager eventManager,
            @NonNull String cometdServerHost,
            @NonNull String cometdChannel,
            @NonNull Long cometdConnectTimeout,
            @NonNull CometdTransport cometdTransport,
            @NonNull HttpTransportClient transportClient) {
        log.trace("create chat apiv2 client with params: cometd server host -  {},cometd channel - {}", cometdServerHost, cometdChannel);
        this.transportClient = transportClient;
        this.eventManager = eventManager;
        this.cometdServerHost = cometdServerHost + "/genesys/cometd";
        this.cometdChannel = cometdChannel;
        this.cometdConnectTimeout = cometdConnectTimeout;
        this.cometdTransport = cometdTransport;
        this.gson = new Gson();
        this.chatListener = new ChatListener();
    }

    /**
     * Method to open cometd connection to server.
     * This method send: comet handshake request, subscribe and connect request.
     */
    public void openConnection() {
        try {
            if (cometdTransport == CometdTransport.LONGPOLLING) {
                log.info("set transport - Long Polling client");
                HttpClient httpClient = new HttpClient();
                httpClient.start();
                client = new BayeuxClient(
                        cometdServerHost,
                        new LongPollingTransport(null, httpClient));
            } else {
                if (cometdTransport == CometdTransport.WEBSOCKET) {
                    log.info("set transport - Web Socket client");

                    WebSocketClientFactory wscf = new WebSocketClientFactory();
                    wscf.start();
                    client = new BayeuxClient(
                            cometdServerHost,
                            WebSocketTransport.create(null, wscf));
                } else {
                    log.info("not set transport in properties, set default Long Polling client");
                    HttpClient httpClient = new HttpClient();
                    httpClient.start();

                    client = new BayeuxClient(
                            cometdServerHost,
                            new LongPollingTransport(null, httpClient));

                }
            }
        } catch (Exception e) {
            log.error("error:", e);
        }

        client.getChannel(Channel.META_HANDSHAKE).addListener((ClientSessionChannel.MessageListener)
                (channel, message) -> {
                    if (message.isSuccessful()) {
                        client.batch(() -> {
                            ClientSessionChannel chatChannel = client.getChannel(cometdChannel);
                            chatChannel.subscribe(chatListener);
                            eventManager.notify(ChatState.CONNECTING);
                        });
                    } else {
                        eventManager.notify(ChatState.DISCONNECT);
                    }
                });

        client.getChannel(Channel.META_CONNECT).addListener((ClientSessionChannel.MessageListener)
                (channel, message) -> {
                    if (message.isSuccessful() && !client.isDisconnected()) {
                        log.info("client receive successful message {}", message);
                    } else {
                        log.error("connection to Server Closed");
                        eventManager.notify(ChatState.DISCONNECT);
                    }
                });

        client.handshake();
        if (client.waitFor(cometdConnectTimeout, BayeuxClient.State.CONNECTED)) {
            log.info("success handshake with server at {}", cometdConnectTimeout);
            eventManager.notify(ChatState.CONNECT);
        } else {
            log.info("could not handshake with server at {}", cometdServerHost);
            eventManager.notify(ChatState.DISCONNECT);
        }
    }

    /**
     * This method close cometd connection
     */
    public void closeConnection() {
        client.disconnect();
        eventManager.notify(ChatState.CLOSECONNECTION);
    }

    /**
     * This method open chat session by Chat APIv2
     * @param userdata specific data of chat session
     * @param nickname nickname of user
     * @param subject of chat session
     */
    public void openSession(Map<String, Object> userdata, String nickname, String subject) {
        try {
            eventManager.notify(ChatState.OPENNINGSESSION);
            Map<String, Object> data = new HashMap<>();
            data.put("operation", "requestChat");
            data.put("nickname", nickname);
            data.put("subject", subject);
            data.put("userData", userdata);
            client.getChannel(cometdChannel).publish(data);
        } catch (Exception e) {
            log.error("error:", e);
        }
    }

    /**
     * This method closing chat session. Cometd session remains open.
     */
    public void closeSession() {
        try {
            eventManager.notify(ChatState.CLOSINGSESSION);
            Map<String, Object> data = new HashMap<>();
            data.put("operation", "disconnect");
            data.put("secureKey", secureKey);
            client.getChannel(cometdChannel).publish(data);
            eventManager.notify(ChatState.CLOSESESSION);
        } catch (Exception e) {
            log.error("error: ", e);
        }
    }

    /**
     * This method send message in chat session from user
     * @param text of message
     */
    public void sendMessage(String text) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operation", "sendMessage");
            data.put("message", text);
            data.put("secureKey", secureKey);
            client.getChannel(cometdChannel).publish(data);
        } catch (Exception e) {
            log.error("error: ", e);
        }
    }

    /**
     * This method send command to indicate that the client has started typing a message.
     * You can include a partial message, if desired, for typing preview.
     * @param text of message
     */
    public void startUserTyping(String text) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "startTyping");
        data.put("message", text);
        data.put("secureKey", secureKey);
        client.getChannel(cometdChannel).publish(data);
    }

    /**
     * This method send command to indicate that the client has stopped typing a message.
     * You can include a partial message, if desired, for typing preview.
     * @param text of message
     */
    public void stopUserTyping(String text) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "stopTyping");
        data.put("message", text);
        data.put("secureKey", secureKey);
        client.getChannel(cometdChannel).publish(data);
    }

    /**
     * This method send request notifications to be delivered for the existing chat session, after the CometD channel has been disconnected
     * @param transcriptposition transcript event position from which the client application would like to receive the previous events. If you set this option to 0 or if you don't set this option, the client will receive all the events.
     */
    public void requestNotifications(String transcriptposition) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "requestNotifications");
        data.put("secureKey", secureKey);
        data.put("transcriptPosition", transcriptposition);
        client.getChannel(cometdChannel).publish(data);
    }

    public void requestNotifications() {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "requestNotifications");
        data.put("secureKey", secureKey);
        client.getChannel(cometdChannel).publish(data);
    }

    /**
     * This method send request to upload a file into a chat session
     * @param file to upload {@link File}
     * @return optional deserialize response from server object
     */
    public Optional<FileSendResponse> sendFile(File file) {
        Optional<String> result = transportClient.post("/genesys/2/chat-ntf", secureKey, file);
        if (result.isPresent()) {
            FileSendResponse fileSendResponse = gson.fromJson(result.get(), FileSendResponse.class);
            eventManager.notify(new FileSendEvent(
                    fileSendResponse.getChatEnded(),
                    fileSendResponse.getStatusCode(),
                    fileSendResponse.getSecureKey(),
                    new FileSendEvent.UserData(fileSendResponse.getUserData().getFileid())
            ));
            return Optional.of(fileSendResponse);
        }
        return Optional.empty();
    }

    /**
     * This method send request to avoid wasting network and CPU overhead by checking for allowable file types or maximum file size—or other constraints on file uploads—before sending an upload request.
     * @return optional deserialize response limits object
     */
    public Optional<CometConnectResponse.Data> fileGetLimits() {
        try {
            Optional<String> result = transportClient.post("/genesys/2/chat-ntf", secureKey);
            if (result.isPresent()) {
                CometConnectResponse.Data data = gson.fromJson(result.get(), CometConnectResponse.Data.class);
                List<FileGetLimitsEvent.Messages> eventMessages = new LinkedList<>();
                data.getMessages().forEach(x -> eventMessages.add(
                        new FileGetLimitsEvent.Messages(
                                x.getFrom().getParticipantId(),
                                x.getFrom().getNickname(),
                                x.getFrom().getType(),
                                x.getIndex(),
                                x.getText(),
                                x.getType(),
                                x.getUtcTime(),
                                new FileGetLimitsEvent.UserData(
                                        x.getUserData().getDownloadattempts(),
                                        x.getUserData().getUploadmaxfiles(),
                                        x.getUserData().getDeletefile(),
                                        x.getUserData().getUploadmaxfilesize(),
                                        x.getUserData().getUseddownloadattempts(),
                                        x.getUserData().getUseduploadmaxtotalsize(),
                                        x.getUserData().getUploadneedagent(),
                                        x.getUserData().getUseduploadmaxfiles(),
                                        x.getUserData().getUploadmaxtotalsize(),
                                        x.getUserData().getUploadfiletypes()
                                )
                        )));
                FileGetLimitsEvent fileGetLimitsEvent = new FileGetLimitsEvent(
                        data.getChatEnded(),
                        data.getStatusCode(),
                        eventMessages
                );
                eventManager.notify(fileGetLimitsEvent);
                return Optional.of(data);
            }
        } catch (Exception e) {
            log.error("error:", e);
            eventManager.notify(new ChatErrorEvent(e.getMessage(), e.getMessage()));
            eventManager.notify(ChatState.DISCONNECT);
        }
        return Optional.empty();
    }

    /**
     * This method send a custom notice to the agent. The agent will need a customized desktop that can process this notice.
     * @param text of message
     */
    public void sendCustomNotice(String text) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operation", "customNotice");
            data.put("message", text);
            data.put("secureKey", secureKey);
            client.getChannel(cometdChannel).publish(data);
        } catch (Exception e) {
            log.error("error:", e);
            eventManager.notify(new ChatErrorEvent(e.getMessage(), e.getMessage()));
            eventManager.notify(ChatState.DISCONNECT);
        }
    }

    private class ChatListener implements ClientSessionChannel.MessageListener {
        @Override
        public synchronized void onMessage(ClientSessionChannel channel, Message message) {
            try {
                CometConnectResponse cometConnectResponse = gson.fromJson(message.getJSON(), CometConnectResponse.class);
                CometConnectResponse.Data data = cometConnectResponse.getData();
                if (data != null) {
                    log.debug("get response from Server: {}", cometConnectResponse);
                    if (secureKey == null && data.getSecureKey() != null) {
                        secureKey = data.getSecureKey();
                        log.debug("secureKey is null, set {}", secureKey);
                        eventManager.notify(ChatState.OPENSESSION);
                    }
                    if (data.getChatEnded()) {
                        eventManager.notify(ChatState.CLOSESESSION);
                    }

                    for (CometConnectResponse.Messages messages : data.getMessages()) {
                        if (messages.getType().equalsIgnoreCase("ParticipantJoined")) {
                            switch (messages.getFrom().getType()) {
                                case ("Agent"): {
                                    eventManager.notify(new JoinedChatEvent(
                                            ChatParticipantType.AGENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                                case ("Client"): {
                                    eventManager.notify(new JoinedChatEvent(
                                            ChatParticipantType.CLIENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                                case ("Supervisor"): {
                                    eventManager.notify(new JoinedChatEvent(
                                            ChatParticipantType.SUPERVISOR,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                                case ("External"): {
                                    eventManager.notify(new JoinedChatEvent(
                                            ChatParticipantType.EXTERNAL,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                            }
                        }
                        if (messages.getType().equalsIgnoreCase("ParticipantLeft")) {
                            switch (messages.getFrom().getType()) {
                                case ("Agent"): {
                                    eventManager.notify(new LeftChatEvent(
                                            ChatParticipantType.AGENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                                case ("Client"): {
                                    eventManager.notify(new LeftChatEvent(
                                            ChatParticipantType.CLIENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                                case ("Supervisor"): {
                                    eventManager.notify(new LeftChatEvent(
                                            ChatParticipantType.SUPERVISOR,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                                case ("External"): {
                                    eventManager.notify(new LeftChatEvent(
                                            ChatParticipantType.EXTERNAL,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition()
                                    ));
                                    break;
                                }
                            }
                        }

                        if (messages.getType().equalsIgnoreCase("Message")) {
                            List<MessageChatEvent.Messages> eventMessages = new LinkedList<>();
                            cometConnectResponse.getData().getMessages().forEach(x -> eventMessages.add(
                                    new MessageChatEvent.Messages(
                                            x.getFrom().getParticipantId(),
                                            x.getFrom().getNickname(),
                                            x.getFrom().getType(),
                                            x.getIndex(),
                                            x.getText(),
                                            x.getType(),
                                            x.getUtcTime()
                                    )));
                            switch (messages.getFrom().getType()) {
                                case ("Agent"): {
                                    eventManager.notify(new MessageChatEvent(
                                            ChatParticipantType.AGENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Client"): {
                                    eventManager.notify(new MessageChatEvent(
                                            ChatParticipantType.CLIENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Supervisor"): {
                                    eventManager.notify(new MessageChatEvent(
                                            ChatParticipantType.SUPERVISOR,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("External"): {
                                    eventManager.notify(new MessageChatEvent(
                                            ChatParticipantType.EXTERNAL,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                            }
                        }
                        if (messages.getType().equalsIgnoreCase("CustomNotice")) {
                            List<MessageChatEvent.Messages> eventMessages = new LinkedList<>();
                            cometConnectResponse.getData().getMessages().forEach(x -> eventMessages.add(
                                    new MessageChatEvent.Messages(
                                            x.getFrom().getParticipantId(),
                                            x.getFrom().getNickname(),
                                            x.getFrom().getType(),
                                            x.getIndex(),
                                            x.getText(),
                                            x.getType(),
                                            x.getUtcTime()
                                    )));
                            switch (messages.getFrom().getType()) {
                                case ("Agent"): {
                                    eventManager.notify(new CustomNoticeEvent(
                                            ChatParticipantType.AGENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Client"): {
                                    eventManager.notify(new CustomNoticeEvent(
                                            ChatParticipantType.CLIENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Supervisor"): {
                                    eventManager.notify(new CustomNoticeEvent(
                                            ChatParticipantType.SUPERVISOR,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("External"): {
                                    eventManager.notify(new CustomNoticeEvent(
                                            ChatParticipantType.EXTERNAL,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                            }
                        }
                        if (messages.getType().equalsIgnoreCase("TypingStarted")) {
                            List<MessageChatEvent.Messages> eventMessages = new LinkedList<>();
                            cometConnectResponse.getData().getMessages().forEach(x -> eventMessages.add(
                                    new MessageChatEvent.Messages(
                                            x.getFrom().getParticipantId(),
                                            x.getFrom().getNickname(),
                                            x.getFrom().getType(),
                                            x.getIndex(),
                                            x.getText(),
                                            x.getType(),
                                            x.getUtcTime()
                                    )));
                            switch (messages.getFrom().getType()) {
                                case ("Agent"): {
                                    eventManager.notify(new TypingStartedEvent(
                                            ChatParticipantType.AGENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Client"): {
                                    eventManager.notify(new TypingStartedEvent(
                                            ChatParticipantType.CLIENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Supervisor"): {
                                    eventManager.notify(new TypingStartedEvent(
                                            ChatParticipantType.SUPERVISOR,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("External"): {
                                    eventManager.notify(new TypingStartedEvent(
                                            ChatParticipantType.EXTERNAL,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                            }
                        }
                        if (messages.getType().equalsIgnoreCase("TypingStopped")) {
                            List<MessageChatEvent.Messages> eventMessages = new LinkedList<>();
                            cometConnectResponse.getData().getMessages().forEach(x -> eventMessages.add(
                                    new MessageChatEvent.Messages(
                                            x.getFrom().getParticipantId(),
                                            x.getFrom().getNickname(),
                                            x.getFrom().getType(),
                                            x.getIndex(),
                                            x.getText(),
                                            x.getType(),
                                            x.getUtcTime()
                                    )));
                            switch (messages.getFrom().getType()) {
                                case ("Agent"): {
                                    eventManager.notify(new TypingStoppedEvent(
                                            ChatParticipantType.AGENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Client"): {
                                    eventManager.notify(new TypingStoppedEvent(
                                            ChatParticipantType.CLIENT,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("Supervisor"): {
                                    eventManager.notify(new TypingStoppedEvent(
                                            ChatParticipantType.SUPERVISOR,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                                case ("External"): {
                                    eventManager.notify(new TypingStoppedEvent(
                                            ChatParticipantType.EXTERNAL,
                                            cometConnectResponse.getChannel(),
                                            data.getChatEnded(),
                                            data.getStatusCode(),
                                            data.getSecureKey(),
                                            data.getUserId(),
                                            data.getNextPosition(),
                                            eventMessages
                                    ));
                                    break;
                                }
                            }
                        }
                    }

                } else {
                    log.error("receive an error response {}, disconnect from chat session", message);
                    eventManager.notify(new ChatErrorEvent(cometConnectResponse.getError(), cometConnectResponse.getError()));
                    eventManager.notify(ChatState.DISCONNECT);
                }
            } catch (Exception e) {
                log.error("error:", e);
                eventManager.notify(new ChatErrorEvent(e.getMessage(), e.getMessage()));
                eventManager.notify(ChatState.DISCONNECT);
            }
        }
    }
}
