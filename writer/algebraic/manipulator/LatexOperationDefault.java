package algebraic.manipulator;

import algebraic.manipulator.statement.Operation;
import algebraic.manipulator.statement.Variable;

import java.io.PrintStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class LatexOperationDefault {
    public static void Setup() {
        LatexWriter.operationWriters.put("add", getByOperationWriter(" + ", 1));
        LatexWriter.operationWriters.put("sub", getByOperationWriter(" - ", 1));
        LatexWriter.operationWriters.put("mult", getByOperationWriter(" \\cdot ", 2));
        LatexWriter.operationWriters.put("div", LatexOperationDefault::latexDiv);
        LatexWriter.operationWriters.put("rec", LatexOperationDefault::latexRec);
        LatexWriter.operationWriters.put("sum", LatexOperationDefault::latexSum);
        LatexWriter.operationWriters.put("prod", LatexOperationDefault::latexProd);
        LatexWriter.operationWriters.put("eval", LatexOperationDefault::latexEval);
        LatexWriter.operationWriters.put("func", LatexOperationDefault::latexFunc);

        LatexWriter.typeNames.put("Natural", "\\mathbb{N}");
        LatexWriter.typeNames.put("Integer", "\\mathbb{Z}");
        LatexWriter.typeNames.put("Rational", "\\mathbb{Q}");
        LatexWriter.typeNames.put("Real", "\\mathbb{R}");
        LatexWriter.typeNames.put("Complex", "\\mathbb{C}");
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
        writer.print("=");
        LatexWriter.writeStatement(writer, operation.getParameter(1), textColor.subOrEmpty(1), backColor.subOrEmpty(1));
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
