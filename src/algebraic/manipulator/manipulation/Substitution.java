package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Substitution implements Manipulation {
    private final Path workPath;
    private final int from;
    private final int to;
    private final int[] position;
    private final List<String> dummy;
    private final List<Statement> values;

    public Substitution(Path workPath, int from, int to, int[] position, List<String> dummy, List<Statement> values) {
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

    public int[] getPosition() {
        return position;
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

        return statement.replace(position, 0, i, s -> replace(s, work));
    }

    private Statement replace(Statement statement, Equation work) {
        Statement fromStatement = work.getStatement(from);
        Set<String> dummies = fromStatement.getDummies();

        PathTree<String> tree = fromStatement.tree(v -> dummies.contains(v.getName()) ? null : v.getName());
        Map<String, Statement> pars = new HashMap<>();

        for (int i = 0; i < values.size(); i++)
            if (values.get(i) != null)
                pars.put(work.getVariable(i).getName(), values.get(i));

        statement.get(tree, (v, s) -> {
            if (!pars.containsKey(v))
                pars.put(v, s);
            else if (!pars.get(v).equals(s))
                throw new IllegalStateException("Expected parameter " + pars.get(v) + ", but received " + s);
        });

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
