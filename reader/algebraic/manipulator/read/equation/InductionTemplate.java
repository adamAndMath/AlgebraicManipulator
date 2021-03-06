package algebraic.manipulator.read.equation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.equation.InductionWork;
import algebraic.manipulator.read.*;
import algebraic.manipulator.read.manipulation.ManipulationTemplate;
import algebraic.manipulator.statement.IntValue;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class InductionTemplate extends EquationTemplate {
    public List<String> inductives = new ArrayList<>();
    public List<Statement> baseStates = new ArrayList<>();
    public WorkTemplate base;
    public List<List<ManipulationTemplate>> up = new ArrayList<>();
    public List<List<ManipulationTemplate>> down = new ArrayList<>();

    public InductionTemplate(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.LCURL);
        reader.assertIgnore("base");

        do {
            inductives.add(reader.readString());
            reader.assertIgnore(Token.EQUAL);
            baseStates.add(WorkReader.readStatement(reader));
            up.add(new LinkedList<>());
            down.add(new LinkedList<>());
        } while (reader.isRead(Token.COMMA));

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
            List<ManipulationTemplate> subWork = getAssumedWork(reader);

            reader.assertIgnore(Token.LCURL);

            while (!reader.isRead(Token.RCURL))
                subWork.add(WorkReader.readManipulation(reader));
        }

        reader.assertIgnore("result");
        reader.assertIgnore(Token.LCURL);
        result = reader.readList(Token.EQUAL, WorkReader::readStatement);
        base.result = result.stream().map(s -> s.set(this::setBase)).collect(toList());
        reader.assertIgnore(Token.RCURL);
    }

    private Statement setBase(Variable var) {
        return inductives.contains(var.getName())
                ? baseStates.get(inductives.indexOf(var.getName()))
                : var.clone();
    }

    private List<ManipulationTemplate> getAssumedWork(TokenReader reader) throws IOException {
        int index = inductives.indexOf(reader.readString());

        switch (reader.read()) {
            case PLUS:
                if (!up.get(index).isEmpty())
                    throw new IOException(reader.getPos() + " " + inductives.get(index) + "+ is already defined");
                return up.get(index);
            case DASH:
                if (!down.get(index).isEmpty())
                    throw new IOException(reader.getPos() + " " + inductives + "- is already defined");
                return down.get(index);
            default:
                throw new IOException(reader.getPos() + " Induction only allows + or -");
        }
    }

    @Override
    public Stream<Path> getDependencies(FileTemplate file) {
        return Stream.concat(
                base.getDependencies(file),
                Stream.concat(up.stream(), down.stream()).flatMap(List::stream).flatMap(m -> m.getDependencies(file))
        );
    }

    @Override
    public Equation toEquation(WorkProject project, WorkFile file) {
        base.dummy = dummy;
        base.parameters = parameters.stream().filter(par -> !inductives.contains(par.getName())).collect(toList());
        InductionWork induction = new InductionWork(dummy, parameters, result.toArray(new Statement[0]), inductives.toArray(new String[0]), baseStates.toArray(new Statement[0]), base.toEquation(project, file));

        for (int i = 0; i < inductives.size(); i++) {
            String ind = inductives.get(i);

            for (int j = 0; j < up.get(i).size(); j++) {
                try {
                    induction.getUp(ind).apply(project, file, up.get(i).get(j).toManipulation(project, file));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to apply manipulation " + (j + 1) + " to " + inductives.get(i) + "+1", e);
                }
            }
        }

        for (int i = 0; i < inductives.size(); i++) {
            String ind = inductives.get(i);

            for (int j = 0; j < down.get(i).size(); j++) {
                try {
                    induction.getDown(ind).apply(project, file, down.get(i).get(j).toManipulation(project, file));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to apply manipulation " + (j + 1) + " to " + inductives.get(i) + "-1", e);
                }
            }
        }

        if (!induction.validate()) {
            for (String ind : induction.getInductive()) {
                if (!induction.getUp(ind).validate()) {
                    throw new IllegalStateException("Result of " + ind + "+1 should be "
                            + result.stream().map(s -> s.set(v -> ind.equals(v.getName()) ? new Operation("add", new Variable(ind), new IntValue(1)) : v.clone()))
                                .map(Object::toString).collect(joining("="))
                            + " not " + Arrays.stream(induction.getUp(ind).getCurrent()).map(Object::toString).collect(joining("="))
                    );
                }

                if (!induction.getDown(ind).validate()) {
                    throw new IllegalStateException("Result of " + ind + "-1 should be "
                            + result.stream().map(s -> s.set(v -> ind.equals(v.getName()) ? new Operation("sub", new Variable(ind), new IntValue(1)) : v.clone()))
                                .map(Object::toString).collect(joining("="))
                            + " not " + Arrays.stream(induction.getDown(ind).getCurrent()).map(Object::toString).collect(joining("="))
                    );
                }
            }

            throw new IllegalStateException("Invalid induction");
        }

        return induction;
    }
}
