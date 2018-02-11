package algebraic.manipulator.write;

import algebraic.manipulator.Definition;
import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.equation.*;
import algebraic.manipulator.manipulation.*;
import algebraic.manipulator.statement.*;
import algebraic.manipulator.type.Func;
import algebraic.manipulator.type.ListType;
import algebraic.manipulator.type.SimpleType;
import algebraic.manipulator.type.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class WorkWriter {
    public static Map<Class<? extends Manipulation>, BiConsumer<CodeWriter, Manipulation>> manipulationWriters = new HashMap<>();

    static {
        manipulationWriters.put(Substitution.class, (w, m) -> writeSubstitution(w, (Substitution) m));
        manipulationWriters.put(Call.class, (w, m) -> writeCall(w, (Call) m));
        manipulationWriters.put(Rename.class, (w, m) -> writeRename(w, (Rename) m));
        manipulationWriters.put(ToEval.class, (w, m) -> writeToEval(w, (ToEval) m));
        manipulationWriters.put(FromEval.class, (w, m) -> writeFromEval(w, (FromEval) m));
    }

    public static List<String> writeFile(WorkFile file, Path path) throws IOException {
        CodeWriter writer = new CodeWriter(Files.newBufferedWriter(path));

        writeFile(writer, file);

        writer.close();

        return writer.getErrors();
    }

    public static void writeFile(CodeWriter writer, WorkFile file) {
        if (!file.getUsing().isEmpty()) {
            file.getUsing().forEach((name, p) -> writeUsing(writer, name, p));
            writer.writeln();
        }

        writeList(writer, file.names().collect(toList()), CodeWriter::writeln, (w, n) -> writeEquation(w, n, file.get(n)));
    }

    private static void writeUsing(CodeWriter writer, String name, Path path) {
        if (!name.equals(path.getFileName().toString()))
            throw new IllegalArgumentException("Named import saving not implemented");

        writer.write("using ");
        writeWorkPath(writer, path);
        writer.writeln(";");
    }


    private static void writeEquation(CodeWriter writer, String name, Equation equation) {
        if (equation instanceof Assumption)
            writeAssumption(writer, name, (Assumption) equation);
        else if (equation instanceof Work)
            writeWork(writer, name, (Work) equation);
        else if (equation instanceof InductionWork)
            writeInductionWork(writer, name, (InductionWork) equation);
        else throw new IllegalArgumentException("Undefined equation type: " + equation.getClass());
    }

    private static void writeAssumption(CodeWriter writer, String name, Assumption assumption) {
        writeHeader(writer, "assume", name, assumption.dummies(), assumption.variables());
        writer.push();
        writer.writeln();
        writeList(writer, Arrays.asList(assumption.getResult()), "=", WorkWriter::writeStatement);
        writer.pop();
        writer.writeln(";");
    }

    private static void writeWork(CodeWriter writer, String name, Work work) {
        writeHeader(writer, "work", name, work.dummies(), work.variables());
        writeWorkBody(writer, work);
        writer.push();
        writer.writeln(" result {");
        writeList(writer, Arrays.asList(work.getResult()), "=", WorkWriter::writeStatement);
        writer.pop();
        writer.writeln();
        writer.writeln("}");
    }

    private static void writeInductionWork(CodeWriter writer, String name, InductionWork induction) {
        writeHeader(writer, "induction", name, induction.dummies(), induction.variables());
        writer.push();
        writer.writeln(" {");
        writer.write("base ");
        writeList(writer, Arrays.asList(induction.getInductive()), ", ", (w, var) -> {
            w.write(var);
            w.write("=");
            writeStatement(w, induction.getBaseState(var));
        });
        writeWorkBody(writer, induction.getBase());

        for (String var : induction.getInductive()) {
            if (induction.getUp(var) != null) {
                writer.write(" ");
                writer.write(var);
                writer.write("+");
                writeAssumedBody(writer, induction.getUp(var));
            }

            if (induction.getDown(var) != null) {
                writer.write(" ");
                writer.write(var);
                writer.write("-");
                writeAssumedBody(writer, induction.getDown(var));
            }
        }

        writer.pop();
        writer.writeln();
        writer.push();
        writer.writeln("} result {");
        writeList(writer, Arrays.asList(induction.getResult()), "=", WorkWriter::writeStatement);
        writer.pop();
        writer.writeln();
        writer.writeln("}");
    }

    private static void writeHeader(CodeWriter writer, String type, String name, List<Variable> dummies, List<Definition> variables) {
        writer.write(type);
        writer.write(" ");
        writer.write(name);

        if (!dummies.isEmpty()) {
            writer.write("<");
            writeList(writer, dummies, ", ", WorkWriter::writeVariable);
            writer.write(">");
        }

        writer.write("(");
        if (!variables.isEmpty())
            writeList(writer, variables, ", ", WorkWriter::writeDefinition);

        writer.write(")");
    }

    private static void writeWorkBody(CodeWriter writer, Work work) {
        writer.push();
        writer.writeln(" {");
        writer.writeln("let " + work.count() + " " + work.getOrigin() + ";");
        writeList(writer, work.getManipulations(), CodeWriter::writeln, WorkWriter::writeManipulation);
        writer.pop();
        writer.writeln();
        writer.write("}");
    }

    private static void writeAssumedBody(CodeWriter writer, AssumedWork work) {
        writer.push();
        writer.writeln(" {");
        writeList(writer, work.getManipulations(), CodeWriter::writeln, WorkWriter::writeManipulation);
        writer.pop();
        writer.writeln();
        writer.write("}");
    }

    private static void writeManipulation(CodeWriter writer, Manipulation manipulation) {
        if (manipulationWriters.containsKey(manipulation.getClass()))
            manipulationWriters.get(manipulation.getClass()).accept(writer, manipulation);
        else
            writer.error("Unknown manipulations type " + manipulation.getClass());

        writer.write(";");
    }

    private static void writeSubstitution(CodeWriter writer, Substitution substitution) {
        writer.write("substitute ");
        writeWorkPath(writer, substitution.getWorkPath());
        writer.write("[");
        writer.write("" + substitution.getFrom());
        writer.write("->");
        writer.write("" + substitution.getTo());
        writer.write("]");

        if (!substitution.getDummy().isEmpty()) {
            writer.write("<");
            writeList(writer, substitution.getDummy(), ",", CodeWriter::write);
            writer.write(">");
        }

        if (!substitution.getValues().stream().allMatch(Objects::isNull)) {
            writer.write("(");
            writeList(writer, substitution.getValues(), ",", (w, s) -> {
                if (s == null) w.write("-");
                else writeStatement(w, s);
            });
            writer.write(")");
        }

        writer.write(":");
        writePositions(writer, substitution.getPosition());
    }

    private static void writeCall(CodeWriter writer, Call call) {
        writer.write("call ");
        writer.write(call.getTemp());
        writer.write(" ");
        writeStatement(writer, call.getCall());
    }

    private static void writeRename(CodeWriter writer, Rename rename) {
        writer.write("rename ");
        writer.write(rename.getFrom());
        writer.write("->");
        writer.write(rename.getTo());
        writer.write(":");
        writePositions(writer, rename.getPosition());
    }

    private static void writeToEval(CodeWriter writer, ToEval toEval) {
        writer.write("toeval(");
        writeList(writer, Arrays.asList(toEval.getParameters()), ", ", (w, par) -> {
            w.write(par.getVariable());

            if (par.getPositions().isLeaf())
                w.write(":[]");
            else if (!par.getPositions().isEmpty()) {
                w.write(":");
                writePositionSiblings(writer, par.getPositions());
            } else {
                w.write("=");
                writeStatement(w, par.getStatement());
            }
        });
        writer.write("):");
        writePositions(writer, toEval.getPosition());
    }

    private static void writeFromEval(CodeWriter writer, FromEval fromEval) {
        writer.write("fromeval:");
        writePositions(writer, fromEval.getPosition());
    }

    private static void writeType(CodeWriter writer, Type type) {
        if (type instanceof SimpleType)
            writer.write(((SimpleType) type).getName());
        else if (type instanceof Func) {
            Func func = (Func) type;
            writer.write("Func<");
            writeType(writer, func.from);
            writer.write(",");
            writeType(writer, func.to);
            writer.write(">");
        }
        else if (type instanceof ListType) {
            ListType list = (ListType) type;
            writer.write("List<");
            writeList(writer, list, ",", WorkWriter::writeType);
            writer.write(">");
        }
        else throw new IllegalArgumentException("Unknown type " + type.getClass());
    }

    private static void writeDefinition(CodeWriter writer, Definition definition) {
        writeType(writer, definition.getType());
        writer.write(" ");
        writer.write(definition.getName());
    }

    private static void writeStatement(CodeWriter writer, Statement statement) {
        if (statement instanceof Operation)
            writeOperation(writer, (Operation) statement);
        else if (statement instanceof Variable)
            writeVariable(writer, (Variable) statement);
        else if (statement instanceof Constant)
            writer.write("\\" + statement);
        else if (statement instanceof IntValue)
            writer.write(statement.toString());
        else
            writer.error("Unknown statement type: " + statement.getClass());
    }

    private static void writeOperation(CodeWriter writer, Operation operation) {
        writer.write(operation.getName());

        if (operation.dummyCount() > 0) {
            writer.write("<");
            writeList(writer, IntStream.range(0, operation.dummyCount()).mapToObj(operation::getDummy).collect(toList()), ",", WorkWriter::writeVariable);
            writer.write(">");
        }

        writer.write("(");
        writeList(writer, IntStream.range(0, operation.parameterCount()).mapToObj(operation::getParameter).collect(toList()), ",", WorkWriter::writeStatement);
        writer.write(")");
    }

    private static void writeVariable(CodeWriter writer, Variable variable) {
        writer.write(variable.getName());
    }

    private static void writePositions(CodeWriter writer, PathTree<?> positions) {
        if (positions.isLeaf())
            writer.write("[]");
        else if (positions.count() > 1)
            writePositionSiblings(writer, positions);
        else
            writePositions(writer, positions, positions.keys().findAny().getAsInt());
    }

    private static void writePositionSiblings(CodeWriter writer, PathTree<?> positions) {
        writer.write("[");

        if (!positions.isLeaf())
            writeList(writer, positions.keys().boxed().collect(Collectors.toCollection(TreeSet::new)), "|", (w, i) -> writePositions(writer, positions, i));

        writer.write("]");
    }

    private static void writePositions(CodeWriter writer, PathTree<?> positions, int index) {
        writer.write("" + index);

        if (positions.sub(index).isLeaf())
            return;

        writer.write(",");
        writePositions(writer, positions.sub(index));
    }

    private static void writeWorkPath(CodeWriter writer, Path path) {
        writeList(writer, path, ".", (w, p) -> writer.write(p.toString()));
    }

    private static<T> void writeList(CodeWriter writer, Iterable<T> iterable, String separator, BiConsumer<CodeWriter, T> function) {
        writeList(writer, iterable, w -> w.write(separator), function);
    }

    private static<T> void writeList(CodeWriter writer, Iterable<T> iterable, Consumer<CodeWriter> separator, BiConsumer<CodeWriter, T> function) {
        Iterator<T> it = iterable.iterator();

        if (!it.hasNext()) return;

        function.accept(writer, it.next());

        while (it.hasNext()) {
            separator.accept(writer);
            function.accept(writer, it.next());
        }
    }
}
