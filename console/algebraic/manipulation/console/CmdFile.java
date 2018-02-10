package algebraic.manipulation.console;

import algebraic.manipulator.Definition;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.equation.Assumption;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.equation.InductionWork;
import algebraic.manipulator.equation.Work;
import algebraic.manipulator.read.WorkReader;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;
import algebraic.manipulator.write.WorkWriter;

import java.io.IOException;
import java.util.*;

import static algebraic.manipulation.console.Main.*;
import static java.util.stream.Collectors.*;

public class CmdFile {
    public static void openFile() {
        getFile().ifPresent(CmdFile::openFile);
    }

    public static void openFile(WorkFile file) {
        while (true) {
            System.out.print(">");
            switch (in.nextLine()) {
                case "exit": return;
                case "save":
                    save(file);
                    break;
                case "uses":
                    file.getUsing().keySet().forEach(System.out::println);
                    break;
                case "works":
                    file.names().forEachOrdered(System.out::println);
                    break;
                case "use":
                    use(file);
                    break;
                case "work":
                    openWork(file);
                    break;
                case "new":
                    newWork(file);
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
    }

    private static void save(WorkFile file) {
        getSavePath().ifPresent(path -> {
            try {
                WorkWriter.writeFile(file, path).forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void use(WorkFile file) {
        getFile()
                .map(WorkFile::getPath)
                .filter(path -> !file.getUsing().containsKey(path.getFileName().toString()), "Already using a work by that name")
                .ifPresent(path -> file.usingFile(path.getFileName().toString(), path));
    }

    public static void openWork(WorkFile file) {
        getWork(file).ifPresent(work -> openWork(file, work));
    }

    public static void openWork(WorkFile file, Equation work) {
        if (work instanceof Assumption)
            System.out.println(Arrays.stream(work.getResult()).map(Statement::toString).collect(joining("=")));
        else if (work instanceof Work)
            CmdWork.open(file, (Work) work);
        else if (work instanceof InductionWork)
            CmdInduction.open(file, (InductionWork) work);
        else
            System.out.println("Can't open works of the type " + work.getClass());
    }

    public static InputSupplier<Equation> getWork(WorkFile file) {
        return InputSupplier.get("Work").filter(file::contains, "No such work").map(file::get);
    }

    public static void newWork(WorkFile file) {
        InputSupplier.get("Name")
                .filter(s -> !file.contains(s), "Work already exists")
                .ifPresent(name ->
                        getStatements("Result").ifPresent(result -> {
                            Set<Variable> resultDummies = Arrays.stream(result).map(Statement::getDummies).flatMap(Set::stream).map(Variable::new).collect(toSet());

                            (resultDummies.isEmpty() ? InputSupplier.of(List.<Variable>of()) : getListByReader("Dummies", WorkReader::readVariable))
                                    .filter(d -> d.containsAll(resultDummies), "All dummies must be defined")
                                    .ifPresent(dummy ->
                                            getListByReader("Parameters", WorkReader::readDefinition)
                                                    .filter(d -> d.stream().map(Definition::getName).collect(toSet()).containsAll(Arrays.stream(result).map(Statement::getVariables).flatMap(Set::stream).collect(toSet())), "All parameters must be defined")
                                                    .ifPresent(parameters ->
                                                            newWork(file, name, result, dummy, parameters)
                                                    )
                                    );
                        })
                );
    }

    private static void newWork(WorkFile file, String name, Statement[] result, List<Variable> dummy, List<Definition> parameters) {
        InputSupplier.get("WorkType")
                .filter(s -> "assumption".equals(s) || "work".equals(s) || "induction".equals(s), "Unknown work type")
                .mapSup(type -> { switch (type) {
                    case "assumption": return assumption(result, dummy, parameters);
                    case "work": return work(result, dummy, parameters);
                    case "induction": return induction(result, dummy, parameters);
                    default: throw new IllegalStateException();
                }}).ifPresent(work -> {
                    file.add(name, work);
                    openWork(file, work);
                });
    }

    private static InputSupplier<Assumption> assumption(Statement[] result, List<Variable> dummy, List<Definition> parameters) {
        return InputSupplier.of(new Assumption(dummy, parameters, result));
    }

    private static InputSupplier<Work> work(Statement[] result, List<Variable> dummy, List<Definition> parameters) {
        return getInt("Amount").mapSup(amount ->
                getStatement("Origin").map(origin ->
                        new Work(dummy, parameters, result, amount, origin)
                )
        );
    }

    private static InputSupplier<InductionWork> induction(Statement[] result, List<Variable> dummy, List<Definition> parameters) {
        return getListByReader("Inductives", w -> WorkReader.readVariable(w).getName()).mapSup(variables -> {
            List<Statement> baseStates = new LinkedList<>();

            for (String inductive : variables) {
                Optional<Statement> base = getStatement(inductive + " base").get();
                if (!base.isPresent()) return InputSupplier.empty();
                baseStates.add(base.get());
            }

            return getStatement("Origin").map(origin ->
                    new InductionWork(dummy, parameters, result, variables.toArray(new String[0]), baseStates.toArray(new Statement[0]), origin)
            );
        });
    }
}
