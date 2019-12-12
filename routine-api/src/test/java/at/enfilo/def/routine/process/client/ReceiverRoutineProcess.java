package at.enfilo.def.routine.process.client;

import at.enfilo.def.routine.process.ReceiverRoutineMock;
import at.enfilo.def.transfer.dto.ProgramDTO;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReceiverRoutineProcess {

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("1 argument needed: out_pipe");
        }
        String outPipe = args[0];

        List<Class<?>> toReceive = new LinkedList<>();
        toReceive.add(Integer.class);
        toReceive.add(ProgramDTO.class);
        ReceiverRoutineMock rr = new ReceiverRoutineMock(new File(outPipe), toReceive);

        try {
            rr.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        System.out.println("Receiving");
        List<Object> received = rr.getReceived();
        if (received.size() == 2) {
            if (!ClientRoutineProcessTest.PROGRAM.equals(received.get(1))) {
                System.out.println("Not equals");
                System.out.println("PROGRAM " + ClientRoutineProcessTest.PROGRAM);
                System.out.println("RECEIVED " + received.get(1));
                System.exit(2);
            }
        } else {
            System.exit(2);
        }
    }
}
