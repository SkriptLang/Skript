package ch.njol.util;

import org.jetbrains.annotations.Nullable;

public class NotifyingReference<V> {

	@Nullable
	private volatile V value;
	private final boolean notifyAll;

	public NotifyingReference(@Nullable V value, boolean notifyAll) {
		this.value = value;
		this.notifyAll = notifyAll;
	}

	public NotifyingReference(@Nullable V value) {
		this.value = value;
		this.notifyAll = true;
	}

	public NotifyingReference() {
		this.value = null;
		this.notifyAll = true;
	}

	@Nullable
	public synchronized V get() {
		return this.value;
	}

	public synchronized void set(@Nullable V newValue) {
		this.value = newValue;
		if (this.notifyAll) {
			notifyAll();
		} else {
			notify();
		}
	}

}
