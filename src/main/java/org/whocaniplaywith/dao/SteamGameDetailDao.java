package org.whocaniplaywith.dao;

import org.whocaniplaywith.app.model.SteamGameDetails;
import org.whocaniplaywith.app.utils.Pair;

import java.util.List;

public interface SteamGameDetailDao {
    SteamGameDetails getSteamGameDetails(String appId);
    boolean saveSteamGameDetails(String appId, String gameDetailJson);
    boolean saveAllSteamGameDetails(List<Pair<String, String>> allGameDetails);
}
