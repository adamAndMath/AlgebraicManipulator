package algebraic.manipulator;

import algebraic.manipulator.read.ProjectTemplate;

import java.io.IOException;
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
        LatexWriter.writeProject(System.out, project);
    }
}
