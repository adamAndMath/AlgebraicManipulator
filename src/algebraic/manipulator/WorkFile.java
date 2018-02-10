package algebraic.manipulator;

import algebraic.manipulator.equation.Equation;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class WorkFile {
    private final Path path;
    private final List<String> names = new ArrayList<>();
    private final Map<String, Equation> equations = new HashMap<>();
    private final Map<String, Path> using = new HashMap<>();

    public WorkFile(Path path) {
        this.path = path;
    }

    public void add(String name, Equation equation) {
        if (contains(name))
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

    public boolean contains(String name) {
        return equations.containsKey(name);
    }

    public Equation get(String name) {
        if (!contains(name))
            throw new IllegalArgumentException("There are no equation by the name " + name);

        return equations.get(name);
    }

    public Stream<Path> getDependencies() {
        return equations.values().stream().flatMap(e -> e.getDependencies(this));
    }

    public Path getPath() {
        return path;
    }

    public void usingFile(String key, Path path) {
        if (using.containsKey(key))
            throw new IllegalArgumentException(key + " is already imported");
        using.put(key, path);
    }

    public Map<String, Path> getUsing() {
        return Collections.unmodifiableMap(using);
    }

    public Path absolutePath(Path path) {
        switch (path.getNameCount()) {
            case 1:
                return getPath().resolve(path);
            case 2:
                if (using.containsKey(path.getName(0).toString()))
                    return using.get(path.getName(0).toString()).resolve(path.getFileName());
                return getPath().getParent().resolve(path);
            default:
                return path;
        }
    }
}
