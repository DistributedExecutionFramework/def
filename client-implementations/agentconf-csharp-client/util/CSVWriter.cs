using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace agentconf_csharp_client.util
{
    public static class CSVWriter
    {
        public static void WriteLinesToFile(string filePath, List<string> lines)
        {
            using (var fileStream = File.OpenWrite(filePath))
            {
                using (var streamWriter = new StreamWriter(fileStream, Encoding.UTF8))
                {
                    foreach (string line in lines)
                    {
                        streamWriter.WriteLine(line);
                    }
                }
            }
        }
    }
}
