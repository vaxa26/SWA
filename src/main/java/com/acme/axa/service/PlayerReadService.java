package com.acme.axa.service;

import com.acme.axa.entity.Player;
import com.acme.axa.repository.PlayerRepository;
import com.acme.axa.repository.SpecificationBuilder;
import com.acme.axa.security.Rolle;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Player suchen.
 * <img src="..\..\..\..\..\..\..\extras\doc\PlayerReadService.png" alt="PlayerReadService" width=400>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PlayerReadService {
    private final PlayerRepository repo;
    private final SpecificationBuilder specificationBuilder;

    /**
     * Player anhand seiner ID suchen.
     *
     * @param id des geuschten Player
     * @param username Benutzername von jwt
     * @param roles rollen aus den Enums
     * @param fetchBalance true fals balances mitgeladen werden muss
     * @return gefundener Player
     * @throws NotFoundException falls kein Player gefunden wurde
     * @throws AccesForbiddenException falls Rolle nicht angegeben
     */
    @Observed(name = "find-by.id")
    public @NonNull Player findById(
        final UUID id,
        final String username,
        final List<Rolle> roles,
        final boolean fetchBalance
    ) {
        log.debug("findById {}, username={}, roles={}", id, username, roles);

        final var playerOptional = fetchBalance ? repo.findByIdFetchStats(id) : repo.findById(id);
        final var player = playerOptional.orElse(null);
        log.trace("findById: player={}", player);

        if (player != null && player.getUsername().contentEquals(username)) {
            return player;
        }

        if (!roles.contains(Rolle.ADMIN)) {
            throw new AccesForbiddenException(roles);
        }

        if (player == null) {
            throw new NotFoundException(id);
        }
        log.debug("findById: player={}, balance={}", player, fetchBalance ? player.getBalances() : "N/A");
        return player;
    }

    /**
     * Player anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Suchkriterien
     * @return gesuchte player oder leere Collection
     */
    @SuppressWarnings({"ReturnCount", "NestedIfDepth"})
    public @NonNull Collection<Player> find(@NotNull final Map<String, List<String>> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var playername = suchkriterien.get("playername");
            if (playername != null && playername.size() == 1) {
                return findByPlayername(playername.getFirst(), suchkriterien);
            }

            final var emails = suchkriterien.get("emails");
            if (emails != null && emails.size() == 1) {
                return findByEmail(emails.getFirst(), suchkriterien);
            }
        }

        final var specification = specificationBuilder
            .build(suchkriterien)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var player = repo.findAll(specification);
        if (player.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", player);
        return player;

    }

    private List<Player> findByPlayername(final String playername, final Map<String, List<String>> suchkriterien) {
        log.trace("findByPlayername: {}", playername);
        final var player = repo.findByPlayername(playername);
        if (player.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("findByPlayername: {}", player);
        return player;
    }

    private Collection<Player> findByEmail(final String email, final Map<String, List<String>> suchkriterien) {
        log.trace("findByEmail: {}", email);
        final var player = repo
            .findbyEmail(email)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var players = List.of(player);
        log.debug("findByEmail: {}", players);
        return players;
    }
}
