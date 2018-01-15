package algebraic.manipulator.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Substitution implements Manipulation {
    private final Path workPath;
    private final int from;
    private final int to;
    private final int[] position;
    private final List<Statement> values;

    public Substitution(Path workPath, int from, int to, int[] position, List<Statement> values) {
        this.workPath = workPath;
        this.from = from;
        this.to = to;
        this.position = position;
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

        Statement fromStatement = work.getStatement(from).set(v -> set(work, v));
        Statement toStatement = work.getStatement(to).set(v -> set(work, v));

        return statement.replace(position, 0, i, s -> replace(s, fromStatement, toStatement));
    }

    private Statement replace(Statement statement, Statement fromStatement, Statement toStatement) {
        if (!fromStatement.equals(statement))
            throw new IllegalStateException("Expected " + fromStatement.toString() + ", but received " + statement.toString());

        return toStatement.clone();
    }

    private Statement set(Equation work, Variable variable) {
        return work.containsVariable(variable.getName())
                ? values.get(work.indexOfVariable(variable.getName())).clone()
                : variable.clone();
    }
}
