package algebraic.manipulator.statement;

import algebraic.manipulator.PathTree;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Statement {
    @Override
    public abstract Statement clone();
    public abstract Set<String> getVariables();
    public abstract Set<String> getDummies();

    public<T> void get(PathTree<T> positions, BiConsumer<T, Statement> consumer) {
        if (positions == null || positions.isEmpty()) return;

        if (!positions.isLeaf())
            throw new IllegalStateException("Illegal path");

        consumer.accept(positions.getLeaf(), this);
    }

    public Statement set(Function<Variable, Statement> function) {
        return clone();
    }

    public Statement setAll(Function<Variable, Statement> function) {
        return clone();
    }

    public<T> PathTree<T> tree(Function<Variable, T> function) {
        return new PathTree<>();
    }

    public<T> Statement replace(PathTree<T> positions, BiFunction<T, Statement, Statement> function) {
        if (positions == null || positions.isEmpty()) return clone();

        if (positions.isLeaf())
            return function.apply(positions.getLeaf(), this);

        throw new IllegalStateException("Illegal path");
    }
}
