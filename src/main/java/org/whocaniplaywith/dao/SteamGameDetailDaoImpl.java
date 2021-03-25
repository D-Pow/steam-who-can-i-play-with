package org.whocaniplaywith.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.whocaniplaywith.app.model.GameDetail;
import org.whocaniplaywith.app.model.SteamGameDetails;
import org.whocaniplaywith.app.model.SteamGameDetailsResponse;
import org.whocaniplaywith.app.utils.Pair;
import org.whocaniplaywith.dao.persistence.SteamGameDetailRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository("SteamGameDetailDao")
public class SteamGameDetailDaoImpl implements SteamGameDetailDao {
    SteamGameDetailRepository steamGameDetailRepository;
    Map<String, SteamGameDetails> steamGameDetailsKeyValMap= new HashMap<>();

    public SteamGameDetailDaoImpl(@Autowired SteamGameDetailRepository steamGameDetailRepository) {
        this.steamGameDetailRepository = steamGameDetailRepository;

        steamGameDetailRepository.findAll().forEach(gameDetail -> {
            SteamGameDetails steamGameDetails = getSteamGameDetailsFromAppIdAndJsonStinrg(gameDetail);

            steamGameDetailsKeyValMap.put(steamGameDetails.getSteamAppid(), steamGameDetails);
        });
    }

    private SteamGameDetails getSteamGameDetailsFromAppIdAndJsonStinrg(GameDetail gameDetail) {
        SteamGameDetailsResponse.GameDetailsResponse steamGameDetailsJson = SteamGameDetailsResponse
            .getAppIdDetailsMap(gameDetail.getGameDetailJson())
            .getOrDefault(gameDetail.getSteamAppid(), null);

        if (steamGameDetailsJson == null) {
            return null;
        }

        return steamGameDetailsJson.getData();
    }

    private SteamGameDetails getSteamGameDetailsFromAppIdAndJsonStinrg(String appId, String gameDetailsJson) {
        SteamGameDetailsResponse.GameDetailsResponse steamGameDetailsJson = SteamGameDetailsResponse
            .getAppIdDetailsMap(gameDetailsJson)
            .getOrDefault(appId, null);

        if (steamGameDetailsJson == null) {
            return null;
        }

        return steamGameDetailsJson.getData();
    }

    public SteamGameDetails getSteamGameDetails(String appId) {
        if (steamGameDetailsKeyValMap.containsKey(appId)) {
            return steamGameDetailsKeyValMap.get(appId);
        }

        GameDetail gameDetails = steamGameDetailRepository.getSteamGameDetailsById(appId);

        if (gameDetails == null) {
            return null;
        }

        return getSteamGameDetailsFromAppIdAndJsonStinrg(gameDetails);
    }

    public boolean saveSteamGameDetails(String appId, String gameDetailJson) {
        if (!steamGameDetailsKeyValMap.containsKey(appId)) {
            steamGameDetailsKeyValMap.put(
                appId,
                getSteamGameDetailsFromAppIdAndJsonStinrg(appId, gameDetailJson)
            );

            steamGameDetailRepository.save(new GameDetail(appId, gameDetailJson));
        }

        return true;
    }

    public boolean saveAllSteamGameDetails(List<Pair<String, String>> allGameDetails) {
        List<Pair<String, String>> unsavedGameDetails = allGameDetails.stream()
            .filter(gameDetailPair -> !steamGameDetailsKeyValMap.containsKey(gameDetailPair.getKey()))
            .collect(Collectors.toList());

        if (!unsavedGameDetails.isEmpty()) {
            List<GameDetail> gameDetails = unsavedGameDetails.stream()
                .peek(gameDetailPair -> steamGameDetailsKeyValMap.put(
                    gameDetailPair.getKey(),
                    getSteamGameDetailsFromAppIdAndJsonStinrg(gameDetailPair.getKey(), gameDetailPair.getValue())
                ))
                .map(gameDetailPair -> new GameDetail(gameDetailPair.getKey(), gameDetailPair.getValue()))
                .collect(Collectors.toList());

            steamGameDetailRepository.saveAll(gameDetails);
        }

        return true;
    }
}
