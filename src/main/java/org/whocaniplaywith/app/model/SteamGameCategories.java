package org.whocaniplaywith.app.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum SteamGameCategories {
    MultiPlayer(1, "Multi-player"),
    SinglePlayer(2, "Single-player"),
    AntiCheatEnabled(8, "Valve Anti-Cheat enabled"),
    CoOp(9, "Co-op"),
    CaptionsAvailable(13, "Captions available"),
    CommentaryAvailable(14, "Commentary available"),
    Stats(15, "Stats"),
    IncludesSourceSdk(16, "Includes Source SDK"),
    IncludesLevelEditor(17, "Includes level editor"),
    PartialControllerSupport(18, "Partial Controller Support"),
    Mods(19, "Mods"),
    MMO(20, "MMO"),
    SteamAchievements(22, "Steam Achievements"),
    SteamCloud(23, "Steam Cloud"),
    SplitScreen(24, "Shared/Split Screen"),
    SteamLeaderboards(25, "Steam Leaderboards"),
    CrossPlatform(27, "Cross-Platform Multiplayer"),
    FullControllerSupport(28, "Full controller support"),
    SteamTradingCards(29, "Steam Trading Cards"),
    SteamWorkshop(30, "Steam Workshop"),
    VrSupport(31, "VR Support"),
    TurnNotifications(32, "Steam Turn Notifications"),
    InAppPurchases(35, "In-App Purchases"),
    OnlinePvP(36, "Online PvP"),
    SharedSplitScreenPvP(37, "Shared/Split Screen PvP"),
    OnlineCoOp(38, "Online Co-op"),
    SharedSplitScreenCoOp(39, "Shared/Split Screen Co-op"),
    VrCollectibles(40, "SteamVR Collectibles"),
    RemotePlayOnPhone(41, "Remote Play on Phone"),
    RemotePlayOnTablet(42, "Remote Play on Tablet"),
    RemotePlayOnTv(43, "Remote Play on TV"),
    RemotePlayTogether(44, "Remote Play Together"),
    LanPvp(47, "LAN PvP"),
    LanCoOp(48, "LAN Co-op"),
    PvP(49, "PvP");

    private final int id;
    private final String description;
    private static final List<SteamGameCategories> multiplayerGames = Arrays.asList(
        SteamGameCategories.MultiPlayer,
        SteamGameCategories.SplitScreen,
        SteamGameCategories.RemotePlayTogether
    );

    private SteamGameCategories(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static SteamGameCategories getCategoryById(int id) {
        return Arrays.stream(SteamGameCategories.values())
            .filter(category -> category.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public static boolean isGameMultiplayer(List<Integer> gameCategoryIds) {
        return multiplayerGames.stream()
            .map(SteamGameCategories::getId)
            .anyMatch(gameCategoryIds::contains);
    }
}
