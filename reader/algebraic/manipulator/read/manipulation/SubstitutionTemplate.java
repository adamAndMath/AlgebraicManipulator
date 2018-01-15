package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Substitution;
import algebraic.manipulator.read.*;
import algebraic.manipulator.statement.Statement;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SubstitutionTemplate implements ManipulationTemplate {
    public Path path;
    public int from;
    public int to;
    public List<Statement> parameters;
    public List<Integer> position;

    public SubstitutionTemplate(TokenReader reader) throws IOException {
        path = reader.readReduce(Token.DOT, w -> Paths.get(w.readString()), (w, p) -> p.resolve(w.readString()));

        reader.assertIgnore(Token.LSQR);
        from = reader.readInt();
        reader.assertIgnore(Token.ARROW);
        to = reader.readInt();
        reader.assertIgnore(Token.RSQR);
        reader.assertIgnore(Token.LPAR);

        parameters = !reader.isCurrent(Token.RPAR)
                ? reader.readList(Token.COMMA, WorkReader::readStatement)
                : new ArrayList<>();

        reader.assertIgnore(Token.RPAR);
        reader.assertIgnore(Token.COLON);

        position = reader.readList(Token.COMMA, TokenReader::readInt);
    }

    @Override
    public Stream<Path> getDependencies(FileTemplate file) {
        return Stream.of(file.absolutePath(path));
    }

    @Override
    public Substitution toManipulation(WorkProject project, WorkFile file) {
        return new Substitution(
                path,
                from,
                to,
                position.stream().mapToInt(i -> i).toArray(),
                parameters
        );
    }
}
