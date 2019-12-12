package at.enfilo.def.routine.factory;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.routine.exception.PipeCreationException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


/**
 * Base factory for creating pipes.
 *
 * Created by mase on 04.08.2017.
 */
public class NamedPipeFactory {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(NamedPipeFactory.class);

    private static final OS HOST_OS = NamedPipeFactory.getHostOS();

    private static final int SUCCESS_EXIT_CODE = 0;
    private static final int SUCCESS_AWAIT_TIMEOUT = 10;

    /**
     * Private constructor.
     * This class provides only static methods.
     */
    private NamedPipeFactory() {
        // Hiding public constructor.
    }

    /**
     * Create a new named pipe.
     * @param name - name of pipe (file name).
     * @return full (absolute) pipe name.
     * @throws PipeCreationException
     */
    public static String createPipe(final String name)
    throws PipeCreationException {
        try {
            switch (HOST_OS) {
                case UNIX:
                case LINUX:
                case BSD:
                case MAC:
                    File pipe = new File(name);
                    String pipeAbsolutePath = pipe.getAbsolutePath();

                    if (!pipe.exists()) {
                        LOGGER.debug("Creating pipe \"{}\".", pipeAbsolutePath);

                        ProcessBuilder createPipe = new ProcessBuilder(
                            HOST_OS.getPipeCreationCommand(),
                            pipe.toString()
                        );

                        Process p = createPipe.start();
                        p.waitFor(SUCCESS_AWAIT_TIMEOUT, TimeUnit.SECONDS);

                        if (p.exitValue() != SUCCESS_EXIT_CODE) {
                            throw new PipeCreationException(String.format(
                                "%s failed with exit code: %d",
                                HOST_OS.getPipeCreationCommand(),
                                p.exitValue()
                            ));
                        }
                    } else {
                        LOGGER.debug("Pipe \"{}\" already exists.", pipeAbsolutePath);
                    }

                    return pipeAbsolutePath;

                case WINDOWS: {
//    				pipeName = WIN_PIPE_PREFIX + name;
//    				LOGGER.debug("Create NamedPipe {}", pipeName);
//    				WinNT.HANDLE handle = Kernel32.INSTANCE.CreateNamedPipe(
//    						pipeName,
//    						WinBase.PIPE_ACCESS_DUPLEX,        // dwOpenMode
//    						WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_WAIT,    // dwPipeMode
//    						10,    // nMaxInstances,
//    						Byte.MAX_VALUE,    // nOutBufferSize,
//    						Byte.MAX_VALUE,    // nInBufferSize,
//    						1000,    // nDefaultTimeOut,
//    						null); // lpSecurityAttributes
//
//    				if (WinBase.INVALID_HANDLE_VALUE.equals(handle)) {
//    					throw new PipeCreationException(handle.toString());
//    				}
//    				handles.put(pipeName, handle);
                    LOGGER.error("Windows OS is currently not supported.");
                    return name;
                }

                default: {
                    String osNotSupportedMessage = String.format(
                        "Your OS (\"%s\") is currently not supported.",
                        HOST_OS
                    );

                    LOGGER.error(osNotSupportedMessage);
                    throw new PipeCreationException(osNotSupportedMessage);
                }

            }
        } catch (IOException | InterruptedException e) {
            throw new PipeCreationException(e);
        }
    }

    /**
     * Create a new named pipe.
     * @param pipe - file for new named pipe.
     * @return full (absolute) pipe reference.
     * @throws PipeCreationException
     */
    public static File createPipe(final File pipe) throws PipeCreationException {
        if (pipe == null) {
        	throw new PipeCreationException("Pipe file can not be null.");
		}
		if (pipe.getParentFile() != null) {
			pipe.getParentFile().mkdirs();
		}
        return new File(createPipe(pipe.getAbsolutePath()));
    }

    /**
     * Delete pipe.
     * @param name - name of a pipe.
     */
    public static boolean deletePipe(final String name) {
        LOGGER.debug("Deleting Pipe \"{}\".", name);

        switch (HOST_OS) {
            case UNIX:
            case LINUX:
            case BSD:
            case MAC:
                File pipe = new File(name);
                return pipe.delete();

            case WINDOWS: {
                // TODO
                // Maybe there is nothing todo
                return true;
            }

            default: return false;
        }
    }

    /**
     * Delete pipe.
     * @param pipe - reference to pipe.
     */
    public static boolean deletePipe(final File pipe) {
        return deletePipe(pipe.getAbsolutePath());
    }

    /**
     * Helper method to resolve host os.
     * @return resolved OS in form of {@code OS} enum.
     */
    private static OS getHostOS() {
        final String currentOSName = System.getProperty("os.name");
        return Arrays.stream(OS.values()).filter(
            os -> currentOSName.toLowerCase().contains(os.getOsAbbreviation())
        ).findFirst().orElse(OS.UNKNOWN);
    }

    /**
     * Helper enum containing info about known OSs.
     */
    private enum OS {
        UNIX("unix", "mkfifo"),
        LINUX("linux", "mkfifo"),
        BSD("bsd", "mkfifo"),
        MAC("mac", "mkfifo"),
        WINDOWS("win", ""),
        UNKNOWN("null", "");

        private final String osAbbreviation;
        private final String pipeCreationCommand;

        OS(final String osAbbreviation, final String pipeCreationCommand) {
            this.osAbbreviation = osAbbreviation;
            this.pipeCreationCommand = pipeCreationCommand;
        }

        public String getOsAbbreviation() {
            return osAbbreviation;
        }

        public String getPipeCreationCommand() {
            return pipeCreationCommand;
        }
    }
}
