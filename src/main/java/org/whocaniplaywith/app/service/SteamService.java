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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
@Slf4j
public class SteamService {
    @Value("${org.whocaniplaywith.steam-api-key}")
    private String steamApiKey;

    private String getSteamApiUrl(String baseUrl, String[][] steamQueryParams) {
        Stream.of(steamQueryParams).reduce(new HashMap<String, String>(), (queryParamMap, queryEntry) -> {
            //
        })

        List<String[]> queryParams = Arrays.asList(steamQueryParams);

        log.info("Ugh I hate me = {}", queryParams);

        queryParams.add(new String[] { "format", "json" });
//        queryParams.add(new String[] { "key", steamApiKey });

        log.info("Fuck me, query params = {}", queryParams.toArray());

        return Requests.getUrlWithQueryParams(baseUrl, queryParams.toArray());
    }

    @Async
    public CompletableFuture<String> getSteamIdFromUsername(String username) {
        log.info("Called with username = {}", username);

        String getSteamIdFromUsernameUrl = getSteamApiUrl(Constants.URL_STEAM_ID_FROM_USERNAME, new String[][]{
            { "vanityurl", username }
        });

        log.info("ASDF req URL = {}", getSteamIdFromUsernameUrl);

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
