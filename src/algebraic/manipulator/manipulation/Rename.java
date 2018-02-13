package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Rename extends PathManipulation {
    private final String from;
    private final String to;

    public Rename(PathTree<?> position, String from, String to) {
        super(position);
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.empty();
    }

    @Override
    protected Statement replace(WorkProject project, WorkFile file, Statement statement) {
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
