# syntax=docker/dockerfile:1.7.0

# Copyright (C) 2020 - present Juergen Zimmermann, Hochschule Karlsruhe
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

# Aufruf:   docker build --tag juergenzimmermann/kunde:2024.04.0-azul .
#               ggf. --progress=plain
#               ggf. --no-cache
#           Get-Content Dockerfile | docker run --rm --interactive hadolint/hadolint:2.12.1-beta-debian

# https://docs.docker.com/engine/reference/builder/#syntax
# https://github.com/moby/buildkit/blob/master/frontend/dockerfile/docs/syntax.md
# https://hub.docker.com/r/docker/dockerfile
# https://docs.docker.com/build/building/multi-stage
# https://containers.gitbook.io/build-containers-the-hard-way

ARG JAVA_VERSION=22.0.1

# ---------------------------------------------------------------------------------------
# S t a g e :   b u i l d e r
#
#   Azul Zulu: JDK, nicht JRE (s.u.)
#   Ubuntu "Jammy Jellyfish" 22.04 LTS (Long Term Support) https://ubuntu.com/about/release-cycle https://wiki.ubuntu.com/Releases
#   JAR bauen mit eigenem Code und Dependencies z.B. Spring, Jackson
# ---------------------------------------------------------------------------------------
FROM azul/zulu-openjdk:${JAVA_VERSION} AS builder

# "working directory" fuer die Docker-Kommandos RUN, ENTRYPOINT, CMD, COPY und ADD
WORKDIR /source

# ADD hat mehr Funktionalitaet als COPY, z.B. auch Download von externen Dateien

# Gradle:
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
COPY gradle ./gradle

# Maven:
#COPY pom.xml mvnw ./
#COPY .mvn ./.mvn

COPY src ./src

# JAR-Datei mit den Schichten ("layers") erstellen und aufbereiten bzw. entpacken
RUN <<EOF
# https://medium.com/vantageai/how-to-make-your-python-docker-images-secure-fast-small-b3a6870373a0
set -eux

# Gradle:
#./gradlew --no-configuration-cache --no-daemon --no-watch-fs -PjavaLanguageVersion=22 -PjavaVersion=22 bootJar
./gradlew --no-configuration-cache --no-daemon --no-watch-fs bootJar
java -Djarmode=layertools -jar ./build/libs/kunde-2024.04.0.jar extract

# Maven:
#./mvnw package spring-boot:repackage -Dmaven.test.skip=true -Dspring-boot.build-image.skip=true
#java -Djarmode=layertools -jar ./target/kunde-2024.04.0.jar extract
EOF

# ---------------------------------------------------------------------------------------
# S t a g e   f i n a l
#
#   JRE statt JDK
#   Dependencies: z.B. Spring, Jackson
#   Loader fuer Spring Boot
#   Eigener uebersetzter Code
# ---------------------------------------------------------------------------------------

FROM azul/zulu-openjdk:${JAVA_VERSION}-jre AS final

# Anzeige bei "docker inspect ..."
# https://specs.opencontainers.org/image-spec/annotations
# https://spdx.org/licenses
# https://snyk.io/de/blog/how-and-when-to-use-docker-labels-oci-container-annotations
# MAINTAINER ist deprecated https://docs.docker.com/engine/reference/builder/#maintainer-deprecated
LABEL org.opencontainers.image.title="player" \
      org.opencontainers.image.description="Microservice kunde v0 mit Basis-Image Azul Zulu und Ubuntu Jammy" \
      org.opencontainers.image.version="2024.04.0-azul" \
      org.opencontainers.image.licenses="GPL-3.0-or-later" \
      org.opencontainers.image.vendor="Juergen Zimmermann" \
      org.opencontainers.image.authors="Juergen.Zimmermann@h-ka.de" \
      org.opencontainers.image.base.name="azul/zulu-openjdk:LATEST_VERSION-jre"

# gleiches Basis-Verzeichnis wie bei "Cloud Native Buildpacks": Mounting von application.yml, private-key.pem, certificate.crt
WORKDIR /workspace

# "here document" wie in einem Shellscipt
RUN <<EOF
set -eux
apt-get update
apt-get upgrade --yes
# https://unix.stackexchange.com/questions/217369/clear-apt-get-list
apt-get autoremove -y
apt-get clean -y
rm -rf /var/lib/apt/lists/*
# https://manpages.debian.org/bookworm/passwd/groupadd.html
# https://manpages.debian.org/bookworm/adduser/addgroup.html
groupadd --gid 1000 app
# https://manpages.debian.org/bookworm/passwd/useradd.html
# https://manpages.debian.org/bookworm/adduser/adduser.html
useradd --uid 1000 --gid app --no-create-home app
chown -R app:app /workspace
EOF

# ADD hat mehr Funktionalitaet als COPY, z.B. auch Download von externen Dateien
# ggf. auch /source/snapshot-dependencies/
COPY --from=builder --chown=app:app /source/dependencies/ /source/spring-boot-loader/ /source/application/ ./

USER app
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=1 CMD wget -qO- --no-check-certificate https://localhost:8080/actuator/health/ | grep UP || exit 1

# Bei CMD statt ENTRYPOINT kann das Kommando bei "docker run ..." ueberschrieben werden
ENTRYPOINT ["java", "--enable-preview", "org.springframework.boot.loader.launch.JarLauncher"]
