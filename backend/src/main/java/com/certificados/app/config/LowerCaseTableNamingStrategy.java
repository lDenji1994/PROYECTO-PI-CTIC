package com.certificados.app.config;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class LowerCaseTableNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(
            Identifier name,
            JdbcEnvironment context) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(
            Identifier name,
            JdbcEnvironment context) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(
            Identifier name,
            JdbcEnvironment context) {

        if (name == null) {
            return null;
        }

        return Identifier.toIdentifier(
                name.getText().toLowerCase(Locale.ROOT),
                name.isQuoted()
        );
    }

    @Override
    public Identifier toPhysicalSequenceName(
            Identifier name,
            JdbcEnvironment context) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(
            Identifier name,
            JdbcEnvironment context) {
        return name;
    }
}