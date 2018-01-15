package algebraic.manipulator.read;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.read.equation.EquationTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileTemplate {
    private final Path path;
    private final List<String> names = new ArrayList<>();
    private final Map<String, EquationTemplate> equations = new HashMap<>();
    private final Map<String, Path> imports = new HashMap<>();

    public FileTemplate(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Stream<Path> getDependencies() {
        return equations.values().stream().flatMap(e -> e.getDependencies(this));
    }

    public WorkFile toFile(WorkProject project) {
        WorkFile file = new WorkFile(path);
        imports.forEach(file::importFile);

        for (String name : names) {
            try {
                file.add(name, equations.get(name).toEquation(project, file));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to build " + name + " in " + path, e);
            }
        }

        return file;
    }

    public void add(String name, EquationTemplate equation) {
         names.add(name);
         equations.put(name, equation);
    }

    public void importFile(String key, Path path) {
        if (imports.containsKey(key))
            throw new IllegalArgumentException(key + " is already imported");
        imports.put(key, path);
    }

    public Path absolutePath(Path path) {
        switch (path.getNameCount()) {
            case 1:
                return getPath().resolve(path);
            case 2:
                if (imports.containsKey(path.getName(0).toString()))
                    return imports.get(path.getName(0).toString()).resolve(path.getFileName());
                return getPath().getParent().resolve(path);
            default:
                return path;
        }
    }
}
