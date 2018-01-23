package algebraic.manipulator.statement;

import algebraic.manipulator.PathTree;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Operation extends Statement {
    private final String name;
    private final Variable[] dummies;
    private final Statement[] parameters;
    private final Set<String> variables;
    private final Set<String> definedDummies;

    public Operation(String name, Statement... parameters) {
        this(name, new Variable[0], parameters);
    }

    public Operation(String name, Variable[] dummies, Statement... parameters) {
        this.name = name;
        this.dummies = dummies.clone();
        this.parameters = parameters.clone();
        Set<String> dummySet = Arrays.stream(dummies).map(Variable::getName).collect(Collectors.toSet());
        variables = Arrays.stream(parameters).map(Statement::getVariables).flatMap(Collection::stream).filter(v -> !dummySet.contains(v)).collect(Collectors.toSet());
        Set<String> subDummies = Arrays.stream(parameters).map(Statement::getDummies).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));

        for (String s : dummySet)
            if (subDummies.contains(s))
                throw new IllegalArgumentException("Redefined dummy " + s);

        for (String s : subDummies)
            if (variables.contains(s))
                throw new IllegalArgumentException("Variable collision " + s);

        subDummies.addAll(dummySet);
        definedDummies = Collections.unmodifiableSet(subDummies);
    }

    public String getName() {
        return name;
    }

    public int dummyCount() {
        return dummies.length;
    }

    public int parameterCount() {
        return parameters.length;
    }

    public Variable getDummy(int i) {
        return dummies[i];
    }

    public Statement getParameter(int i) {
        return parameters[i];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        if (dummies.length != 0)
            builder.append(Arrays.stream(dummies).map(Object::toString).collect(Collectors.joining(", ", "<", ">")));
        builder.append(Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining(",", "(", ")")));
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;
        Operation operation = (Operation) o;
        return Objects.equals(name, operation.name) &&
                Arrays.equals(dummies, operation.dummies) &&
                Arrays.equals(parameters, operation.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dummies, parameters);
    }

    @Override
    public Operation clone() {
        return new Operation(
                getName(),
                Arrays.stream(dummies).map(Variable::clone).toArray(Variable[]::new),
                Arrays.stream(parameters).map(Statement::clone).toArray(Statement[]::new)
        );
    }

    @Override
    public <T> void get(PathTree<T> positions, BiConsumer<T, Statement> consumer) {
        if (positions == null || positions.isEmpty())
            return;

        if (positions.isLeaf()) {
            consumer.accept(positions.getLeaf(), this);
        } else {
            for (int i = 0; i < parameters.length; i++)
                parameters[i].get(positions.sub(i), consumer);
        }
    }

    @Override
    public Statement set(Function<Variable, Statement> value) {
        return new Operation(
                getName(),
                Arrays.stream(dummies).map(Variable::clone).toArray(Variable[]::new),
                Arrays.stream(parameters).map(par -> par.set(value)).toArray(Statement[]::new)
        );
    }

    @Override
    public Statement setAll(Function<Variable, Statement> function) {
        return new Operation(
                getName(),
                Arrays.stream(dummies).map(function).toArray(Variable[]::new),
                Arrays.stream(parameters).map(par -> par.setAll(function)).toArray(Statement[]::new)
        );
    }

    @Override
    public <T> PathTree<T> tree(Function<Variable, T> function) {
        Map<Integer, PathTree<T>> map = new HashMap<>();

        for (int i = 0; i < parameters.length; i++) {
            PathTree<T> tree = parameters[i].tree(function);

            if (!tree.isEmpty())
                map.put(i, tree);
        }

        return new PathTree<>(map);
    }

    public<T> Statement replace(PathTree<T> positions, BiFunction<T, Statement, Statement> function) {
        if (positions == null) return clone();

        if (positions.isLeaf())
            return function.apply(positions.getLeaf(), this);

        return new Operation(
                getName(),
                Arrays.stream(dummies).map(Variable::clone).toArray(Variable[]::new),
                IntStream.range(0, parameters.length).mapToObj(par -> parameters[par].replace(positions.sub(par), function)).toArray(Statement[]::new)
        );
    }

    @Override
    public Set<String> getVariables() {
        return variables;
    }

    @Override
    public Set<String> getDummies() {
        return definedDummies;
    }
}
