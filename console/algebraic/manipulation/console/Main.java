package algebraic.manipulation.console;

import algebraic.manipulator.*;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.read.ProjectTemplate;
import algebraic.manipulator.read.Token;
import algebraic.manipulator.read.TokenReader;
import algebraic.manipulator.read.WorkReader;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;
import algebraic.manipulator.write.WorkWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

public class Main {
    public static final Scanner in = new Scanner(System.in);
    public static WorkProject project;

    public static void main(String[] args) throws IOException {
        setup();

        ProjectTemplate template = new ProjectTemplate(Paths.get(args[0]));
        project = template.toProject();
        commands();
    }

    private static void setup() {
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
    }

    private static void commands() {
        while (true) {
            System.out.print(">");
            switch (in.nextLine()) {
                case "exit": return;
                case "list":
                    listFiles();
                    break;
                case "savefile":
                    saveFile();
                    break;
                case "file":
                    CmdFile.openFile();
                    break;
                case "latex":
                    writeLatex();
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
    }

    private static void listFiles() {
        project.sorted().stream().map(WorkFile::getPath).forEach(System.out::println);
    }

    private static void saveFile() {
        getFile().ifPresent(file ->
            getSavePath().ifPresent(path -> {
                try {
                    WorkWriter.writeFile(file, path).forEach(System.out::println);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }));
    }

    private static void writeLatex() {
        getSavePath().ifPresent(path ->
            InputSupplier.get("Title").ifPresent(title ->
                InputSupplier.get("Author").ifPresent(author -> {
                    try {
                        PrintStream file = new PrintStream(Files.newOutputStream(path));
                        LatexWriter.writeProject(file, project, title, author);
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
            )
        );
    }

    public static InputSupplier<Integer> getInt(String msg) {
        return InputSupplier.get(msg).tryMap(Integer::parseInt, "Not an int");
    }

    public static InputSupplier<Boolean> getBool(String msg) {
        return InputSupplier.get(msg).tryMap(Boolean::parseBoolean, "Not a boolean");
    }

    public static InputSupplier<Equation> getWork() {
        return InputSupplier.get("Work Path")
                .map(Paths::get)
                .filter(path -> project.contains(path.getParent()), "No such file")
                .filter(path -> project.getFile(path.getParent()).contains(path.getFileName().toString()), "No such work")
                .map(project::getWork);
    }

    public static InputSupplier<Path> getWork(WorkFile file) {
        return InputSupplier.get("Work Path")
                .map(Paths::get)
                .filter(path -> project.contains(file.absolutePath(path).getParent()), "No such file")
                .filter(path -> project.getFile(file.absolutePath(path).getParent()).contains(file.absolutePath(path).getFileName().toString()), "No such work");
    }

    public static InputSupplier<WorkFile> getFile() {
        return InputSupplier.get("File Path")
                .map(Paths::get)
                .filter(project::contains, "No such file")
                .map(project::getFile);
    }

    public static InputSupplier<Path> getSavePath() {
        return InputSupplier.get("Save Path")
                .map(Paths::get)
                .map(Path::toAbsolutePath)
                .filter(path -> Files.isDirectory(path.getParent()), "Undefined directory")
                .filter(path -> !Files.exists(path), "File already exists");
    }

    public static InputSupplier<Variable> getVariable(String msg) {
        return InputSupplier.get(msg)
                .filter(s -> !s.contains(" "), "Variables can't contain whitespace")
                .map(Variable::new);
    }

    public static InputSupplier<List<Variable>> getVariables(String msg) {
        return getByReader(msg, "Invalid List", w -> w.readList(Token.COMMA, WorkReader::readVariable));
    }

    public static InputSupplier<Statement> getStatement(String msg) {
        return getByReader(msg, "Invalid statement", WorkReader::readStatement);
    }

    public static InputSupplier<Statement[]> getStatements(String msg) {
        return getByReader(msg, "Invalid equation", w -> w.readList(Token.EQUAL, WorkReader::readStatement).toArray(new Statement[0]));
    }

    public static InputSupplier<PathTree<?>> getPositions(String msg) {
        return getByReader(msg, "Invalid tree", WorkReader::readPaths);
    }

    public static<T> InputSupplier<T> getByReader(String msg, String fail, WorkReader.PartReader<T> function) {
        Function<String, Optional<T>> read = s -> {
            try {
                return Optional.of(function.read(new TokenReader(new StringReader(s))));
            } catch (IOException e) {
                return Optional.empty();
            }
        };

        return InputSupplier.get(msg)
                .map(read)
                .filter(Optional::isPresent, fail)
                .map(Optional::get);
    }

    public static<T> InputSupplier<List<T>> getListByReader(String msg, WorkReader.PartReader<T> function) {
        return getByReader(msg, "Invalid List", w -> w.readList(Token.COMMA, function));
    }
}
