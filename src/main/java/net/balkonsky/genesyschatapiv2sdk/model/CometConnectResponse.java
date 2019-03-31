package net.balkonsky.genesyschatapiv2sdk.model;

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
}
