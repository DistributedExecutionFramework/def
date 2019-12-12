package at.enfilo.def.worker.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.thrift.NodeService;
import at.enfilo.def.node.impl.NodeServiceImpl;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerService;
import at.enfilo.def.worker.impl.WorkerServiceImpl;
import at.enfilo.def.worker.impl.WorkerResponseServiceImpl;
import at.enfilo.def.worker.util.WorkerConfiguration;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

/**
 * Startup class for WorkerService(s).
 */
public class Worker extends ServerStartup<WorkerConfiguration> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Worker.class);
	private static final String CONFIG_FILE = "worker.yml";

	private static Worker instance;

	/**
	 * Avoid instancing
	 */
	private Worker() {
		super(Worker.class, WorkerConfiguration.class, CONFIG_FILE, LOGGER);
	}


	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ThriftProcessor<WorkerServiceImpl> workerServiceProcessor = new ThriftProcessor<>(
				WorkerService.class.getName(),
				new WorkerServiceImpl(),
				WorkerService.Processor<WorkerService.Iface>::new
		);
		ThriftProcessor<WorkerResponseServiceImpl> workerServiceResponseProcessor = new ThriftProcessor<>(
				WorkerResponseService.class.getName(),
				new WorkerResponseServiceImpl(),
				WorkerResponseService.Processor<WorkerResponseService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		thriftProcessors.add(workerServiceProcessor);
		thriftProcessors.add(workerServiceResponseProcessor);
		return thriftProcessors;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(new WorkerServiceImpl());
		resourceList.add(new WorkerResponseServiceImpl());
		return resourceList;
	}

	/**
	 * Worker entry point for WorkerService.
	 * @param args
	 */
	public static void main(String[] args) {

		LOGGER.info("Startup worker");

		// Cleanup working dir
		Path workingDir = Paths.get(getInstance().readConfiguration().getWorkingDir());
		if (workingDir.toFile().exists()) {
			LOGGER.info("Existing working directory {} found.", workingDir.toAbsolutePath());
			if (getInstance().readConfiguration().isCleanupWorkingDirOnStart()) {
				LOGGER.debug("Try to cleanup.");
				try {
					cleanWorkingDirectory(workingDir);
				} catch (IOException e) {
					LOGGER.warn("Cannot clean working directory.", e);
				}

				boolean isDirsCreated = workingDir.toFile().mkdirs();
				LOGGER.debug(
					"Cleaned working directory \"{}\" - {}.",
					workingDir.toAbsolutePath(),
					isDirsCreated
				);
			} else {
				LOGGER.info("Ignoring already existing working directory.");
			}

		} else {
			boolean isDirsCreated = workingDir.toFile().mkdirs();
			LOGGER.info(
				"Create working directory: \"{}\" - {}.",
				workingDir.toAbsolutePath(),
				isDirsCreated
			);
		}

		// Start services
		getInstance().startServices();
	}


	/**
	 * Cleanup WorkingDirectory.
	 * Code from: http://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
	 *
	 * @param workingDir
	 * @throws IOException
	 */
	private static void cleanWorkingDirectory(Path workingDir) throws IOException {
		Files.walkFileTree(workingDir, new SimpleFileVisitor<Path>() {
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
	}

	public static Worker getInstance() {
		if (instance == null) {
			instance = new Worker();
		}
		return instance;
	}
}
