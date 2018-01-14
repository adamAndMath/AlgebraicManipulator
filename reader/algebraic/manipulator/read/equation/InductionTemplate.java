package algebraic.manipulator.read.equation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.equation.InductionWork;
import algebraic.manipulator.read.*;
import algebraic.manipulator.read.manipulation.ManipulationTemplate;
import algebraic.manipulator.statement.Statement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class InductionTemplate extends EquationTemplate {
    public String inductive;
    public Statement baseState;
    public WorkTemplate base;
    public List<ManipulationTemplate> up;
    public List<ManipulationTemplate> down;

    public InductionTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.LCURL);
        reader.assertIgnore("base");
        inductive = reader.readString();
        reader.assertIgnore(Token.EQUAL);
        baseState = WorkReader.readStatement(reader);
        base = new WorkTemplate();
        reader.assertIgnore(Token.LCURL);

        String keyWord = reader.readString();
        switch (keyWord) {
            case "let":
                base.amount = reader.readInt();
                base.origin = WorkReader.readStatement(reader);
                reader.assertIgnore(Token.SEMI);
                break;
            default: throw new IOException(reader.getPos() + " Unknown keyword: " + keyWord);
        }

        while (!reader.isRead(Token.RCURL))
            base.manipulations.add(WorkReader.readManipulation(reader));

        while (!reader.isRead(Token.RCURL)) {
            reader.assertIgnore(inductive);

            switch (reader.read()) {
                case PLUS:
                    if (up != null)
                        throw new IOException(reader.getPos() + " " + inductive + "+ is already defined");

                    reader.assertIgnore(Token.LCURL);
                    up = new ArrayList<>();

                    while (!reader.isRead(Token.RCURL))
                        up.add(WorkReader.readManipulation(reader));

                    break;
                case DASH:
                    if (down != null)
                        throw new IOException(reader.getPos() + " " + inductive + "- is already defined");

                    reader.assertIgnore(Token.LCURL);
                    down = new ArrayList<>();

                    while (!reader.isRead(Token.RCURL))
                        down.add(WorkReader.readManipulation(reader));

                    break;
                default:
                    throw new IOException(reader.getPos() + " Induction only allows + or -");
            }
        }

        reader.assertIgnore("result");
        reader.assertIgnore(Token.LCURL);
        result = reader.readList(Token.EQUAL, WorkReader::readStatement);
        base.result = result.stream().map(s -> s.set(var -> inductive.equals(var.getName()) ? baseState.clone() : var.clone())).collect(toList());
        reader.assertIgnore(Token.RCURL);
    }

    @Override
    public Stream<Path> getDependencies(ProjectTemplate project, FileTemplate file) {
        return Stream.concat(
                base.getDependencies(project, file),
                Stream.concat(up.stream(), down.stream()).flatMap(m -> m.getDependencies(project, file))
        );
    }

    @Override
    public Equation toEquation(WorkProject project, WorkFile file) {
        base.parameters = parameters.stream().filter(par -> !par.getName().equals(inductive)).collect(toList());
        InductionWork induction = new InductionWork(parameters, result.toArray(new Statement[0]), inductive, baseState, base.toEquation(project, file));

        for (int i = 0; i < up.size(); i++) {
            try {
                induction.getUp().apply(project, file, up.get(i).toManipulation(project, file));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to apply manipulation " + (i + 1) + " to +1", e);
            }
        }

        if (down != null) {
            for (int i = 0; i < down.size(); i++) {
                try {
                    induction.getDown().apply(project, file, down.get(i).toManipulation(project, file));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to apply manipulation " + (i + 1) + " to -1", e);
                }
            }
        }

        if (!induction.validate())
            throw new IllegalStateException("Result should be " + result.stream().map(Object::toString).collect(Collectors.joining("=")));

        return induction;
    }
}
