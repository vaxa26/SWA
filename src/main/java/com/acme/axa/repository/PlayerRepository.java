package com.acme.axa.repository;

import com.acme.axa.entity.Player;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import static com.acme.axa.entity.Player.STATS_BALANCE_GRAPH;
import static com.acme.axa.entity.Player.STATS_GRAPH;

/**
 *  Repository für den DB-Zugriff.
 */
public interface PlayerRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {
    @EntityGraph(STATS_GRAPH)
    @NonNull
    @Override
    List<Player> findAll();

    @EntityGraph(STATS_GRAPH)
    @NonNull
    @Override
    Optional<Player> findById(@NonNull UUID id);

    /**
     * Player mit email aus DB finden.
     *
     * @param email -adresse für die suche
     *
     * @return gefundener Player oder keiner
     */
    @Query("""
         SELECT p
         from #{#entityName} p
         WHERE lower(p.email) LIKE concat(lower(:email), '%')
         """)
    @EntityGraph(STATS_GRAPH)
    Optional<Player> findbyEmail(String email);

    /**
     * Player einschließlich Stats anhand ID finden.
     *
     * @param id Player ID
     * @return Gefundener Player
     */
    @Query("""
         SELECT DISTINCT p
         FROM #{#entityName} p
         WHERE p.id = :id
         """)
    @EntityGraph(STATS_BALANCE_GRAPH)
    @NonNull
    Optional<Player> findByIdFetchStats(UUID id);

    /**
     * Player anhnad Playername in der DB suchen.
     *
     * @param playername (Teil-) Name für geuschten Player
     * @return gefundener Player oder leere Collection
     */
    @Query("""
        SELECT p
        FROM #{#entityName} p
        where  lower(p.playername) LIKE concat('%', lower(:playername), '%')
        ORDER BY p.playername""")
    @EntityGraph(STATS_GRAPH)
    List<Player> findByPlayername(CharSequence playername);

    /**
     * Abfrage ob Playername schon existiert.
     *
     * @param playername playername für Suche
     *
     * @return true wenns noch nicht gibt false wenn schon vorhanden
     */
    boolean existsByPlayername(String playername);

    /**
     * Abfrage ob Email schon existiert.
     *
     * @param email email für suche
     *
     * @return true wenns noch nicht gibt false wenn schon vorhanden
     */
    boolean existsByEmail(String email);
}
