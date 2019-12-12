package at.enfilo.def.dto.cache;

import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class DTOCacheTest {
	private DTOCache<TaskDTO> cache;

	@Before
	public void setUp() throws Exception {
		cache = DTOCache.getInstance("unit-test", TaskDTO.class);
	}

	@Test
	public void cacheAndFetch() throws Exception {
		TaskDTO origin = createTask();
		cache.cache(origin.getId(), origin);

		TaskDTO cached = cache.fetch(origin.getId());
		assertEquals(origin, cached);
	}

	@Test
	public void cacheAndFetchWithWait() throws Exception {
		TaskDTO origin = createTask();
		cache.cache(origin.getId(), origin);

		Thread.sleep(5 * 1000);

		TaskDTO cached = cache.fetch(origin.getId());
		assertEquals(origin, cached);
	}

	@Test(expected = UnknownCacheObjectException.class)
	public void fetchUnknownTask() throws Exception {
		cache.fetch(UUID.randomUUID().toString());
	}

	@Test(expected = UnknownCacheObjectException.class)
	public void remove() throws Exception {
		TaskDTO task = createTask();
		cache.cache(task.getId(), task);
		cache.remove(task.getId());
		cache.fetch(task.getId());
	}

	//@Test
	public void stressTests() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(20);

		List<String> tIds = new LinkedList<>();
		Random rnd = new Random();
		for (int i = 0; i < 20; i++) {
			TaskDTO task = createTask();
			if (i == 0) {
				task.setId("0");
			}
			byte[] buf = new byte[1000000];
			rnd.nextBytes(buf);
			task.setState(ExecutionState.SCHEDULED);
			task.getOutParameters().get(0).setData(buf);
			cache.cache(task.getId(), task);
			tIds.add(task.getId());
		}

		Future f = null;
		for (String tId: tIds) {
			f = pool.submit(() -> checkAndModifyTask(tId, rnd));
		}

		f.get();
	}

	private void checkAndModifyTask(String tId, Random rnd) throws RuntimeException {
		ExecutionState start = ExecutionState.SCHEDULED;
		while (true) {
			ExecutionState next = ExecutionState.values()[(start.ordinal() + 1) % ExecutionState.values().length];
			try {
				int sleep = 3000 + rnd.nextInt(200);
				System.out.println(tId + " - fetch");
				TaskDTO task = cache.fetch(tId);
				task.setState(next);
				System.out.println(tId + " - cache & wait " + sleep);
				cache.cache(tId, task);

				Thread.sleep(sleep);

				System.out.println(tId + " - fetch & compare");
				task = cache.fetch(tId);
				if (next != task.getState()) {
					System.out.println(tId + " not matching");
					throw new RuntimeException("not matching");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			start = next;
		}
	}

	private TaskDTO createTask() {
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setJobId(UUID.randomUUID().toString());
		task.setProgramId(UUID.randomUUID().toString());
		task.setCreateTime(System.currentTimeMillis());
		task.setFinishTime(System.currentTimeMillis());
		task.setState(ExecutionState.RUN);
		task.setObjectiveRoutineId(UUID.randomUUID().toString());
		task.setMapRoutineId(UUID.randomUUID().toString());
		task.setInParameters(new HashMap<>());
		task.getInParameters().put(UUID.randomUUID().toString(), new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		task.getInParameters().put(UUID.randomUUID().toString(), new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		task.setOutParameters(new LinkedList<>());
		task.getOutParameters().add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		return task;
	}
}
