package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.statement.IntValue;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;
import algebraic.manipulator.type.SimpleType;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InductionWork extends Equation {
    private final String[] inductive;
    private final Statement[] baseState;
    private final Work base;
    private final AssumedWork[] up;
    private final AssumedWork[] down;

    public InductionWork(List<Variable> dummy, List<Definition> variables, Statement[] result, String[] inductive, Statement[] baseState, Work base) {
        super(dummy, variables, result);
        this.inductive = inductive.clone();
        this.baseState = baseState.clone();
        this.base = base;

        if (inductive.length != baseState.length)
            throw new IllegalArgumentException("Inductive variables don't match base states");

        up = new AssumedWork[inductive.length];
        down = new AssumedWork[inductive.length];

        for (int i = 0; i < inductive.length; i++) {
            String ind = inductive[i];

            up[i] = new AssumedWork(dummy, variables(), Arrays.stream(result).map(r -> r.set(var -> ind.equals(var.getName()) ? new Operation("add", var.clone(), new IntValue(1)) : var.clone())).toArray(Statement[]::new), result);
            down[i] = new AssumedWork(dummy, variables(), Arrays.stream(result).map(r -> r.set(var -> ind.equals(var.getName()) ? new Operation("sub", var.clone(), new IntValue(1)) : var.clone())).toArray(Statement[]::new), result);

            if (!containsVariable(ind))
                throw new IllegalArgumentException(ind + " is not a defined variable");

            if (!new SimpleType("Integer").is(getVariable(ind).getType()))
                throw new IllegalArgumentException("The inductive variable has to be an integer");

            variables.remove(variables.stream().filter(v -> ind.equals(v.getName())).findAny().get());
        }

        if (!base.variables().equals(variables))
            throw new IllegalArgumentException("Variables of base work doesn't mach: " + base.variables() + " and not " + variables);

        if (count() != base.count())
            throw new IllegalArgumentException("Invalid base");

        for (int i = 0; i < result.length; i++)
            if (!result[i].set(this::setBase).equals(base.getStatement(i)))
                throw new IllegalArgumentException("Invalid base");
    }

    private Statement setBase(Variable var) {
        for (int i = 0; i < inductive.length; i++)
            if (inductive[i].equals(var.getName()))
                return baseState[i].clone();

        return var.clone();
    }

    @Override
    public boolean validate() {
        for (AssumedWork u : up)
            if (!u.validate())
                return false;

        for (AssumedWork d : down)
            if (!d.validate())
                return false;

        return base.validate();
    }

    @Override
    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.concat(Stream.of(base), Stream.of(up, down).flatMap(Arrays::stream))
                .flatMap(e -> e.getDependencies(file));
    }

    public int indexOf(String var) {
        for (int i = 0; i < inductive.length; i++)
            if (var.equals(inductive[i]))
                return i;

        return -1;
    }

    public String[] getInductive() {
        return inductive.clone();
    }

    public Statement getBaseState(String var) {
        return baseState[indexOf(var)];
    }

    public Work getBase() {
        return base;
    }

    public AssumedWork getUp(String var) {
        return up[indexOf(var)];
    }

    public AssumedWork getDown(String var) {
        return down[indexOf(var)];
    }
}
