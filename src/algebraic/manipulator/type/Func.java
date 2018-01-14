package algebraic.manipulator.type;

import java.util.Objects;

public class Func implements Type {
    public final Type from;
    public final Type to;

    public Func(Type from, Type to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "(" + from + " -> " + to + ')';
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Func)) return false;
        Func func = (Func) o;
        return Objects.equals(from, func.from) && Objects.equals(to, func.to);
    }

    @Override
    public boolean is(Type type) {
        return type instanceof Func && from.is(((Func) type).from) && to.is(((Func) type).to);
    }
}
