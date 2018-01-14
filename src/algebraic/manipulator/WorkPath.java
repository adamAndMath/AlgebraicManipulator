package algebraic.manipulator;

import java.util.List;

public class WorkPath {
    private final String[] path;

    public WorkPath(String... path) {
        this.path = path.clone();
    }

    public WorkPath(List<String> path) {
        this.path = path.toArray(new String[0]);
    }
}
