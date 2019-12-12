package at.enfilo.def.worker.queue;

public interface ITaskQueueObserver {
	void notifyNewTask(TaskQueue queue);
	void notifyQueueReleased(TaskQueue queue);
}
