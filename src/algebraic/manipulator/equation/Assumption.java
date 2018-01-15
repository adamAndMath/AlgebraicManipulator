package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.statement.Statement;
import algebraic.manipulator.statement.Variable;

import java.util.List;

public class Assumption extends Equation {
    public Assumption(List<Variable> dummy, List<Definition> variables, Statement... result) {
        super(dummy, variables, result);
    }

    @Override
    public boolean validate() {
        return true;
    }
}
