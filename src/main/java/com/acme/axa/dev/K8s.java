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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.axa.dev;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import static org.springframework.boot.cloud.CloudPlatform.KUBERNETES;
import static org.springframework.context.annotation.Bean.Bootstrap.BACKGROUND;

/**
 * Protokoll-Ausgabe, wenn Kubernetes erkannt wird.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
interface K8s {
    /**
     * Protokoll-Ausgabe, wenn Kubernetes erkannt wird.
     *
     * @return Listener zur Ausgabe, ob Kubernetes erkannt wird.
     */
    @Bean(bootstrap = BACKGROUND)
    @ConditionalOnCloudPlatform(KUBERNETES)
    default ApplicationListener<ApplicationReadyEvent> detectK8s() {
        return _ -> LoggerFactory.getLogger(K8s.class).debug("Plattform \"Kubernetes\"");
    }
}
