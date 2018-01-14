package algebraic.manipulator;

import algebraic.manipulator.read.ProjectTemplate;
import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Variable;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Main {
    public static void main(String[] args) throws IOException {
        LatexWriter.operationWriters.put("add", getByOperationWriter(" + ", 1));
        LatexWriter.operationWriters.put("sub", getByOperationWriter(" - ", 1));
        LatexWriter.operationWriters.put("mult", getByOperationWriter(" \\cdot ", 2));
        LatexWriter.operationWriters.put("div", Main::latexDiv);
        LatexWriter.operationWriters.put("rec", Main::latexRec);
        LatexWriter.operationWriters.put("sum", Main::latexSum);
        LatexWriter.operationWriters.put("prod", Main::latexProd);
        LatexWriter.operationWriters.put("eval", Main::latexEval);
        LatexWriter.operationWriters.put("func", Main::latexFunc);

        LatexWriter.typeNames.put("Complex", "\\mathbb{C}");
        LatexWriter.typeNames.put("Integer", "\\mathbb{Z}");

        LatexWriter.colors.add("red");
        LatexWriter.colors.add("blue");
        LatexWriter.colors.add("green");
        LatexWriter.colors.add("orange");
        LatexWriter.colors.add("yellow");

        /*LatexWriter.colors.add("orange");
        LatexWriter.colors.add("blue");
        LatexWriter.colors.add("cyan");
        LatexWriter.colors.add("purple");*/

        ProjectTemplate template = new ProjectTemplate(Paths.get(args[0]));
        WorkProject project = template.toProject();

        //loadFile(project, "Add", Paths.get("work/Add.txt"));
        //loadFile(project, "Sub", Paths.get("work/Sub.txt"));
        //loadFile(project, "Mult", Paths.get("work/Mult.txt"));
        //loadFile(project, "Div", Paths.get("work/Div.txt"));
        //loadFile(project, "Rec", Paths.get("work/Rec.txt"));

        LatexWriter.writeProject(System.out, project);
    }

    private static LatexWriter.OperationWriter getByOperationWriter(String separator, int bind) {
        return (writer, operation, textColors, backColor, binding) -> latexBiOperator(writer, operation, textColors, backColor, binding, separator, bind);
    }

    private static void latexBiOperator(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding, String separator, int bind) {
        if (operation.dummyCount() != 0 || operation.parameterCount() != 2) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        if (binding > bind) writer.print("\\left(");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), bind);
        writer.append(separator);
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1), bind+1);
        if (binding > bind) writer.print("\\right)");
    }

    private static void latexDiv(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 0 || operation.parameterCount() != 2) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        writer.print("\\frac{");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0));
        writer.print("}{");
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1));
        writer.print("}");
    }

    private static void latexRec(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 2 || operation.parameterCount() != 3) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        writer.print("\\underset{");
        writer.print(operation.getDummy(0));
        writer.print("=1..");
        LatexWriter.writeStatement(writer, operation.getParameter(2), textColor.subOrEmpty(2), backColor.subOrEmpty(2));
        writer.print("}{\\overset{");
        writer.print(operation.getDummy(1));
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1));
        writer.print("=");
        writer.print("}{\\mathrm{R}}}");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), Integer.MAX_VALUE);
    }

    private static void latexSum(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 1 || operation.parameterCount() != 3) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        writer.print("\\displaystyle\\sum_{");
        writer.print(operation.getDummy(0));
        writer.print("=");
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1));
        writer.print("}^{");
        LatexWriter.writeStatement(writer, operation.getParameter(2), textColor.subOrEmpty(2), backColor.subOrEmpty(2));
        writer.print("}");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), Integer.MAX_VALUE);
    }

    private static void latexProd(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 1 || operation.parameterCount() != 3) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        writer.print("\\displaystyle\\prod_{");
        writer.print(operation.getDummy(0));
        writer.print("=");
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1));
        writer.print("}^{");
        LatexWriter.writeStatement(writer, operation.getParameter(2), textColor.subOrEmpty(2), backColor.subOrEmpty(2));
        writer.print("}");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), Integer.MAX_VALUE);
    }

    private static void latexEval(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 0 || operation.parameterCount() == 0) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        if (operation.getParameter(0) instanceof Variable) {
            LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0));
            writer.print("\\left(");
            LatexWriter.writeList(writer, IntStream.range(1, operation.parameterCount()).boxed().collect(Collectors.toList()), ",", (w, i) ->
                    LatexWriter.writeStatement(w, operation.getParameter(i), textColor.subOrEmpty(i), backColor.subOrEmpty(i))
            );
            writer.print("\\right)");
            return;
        }


        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), Integer.MAX_VALUE);
        writer.print("\\circ\\left(");
        LatexWriter.writeList(writer, IntStream.range(1, operation.parameterCount()).boxed().collect(Collectors.toList()), ",", (w, i) ->
                LatexWriter.writeStatement(w, operation.getParameter(i), textColor.subOrEmpty(i), backColor.subOrEmpty(i))
        );
        writer.print("\\right)");
    }

    public static void latexFunc(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {

        writer.print("\\underset{");
        LatexWriter.writeList(writer, IntStream.range(0, operation.dummyCount()).mapToObj(operation::getDummy).collect(toList()), ",", PrintStream::print);

        writer.print("}{");
        writer.print(operation.getName());
        writer.print("}\\left(");

        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0));
        for (int i = 1; i < operation.parameterCount(); i++) {
            writer.print(",");
            LatexWriter.writeStatement(writer, operation.getParameter(i), textColor.subOrEmpty(i), backColor.subOrEmpty(i));
        }

        writer.print("\\right)");
    }
}
