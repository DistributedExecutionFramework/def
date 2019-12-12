package at.enfilo.def.worker.queue;

import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.QueueNotExistsException;
import at.enfilo.def.node.api.QueueNotReleasedException;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.server.Worker;
import at.enfilo.def.worker.util.WorkerConfiguration;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Wrapper for a TaskQueues which are ordered (priority).
 */
public class QueuePriorityWrapper implements ITaskQueueObserver {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(QueuePriorityWrapper.class);
	private static final String QUEUE_ID_NOT_KNOWN = "Queue with id \"%s\" is not known.";

	private final Object queueLock;
	private final Semaphore waitLock;
	private final List<TaskQueue> orderedQueues;
	private final TimeoutMap<String, TaskQueue> queueMap;

	public QueuePriorityWrapper() {
		this.queueLock = new Object();
		this.waitLock = new Semaphore(0);
		this.orderedQueues = new LinkedList<>();
		WorkerConfiguration configuration = Worker.getInstance().getConfiguration();
		this.queueMap = new TimeoutMap<>(
				configuration.getQueueLifeTime(),
				TimeUnit.valueOf(configuration.getQueueLifeTimeUnit().name()),
				configuration.getQueueLifeTime() / 2,
				TimeUnit.valueOf(configuration.getQueueLifeTimeUnit().name()),
				(qId, tmp) -> queueTimeout(qId)
		);
	}

	public void addQueue(TaskQueue queue) {
		synchronized (queueLock) {
			orderedQueues.add(queue);
			queueMap.put(queue.getQueueId(), queue);
			queue.registerObserver(this);
		}
	}

	public TaskQueue getQueue(String qId) throws QueueNotExistsException {
		synchronized (queueLock) {
			if (queueMap.containsKey(qId)) {
				return queueMap.get(qId);
			} else {
				throw new QueueNotExistsException(String.format(
					"Queue with id \"%s\" not known.",
					qId
				));
			}
		}
	}

	public TaskDTO enqueue() throws InterruptedException, QueueNotReleasedException, IOException {
		while (true) {
			waitLock.acquire();
			synchronized (queueLock) {
				for (TaskQueue queue : orderedQueues) {
					if (queue.isReleased() && queue.size() > 0) {
						return queue.enqueue();
					}
				}
			}
		}
	}

	public int getNumberOfQueuedTasks() {
		synchronized (queueLock) {
			return queueMap.values().stream().mapToInt(TaskQueue::size).sum();
		}
	}

	public Set<String> getQueuedTasks() {
		synchronized (queueLock) {
			return queueMap.values().stream().map(
				TaskQueue::getQueuedTasks
			).flatMap(Collection::stream).collect(Collectors.toSet());
		}
	}

	public int getNumberOfQueues() {
		synchronized (queueLock) {
			return queueMap.size();
		}
	}

	public boolean containsQueue(String qId) {
		synchronized (queueLock) {
			return queueMap.containsKey(qId);
		}
	}

	public void delQueue(String qId) throws QueueNotExistsException {
		LOGGER.debug("Try to delete queue {}.", qId);
		synchronized (queueLock) {
			if (queueMap.containsKey(qId)) {
				TaskQueue queue = queueMap.remove(qId);
				orderedQueues.remove(queue);
				queue.clear();
				LOGGER.info("Queue {} deleted successfully.", qId);

			} else {
				throw new QueueNotExistsException(String.format(QUEUE_ID_NOT_KNOWN, qId));
			}
		}
	}

	public List<TaskQueue> getAllQueues() {
		List<TaskQueue> queues = new LinkedList<>();
		synchronized (queueLock) {
			queues.addAll(queueMap.values());
		}
		return queues;
	}

	private void queueTimeout(String qId) {
		LOGGER.debug("Queue {} timeout detected.");
		try {
			delQueue(qId);
		} catch (QueueNotExistsException e) {
			LOGGER.error("Error while delete queue {}.", qId, e);
		}
	}

	@Override
	public void notifyNewTask(TaskQueue queue) {
		waitLock.release();
	}

	@Override
	public void notifyQueueReleased(TaskQueue queue) {
		waitLock.release();
	}

}
