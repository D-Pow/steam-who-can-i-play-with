package org.whocaniplaywith.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.whocaniplaywith.app.model.GetPlayableGamesRequest;
import org.whocaniplaywith.app.model.SteamUserProfile;
import org.whocaniplaywith.app.service.SteamService;

import java.util.concurrent.ExecutionException;

@Controller
@Slf4j
public class SteamWhoCanIPlayWithController {
    @Autowired
    SteamService steamService;

    public ResponseEntity<Object> getPlayableGamesForUser(@RequestBody GetPlayableGamesRequest getPlayableGamesRequest) {
        String username = getPlayableGamesRequest.getUsername();
        String steamUserId = null;

        log.info("Request = {}", getPlayableGamesRequest);

        try {
            steamUserId = steamService.getSteamIdFromUsername(username).get();
            SteamUserProfile userProfile = steamService.getUserProfile(steamUserId).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Could not get Steam UserId future. Error = {}", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }

        return ResponseEntity.ok(steamUserId);
    }
}
