package algebraic.manipulator.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Call implements Manipulation {
    private final String temp;
    private final Statement call;

    public Call(String temp, Statement call) {
        this.temp = temp;
        this.call = call;
    }

    public String getTemp() {
        return temp;
    }

    public Statement getCall() {
        return call;
    }

    @Override
    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.empty();
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        return call.set(v -> set(v, statement));
    }

    private Statement set(Variable variable, Statement replace) {
        return (temp.equals(variable.getName()) ? replace : variable).clone();
    }
}
