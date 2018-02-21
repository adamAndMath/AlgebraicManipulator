package algebraic.manipulator.read;

import algebraic.manipulator.Definition;
import algebraic.manipulator.PathTree;
import algebraic.manipulator.read.equation.AssumptionTemplate;
import algebraic.manipulator.read.equation.EquationTemplate;
import algebraic.manipulator.read.equation.InductionTemplate;
import algebraic.manipulator.read.equation.WorkTemplate;
import algebraic.manipulator.read.manipulation.*;
import algebraic.manipulator.statement.*;
import algebraic.manipulator.type.Func;
import algebraic.manipulator.type.TupleType;
import algebraic.manipulator.type.SimpleType;
import algebraic.manipulator.type.Type;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkReader {
    @FunctionalInterface
    public interface PartReader<T> {
        T read(TokenReader reader) throws IOException;
    }

    @FunctionalInterface
    public interface Reducer<T> {
        T read(TokenReader reader, T current) throws IOException;
    }

    public static final Map<String, PartReader<EquationTemplate>> workReaders = new HashMap<>();
    public static final Map<String, PartReader<ManipulationTemplate>> manipulationReaders = new HashMap<>();

    static {
        workReaders.put("assume", AssumptionTemplate::new);
        workReaders.put("work", WorkTemplate::new);
        workReaders.put("induction", InductionTemplate::new);

        manipulationReaders.put("substitute", SubstitutionTemplate::new);
        manipulationReaders.put("call", CallTemplate::new);
        manipulationReaders.put("fromeval", FromEvalTemplate::new);
        manipulationReaders.put("toeval", ToEvalTemplate::new);
        manipulationReaders.put("rename", RenameTemplate::new);
    }

    public static PathTree<?> readPaths(TokenReader reader) throws IOException {
        PathTree.Tree<Integer> tree = new PathTree.Tree<>();
        readPaths(reader, tree);
        return new PathTree<Integer>(tree);
    }

    public static void readPaths(TokenReader reader, PathTree.Tree<Integer> tree) throws IOException {
        if (reader.isCurrent(Token.LSQR)) {
            readPathsSiblings(reader, tree);
            return;
        }

        int i = reader.readInt();
        if (reader.isRead(Token.COMMA))
            readPaths(reader, tree.sub(i));
        else
            tree.sub(i).set(0);
    }

    public static void readPathsSiblings(TokenReader reader, PathTree.Tree<Integer> tree) throws IOException {
        reader.assertIgnore(Token.LSQR);

        if (reader.isRead(Token.RSQR)) {
            tree.set(0);
            return;
        }

        do readPaths(reader, tree);
        while (reader.isRead(Token.VBAR));

        reader.assertIgnore(Token.RSQR);
    }

    public static Variable readVariable(TokenReader reader) throws IOException {
        return new Variable(reader.readString());
    }

    public static Constant readConstant(TokenReader reader) throws IOException {
        reader.assertIgnore(Token.BACKSLASH);
        return new Constant(reader.readString());
    }

    public static Statement readOperation(TokenReader reader, String name) throws IOException {
        List<Variable> dummies = new ArrayList<>();
        List<Statement> parameters = new ArrayList<>();

        if (reader.isRead(Token.LESS)) {
            if (reader.isCurrent(Token.STRING))
                dummies.addAll(reader.readList(Token.COMMA, WorkReader::readVariable));

            reader.assertIgnore(Token.GREAT);
        }

        reader.assertIgnore(Token.LPAR);

        if (reader.isCurrent(Token.BACKSLASH) || reader.isCurrent(Token.STRING) || reader.isCurrent(Token.INT))
            parameters.addAll(reader.readList(Token.COMMA, WorkReader::readStatement));

        reader.assertIgnore(Token.RPAR);

        return new Operation(name, dummies.toArray(new Variable[0]), parameters.toArray(new Statement[0]));
    }

    public static Statement readStatement(TokenReader reader) throws IOException {
        if (reader.isCurrent(Token.INT))
            return new IntValue(reader.readInt());

        if (reader.isCurrent(Token.BACKSLASH))
            return readConstant(reader);

        String name = reader.readString();

        if (reader.isCurrent(Token.LESS) || reader.isCurrent(Token.LPAR))
            return readOperation(reader, name);

        return new Variable(name);
    }

    public static Definition readDefinition(TokenReader reader) throws IOException {
        return new Definition(readType(reader), reader.readString());
    }

    public static Type readType(TokenReader reader) throws IOException {
        Type first = readTypeIn(reader);

        if (reader.isRead(Token.ARROW))
            return new Func(first, readTypeIn(reader));

        return first;
    }

    public static Type readTypeIn(TokenReader reader) throws IOException {
        if (reader.isRead(Token.LPAR)) {
            List<Type> types = reader.readList(Token.COMMA, WorkReader::readType);
            reader.assertIgnore(Token.RPAR);
            return new TupleType(types.toArray(new Type[0]));
        }

        return new SimpleType(reader.readString());
    }

    public static ManipulationTemplate readManipulation(TokenReader reader) throws IOException {
        String manipulationType = reader.readString();

        if (!manipulationReaders.containsKey(manipulationType))
            throw new IllegalStateException(reader.getPos() + " " + manipulationType + " Not implemented");

        ManipulationTemplate manipulation = manipulationReaders.get(manipulationType).read(reader);
        reader.assertIgnore(Token.SEMI);
        return manipulation;
    }

    public static void readUsing(TokenReader reader, FileTemplate file) throws IOException {
        Path path = reader.readReduce(Token.DOT, r -> Paths.get(r.readString()), (r, p) -> p.resolve(r.readString()));
        String key = path.getFileName().toString();

        if (!reader.isRead(Token.SEMI)) {
            key = reader.readString();
            reader.assertIgnore(Token.SEMI);
        }

        file.usingFile(key, path);
    }

    public static FileTemplate readFile(TokenReader reader, Path path) throws IOException {
        FileTemplate file = new FileTemplate(path);

        while (!reader.isCurrent(Token.EOF)) {
            String keyWork = reader.readString();

            if ("using".equals(keyWork)) {
                readUsing(reader, file);
                continue;
            }

            if (!workReaders.containsKey(keyWork))
                throw new IOException(reader.getPos() + " Unknown keyword " + keyWork);

            String name = reader.readString();
            List<Variable> dummy = List.of();

            if (reader.isRead(Token.LESS)) {
                dummy = reader.readList(Token.COMMA, WorkReader::readVariable);
                reader.assertIgnore(Token.GREAT);
            }

            reader.assertIgnore(Token.LPAR);

            List<Definition> parameters = reader.isCurrent(Token.RPAR)
                    ? new ArrayList<>()
                    : reader.readList(Token.COMMA, WorkReader::readDefinition);

            reader.assertIgnore(Token.RPAR);

            EquationTemplate equation = workReaders.get(keyWork).read(reader);
            equation.dummy = dummy;
            equation.parameters = parameters;

            file.add(name, equation);
        }

        return file;
    }
}
