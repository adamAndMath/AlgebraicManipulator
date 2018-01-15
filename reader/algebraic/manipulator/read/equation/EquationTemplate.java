package algebraic.manipulator.read.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.read.FileTemplate;
import algebraic.manipulator.statement.Statement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class EquationTemplate {
    public List<Definition> parameters = new ArrayList<>();
    public List<Statement> result;

    public Stream<Path> getDependencies(FileTemplate file) {
        return Stream.empty();
    }

    public abstract Equation toEquation(WorkProject project, WorkFile file);
}
