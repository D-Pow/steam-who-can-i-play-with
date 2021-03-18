package org.whocaniplaywith.dao.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.whocaniplaywith.app.model.GameDetail;

public interface SteamGameDetailRepository extends JpaRepository<GameDetail, String> {
    @Query("SELECT gameDetail FROM GameDetail gameDetail WHERE gameDetail.steamAppid = :appId")
    GameDetail getSteamGameDetailsById(@Param("appId") String appId);
}
