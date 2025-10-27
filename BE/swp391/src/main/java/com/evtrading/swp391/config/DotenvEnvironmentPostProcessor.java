package com.evtrading.swp391.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        // Try cwd/.env then BE/swp391/.env
        Path cwd = Paths.get("").toAbsolutePath();
        Path beEnv = cwd.resolve("BE").resolve("swp391").resolve(".env");
        Path rootEnv = cwd.resolve(".env");
        String dirToUse = Files.exists(rootEnv) ? cwd.toString()
                : (Files.exists(beEnv) ? cwd.resolve("BE").resolve("swp391").toString()
                : cwd.toString());

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .directory(dirToUse)
                .filename(".env")
                .load();

        Map<String, Object> map = new HashMap<>();
        dotenv.entries().forEach(e -> map.put(e.getKey(), e.getValue()));
        if (!map.isEmpty()) {
            env.getPropertySources().addFirst(new MapPropertySource("dotenv", map));
        }
    }
}