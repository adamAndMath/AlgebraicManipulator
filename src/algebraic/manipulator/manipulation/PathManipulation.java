package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.statement.Statement;

public abstract class PathManipulation extends Manipulation {
    private final PathTree<?> position;

    public PathManipulation(PathTree<?> position) {
        this.position = position;
    }

    public PathTree<?> getPosition() {
        return position;
    }

    @Override
    public Statement[] apply(WorkProject project, WorkFile file, Equation equation, Statement[] statements) {
        if (position.min() < 0 || position.max() >= statements.length)
            throw new IllegalArgumentException("Invalid Path");

        return super.apply(project, file, equation, statements);
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        return statement.replace(position.sub(i), (o, s) -> replace(project, file, s));
    }

    protected abstract Statement replace(WorkProject project, WorkFile file, Statement statement);
}
