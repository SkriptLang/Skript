package org.skriptlang.skript.bukkit.spawners.util;

import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptMobSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;

import java.util.List;
import java.util.Locale;

/**
 * Represents the type of spawner data.
 */
public enum SpawnerDataType {

	MOB(SkriptMobSpawnerData.class),
	TRIAL(SkriptTrialSpawnerData.class),
	ANY(SkriptSpawnerData.class);

	/**
	 * Creates a SpawnerDataType from the given parse result tags.
	 * @param tags the tags from the parse result
	 * @return the corresponding SpawnerDataType
	 */
	public static SpawnerDataType fromTags(List<String> tags) {
		if (tags.contains("trial")) {
			return TRIAL;
		} else if (tags.contains("mob")) {
			return MOB;
		}
		return ANY;
	}

	private final Class<? extends SkriptSpawnerData> dataClass;

	SpawnerDataType(Class<? extends SkriptSpawnerData> dataClass) {
		this.dataClass = dataClass;
	}

	/**
	 * Gets the class of the SkriptSpawnerData associated with this type.
	 * @return the class of SkriptSpawnerData
	 */
	public Class<? extends SkriptSpawnerData> getDataClass() {
		return dataClass;
	}

	/**
	 * @return whether the type is {@link #MOB}
	 */
	public boolean isMob() {
		return this == MOB;
	}

	/**
	 * @return whether the type is {@link #TRIAL}
	 */
	public boolean isTrial() {
		return this == TRIAL;
	}

	/**
	 * @return whether the type is {@link #ANY}
	 */
	public boolean isAny() {
		return this == ANY;
	}

	/**
	 * Checks if the given spawner object matches this spawner data type.
	 * @param spawnerObject the spawner object to check
	 * @return true if the spawner object matches the type, false otherwise
	 */
	public boolean matches(Object spawnerObject) {
		if (isTrial()) {
			return SpawnerUtils.isTrialSpawner(spawnerObject);
		} else if (isMob()) {
			return SpawnerUtils.isMobSpawner(spawnerObject);
		}

		return SpawnerUtils.isMobSpawner(spawnerObject) || SpawnerUtils.isTrialSpawner(spawnerObject);
	}

	@Override
	public String toString() {
		if (isAny())
			return "";
		return name().toLowerCase(Locale.ENGLISH);
	}

}
