using System;
using System.Collections.Generic;
using client_api;
using client_api.client;

namespace def_csharp_client
{
    class Program
    {
        private static int programs = 1;
        private static int jobs = 2;
        private static int tasks = 30;

        private static String manager = "t-manager";
        private static int port = 40002;
        private static Protocol protocol = Protocol.THRIFT_TCP;
        private static String clusterId = "t-cluster";
        private static String userId = "rosa";

        private static int useCase = 3;

        public static void Main(String[] args)
        {
            System.Console.WriteLine("Start DEF client");
            DEFClient client = new DEFClient(manager, port, protocol);

            if (useCase == 1)
            {
                DEFDouble start = new DEFDouble();
                start.Value = 0;
                DEFDouble end = new DEFDouble();
                end.Value = 1e9;
                DEFDouble stepSize = new DEFDouble();
                stepSize.Value = 1e-9;

                RoutineInstanceDTO piCalc = new RoutineInstanceBuilder("cfec958c-e34f-3240-bcea-cdeebd186cf6")
                    .AddParameter("start", start._id, start)
                    .AddParameter("end", end._id, end)
                    .AddParameter("stepSize", stepSize._id, stepSize)
                    .Build();

                System.Console.WriteLine("Start program");

                for (int p = 0; p < programs; p++)
                {
                    String pId = client.CreateProgram(clusterId, userId).Result;
                    System.Console.WriteLine(String.Format("Created Program ({0}/{1}): {2}", p + 1, programs, pId));

                    for (int j = 0; j < jobs; j++)
                    {
                        String jId = client.CreateJob(pId).Result;
                        System.Console.WriteLine(String.Format("   Created Job ({0}/{1}): {2}", j + 1, jobs, jId));
                        System.Console.WriteLine(String.Format("      Create {0} tasks.", tasks));

                        for (int t = 0; t < tasks; t++)
                        {
                            System.Console.WriteLine(String.Format("         Create task {0}", t + 1));
                            client.CreateTask(pId, jId, piCalc);
                        }

                        client.MarkJobAsComplete(pId, jId);
                        System.Console.WriteLine("      Waiting for Job being done");

                        JobDTO job = client.WaitForJob(pId, jId);

                        if (job.State == ExecutionState.SUCCESS)
                        {
                            List<String> tIds = client.GetAllTasks(pId, jId, SortingCriterion.NO_SORTING).Result;
                            System.Console.WriteLine("      Fetching all Tasks of Job");
                        }
                        else
                        {
                            System.Console.WriteLine(String.Format("      Job state: {0}", job.State));
                        }

                    }
                    client.MarkProgramAsFinished(pId);
                    TicketStatusDTO status = client.DeleteProgram(pId).Result;
                    System.Console.WriteLine(String.Format("Program deleted with state {0}", status));
                }
            }
            else if (useCase == 2)
            {
                String pId = client.CreateProgram(clusterId, userId).Result;
                System.Console.WriteLine(String.Format("Created program: {0}", pId));
                String jId = client.CreateJob(pId).Result;
                System.Console.WriteLine(String.Format("   Created job: {0}", jId));

                DEFDouble start = new DEFDouble();
                start.Value = 0;
                DEFDouble end = new DEFDouble();
                end.Value = 1e9;
                DEFDouble stepSize = new DEFDouble();
                stepSize.Value = 1e-9;

                RoutineInstanceDTO piCalc = new RoutineInstanceBuilder("cfec958c-e34f-3240-bcea-cdeebd186cf6")
                    .AddParameter("start", start._id, start)
                    .AddParameter("end", end._id, end)
                    .AddParameter("stepSize", stepSize._id, stepSize)
                    .Build();

                String tId = client.CreateTask(pId, jId, piCalc).Result;
                System.Console.WriteLine(String.Format("      Created task: {0}", tId));

                client.MarkJobAsComplete(pId, jId);
                System.Console.WriteLine(String.Format("   Marked job as finished"));

                System.Console.WriteLine(String.Format("   Waiting for job being done"));
                JobDTO job = client.WaitForJob(pId, jId);

                if (job.State == ExecutionState.SUCCESS)
                {
                    TaskDTO task = client.GetTask(pId, jId, tId).Result;
                    System.Console.WriteLine("   Fetched task");

                    DEFDouble outParam = client.ExtractOutParameter<DEFDouble>(task);
                    System.Console.WriteLine(String.Format("   Extracted out parameter with value {0}", outParam.Value));
                }

                TicketStatusDTO delProgramStatus = client.DeleteProgram(pId).Result;
                System.Console.WriteLine(String.Format("Deleted program with id {0}", delProgramStatus));
            }
            else if (useCase == 3)
            {
                String pId = client.CreateProgram(clusterId, userId).Result;
                System.Console.WriteLine(String.Format("Created program: {0}", pId));

                DEFDouble resourceValue = new DEFDouble();
                resourceValue.Value = 2.0;
                String rId = client.CreateSharedResource(pId, resourceValue._id, resourceValue).Result;
                System.Console.WriteLine(String.Format("   Created shared resource: {0}", rId));

                ResourceDTO resouce = client.GetSharedResource(pId, rId).Result;

                DEFDouble fetchedValue = client.ExtractValueFromResource<DEFDouble>(resouce);
                System.Console.WriteLine(String.Format("   Fetched shared resource with value: {0}", fetchedValue.Value));

                List<String> sharedResources = client.GetAllSharedResources(pId).Result;
                System.Console.WriteLine("   Fetched all shared resources");

                TicketStatusDTO delResourcesStatus = client.DeleteSharedResource(pId, rId).Result;
                System.Console.WriteLine(String.Format("   Deleted shared resource with state {0}", delResourcesStatus));

                TicketStatusDTO abortProgramStatus = client.AbortProgram(pId).Result;
                System.Console.WriteLine(String.Format("Aborted program with state {0}", abortProgramStatus));

                TicketStatusDTO delProgramStatus = client.DeleteProgram(pId).Result;
                System.Console.WriteLine(String.Format("Deleted program with state {0}", delProgramStatus));
            }
        }
    }
}


