package net.balkonsky.genesyschatapiv2sdk.model;

//{"messages":
// [{"from":{
// "nickname":"test",
// "participantId":1,
// "type":"Client"
// },
// "text":"file-client-cfg-get",
// "type":"Notice",
// "utcTime":1554322581000,
// "userData":{
// "download-attempts":"2",
// "upload-max-files":"5",
// "delete-file":"odd",
// "upload-max-file-size":"10485760",
// "used-download-attempts":"0",
// "used-upload-max-total-size":"0",
// "upload-need-agent":"false",
// "used-upload-max-files":"0",
// "upload-max-total-size":"99614720",
// "upload-file-types":"jpeg:jpg:pdf:png:zip"}}],
// "chatEnded":false,"statusCode":0}


import java.util.List;

public class HttpResponse {
    private List<Messages> messages;

    public class Messages{
        private From from;
        private String text;
        private String type;


    }

    public class From {
        private String nickname;
        private String participantId;
        private String type;
        private String text;

    }
}
