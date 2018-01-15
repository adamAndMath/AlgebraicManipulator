package algebraic.manipulator.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.statement.Statement;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Manipulation {
    Stream<Path> getDependencies(WorkFile file);
    Statement apply(WorkProject project, WorkFile file, int i, Statement statement);
}
