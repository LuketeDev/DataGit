package com.lukete.datagit.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RowChangeType {
    INSERTED("inserted"),
    DELETED("deleted"),
    UPDATED("updated");

    private final String label;

}