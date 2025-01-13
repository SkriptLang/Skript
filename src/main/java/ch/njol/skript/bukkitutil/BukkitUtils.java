package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class with methods pertaining to Bukkit API
 */
public class BukkitUtils {

	/**
	 * Check if a registry exists
	 *
	 * @param registry Registry to check for (Fully qualified name of registry)
	 * @return True if registry exists else false
	 */
	public static boolean registryExists(String registry) {
		return Skript.classExists("org.bukkit.Registry") && Skript.fieldExists(Registry.class, registry);
	}

	/**
	 * Get an instance of the {@link PotionEffectType} {@link Registry}
	 * <p>Paper/Bukkit have 2 different names for the same registry.</p>
	 *
	 * @return PotionEffectType Registry
	 */
	@SuppressWarnings("NullableProblems")
	public static @Nullable Registry<PotionEffectType> getPotionEffectTypeRegistry() {
		if (registryExists("MOB_EFFECT")) { // Paper (1.21.4)
			return Registry.MOB_EFFECT;
		} else if (registryExists("EFFECT")) { // Bukkit (1.21.x)
			return Registry.EFFECT;
		}
		return null;
	}

	/**
	 * @hidden
	 * @param keyedClassInfo ClassInfo to add usage to
	 */
	@ApiStatus.Internal
	public static void generateUsageForKeyedClassInfo(ClassInfo<Keyed> keyedClassInfo) {
		if (keyedClassInfo.getUsage() != null) {
			throw new IllegalStateException("Keyed class already has usage");
		}
		// Register usage later so we can capture ALL ClassInfos that have registered
		Bukkit.getScheduler().runTaskLater(Skript.getInstance(), () -> {
			List<String> infos = new ArrayList<>();
			for (ClassInfo<?> classInfo : Classes.getClassInfos()) {
				Class<?> c = classInfo.getC();
				String docName = classInfo.getDocName();
				if (Keyed.class.isAssignableFrom(c) && c != Keyed.class && docName != null && !docName.isEmpty()) {
					String line = String.format("<a href='#%s'>%s</a>", classInfo.getCodeName(), docName);
					infos.add(line);
				}
			}
			Collections.sort(infos);
			String joined = Joiner.on(", ").join(infos);
			keyedClassInfo.usage(joined);
		}, 1);
	}

}
