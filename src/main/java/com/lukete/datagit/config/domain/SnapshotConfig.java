package com.lukete.datagit.config.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnapshotConfig {
    private List<String> ignoredColumns;
}
