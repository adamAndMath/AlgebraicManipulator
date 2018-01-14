package algebraic.manipulator.read;

import algebraic.manipulator.WorkProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectTemplate {
    public static class WPFile implements WorkProject.WP {
        final FileTemplate file;

        public WPFile(FileTemplate file) {
            this.file = file;
        }

        @Override
        public <T extends WorkProject.WP, R> R get(Path path, BiFunction<Path, T, R> function) {
            return function.apply(path, (T) this);
        }

        @Override
        public <T extends WorkProject.WP, R> R get(Path path, Function<T, R> function) {
            if (path.getNameCount() != 0) throw new IllegalArgumentException();
            return function.apply((T) this);
        }

        @Override
        public <T extends WorkProject.WP> void run(Path path, BiConsumer<Path, T> consumer) {
            consumer.accept(path, (T) this);
        }

        @Override
        public <T extends WorkProject.WP> void run(Path path, Consumer<T> consumer) {
            if (path.getNameCount() != 0) throw new IllegalArgumentException();
            consumer.accept((T) this);
        }
    }

    private final WorkProject.WPFolder root = new WorkProject.WPFolder();
    private final List<FileTemplate> files = new ArrayList<>();

    public ProjectTemplate(Path projectPath) throws IOException {
        Files.walk(projectPath).filter(Files::isRegularFile).map(path -> readFile(projectPath, path)).forEach(this::put);
    }

    public FileTemplate readFile(Path projectPath, Path path) {
        try {
            String name = path.getFileName().toString();
            name = name.substring(0, name.indexOf('.'));
            Path relativePath = projectPath.relativize(path).getParent();
            return WorkReader.readFile(new TokenReader(Files.newBufferedReader(path)), relativePath == null ? Paths.get(name) : relativePath.resolve(name));
        } catch (IOException e) {
            System.err.println("Failed to read: " + path);
            e.printStackTrace(System.err);
            return null;
        }
    }

    public FileTemplate getFile(Path path) {
        return root.get(path, (Path p, WPFile f) -> f.file);
    }

    public void put(FileTemplate file) {
        root.run(file.getPath().getParent(), (WorkProject.WPFolder f) -> f.contents.put(file.getPath().getFileName().toString(), new WPFile(file)));
        files.add(file);
    }

    public Path absolutePath(FileTemplate file, Path path) {
        if (path.getNameCount() == 1)
            return file.getPath().resolve(path);

        return path;
    }

    public WorkProject toProject() {
        WorkProject project = new WorkProject();
        topologicalSort(files).forEach(f -> project.put(f.toFile(project), f.getPath()));
        return project;
    }

    private List<FileTemplate> topologicalSort(List<FileTemplate> files) {
        Map<Path, Integer> indexMap = new HashMap<>();

        for (int i = 0; i < files.size(); i++)
            indexMap.put(files.get(i).getPath(), i);

        List<Set<Integer>> graph = files.stream()
                .map(f -> f.getDependencies(this).map(Path::getParent).map(indexMap::get).collect(Collectors.toSet()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Set<Integer>> graphInverse = new ArrayList<>(graph.size());

        for (int i = 0; i < graph.size(); i++) {
            graph.get(i).remove(i);
            graphInverse.add(new HashSet<>());
        }

        for (int i = 0; i < graph.size(); i++)
            for (int j : graph.get(i))
                graphInverse.get(j).add(i);

        Queue<Integer> freeNodes = new PriorityQueue<>();
        for (int i = 0; i < graph.size(); i++)
            if (graph.get(i).isEmpty())
                freeNodes.add(i);

        List<FileTemplate> result = new LinkedList<>();

        while (!freeNodes.isEmpty()) {
            int i = freeNodes.remove();
            result.add(files.get(i));

            for (int j : graphInverse.get(i)) {
                graph.get(j).remove(i);

                if (graph.get(j).isEmpty())
                    freeNodes.add(j);
            }
        }

        if (!graph.stream().allMatch(Set::isEmpty)) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < graph.size(); i++)
                for (int j : graph.get(i))
                    builder.append("(").append(j).append("-").append(i).append(")");

            throw new IllegalArgumentException("Graph is cyclic: " + builder.toString());
        }

        return result;
    }
}
