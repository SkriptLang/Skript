package ch.njol.skript.registrations;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;

// When using these fields, be aware all ClassInfo's must be registered!
public class DefaultClasses {

	public static ClassInfo<Object> OBJECT = getClassInfo(Object.class);

	// Java
	public static ClassInfo<Number> NUMBER = getClassInfo(Number.class);
	public static ClassInfo<Long> LONG = getClassInfo(Long.class);
	public static ClassInfo<Boolean> BOOLEAN = getClassInfo(Boolean.class);
	public static ClassInfo<String> STRING = getClassInfo(String.class);

	// Bukkit
	public static ClassInfo<OfflinePlayer> OFFLINE_PLAYER = getClassInfo(OfflinePlayer.class);
	public static ClassInfo<Location> LOCATION = getClassInfo(Location.class);
	public static ClassInfo<Vector> VECTOR = getClassInfo(Vector.class);
	public static ClassInfo<Player> PLAYER = getClassInfo(Player.class);
	public static ClassInfo<World> WORLD = getClassInfo(World.class);

	// Skript
	public static ClassInfo<Color> COLOR = getClassInfo(Color.class);
	public static ClassInfo<Date> DATE = getClassInfo(Date.class);
	public static ClassInfo<Timespan> TIMESPAN = getClassInfo(Timespan.class);

	@NotNull
	private static <T> ClassInfo<T> getClassInfo(Class<T> type) {
		//noinspection ConstantConditions
		ClassInfo<T> classInfo = Classes.getExactClassInfo(type);
		if (classInfo == null)
			throw new NullPointerException();
		return classInfo;
	}

}
