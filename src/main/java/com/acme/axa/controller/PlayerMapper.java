package com.acme.axa.controller;
import com.acme.axa.entity.Balance;
import com.acme.axa.entity.Player;
import com.acme.axa.entity.Stats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import static org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT;

/**
 * Mapper zwischen Entity Klassen.
 */
@Mapper(nullValueIterableMappingStrategy = RETURN_DEFAULT, componentModel = "spring")
interface PlayerMapper {
    /**
     * DTO-Objekt von PlayerDTO in ein Objekt für Player konvertieren.
     *
     * @param dto dto Objekt für Spieler ohne Id
     * @return konvertierter Player mit null als Id
     */
    @Mapping(target = "skinTypeList", ignore = true)
    @Mapping(target = "balances", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "skinTypeListSTR", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    Player toPlayer(PlayerDTO dto);

    /**
     * DTO-Objekt von StatsDTO in ein Objekt für Stats konvertieren.
     *
     * @param dto dto Objekt für Stats ohne Player
     * @return konvertierte Stats-Objekte
     */
    @Mapping(target = "id", ignore = true)
    Stats toStats(StatsDTO dto);

    @Mapping(target = "points", ignore = true)
    @Mapping(target = "credits", ignore = true)
    @Mapping(target = "id", ignore = true)
    Balance toBalance(BalanceDTO dto);
}
