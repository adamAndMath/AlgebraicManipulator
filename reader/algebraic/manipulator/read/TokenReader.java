package algebraic.manipulator.read;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TokenReader {
    private static final Predicate<String> isLetter = Pattern.compile("\\p{Alpha}").asPredicate();
    private static final Predicate<String> isDigit = Pattern.compile("\\p{Digit}").asPredicate();

    private final Reader reader;
    private Token currentToken;
    private int c;

    private int pos = 1;
    private int line = 1;
    private int tokenPos = 1;
    private int tokenLine = 1;

    private int i;
    private String str;

    public TokenReader(Reader reader) throws IOException {
        this.reader = reader;
        readChar();
        next();
    }

    public void close() throws IOException {
        reader.close();
    }

    private boolean letter(int c) {
        return isLetter.test("" + (char) c);
    }

    private boolean digit(int c) {
        return isDigit.test("" + (char) c);
    }

    private boolean alpha(int c) {
        return letter(c) || digit(c);
    }

    private int readChar() throws IOException {
        c = reader.read();

        if (c=='\n') {
            pos = 1;
            line++;
        } else {
            pos++;
        }

        return c;
    }

    public String getPos() {
        return String.format("(%d,%d)", tokenLine, tokenPos);
    }

    private void next() throws IOException {
        while (c == ' ' || c == '\t' || c == '\r' || c == '\n') readChar();

        tokenPos = pos;
        tokenLine = line;

        switch (c) {
            case -1:
                currentToken = Token.EOF;
                close();
                return;
            case '<': currentToken = Token.LESS; break;
            case '>': currentToken = Token.GREAT; break;
            case '(': currentToken = Token.LPAR; break;
            case ')': currentToken = Token.RPAR; break;
            case '[': currentToken = Token.LSQR; break;
            case ']': currentToken = Token.RSQR; break;
            case '{': currentToken = Token.LCURL; break;
            case '}': currentToken = Token.RCURL; break;
            case '|': currentToken = Token.VBAR; break;
            case '.': currentToken = Token.DOT; break;
            case ',': currentToken = Token.COMMA; break;
            case ':': currentToken = Token.COLON; break;
            case ';': currentToken = Token.SEMI; break;
            case '=': currentToken = Token.EQUAL; break;
            case '+': currentToken = Token.PLUS; break;
            case '\\': currentToken = Token.BACKSLASH; break;
            case '/':
                readChar();

                if (c == '/') {
                    do readChar(); while (c != '\n' && c != -1);
                    next();
                    return;
                }

                if (c == '*') {
                    readChar();

                    while (c != -1) {
                        if (c == '*') {
                            if (readChar() == '/') {
                                readChar();
                                next();
                                return;
                            }
                        } else readChar();
                    }

                    throw new IOException("Unended commend");
                }

                currentToken = Token.SLASH;
                return;
            case '-':
                if (readChar() == '>') {
                    currentToken = Token.ARROW;
                    break;
                }

                if (!digit(c)) {
                    currentToken = Token.DASH;
                    return;
                }

                i = '0' - c;
                while (digit(readChar())) i = 10 * i + '0' - c;
                currentToken = Token.INT;
                return;
            default:
                if (digit(c)) {
                    i = c - '0';
                    while (digit(readChar())) i = 10* i + c - '0';
                    currentToken = Token.INT;
                    return;
                }

                if (letter(c)) {
                    str = "";

                    do str += (char) c; while (alpha(readChar()));
                    currentToken = Token.STRING;
                    return;
                }

                throw new IOException(getPos() + " Unknown token + " + c + "(" + (char)c + ")");
        }

        readChar();
    }

    private void assertToken(Token token) throws IOException {
        if (currentToken != token) throw new IOException(getPos() + " Expected a " + token + ", but received " + currentToken);
    }

    public void assertIgnore(Token token) throws IOException {
        assertToken(token);
        next();
    }

    public Token read() throws IOException {
        Token token = currentToken;
        next();
        return token;
    }

    public Token current() {
        return currentToken;
    }

    public boolean isCurrent(Token token) {
        return currentToken == token;
    }

    public boolean isRead(Token token) throws IOException {
        if (currentToken != token) return false;
        next();
        return true;
    }

    public void assertIgnore(int val) throws IOException {
        assertToken(Token.INT);
        if (i != val) throw new IOException(getPos() + " Expected a " + val + ", but received " + i);
        next();
    }

    public int readInt() throws IOException {
        assertToken(Token.INT);
        int re = i;
        next();
        return re;
    }

    public void assertIgnore(String string) throws IOException {
        assertToken(Token.STRING);
        if (!str.equals(string)) throw new IOException(getPos() + " Expected a " + string + ", but received " + str);
        next();
    }

    public String getString() throws IOException {
        assertToken(Token.STRING);
        return str;
    }

    public String readString() throws IOException {
        assertToken(Token.STRING);
        String re = str;
        next();
        return re;
    }

    public boolean isRead(String str) throws IOException {
        if (!getString().equals(str)) return false;
        next();
        return true;
    }

    public<T> List<T> readList(Token separator, WorkReader.PartReader<T> function) throws IOException {
        List<T> list = new ArrayList<>();

        do list.add(function.read(this));
        while (isRead(separator));

        return list;
    }

    public<T> T readReduce(Token separator, Supplier<T> supplier, WorkReader.Reducer<T> function) throws IOException {
        T t = supplier.get();

        do t = function.read(this, t);
        while (isRead(separator));

        return t;
    }

    public<T> T readReduce(Token separator, WorkReader.PartReader<T> supplier, WorkReader.Reducer<T> function) throws IOException {
        T t = supplier.read(this);

        while (isRead(separator)) t = function.read(this, t);

        return t;
    }
}
