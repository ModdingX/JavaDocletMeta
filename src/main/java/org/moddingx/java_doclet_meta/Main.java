package org.moddingx.java_doclet_meta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.moddingx.java_doclet_meta.option.PackagesOption;
import org.moddingx.java_doclet_meta.option.PathOption;
import org.moddingx.java_doclet_meta.option.UselessOption;
import org.moddingx.java_doclet_meta.record.ClassData;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Main implements Doclet {
    
    public static final SourceVersion MIN_SOURCE_VERSION = SourceVersion.RELEASE_16;

    public static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }

    private final PathOption destinationDir = new PathOption("Destination directory", "-d");
    private final PackagesOption excludedPackages = new PackagesOption("Exclude a package", "--exclude-package");
    
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
                excludedPackages,
                new UselessOption(1, "-notimestamp"),
                new UselessOption(1, "-windowtitle"),
                new UselessOption(1, "-doctitle")
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SourceVersion latest = SourceVersion.latestSupported();
        return latest.ordinal() >= MIN_SOURCE_VERSION.ordinal() ? latest: MIN_SOURCE_VERSION;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        try {
            DocEnv env = new DocEnv(environment.getElementUtils(), environment.getTypeUtils(), environment.getDocTrees());
            Path base = destinationDir.path();
            List<String> excluded = excludedPackages.packages();
            DocIndex index = new DocIndex();
            for (Element element : environment.getIncludedElements()) {
                PackageElement pkg = env.elements().getPackageOf(element);
                if ((pkg == null || !excluded.contains(pkg.getQualifiedName().toString())) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                    if ((element.getKind().isClass() || element.getKind().isInterface()) && element instanceof TypeElement type) {
                        ClassData data = ClassData.from(env, type);
                        index.add(data);
                        Path dest = base.resolve(data.binaryName() + ".json");
                        Files.createDirectories(dest.getParent());
                        Files.writeString(dest, GSON.toJson(data.json()) + "\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
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
