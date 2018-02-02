package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FromEval implements Manipulation {
    private final PathTree<?> position;

    public FromEval(PathTree<?> position) {
        this.position = position;
    }

    @Override
    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.empty();
    }

    public PathTree<?> getPosition() {
        return position;
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        return statement.replace(position.sub(i), (o, s) -> replace(s));
    }

    private Statement replace(Statement statement) {
        if (!(statement instanceof Operation && "eval".equals(((Operation) statement).getName())))
            throw new IllegalStateException("Invalid statement: " + statement.toString());

        Operation eval = (Operation) statement;

        if (!(eval.getParameter(0) instanceof Operation && "func".equals(((Operation) eval.getParameter(0)).getName())))
            throw new IllegalStateException("Invalid statement: " + statement.toString());

        Operation func = (Operation) eval.getParameter(0);

        if (eval.dummyCount() != 0 || func.parameterCount() != 1 || eval.parameterCount() != func.dummyCount() + 1)
            throw new IllegalStateException("Invalid statement: " + statement.toString());

        Map<String, Statement> map = new HashMap<>();

        for (int j = 0; j < func.dummyCount(); j++)
            map.put(func.getDummy(j).getName(), eval.getParameter(j + 1));

        return func.getParameter(0).set(v -> set(v, map));
    }

    private Statement set(Variable variable, Map<String, Statement> map) {
        return map.containsKey(variable.getName()) ? map.get(variable.getName()) : variable.clone();
    }
}
