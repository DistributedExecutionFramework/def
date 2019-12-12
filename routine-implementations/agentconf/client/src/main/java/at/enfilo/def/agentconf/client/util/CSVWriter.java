package at.enfilo.def.agentconf.client.util;

import java.io.*;
import java.util.List;

public class CSVWriter {

    public static void writeLinesToFile(String filePath, List<String> csvLines) {
        BufferedWriter writer = null;

        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (String line: csvLines) {
                writer.write((line));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
