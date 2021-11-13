package io.github.noeppi_noeppi.tools.java_doclet_meta.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackagesOption extends BaseOption {

    private final List<String> packages = new ArrayList<>();
    
    public PackagesOption(String description, String... names) {
        super(description, names);
    }

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public String getParameters() {
        return "<package>";
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        this.packages.add(arguments.get(0).replace('/', '.'));
        return true;
    }
    
    public List<String> packages() {
        return Collections.unmodifiableList(this.packages);
    }
}
