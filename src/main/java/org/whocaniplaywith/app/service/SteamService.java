package org.whocaniplaywith.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.whocaniplaywith.ApplicationConfig;
import org.whocaniplaywith.app.model.*;
import org.whocaniplaywith.app.utils.AppProxy;
import org.whocaniplaywith.app.utils.Constants;
import org.whocaniplaywith.app.utils.Pair;
import org.whocaniplaywith.app.utils.http.Requests;
import org.whocaniplaywith.dao.SteamGameDetailDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SteamService {
    private static int gameDetailsCounter = 0;

    @Value("${org.whocaniplaywith.steam-api-key}")
    private String steamApiKey;

    @Autowired
    private SteamGameDetailDao steamGameDetailDao;

    private List<Pair<String, String>> steamGameDetailsResponses = new ArrayList<>();

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
        gameDetailsCounter++;
        log.info("Getting game details for game app ID [{}]", gameAppId);

        SteamGameDetails gameDetails = null;
        String getGameDetailsUrl = Requests.getUrlWithQueryParams(Constants.URL_STEAM_GET_GAME_DETAILS, new String[][]{
            { "appids", gameAppId }
        });

        // Whole number of times the requests surpassed the rate limit of steam.storepowered.com
        // Subtract 1 so that the original IP address can be used for the first n SteamGameDetails requests
        int numTimesGameDetailsRequestsExceedRateLimiter = (gameDetailsCounter / Constants.NUM_ALLOWED_REQUESTS_TO_STORE_STEAMPOWERED) - 1;
        // How many times until the next rate limit
        int gameDetailsCountUntilNextRateLimiter = gameDetailsCounter % Constants.NUM_ALLOWED_REQUESTS_TO_STORE_STEAMPOWERED;
        int proxyIndex = numTimesGameDetailsRequestsExceedRateLimiter;

        gameDetails = steamGameDetailDao.getSteamGameDetails(gameAppId);

        if (gameDetails != null) {
            return CompletableFuture.completedFuture(gameDetails);
        }

        String gameDetailsResponseString = AppProxy.attemptRequestThroughProxiesUntilSuccess(
            getGameDetailsUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            String.class,
            proxyIndex
        );

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

                    steamGameDetailsResponses.add(new Pair<>(gameAppId, gameDetailsResponseString));
                } else {
                    log.info("Game details request failed for appId [{}]", gameAppId);
                }
            } catch (Exception e) {
                log.info("Could not obtain game details for appId [{}]. Error:", gameAppId, e);
            }
        }

        return CompletableFuture.completedFuture(gameDetails);
    }

    /**
     * SQLite is single-threaded, so the DAO can't save each response individually
     * since each {@code getGameDetails()} call is made on a different thread.
     * Thus, store all the responses in a List and save all the entries here.
     * Must be called by the controller to ensure all {@code getGameDetails()} calls
     * are finished.
     *
     * TODO This doesn't protect against multiple users hitting the endpoint simultaneously.
     *
     * @return
     */
    public boolean saveAllRequestedGameDetails() {
        boolean success = steamGameDetailDao.saveAllSteamGameDetails(steamGameDetailsResponses);

        steamGameDetailsResponses.clear();

        return success;
    }

    private List<SteamGameDetails> getMultiplayerGamesOfCategory(List<SteamGameDetails> games, Function<List<Integer>, Boolean> multiplayerCategoryFunc) {
        return games.stream()
            .filter(gameDetails -> {
                if (gameDetails.getCategories() == null) {
                    return false;
                }

                List<Integer> gameCategoriesIds = gameDetails.getCategories().stream()
                    .map(SteamGameDetails.GameCategories::getId)
                    .collect(Collectors.toList());

                return multiplayerCategoryFunc.apply(gameCategoriesIds);
            })
            .collect(Collectors.toList());
    }

    public List<SteamGameDetails> getMultiplayerGames(List<SteamGameDetails> games) {
        return getMultiplayerGamesOfCategory(games, SteamGameCategories::isGameMultiplayer);
    }

    public List<SteamGameDetails> getRemotePlayGames(List<SteamGameDetails> games) {
        return getMultiplayerGamesOfCategory(games, SteamGameCategories::isGameRemotePlay);
    }

    public List<SteamGameDetails> getSplitScreenGames(List<SteamGameDetails> games) {
        return getMultiplayerGamesOfCategory(games, SteamGameCategories::isGameSplitScreen);
    }
}
