package org.whocaniplaywith.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class SteamGameDetails {
    @Data
    public static class PcRequirements {
        private String minimum;
        private String recommended;
    }

    @Data
    public static class Platforms {
        private boolean windows;
        private boolean mac;
        private boolean linux;
    }

    @Data
    public static class MetacriticRating {
        private int score;
        private URI url;
    }

    @Data
    public static class GameCategories {
        private int id;
        private String description;
    }

    @Data
    public static class GameGenres {
        private int id;
        private String description;
    }

    @Data
    public static class GameScreenshots {
        private int id;
        @JsonProperty("path_thumbnail")
        private URI pathThumbnail;
        @JsonProperty("path_full")
        private URI pathFull;
    }

    @Data
    public static class Recommendations {
        private int total;
    }

    @Data
    public static class ReleaseDate {
        @JsonProperty("coming_soon")
        private boolean comingSoon;
        private String date;

        public LocalDate getDate() {
            try {
                return LocalDate.parse(
                    this.date,
                    DateTimeFormatter.ofPattern("MMM d, yyyy")
                );
            } catch (Exception e) {}

            try {
                return LocalDate.parse(
                    this.date,
                    DateTimeFormatter.ofPattern("d MMM, yyyy")
                );
            } catch (Exception e) {}

            return null;
        }
    }

    @Data
    public static class SupportInfo {
        private URI url;
        private String email;
    }

    @Data
    public static class ContentDescriptors {
        private List<String> ids;
        private String notes;
    }

    private String type;
    private String name;
    @JsonProperty("steam_appid")
    private Long steamAppid;
    @JsonProperty("required_age")
    private int requiredAge;
    @JsonProperty("is_free")
    private boolean isFree;
    @JsonProperty("detailed_description")
    private String detailedDescription;
    @JsonProperty("about_the_game")
    private String aboutTheGame;
    @JsonProperty("short_description")
    private String shortDescription;
    @JsonProperty("supported_languages")
    private String supportedLanguages;
    @JsonProperty("header_image")
    private URI headerImage;
    private URI website;
    @JsonProperty("pc_requirements")
    private PcRequirements pcRequirements;
    @JsonProperty("mac_requirements")
    private PcRequirements macRequirements;
    @JsonProperty("linux_requirements")
    private PcRequirements linuxRequirements;
    @JsonProperty("legal_notice")
    private String legalNotice;
    private List<String> developers;
    private List<String> publishers;
    private Platforms platforms;
    private MetacriticRating metacritic;
    private List<GameCategories> categories;
    private List<GameGenres> genres;
    private List<GameScreenshots> screenshots;
    private Recommendations recommendations;
    @JsonProperty("release_date")
    private ReleaseDate releaseDate;
    @JsonProperty("support_info")
    private SupportInfo supportInfo;
    private URI background;
    @JsonProperty("content_descriptors")
    private ContentDescriptors contentDescriptors;
}
