package at.enfilo.def.node.queue;

import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.api.exception.QueueNotReleasedException;
import at.enfilo.def.node.util.NodeConfiguration;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper for a DEFQueues which are ordered (priority).
 */

public class QueuePriorityWrapper<T extends TBase> implements IQueueObserver<T> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(QueuePriorityWrapper.class);
	private static final String QUEUE_ID_NOT_KNOWN = "Queue with id \"%s\" is not known.";

	protected final Object queueLock;
	protected final Semaphore waitLock;
	private final TimeoutMap<String, Queue<T>> queueMap;
	private final List<Queue<T>> orderedQueues;

	public QueuePriorityWrapper(NodeConfiguration configuration) {
		this.queueLock = new Object();
		this.waitLock = new Semaphore(0);
		this.queueMap = new TimeoutMap<>(
				configuration.getQueueLifeTime(),
				TimeUnit.valueOf(configuration.getQueueLifeTimeUnit().name()),
				configuration.getQueueLifeTime() / 2,
				TimeUnit.valueOf(configuration.getQueueLifeTimeUnit().name()),
				(qId, tmp) -> queueTimeout(qId)
		);
		this.orderedQueues = new LinkedList<>();
	}

	public void addQueue(Queue<T> queue) {
		synchronized (queueLock) {
			queueMap.put(queue.getQueueId(), queue);
			orderedQueues.add(queue);
			queue.registerObserver(this);
		}
	}

	public Queue<T> getQueue(String qId) throws QueueNotExistsException {
		synchronized (queueLock) {
			if (queueMap.containsKey(qId)) {
				queueMap.touch(qId);
				return queueMap.get(qId);
			} else {
				throw new QueueNotExistsException(String.format(
						QUEUE_ID_NOT_KNOWN,
						qId
				));
			}
		}
	}

	public int getNumberOfQueuedElements() {
		synchronized (queueLock) {
			return queueMap.values().stream().mapToInt(Queue::size).sum();
		}
	}

	public int getNumberOfQueues() {
		synchronized (queueLock) {
			return queueMap.size();
		}
	}

	public boolean containsQueue(String qId) {
		synchronized (queueLock) {
			if (queueMap.containsKey(qId)) {
				queueMap.touch(qId);
				return true;
			}
			return false;
		}
	}

	public void deleteQueue(String qId) throws QueueNotExistsException {
		LOGGER.debug("Trying to delete queue {}.", qId);
		synchronized (queueLock) {
			if (queueMap.containsKey(qId)) {
				Queue<T> queue = queueMap.remove(qId);
				orderedQueues.remove(queue);
				queue.clear();
				LOGGER.info("Queue {} deleted successfully.", qId);
			} else {
				throw new QueueNotExistsException(String.format(QUEUE_ID_NOT_KNOWN, qId));
			}
		}
	}

	public List<Queue<T>> getAllQueues() {
		List<Queue<T>> queues;
		synchronized (queueLock) {
			queues = new LinkedList<>(orderedQueues);
		}
		return queues;
	}

	protected void queueTimeout(String qId) {
		LOGGER.debug("Queue {} timeout detected.", qId);
		try {
			deleteQueue(qId);
		} catch (QueueNotExistsException e) {
			LOGGER.error("Error while deleting queue {}.", qId, e);
		}
	}

	public T enqueue() throws InterruptedException, QueueNotReleasedException, IOException {
		while (true) {
			synchronized (queueLock) {
				for (Queue<T> queue : orderedQueues) {
					if (queue.isReleased() && queue.hasElements()) {
						queueMap.touch(queue.getQueueId());
						return queue.enqueue();
					}
				}
			}
			waitLock.acquire();
		}
	}

	@Override
	public void notifyQueueReleased(Queue<T> queue) {
		queueMap.touch(queue.getQueueId());
		if (waitLock.availablePermits() <= 0) {
			waitLock.release();
		}
	}

	@Override
	public void notifyQueuePaused(Queue<T> queue) {
		queueMap.touch(queue.getQueueId());
	}

	@Override
	public void notifyNewElement(Queue<T> queue) {
		queueMap.touch(queue.getQueueId());
		if (waitLock.availablePermits() <= 0) {
			waitLock.release();
		}
	}
}

