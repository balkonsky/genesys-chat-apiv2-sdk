package net.balkonsky.genesyschatapiv2sdk.model.chatevents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class ChatErrorEvent implements ChatEvent {
    private String errorCode;
    private String description;

}
