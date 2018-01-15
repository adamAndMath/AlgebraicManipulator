package algebraic.manipulator.read.equation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Work;
import algebraic.manipulator.read.*;
import algebraic.manipulator.read.manipulation.ManipulationTemplate;
import algebraic.manipulator.statement.Statement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class WorkTemplate extends EquationTemplate {
    public Statement origin;
    public int amount;

    public List<ManipulationTemplate> manipulations = new ArrayList<>();

    public WorkTemplate() {

    }

    public WorkTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.LCURL);

        String keyWord = reader.readString();
        switch (keyWord) {
            case "let":
                amount = reader.readInt();
                origin = WorkReader.readStatement(reader);
                reader.assertIgnore(Token.SEMI);
                break;
            default: throw new IOException(reader.getPos() + " Unknown keyword: " + keyWord);
        }

        while (!reader.isRead(Token.RCURL))
            manipulations.add(WorkReader.readManipulation(reader));

        reader.assertIgnore("result");
        reader.assertIgnore(Token.LCURL);
        result = reader.readList(Token.EQUAL, WorkReader::readStatement);
        reader.assertIgnore(Token.RCURL);
    }

    @Override
    public Stream<Path> getDependencies(FileTemplate file) {
        return manipulations.stream().flatMap(m -> m.getDependencies(file));
    }

    @Override
    public Work toEquation(WorkProject project, WorkFile file) {
        Work work = new Work(parameters, result.toArray(new Statement[0]), amount, origin);

        for (int i = 0; i < manipulations.size(); i++) {
            try {
                work.apply(project, file, manipulations.get(i).toManipulation(project, file));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to apply manipulation " + (i+1), e);
            }
        }

        if (!work.validate()) {
            throw new IllegalStateException("Result should be "
                    + result.stream().map(Object::toString).collect(joining("="))
                    + " not " + Arrays.stream(work.getCurrent()).map(Object::toString).collect(joining("="))
            );
        }

        return work;
    }
}
