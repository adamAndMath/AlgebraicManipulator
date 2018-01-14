package algebraic.manipulator.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Rename implements Manipulation {
    private final int[] position;
    private final String from;
    private final String to;

    public Rename(int[] position, String from, String to) {
        this.position = position;
        this.from = from;
        this.to = to;
    }

    public int[] getPosition() {
        return position.clone();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public Stream<Path> getDependencies(WorkProject project, WorkFile file) {
        return Stream.empty();
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        return statement.replace(position, 0, i, this::rename);
    }

    private Statement rename(Statement statement) {
        if (!(statement instanceof Operation))
            throw new IllegalArgumentException("Rename can only be applied to an operation");

        Operation operation = (Operation) statement;

        if (!operation.getDummies().contains(from))
            throw new IllegalArgumentException("The operation doesn't contain: " + from);

        return new Operation(operation.getName(),
                IntStream.range(0, operation.dummyCount()).mapToObj(operation::getDummy)
                        .map(var -> from.equals(var.getName()) ? new Variable(to) : var.clone()).toArray(Variable[]::new),
                IntStream.range(0, operation.parameterCount()).mapToObj(operation::getParameter)
                        .map(s -> s.set(var -> from.equals(var.getName()) ? new Variable(to) : var.clone())).toArray(Statement[]::new)
        );
    }
}
