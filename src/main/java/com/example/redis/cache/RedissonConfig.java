package com.example.redis.cache;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        String configPath = "redisson/redisson-single.yml";
        String configPath2 = "redisson/redisson-cluster.yml";

        RedissonClient redisson = Redisson.create(
                Config.fromYAML(new ClassPathResource(configPath).getInputStream()));
        return redisson;
    }
}
