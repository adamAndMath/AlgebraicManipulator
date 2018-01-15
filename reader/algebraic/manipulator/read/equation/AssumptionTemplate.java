package algebraic.manipulator.read.equation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.equation.Assumption;
import algebraic.manipulator.read.Token;
import algebraic.manipulator.read.TokenReader;
import algebraic.manipulator.read.WorkReader;
import algebraic.manipulator.statement.Statement;

import java.io.IOException;

public class AssumptionTemplate extends EquationTemplate {
    public AssumptionTemplate(TokenReader reader) throws IOException {
        if (reader.isRead(Token.LCURL)) {
            result = reader.readList(Token.EQUAL, WorkReader::readStatement);
            reader.isRead(Token.SEMI);
            reader.assertIgnore(Token.RCURL);
        } else {
            result = reader.readList(Token.EQUAL, WorkReader::readStatement);
            reader.assertIgnore(Token.SEMI);
        }
    }

    @Override
    public Assumption toEquation(WorkProject project, WorkFile file) {
        return new Assumption(dummy, parameters, result.toArray(new Statement[0]));
    }
}
