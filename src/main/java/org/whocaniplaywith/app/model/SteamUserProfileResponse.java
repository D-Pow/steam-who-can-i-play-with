package org.whocaniplaywith.app.model;

import lombok.Data;

import java.util.List;

@Data
public class SteamUserProfileResponse {
    @Data
    public static class Response {
        private List<SteamUserProfile> players;
    }

    private Response response;
}
