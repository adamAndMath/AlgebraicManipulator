package algebraic.manipulation.console;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.equation.AssumedWork;

import java.util.Arrays;

import static algebraic.manipulation.console.Main.*;
import static java.util.stream.Collectors.joining;

public class CmdAssumed {
    public static void open(WorkFile file, AssumedWork work) {
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
                case "remove":
                    work.remove(project, file);
                    printState(work);
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

    public static void printState(AssumedWork work) {
        System.out.println(Arrays.stream(work.getCurrent()).map(Object::toString).collect(joining("=")));
        if (work.validate()) System.out.println("Complete");
    }

    public static void apply(WorkFile file, AssumedWork work) {
        CmdManipulation.get(file, work.getCurrent()).ifPresent(manipulation -> {
            work.apply(Main.project, file, manipulation);
            printState(work);
        });
    }
}
