package com.acme.axa.service;

import com.acme.axa.entity.Player;
import com.acme.axa.repository.PlayerRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anwendungslogik für Player.
 * <img src="..\..\..\..\..\..\..\extras\doc\PlayerWriteService.png" alt="PlayerWriteService" width=400>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PlayerWriteService {
    private final PlayerRepository rep;

    /**
     * Neuanlegen eines Spielers.
     *
     * @param player objekt das neuangelegt wird
     * @return neuer Player mit erstellten ID
     * @throws EmailExistException    Es gibt bereits so eine Email
     * @throws UsernameExistException Es gibt bereits so ein Username
     */
    @Transactional
    @SuppressWarnings("TrailingComment")
    public Player create(final Player player) {
        log.debug("Create player : {}", player);
        log.debug("create: stats={}", player.getStats());
        log.debug("create: balance={}", player.getBalances());

        if (rep.existsByEmail(player.getEmail())) {
            throw new EmailExistException(player.getEmail());
        }

        if (rep.existsByPlayername(player.getPlayername())) {
            throw new UsernameExistException(player.getPlayername());
        }
        player.setUsername("user");

        final var playerData = rep.save(player);

        log.trace("create: Thread-ID={}", Thread.currentThread().threadId());

        log.debug("create: playerData={}", playerData);
        return playerData;

    }

    /**
     * Aktualiesieren eines Spielers.
     *
     * @param player Objekt mit neuen Daten ohne Id
     * @param id     von dem aktualisiertem Player
     * @param version dier erforderte Version
     * @return aktualisierter plpayer
     * @throws EmailExistException    ist email schon vorhanden
     * @throws UsernameExistException ist playername schon vorhanden
     */
    @Transactional
    public Player update(final Player player, final UUID id, final int version) {
        log.debug("Update : {}", player);
        log.debug("Update id : {}", id, version);

        var playerData = rep
            .findById(id)
            .orElseThrow(() -> new NotFoundException(id));
        log.trace("update: version={}, playerData={}", version, playerData);

        if (version != playerData.getVersion()) {
            throw new VersionOutdatedException(version);
        }
        final var email = player.getEmail();
        if (!Objects.equals(email, playerData.getEmail()) && rep.existsByEmail(email)) {
            throw new EmailExistException(email);
        }
        final var playername = player.getPlayername();
        if (!Objects.equals(playername, playerData.getPlayername()) && rep.existsByPlayername(playername)) {
            log.debug("Update player : {}", player);
            throw new UsernameExistException(playername);
        }
        log.trace("update; Kein Konflikt mit Emailadressse");
        playerData.set(player);
        playerData = rep.save(playerData);

        log.debug("update:{}", playerData);
        return playerData;
    }
}
