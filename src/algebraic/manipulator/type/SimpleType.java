package algebraic.manipulator.type;

import java.util.Objects;

public class SimpleType implements Type {
    private final String name;

    public SimpleType(String name) {
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
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleType)) return false;
        SimpleType that = (SimpleType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public boolean is(Type type) {
        return type instanceof SimpleType && name.equals(((SimpleType) type).name);
    }
}
