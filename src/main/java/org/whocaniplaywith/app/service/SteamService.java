package org.whocaniplaywith.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.whocaniplaywith.ApplicationConfig;
import org.whocaniplaywith.app.model.*;
import org.whocaniplaywith.app.utils.Constants;
import org.whocaniplaywith.app.utils.http.Requests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SteamService {
    @Value("${org.whocaniplaywith.steam-api-key}")
    private String steamApiKey;

    private String getSteamApiUrl(String baseUrl, String[][] steamQueryParams) {
        List<String[]> queryParams = new ArrayList<>(Arrays.asList(steamQueryParams));

        queryParams.add(new String[] { "key", steamApiKey });
        queryParams.add(new String[] { "format", "json" });

        String[][] queryParamsWithNecessaryFieldsForSteam = queryParams.toArray(new String[queryParams.size()][]);

        return Requests.getUrlWithQueryParams(baseUrl, queryParamsWithNecessaryFieldsForSteam);
    }

    @Async
    public CompletableFuture<String> getSteamIdFromUsername(String username) {
        log.info("Getting Steam ID for username = {}", username);

        String steamId = null;
        String getSteamIdFromUsernameUrl = getSteamApiUrl(Constants.URL_STEAM_ID_FROM_USERNAME, new String[][]{
            { "vanityurl", username }
        });

        SteamIdResponse steamIdResponse = new RestTemplate().exchange(
            getSteamIdFromUsernameUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            SteamIdResponse.class
        ).getBody();

        if (steamIdResponse != null && steamIdResponse.getResponse() != null) {
            steamId = steamIdResponse.getResponse().getSteamid();
        }

        log.info("Steam ID from username = {}", steamIdResponse);

        return CompletableFuture.completedFuture(steamId);
    }

    @Async
    public CompletableFuture<SteamUserProfile> getUserProfile(String steamId) {
        log.info("Getting Steam user profile for Steam ID [{}]", steamId);

        SteamUserProfile userProfile = null;
        String getUserProfileUrl = getSteamApiUrl(Constants.URL_STEAM_PROFILE_INFO, new String[][]{
            { "steamids", steamId }
        });

        SteamUserProfileResponse userProfileResponse = new RestTemplate().exchange(
            getUserProfileUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            SteamUserProfileResponse.class
        ).getBody();

        if (userProfileResponse != null && userProfileResponse.getResponse() != null) {
            userProfile = userProfileResponse.getResponse().getPlayers().stream()
                .filter(profile -> profile.getSteamid().equals(steamId)).findFirst().orElse(null);
        }

        log.info("Steam user profile = {}", userProfile);

        return CompletableFuture.completedFuture(userProfile);
    }

    @Async
    public CompletableFuture<List<String>> getSteamFriendsIds(String steamId) {
        log.info("Getting Steam friend IDs for Steam ID [{}]", steamId);

        List<String> friendIds = null;
        String getSteamFriendssUrl = getSteamApiUrl(Constants.URL_STEAM_GET_FRIENDS_OF_USER_ID, new String[][]{
            { "steamid", steamId },
            { "relationship", "friend" }
        });

        SteamFriendsResponse steamFriendsResponse = new RestTemplate().exchange(
            getSteamFriendssUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            SteamFriendsResponse.class
        ).getBody();

        if (
            steamFriendsResponse != null
            && steamFriendsResponse.getFriendslist() != null
            && !steamFriendsResponse.getFriendslist().getFriends().isEmpty()
        ) {
            List<SteamFriends> friends = steamFriendsResponse.getFriendslist().getFriends();
            friendIds = friends.stream().map(SteamFriends::getSteamid).collect(Collectors.toList());
        }

        log.info("FriendIds for Steam ID [{}] are = {}", steamId, friendIds);

        return CompletableFuture.completedFuture(friendIds);
    }

    @Async
    public CompletableFuture<List<SteamOwnedGame>> getSteamOwnedGamesForUser(String steamId) {
        log.info("Getting owned games for Steam ID [{}]", steamId);

        List<SteamOwnedGame> ownedGames = new ArrayList<>();
        String getSteamOwnedGamesUrl = getSteamApiUrl(Constants.URL_STEAM_GET_OWNED_GAMES, new String[][]{
            { "steamid", steamId },
            { "include_appinfo", "true" }
        });

        SteamOwnedGamesResponse steamOwnedGamesResponse = new RestTemplate().exchange(
            getSteamOwnedGamesUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            SteamOwnedGamesResponse.class
        ).getBody();

        if (
            steamOwnedGamesResponse != null
            && steamOwnedGamesResponse.getResponse() != null
            && !steamOwnedGamesResponse.getResponse().getGames().isEmpty()
        ) {
            ownedGames = steamOwnedGamesResponse.getResponse().getGames();
        }

        log.info("Obtained {} owned games for Steam ID [{}]", ownedGames.size(), steamId);

        return CompletableFuture.completedFuture(ownedGames);
    }

    @Async
    @Cacheable(ApplicationConfig.STEAM_GAME_DETAILS_CACHE_NAME)
    public CompletableFuture<SteamGameDetails> getGameDetails(String gameAppId) {
        log.info("Getting game details for game app ID [{}]", gameAppId);

        SteamGameDetails gameDetails = null;
        String getGameDetailsUrl = Requests.getUrlWithQueryParams(Constants.URL_STEAM_GET_GAME_DETAILS, new String[][]{
            { "appids", gameAppId }
        });

        String gameDetailsResponseString = new RestTemplate().exchange(
            getGameDetailsUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            String.class
        ).getBody();

        Map<String, SteamGameDetailsResponse.GameDetailsResponse> gameDetailsResponseMap =
            SteamGameDetailsResponse.getAppIdDetailsMap(gameDetailsResponseString);

        if (
            gameDetailsResponseMap != null
            && gameDetailsResponseMap.size() > 0
        ) {
            try {
                SteamGameDetailsResponse.GameDetailsResponse gameDetailsResponse = gameDetailsResponseMap.get(gameAppId);

                if (gameDetailsResponse.isSuccess()) {
                    gameDetails = gameDetailsResponse.getData();
                } else {
                    log.info("Game details request failed for appId [{}]", gameAppId);
                }
            } catch (Exception e) {
                log.info("Could not obtain game details for appId [{}]. Error:", gameAppId, e);
            }
        }

        return CompletableFuture.completedFuture(gameDetails);
    }

    public List<SteamGameDetails> getMultiplayerGames(List<SteamGameDetails> games) {
        return games.stream()
            .filter(gameDetails -> {
                if (gameDetails.getCategories() == null) {
                    return false;
                }

                List<Integer> gameCategoriesIds = gameDetails.getCategories().stream()
                    .map(SteamGameDetails.GameCategories::getId)
                    .collect(Collectors.toList());

                return SteamGameCategories.isGameMultiplayer(gameCategoriesIds);
            })
            .collect(Collectors.toList());
    }
}
