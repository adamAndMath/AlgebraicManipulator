package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.IntValue;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.type.SimpleType;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InductionWork extends Equation {
    private final String inductive;
    private final Statement baseState;
    private final Work base;
    private final AssumedWork up;
    private final AssumedWork down;

    public InductionWork(List<Definition> variables, Statement[] result, String inductive, Statement baseState, Work base) {
        super(variables, result);
        this.inductive = inductive;
        this.baseState = baseState;
        this.base = base;

        up = new AssumedWork(variables, Arrays.stream(result).map(r -> r.set(var -> inductive.equals(var.getName()) ? new Operation("add", var.clone(), new IntValue(1)) : var.clone())).toArray(Statement[]::new), result);
        down = new AssumedWork(variables, Arrays.stream(result).map(r -> r.set(var -> inductive.equals(var.getName()) ? new Operation("sub", var.clone(), new IntValue(1)) : var.clone())).toArray(Statement[]::new), result);

        if (!containsVariable(inductive))
            throw new IllegalArgumentException(inductive + " is not a defined variable");

        if (!new SimpleType("Integer").is(getVariable(inductive).getType()))
            throw new IllegalArgumentException("The inductive variable has to be an integer");

        variables.remove(indexOfVariable(inductive));
        if (!base.streamVariables().collect(Collectors.toList()).equals(variables))
            throw new IllegalArgumentException("Variables of base work doesn't mach: " + base.streamVariables().collect(Collectors.toList()) + " and not " + variables);

        if (count() != base.count())
            throw new IllegalArgumentException("Invalid base");

        for (int i = 0; i < result.length; i++)
            if (!result[i].set(var -> inductive.equals(var.getName()) ? baseState.clone() : var.clone()).equals(base.getStatement(i)))
                throw new IllegalArgumentException("Invalid base");
    }

    @Override
    public boolean validate() {
        return base.validate() && up.validate() && down.validate();
    }

    @Override
    public Stream<Path> getDependencies(WorkProject project, WorkFile file) {
        return Stream.of(base, getUp(), getDown()).flatMap(e -> e.getDependencies(project, file));
    }

    public String getInductive() {
        return inductive;
    }

    public Statement getBaseState() {
        return baseState;
    }

    public Work getBase() {
        return base;
    }

    public AssumedWork getUp() {
        return up;
    }

    public AssumedWork getDown() {
        return down;
    }
}
