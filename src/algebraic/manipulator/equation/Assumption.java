package algebraic.manipulator.equation;

import algebraic.manipulator.Definition;
import algebraic.manipulator.statement.Statement;
import java.util.List;

public class Assumption extends Equation {
    public Assumption(List<Definition> variables, Statement... result) {
        super(variables, result);
    }

    @Override
    public boolean validate() {
        return true;
    }
}
