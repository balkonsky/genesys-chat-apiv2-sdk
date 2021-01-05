package net.balkonsky.genesyschatapiv2sdk.model;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class FileSendResponse {
    private List<CometConnectResponse.Messages> messages;
    private Boolean chatEnded;
    private Integer statusCode;
    private UserData userData;
    private String secureKey;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @ToString
    public static class UserData {
        @SerializedName("file-id")
        private String fileid;
    }
}
