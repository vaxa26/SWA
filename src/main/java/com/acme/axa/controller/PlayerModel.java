package com.acme.axa.controller;

import com.acme.axa.entity.Player;
import com.acme.axa.entity.RankedType;
import com.acme.axa.entity.SkinType;
import com.acme.axa.entity.Stats;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 *  Model Klasse für HATEOS.
 */
@JsonPropertyOrder({
    "playername", "email", "stats", "ranked", "skins"
})
@Relation(collectionRelation = "player", itemRelation = "player")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@ToString(callSuper = true)
public class PlayerModel extends RepresentationModel<PlayerModel> {
    private final String username;

    @EqualsAndHashCode.Include
    private final String email;

    private final Stats stats;
    private final RankedType rank;
    private final List<SkinType> skins;

    PlayerModel(final Player player) {
        username = player.getPlayername();
        email = player.getEmail();
        stats = player.getStats();
        rank = player.getRank();
        skins = player.getSkinTypeList();
    }

}
