/**
 * Copyright (c) 2013-2021 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.redisson.client.runtime;

import io.quarkus.arc.DefaultBean;
import org.eclipse.microprofile.config.ConfigProvider;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.PropertiesConvertor;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 *
 * @author Nikita Koksharov
 *
 */
@ApplicationScoped
public class RedissonClientProducer {

    private RedissonClient redisson;

    @Produces
    @Singleton
    @DefaultBean
    public RedissonClient create() throws IOException {
        InputStream configStream;
        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue("quarkus.redisson.file", String.class);
        if (configFile.isPresent()) {
            configStream = getClass().getResourceAsStream(configFile.get());
        } else {
            configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("redisson.yaml");
        }
        String config;
        if (configStream != null) {
            byte[] array = new byte[configStream.available()];
            configStream.read(array);
            config = new String(array, StandardCharsets.UTF_8);
        } else {
            String yaml = PropertiesConvertor.toYaml("quarkus.redisson.", ConfigProvider.getConfig().getPropertyNames(), prop -> {
                return ConfigProvider.getConfig().getValue(prop, String.class);
            });
            config = yaml;
        }

        Config c = Config.fromYAML(config);
        redisson = Redisson.create(c);
        return redisson;
    }

    public void setConfig(org.eclipse.microprofile.config.Config config) {

    }

    @PreDestroy
    public void close() {
        if (redisson != null) {
            redisson.shutdown();
        }
    }

}
