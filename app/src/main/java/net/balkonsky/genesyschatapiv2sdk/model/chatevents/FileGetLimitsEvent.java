package net.balkonsky.genesyschatapiv2sdk.model.chatevents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.balkonsky.genesyschatapiv2sdk.model.ChatParticipantType;

import java.util.List;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class FileGetLimitsEvent implements ChatEvent {
    private Boolean chatEnded;
    private Integer statusCode;
    private List<FileGetLimitsEvent.Messages> messages;

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
        private UserData userData;
    }

    @AllArgsConstructor
    @Data
    @EqualsAndHashCode
    @ToString
    public static class UserData {
        private String downloadattempts;
        private String uploadmaxfiles;
        private String deletefile;
        private String uploadmaxfilesize;
        private String useddownloadattempts;
        private String useduploadmaxtotalsize;
        private String uploadneedagent;
        private String useduploadmaxfiles;
        private String uploadmaxtotalsize;
        private String uploadfiletypes;
    }
}
