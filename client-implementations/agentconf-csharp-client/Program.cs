using System;
using System.Collections.Generic;
using client_api;
using client_api.client;
using agentconf_csharp_client.util;

namespace agentconf_csharp_client
{
    class Program
    {
        private static string manager = "10.0.50.53";
        private static int port = 40002;
        private static Protocol protocol = Protocol.THRIFT_TCP;
        private static string clusterId = "cluster1";

        private static string userId = "agentconf";
           
        // TODO anpassen!
        private static string mm43DBasePath = "/Users/admin/Desktop/test/mm43D/";
        private static string nVULBasePath = "/Users/admin/Desktop/test/numberVisitsUniqueLevelplot/";
        private static string nVUNLBasePath = "/Users/admin/Desktop/test/numberVisitsUniqueNewestLevelplot/";

        public static void Main(String[] args)
        {
            // Create client
            System.Console.WriteLine("Start DEF client");
            DEFClient client = new DEFClient(manager, port, protocol);

            // Create program and job
            System.Console.WriteLine("Create program and job");
            string pId = client.CreateProgram(clusterId, userId).Result;
            string jId = client.CreateJob(pId).Result;

            // Create task for mm43D routine
            System.Console.WriteLine("Create task for mm43D routine");
            List<string> mm43DFileNames = CSVReader.GetAllFilesFromFolder(mm43DBasePath, "data*.csv");
            string mm43DFileName = mm43DFileNames[mm43DFileNames.Count - 1];
            List<string> mm43DLines = CSVReader.ReadLinesFromFile(mm43DBasePath + mm43DFileName);
            AgentConfCSVFile mm43DFile = new AgentConfCSVFile();
            mm43DFile.Lines = mm43DLines;
            mm43DFile.Filename = mm43DFileName;

            RoutineInstanceDTO mm43D = new RoutineInstanceBuilder("4d5771e1-af9d-4491-9c90-ead65965bead")
                .AddParameter("file", mm43DFile._id, mm43DFile)
                .Build();

            string mm43DTaskId = client.CreateTask(pId, jId, mm43D).Result;


            // Create task for numberVisitsUniqueLevelplot routine
            System.Console.WriteLine("Create task for numberVisitsUniqueLevelplot routine");
            List<string> nVULPreviousResultingFileNames = CSVReader.GetAllFilesFromFolder(nVULBasePath, "numberVisits*.csv");
            string nVULPreviousResultingFileName = nVULPreviousResultingFileNames[nVULPreviousResultingFileNames.Count - 1];
            List<string> nVULPreviousResultingLines = CSVReader.ReadLinesFromFile(nVULBasePath + nVULPreviousResultingFileName);
            AgentConfCSVFile nVULPreviousResultingFile = new AgentConfCSVFile();
            nVULPreviousResultingFile.Lines = nVULPreviousResultingLines;
            nVULPreviousResultingFile.Filename = nVULPreviousResultingFileName;

            List<string> nVULNewFileNames = CSVReader.GetAllFilesFromFolder(nVULBasePath, "data*.csv");
            string nVULNewFileName = nVULNewFileNames[nVULNewFileNames.Count - 1];
            List<string> nVULNewLines = CSVReader.ReadLinesFromFile(nVULBasePath + nVULNewFileName);
            AgentConfCSVFile nVULNewFile = new AgentConfCSVFile();
            nVULNewFile.Lines = nVULNewLines;
            nVULNewFile.Filename = nVULNewFileName;

            DEFInteger pixelWindowWidth = new DEFInteger();
            pixelWindowWidth.Value = 20;

            DEFInteger pixelWindowHeight = new DEFInteger();
            pixelWindowHeight.Value = 20;

            DEFInteger imageWidth = new DEFInteger();
            imageWidth.Value = 1920;

            DEFInteger imageHeight = new DEFInteger();
            imageHeight.Value = 1080;

            RoutineInstanceDTO numberVisitsUniqueLevelplot = new RoutineInstanceBuilder("aab75faa-6aea-439b-aed7-efb28f0069db")
                .AddParameter("previousResults", nVULPreviousResultingFile._id, nVULPreviousResultingFile)
                .AddParameter("newFile", nVULNewFile._id, nVULNewFile)
                .AddParameter("pixelWindowWidth", pixelWindowWidth._id, pixelWindowWidth)
                .AddParameter("pixelWindowHeight", pixelWindowHeight._id, pixelWindowHeight)
                .AddParameter("imageWidth", imageWidth._id, imageWidth)
                .AddParameter("imageHeight", imageHeight._id, imageHeight)
                .Build();

            string nVULTaskId = client.CreateTask(pId, jId, numberVisitsUniqueLevelplot).Result;


            // Create task for numberVisitsUniqueNewestLevelplot routine
            System.Console.WriteLine("Create task for numberVisitsUniqueNewestLevelplot routine");
            List<string> nVUNLFileNames = CSVReader.GetAllFilesFromFolder(nVUNLBasePath, "data*.csv");
            string nVUNLFileName = nVUNLFileNames[nVUNLFileNames.Count - 1];
            List<string> nVUNLLines = CSVReader.ReadLinesFromFile(nVUNLBasePath + nVUNLFileName);
            AgentConfCSVFile nVUNLFile = new AgentConfCSVFile();
            nVUNLFile.Lines = nVUNLLines;
            nVUNLFile.Filename = nVUNLFileName;

            RoutineInstanceDTO numberVisitsUniqueNewestLevelplot = new RoutineInstanceBuilder("be1273d7-682e-4bfb-90aa-7978d40366ab")
                .AddParameter("file", nVUNLFile._id, nVUNLFile)
                .AddParameter("pixelWindowWidth", pixelWindowWidth._id, pixelWindowWidth)
                .AddParameter("pixelWindowHeight", pixelWindowHeight._id, pixelWindowHeight)
                .AddParameter("imageWidth", imageWidth._id, imageWidth)
                .AddParameter("imageHeight", imageHeight._id, imageHeight)
                .Build();

            string nVUNLTaskId = client.CreateTask(pId, jId, numberVisitsUniqueNewestLevelplot).Result;


            // Finish job
            System.Console.WriteLine("Mark job as complete");
            client.MarkJobAsComplete(pId, jId);

            System.Console.WriteLine("Wait for job");
            JobDTO job = client.WaitForJob(pId, jId);

            if (job.State == ExecutionState.SUCCESS)
            {
                System.Console.WriteLine("Process results");
                TaskDTO mm43DTask = client.GetTask(pId, jId, mm43DTaskId).Result;
                AgentConfCSVFile mm43DResultFile = client.ExtractOutParameter<AgentConfCSVFile>(mm43DTask);
                CSVWriter.WriteLinesToFile(mm43DBasePath + "result.csv", mm43DResultFile.Lines);

                TaskDTO nVULTask = client.GetTask(pId, jId, nVULTaskId).Result;
                AgentConfCSVFile nVULResultFile = client.ExtractOutParameter<AgentConfCSVFile>(nVULTask);
                CSVWriter.WriteLinesToFile(nVULBasePath + "result.csv", nVULResultFile.Lines);

                TaskDTO nVUNLTask = client.GetTask(pId, jId, nVUNLTaskId).Result;
                AgentConfCSVFile nVUNLResultFile = client.ExtractOutParameter<AgentConfCSVFile>(nVUNLTask);
                CSVWriter.WriteLinesToFile(nVUNLBasePath + "result.csv", nVUNLResultFile.Lines);
            }

            System.Console.WriteLine("Delete program");
            client.DeleteProgram(pId);
        }
    }
}
