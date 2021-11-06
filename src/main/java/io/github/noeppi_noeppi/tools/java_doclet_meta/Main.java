package io.github.noeppi_noeppi.tools.java_doclet_meta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.noeppi_noeppi.tools.java_doclet_meta.option.PathOption;
import io.github.noeppi_noeppi.tools.java_doclet_meta.option.UselessOption;
import io.github.noeppi_noeppi.tools.java_doclet_meta.record.ClassData;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public class Main implements Doclet {

    @SuppressWarnings("TrivialFunctionalExpressionUsage")
    public static final Gson GSON = ((Supplier<Gson>) () -> {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        return builder.create();
    }).get();
    
    private final PathOption destinationDir = new PathOption("Destination directory", "-d");
    
    private Reporter reporter;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return "java-doclet-db";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(
                destinationDir,
                new UselessOption(1, "-notimestamp"),
                new UselessOption(1, "-windowtitle"),
                new UselessOption(1, "-doctitle")
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_16;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        try {
            DocEnv env = new DocEnv(environment.getElementUtils(), environment.getTypeUtils(), environment.getDocTrees());
            Path base = destinationDir.path();
            DocIndex index = new DocIndex();
            for (Element element : environment.getIncludedElements()) {
                if ((element.getKind().isClass() || element.getKind().isInterface()) && element instanceof TypeElement type) {
                    ClassData data = ClassData.from(env, type);
                    index.add(data);
                    Path dest = base.resolve(data.binaryName() + ".json");
                    Files.createDirectories(dest.getParent());
                    Files.writeString(dest, GSON.toJson(data.json()) + "\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
            Files.writeString(base.resolve("index.json"), GSON.toJson(index.json()) + "\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
