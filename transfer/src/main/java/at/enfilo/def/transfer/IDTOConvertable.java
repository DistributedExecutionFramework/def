package at.enfilo.def.transfer;

import org.apache.thrift.TBase;

public interface IDTOConvertable<T extends TBase> {
	T toDTO();
}
