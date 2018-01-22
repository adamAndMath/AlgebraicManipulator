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

import static java.util.stream.Collectors.toList;

public class SubstitutionTemplate implements ManipulationTemplate {
    public Path path;
    public int from;
    public int to;
    public List<String> dummy;
    public List<Statement> parameters;
    public List<Integer> position;

    public SubstitutionTemplate(TokenReader reader) throws IOException {
        path = reader.readReduce(Token.DOT, w -> Paths.get(w.readString()), (w, p) -> p.resolve(w.readString()));

        reader.assertIgnore(Token.LSQR);
        from = reader.readInt();
        reader.assertIgnore(Token.ARROW);
        to = reader.readInt();
        reader.assertIgnore(Token.RSQR);

        if (reader.isRead(Token.LESS)) {
            dummy = reader.readList(Token.COMMA, TokenReader::readString);
            reader.assertIgnore(Token.GREAT);
        } else dummy = List.of();

        if (reader.isRead(Token.LPAR)) {
            parameters = !reader.isCurrent(Token.RPAR)
                    ? reader.readList(Token.COMMA, w -> w.isRead(Token.DASH) ? null : WorkReader.readStatement(w))
                    : new ArrayList<>();

            reader.assertIgnore(Token.RPAR);
        }

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
                dummy,
                parameters == null
                        ? Stream.generate(() -> (Statement)null).limit(project.getWork(file, path).variables().size()).collect(toList())
                        : parameters
        );
    }
}
