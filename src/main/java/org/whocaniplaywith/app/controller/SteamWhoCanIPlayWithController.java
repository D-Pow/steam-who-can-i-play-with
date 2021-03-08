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
import org.whocaniplaywith.app.model.SteamUserProfile;
import org.whocaniplaywith.app.service.SteamService;
import org.whocaniplaywith.app.utils.ObjectUtils;
import org.whocaniplaywith.app.utils.Pair;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class SteamWhoCanIPlayWithController {
    @Autowired
    SteamService steamService;

    public ResponseEntity<Object> getPlayableGamesForUser(@RequestBody GetPlayableGamesRequest getPlayableGamesRequest) {
        String username = getPlayableGamesRequest.getUsername();
        String steamUserId = null;

        log.info("Request = {}", getPlayableGamesRequest);

        try {
            steamUserId = steamService.getSteamIdFromUsername(username).get();
            SteamUserProfile userProfile = steamService.getUserProfile(steamUserId).get();
            List<SteamOwnedGame> userOwnedGames = steamService.getSteamOwnedGamesForUser(steamUserId).get();

            List<CompletableFuture<SteamGameDetails>> ownedGameDetailsFutures = userOwnedGames.stream()
                .map(game -> steamService.getGameDetails(String.valueOf(game.getAppid())))
                .collect(Collectors.toList());
            List<SteamGameDetails> ownedGameDetails =
                ObjectUtils.getAllCompletableFutureResults(ownedGameDetailsFutures).stream()
                    .filter(game -> game != null)
                    .collect(Collectors.toList());
            List<SteamGameDetails> ownedMultiplayerGames = steamService.getMultiplayerGames(ownedGameDetails);

            List<String> friendsIds = steamService.getSteamFriendsIds(userProfile.getSteamid()).get();
            List<Pair<String, CompletableFuture<List<SteamOwnedGame>>>> friendsOwnedGamesFutures = friendsIds.stream()
                .map(friendId -> new Pair<>(friendId, steamService.getSteamOwnedGamesForUser(friendId)))
                .collect(Collectors.toList());
            List<Pair<String, List<SteamOwnedGame>>> friendsOwnedGames = friendsOwnedGamesFutures.stream()
                .map(futurePair -> new Pair<>(
                    futurePair.getKey(),
                    ObjectUtils.getAllCompletableFutureResults(futurePair.getValue())
                ))
                .collect(Collectors.toList());
            List<Pair<String, List<CompletableFuture<SteamGameDetails>>>> friendsGameDetailsFutures = friendsOwnedGames.stream()
                .map(pair -> new Pair<>(
                    pair.getKey(),
                    pair.getValue().stream()
                        .map(game -> steamService.getGameDetails(String.valueOf(game.getAppid())))
                        .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
            List<Pair<String, List<SteamGameDetails>>> friendsGameDetails = friendsGameDetailsFutures.stream()
                .map(futurePair -> new Pair<>(
                    futurePair.getKey(),
                    ObjectUtils.getAllCompletableFutureResults(futurePair.getValue()).stream()
                        .filter(game -> game != null)
                        .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
            List<Pair<String, List<SteamGameDetails>>> friendsMultiplayerGames = friendsGameDetails.stream()
                .map(pair -> new Pair<>(
                    pair.getKey(),
                    steamService.getMultiplayerGames(pair.getValue())
                ))
                .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Could not get Steam UserId future. Error = {}", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }

        return ResponseEntity.ok(steamUserId);
    }
}
