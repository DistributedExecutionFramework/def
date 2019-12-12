package at.enfilo.def.node.util;

import at.enfilo.def.logging.api.IDEFLogger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryCleaner {

    private final IDEFLogger logger;

    public DirectoryCleaner(IDEFLogger logger) {
        this.logger = logger;
    }

    public void cleanWorkingDirectory(NodeConfiguration configuration) {
        Path workingDir = Paths.get(configuration.getWorkingDir());
        if (workingDir.toFile().exists()) {
            this.logger.info("Existing working directory {} found.", workingDir.toAbsolutePath());
            if (configuration.isCleanupWorkingDirOnStart()) {
                this.logger.debug("Try to cleanup");
                try {
                    cleanDirectory(workingDir);
                } catch (IOException e) {
                    this.logger.warn("Cannot clean working directory", e);
                }

                boolean isDirsCreated = workingDir.toFile().mkdirs();
                this.logger.debug(
                        "Cleaned working directory \"{}\" - {}.",
                        workingDir.toAbsolutePath(),
                        isDirsCreated
                );
            } else {
                this.logger.info("Ignoring already existing working directory.");
            }
        } else {
            boolean isDirsCreated = workingDir.toFile().mkdirs();
            this.logger.info(
                    "Create working directory: \"{}\" - {}.",
                    workingDir.toAbsolutePath(),
                    isDirsCreated
            );
        }
    }

    /**
     * Cleanup WorkingDirectory.
     * Code from: http://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
     *
     * @param directory
     * @throws IOException
     */
    private void cleanDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                if (exc == null)
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                else
                {
                    // directory iteration failed; propagate exception
                    throw exc;
                }
            }
        });
    }}
