package org.whocaniplaywith.app.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SteamPlayableMultiplayerGamesResponse {
    Map<String, List<String>> sharedMultiplayerGames;
    Map<String, List<String>> remotePlayGames;
    Map<String, List<String>> splitScreenGames;
}
