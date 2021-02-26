package org.whocaniplaywith.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

        String getSteamIdFromUsernameUrl = getSteamApiUrl(Constants.URL_STEAM_ID_FROM_USERNAME, new String[][]{
            { "vanityurl", username }
        });

        String body = new RestTemplate().exchange(
            getSteamIdFromUsernameUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, null),
            String.class
        ).getBody();

        log.info("Steam ID from username = {}", body);

        return CompletableFuture.completedFuture(body);
    }
}
