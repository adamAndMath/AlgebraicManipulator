package algebraic.manipulator.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.statement.Statement;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Manipulation {
    public abstract Stream<Path> getDependencies(WorkFile file);
    public abstract Statement apply(WorkProject project, WorkFile file, int i, Statement statement);

    public Statement[] apply(WorkProject project, WorkFile file, Equation equation, Statement[] statements) {
        Statement[] result = new Statement[statements.length];

        for (int i = 0; i < statements.length; i++) {
            result[i] = apply(project, file, i, statements[i]);

            Set<String> variables = new HashSet<>(equation.variableNames());

            if (variables.stream().anyMatch(result[i].getDummies()::contains))
                throw new IllegalArgumentException("A variable and a dummy can not have the same name");

            for (String var : statements[i].getVariables())
                if (!variables.contains(var))
                    throw new IllegalArgumentException("Undefined variable: " + var);
        }

        return result;
    }
}
