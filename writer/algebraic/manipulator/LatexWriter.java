package algebraic.manipulator;

import algebraic.manipulator.equation.*;
import algebraic.manipulator.manipulation.*;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;
import algebraic.manipulator.type.Func;
import algebraic.manipulator.type.ListType;
import algebraic.manipulator.type.SimpleType;
import algebraic.manipulator.type.Type;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class LatexWriter {
    public interface OperationWriter {
        void write(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding);
    }

    public static final Map<String, OperationWriter> operationWriters = new HashMap<>();
    public static final Map<String, String> typeNames = new HashMap<>();
    public static final List<String> colors = new ArrayList<>();

    public static void writeProject(PrintStream writer, WorkProject project, String title, String author) {
        LocalDate date = LocalDate.now();

        writer.println("\\documentclass{report}");
        writer.println("\\usepackage[utf8]{inputenc}");
        writer.println("\\usepackage{hyperref}");
        writer.println("\\usepackage{amssymb}");
        writer.println("\\usepackage{amsmath}");
        writer.println("\\usepackage{xcolor}");
        writer.println();
        writer.println("\\title{" + title + "}");
        writer.println("\\author{" + author + "}");
        writer.println("\\date{" + date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + date.getYear() + "}");
        writer.println();
        writer.println();
        writer.println("\\begin{document}");
        writer.println();
        writer.println("\\maketitle");
        writer.println("\\tableofcontents");
        writer.println();

        for (WorkFile file : project.sorted()) {
            writer.print("\\chapter{");
            writer.print(file.getPath().getFileName());
            writer.println("}\n");
            writeFile(writer, project, file);
        }

        writer.println();
        writer.println("\\end{document}");
    }

    public static void writeFile(PrintStream writer, WorkProject project, WorkFile file) {
        writer.print("Given the following assumptions:");
        file.names().filter(name -> file.get(name) instanceof Assumption).forEach(assertion -> {
            writer.println("\\\\");
            writer.print("\\label{");
            writeList(writer, file.getPath().resolve(assertion), ":", PrintStream::print);
            writer.println("}");
            writeAssumption(writer, (Assumption) file.get(assertion));
        });

        file.names().filter(name -> !(file.get(name) instanceof Assumption)).forEach(name -> {
            writer.print("\\section{");
            writer.print(name);
            writer.println("}");
            writer.print("\\label{");
            writeList(writer, file.getPath().resolve(name), ":", PrintStream::print);
            writer.println("}");
            writeEquation(writer, project, file, file.get(name));
            writer.println();
        });
    }

    public static void writeAssumption(PrintStream writer, Assumption assumption) {
        writer.print("$");

        writeDefinitions(writer, assumption.variables(), (w, var) -> w.print(var.getName()));

        writeList(writer, Arrays.asList(assumption.getResult()), "=", (w, s) ->
                writeStatement(w, s, new PathTree<>(), new PathTree<>())
        );

        writer.println("$");
    }

    public static void writeType(PrintStream writer, Type type) {
        if (type instanceof SimpleType) {
            SimpleType simple = (SimpleType) type;
            writer.print(typeNames.containsKey(simple.name) ? typeNames.get(simple.name) : simple.name);
        } else if (type instanceof Func) {
            writer.print("\\left(");
            writeType(writer, ((Func) type).from);
            writer.print(" \\rightarrow ");
            writeType(writer, ((Func) type).to);
            writer.print("\\right)");
        } else if (type instanceof ListType) {
            writer.print("\\left(");
            writeList(writer, Arrays.asList(((ListType) type).getTypes()), ",", LatexWriter::writeType);
            writer.print("\\right)");
        } else
            throw new IllegalArgumentException("Not Implemented");
    }

    public static void writeEquation(PrintStream writer, WorkProject project, WorkFile file, Equation equation) {
        if (equation instanceof Work)
            writeWork(writer, project, file, (Work) equation);
        else if (equation instanceof InductionWork)
            writeInduction(writer, project, file, (InductionWork) equation);
        else throw new IllegalArgumentException("Not Implemented");
    }

    public static void writeWork(PrintStream writer, WorkProject project, WorkFile file, Work work) {
        Statement[] statements = new Statement[work.count()];
        Arrays.fill(statements, work.getOrigin());

        PathTree<String> backColor;
        PathTree<String> textColor = new PathTree<>();

        for (Manipulation manipulation : work.getManipulations()) {
            backColor = getInputColors(project, file, statements, manipulation);

            writer.print("$$");

            writeStatement(writer, statements[0], textColor.subOrEmpty(0), backColor.subOrEmpty(0));

            for (int i = 1; i < statements.length; i++) {
                writer.print(" = ");
                writeStatement(writer, statements[i], textColor.subOrEmpty(i), backColor.subOrEmpty(i));
            }

            writer.println("$$");

            for (int i = 0; i < statements.length; i++)
                statements[i] = manipulation.apply(project, file, i, statements[i]);

            writer.print("{\\color{gray}");
            writeManipulation(writer, project, file, manipulation);
            writer.println("}");

            textColor = getOutputColors(project, file, statements, manipulation);
        }

        backColor = new PathTree<>();
        writer.print("$$");

        writeStatement(writer, statements[0], textColor.subOrEmpty(0), backColor.subOrEmpty(0));

        for (int i = 1; i < statements.length; i++) {
            writer.print(" = ");
            writeStatement(writer, statements[i], textColor.subOrEmpty(i), backColor.subOrEmpty(i));
        }

        writer.println("$$");
    }

    public static void writeInduction(PrintStream writer, WorkProject project, WorkFile file, InductionWork work) {
        writer.println("Proof by induction");
        writer.print("\\subsection{");
        writeList(writer, Arrays.asList(work.getInductive()), ", ", (w, var) -> {
            w.print(var);
            w.print("=");
            w.print(work.getBaseState(var));
        });
        writer.println("}");

        writeWork(writer, project, file, work.getBase());

        writeList(writer, Arrays.asList(work.getInductive()), "", (w, var) -> {
            w.print("\\subsection{");
            w.print(var);
            w.print("'=");
            w.println(var);
            w.println("+1}");

            writeAssumedWork(w, project, file, work.getUp(var));

            w.print("\\subsection{");
            w.print(var);
            w.print("'=");
            w.println(var);
            w.println("-1}");

            writeAssumedWork(w, project, file, work.getDown(var));
        });
    }

    public static void writeAssumedWork(PrintStream writer, WorkProject project, WorkFile file, AssumedWork work) {
        Statement[] statements = work.getOrigin();

        PathTree<String> backColor;
        PathTree<String> textColor = new PathTree<>();

        for (Manipulation manipulation : work.getManipulations()) {
            backColor = getInputColors(project, file, statements, manipulation);

            writer.print("$$");

            writeStatement(writer, statements[0], textColor.subOrEmpty(0), backColor.subOrEmpty(0));

            for (int i = 1; i < statements.length; i++) {
                writer.print(" = ");
                writeStatement(writer, statements[i], textColor.subOrEmpty(i), backColor.subOrEmpty(i));
            }

            writer.println("$$");

            for (int i = 0; i < statements.length; i++)
                statements[i] = manipulation.apply(project, file, i, statements[i]);

            writer.print("{\\color{gray}");
            writeManipulation(writer, project, file, manipulation);
            writer.println("}");

            textColor = getOutputColors(project, file, statements, manipulation);
        }

        backColor = new PathTree<>();
        writer.print("$$");

        writeStatement(writer, statements[0], textColor.subOrEmpty(0), backColor.subOrEmpty(0));

        for (int i = 1; i < statements.length; i++) {
            writer.print(" = ");
            writeStatement(writer, statements[i], textColor.subOrEmpty(i), backColor.subOrEmpty(i));
        }

        writer.println("$$");
    }

    public static PathTree<String> getInputColors(WorkProject project, WorkFile file, Statement[] current, Manipulation manipulation) {
        if (manipulation instanceof Call)
            return new PathTree<>();

        if (manipulation instanceof Substitution) {
            Substitution substitution = (Substitution) manipulation;
            Equation work = substitution.getWork(project, file);

            List<String> variables = work.variables().stream().map(Definition::getName).collect(toList());
            return substitution.getPosition().surround(work.getStatement(substitution.getFrom()).tree(var -> variables.contains(var.getName()) ? colors.get(variables.indexOf(var.getName())) : null));
        }

        if (manipulation instanceof ToEval) {
            ToEval toEval = (ToEval) manipulation;
            List<String> variables = toEval.getVariables();
            return toEval.getPosition().surround(toEval.getTree().map(var -> variables.contains(var.getVariable()) ? colors.get(variables.indexOf(var.getVariable())) : null));
        }

        if (manipulation instanceof FromEval)
            return new PathTree<>();

        if (manipulation instanceof Rename) {
            Rename rename = (Rename) manipulation;
            return rename.getPosition().surround(new PathTree<>(colors.get(0)));
        }

        throw new IllegalStateException("Not implemented");
    }

    public static PathTree<String> getOutputColors(WorkProject project, WorkFile file, Statement[] current, Manipulation manipulation) {
        if (manipulation instanceof Call) {
            Call call = (Call) manipulation;
            PathTree<String> tree = call.getCall().tree(var -> var.getName().equals(call.getTemp()) ? colors.get(0) : null);

            return new PathTree<>(IntStream.range(0, current.length).boxed().collect(toMap(i -> i, i -> tree)));
        }

        if (manipulation instanceof Substitution) {
            Substitution substitution = (Substitution) manipulation;
            Equation work = substitution.getWork(project, file);

            List<String> variables = work.variables().stream().map(Definition::getName).collect(toList());
            return substitution.getPosition().surround(work.getStatement(substitution.getTo()).tree(var -> variables.contains(var.getName()) ? colors.get(variables.indexOf(var.getName())) : null));
        }

        if (manipulation instanceof ToEval) {
            ToEval toEval = (ToEval) manipulation;
            List<String> variables = toEval.getVariables();
            Map<Integer, PathTree<String>> children = new HashMap<>();
            children.put(0, new PathTree<>(0, toEval.getTree().map(var -> variables.contains(var.getVariable()) ? colors.get(variables.indexOf(var.getVariable())) : null)));

            for (int i = 0; i < variables.size(); i++) children.put(i + 1, new PathTree<>(colors.get(i)));

            return toEval.getPosition().surround(new PathTree<>(children));
        }

        if (manipulation instanceof FromEval)
            return new PathTree<>();

        if (manipulation instanceof Rename) {
            Rename rename = (Rename) manipulation;
            return rename.getPosition().surround(new PathTree<>(colors.get(0)));
        }

        throw new IllegalStateException("Not implemented");
    }

    public static void writeManipulation(PrintStream writer, WorkProject project, WorkFile file, Manipulation manipulation) {
        if (manipulation instanceof Call)
            writeCall(writer, project, file, (Call) manipulation);
        else if (manipulation instanceof Substitution)
            writeSubstitution(writer, project, file, (Substitution) manipulation);
        else if (manipulation instanceof ToEval)
            writeToEval(writer, project, file, (ToEval) manipulation);
        else if (manipulation instanceof FromEval)
            writeFromEval(writer, project, file, (FromEval) manipulation);
        else if (manipulation instanceof Rename)
            writeRename(writer, project, file, (Rename) manipulation);
        else throw new IllegalStateException("Not implemented");
    }

    public static void writeCall(PrintStream writer, WorkProject project, WorkFile file, Call call) {
        writer.print("Call $");
        writeStatement(writer, call.apply(project, file, 0, new Variable("\\textcolor{" + colors.get(0) + "}{...}")), new PathTree<>(), new PathTree<>());
        writer.print("$");
    }

    public static void writeSubstitution(PrintStream writer, WorkProject project, WorkFile file, Substitution substitution) {
        Path workPath = file.absolutePath(substitution.getWorkPath());
        Equation work = substitution.getWork(project, file);
        List<String> variables = work.variables().stream().map(Definition::getName).collect(toList());

        Statement from = work.getStatement(substitution.getFrom());
        Statement to = work.getStatement(substitution.getTo());

        writer.print("\\hyperref[");
        writeList(writer, workPath, ":", PrintStream::print);
        writer.print("]{$");
        writeDefinitions(writer, work.variables().stream().collect(toList()), (w, var) -> {
            w.print("\\textcolor{");
            w.print(colors.get(variables.indexOf(var.getName())));
            w.print("}{");
            w.print(var.getName());
            w.print("}");
        });

        writeStatement(writer, from, from.tree(var -> variables.contains(var.getName()) ? colors.get(variables.indexOf(var.getName())) : null), new PathTree<>());
        writer.print("=");
        writeStatement(writer, to, to.tree(var -> variables.contains(var.getName()) ? colors.get(variables.indexOf(var.getName())) : null), new PathTree<>());
        writer.print("$}");
    }

    public static void writeToEval(PrintStream writer, WorkProject project, WorkFile file, ToEval toEval) {
        writer.print("Convert to function call");
    }

    public static void writeFromEval(PrintStream writer, WorkProject project, WorkFile file, FromEval fromEval) {
        writer.print("Convert from function call");
    }

    public static void writeRename(PrintStream writer, WorkProject project, WorkFile file, Rename rename) {
        writer.print("Renaming ");
        writer.print(rename.getFrom());
        writer.print(" to ");
        writer.print(rename.getTo());
    }

    public static void writeStatement(PrintStream writer, Statement statement, PathTree<String> textColor, PathTree<String> backColor) {
        writeStatement(writer, statement, textColor, backColor, 0);
    }

    public static void writeStatement(PrintStream writer, Statement statement, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (textColor.isLeaf()) {
            writer.print("\\textcolor{");
            writer.print(textColor.getLeaf());
            writer.print("}{");
            writeStatement(writer, statement, new PathTree<>(), backColor, binding);
            writer.print("}");
        } else if (backColor.isLeaf()) {
            writer.print("\\fcolorbox{");
            writer.print(backColor.getLeaf());
            writer.print("}{white}{$");
            writeStatement(writer, statement, textColor, new PathTree<>(), binding);
            writer.print("$}");
        } else if (statement instanceof Operation)
            writeOperation(writer, (Operation) statement, textColor, backColor, binding);
        else if (textColor.isEmpty() && backColor.isEmpty())
            writer.print(statement.toString());
        else throw new IllegalArgumentException("Invalid path");
    }

    public static void writeOperation(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operationWriters.containsKey(operation.getName()))
            operationWriters.get(operation.getName()).write(writer, operation, textColor, backColor, binding);
        else
            defaultWriter(writer, operation, textColor, backColor);
    }

    public static void defaultWriter(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor) {
        writer.print(operation.getName());

        if (operation.dummyCount() != 0) {
            writer.print("\\left<");
            writeList(writer, IntStream.range(0, operation.dummyCount()).mapToObj(operation::getDummy).collect(toList()), ",", PrintStream::print);
            writer.print("\\right>");
        }


        writer.print("\\left(");

        writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0));
        for (int i = 1; i < operation.parameterCount(); i++) {
            writer.print(",");
            writeStatement(writer, operation.getParameter(i), textColor.subOrEmpty(i), backColor.subOrEmpty(i));
        }

        writer.print("\\right)");
    }

    public static void writeDefinitions(PrintStream writer, List<Definition> definitions, BiConsumer<PrintStream, Definition> consumer) {
        if (!definitions.isEmpty()) {
            Definition first = definitions.get(0);
            writer.print("\\forall ");

            if (definitions.size() == 1) {
                consumer.accept(writer, first);
                writer.print("\\in ");
                writeType(writer, first.getType());
            } else if (definitions.stream().allMatch(v -> v.getType().equals(first.getType()))) {
                writer.print("\\left(");
                writeList(writer, definitions, ", ", consumer);
                writer.print("\\right)\\in ");
                writeType(writer, definitions.get(0).getType());
                writer.print("^");
                writer.print(definitions.size());
            } else {
                writer.print("\\left(");
                writeList(writer, definitions, ",", (w, var) -> {
                    consumer.accept(w, var);
                    w.print("\\in ");
                    writeType(writer, var.getType());
                });

                writer.print("\\right)");
            }

            writer.print(":");
        }
    }

    public static<T> void writeList(PrintStream writer, Iterable<T> list, String separator, BiConsumer<PrintStream, T> consumer) {
        Iterator<T> iterator = list.iterator();
        if (iterator.hasNext()) {
            consumer.accept(writer, iterator.next());

            while (iterator.hasNext()) {
                writer.print(separator);
                consumer.accept(writer, iterator.next());
            }
        }
    }
}
