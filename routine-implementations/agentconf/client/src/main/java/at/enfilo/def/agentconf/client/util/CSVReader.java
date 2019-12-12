package at.enfilo.def.agentconf.client.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVReader {

    public static List<String> readLinesFromFile(Path filePath) {

        BufferedReader reader = null;
        List<String> lines = new LinkedList<>();

        try {
            //URL resource = CSVReader.class.getClassLoader().getResource(filePath);
            File file = new File(filePath.toString());
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (!line.toLowerCase().contains("milliseconds")) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();;
                }
            }
        }
        return lines;
    }

    public static List<Path> getAllFilesFromFolder(String folderPath, String pattern) {
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths.filter(Files::isRegularFile)
                .filter(p -> {
                    if (pattern != null && !pattern.isEmpty()) {
                        return p.toString().matches(pattern);
                    }
                    if (p.getFileName().toString().startsWith(".")) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
