package at.enfilo.def.transfer.util.mappers;

import at.enfilo.def.domain.entity.Feature;
import at.enfilo.def.transfer.dto.FeatureDTO;
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
		List<Feature> tasks = new LinkedList<>();
		List<FeatureDTO> mappedList = MapManager.map(tasks, FeatureDTO.class).collect(Collectors.toList());
		assertTrue(mappedList.isEmpty());
	}

	@Test
	public void emptySet() throws Exception {
		Set<Feature> tasks = new HashSet<>();
		Set<FeatureDTO> mappedSet = MapManager.map(tasks, FeatureDTO.class).collect(Collectors.toSet());
		assertTrue(mappedSet.isEmpty());
	}

	@Test
	public void normalList() throws Exception {
		int size = 10;
		List<Feature> tasks = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			tasks.add(new Feature());
		}
		List<FeatureDTO> mappedList = MapManager.map(tasks, FeatureDTO.class).collect(Collectors.toList());
		assertEquals(tasks.size(), mappedList.size());
		for (int i = 0; i < size; i++) {
			Feature task = tasks.get(i);
			FeatureDTO taskDTO = mappedList.get(i);
			assertNotNull(taskDTO);
			// Prove if order of new list is correct
			assertEquals(task.getId(), taskDTO.getId());
		}
	}

	@Test
	public void normalSet() throws Exception {
		int size = 10;
		Set<Feature> tasks = new HashSet<>();
		for (int i = 0; i < size; i++) {
			tasks.add(new Feature());
		}
		Set<FeatureDTO> mappedSet = MapManager.map(tasks, FeatureDTO.class).collect(Collectors.toSet());
		assertEquals(tasks.size(), mappedSet.size());
		for (FeatureDTO task : mappedSet) {
			assertNotNull(task);
		}
	}
}
