package algebraic.manipulator;

import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Variable;

import java.io.PrintStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class LatexOperationDefault {
    public static void Setup() {
        LatexWriter.operationWriters.put("add", getBiOperationWriter(" + ", 1));
        LatexWriter.operationWriters.put("sub", getBiOperationWriter(" - ", 1));
        LatexWriter.operationWriters.put("mult", getBiOperationWriter(" \\cdot ", 2));
        LatexWriter.operationWriters.put("div", LatexOperationDefault::div);
        LatexWriter.operationWriters.put("rec", LatexOperationDefault::rec);
        LatexWriter.operationWriters.put("sum", LatexOperationDefault::sum);
        LatexWriter.operationWriters.put("prod", LatexOperationDefault::prod);
        LatexWriter.operationWriters.put("eval", LatexOperationDefault::eval);
        LatexWriter.operationWriters.put("func", LatexOperationDefault::func);
        LatexWriter.operationWriters.put("lim", LatexOperationDefault::lim);
        LatexWriter.operationWriters.put("diff", LatexOperationDefault::diff);

        LatexWriter.typeNames.put("Natural", "\\mathbb{N}");
        LatexWriter.typeNames.put("Integer", "\\mathbb{Z}");
        LatexWriter.typeNames.put("Rational", "\\mathbb{Q}");
        LatexWriter.typeNames.put("Real", "\\mathbb{R}");
        LatexWriter.typeNames.put("Complex", "\\mathbb{C}");
    }

    private static LatexWriter.OperationWriter getBiOperationWriter(String separator, int bind) {
        return (writer, operation, textColors, backColor, binding) -> biOperator(writer, operation, textColors, backColor, binding, separator, bind);
    }

    private static void biOperator(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding, String separator, int bind) {
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

    private static void div(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
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

    private static void rec(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
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
        writer.print("=");
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1));
        writer.print("}{\\mathrm{R}}}");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), Integer.MAX_VALUE);
    }

    private static void sum(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
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

    private static void prod(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
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

    private static void eval(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
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

    private static void func(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
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

    private static void lim(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 1 || operation.parameterCount() != 2) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        writer.print("\\displaystyle\\lim_{");
        writer.print(operation.getDummy(0));
        writer.print("\\to ");
        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0));
        writer.print("}");

        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1), Integer.MAX_VALUE);
    }

    private static void diff(PrintStream writer, Operation operation, PathTree<String> textColor, PathTree<String> backColor, int binding) {
        if (operation.dummyCount() != 0 || operation.parameterCount() != 2) {
            LatexWriter.defaultWriter(writer, operation, textColor, backColor);
            return;
        }

        writer.print("\\frac{\\mathrm d}{\\mathrm d ");
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1), Integer.MAX_VALUE);
        writer.print("}");

        LatexWriter.writeStatement(writer, operation.getParameter(0), textColor.subOrEmpty(0), backColor.subOrEmpty(0), Integer.MAX_VALUE);
    }
}
