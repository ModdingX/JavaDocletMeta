package org.moddingx.java_doclet_meta.option;

import java.util.List;

public class UselessOption extends BaseOption {

    private final int args;

    public UselessOption(int args, String... names) {
        super("Does nothing. Provided for compatibility.", names);
        this.args = args;
    }
    
    @Override
    public int getArgumentCount() {
        return args;
    }

    @Override
    public String getParameters() {
        return "<arg> ".repeat(this.args).trim();
    }

    @Override
    public boolean process(String option, List<String> arguments) {
        return true;
    }
}
