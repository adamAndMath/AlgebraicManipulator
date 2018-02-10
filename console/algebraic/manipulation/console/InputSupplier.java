package algebraic.manipulation.console;

import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class InputSupplier<T> implements Supplier<Optional<T>> {
    private static final Scanner in = new Scanner(System.in);

    private final Supplier<Optional<T>> content;

    private InputSupplier(Supplier<Optional<T>> content) {
        this.content = content;
    }

    public static<T> InputSupplier<T> empty() {
        return new InputSupplier<>(Optional::empty);
    }

    public static<T> InputSupplier<T> of(T content) {
        return new InputSupplier<>(() -> Optional.of(content));
    }

    public static InputSupplier<String> get(String msg) {
        return new InputSupplier<>(() -> {
            System.out.print(msg + ": ");
            return Optional.of(in.nextLine()).filter(r -> !"".equals(r));
        });
    }

    public InputSupplier<T> filter(Predicate<T> predicate, String msg) {
        return new InputSupplier<>(() -> {
            while (true) {
                Optional<T> result = content.get();
                if (!result.isPresent()) return Optional.empty();
                if (predicate.test(result.get())) return result;
                System.out.println(msg);
            }
        });
    }

    public<U> InputSupplier<U> map(Function<T, U> function) {
        return new InputSupplier<>(() -> content.get().map(function));
    }

    public<U> InputSupplier<U> mapOpt(Function<T, Optional<? extends U>> function) {
        return new InputSupplier<>(() -> content.get().map(function).filter(Optional::isPresent).map(Optional::get));
    }

    public<U> InputSupplier<U> mapSup(Function<T, InputSupplier<? extends U>> function) {
        return map(function).mapOpt(InputSupplier::get);
    }

    public<U> InputSupplier<U> tryMap(Function<T, U> function, String msg) {
        Function<T, Optional<U>> tryFunc = t -> {
            try {
                return Optional.of(function.apply(t));
            } catch (Exception e) {
                return Optional.empty();
            }
        };

        return map(tryFunc).filter(Optional::isPresent, msg).map(Optional::get);
    }

    public Optional<T> get() {
        return content.get();
    }

    public void ifPresent(Consumer<T> consumer) {
        get().ifPresent(consumer);
    }
}
