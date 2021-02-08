package org.whocaniplaywith;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.whocaniplaywith.app.controller.SteamWhoCanIPlayWithController;
import org.whocaniplaywith.app.model.GetPlayableGamesRequest;

@RestController
@CrossOrigin
@Slf4j
public class ApplicationApi {
    @Autowired
    SteamWhoCanIPlayWithController steamWhoCanIPlayWithController;

    @PostMapping(value = "/getPlayableGames")
    public ResponseEntity<Object> getVideosForEpisode(@RequestBody GetPlayableGamesRequest getPlayableGamesRequest) {
        return steamWhoCanIPlayWithController.getVideosForEpisode(getPlayableGamesRequest);
    }
}
