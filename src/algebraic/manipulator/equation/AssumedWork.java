package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Manipulation;
import algebraic.manipulator.statement.Statement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class AssumedWork extends Equation {
    private final Statement[] origin;
    private final List<Manipulation> manipulations = new ArrayList<>();
    private Statement[] current;

    public AssumedWork(List<Definition> variables, Statement[] result, Statement[] origin) {
        super(variables, result);
        this.origin = origin.clone();
        this.current = origin.clone();
    }

    public Statement[] getOrigin() {
        return origin.clone();
    }

    public Statement[] getCurrent() {
        return current.clone();
    }

    public List<Manipulation> getManipulations() {
        return Collections.unmodifiableList(manipulations);
    }

    @Override
    public boolean validate() {
        if (count() != current.length) return false;

        for (int i = 0; i < count(); i++)
            if (!getStatement(i).equals(current[i]))
                return false;

        return true;
    }

    @Override
    public Stream<Path> getDependencies(WorkProject project, WorkFile file) {
        return manipulations.stream().flatMap(m -> m.getDependencies(project, file));
    }

    public void apply(WorkProject project, WorkFile file, Manipulation manipulation) {
        manipulations.add(manipulation);

        for (int i = 0; i < current.length; i++)
            current[i] = manipulation.apply(project, file, i, current[i]);
    }
}
