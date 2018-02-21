package algebraic.manipulator.type;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TupleType implements Type, Iterable<Type> {
    private final Type[] types;

    public TupleType(Type... types) {
        this.types = types;
    }

    @Override
    public String toString() {
        return Arrays.stream(types).map(Object::toString).collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getTypes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TupleType)) return false;
        TupleType tupleType = (TupleType) o;
        return Arrays.equals(getTypes(), tupleType.getTypes());
    }

    @Override
    public boolean is(Type type) {
        return type instanceof TupleType && Arrays.equals(types, ((TupleType) type).types);
    }

    public Type[] getTypes() {
        return types.clone();
    }

    @Override
    public Iterator<Type> iterator() {
        return Arrays.asList(types).iterator();
    }

    public Stream<Type> stream() {
        return Arrays.stream(types);
    }
}
