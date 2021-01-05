package net.balkonsky.genesyschatapiv2sdk.model.chatevents;

import net.balkonsky.genesyschatapiv2sdk.model.ChatParticipantType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class LeftChatEvent implements ChatEvent {
    private ChatParticipantType participantType;
    private String channel;
    private Boolean chatEnded;
    private Integer statusCode;
    private String secureKey;
    private String userId;
    private Long nextPosition;
}
