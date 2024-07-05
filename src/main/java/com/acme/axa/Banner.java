/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.axa;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * Banner als String-Konstante für den Start des Servers.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@SuppressWarnings({
    "AccessOfSystemProperties",
    "CallToSystemGetenv",
    "UtilityClassCanBeEnum",
    "UtilityClass",
    "ClassUnconnectedToPackage"
})
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
final class Banner {

    // http://patorjk.com/software/taag/#p=display&f=Slant&t=kunde%202024.04.0
    private static final String FIGLET = """

                         ."-,.__
                         `.     `.  ,
                      .--'  .._,'"-' `.
                     .    .'         `'
                     `.   /          ,'
                       `  '--.   ,-"'
                        `"`   |  \\
                           -. \\, |
                            `--Y.'      ___.
                                 \\     L._, \\
                       _.,        `.   <  <\\                _
                     ,' '           `, `.   | \\            ( `
                  ../, `.            `  |    .\\`.           \\ \\_
                 ,' ,..  .           _.,'    ||\\l            )  '".
                , ,'   \\           ,'.-.`-._,'  |           .  _._`.
              ,' /      \\ \\        `' ' `--/   | \\          / /   ..\\
            .'  /        \\ .         |\\__ - _ ,'` `        / /     `.`.
            |  '          ..         `-...-"  |  `-'      / /        . `.
            | /           |L__           |    |          / /          `. `.
           , /            .   .          |    |         / /             ` `
          / /          ,. ,`._ `-_       |    |  _   ,-' /               ` \\
         / .           \\"`_/. `-_ \\_,.  ,'    +-' `-'  _,        ..,-.    \\`.
        .  '         .-f    ,'   `    '.       \\__.---'     _   .'   '     \\ \\
        ' /          `.'    l     .' /          \\..      ,_|/   `.  ,'`     L`
        |'      _.-""` `.    \\ _,'  `            \\ `.___`.'"`-.  , |   |    | \\
        ||    ,'      `. `.   '       _,...._        `  |    `/ '  |   '     .|
        ||  ,'          `. ;.,.---' ,'       `.   `.. `-'  .-' /_ .'    ;_   ||
        || '              V      / /           `   | `   ,'   ,' '.    !  `. ||
        ||/            _,-------7 '              . |  `-'    l         /    `||
        . |          ,' .-   ,' ||               | .-.        `.      .'     ||
         `'        ,'    `".'    |               |    `.        '. -.'       `'
                  /      ,'      |               |,'    \\-.._,.'/'
                  .     /        .               .       \\    .''
                .`.    |         `.             /         :_,'.'
                  \\ `...\\   _     ,'-.        .'         /_.-'
                   `-.__ `,  `'   .  _.>----''.  _  __  /
                        .'        /"'          |  "'   '_
                       /_|.-'\\ ,".             '.'`__'-( \\
                         / ,"'"\\,'               `/  `-.|" mh
        """;
    private static final String SERVICE_HOST = System.getenv("KUNDE_SERVICE_HOST");
    private static final String KUBERNETES = SERVICE_HOST == null
        ? "N/A"
        : STR."KUNDE_SERVICE_HOST=\{SERVICE_HOST}, KUNDE_SERVICE_PORT=\{System.getenv("KUNDE_SERVICE_PORT")}";

    /**
     * Banner für den Server-Start.
     */
    static final String TEXT = STR."""

        \{FIGLET}
        (C) Juergen Zimmermann, Hochschule Karlsruhe
        Version             2024.04.0
        Spring Boot         \{SpringBootVersion.getVersion()}
        Spring Security     \{SpringSecurityCoreVersion.getVersion()}
        Spring Framework    \{SpringVersion.getVersion()}
        Hibernate           \{org.hibernate.Version.getVersionString()}
        Java                \{Runtime.version()} - \{System.getProperty("java.vendor")}
        Betriebssystem      \{System.getProperty("os.name")}
        Rechnername         \{getLocalhost().getHostName()}
        IP-Adresse          \{getLocalhost().getHostAddress()}
        Heap: Size          \{Runtime.getRuntime().totalMemory() / (1024L * 1024L)} MiB
        Heap: Free          \{Runtime.getRuntime().freeMemory() / (1024L * 1024L)} MiB
        Kubernetes          \{KUBERNETES}
        Username            \{System.getProperty("user.name")}
        JVM Locale          \{Locale.getDefault().toString()}
        GraphiQL            /graphiql
        OpenAPI             /swagger-ui.html /v3/api-docs.yaml
        H2 Console          /h2-console (JDBC URL: "jdbc:h2:mem:testdb" mit User "sa" und Passwort "")
        """;

    @SuppressWarnings("ImplicitCallToSuper")
    private Banner() {
    }

    private static InetAddress getLocalhost() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
