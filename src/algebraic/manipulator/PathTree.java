package algebraic.manipulator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class PathTree<T> {
    public static class Tree<T> {
        private final Map<Integer, Tree<T>> children;
        private T leaf;

        public Tree() {
            children = new HashMap<>();
        }

        public Tree(PathTree<T> tree) {
            children = tree.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new Tree<>(e.getValue())));
            set(tree.getLeaf());
        }

        public void union(Tree<?> tree, T leaf) {
            if (tree.leaf != null)
                set(leaf);
            else for (int key : tree.children.keySet())
                sub(key).union(tree.sub(key), leaf);
        }

        public Tree<T> sub(int i) {
            if (leaf != null)
                throw new IllegalArgumentException();

            if (!children.containsKey(i))
                children.put(i, new Tree<>());
            return children.get(i);
        }

        public void set(T leaf) {
            if (!children.isEmpty())
                throw new IllegalArgumentException();

            this.leaf = leaf;
        }

        public T get() {
            return leaf;
        }
    }

    private final Map<Integer, PathTree<T>> children;
    private final T leaf;

    private final boolean empty;

    public PathTree() {
        children = Map.of();
        leaf = null;
        empty = true;
    }

    public<S> PathTree(Collection<S> collection, Function<S, PathTree<?>> pos, Function<S, T> leaf) {
        this(build(collection, pos.andThen(Tree::new), leaf));
    }

    public PathTree(Tree<T> tree) {
        children = tree.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new PathTree<>(e.getValue())));
        leaf = tree.get();
        empty = leaf == null && children.values().stream().allMatch(PathTree::isEmpty);
    }

    public PathTree(T leaf) {
        children = Map.of();
        this.leaf = leaf;
        empty = leaf == null;
    }

    public PathTree(Map<Integer, PathTree<T>> children) {
        this.children = Collections.unmodifiableMap(children);
        this.leaf = null;
        empty = children.values().stream().allMatch(PathTree::isEmpty);
    }

    public PathTree(int key, PathTree<T> child) {
        this.children = Map.of(key, child);
        this.leaf = null;
        empty = children.values().stream().allMatch(PathTree::isEmpty);
    }

    public boolean isLeaf() {
        return leaf != null;
    }

    public T getLeaf() {
        return leaf;
    }

    public PathTree<T> sub(int i) {
        return children.get(i);
    }

    public PathTree<T> subOrEmpty(int i) {
        return children.getOrDefault(i, new PathTree<>());
    }

    public boolean isEmpty() {
        return empty;
    }

    public<S> PathTree<S> map(Function<T, S> function) {
        return isLeaf() ? new PathTree<>(function.apply(getLeaf())) : new PathTree<>(children.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().map(function))));
    }

    public<S> PathTree<S> surround(PathTree<S> tree) {
        return isLeaf() ? tree : new PathTree<>(children.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().surround(tree))));
    }

    @Override
    public String toString() {
        return isLeaf() ? getLeaf().toString() : children.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{", "}"));
    }

    public Stream<T> stream() {
        return isLeaf() ? Stream.of(getLeaf()) : children.values().stream().filter(Objects::nonNull).flatMap(PathTree::stream);
    }

    private static<T,S> Tree<T> build(Collection<S> collection, Function<S, Tree<?>> pos, Function<S, T> leaf) {
        Tree<T> root = new Tree<>();

        for (S obj : collection) {
            Tree<?> posTree = pos.apply(obj);

            root.union(posTree, leaf.apply(obj));
        }

        return root;
    }
}
