response.setSharedMultiplayerGames(
    sharedMultiplayerGamesById.entrySet().stream()
    .map()
    );

    log.info("Differences: User owned [{}], user multiplayer [{}], friends owned [{}], friends multiplayer [{}]",
    ownedGamesDetails.size(),
    ownedMultiplayerGames.size(),
    friendsGameDetails.size(),
    friendsMultiplayerGames.size()
    );

    log.info("User owned games: {}",
    ownedMultiplayerGames.stream()
    .map(SteamGameDetails::getName)
    .collect(Collectors.toList())
    );

    log.info("Friend owned games: {}",
    friendsMultiplayerGames.entrySet().stream()
    .map(entry -> Arrays.asList(
    entry.getKey(),
    entry.getValue().stream().map(SteamGameDetails::getName).collect(Collectors.toList())
    ))
    .collect(Collectors.toList())
    );