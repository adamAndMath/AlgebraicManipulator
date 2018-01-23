package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.FromEval;
import algebraic.manipulator.read.FileTemplate;
import algebraic.manipulator.read.Token;
import algebraic.manipulator.read.TokenReader;
import algebraic.manipulator.read.WorkReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FromEvalTemplate implements ManipulationTemplate {
    public PathTree<?> position;

    public FromEvalTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.COLON);
        position = WorkReader.readPaths(reader);
    }

    @Override
    public Stream<Path> getDependencies(FileTemplate file) {
        return Stream.empty();
    }

    @Override
    public FromEval toManipulation(WorkProject project, WorkFile file) {
        return new FromEval(position);
    }
}
