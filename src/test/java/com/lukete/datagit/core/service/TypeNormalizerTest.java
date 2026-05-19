package com.lukete.datagit.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TypeNormalizerTest {
    @Test
    void shouldNormalizePostgresAliases() {
        assertThat(TypeNormalizer.normalize("int4")).isEqualTo("integer");
        assertThat(TypeNormalizer.normalize("int8")).isEqualTo("bigint");
        assertThat(TypeNormalizer.normalize("varchar")).isEqualTo("character varying");
        assertThat(TypeNormalizer.normalize("bool")).isEqualTo("boolean");
        assertThat(TypeNormalizer.normalize("timestamptz")).isEqualTo("timestamp with time zone");
    }

    @Test
    void shouldTrimWhitespaceAndIgnoreCasing() {
        assertThat(TypeNormalizer.normalize(" INT4 ")).isEqualTo("integer");
        assertThat(TypeNormalizer.normalize(" VarChar ")).isEqualTo("character varying");
        assertThat(TypeNormalizer.normalize(" TIMESTAMPTZ ")).isEqualTo("timestamp with time zone");
    }

    @Test
    void shouldHandleUnknownTypesSafely() {
        assertThat(TypeNormalizer.normalize("citext")).isEqualTo("citext");
        assertThat(TypeNormalizer.normalize(" custom_type ")).isEqualTo("custom_type");
        assertThat(TypeNormalizer.normalize(null)).isNull();
    }

    @Test
    void shouldBeIdempotent() {
        String normalized = TypeNormalizer.normalize(" INT4 ");

        assertThat(TypeNormalizer.normalize(normalized)).isEqualTo(normalized);
        assertThat(TypeNormalizer.normalize("timestamp with time zone")).isEqualTo("timestamp with time zone");
    }
}
