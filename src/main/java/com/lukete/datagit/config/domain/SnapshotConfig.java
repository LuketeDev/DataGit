package com.lukete.datagit.config.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnapshotConfig {
    @JsonProperty("ignoredColumns")
    @JsonAlias("ignoredColmns")
    private List<String> ignoredColumns;
}
