package org.whocaniplaywith.app.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ObjectUtils {
    private static final Logger log = LoggerFactory.getLogger(ObjectUtils.class);

    public static <T> T findObjectInList(List<T> list, Predicate<T> filterPredicate) {
        return list.stream().filter(filterPredicate).findFirst().orElse(null);
    }

    /**
     * Waits for the {@link CompletableFuture}s to complete and extracts
     * the result.
     *
     * @param future Future to complete.
     * @return The future result.
     */
    public static <T> T getAllCompletableFutureResults(CompletableFuture<T> future) {
        List<T> allCompletableFutureResults = getAllCompletableFutureResults(Collections.singletonList(future), result -> {});

        if (allCompletableFutureResults != null && !allCompletableFutureResults.isEmpty()) {
            return allCompletableFutureResults.get(0);
        }

        return null;
    }

    /**
     * Waits for all {@link CompletableFuture}s to complete and extracts
     * the result from each.
     *
     * @param futures List of futures to complete.
     * @return All future results in the same order as the {@code futures} parameter.
     */
    public static <T> List<T> getAllCompletableFutureResults(List<CompletableFuture<T>> futures) {
        return getAllCompletableFutureResults(futures, result -> {});
    }

    /**
     * Waits for all {@link CompletableFuture}s to complete and extracts
     * the result from each.
     *
     * @param futures List of futures to complete.
     * @param sideEffect Function to run on each {@link CompletableFuture#get()} call.
     *                   Accepts the future result as the argument.
     *                   Runs sequentially from {@code futures} start (index 0) to end,
     *                   regardless of individual future completion order.
     * @return All future results in the same order as the {@code futures} parameter.
     */
    public static <T> List<T> getAllCompletableFutureResults(
        List<CompletableFuture<T>> futures,
        Consumer<T> sideEffect
    ) {
        return getAllCompletableFutureResults(futures, (result, index) -> sideEffect.accept(result));
    }

    /**
     * Waits for all {@link CompletableFuture}s to complete and extracts
     * the result from each.
     *
     * @param futures List of futures to complete.
     * @param sideEffect Function to run on each {@link CompletableFuture#get()} call.
     *                   Accepts the future result and its index in the {@code futures} list as arguments.
     *                   Runs sequentially from {@code futures} start (index 0) to end,
     *                   regardless of individual future completion order.
     * @return All future results in the same order as the {@code futures} parameter.
     */
    public static <T> List<T> getAllCompletableFutureResults(
        List<CompletableFuture<T>> futures,
        BiConsumer<T, Integer> sideEffect
    ) {
        if (futures == null || futures.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> results = new ArrayList<>(futures.size());

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<T> future = futures.get(i);
            T result = null;

            if (future == null) {
                sideEffect.accept(result, i);
                continue;
            }

            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Could not get future. Error:", e);
            }

            if (result == null) {
                sideEffect.accept(result, i);
                continue;
            }

            sideEffect.accept(result, i);
            results.add(result);
        }

        return results.stream()
            .filter(result -> result != null)
            .collect(Collectors.toList());
    }

    public static String hashResource(Resource resource) {
        if (resource == null) {
            return null;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] imageBytes = new byte[(int) resource.contentLength()];
            inputStream.read(imageBytes);

            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] resourceHash = hash.digest(imageBytes);

            return new BigInteger(1, resourceHash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Could not hash Resource ({}). Error cause ({}), message = {}", resource, e.getCause(), e.getMessage());
        }

        return null;
    }

    public static <T> T sanitizeAndParseJsonToClass(String json, Class<T> parseToClass) {
        return sanitizeAndParseJsonToClass(json, parseToClass, null);
    }

    public static <T> T sanitizeAndParseJsonToClass(String json, Class<T> parseToClass, Map<DeserializationFeature, Boolean> objMapperFlags) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (objMapperFlags != null) {
            objMapperFlags.forEach(objectMapper::configure);
        }

        String nonAsciiCharactersRegex = "[^\\x00-\\x7F]";
        String asciiControlCharactersRegex = "[\\p{Cntrl}&&[^\r\n\t]]";
        String nonPrintableCharactersRegex = "\\p{C}";

        String sanitizeRegex = String.format("(%s)|(%s)|(%s)", nonAsciiCharactersRegex, asciiControlCharactersRegex, nonPrintableCharactersRegex);
        String sanitizedJson = json.replaceAll(sanitizeRegex, "");

        try {
            return objectMapper.readValue(sanitizedJson, parseToClass);
        } catch (JsonProcessingException e) {
            log.error("Could not parse json to class ({}). Error = {}", parseToClass, e.getMessage());
        }

        return null;
    }

    public static String toString(Object obj) {
        StringBuilder sb = new StringBuilder(obj.getClass().getName() + " {");

        Arrays.stream(obj.getClass().getDeclaredFields())
            .forEach(field -> {
                String fieldName = field.getName();

                sb.append(fieldName)
                    .append("=");

                Object fieldValue = "Unparsed";

                try {
                    field.setAccessible(true); // Make private fields readable
                    fieldValue = field.get(obj);
                } catch (IllegalAccessException ignored) {}

                sb.append("(")
                    .append(fieldValue)
                    .append(")");

                sb.append(", ");
            });

        sb.delete(sb.length() - 2, sb.length()); // Remove last comma+space
        sb.append("}");

        return sb.toString();
    }
}
