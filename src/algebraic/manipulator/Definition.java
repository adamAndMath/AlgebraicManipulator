package algebraic.manipulator;

import algebraic.manipulator.type.Type;

import java.util.Objects;

public class Definition {
    private final Type type;
    private final String name;

    public Definition(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " in " + type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Definition)) return false;
        Definition that = (Definition) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getName(), that.getName());
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
