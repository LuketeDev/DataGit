package com.lukete.datagit.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class RowChangeTest {
    @Test
    void shouldUseEmptyFieldChangesWhenNullIsPassed() {
        RowChange change = new RowChange(Map.of("id", 1), Map.of("id", 1), null);

        assertThat(change.fieldChanges()).isEmpty();
    }

    @Test
    void shouldDefensivelyCopyFieldChanges() {
        List<FieldChange> fieldChanges = new ArrayList<>();
        fieldChanges.add(new FieldChange("name", "before", "after"));

        RowChange change = new RowChange(Map.of("id", 1), Map.of("id", 1), fieldChanges);
        fieldChanges.add(new FieldChange("age", 20, 21));

        assertThat(change.fieldChanges()).containsExactly(new FieldChange("name", "before", "after"));
        assertThatThrownBy(() -> change.fieldChanges().add(new FieldChange("city", "SP", "RJ")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldAllowInsertedAndDeletedRowsWithEmptyFieldChanges() {
        RowChange inserted = new RowChange(null, Map.of("id", 1), List.of());
        RowChange deleted = new RowChange(Map.of("id", 2), null, List.of());

        assertThat(inserted.fieldChanges()).isEmpty();
        assertThat(deleted.fieldChanges()).isEmpty();
    }
}
