package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ToEval implements Manipulation {
    public static class Parameter {
        private final String variable;
        private final Statement statement;
        private final int[][] positions;

        public Parameter(String variable, Statement statement, int[][] positions) {
            this.variable = variable;
            this.statement = statement;
            this.positions = positions;
        }

        public String getVariable() {
            return variable;
        }

        public Statement getStatement() {
            return statement;
        }

        private int[][] getPositions() {
            return positions;
        }
    }

    private final int[] position;
    private final PathTree<Parameter> tree;
    private final String[] variables;
    private final Statement[] statements;

    public ToEval(int[] position, Parameter... parameters) {
        this.position = position;
        variables = Arrays.stream(parameters).map(Parameter::getVariable).toArray(String[]::new);
        statements = Arrays.stream(parameters).map(Parameter::getStatement).toArray(Statement[]::new);
        tree = new PathTree<>(Arrays.asList(parameters), Parameter::getPositions, Function.identity());
    }

    public int[] getPosition() {
        return position.clone();
    }

    public PathTree<Parameter> getTree() {
        return tree;
    }

    public List<String> getVariables() {
        return Arrays.asList(variables);
    }

    @Override
    public Stream<Path> getDependencies(WorkProject project, WorkFile file) {
        return Stream.empty();
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        return statement.replace(position, 0, i, this::replace);
    }

    private Operation replace(Statement statement) {
        return new Operation("eval",
                Stream.concat(
                        Stream.of(new Operation("func", Arrays.stream(variables).map(Variable::new).toArray(Variable[]::new), statement.replace(tree, this::replace))),
                        Arrays.stream(statements).map(Statement::clone)
                ).toArray(Statement[]::new)
        );
    }

    private Statement replace(Parameter par, Statement state) {
        if (!par.statement.equals(state))
            throw new IllegalStateException("Expected " + par.getStatement().toString() + ", but received " + state.toString());

        return new Variable(par.getVariable());
    }
}
