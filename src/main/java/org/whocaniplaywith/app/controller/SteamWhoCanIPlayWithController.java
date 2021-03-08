package org.whocaniplaywith.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.whocaniplaywith.app.model.SteamGameDetails;
import org.whocaniplaywith.app.model.GetPlayableGamesRequest;
import org.whocaniplaywith.app.model.SteamOwnedGame;
import org.whocaniplaywith.app.model.SteamPlayableMultiplayerGamesResponse;
import org.whocaniplaywith.app.model.SteamUserProfile;
import org.whocaniplaywith.app.service.SteamService;
import org.whocaniplaywith.app.utils.ObjectUtils;
import org.whocaniplaywith.app.utils.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class SteamWhoCanIPlayWithController {
    @Autowired
    SteamService steamService;

    private List<SteamGameDetails> getAllGameDetailsForUser(String steamUserId) {
        return getAllGameDetailsForUsers(Collections.singletonList(steamUserId)).get(steamUserId);
    }

    private Map<String, List<SteamGameDetails>> getAllGameDetailsForUsers(List<String> steamUserIds) {
        List<Pair<String, CompletableFuture<List<SteamOwnedGame>>>> usersOwnedGamesFutures = steamUserIds.stream()
            .map(steamId -> new Pair<>(
                steamId,
                steamService.getSteamOwnedGamesForUser(steamId)
            ))
            .collect(Collectors.toList());
        List<Pair<String, List<SteamOwnedGame>>> usersOwnedGames = usersOwnedGamesFutures.stream()
            .map(futurePair -> new Pair<>(
                futurePair.getKey(),
                ObjectUtils.getAllCompletableFutureResults(futurePair.getValue())
            ))
            .collect(Collectors.toList());
        List<Pair<String, List<CompletableFuture<SteamGameDetails>>>> usersGameDetailsFutures = usersOwnedGames.stream()
            .map(pair -> new Pair<>(
                pair.getKey(),
                pair.getValue().stream()
                    .map(game -> steamService.getGameDetails(String.valueOf(game.getAppid())))
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
        List<Pair<String, List<SteamGameDetails>>> usersGameDetails = usersGameDetailsFutures.stream()
            .map(futurePair -> new Pair<>(
                futurePair.getKey(),
                ObjectUtils.getAllCompletableFutureResults(futurePair.getValue()).stream()
                    .filter(game -> game != null)
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());

        return usersGameDetails.stream().reduce(
            new HashMap<>(),
            (map, pair) -> {
                map.put(pair.getKey(), pair.getValue());

                return map;
            },
            (map1, map2) -> map2
        );
    }

    private List<SteamGameDetails> getAllMultiplayerGameDetailsForUser(List<SteamGameDetails> userGames) {
        String steamId = "nil";
        Map<String, List<SteamGameDetails>> multiplayerGames = new HashMap<>();

        multiplayerGames.put(steamId, userGames);

        return getAllMultiplayerGameDetailsForUsers(multiplayerGames).get(steamId);
    }

    private Map<String, List<SteamGameDetails>> getAllMultiplayerGameDetailsForUsers(Map<String, List<SteamGameDetails>> usersGames) {
        return usersGames.entrySet().stream().reduce(
            new HashMap<>(),
            (map, entry) -> {
                String steamId = entry.getKey();
                List<SteamGameDetails> gameDetails = entry.getValue();
                List<SteamGameDetails> multiplayerGames = steamService.getMultiplayerGames(gameDetails);

                map.put(steamId, multiplayerGames);

                return map;
            },
            (map1, map2) -> map2
        );
    }

    public ResponseEntity<SteamPlayableMultiplayerGamesResponse> getPlayableGamesForUser(@RequestBody GetPlayableGamesRequest getPlayableGamesRequest) {
        SteamPlayableMultiplayerGamesResponse response = new SteamPlayableMultiplayerGamesResponse();
        String username = getPlayableGamesRequest.getUsername();

        log.info("Request = {}", getPlayableGamesRequest);

        try {
            String steamUserId = steamService.getSteamIdFromUsername(username).get();

            SteamUserProfile userProfile = steamService.getUserProfile(steamUserId).get();
            List<SteamGameDetails> ownedGamesDetails = getAllGameDetailsForUser(steamUserId);
            List<SteamGameDetails> ownedMultiplayerGames = getAllMultiplayerGameDetailsForUser(ownedGamesDetails);

            List<String> friendsIds = steamService.getSteamFriendsIds(userProfile.getSteamid()).get();
            Map<String, List<SteamGameDetails>> friendsGameDetails = getAllGameDetailsForUsers(friendsIds);
            Map<String, List<SteamGameDetails>> friendsMultiplayerGames = getAllMultiplayerGameDetailsForUsers(friendsGameDetails);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Could not get Steam UserId future. Error = {}", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }

        return ResponseEntity.ok(response);
    }
}
