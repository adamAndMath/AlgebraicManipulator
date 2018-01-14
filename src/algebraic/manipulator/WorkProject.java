package algebraic.manipulator;

import algebraic.manipulator.equation.Equation;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorkProject {
    public interface WP {
        <T extends WP, R> R get(Path path, BiFunction<Path, T, R> function);
        <T extends WP, R> R get(Path path, Function<T, R> function);
        <T extends WP> void run(Path path, BiConsumer<Path, T> consumer);
        <T extends WP> void run(Path path, Consumer<T> consumer);
    }

    public static class WPFolder implements WP {
        public final Map<String, WP> contents = new HashMap<>();

        public Set<Map.Entry<String, WP>> entries() {
            return contents.entrySet();
        }

        @Override
        public <T extends WP, R> R get(Path path, BiFunction<Path, T, R> function) {
            return path == null
                    ? function.apply(null, (T) this)
                    : contents.get(path.getName(0).toString()).get(path.subpath(1, path.getNameCount()), function);
        }

        @Override
        public <T extends WP, R> R get(Path path, Function<T, R> function) {
            return path == null
                    ? function.apply((T) this)
                    : contents.get(path.getName(0).toString()).get(path.subpath(1, path.getNameCount()), function);
        }

        @Override
        public <T extends WP> void run(Path path, BiConsumer<Path, T> consumer) {
            if (path == null) consumer.accept(null, (T) this);
            else contents.get(path.getName(0).toString()).run(path.subpath(1, path.getNameCount()), consumer);
        }

        @Override
        public <T extends WP> void run(Path path, Consumer<T> consumer) {
            if (path == null) consumer.accept((T) this);
            else contents.get(path.getName(0).toString()).run(path.subpath(1, path.getNameCount()), consumer);
        }
    }

    public static class WPFile implements WP {
        final WorkFile file;

        public WPFile(WorkFile file) {
            this.file = file;
        }

        @Override
        public <T extends WP, R> R get(Path path, BiFunction<Path, T, R> function) {
            return function.apply(path, (T) this);
        }

        @Override
        public <T extends WP, R> R get(Path path, Function<T, R> function) {
            if (path.getNameCount() != 0) throw new IllegalArgumentException();
            return function.apply((T) this);
        }

        @Override
        public <T extends WP> void run(Path path, BiConsumer<Path, T> consumer) {
            consumer.accept(path, (T) this);
        }

        @Override
        public <T extends WP> void run(Path path, Consumer<T> consumer) {
            if (path.getNameCount() != 0) throw new IllegalArgumentException();
            consumer.accept((T) this);
        }
    }

    private final WPFolder root = new WPFolder();

    public WorkFile getFile(Path path) {
        return root.get(path, (Path p, WPFile f) -> f.file);
    }

    public Equation getWork(Path path) {
        return root.get(path, (Path p, WPFile f) -> f.file.get(p.toString()));
    }

    public Equation getWork(WorkFile file, Path path) {
        if (path.getNameCount() == 1) return file.get(path.toString());
        return getWork(path);
    }

    public Path absolutePath(WorkFile file, Path path) {
        if (path.getNameCount() == 1)
            return file.getPath().resolve(path);

        return path;
    }

    public void put(WorkFile file, Path path) {
        root.run(path.getParent(), (WPFolder f) -> f.contents.put(path.getFileName().toString(), new WPFile(file)));
    }

    public List<WorkFile> sorted() {
        List<WorkFile> files = new LinkedList<>();

        Queue<WP> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            WP wp = queue.remove();

            if (wp instanceof WPFile)
                files.add(((WPFile) wp).file);
            else if (wp instanceof WPFolder)
                queue.addAll(((WPFolder) wp).contents.values());
        }

        return topologicalSort(files);
    }

    private List<WorkFile> topologicalSort(List<WorkFile> files) {
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

        List<WorkFile> result = new LinkedList<>();

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
