package org.whocaniplaywith.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.whocaniplaywith.app.model.GameDetail;
import org.whocaniplaywith.app.model.SteamGameDetails;
import org.whocaniplaywith.app.model.SteamGameDetailsResponse;
import org.whocaniplaywith.app.utils.Pair;
import org.whocaniplaywith.dao.persistence.SteamGameDetailRepository;

import java.util.List;
import java.util.stream.Collectors;

@Repository("SteamGameDetailDao")
public class SteamGameDetailDaoImpl implements SteamGameDetailDao {
    @Autowired
    SteamGameDetailRepository steamGameDetailRepository;

    public SteamGameDetails getSteamGameDetails(String appId) {
        GameDetail gameDetails = steamGameDetailRepository.getSteamGameDetailsById(appId);

        if (gameDetails == null) {
            return null;
        }

        SteamGameDetailsResponse.GameDetailsResponse steamGameDetailsJson = SteamGameDetailsResponse
            .getAppIdDetailsMap(gameDetails.getGameDetailJson())
            .getOrDefault(gameDetails.getSteamAppid(), null);

        if (steamGameDetailsJson == null) {
            return null;
        }

        return steamGameDetailsJson.getData();
    }

    public boolean saveSteamGameDetails(String appId, String gameDetailJson) {
        steamGameDetailRepository.save(new GameDetail(appId, gameDetailJson));

        return true;
    }

    public boolean saveAllSteamGameDetails(List<Pair<String, String>> allGameDetails) {
        List<GameDetail> gameDetails = allGameDetails.stream()
            .map(gameDetailPair -> new GameDetail(gameDetailPair.getKey(), gameDetailPair.getValue()))
            .collect(Collectors.toList());

        steamGameDetailRepository.saveAll(gameDetails);

        return true;
    }
}
