package at.enfilo.def.worker.impl;


import at.enfilo.def.transfer.dto.ExecutionState;

interface ITaskStateChangeListener {
	void notifyStateChanged(String tId, ExecutionState oldState, ExecutionState newState);
}
