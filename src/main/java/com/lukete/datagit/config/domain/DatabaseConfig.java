package com.lukete.datagit.config.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

/**
 * Database connection settings
 */
public class DatabaseConfig {
    private String type;
    private String host;
    private Integer port;
    private String name;
    private String username;
    private String password;
}
