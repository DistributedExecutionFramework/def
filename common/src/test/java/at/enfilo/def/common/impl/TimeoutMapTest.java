package at.enfilo.def.common.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TimeoutMapTest {

	private TimeoutMap<String, String> map;
	private long expirationTime;
	private TimeUnit expirationTimeUnit;
	private long cleanScheduleDelay;
	private TimeUnit cleanScheduleDelayUnit;
	private long delta;
	private long pollFrequence;

	@Before
	public void setUp() throws Exception {
		expirationTime = 1000;
		expirationTimeUnit = TimeUnit.MILLISECONDS;
		cleanScheduleDelay = 2000;
		cleanScheduleDelayUnit = TimeUnit.MILLISECONDS;
		delta = 200;
		pollFrequence = 100;

		map = new TimeoutMap<>(
			expirationTime,
			expirationTimeUnit,
			cleanScheduleDelay,
			cleanScheduleDelayUnit
		);
	}

	@Test
	public void putContainsGetRemove() throws Exception {
		Map<String, String> map = this.map;
		// Put
		for (int i = 0; i < 1000; i++) {
			String key = "k" + i;
			String value = "v" + i;
			map.put(key, value);
			assertTrue(map.containsKey(key));
			assertTrue(map.containsValue(value));
		}
		// Get and remove
		for (int i = 0; i < 1000; i++) {
			String key = "k" + i;
			String value = "v" + i;
			assertEquals(value, map.get(key));
			if (i % 2 == 0) {
				assertEquals(value, map.remove(key));
			} else {
				assertTrue(map.remove(key, value));
			}
		}
		assertTrue(map.isEmpty());
	}

	@Test
	public void specialPutAndGet() throws Exception {
		assertNull(map.get(UUID.randomUUID().toString()));

		String k1 = UUID.randomUUID().toString();
		String v1 = UUID.randomUUID().toString();
		map.put(k1, v1);
		String oldValue = map.put(k1, null);
		assertEquals(v1, oldValue);
		assertNull(map.get(k1));
	}


	@Test
	public void collections() throws Exception {
		for (int i = 0; i < 1000; i++) {
			String key = "k" + i;
			String value = "v" + i;
			map.put(key, value);
		}

		assertEquals(1000, map.entrySet().size());

		assertEquals(1000, map.keySet().size());
		assertTrue(map.keySet().contains("k0"));
		assertTrue(map.keySet().contains("k999"));

		assertEquals(1000, map.values().size());
		assertTrue(map.values().contains("v0"));
		assertTrue(map.values().contains("v999"));
	}


	@Test
	public void putAllAndClear() throws Exception {
		Map<String, String> map2 = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			String key = "k" + i;
			String value = "v" + i;
			map2.put(key, value);
		}

		map.putAll(map2);
		assertEquals(map2.size(), map.size());
		assertTrue(map.containsKey("k1"));
		assertTrue(map.containsValue("v88"));

		map.clear();
		assertTrue(map.isEmpty());
	}

	@Test
	public void cleanup() throws Exception {
		String key = UUID.randomUUID().toString();
		String value = UUID.randomUUID().toString();
		map.put(key, value);
		assertTrue(map.containsKey(key));

		await().atMost(10, TimeUnit.SECONDS).until(() -> 1 == map.getCleanupCounter());
		assertFalse(map.containsKey(key));
	}

	@Test
	public void touch() throws Exception {
		String k1 = UUID.randomUUID().toString();
		String v1 = UUID.randomUUID().toString();
		String k2 = UUID.randomUUID().toString();
		String v2 = UUID.randomUUID().toString();

		map.put(k1, v1);
		map.put(k2, v2);

		assertFalse(map.isExpired(k1));
		assertFalse(map.isExpired(k2));

		await()
				.between(expirationTime - delta, expirationTimeUnit, expirationTime + delta, expirationTimeUnit)
				.pollDelay(pollFrequence, TimeUnit.MILLISECONDS)
				.until(() -> map.isExpired(k1));
		await()
				.atMost(expirationTime + delta, expirationTimeUnit)
				.until(() -> map.isExpired(k2));
		assertTrue(map.isExpired(k1));
		assertTrue(map.isExpired(k2));
		map.touch(k1);
		assertFalse(map.isExpired(k1));
		assertTrue(map.isExpired(k2));
	}


	@Test
	@SuppressWarnings("unchecked")
	public void notification() throws Exception {
		BiConsumer<String, String> consumerMock = Mockito.mock(BiConsumer.class);
		map = new TimeoutMap<>(
				expirationTime,
				expirationTimeUnit,
				cleanScheduleDelay,
				cleanScheduleDelayUnit,
				consumerMock
		);

		String k1 = UUID.randomUUID().toString();
		String v1 = UUID.randomUUID().toString();
		String k2 = UUID.randomUUID().toString();
		String v2 = UUID.randomUUID().toString();
		map.put(k1, v1);
		map.put(k2, v2);
		await().until(() -> !map.containsKey(k1));
		await().until(() -> !map.containsKey(k2));

		verify(consumerMock).accept(k1, v1);
		verify(consumerMock).accept(k2, v2);
	}


	@Test
	@SuppressWarnings("unchecked")
	public void notificationAndTouch() throws Exception {
		BiConsumer<String, String> consumerMock = Mockito.mock(BiConsumer.class);
		map = new TimeoutMap<>(
				expirationTime,
				expirationTimeUnit,
				cleanScheduleDelay,
				cleanScheduleDelayUnit,
				consumerMock
		);

		String key = UUID.randomUUID().toString();
		String value = UUID.randomUUID().toString();

		map.put(key, value);
		for (int i = 0; i < expirationTime * 2; i++) {
			Thread.sleep(expirationTimeUnit.toMillis(1));
			map.touch(key);
		}
		verify(consumerMock, times(0)).accept(anyString(), anyString());
	}
}
