package algebraic.manipulator;

import algebraic.manipulator.equation.Equation;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class WorkFile {
    private final Path path;
    private final List<String> names = new ArrayList<>();
    private final Map<String, Equation> equations = new HashMap<>();

    public WorkFile(Path path) {
        this.path = path;
    }

    public void add(String name, Equation equation) {
        if (equations.containsKey(name))
            throw new IllegalArgumentException("There already exists a work named " + name);

        names.add(name);
        equations.put(name, equation);
    }

    public void remove(String name) {
        equations.remove(name);
    }

    public Stream<String> names() {
        return names.stream();
    }

    public Equation get(String name) {
        if (!equations.containsKey(name))
            throw new IllegalArgumentException("There are no equation by the name " + name);

        return equations.get(name);
    }

    public Stream<Path> getDependencies(WorkProject project) {
        return equations.values().stream().flatMap(e -> e.getDependencies(project, this));
    }

    public Path getPath() {
        return path;
    }
}
