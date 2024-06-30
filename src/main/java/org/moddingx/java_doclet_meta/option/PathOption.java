package org.moddingx.java_doclet_meta.option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class PathOption extends BaseOption {

    private Path path = null;

    public PathOption(String description, String... names) {
        super(description, names);
    }
    
    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public String getParameters() {
        return "<path>";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        path = Paths.get(arguments.getFirst());
        return true;
    }

    public Path path() {
        return Objects.requireNonNull(path, "Option not set.");
    }
}
