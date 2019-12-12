package at.enfilo.def.library.api.client.factory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ThreadSafeFuture<T> implements Future<T> {
	private final Object lock;
	private T result;
	private Future<T> future;

	public ThreadSafeFuture(Future<T> future) {
		this.future = future;
		this.lock = new Object();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		synchronized (lock) {
			if (result != null) {
				return true;
			}
			return future.isDone();
		}
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		synchronized (lock) {
			if (result == null) {
				result = future.get();
				future = null;
			}
		}
		return result;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		synchronized (lock) {
			if (result == null) {
				result = future.get(timeout, unit);
				future = null;
			}
		}
		return result;
	}
}
