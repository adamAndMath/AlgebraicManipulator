package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Statement;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Equation {
    private final List<Definition> variables;
    private final Map<String, Definition> variableMap;
    private final Statement[] result;

    public Equation(List<Definition> variables, Statement[] result) {
        this.variables = Collections.unmodifiableList(variables);
        this.result = result.clone();

        variableMap = variables.stream().collect(Collectors.toMap(Definition::getName, Function.identity()));
    }

    public abstract boolean validate();

    public int count() {
        return result.length;
    }

    public Statement getStatement(int i) {
        return result[i];
    }

    public Statement[] getResult() {
        return result.clone();
    }

    public Set<String> variableNames() {
        return variableMap.keySet();
    }

    public Stream<Definition> streamVariables() {
        return variables.stream();
    }

    public boolean containsVariable(String name) {
        return variableMap.containsKey(name);
    }

    public boolean containsVariables(Collection<String> collection) {
        return variableMap.keySet().containsAll(collection);
    }

    public int indexOfVariable(String name) {
        return variables.indexOf(streamVariables().filter(var -> var.getName().equals(name)).findAny().get());
    }

    public Definition getVariable(int i) {
        return variables.get(i);
    }

    public Definition getVariable(String name) {
        return variableMap.get(name);
    }

    public Stream<Path> getDependencies(WorkProject project, WorkFile file) {
        return Stream.empty();
    }
}
