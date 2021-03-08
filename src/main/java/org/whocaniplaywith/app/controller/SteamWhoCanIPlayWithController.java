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
import java.util.function.Function;
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

    private Map<String, List<String>> getSharedMultiplayerGames(List<String> ownedMultiplayerGamesIds, Map<String, List<SteamGameDetails>> friendsMultiplayerGames) {
        return friendsMultiplayerGames.entrySet().stream()
            .reduce(
                new HashMap<>(),
                (map, friendEntry) -> {
                    String friendSteamId = friendEntry.getKey();
                    List<SteamGameDetails> friendMultiplayerGames = friendEntry.getValue();

                    List<SteamGameDetails> sharedGames = friendMultiplayerGames.stream()
                        .filter(game -> {
                            String steamAppid = game.getSteamAppid();

                            return ownedMultiplayerGamesIds.contains(steamAppid);
                        })
                        .collect(Collectors.toList());

                    if (!sharedGames.isEmpty()) {
                        map.put(
                            friendSteamId,
                            sharedGames.stream()
                                .map(SteamGameDetails::getName)
                                .collect(Collectors.toList())
                        );
                    }

                    return map;
                },
                (map1, map2) -> map2
            );
    }

    private Map<String, List<String>> filterMultiplayerGamesByCategory(
        Map<String, List<SteamGameDetails>> userGamesMap,
        Function<List<SteamGameDetails>, List<SteamGameDetails>> multiplayerCategoryFunc
    ) {
        return userGamesMap.entrySet().stream()
            .reduce(
                new HashMap<>(),
                (map, friendEntry) -> {
                    String friendSteamId = friendEntry.getKey();
                    List<SteamGameDetails> friendMultiplayerGames = friendEntry.getValue();

                    List<SteamGameDetails> remotePlayGames = multiplayerCategoryFunc.apply(friendMultiplayerGames);

                    if (!remotePlayGames.isEmpty()) {
                        map.put(
                            friendSteamId,
                            remotePlayGames.stream()
                                .map(SteamGameDetails::getName)
                                .collect(Collectors.toList())
                        );
                    }

                    return map;
                },
                (map1, map2) -> map2
            );
    }

    private List<String> getRemotePlayGames(List<SteamGameDetails> games) {
        Map<String, List<SteamGameDetails>> userGamesMap = new HashMap<>();
        String steamId = "nil";

        userGamesMap.put(steamId, games);

        return filterMultiplayerGamesByCategory(userGamesMap, steamService::getRemotePlayGames).get(steamId);
    }

    private Map<String, List<String>> getRemotePlayGames(Map<String, List<SteamGameDetails>> userGamesMap) {
        return filterMultiplayerGamesByCategory(userGamesMap, steamService::getRemotePlayGames);
    }

    private List<String> getSplitScreenGames(List<SteamGameDetails> games) {
        Map<String, List<SteamGameDetails>> userGamesMap = new HashMap<>();
        String steamId = "nil";

        userGamesMap.put(steamId, games);

        return filterMultiplayerGamesByCategory(userGamesMap, steamService::getSplitScreenGames).get(steamId);
    }

    private Map<String, List<String>> getSplitScreenGames(Map<String, List<SteamGameDetails>> userGamesMap) {
        return filterMultiplayerGamesByCategory(userGamesMap, steamService::getSplitScreenGames);
    }

    private Map<String, List<String>> convertSteamIdsToUsernames(Map<String, List<String>> mapWithIds, Map<String, String> profileIdToNameMap) {
        return mapWithIds.entrySet().stream()
            .reduce(
                new HashMap<>(),
                (map, entry) -> {
                    String steamId = entry.getKey();
                    List<String> gameNames = entry.getValue();
                    String steamUsername = profileIdToNameMap.get(steamId);

                    map.put(steamUsername, gameNames);

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

            List<CompletableFuture<SteamUserProfile>> friendProfilesFutures = friendsIds.stream()
                .map(steamService::getUserProfile)
                .collect(Collectors.toList());
            List<SteamUserProfile> friendProfiles = ObjectUtils.getAllCompletableFutureResults(friendProfilesFutures);
            Map<String, String> profileIdToNameMap = friendProfiles.stream().reduce(
                new HashMap<>(),
                (map, profile) -> {
                    map.put(profile.getSteamid(), profile.getPersonaname());

                    return map;
                },
                (map1, map2) -> map2
            );
            profileIdToNameMap.put(steamUserId, userProfile.getPersonaname());

            List<String> ownedMultiplayerGamesIds = ownedMultiplayerGames.stream().map(SteamGameDetails::getSteamAppid).collect(Collectors.toList());

            Map<String, List<String>> sharedMultiplayerGamesById = getSharedMultiplayerGames(ownedMultiplayerGamesIds, friendsMultiplayerGames);
            List<String> ownedRemotePlayGames = getRemotePlayGames(ownedMultiplayerGames);
            List<String> ownedSplitScreenGames = getSplitScreenGames(ownedMultiplayerGames);
            Map<String, List<String>> friendsRemotePlayGames = getRemotePlayGames(friendsMultiplayerGames);
            Map<String, List<String>> friendsSplitScreenGames = getSplitScreenGames(friendsMultiplayerGames);

            friendsRemotePlayGames.put(steamUserId, ownedRemotePlayGames);
            friendsSplitScreenGames.put(steamUserId, ownedSplitScreenGames);

            // TODO reduce number of Map/List iterations by using PersonaName as key in the first place
            Map<String, List<String>> sharedMultiplayerGamesByName = convertSteamIdsToUsernames(sharedMultiplayerGamesById, profileIdToNameMap);
            Map<String, List<String>> remotePlayGames = convertSteamIdsToUsernames(friendsRemotePlayGames, profileIdToNameMap);
            Map<String, List<String>> splitScreenGames = convertSteamIdsToUsernames(friendsSplitScreenGames, profileIdToNameMap);

            response.setSharedMultiplayerGames(sharedMultiplayerGamesByName);
            response.setRemotePlayGames(remotePlayGames);
            response.setSplitScreenGames(splitScreenGames);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Could not get Steam UserId future. Error = {}", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }

        return ResponseEntity.ok(response);
    }
}
