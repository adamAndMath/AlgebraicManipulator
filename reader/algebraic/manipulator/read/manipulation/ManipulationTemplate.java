package algebraic.manipulator.read.manipulation;

import algebraic.manipulator.WorkFile;
import algebraic.manipulator.WorkProject;
import algebraic.manipulator.manipulation.Manipulation;
import algebraic.manipulator.read.FileTemplate;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface ManipulationTemplate {
    Stream<Path> getDependencies(FileTemplate file);
    Manipulation toManipulation(WorkProject project, WorkFile file);
}
