package at.enfilo.def.library.util.store.driver.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FSStoreDriverTest {

    private static final Path BASE_PATH;

    static {
        String basePath;
        basePath = Paths.get("./routine-binaries").toString();
        BASE_PATH = Paths.get(basePath);
        if (!Files.exists(BASE_PATH)) {
            try {
                Files.createDirectories(BASE_PATH);
            } catch (IOException e) {
                System.out.println(String.format("Error while setup base directory %s: %s", BASE_PATH, e));
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String routineId = UUID.randomUUID().toString();
        String routineBinaryId = UUID.randomUUID().toString();
        String routineBinaryName = "binary";

        createExecutionLink(routineId, routineBinaryName, createPathToFile(routineBinaryId));
    }

    protected static void createExecutionLink(String routineId, String binaryName, Path filePath) throws IOException{
        Path linkPath = createPathToLink(routineId, binaryName);
        Path parentDirectoryPath = linkPath.getParent();
        if (!Files.exists(parentDirectoryPath)) {
            try {
                Files.createDirectories(parentDirectoryPath);
            } catch (IOException e) {
                System.out.println(String.format("Error while setup routine binary directory %s: %s", linkPath, e));
                throw new RuntimeException(e);
            }
        }
        if (Files.exists(linkPath)) {
            Files.delete(linkPath);
        }
        Files.createLink(linkPath, filePath);
    }

    private static Path createPathToLink(String routineId, String binaryName) {
        return BASE_PATH.resolve(routineId).resolve(binaryName);
    }

    private static Path createPathToFile(String routineBinaryId) {
        return BASE_PATH.resolve(routineBinaryId);
    }
}

