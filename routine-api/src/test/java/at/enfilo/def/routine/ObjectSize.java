package at.enfilo.def.routine;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

public class ObjectSize {
	private final static TSerializer SERIALIZER = new TSerializer();

	public static int getSize(TBase obj) throws TException {
		return SERIALIZER.serialize(obj).length;
	}

	public static boolean proofSize(TBase obj, int size) throws TException {
		return (SERIALIZER.serialize(obj).length == size);
	}

}
