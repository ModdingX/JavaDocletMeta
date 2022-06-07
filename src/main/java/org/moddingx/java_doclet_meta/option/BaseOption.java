package org.moddingx.java_doclet_meta.option;

import jdk.javadoc.doclet.Doclet;

import java.util.Arrays;
import java.util.List;

public abstract class BaseOption implements Doclet.Option {

    private final List<String> names;
    private final String description;

    protected BaseOption(String description, String... names) {
        this.names = Arrays.stream(names).toList();
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
        return names;
    }
}
