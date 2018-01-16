package algebraic.manipulator;

import algebraic.manipulator.read.ProjectTemplate;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        LatexOperationDefault.Setup();

        LatexWriter.colors.add("red");
        LatexWriter.colors.add("blue");
        LatexWriter.colors.add("olive");
        LatexWriter.colors.add("orange");
        LatexWriter.colors.add("yellow");

        /*LatexWriter.colors.add("orange");
        LatexWriter.colors.add("blue");
        LatexWriter.colors.add("cyan");
        LatexWriter.colors.add("purple");*/

        ProjectTemplate template = new ProjectTemplate(Paths.get(args[0]));
        WorkProject project = template.toProject();

        PrintStream file = new PrintStream(Files.newOutputStream(Paths.get(args[1])));
        LatexWriter.writeProject(file, project, args[2], args[3]);
        file.close();
    }
}
