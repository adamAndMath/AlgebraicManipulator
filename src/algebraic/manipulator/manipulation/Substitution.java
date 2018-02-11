package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Substitution implements Manipulation {
    private final Path workPath;
    private final int from;
    private final int to;
    private final PathTree<?> position;
    private final List<String> dummy;
    private final List<Statement> values;

    public Substitution(Path workPath, int from, int to, PathTree<?> position, List<String> dummy, List<Statement> values) {
        this.workPath = workPath;
        this.from = from;
        this.to = to;
        this.position = position;
        this.dummy = dummy;
        this.values = values;
    }

    public Path getWorkPath() {
        return workPath;
    }

    public Equation getWork(WorkProject project, WorkFile file) {
        return project.getWork(file, workPath);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public PathTree<?> getPosition() {
        return position;
    }

    public List<String> getDummy() {
        return Collections.unmodifiableList(dummy);
    }

    public List<Statement> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.of(file.absolutePath(workPath));
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        Equation work = getWork(project, file);

        if (values.size() != work.variableNames().size())
            throw new IllegalStateException("Parameter count does't match referred work");

        if (dummy.size() != work.dummies().size())
            throw new IllegalStateException("Dummy count does't match referred work");

        return statement.replace(position.sub(i), (o, s) -> replace(s, work));
    }

    private Statement replace(Statement statement, Equation work) {
        Statement fromStatement = work.getStatement(from);
        Set<String> dummies = fromStatement.getDummies();

        PathTree<String> tree = fromStatement.tree(v -> dummies.contains(v.getName()) ? null : v.getName());
        Map<String, Statement> pars = new HashMap<>();

        for (int i = 0; i < values.size(); i++)
            if (values.get(i) != null)
                pars.put(work.getVariable(i).getName(), values.get(i));

        try {
            statement.get(tree, (v, s) -> {
                if (!pars.containsKey(v))
                    pars.put(v, s);
                else if (!pars.get(v).equals(s))
                    throw new IllegalStateException("Expected parameter " + pars.get(v) + ", but received " + s);
            });
        } catch (Exception e) {
            throw new IllegalStateException("Expected substitute of " + fromStatement.toString() + ", but received " + statement.toString());
        }

        fromStatement = fromStatement.setAll(v -> set(work, pars, v));

        if (!fromStatement.equals(statement))
            throw new IllegalStateException("Expected " + fromStatement.toString() + ", but received " + statement.toString());

        return work.getStatement(to).setAll(v -> set(work, pars, v));
    }

    private Statement set(Equation work, Map<String, Statement> parameters, Variable variable) {
        int index = work.dummies().indexOf(variable);

        if (index != -1)
            return new Variable(dummy.get(index));
        else if (parameters.containsKey(variable.getName()))
            return parameters.get(variable.getName()).clone();
        else
            throw new IllegalStateException("Cannot infer " + variable);
    }
}
