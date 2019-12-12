package at.enfilo.def.node.impl;


import at.enfilo.def.transfer.dto.ExecutionState;

public interface IStateChangeListener {
	void notifyStateChanged(String eId, ExecutionState oldState, ExecutionState newState);
}
