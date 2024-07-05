package com.acme.axa.repository;

import com.acme.axa.entity.Player;
import com.acme.axa.entity.Player_;
import com.acme.axa.entity.RankedType;
import com.acme.axa.entity.SkinType;
import com.acme.axa.entity.Stats_;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Singleton-Klasse, um Specifications für Queries in Spring Data JPA zu bauen.
 */
@Component
@Slf4j
@SuppressWarnings({"LambdaParameterName", "IllegalIdentifierName"})
public class SpecificationBuilder {
    /**
     * Specification für eine Query mit Spring Data bauen.
     *
     * @param queryParams als MultiValueMap
     * @return Specification für eine Query mit Spring Data
     */
    public Optional<Specification<Player>> build(final Map<String, ? extends List<String>> queryParams) {
        log.debug("build: queryParams={}", queryParams);

        if (queryParams.isEmpty()) {
            return Optional.empty();
        }

        final var specs = queryParams
            .entrySet()
            .stream()
            .map(this::toSpecification)
            .toList();

        if (specs.isEmpty() || specs.contains(null)) {
            return Optional.empty();
        }

        return Optional.of(Specification.allOf(specs));
    }

    @SuppressWarnings("CyclomaticComplexity")
    private Specification<Player> toSpecification(final Map.Entry<String, ? extends List<String>> entry) {
        log.trace("toSpec: entry:={}", entry);
        final var key = entry.getKey();
        final var values = entry.getValue();
        if (Objects.equals(key, "skinType")) {
            return toSpecificationSkyinType(values);
        }

        if (values == null || values.size() != 1) {
            return null;
        }

        final var value = values.getFirst();
        return switch (key) {
            case "playername" -> playername(value);
            case "email" -> email(value);
            case "rankedType" -> rankedType(value);
            case "skinType" -> skinType(value);
            case "wins" -> wins(value);
            case "loss" -> loss(value);
            default -> null;
        };
    }

    @SuppressWarnings({"CatchParameterName", "LocalFinalVariableName"})
    private Specification<Player> toSpecificationSkyinType(final Collection<String> skins) {
        log.trace("build: skins:={}", skins);
        if (skins == null || skins.isEmpty()) {
            return null;
        }

        final var specsImmutable = skins.stream()
            .map(this::skinType)
            .toList();
        if (specsImmutable.contains(null) || specsImmutable.isEmpty()) {
            return null;
        }

        final List<Specification<Player>> specs = new ArrayList<>(specsImmutable);
        final var first = specs.removeFirst();
        return specs.stream().reduce(first, Specification::and);
    }

    private Specification<Player> playername(final String part) {
        return (root, _, builder) -> builder.like(
            builder.lower(root.get(Player_.playername)),
            builder.lower(builder.literal(STR."%\{part}%"))
        );
    }

    private Specification<Player> email(final String part) {
        return (root, _, builder) -> builder.like(
            builder.lower(root.get(Player_.email)),
            builder.lower(builder.literal(STR."%\{part}%"))
        );
    }

    private Specification<Player> rankedType(final String rank) {
        return (root, _, builder) -> builder.equal(
            root.get(Player_.rank),
            RankedType.of(rank)
        );
    }

    private Specification<Player> skinType(final String skin) {
        final var skinEnum = SkinType.of(skin);
        if (skinEnum == null) {
            return null;
        }
        return (root, _, builder) -> builder.like(
            root.get(Player_.skinTypeListSTR),
            builder.literal(STR."%\{skinEnum.name()}%")
        );
    }

    private Specification<Player> wins(final String prefix) {
        return (root, _, builder) -> builder.like(root.get(Player_.stats)
            .get(String.valueOf(Stats_.wins)), prefix + '%');
    }

    private Specification<Player> loss(final String prefix) {
        return (root, _, builder) -> builder.like(root.get(Player_.stats)
            .get(String.valueOf(Stats_.loss)), prefix + '%');
    }

}
