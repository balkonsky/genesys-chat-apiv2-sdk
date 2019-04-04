package net.balkonsky.genesyschatapiv2sdk.model;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class CometConnectResponse {
    private String error;
    private Data data;
    private String channel;

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Data {
        private List<Messages> messages = new LinkedList<>();
        private Boolean chatEnded;
        private Integer statusCode;
        private String alias;
        private String secureKey;
        private String userId;
        private String chatId;
        private Long nextPosition;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class Messages {
        private From from;
        private String index;
        private String text;
        private String type;
        private Long utcTime;
        private UserData userData;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class From {
        private Long participantId;
        private String nickname;
        private String type;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class UserData {
        @SerializedName("download-attempts")
        private String downloadattempts;
        @SerializedName("upload-max-files")
        private String uploadmaxfiles;
        @SerializedName("delete-file")
        private String deletefile;
        @SerializedName("upload-max-file-size")
        private String uploadmaxfilesize;
        @SerializedName("used-download-attempts")
        private String useddownloadattempts;
        @SerializedName("used-upload-max-total-size")
        private String useduploadmaxtotalsize;
        @SerializedName("upload-need-agent")
        private String uploadneedagent;
        @SerializedName("used-upload-max-files")
        private String useduploadmaxfiles;
        @SerializedName("upload-max-total-size")
        private String uploadmaxtotalsize;
        @SerializedName("upload-file-types")
        private String uploadfiletypes;
    }
}
