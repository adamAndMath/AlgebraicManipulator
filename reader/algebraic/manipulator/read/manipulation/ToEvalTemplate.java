package algebraic.manipulator.read.manipulation;

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
        public List<List<Integer>> positions;

        public Parameter(TokenReader reader) throws IOException {
            variable = reader.readString();
            reader.assertIgnore(Token.EQUAL);
            statement = WorkReader.readStatement(reader);
            if (reader.isRead(Token.COLON)) {
                reader.assertIgnore(Token.LSQR);

                positions = reader.isCurrent(Token.RSQR)
                        ? new ArrayList<>()
                        : reader.readList(Token.VBAR, r -> r.readList(Token.COMMA, TokenReader::readInt));

                reader.assertIgnore(Token.RSQR);
            } else {
                positions = List.of();
            }
        }

        public ToEval.Parameter toParameter() {
            return new ToEval.Parameter(variable, statement, positions.stream().map(p -> p.stream().mapToInt(i -> i).toArray()).toArray(int[][]::new));
        }
    }

    public List<Integer> position;
    public List<Parameter> parameters;

    public ToEvalTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.LPAR);

        parameters = reader.isCurrent(Token.RPAR)
                ? new ArrayList<>()
                : reader.readList(Token.COMMA, Parameter::new);

        reader.assertIgnore(Token.RPAR);
        reader.assertIgnore(Token.COLON);
        position = reader.readList(Token.COMMA, TokenReader::readInt);
    }

    @Override
    public Stream<Path> getDependencies(FileTemplate file) {
        return Stream.empty();
    }

    @Override
    public ToEval toManipulation(WorkProject project, WorkFile file) {
        return new ToEval(position.stream().mapToInt(i -> i).toArray(), parameters.stream().map(Parameter::toParameter).toArray(ToEval.Parameter[]::new));
    }
}
