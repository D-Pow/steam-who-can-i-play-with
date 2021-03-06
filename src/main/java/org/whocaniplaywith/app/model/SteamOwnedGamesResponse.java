package org.whocaniplaywith.app.model;

import lombok.Data;

import java.util.List;

@Data
public class SteamOwnedGamesResponse {
    @Data
    public static class OwnedGamesResponse {
        private List<SteamOwnedGame> games;
    }

    private OwnedGamesResponse response;
}
