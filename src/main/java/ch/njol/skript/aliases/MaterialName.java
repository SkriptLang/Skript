package ch.njol.skript.aliases;

import org.bukkit.Material;

final class MaterialName {
	String singular;
	String plural;
	int gender = 0;
	Material id;
	
	public MaterialName(final Material id, final String singular, final String plural, final int gender) {
		this.id = id;
		this.singular = singular;
		this.plural = plural;
		this.gender = gender;
	}
	
	public String toString(boolean p) {
		return p ? plural : singular;
	}
	
	public String getDebugName(boolean p) {
		// TODO more useful debug name wouldn't hurt
		return p ? plural : singular;
	}
}
