package org.whocaniplaywith.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.whocaniplaywith.app.model.SteamIdResponse;
import org.whocaniplaywith.app.model.SteamUserProfile;
import org.whocaniplaywith.app.model.SteamUserProfileResponse;
import org.whocaniplaywith.app.utils.Constants;
import org.whocaniplaywith.app.utils.http.Requests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
}
