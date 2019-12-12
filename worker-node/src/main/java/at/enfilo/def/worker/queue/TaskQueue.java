package at.enfilo.def.worker.queue;

import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.QueueNotReleasedException;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.impl.WorkerServiceController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class TaskQueue {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(TaskQueue.class);

	private final String qId;
	private boolean released;
	private final BlockingDeque<String> queue;
	private final Object releaseLock;
	private final DTOCache<TaskDTO> taskCache;
	private final List<ITaskQueueObserver> observers;

	/**
	 * Create a TaskQueue for a given job.
	 * @param qId - id of this queue. can/should be job id
     */
	public TaskQueue(String qId) {
		this.qId = qId;
		this.queue = new LinkedBlockingDeque<>();
		this.releaseLock = new Object();
		this.observers = new LinkedList<>();
		this.taskCache = DTOCache.getInstance(WorkerServiceController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
	}


	public String getQueueId() {
		return qId;
	}


	public boolean isReleased() {
		return released;
	}


	/**
	 * Release queue. Enqueuing is allowed.
	 */
	public void release() {
		synchronized (releaseLock) {
			released = true;
		}
		observers.forEach(observer -> observer.notifyQueueReleased(this));
	}


	/**
	 * Pause queue. No enqueuing is allowed.
	 */
	public void pause() {
		synchronized (releaseLock) {
			released = false;
		}
	}


	/**
	 * Returns a list of all queued tasks.
	 *
	 * @return list of all tasks.
	 */
	public List<String> getQueuedTasks() {
		return new ArrayList<>(queue);
	}


	/**
	 * Remove the first task in queue and return it.
	 *
	 * @return first Task in queue.
	 * @throws QueueNotReleasedException if queue is on hold (pause)
	 */
	public TaskDTO enqueue() throws QueueNotReleasedException, InterruptedException, IOException {
		synchronized (releaseLock) {
			if (!released) {
				throw new QueueNotReleasedException();
			}
		}
		String tId = queue.take();
		try {
			return taskCache.fetch(tId);

		} catch (UnknownCacheObjectException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Next task from TaskQueue is not available at cache.", e);
			return null;
		}
	}


	/**
	 * Add a task ({@link TaskDTO}) to queue. (last position)
	 *
	 * @param task task to add
	 */
	public void queue(TaskDTO task) throws InterruptedException {
		taskCache.cache(task.getId(), task);
		queue.put(task.getId());
		observers.forEach(observer -> observer.notifyNewTask(this));
	}


	public int size() {
		return queue.size();
	}


	public void clear() {
		synchronized (releaseLock) {
			this.released = false;
		}
		this.queue.clear();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TaskQueue taskQueue = (TaskQueue) o;

		return qId.equals(taskQueue.qId);
	}

	@Override
	public int hashCode() {
		return qId.hashCode();
	}

	/**
	 * Returns QueueInfoDTO from this Queue.
	 * @return
	 */
	public QueueInfoDTO toQueueInfoDTO() {
		return new QueueInfoDTO(qId, released, queue.size());
	}

	/**
	 * Remove task from queue.
	 * @param tId - task id
	 */
	public TaskDTO remove(String tId) {
		Iterator<String> it = queue.iterator();
		while (it.hasNext()) {
			String t = it.next();
			if (t.equals(tId)) {
				it.remove();
				try {
					TaskDTO task = taskCache.fetch(tId);
					taskCache.remove(tId);
					return task;
				} catch (UnknownCacheObjectException | IOException e) {
					LOGGER.warn(DEFLoggerFactory.createTaskContext(tId), "Could not fetch task from cache.", e);
					return null;
				}
			}
		}
		return null;
	}

	public void registerObserver(ITaskQueueObserver observer) {
		observers.add(observer);
	}
}
