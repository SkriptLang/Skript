package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.YggdrasilSerializable;

public class GameruleValue<T> implements YggdrasilSerializable {
	private T gameruleValue;
	
	public GameruleValue(T gameruleValue) {
		this.gameruleValue = gameruleValue;
	}
	
	public T getGameruleValue() {
		return gameruleValue;
	}
	
	@Override
	public String toString() {
		return gameruleValue.toString();
	}
	
	@Override
	public boolean equals(@Nullable Object other) {
		if (!(other instanceof GameruleValue)) return false;
		return this.gameruleValue.equals(((GameruleValue) other).gameruleValue);
	}
}
