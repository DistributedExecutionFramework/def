package at.enfilo.def.common.impl;

import at.enfilo.def.common.api.ITimeoutMap;
import at.enfilo.def.common.util.DaemonThreadFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * TimeoutMap - a map which has values with a expiration time.
 * If a value is expired, a cleanup thread will remove this value from map.
 *
 * @param <K> - KEY
 * @param <V> - VALUE
 */
public class TimeoutMap<K, V> implements ITimeoutMap<K, V> {

	private class TimeCapsule<U> {

		private U value;
		private Instant lastTouchTime;

		public TimeCapsule(U value) {
			this.value = value;
			this.lastTouchTime = Instant.now();
		}

		public U getValue() {
			return value;
		}

		public void setValue(U value) {
			this.value = value;
			this.setLastTouchTime();
		}

		public Instant getLastTouchTime() {
			return lastTouchTime;
		}

		public void setLastTouchTime() {
			setLastTouchTime(Instant.now());
		}

		public void setLastTouchTime(Instant lastTouchTime) {
			this.lastTouchTime = lastTouchTime;
		}

		public void setLastTouchTime(long epochSecond, int nanoAdjustment) {
			this.lastTouchTime = Instant.ofEpochSecond(epochSecond, nanoAdjustment);
		}

		public boolean isExpired() {
			// Distinguishing time elapsed since the last capsule touch().
			Duration duration = Duration.between(lastTouchTime, Instant.now());
			return duration.compareTo(expirationDuration) > 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TimeCapsule<?> that = (TimeCapsule<?>) o;

			if (value != null ? !value.equals(that.value) : that.value != null) return false;
			return lastTouchTime != null ? lastTouchTime.equals(that.lastTouchTime) : that.lastTouchTime == null;
		}

		@Override
		public int hashCode() {
			int result = value != null ? value.hashCode() : 0;
			result = 31 * result + (lastTouchTime != null ? lastTouchTime.hashCode() : 0);
			return result;
		}
	}

	private static final int NUMBER_OF_CLEANUP_THREADS = 1;

	private final Map<K, TimeCapsule<V>> backgroundMap;
	private final BiConsumer<K, V> notifyOnExpiration;
	private final Duration expirationDuration;
	private final Object lock;

	private AtomicLong cleanupCounter; // Used for unit testing

	public TimeoutMap(
			long expirationTime,
			TimeUnit expirationTimeUnit,
			long cleanScheduleDelay,
			TimeUnit cleanScheduleTimeUnit
	) {
		this(expirationTime, expirationTimeUnit, cleanScheduleDelay, cleanScheduleTimeUnit, null);
	}

	public TimeoutMap(
			long expirationTime,
			TimeUnit expirationTimeUnit,
			long cleanScheduleDelay,
			TimeUnit cleanScheduleTimeUnit,
			BiConsumer<K, V> notifyOnExpiration
	) {
		this.lock = new Object();
		this.backgroundMap = new HashMap<>();
		this.expirationDuration = Duration.ofMillis(expirationTimeUnit.toMillis(expirationTime));

		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
				NUMBER_OF_CLEANUP_THREADS,
				r -> DaemonThreadFactory.newDaemonThread(r, "TimeoutMapCleanup")
		);

		cleanupCounter = new AtomicLong(0);

		scheduledExecutorService.scheduleAtFixedRate(
				this::doTimeoutClean,
				cleanScheduleDelay,
				cleanScheduleDelay,
				cleanScheduleTimeUnit
		);

		this.notifyOnExpiration = notifyOnExpiration;
	}


	@Override
	public int size() {
		synchronized (lock) {
			return backgroundMap.size();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (lock) {
			return backgroundMap.isEmpty();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		synchronized (lock) {
			return backgroundMap.containsKey(key);
		}
	}

	@Override
	public boolean containsValue(Object value) {
		synchronized (lock) {
			return backgroundMap.values().stream().map(TimeCapsule::getValue).anyMatch(v -> v.equals(value));
		}
	}

	@Override
	public V get(Object key) {
		synchronized (lock) {
			if (backgroundMap.containsKey(key)) {
				return backgroundMap.get(key).getValue();
			}
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		V rv = null;
		synchronized (lock) {
			TimeCapsule<V> timeCapsule = backgroundMap.get(key);
			if (timeCapsule == null) {
				timeCapsule = new TimeCapsule<>(value);
			} else {
				rv = timeCapsule.getValue();
				timeCapsule.setValue(value);
			}
			backgroundMap.put(key, timeCapsule);
		}
		return rv;
	}

	@Override
	public V remove(Object key) {
		synchronized (lock) {
			TimeCapsule<V> timeCapsule = backgroundMap.remove(key);
			return timeCapsule != null ? timeCapsule.getValue() : null;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> donorMap) {
		donorMap.forEach(this::put);
	}

	@Override
	public void clear() {
		synchronized (lock) {
			backgroundMap.clear();
		}
	}

	@Override
	public Set<K> keySet() {
		synchronized (lock) {
			return backgroundMap.keySet();
		}
	}

	@Override
	public Collection<V> values() {
		synchronized (lock) {
			return backgroundMap
					.values()
					.stream().map(TimeCapsule::getValue)
					.collect(Collectors.toList());
		}
	}

	@Nonnull
	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> entrySet = new HashSet<>();
		synchronized (lock) {
			backgroundMap
					.entrySet()
					.forEach(
							entry -> entrySet.add(new AbstractMap.SimpleEntry<K, V>(entry.getKey(), entry.getValue().getValue()))
					);
		}
		return entrySet;
	}

	@Override
	public boolean remove(Object key, Object value) {
		synchronized (lock) {
			TimeCapsule<V> capsule = backgroundMap.get(key);
			if (capsule != null && capsule.getValue().equals(value)) {
				backgroundMap.remove(key);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean touch(K key) {
		TimeCapsule<V> capsule;
		synchronized (lock) {
			if ((capsule = backgroundMap.get(key)) != null) {
				capsule.setLastTouchTime(Instant.now());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isExpired(K key) {
		synchronized (lock) {
			if (backgroundMap.containsKey(key)) {
				return backgroundMap.get(key).isExpired();
			}
		}
		return false;
	}

	private void doTimeoutClean() {
		// Iterating threw backgroundMap and searching for expired capsules.
		// If expired capsule was found - remove entry from background map and call notify handler.
		Map<K, V> notifiers = new HashMap<>();
		synchronized (lock) {
			Iterator<Entry<K, TimeCapsule<V>>> iterator = backgroundMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<K, TimeCapsule<V>> entry = iterator.next();
				TimeCapsule<V> timeCapsule = entry.getValue();
				if (timeCapsule.isExpired()) {
					notifiers.put(entry.getKey(), timeCapsule.getValue());
				}
			}

			// Notify that values are expired
			if (notifyOnExpiration != null) {
				for (Entry<K, V> e: notifiers.entrySet()) {
					notifyOnExpiration.accept(e.getKey(), e.getValue());
				}
			}

			for (Entry<K, V> e: notifiers.entrySet()) {
				backgroundMap.remove(e.getKey());
			}
		}

		cleanupCounter.incrementAndGet(); // cleanup counter, used for unit tests
	}

	long getCleanupCounter() {
		return cleanupCounter.get();
	}
}
