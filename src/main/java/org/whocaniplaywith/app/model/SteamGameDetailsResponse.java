package org.whocaniplaywith.app.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.whocaniplaywith.app.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class SteamGameDetailsResponse {
    @Data
    public static class GameDetailsResponse {
        private boolean success;
        private SteamGameDetails data;
    }

    private static class Response extends HashMap<String, GameDetailsResponse> {}

    /**
     * Since the Steam API is poorly developed and uses the game ID as the key
     * (e.g. {@code { 1234: {...gameDetails}}})
     * rather than having a dedicated field to represent the game ID
     * (e.g. {@code { appId: 1234, info: {...gameDetails}}}),
     * the response must be received as a String and then parsed to an object afterwards.
     *
     * Note: There may be a way to inherently parse it as a {@code Map<String, MyObj>} but it didn't
     * work here, so {@link ObjectMapper} is used instead.
     *
     * @param responseBody The String returned from the request.
     * @return A Map of the game app IDs to the {@link GameDetailsResponse}.
     */
    public static Map<String, GameDetailsResponse> getAppIdDetailsMap(String responseBody) {
        // (pc|mac|linux)_requirements response field is poorly developed and will
        // return an empty array if the details are empty.
        // Convert the empty arrays to null so the non-empty fields will be populated correctly.
        Map<DeserializationFeature, Boolean> parserOptions = new HashMap<DeserializationFeature, Boolean>() {{
            put(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
            put(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        }};

        return ObjectUtils.sanitizeAndParseJsonToClass(
            responseBody,
            Response.class,
            parserOptions
        );
    }
}
