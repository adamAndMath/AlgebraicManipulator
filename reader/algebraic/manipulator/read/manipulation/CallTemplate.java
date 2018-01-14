package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Call;
import algebraic.manipulator.read.FileTemplate;
import algebraic.manipulator.read.ProjectTemplate;
import algebraic.manipulator.read.TokenReader;
import algebraic.manipulator.read.WorkReader;
import algebraic.manipulator.statement.Statement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CallTemplate implements ManipulationTemplate {
    public String temp;
    public Statement call;

    public CallTemplate(TokenReader reader) throws IOException {
        temp = reader.readString();
        call = WorkReader.readStatement(reader);
    }

    @Override
    public Stream<Path> getDependencies(ProjectTemplate project, FileTemplate file) {
        return Stream.empty();
    }

    @Override
    public Call toManipulation(WorkProject project, WorkFile file) {
        return new Call(temp, call);
    }
}
