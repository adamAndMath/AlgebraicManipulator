package algebraic.manipulation.console;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.equation.Work;
import algebraic.manipulator.manipulation.Manipulation;

import java.util.Arrays;

import static java.util.stream.Collectors.*;

public class CmdWork {
    public static void open(WorkFile file, Work work) {
        System.out.println(work.validate()
            ? Arrays.stream(work.getResult()).map(Object::toString).collect(joining("="))
            : Arrays.stream(work.getCurrent()).map(Object::toString).collect(joining("=")) + " != " + Arrays.stream(work.getResult()).map(Object::toString).collect(joining("="))
        );

        while (true) {
            switch (Main.in.nextLine())  {
                case "exit": return;
                case "state":
                    printState(work);
                    break;
                case "apply":
                    apply(file, work);
                    break;
                case "replace":
                    //TODO: Implement replace
                    System.out.println("Not implemented");
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
    }

    public static void printState(Work work) {
        System.out.println(Arrays.stream(work.getCurrent()).map(Object::toString).collect(joining("=")));
    }

    public static void apply(WorkFile file, Work work) {
        CmdManipulation.get(file, work.getCurrent()).ifPresent(manipulation -> {
            work.apply(Main.project, file, manipulation);
            printState(work);
        });
    }
}
