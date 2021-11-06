package io.github.noeppi_noeppi.tools.java_doclet_meta;

import com.sun.source.util.DocTrees;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public record DocEnv(Elements elements, Types types, DocTrees docs) {}
