package at.enfilo.def.node.routine.factory.impl;

import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class PlaceholderTest {

    @Test
    public void argumentsTest() {
        List<String> arguments = new ArrayList<>();
        arguments.add("arg1");
        arguments.add("arg2");
        arguments.add("arg3");
        arguments.add("arg4");

        IPlaceholder aPlaceholder = new ArgumentsPlaceholder(arguments);

        assertEquals("arg1", aPlaceholder.get("arg"));
        assertEquals("arg2", aPlaceholder.get("arg"));
        assertEquals("arg3", aPlaceholder.get("arg"));
        assertEquals("arg4", aPlaceholder.get("arg"));
        assertEquals("arg1", aPlaceholder.get("arg"));

        assertEquals("arg1 arg2 arg3 arg4", aPlaceholder.get("args"));

        assertEquals("arg1", aPlaceholder.get("arg0"));
        assertEquals("arg2", aPlaceholder.get("arg1"));
        assertEquals("arg3", aPlaceholder.get("arg2"));
        assertEquals("arg4", aPlaceholder.get("arg3"));

        IPlaceholder aPlaceholder2 = new ArgumentsPlaceholder(null);
        IPlaceholder aPlaceholder3 = new ArgumentsPlaceholder(new ArrayList<>());

        assertEquals(null, aPlaceholder2.get("args"));
        assertEquals(null, aPlaceholder3.get("args"));
    }

    @Test
    public void pipeTest() {
        File inFile = Mockito.mock(File.class);
        File ctrlFile = Mockito.mock(File.class);
        File outFile = Mockito.mock(File.class);
        Pipe inPipe = Mockito.mock(Pipe.class);
        Pipe ctrlPipe = Mockito.mock(Pipe.class);
        Pipe outPipe = Mockito.mock(Pipe.class);
        when(inPipe.resolve()).thenReturn(inFile);
        when(ctrlPipe.resolve()).thenReturn(ctrlFile);
        when(outPipe.resolve()).thenReturn(outFile);

        when(inFile.getAbsolutePath()).thenReturn("in");
        when(ctrlFile.getAbsolutePath()).thenReturn("ctrl");
        when(outFile.getAbsolutePath()).thenReturn("out");

        SequenceStep sequenceStep = Mockito.mock(SequenceStep.class);
        when(sequenceStep.getInPipe()).thenReturn(null);
        when(sequenceStep.getCtrlPipe()).thenReturn(ctrlPipe);
        when(sequenceStep.getOutPipe()).thenReturn(null);

        IPlaceholder pPlaceholder = new PipePlaceholder(sequenceStep, false);
        assertEquals(null, pPlaceholder.get("in"));
        assertEquals("ctrl", pPlaceholder.get("ctrl"));
        assertEquals(null, pPlaceholder.get("out"));
        assertEquals("ctrl", pPlaceholder.get("pipes"));

        SequenceStep sequenceStep2 = Mockito.mock(SequenceStep.class);
        when(sequenceStep2.getInPipe()).thenReturn(inPipe);
        when(sequenceStep2.getCtrlPipe()).thenReturn(ctrlPipe);
        when(sequenceStep2.getOutPipe()).thenReturn(null);

        IPlaceholder pPlaceholder2 = new PipePlaceholder(sequenceStep2, false);
        assertEquals("in", pPlaceholder2.get("in"));
        assertEquals("ctrl", pPlaceholder2.get("ctrl"));
        assertEquals(null, pPlaceholder2.get("out"));
        assertEquals("in ctrl", pPlaceholder2.get("pipes"));

        IPlaceholder pPlaceholder3 = new PipePlaceholder(sequenceStep2, true);
        assertEquals("in", pPlaceholder3.get("in"));
        assertEquals("ctrl", pPlaceholder3.get("ctrl"));
        assertEquals(null, pPlaceholder3.get("out"));
        assertEquals("in ctrl", pPlaceholder3.get("pipes"));


        sequenceStep2 = Mockito.mock(SequenceStep.class);
        when(sequenceStep2.getInPipe()).thenReturn(inPipe);
        when(sequenceStep2.getCtrlPipe()).thenReturn(ctrlPipe);
        when(sequenceStep2.getOutPipe()).thenReturn(outPipe);

        IPlaceholder pPlaceholder4 = new PipePlaceholder(sequenceStep2, true);
        assertEquals("in", pPlaceholder4.get("in"));
        assertEquals("ctrl", pPlaceholder4.get("ctrl"));
        assertEquals("out", pPlaceholder4.get("out"));
        assertEquals("in out ctrl", pPlaceholder4.get("pipes"));
    }

    @Test
    public void binaryTest() throws Exception {
        RoutineBinaryDTO binary1 = new RoutineBinaryDTO();
        binary1.setPrimary(false);
        binary1.setUrl("b1");

        RoutineBinaryDTO binary2 = new RoutineBinaryDTO();
        binary2.setPrimary(true);
        binary2.setUrl("b2");

        RoutineBinaryDTO binary3 = new RoutineBinaryDTO();
        binary3.setPrimary(false);
        binary3.setUrl("b3");

        RoutineBinaryDTO binary4 = new RoutineBinaryDTO();
        binary4.setPrimary(false);
        binary4.setUrl("b4");

        Set<RoutineBinaryDTO> binaries = new HashSet<>();
        binaries.add(binary1);
        binaries.add(binary3);
        binaries.add(binary4);

        Set<RoutineBinaryDTO> binaries2 = new HashSet<>();
        binaries2.add(binary1);
        binaries2.add(binary2);
        binaries2.add(binary3);
        binaries2.add(binary4);

        IPlaceholder bPlaceholder = new RoutineBinaryPlaceholder(binaries, false);
        IPlaceholder bPlaceholder2 = new RoutineBinaryPlaceholder(binaries2, false);

        assertEquals(null, bPlaceholder.get("rbp"));
        List<String> bResult = binaries.stream().map(RoutineBinaryDTO::getUrl).collect(Collectors.toList());
        assertEquals(String.join(" ", bResult), bPlaceholder.get("rbs"));
        assertEquals(bResult.get(0), bPlaceholder.get("rb"));
        assertEquals(bResult.get(1), bPlaceholder.get("rb"));
        assertEquals(bResult.get(2), bPlaceholder.get("rb"));
        assertEquals(bResult.get(0), bPlaceholder.get("rb"));
        assertEquals(bResult.get(0), bPlaceholder.get("rb0"));
        assertEquals(bResult.get(1), bPlaceholder.get("rb1"));
        assertEquals(bResult.get(2), bPlaceholder.get("rb2"));

        List<String> bResult2 = binaries2.stream().filter(binary -> !binary.isPrimary()).map(RoutineBinaryDTO::getUrl).collect(Collectors.toList());
        assertEquals(binary2.getUrl(), bPlaceholder2.get("rbp"));
        assertEquals(String.join(" ", bResult2), bPlaceholder2.get("rbs"));
        assertEquals(bResult2.get(0), bPlaceholder2.get("rb"));
        assertEquals(bResult2.get(1), bPlaceholder2.get("rb"));
        assertEquals(bResult2.get(2), bPlaceholder2.get("rb"));
        assertEquals(bResult2.get(0), bPlaceholder2.get("rb"));
        assertEquals(bResult2.get(0), bPlaceholder2.get("rb0"));
        assertEquals(bResult2.get(1), bPlaceholder2.get("rb1"));
        assertEquals(bResult2.get(2), bPlaceholder2.get("rb2"));
    }
}
