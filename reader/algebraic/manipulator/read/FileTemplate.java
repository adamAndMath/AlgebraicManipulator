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

    public FileTemplate(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Stream<Path> getDependencies(ProjectTemplate project) {
        return equations.values().stream().flatMap(e -> e.getDependencies(project, this));
    }

    public WorkFile toFile(WorkProject project) {
        WorkFile file = new WorkFile(path);

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
}
