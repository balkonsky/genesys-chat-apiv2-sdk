package net.balkonsky.genesyschatapiv2sdk.model.chatevents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class FileSendEvent implements ChatEvent {
    private Boolean chatEnded;
    private Integer statusCode;
    private String secureKey;
    private UserData userData;

    @AllArgsConstructor
    @Data
    @EqualsAndHashCode
    @ToString
    public static class UserData {
        private String fileid;
    }
}
