package at.enfilo.def.routine.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class BlockingIO<T extends Closeable> extends Thread {
	private File pipe;
	private Object waitLock = new Object();
	private T t;
	private Class<T> cls;
	private boolean running;

	public BlockingIO(File pipe, Class<T> cls) {
		this.pipe = pipe;
		this.cls = cls;
	}

	@Override
	public void run() {
		try {
			t = (T)cls.getConstructors()[0].newInstance(pipe);
			synchronized (waitLock) {
				running = true;
				waitLock.wait();
			}
			t.close();

		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		} catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		} finally {
			running = false;
		}
	}

	public void shutdown() {
		synchronized (waitLock) {
			waitLock.notify();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public T getIO() {
		return t;
	}
}

