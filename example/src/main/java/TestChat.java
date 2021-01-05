import net.balkonsky.genesyschatapiv2sdk.genesysclient.ChatAPIv2Client;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.CometdTransport;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.EventListener;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.EventManager;
import net.balkonsky.genesyschatapiv2sdk.genesysclient.impl.ChatAPIv2ClientImpl;
import net.balkonsky.genesyschatapiv2sdk.httpclient.HttpTransportClient;
import net.balkonsky.genesyschatapiv2sdk.httpclient.HttpTransportClientImpl;
import net.balkonsky.genesyschatapiv2sdk.model.ChatParticipantType;
import net.balkonsky.genesyschatapiv2sdk.model.ChatState;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.ChatEvent;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.JoinedChatEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestChat {

    private ChatAPIv2Client chatAPIv2Client;

    public static void main(String[] args) {
        new TestChat().startTest();
    }

    private void startTest() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            EventManager eventManager = new EventManager();
            eventManager.subscribe(new ChatEventListener());
            HttpTransportClient httpTransportClient = new HttpTransportClientImpl(
                    "https://test-mapp-cimv2.g4lab.com",
                    500,
                    100,
                    1000
            );
            chatAPIv2Client = new ChatAPIv2ClientImpl(
                    eventManager,
                    "https://test-mapp-cimv2.g4lab.com",
                    "/service/chatV2/mobile-chat",
                    100L,
                    CometdTransport.LONGPOLLING,
                    httpTransportClient
            );
            chatAPIv2Client.openConnection();
        });
    }


    class ChatEventListener implements EventListener {
        @Override
        public void onEvent(ChatEvent chatEvent) {
            System.out.println(chatEvent);
            if (chatEvent instanceof JoinedChatEvent && ((JoinedChatEvent) chatEvent).getParticipantType().equals(ChatParticipantType.AGENT)) {
                chatAPIv2Client.sendMessage("i need help");
            }

        }

        @Override
        public void onState(ChatState chatState) {
            System.out.println(chatState);
            if (chatState == ChatState.CONNECT) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("push_notification_provider", "test");
                userData.put("push_notification_type", "customhttp");
                userData.put("UniqId", UUID.randomUUID().toString().toUpperCase());
                userData.put("cellPhone", "1789872");

                try {
                    chatAPIv2Client.openSession(
                            userData,
                            InetAddress.getLocalHost().getHostAddress(),
                            "test-subject");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            if (chatState == ChatState.OPENSESSION) {
                chatAPIv2Client.sendMessage("hello");
            }
        }
    }
}