package org.whocaniplaywith.app.model;

import lombok.Data;

@Data
public class SteamIdResponse {
    @Data
    public static class Response {
        private String steamid;
        private int success;
    }

    private Response response;
}
