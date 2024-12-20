package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Experience implements YggdrasilSerializable {
	
	private final int xp;
	
	public Experience() {
		xp = -1;
	}
	
	public Experience(final int xp) {
		this.xp = xp;
	}
	
	public int getXP() {
		return xp == -1 ? 1 : xp;
	}
	
	public int getInternalXP() {
		return xp;
	}
	
	@Override
	public String toString() {
		return xp == -1 ? "xp" : xp + " xp";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xp;
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Experience))
			return false;
		final Experience other = (Experience) obj;
		if (xp != other.xp)
			return false;
		return true;
	}
	
}
