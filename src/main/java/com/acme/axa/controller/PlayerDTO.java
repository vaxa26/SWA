package com.acme.axa.controller;

import com.acme.axa.entity.RankedType;
import com.acme.axa.entity.SkinType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Builder;




/**
 * Object für ändern oder neuanlegen eines Players.
 *
 * @param playername gültiger playername eines Spielers
 * @param email email eines Spielers
 * @param stats stats eines Spielers
 * @param rank rank eines Spielers
 * @param skins skins eines Spielers
 * @param username username des nutzers
 * @param password password des nutzers
 */
@Builder
@SuppressFBWarnings("SE_BAD_FIELD")
public record PlayerDTO(
    @NotNull
    @Pattern(regexp = USERNAME_PATTERN)
    String playername,

    @Email
    @NotNull
    String email,

    @Valid
    @NotNull(groups = OnCreate.class)
    StatsDTO stats,

    @NotNull
    RankedType rank,

    List<SkinType> skins,

    String username,
    String password

) {
    /**
     * Muster für gültigen Username.
     */
    public static final String USERNAME_PATTERN =
        "^[a-zA-Z0-9]{4,10}$";

    /**
     * Marker-Interface f&uuml;r Jakarta Validation: zus&auml;tzliche Validierung beim Neuanlegen.
     */
    interface OnCreate { }
}
