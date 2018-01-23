package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.ToEval;
import algebraic.manipulator.read.*;
import algebraic.manipulator.statement.Statement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ToEvalTemplate implements ManipulationTemplate {
    public static class Parameter {
        public String variable;
        public Statement statement;
        public PathTree<?> positions;

        public Parameter(TokenReader reader) throws IOException {
            variable = reader.readString();
            if (reader.isRead(Token.EQUAL))
                statement = WorkReader.readStatement(reader);

            PathTree.Tree<Integer> tree = new PathTree.Tree<>();
            if (reader.isRead(Token.COLON))
                WorkReader.readPathsSiblings(reader, tree);
            positions = new PathTree<Integer>(tree);
        }

        public ToEval.Parameter toParameter() {
            return new ToEval.Parameter(variable, statement, positions);
        }
    }

    public PathTree<?> position;
    public List<Parameter> parameters;

    public ToEvalTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.LPAR);

        parameters = reader.isCurrent(Token.RPAR)
                ? new ArrayList<>()
                : reader.readList(Token.COMMA, Parameter::new);

        reader.assertIgnore(Token.RPAR);
        reader.assertIgnore(Token.COLON);
        position = WorkReader.readPaths(reader);
    }

    @Override
    public Stream<Path> getDependencies(FileTemplate file) {
        return Stream.empty();
    }

    @Override
    public ToEval toManipulation(WorkProject project, WorkFile file) {
        return new ToEval(position, parameters.stream().map(Parameter::toParameter).toArray(ToEval.Parameter[]::new));
    }
}
