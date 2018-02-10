package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Manipulation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Work extends Equation {
    private final Statement origin;
    private final int amount;
    private final List<Manipulation> manipulations = new LinkedList<>();
    private Statement[] current;

    public Work(List<Variable> dummy, List<Definition> variables, Statement[] result, int amount, Statement origin) {
        super(dummy, variables, result);
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
    public Stream<Path> getDependencies(WorkFile file) {
        return manipulations.stream().flatMap(m -> m.getDependencies(file));
    }

    public Statement getOrigin() {
        return origin.clone();
    }

    public Statement[] getCurrent() {
        return current.clone();
    }

    public List<Manipulation> getManipulations() {
        return Collections.unmodifiableList(manipulations);
    }

    public void apply(WorkProject project, WorkFile file, Manipulation manipulation) {
        current = manipulation.apply(project, file, this, current);
        manipulations.add(manipulation);
    }

    public void remove(WorkProject project, WorkFile file) {
        manipulations.remove(manipulations.size() - 1);
        recalculate(project, file);
    }

    private void recalculate(WorkProject project, WorkFile file) {
        current = new Statement[amount];
        Arrays.fill(current, origin);

        for (Manipulation manipulation : manipulations)
            current = manipulation.apply(project, file, this, current);
    }
}
