using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace agentconf_csharp_client.util
{
    public static class CSVReader
    {
        public static List<string> ReadLinesFromFile(string filePath)
        {
            List<string> lines = new List<string>();

            using (var fileStream = File.OpenRead(filePath))
            {
                using (var streamReader = new StreamReader(fileStream, Encoding.UTF8))
                {
                    String line;
                    while((line = streamReader.ReadLine()) != null)
                    {
                        if (!line.ToLower().Contains("milliseconds"))
                        {
                            lines.Add(line);
                        }
                    }
                }
            }
            return lines;
        }

        public static List<string> GetAllFilesFromFolder(string folderPath, string pattern)
        {
            DirectoryInfo directory = new DirectoryInfo(folderPath);
            FileInfo[] files = null;
            if (String.IsNullOrEmpty(pattern))
            {
                files = directory.GetFiles();
            }
            else
            {
                files = directory.GetFiles(pattern);
            }

            List<string> filePaths = new List<string>();
            foreach (FileInfo file in files)
            {
                if (!file.Name.StartsWith(".", StringComparison.InvariantCultureIgnoreCase))
                {
                    filePaths.Add(file.Name);
                }
            }
            return filePaths;
        }
    }
}
