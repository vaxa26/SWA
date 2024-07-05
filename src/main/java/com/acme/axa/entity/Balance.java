package com.acme.axa.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Spielwährung eines Spielers.
 */
@Entity
@Table(name = "balance")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@SuppressWarnings({"JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup", "MissingSummary"})
public class Balance {
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * In game Währung die man erspielen kann.
     *
     * @param points Points
     * @return die points eins Spielers
     */
    private int points;

    /**
     * In game Währung die man mit echtem Geld kauft.
     *
     * @param credits Credits
     * @return credits
     */
    private int credits;
}
