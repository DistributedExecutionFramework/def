package at.enfilo.def.node.queue;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotReleasedException;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class Queue<T extends TBase> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Queue.class);

	private final String qId;
	private boolean released;
	private final BlockingQueue<String> queue;
	private final Object releaseLock;
	private final DTOCache<T> cache;
	private final List<IQueueObserver<T>> observers;

	/**
	 * Create a DEFQueue
	 *
	 * @param qId - id of this queue
	 */
	public Queue(
			String qId,
			String dtoCacheContext,
			Class<T> dtoClass
	) {
		this(qId, new LinkedBlockingDeque<>(), DTOCache.getInstance(dtoCacheContext, dtoClass));
	}

	/**
	 * Constructor for unit testing
	 */
	protected Queue(
			String qId,
			LinkedBlockingDeque<String> queue,
			DTOCache<T> cache
	) {
		this.qId = qId;
		this.queue = queue;
		this.releaseLock = new Object();
		this.cache = cache;
		this.observers = new LinkedList<>();
	}

	public String getQueueId() {
		return qId;
	}

	public boolean isReleased() {
		return released;
	}

	/**
	 * For unit testing
	 */
	void setReleased(boolean released) {
		this.released = released;
	}

	/**
	 * Release queue. Enqueuing is allowed.
	 */
	public void release() {
		synchronized (releaseLock) {
			released = true;
		}
		observers.forEach(o -> o.notifyQueueReleased(this));
	}

	/**
	 * Pause queue. No enqueuing is allowed.
	 */
	public void pause() {
		synchronized (releaseLock) {
			released = false;
		}
		observers.forEach(o -> o.notifyQueuePaused(this));
	}

	/**
	 * Returns a list of all queued elements.
	 *
	 * @return - list of all elements
	 */
	public List<String> getQueuedElements() {
		return new ArrayList<>(queue);
	}

	/**
	 * Removes the first element in queue and returns it.
	 *
	 * @return first element in queue
	 * @throws QueueNotReleasedException if queue is on hold (pause)
	 */
	public T enqueue() throws QueueNotReleasedException, InterruptedException, IOException {
		synchronized (releaseLock) {
			if (!released) {
				throw new QueueNotReleasedException();
			}
		}
		String eId = queue.take();
		try {
			return cache.fetch(eId);
		} catch (UnknownCacheObjectException e) {
			LOGGER.error(getLoggingContext(eId), "Next element from DEFQueue is not available at cache.", e);
			return null;
		}
	}



	/**
	 * Add a element to queue (last position).
	 *
	 * @param element - element to add
	 */
	public void queue(T element) throws InterruptedException {
		String eId = getElementId(element);
		cache.cache(eId, element);
		queue.put(eId);
		observers.forEach(o -> o.notifyNewElement(this));
	}

	/**
	 * Returns the current queue size (elements in queue)
	 *
	 * @return size of queue
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * Clears queue and pause it.
	 */
	public void clear() {
		pause();
		queue.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		Queue queue = (Queue) obj;
		return this.qId.equals(queue.qId);
	}

	@Override
	public int hashCode() {
		return qId.hashCode();
	}

	/**
	 * Returns QueueInfoDTO form this Queue.
	 *
	 * @return queue info
	 */
	public QueueInfoDTO toQueueInfoDTO() {
		return new QueueInfoDTO(qId, released, queue.size());
	}

	/**
	 * Remove element from queue.
	 *
	 * @param eId - element id
	 * @return - removed element
	 */
	public T remove(String eId) {
		Iterator<String> it = queue.iterator();
		while (it.hasNext()) {
			String t = it.next();
			if (t.equals(eId)) {
				it.remove();
				try {
					T element = cache.fetch(eId);
					cache.remove(eId);
					return element;
				} catch (UnknownCacheObjectException | IOException e) {
					LOGGER.warn(getLoggingContext(eId), "Could not fetch element from cache.", e);
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Register an observer on this queue.
	 *
	 * @param observer - observer to register
	 */
	public void registerObserver(IQueueObserver<T> observer) {
		observers.add(observer);
		if (isReleased() && hasElements()) {
			observer.notifyNewElement(this);
		}
	}

	/**
	 * Returns all registered observers.
	 */
	public List<IQueueObserver<T>> getObservers() {
		return observers;
	}

	/**
	 * Extract id from given element
	 * @param element - DTO
	 * @return id of DTO
	 */
	protected abstract String getElementId(T element);

	/**
	 * Returns logging context for given element id.
	 * @param eId - element id
	 * @return logging context
	 */
	protected abstract Set<ITuple<ContextIndicator, ?>> getLoggingContext(String eId);

	/**
	 * Returns true if there are elements in the queue.
	 * @return false if queue is empty
	 */
	public boolean hasElements() {
		return !queue.isEmpty();
	}
}
