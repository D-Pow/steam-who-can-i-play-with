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
import org.whocaniplaywith.app.model.SteamPlayableMultiplayerGamesResponse;

// TODO: https://stackoverflow.com/questions/5117248/spring-sqlite-in-multi-threaded-application

@RestController
@CrossOrigin
@Slf4j
public class ApplicationApi {
    @Autowired
    SteamWhoCanIPlayWithController steamWhoCanIPlayWithController;

    @PostMapping(value = "/getPlayableGamesForUser")
    public ResponseEntity<SteamPlayableMultiplayerGamesResponse> getPlayableGamesForUser(@RequestBody GetPlayableGamesRequest getPlayableGamesRequest) {
        return steamWhoCanIPlayWithController.getPlayableGamesForUser(getPlayableGamesRequest);
    }
}
