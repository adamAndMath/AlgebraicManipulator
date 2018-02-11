package algebraic.manipulation.console;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.equation.InductionWork;
import algebraic.manipulator.statement.Variable;

import java.util.Arrays;

import static algebraic.manipulation.console.Main.*;
import static java.util.stream.Collectors.joining;

public class CmdInduction {
    public static void open(WorkFile file, InductionWork work) {
        System.out.println(work.validate()
                ? Arrays.stream(work.getInductive()).map(Object::toString).collect(joining(", ")) + " != " + Arrays.stream(work.getResult()).map(Object::toString).collect(joining("="))
                : Arrays.stream(work.getResult()).map(Object::toString).collect(joining("=")));

        while (true) {
            switch (Main.in.nextLine())  {
                case "exit": return;
                case "inductives":
                    printInductive(work);
                    break;
                case "base":
                    CmdWork.open(file, work.getBase());
                    break;
                case "inductive":
                    openInductive(file, work);
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
    }

    private static void printInductive(InductionWork work) {
        System.out.println(Arrays.stream(work.getInductive()).collect(joining(", ", "(", ")")));
    }

    private static void openInductive(WorkFile file, InductionWork work) {
        getVariable("Variable")
                .map(Variable::getName)
                .filter(var -> work.indexOf(var) != -1, "Not an inductive variable")
                .ifPresent(var -> InputSupplier.get("+/-")
                        .filter(s -> "+".equals(s) || "-".equals(s), "Undefined prefix")
                        .ifPresent(s -> { switch (s) {
                            case "+": CmdAssumed.open(file, work.getUp(var)); break;
                            case "-": CmdAssumed.open(file, work.getDown(var)); break;
                        }})
                );
    }
}
