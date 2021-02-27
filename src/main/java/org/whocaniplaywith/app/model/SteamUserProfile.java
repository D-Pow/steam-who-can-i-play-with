package org.whocaniplaywith.app.model;

import lombok.Data;

@Data
public class SteamUserProfile {
    private String steamid;
    private int communityvisibilitystate;
    private int profilestate;
    private String personaname;
    private int commentpermission;
    private String profileurl;
    private String avatar;
    private String avatarmedium;
    private String avatarfull;
    private String avatarhash;
    private int lastlogoff;
    private int personastate;
    private String realname;
    private String primaryclanid;
    private int timecreated;
    private int personastateflags;
    private String loccountrycode;
    private String locstatecode;
}
