package org.whocaniplaywith.app.model;

import lombok.Data;

@Data
public class SteamFriends {
    private Long friend_since;
    private String relationship;
    private String steamid;
}
