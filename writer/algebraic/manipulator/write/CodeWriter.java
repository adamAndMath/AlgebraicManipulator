package algebraic.manipulator.write;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class CodeWriter extends Writer {
    private final Writer writer;
    private List<String> errors = new LinkedList<>();

    private int line = 1;
    private int pos = 1;
    private int indent = 0;

    public CodeWriter(Writer writer) {
        this.writer = writer;
    }

    public void push() {
        indent++;
    }

    public void pop() {
        indent--;
    }

    public void error(Exception e) {
        error(e.getMessage());
    }

    public void error(String str) {
        errors.add(line + ", " + pos + ": " + str);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            error(e);
        }
    }

    @Override
    public void write(char[] chars, int i, int i1) {
        try {
            writer.write(chars, i, i1);
        } catch (IOException e) {
            error(e);
        }
    }

    @Override
    public void write(String str) {
        try {
            writer.write(str);
        } catch (IOException e) {
            error(e);
        }
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            error(e);
        }
    }

    public void writeln(String str) {
        write(str + "\n" + Stream.generate(() -> "\t").limit(indent).collect(joining()));
    }

    public void writeln() {
        write("\n" + Stream.generate(() -> "\t").limit(indent).collect(joining()));
    }
}
