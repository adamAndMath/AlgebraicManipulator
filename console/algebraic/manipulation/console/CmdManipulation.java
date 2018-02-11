package algebraic.manipulation.console;

import algebraic.manipulator.Definition;
import algebraic.manipulator.PathTree;
import algebraic.manipulator.WorkFile;
import algebraic.manipulator.equation.Equation;
import algebraic.manipulator.manipulation.*;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static algebraic.manipulation.console.Main.*;

public class CmdManipulation {
    private static final Map<String, Function<WorkFile, InputSupplier<? extends Manipulation>>> manipulations = new HashMap<>();

    static {
        manipulations.put("call", CmdManipulation::call);
        manipulations.put("substitute", CmdManipulation::substitute);
        manipulations.put("rename", CmdManipulation::rename);
        manipulations.put("toeval", CmdManipulation::toEval);
        manipulations.put("fromeval", CmdManipulation::fromEval);
    }

    public static InputSupplier<Manipulation> get(WorkFile file, Statement[] statements) {
        return InputSupplier.get("Type")
                .filter(manipulations::containsKey, "No such manipulation type")
                .map(manipulations::get)
                .<Manipulation>mapSup(f -> f.apply(file))
                .filter(m -> {
                    try {
                        for (int i = 0; i < statements.length; i++)
                            m.apply(project, file, i, statements[i]);
                        return true;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return false;
                    }
                }, "Failed to apply manipulation");
    }

    public static InputSupplier<Call> call(WorkFile file) {
        return getVariable("Temporary Variable")
                .map(Variable::getName)
                .mapSup(temp ->
                        getStatement("Call statement")
                                .filter(statement -> statement.getVariables().contains(temp), "The temporary variable must be present")
                                .map(statement -> new Call(temp, statement))
                );
    }

    public static InputSupplier<Substitution> substitute(WorkFile file) {
        return getWork(file).mapSup(path -> {
            Equation work = project.getWork(file, path);
            return getInt("From").filter(i -> 0 <= i && i < work.count(), "Out of bounds")
                .mapSup(from ->
                    getInt("To").filter(i -> 0 <= i && i < work.count(), "Out of bounds")
                        .mapOpt(to -> {
                            List<String> dummy = new LinkedList<>();

                            for (Variable dum : work.dummies()) {
                                Optional<Variable> d = getVariable(dum.getName()).get();
                                if (!d.isPresent())
                                    return Optional.empty();
                                dummy.add(d.get().getName());
                            }

                            List<Statement> values = new LinkedList<>();
                            for (Definition par : work.variables()) {
                                if (work.getStatement(from).getVariables().contains(par.getName()))
                                    values.add(null);
                                else {
                                    Optional<Statement> val = getStatement(par.getName()).get();
                                    if (!val.isPresent())
                                        return Optional.empty();
                                    values.add(val.get());
                                }
                            }

                            Optional<PathTree<?>> positions = getPositions("Positions").get();
                            return positions.map(pathTree -> new Substitution(path, from, to, pathTree, dummy, values));
                        })
                );
        });
    }

    public static InputSupplier<Manipulation> rename(WorkFile file) {
        return getVariable("From").mapSup(from ->
                getVariable("To").mapSup(to ->
                        getPositions("Positions").map(positions ->
                                (Manipulation)new Rename(positions, from.getName(), to.getName())
                        )
                )
        );
    }

    private static InputSupplier<Manipulation> toEval(WorkFile file) {
        ToEval.Parameter[] parameters = Stream.generate(getVariable("Parameter").mapOpt(var ->
                getPositions("Path").map(path -> new ToEval.Parameter(var.getName(), null, path)).get()
                        .or(getStatement("Statement").map(statement -> new ToEval.Parameter(var.getName(), statement, null)))
        )).takeWhile(Optional::isPresent).map(Optional::get).toArray(ToEval.Parameter[]::new);

        return getPositions("Positions").map(positions -> (Manipulation)new ToEval(positions, parameters));
    }

    private static InputSupplier<FromEval> fromEval(WorkFile file) {
        return getPositions("Positions").map(FromEval::new);
    }
}
