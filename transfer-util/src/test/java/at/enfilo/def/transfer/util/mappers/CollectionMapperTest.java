package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.Task;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.transfer.util.MapManager;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CollectionMapperTest {
	@Test
	public void emptyList() throws Exception {
		List<Task> tasks = new LinkedList<>();
		List<TaskDTO> mappedList = MapManager.map(tasks, TaskDTO.class).collect(Collectors.toList());
		assertTrue(mappedList.isEmpty());
	}

	@Test
	public void emptySet() throws Exception {
		Set<Task> tasks = new HashSet<>();
		Set<TaskDTO> mappedSet = MapManager.map(tasks, TaskDTO.class).collect(Collectors.toSet());
		assertTrue(mappedSet.isEmpty());
	}

	@Test
	public void normalList() throws Exception {
		int size = 10;
		List<Task> tasks = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			tasks.add(new Task());
		}
		List<TaskDTO> mappedList = MapManager.map(tasks, TaskDTO.class).collect(Collectors.toList());
		assertEquals(tasks.size(), mappedList.size());
		for (int i = 0; i < size; i++) {
			Task task = tasks.get(i);
			TaskDTO taskDTO = mappedList.get(i);
			assertNotNull(taskDTO);
			// Prove if order of new list is correct
			assertEquals(task.getId(), taskDTO.getId());
		}
	}

	@Test
	public void normalSet() throws Exception {
		int size = 10;
		Set<Task> tasks = new HashSet<>();
		for (int i = 0; i < size; i++) {
			tasks.add(new Task());
		}
		Set<TaskDTO> mappedSet = MapManager.map(tasks, TaskDTO.class).collect(Collectors.toSet());
		assertEquals(tasks.size(), mappedSet.size());
		for (TaskDTO task : mappedSet) {
			assertNotNull(task);
		}
	}
}
