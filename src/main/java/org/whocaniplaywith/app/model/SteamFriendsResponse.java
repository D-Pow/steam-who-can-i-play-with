package org.whocaniplaywith.app.model;

import lombok.Data;

import java.util.List;

@Data
public class SteamFriendsResponse {
    @Data
    public static class FriendsList {
        private List<SteamFriends> friends;
    }

    private FriendsList friendslist;
}
