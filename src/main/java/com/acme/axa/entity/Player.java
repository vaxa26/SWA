package com.acme.axa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.mapstruct.Javadoc;
import static com.acme.axa.entity.Player.STATS_BALANCE_GRAPH;
import static com.acme.axa.entity.Player.STATS_GRAPH;
import static java.util.Collections.emptyList;

/**
 * Entity Player.
 * <img src="..\..\..\..\..\..\..\extras\doc\Player.png" alt="Player    " width=400>
 */
@Entity
@Table(name = "player")
@NamedEntityGraph(name = STATS_GRAPH, attributeNodes = @NamedAttributeNode("stats"))
@NamedEntityGraph(name = STATS_BALANCE_GRAPH, attributeNodes = {
    @NamedAttributeNode("stats"), @NamedAttributeNode("balances")
})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@Setter
@Getter
@ToString
@Javadoc
@SuppressWarnings({
    "ClassFanOutComplexity",
    "RequireEmptyLineBeforeBlockTagGroup",
    "DeclarationOrder",
    "JavadocDeclaration",
    "MissingSummary",
    "RedundantSuppression", "com.intellij.jpb.LombokEqualsAndHashCodeInspection"})
public class Player {

    /**
    * Muster für gültigen Spielername.
    * Name egal ob klein oder groß, mit zahlen und mind. 4
    * lang bzw höchsten 10 lang.
    **/
    public static final String USERNAME_PATTERN =
        "^[a-zA-Z0-9]{4,10}$";
    /**
     * NamedEntityGraph für Attrobut Stats.
     */
    public static final String STATS_GRAPH = "player.stats";
    /**
     * NamedEntityGraph für Balance.
     */
    public static final String STATS_BALANCE_GRAPH = "player.balance";

    /**
     * id eines player.
     */
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Versionsnummer für Synchronisation.
     */
    @Version
    private int version;

    /**
     * Playername anhand seines Pattern.
     */
    private String playername;

    /**
     * email des Spielers
     * mit @email.
     **/
    private String email;

    /**
    * Stats eines Player.
     */
    @OneToOne(optional = false, cascade = {
        CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private Stats stats;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "player_id")
    @OrderColumn(name = "idx", nullable = false)
    @ToString.Exclude
    private List<Balance> balances;

    /**
    * rank von einem player.
    */
    @Enumerated(EnumType.STRING)
    private RankedType rank;

    /**
     * Skintyp(en) von einem Player.
     */
    @Transient
    private List<SkinType> skinTypeList;

    @Column(name = "skintypelist")
    private String skinTypeListSTR;

    private String username;

    @CreationTimestamp
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime updated;

    /**
     * Spielerdaten überschreiben.
     *
     * @param player neue Spielerdaten.
     */
    public void set(final Player player) {
        playername = player.playername;
        email = player.email;
        rank = player.rank;
        skinTypeList = player.skinTypeList;
    }

    @PrePersist
    private void buildSkinTypeList() {
        if (skinTypeList == null || skinTypeList.isEmpty()) {

            skinTypeListSTR = null;
            return;
        }
        final var stringList = skinTypeList.stream()
            .map(Enum::name)
            .toList();
        skinTypeListSTR = String.join(",", stringList);
    }

    @PostLoad
    @SuppressWarnings("java:S6204")
    private void loadSkinTypeList() {
        if (skinTypeListSTR == null) {
            skinTypeList = emptyList();
            return;
        }
        final var skintypeArray = skinTypeListSTR.split(",");
        skinTypeList = Arrays.stream(skintypeArray)
            .map(SkinType::valueOf)
            .collect(Collectors.toList());
    }
}
