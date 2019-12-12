package at.enfilo.def.routine;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractRoutineTest {

	@Test
	public void createRoutine() throws Exception {
		String inPipeName = UUID.randomUUID().toString();
		String outPipeName = "null";
		String ctrlPipeName = null;
		AbstractRoutine routine = AbstractRoutine.createRoutine(
				ObjectiveTestRoutine.class.getCanonicalName(),
				inPipeName,
				outPipeName,
				ctrlPipeName
		);

		assertTrue(ObjectiveTestRoutine.class.isInstance(routine));
		assertEquals(ObjectiveTestRoutine.class.getCanonicalName(), routine.getRoutineName());
		assertEquals(inPipeName, routine.getInPipeName());
		assertNull(routine.getOutPipeName());
		assertNull(routine.getCtrlPipeName());
	}

}
