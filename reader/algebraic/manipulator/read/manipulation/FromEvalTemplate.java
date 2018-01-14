package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.FromEval;
import algebraic.manipulator.read.FileTemplate;
import algebraic.manipulator.read.ProjectTemplate;
import algebraic.manipulator.read.Token;
import algebraic.manipulator.read.TokenReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FromEvalTemplate implements ManipulationTemplate {
    public List<Integer> position;

    public FromEvalTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.COLON);
        position = reader.readList(Token.COMMA, TokenReader::readInt);
    }

    @Override
    public Stream<Path> getDependencies(ProjectTemplate project, FileTemplate file) {
        return Stream.empty();
    }

    @Override
    public FromEval toManipulation(WorkProject project, WorkFile file) {
        return new FromEval(position.stream().mapToInt(i -> i).toArray());
    }
}
