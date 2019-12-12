package at.enfilo.def.node.util;

import at.enfilo.def.routine.api.Result;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;

public class ResultUtil {
	public static final TFieldIdEnum ID_FIELD;
	private static final TDeserializer DESERIALIZER;

	static {
		ID_FIELD = new TFieldIdEnum() {
			@Override
			public short getThriftFieldId() {
				return 1;
			}

			@Override
			public String getFieldName() {
				return "_id";
			}
		};

		DESERIALIZER = new TDeserializer();
	}

	public static String extractDataTypeId(Result result) {
		return extractDataTypeId(result, DESERIALIZER);
	}

	public static String extractDataTypeId(Result result, TDeserializer deserializer) {
		try {
			return deserializer.partialDeserializeString(result.getData(), ID_FIELD);
		} catch (TException e) {
			return null;
		}
	}
}
