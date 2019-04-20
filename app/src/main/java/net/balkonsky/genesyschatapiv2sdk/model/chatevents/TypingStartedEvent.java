package net.balkonsky.genesyschatapiv2sdk.model.chatevents;

import net.balkonsky.genesyschatapiv2sdk.model.ChatParticipantType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class TypingStartedEvent implements ChatEvent {
    private ChatParticipantType participantType;
    private String channel;
    private Boolean chatEnded;
    private Integer statusCode;
    private String secureKey;
    private String userId;
    private Long nextPosition;
    private List<MessageChatEvent.Messages> messages;

    @AllArgsConstructor
    @Data
    @EqualsAndHashCode
    @ToString
    public static class Messages {
        private Long participantId;
        private String fromNickname;
        private String fromType;
        private String index;
        private String text;
        private String messageType;
        private Long utcTime;

    }
}
