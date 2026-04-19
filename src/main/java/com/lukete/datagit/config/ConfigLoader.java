package com.lukete.datagit.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.config.domain.DataGitConfig;
import com.lukete.datagit.config.exception.ConfigFileNotFoundException;
import com.lukete.datagit.config.exception.InvalidConfigException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigLoader {
    private final ObjectMapper objMapper;

    public DataGitConfig load(Path cfgPath) {
        if (cfgPath == null || !Files.exists(cfgPath)) {
            throw new ConfigFileNotFoundException(String.valueOf(cfgPath));
        }

        try {
            return objMapper.readValue(cfgPath.toFile(), DataGitConfig.class);
        } catch (IOException e) {
            throw new InvalidConfigException(String.valueOf(cfgPath), e);
        }
    }
}
