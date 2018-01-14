package algebraic.manipulator.statement;

import java.util.Objects;
import java.util.Set;

public class IntValue extends Statement {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof IntValue && value == ((IntValue) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public IntValue clone() {
        return new IntValue(value);
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
