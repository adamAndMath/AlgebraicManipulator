package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Rename;
import algebraic.manipulator.read.FileTemplate;
import algebraic.manipulator.read.ProjectTemplate;
import algebraic.manipulator.read.Token;
import algebraic.manipulator.read.TokenReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class RenameTemplate implements ManipulationTemplate {
    public int[] position;
    public String from;
    public String to;

    public RenameTemplate(TokenReader reader) throws IOException {
        this.from = reader.readString();
        reader.assertIgnore(Token.ARROW);
        this.to = reader.readString();
        reader.assertIgnore(Token.COLON);
        this.position = reader.readList(Token.COMMA, TokenReader::readInt).stream().mapToInt(i -> i).toArray();
    }

    @Override
    public Stream<Path> getDependencies(ProjectTemplate project, FileTemplate file) {
        return Stream.empty();
    }

    @Override
    public Rename toManipulation(WorkProject project, WorkFile file) {
        return new Rename(position, from, to);
    }
}
