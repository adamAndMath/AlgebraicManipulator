package algebraic.manipulator.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class ToEval implements Manipulation {
    public static class Parameter {
        private final String variable;
        private final Statement statement;
        private final PathTree<?> positions;

        public Parameter(String variable, Statement statement, PathTree<?> positions) {
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

        public PathTree<?> getPositions() {
            return positions;
        }
    }

    private final PathTree<?> position;
    private final PathTree<Parameter> tree;
    private final Parameter[] parameters;

    public ToEval(PathTree<?> position, Parameter... parameters) {
        this.position = position;
        this.parameters = parameters.clone();
        tree = new PathTree<>(Arrays.asList(parameters), Parameter::getPositions, Function.identity());
    }

    public PathTree<?> getPosition() {
        return position;
    }

    public PathTree<Parameter> getTree() {
        return tree;
    }

    public List<String> getVariables() {
        return Arrays.stream(parameters).map(Parameter::getVariable).collect(toList());
    }

    public Parameter[] getParameters() {
        return parameters.clone();
    }

    @Override
    public Stream<Path> getDependencies(WorkFile file) {
        return Stream.empty();
    }

    @Override
    public Statement apply(WorkProject project, WorkFile file, int i, Statement statement) {
        return statement.replace(position.sub(i), (o, s) -> replace(s));
    }

    private Operation replace(Statement statement) {
        Map<String, Statement> pars = new HashMap<>();

        Arrays.stream(parameters).filter(par -> par.getStatement() != null).forEach(par -> pars.put(par.getVariable(), par.getStatement()));

        return new Operation("eval",
                Stream.concat(
                        Stream.of(new Operation("func", Arrays.stream(parameters)
                                .map(Parameter::getVariable).map(Variable::new).toArray(Variable[]::new),
                        statement.replace(tree, (par, state) -> replace(pars, par, state)))),
                        Arrays.stream(parameters).map(Parameter::getVariable).map(pars::get).map(Statement::clone)
                ).toArray(Statement[]::new)
        );
    }

    private Statement replace(Map<String, Statement> pars, Parameter par, Statement state) {
        if (!pars.containsKey(par.getVariable()))
            pars.put(par.getVariable(), state);

        if (!pars.get(par.getVariable()).equals(state))
            throw new IllegalStateException("Expected " + par.getStatement().toString() + ", but received " + state.toString());

        return new Variable(par.getVariable());
    }
}
