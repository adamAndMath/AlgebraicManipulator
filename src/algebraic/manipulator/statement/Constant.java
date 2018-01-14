package algebraic.manipulator.statement;

import java.util.Objects;
import java.util.Set;

public class Constant extends Statement {
    private final String name;

    public Constant(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "\\" + name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Constant && Objects.equals(name, ((Constant) o).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public Constant clone() {
        return new Constant(name);
    }

    @Override
    public Set<String> getVariables() {
        return Set.of();
    }

    @Override
    public Set<String> getDummies() {
        return Set.of();
    }
}
