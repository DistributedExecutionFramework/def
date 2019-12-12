package at.enfilo.def.client.shell;

import org.junit.After;
import org.junit.Before;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.JLineShellComponent;

public abstract class ShellBaseTest {
	protected JLineShellComponent shell;
	protected ObjectCommands objects;
	protected DEFShellSession session;

	@Before
	public void setUp() throws Exception {
		Bootstrap bootstrap = new Bootstrap();
		shell = bootstrap.getJLineShellComponent();

		String[] beans = bootstrap.getApplicationContext().getBeanNamesForType(ObjectCommands.class);
		if (beans.length == 1) {
			objects = ObjectCommands.class.cast(bootstrap.getApplicationContext().getBean(beans[0]));
		} else {
			throw new RuntimeException("Bean ObjectCommands not found.");
		}
		beans = bootstrap.getApplicationContext().getBeanNamesForType(DEFShellSession.class);
		if (beans.length == 1) {
			session = DEFShellSession.class.cast(bootstrap.getApplicationContext().getBean(beans[0]));
		} else {
			throw new RuntimeException("Bean DEFShellSession not found.");
		}
	}


	@After
	public void tearDown() throws Exception {
		shell.stop();
	}

	protected void changeToManagerContext() {
		session.setActiveService(Service.MANAGER);
	}

	protected void changeToClusterContext() {
		session.setActiveService(Service.CLUSTER);
	}

	protected void changeToWorkerContext() {
		session.setActiveService(Service.WORKER);
	}

	protected void changeToLibraryContext() {
		session.setActiveService(Service.LIBRARY);
	}

	protected void changeToSchedulerContext() {
		session.setActiveService(Service.SCHEDULER);
	}

	protected void changeToExecLogicContext() {
		session.setActiveService(Service.EXEC_LOGIC);
	}
}
