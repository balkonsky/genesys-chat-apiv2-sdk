package net.balkonsky.genesyschatapiv2sdk.client.impl;

import com.google.gson.Gson;
import net.balkonsky.genesyschatapiv2sdk.client.ChatAPIv2;
import net.balkonsky.genesyschatapiv2sdk.client.EventManager;
import net.balkonsky.genesyschatapiv2sdk.httpclient.TransportClient;
import net.balkonsky.genesyschatapiv2sdk.httpclient.TransportClientImpl;
import net.balkonsky.genesyschatapiv2sdk.model.ChatParticipantType;
import net.balkonsky.genesyschatapiv2sdk.model.ChatState;
import net.balkonsky.genesyschatapiv2sdk.model.CometConnectResponse;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.*;
import net.balkonsky.genesyschatapiv2sdk.utils.Config;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ChatAPIv2Impl implements ChatAPIv2 {
    private ChatListener chatListener;
    private EventManager eventManager;
    private BayeuxClient client;
    private Gson gson;
    private TransportClient transportclient;

    private String secureKey;

    public ChatAPIv2Impl(EventManager eventManager) {
        this.gson = new Gson();
        this.transportclient = new TransportClientImpl();
        this.eventManager = eventManager;
        this.chatListener = new ChatListener();
    }

    public void openConnection() {
        try {
            switch (Config.instance().getCometdTransport()) {
                case ("longpolling"): {
                    log.info("set transport - Long Polling client");
                    HttpClient httpClient = new HttpClient();
                    httpClient.start();
                    client = new BayeuxClient(
                            Config.instance().getCometdServerHost(),
                            new LongPollingTransport(null, httpClient));
                    break;
                }
                case ("websocket"): {
                    log.info("set transport - Web Socket client");
                    WebSocketClientFactory wscf = new WebSocketClientFactory();
                    wscf.start();
                    client = new BayeuxClient(
                            Config.instance().getCometdServerHost(),
                            WebSocketTransport.create(null, wscf));
                    break;
                }
                default: {
                    log.info("not set transport in properties, set default Long Polling client");
                    HttpClient httpClient = new HttpClient();
                    httpClient.start();

                    client = new BayeuxClient(
                            Config.instance().getCometdServerHost(),
                            new LongPollingTransport(null, httpClient));
                    break;
                }
            }
        } catch (Exception e) {
            log.error("error:", e);
        }

        client.getChannel(Channel.META_HANDSHAKE).addListener((ClientSessionChannel.MessageListener)
                (channel, message) -> {
                    if (message.isSuccessful()) {
                        client.batch(() -> {
                            ClientSessionChannel chatChannel = client.getChannel(Config.instance().getCometdChannel());
                            chatChannel.subscribe(chatListener);
                            eventManager.notify(ChatState.CONNECTING);
                        });
                    } else {
                        closeConnection();
                    }
                });

        client.getChannel(Channel.META_CONNECT).addListener((ClientSessionChannel.MessageListener)
                (channel, message) -> {
                    if (message.isSuccessful() && !client.isDisconnected()) {
                        log.info("Client receive successful message {}", message);
                    } else {
                        log.error("Connection to Server Closed");
                        closeConnection();
                    }
                });

        client.handshake();
        if (client.waitFor(Config.instance().getConnectTimeout(), BayeuxClient.State.CONNECTED)) {
            log.info("Success handshake with server at {}", Config.instance().getCometdServerHost());
            eventManager.notify(ChatState.CONNECT);
        } else {
            log.info("Could not handshake with server at {}", Config.instance().getCometdServerHost());
            closeConnection();
        }
    }

    public void closeConnection() {
        client.disconnect();
        eventManager.notify(ChatState.CLOSECONNECTION);
    }

    public void openSession(Map<String, Object> userdata, String nickname, String subject) {
        try {
            eventManager.notify(ChatState.OPENNINGSESSION);
            Map<String, Object> data = new HashMap<>();
            data.put("operation", "requestChat");
            data.put("nickname", nickname);
            data.put("subject", subject);
            data.put("userData", userdata);
            client.getChannel(Config.instance().getCometdChannel()).publish(data);
        } catch (Exception e) {
            log.error("error:", e);
        }
    }

    public void closeSession() {
        //TODO
        eventManager.notify(ChatState.CLOSESESSION);
    }

    public void sendMessage(String text) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operation", "sendMessage");
            data.put("message", text);
            data.put("secureKey", secureKey);
            client.getChannel(Config.instance().getCometdChannel()).publish(data);
        } catch (Exception e) {
            log.error("error: ", e);
        }
    }

    public void startUserTyping(String text) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "startTyping");
        data.put("message", text);
        data.put("secureKey", secureKey);
        client.getChannel(Config.instance().getCometdChannel()).publish(data);
    }

    public void stopUserTyping(String text) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "stopTyping");
        data.put("message", text);
        data.put("secureKey", secureKey);
        client.getChannel(Config.instance().getCometdChannel()).publish(data);
    }

    public void requestNotifications(String transcriptposition) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "requestNotifications");
        data.put("secureKey", secureKey);
        data.put("transcriptPosition", transcriptposition);
        client.getChannel(Config.instance().getCometdChannel()).publish(data);
    }

    public void sendFile(File file) {
        //TODO
//        transportclient.post("/genesys/2/chat-ntf", secureKey, file);
    }

    public void fileGetLimits() {
        //TODO
//        transportclient.post(secureKey, "/genesys/2/chat-ntf");
    }

    public void sendCustomNotice(String text) {
        Map<String, Object> data = new HashMap<>();
        data.put("operation", "customNotice");
        data.put("message", text);
        data.put("secureKey", secureKey);
        client.getChannel(Config.instance().getCometdChannel()).publish(data);
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
                        closeConnection();
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
                    eventManager.notify(new ChatErrorEvent());
                    closeConnection();
                }
            } catch (
                    Exception e)

            {
                log.error("error:", e);
            }
        }
    }
}
