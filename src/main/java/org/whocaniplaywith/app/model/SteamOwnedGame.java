package org.whocaniplaywith.app.model;

import lombok.Data;

@Data
public class SteamOwnedGame {
    private Long appid;
    private String img_icon_url;
    private String img_logo_url;
    private String name;
    private Long playtime_forever;
    private Long playtime_linux_forever;
    private Long playtime_mac_forever;
    private Long playtime_windows_forever;
}
