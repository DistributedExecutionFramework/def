package at.enfilo.def.agentconf.client;

import at.enfilo.def.agentconf.client.datatypes.AgentConfCSVFile;
import at.enfilo.def.agentconf.client.datatypes.DEFInteger;
import at.enfilo.def.agentconf.client.util.CSVReader;
import at.enfilo.def.agentconf.client.util.CSVWriter;
import at.enfilo.def.client.api.DEFClientFactory;
import at.enfilo.def.client.api.IDEFClient;
import at.enfilo.def.client.api.RoutineInstanceBuilder;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.transfer.dto.*;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

public class AgentConfClient {

    public static void main(String[] args) throws Exception {


        /**
         * mm43D routine
         */
//        List<Path> paths = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/mm43D", ".*data[0-9]+\\.csv");
//
//        for (Path path: paths) {
//            List<String> lines = CSVReader.readLinesFromFile(path);
//            String fileNameWithoutExtension = path.getFileName().toString().split("\\.")[0];
//            int timeOffset = Integer.parseInt(lines.get(0).split(";")[0]);
//            List<String> resultingLines = new LinkedList<>();
//
//            for (String line: lines) {
//                String[] elements = line.split(";");
//                if (elements.length > 0) {
//                    try {
//                        int milliseconds = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[0]).intValue();
//                        int x = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[1]).intValue();
//                        int y = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[2]).intValue();
//                        resultingLines.add(MessageFormat.format(
//                                "{0,number,#};{1,number,#};{2,number,#};{3}",
//                                milliseconds - timeOffset, x, y, fileNameWithoutExtension));
//                    } catch (ParseException e) {
//                        System.out.println(MessageFormat.format("CSV line '{0}' couldn't be parsed.", line));
//                    }
//                }
//            }
//
//            CSVWriter.writeLinesToFile("/Users/admin/Desktop/test/mm43D/result.csv", resultingLines);
//        }

        /**
         * numberVisits_unique_levelplot routine
         */
//        Path previousResultsPath = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/numberVisitsUniqueLevelplot", ".*numberVisits.*\\.csv").get(0);
//        List<String> previousResultingLines = CSVReader.readLinesFromFile(previousResultsPath);
//
//        Path newFilePath = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/numberVisitsUniqueLevelplot", ".*data[0-9]+\\.csv").get(0);
//        List<String> newLines = CSVReader.readLinesFromFile(newFilePath);
//
//        int pixelWindowWidth = 20;
//        int pixelWindowHeight = 20;
//
//        Table<Integer, Integer, Integer> visitsCountTotal = HashBasedTable.create();
//        for (String line: previousResultingLines) {
//            String[] elements = line.split(";");
//            int x = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[0]).intValue();
//            int y = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[1]).intValue();
//            int count = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[2]).intValue();
//            visitsCountTotal.put(x, y, count);
//        }
//
//        Table<Integer, Integer, Integer> visitsCountCurrent = HashBasedTable.create();
//        for (String line: newLines) {
//            String[] elements = line.split(";");
//            int xPos = Integer.parseInt(elements[1]);
//            int yPos = Integer.parseInt(elements[2]);
//
//            if (xPos > 0 && yPos > 0) {
//                int xField = xPos / pixelWindowWidth;
//                int yField = yPos / pixelWindowHeight;
//
//                int xFieldCenter = xField * pixelWindowWidth + pixelWindowWidth/2;
//                int yFieldCenter = yField * pixelWindowHeight + pixelWindowHeight/2;
//
//                visitsCountCurrent.put(xFieldCenter, yFieldCenter, 1);
//            }
//        }
//
//        visitsCountCurrent.cellSet().forEach(cell -> {
//            if (!visitsCountTotal.contains(cell.getRowKey(), cell.getColumnKey())) {
//                visitsCountTotal.put(cell.getRowKey(), cell.getColumnKey(), 0);
//            }
//            visitsCountTotal.put(cell.getRowKey(), cell.getColumnKey(), visitsCountTotal.get(cell.getRowKey(), cell.getColumnKey()) + 1);
//        });
//
//        List<String> resultingLines = new LinkedList<>();
//        visitsCountTotal.cellSet().forEach(cell -> {
//            resultingLines.add(MessageFormat.format(
//                    "{0,number,#};{1,number,#};{2,number,#}",
//                    cell.getRowKey(), cell.getColumnKey(), cell.getValue()
//            ));
//        });
//
//        CSVWriter.writeLinesToFile("/Users/admin/Desktop/test/numberVisitsUniqueLevelplot/result.csv", resultingLines);


        /**
         * numberVisits_unique_newest_levelplot
         */
//        Path path = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/numberVisitsUniqueNewestLevelplot", ".*data[0-9]+\\.csv").get(0);
//        List<String> lines = CSVReader.readLinesFromFile(path);
//
//        String fileNameWithoutExtension = path.getFileName().toString().split("\\.")[0];
//        int pixelWindowWidth = 20;
//        int pixelWindowHeight = 20;
//
//        Table<Integer, Integer, Integer> visitsCount = HashBasedTable.create();
//        for (String line: lines) {
//            String[] elements = line.split(";");
//            try {
//                int xPos = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[1]).intValue();
//                int yPos = NumberFormat.getNumberInstance(Locale.GERMANY).parse(elements[2]).intValue();
//
//                if (xPos > 0 && yPos > 0) {
//                    int xField = xPos / pixelWindowWidth;
//                    int yField = yPos / pixelWindowHeight;
//
//                    int xFieldCenter = xField * pixelWindowWidth + pixelWindowWidth/2;
//                    int yFieldCenter = yField * pixelWindowHeight + pixelWindowHeight/2;
//
//                    visitsCount.put(xFieldCenter, yFieldCenter, 1);
//                }
//            } catch (ParseException e) {
//                System.out.println(MessageFormat.format("CSV line '{0}' couldn't be parsed.", line));
//            }
//        }
//
//        List<String> resultingLines = new LinkedList<>();
//        visitsCount.cellSet().forEach(cell -> {
//            resultingLines.add(MessageFormat.format(
//                    "{0,number,#};{1,number,#};{2,number,#};{3}",
//                    cell.getRowKey(), cell.getColumnKey(), cell.getValue(), fileNameWithoutExtension
//            ));
//        });
//
//        CSVWriter.writeLinesToFile("/Users/admin/Desktop/test/numberVisitsUniqueNewestLevelplot/result.csv", resultingLines);


        /**
         * old client
         */
//        List<Path> paths = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/LNF_Daten_roboter_V3", null);
//
//        long start = System.currentTimeMillis();
//
//        // Create client
//        ServiceEndpointDTO managerEndpoint = new ServiceEndpointDTO("t-manager", 40002, Protocol.THRIFT_TCP);   // test environment
////       ServiceEndpointDTO managerEndpoint = new ServiceEndpointDTO("manager", 40002, Protocol.THRIFT_TCP);     // prod environment
//        IDEFClient defClient = DEFClientFactory.createClient(managerEndpoint);
//
//        // Create program
//        Future<String> fPId = defClient.createProgram("t-cluster", "rosa");     // test environment
////        Future<String> fPId = defClient.createProgram("cluster1", "rosa");      // prod environment
//        String pId = fPId.get();
//
//        // Create job
//        Future<String> fJId = defClient.createJob(pId);
//        String jId = fJId.get();
//        defClient.attachReduceRoutine(pId, jId, "dff9d60d-3451-49df-8246-25686ddb466d");    // test environment
////        defClient.attachReduceRoutine(pId, jId, "64117896-fb8d-482d-907a-fb34055e2599"); // prod environment
//
//        for (Path path: paths) {
//            List<String> csvLines = CSVReader.readLinesFromFile(path);
//
//            // Create task
//            RoutineInstanceDTO routine = new RoutineInstanceBuilder("5f5505a5-0019-4e2e-af80-cbfb26366322") // test environment
////            RoutineInstanceDTO routine = new RoutineInstanceBuilder("218018d6-d861-42ca-8867-bc5f89af8876") // prod environment
//                    .addParameter("file", new CSVFile(csvLines))
//                    .addParameter("nrOfRows", new DEFInteger(1080))
//                    .addParameter("nrOfColumns", new DEFInteger(1920))
//                    .build();
//            defClient.createTask(pId, jId, routine).get();
//        }
//
//        // Wait for results
//        defClient.markJobAsComplete(pId, jId);
//        JobDTO job = defClient.waitForJob(pId, jId);
//        defClient.markProgramAsFinished(pId);
//
//        System.out.println("Processing time: " + (System.currentTimeMillis() - start)/1000 + "s");
//        start = System.currentTimeMillis();
//
//        if (job.getState() == ExecutionState.SUCCESS) {
//
//            PixelCount result = defClient.extractReducedResult(job, PixelCount.class);
//
//             //Process reduced result
//
////            // Fetch all tasks and results
////            List<String> tIds = defClient.getAllTasks(pId, jId, SortingCriterion.NO_SORTING).get();
////
////            for (String tId: tIds) {
////                TaskDTO task = defClient.getTask(pId, jId, tId).get();
////                PixelCount result = defClient.extractOutParameter(task, PixelCount.class);
////
////                // Process task result
////            }
//        }
//
//        System.out.println("Download time: " + (System.currentTimeMillis() - start)/1000 + "s");
//
//        defClient.deleteJob(pId, jId);
//        defClient.deleteProgram(pId);

        /**
         * new client
         */

        // Create client
//        ServiceEndpointDTO managerEndpoint = new ServiceEndpointDTO("manager", 40002, Protocol.THRIFT_TCP);   // test environment
////       ServiceEndpointDTO managerEndpoint = new ServiceEndpointDTO("manager", 40002, Protocol.THRIFT_TCP);     // prod environment
//        IDEFClient defClient = DEFClientFactory.createClient(managerEndpoint);
//
//        // Create program
//        Future<String> fPId = defClient.createProgram("cluster1", "rosa");     // test environment
////        Future<String> fPId = defClient.createProgram("cluster1", "rosa");      // prod environment
//        String pId = fPId.get();
//
//        // Create job
//        Future<String> fJId = defClient.createJob(pId);
//        String jId = fJId.get();
//
//        // Create task for mm43D routine
//        Path path1 = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/mm43D", ".*data[0-9]+\\.csv").get(0);
//        List<String> lines = CSVReader.readLinesFromFile(path1);
//        AgentConfCSVFile file = new AgentConfCSVFile(lines, path1.getFileName().toString());
//
//        RoutineInstanceDTO routine1 = new RoutineInstanceBuilder("ee313ae0-b084-49f4-ab9b-9b73e168ef13") // test environment
////      RoutineInstanceDTO routine = new RoutineInstanceBuilder("") // prod environment
//                    .addParameter("file", file)
//                    .build();
//
//        String t1Id = defClient.createTask(pId, jId, routine1).get();
//
//        // Create task for numberVisitsUniqueLevelplot routine
//        Path previousResultsPath = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/numberVisitsUniqueLevelplot", ".*numberVisits.*\\.csv").get(0);
//        List<String> previousResultingLines = CSVReader.readLinesFromFile(previousResultsPath);
//        AgentConfCSVFile previousResultsFile = new AgentConfCSVFile(previousResultingLines, previousResultsPath.getFileName().toString());
//
//        Path newFilePath = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/numberVisitsUniqueLevelplot", ".*data[0-9]+\\.csv").get(0);
//        List<String> newLines = CSVReader.readLinesFromFile(newFilePath);
//        AgentConfCSVFile newFile = new AgentConfCSVFile(newLines, newFilePath.getFileName().toString());
//
//
//        // Create task for numberVisitsUniqueNewestLevelplot routine
//        Path path2 = CSVReader.getAllFilesFromFolder("/Users/admin/Desktop/test/numberVisitsUniqueNewestLevelplot", ".*data[0-9]+\\.csv").get(0);
//        List<String> lines2 = CSVReader.readLinesFromFile(path2);
//        AgentConfCSVFile file2 = new AgentConfCSVFile(lines2, path2.getFileName().toString());
//
//        RoutineInstanceDTO routine3 = new RoutineInstanceBuilder("86b78260-9952-4b6d-af6a-b766aa5ba666")
//                .addParameter("file", file2)
//                .addParameter("pixelWindowWidth", new DEFInteger(20))
//                .addParameter("pixelWindowHeight", new DEFInteger(20))
//                .build();
//
//        String t3Id = defClient.createTask(pId, jId, routine3).get();
//
//
//        // Wait for results
//        defClient.markJobAsComplete(pId, jId);
//        JobDTO job = defClient.waitForJob(pId, jId);
//        defClient.markProgramAsFinished(pId);
//
//        if (job.getState() == ExecutionState.SUCCESS) {
//            TaskDTO task1 = defClient.getTask(pId, jId, t1Id).get();
//            AgentConfCSVFile resultingFile1 = defClient.extractOutParameter(task1, AgentConfCSVFile.class);
//            CSVWriter.writeLinesToFile("/Users/admin/Desktop/test/mm43D/result.csv", resultingFile1.getLines());
//
//            TaskDTO task2 = defClient.getTask(pId, jId, t2Id).get();
//            AgentConfCSVFile resultingFile2 = defClient.extractOutParameter(task2, AgentConfCSVFile.class);
//            CSVWriter.writeLinesToFile("/Users/admin/Desktop/test/numberVisitsUniqueLevelplot/result.csv", resultingFile2.getLines());
//
//            TaskDTO task3 = defClient.getTask(pId, jId, t3Id).get();
//            AgentConfCSVFile resultingFile3 = defClient.extractOutParameter(task3, AgentConfCSVFile.class);
//            CSVWriter.writeLinesToFile("/Users/admin/Desktop/test/numberVisitsUniqueNewestLevelplot/result.csv", resultingFile3.getLines());
//        }
//
//        defClient.deleteProgram(pId).get();
    }
}
