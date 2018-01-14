package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Manipulation;
import algebraic.manipulator.statement.Statement;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Work extends Equation {
    private final Statement origin;
    private final int amount;
    private final List<Manipulation> manipulations = new ArrayList<>();
    private Statement[] current;

    public Work(List<Definition> variables, Statement[] result, int amount, Statement origin) {
        super(variables, result);
        this.origin = origin;
        this.amount = amount;

        current = new Statement[amount];
        Arrays.fill(current, origin);
    }

    @Override
    public boolean validate() {
        if (count() != current.length) return false;

        for (int i = 0; i < count(); i++)
            if (!getStatement(i).equals(current[i]))
                return false;

        return true;
    }

    @Override
    public Stream<Path> getDependencies(WorkProject project, WorkFile file) {
        return manipulations.stream().flatMap(m -> m.getDependencies(project, file));
    }

    public Statement getOrigin() {
        return origin;
    }

    public int getAmount() {
        return amount;
    }

    public List<Manipulation> getManipulations() {
        return Collections.unmodifiableList(manipulations);
    }

    public void apply(WorkProject project, WorkFile file, Manipulation manipulation) {
        manipulations.add(manipulation);
        for (int i = 0; i < amount; i++) {
            current[i] = manipulation.apply(project, file, i, current[i]);

            Set<String> variables = new HashSet<>(variableNames());

            if (variables.stream().anyMatch(current[i].getDummies()::contains))
                throw new IllegalArgumentException("A variable and a dummy can not have the same name");

            variables.addAll(current[i].getDummies());

            for (String var : current[i].getVariables())
                if (!variables.contains(var))
                    throw new IllegalArgumentException("Undefined variable: " + var);
        }
    }
}
