package algebraic.manipulator.statement;

import algebraic.manipulator.PathTree;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Statement {
    @Override
    public abstract Statement clone();
    public abstract Set<String> getVariables();
    public abstract Set<String> getDummies();

    public Statement set(Function<Variable, Statement> function) {
        return clone();
    }

    public Statement setAll(Function<Variable, Statement> function) {
        return clone();
    }

    public<T> PathTree<T> tree(Function<Variable, T> function) {
        return new PathTree<>();
    }

    public Statement replace(int[] position, int depth, int i, Function<Statement, Statement> function) {
        if (i != position[depth]) return clone();

        if (depth == position.length - 1)
            return function.apply(this);

        throw new IllegalStateException("Illegal path");
    }

    public<T> Statement replace(PathTree<T> positions, BiFunction<T, Statement, Statement> function) {
        if (positions == null) return clone();

        if (positions.isLeaf())
            return function.apply(positions.getLeaf(), this);

        throw new IllegalStateException("Illegal path");
    }
}
