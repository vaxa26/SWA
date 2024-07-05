Create TABLE if not exists stats (

    id      uuid PRIMARY KEY USING INDEX TABLESPACE playerspace,
    wins        integer NOT NULL default 0,
    loss        INTEGER NOT NULL DEFAULT 0,
    winrate     DECIMAL(2,1) NOT NULL,
    lossrate    DECIMAL(2,1) NOT NULL,
    hotstreak   BOOLEAN NOT NULL DEFAULT False,
    loosestreak BOOLEAN NOT NULL DEFAULT FALSE
) TABLESPACE playerspace;
CREATE INDEX IF NOT EXISTS stats_wins_idx ON stats(wins) TABLESPACE playerspace;

CREATE TABLE IF NOT EXISTS player (
    id              uuid PRIMARY KEY USING INDEX TABLESPACE playerspace,

    version         integer NOT NULL DEFAULT 0,
    playername      varchar(15) NOT NULL,
    email           varchar(40) NOT NULL UNIQUE USING INDEX TABLESPACE playerspace,

    rank            varchar(12) CHECK (rank ~ 'UNRANKED|BRONZE|SILVER|GOLD|PLATINUM|EMERALD|DIAMOND|MASTER|GRANDMASTER|CHALLENGER'),
    skinTypeList    varchar(32),

    stats_id        uuid NOT NULL UNIQUE USING INDEX TABLESPACE playerspace REFERENCES stats,
    username        varchar(15) NOT NULL,

    created         timestamp NOT NULL,
    updated         timestamp NOT NULL

)TABLESPACE playerspace;

CREATE INDEX IF NOT EXISTS player_playername_idx ON player(playername) TABLESPACE playerspace;

CREATE TABLE IF NOT EXISTS balances (
    id      uuid PRIMARY KEY,

    points  INTEGER NOT NULL DEFAULT 0,
    credits INTEGER NOT NULL DEFAULT 0,

    player_id uuid REFERENCES player,
    idx integer NOT NULL DEFAULT 0
) TABLESPACE playerspace;
CREATE INDEX IF NOT EXISTS balances_player_id_idx ON balances(player_id) TABLESPACE playerspace;
