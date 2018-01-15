package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Equation {
    private final List<Variable> dummy;
    private final List<Definition> variables;
    private final Map<String, Definition> variableMap;
    private final Statement[] result;

    public Equation(List<Variable> dummy, List<Definition> variables, Statement[] result) {
        this.dummy = Collections.unmodifiableList(new ArrayList<>(dummy));
        this.variables = Collections.unmodifiableList(new ArrayList<>(variables));
        this.result = result.clone();

        variableMap = variables.stream().collect(Collectors.toMap(Definition::getName, Function.identity()));
        Set<String> dums = dummy.stream().map(Variable::getName).collect(Collectors.toSet());
        Set<String> vars = Stream.concat(variables.stream().map(Definition::getName), dums.stream()).collect(Collectors.toSet());

        for (Statement s : result) {
            for (String v : s.getVariables())
                if (!vars.contains(v))
                    throw new IllegalArgumentException("Undefined variable " + v + " in result");

            for (String d : s.getDummies())
                if (!dums.contains(d))
                    throw new IllegalArgumentException("Unregistered dummy " + d + " in result");
        }
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

    public List<Variable> dummies() {
        return dummy;
    }
    public Set<String> variableNames() {
        return variableMap.keySet();
    }

    public List<Definition> variables() {
        return variables;
    }

    public boolean containsVariable(String name) {
        return variableMap.containsKey(name);
    }

    public boolean containsVariables(Collection<String> collection) {
        return variableMap.keySet().containsAll(collection);
    }

    public int indexOfVariable(String name) {
        for (int i = 0; i < variables.size(); i++)
            if (name.equals(variables.get(i).getName()))
                return i;

        throw new IllegalArgumentException("Undefined variable " + name);
    }

    public Definition getVariable(int i) {
        return variables.get(i);
    }

    public Definition getVariable(String name) {
        return variableMap.get(name);
    }

    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.empty();
    }
}
