CREATE TABLE IF NOT EXISTS game_detail (
    steamAppid VARCHAR(32) NOT NULL PRIMARY KEY,
    game_detail_json VARCHAR(1024) NOT NULL
);

CREATE TABLE IF NOT EXISTS game_detail_individual_columns (
    type VARCHAR(32),
    name VARCHAR(64) NOT NULL,
    steamAppid VARCHAR(32) NOT NULL PRIMARY KEY,
    requiredAge INTEGER,
    isFree INTEGER,
    detailedDescription VARCHAR(256),
    aboutTheGame VARCHAR(256),
    shortDescription VARCHAR(256),
    supportedLanguages VARCHAR(128),
    headerImage VARCHAR(32),
    website VARCHAR(32),
    pcRequirements VARCHAR(256), -- TODO
    macRequirements VARCHAR(256), -- TODO
    linuxRequirements VARCHAR(256), -- TODO
    legalNotice VARCHAR(32),
    developers VARCHAR(256), -- TODO
    publishers VARCHAR(256), -- TODO
    platforms VARCHAR(64), -- TODO
    metacritic VARCHAR(256), -- TODO
    categories VARCHAR(512), -- TODO
    genres VARCHAR(512), -- TODO
    screenshots VARCHAR(512), -- TODO
    recommendations VARCHAR(32), -- TODO
    releaseDate VARCHAR(64), -- TODO
    supportInfo VARCHAR(128), -- TODO
    background VARCHAR(128),
    contentDescriptors VARCHAR(512) -- TODO
);
