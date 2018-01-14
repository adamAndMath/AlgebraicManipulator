package algebraic.manipulator.statement;

import algebraic.manipulator.PathTree;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Variable extends Statement {
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Variable && Objects.equals(getName(), ((Variable) o).getName()));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public Variable clone() {
        return new Variable(name);
    }

    @Override
    public Statement set(Function<Variable, Statement> value) {
        return value.apply(this);
    }

    @Override
    public <T> PathTree<T> tree(Function<Variable, T> function) {
        return new PathTree<>(function.apply(this));
    }

    @Override
    public Set<String> getVariables() {
        return Set.of(name);
    }

    @Override
    public Set<String> getDummies() {
        return Set.of();
    }
}
