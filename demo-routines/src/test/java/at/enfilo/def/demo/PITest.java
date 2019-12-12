package at.enfilo.def.demo;

import at.enfilo.def.datatype.DEFDouble;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class PITest {

	@Test
	public void pi() throws Exception {
		PICalc piCalc = Mockito.mock(PICalc.class);
		when(piCalc.getParameter("start", DEFDouble.class)).thenReturn(new DEFDouble(0));
		when(piCalc.getParameter("end", DEFDouble.class)).thenReturn(new DEFDouble(1e6));
		when(piCalc.getParameter("stepSize", DEFDouble.class)).thenReturn(new DEFDouble(1e-6));
		when(piCalc.routine()).thenCallRealMethod();

		DEFDouble result = piCalc.routine();

		assertEquals(Math.PI, result.getValue(), 1e-2);
	}

}
