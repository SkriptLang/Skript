package ch.njol.yggdrasil;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Map.Entry;

@NotThreadSafe
public class SimpleClassResolver implements ClassResolver {

	private final BiMap<Class<?>, String> classes = HashBiMap.create();

	public void registerClass(Class<?> type, String id) {
		String oldId = classes.put(type, id);
		if (oldId != null && !oldId.equals(id))
			throw new YggdrasilException("Changed id of " + type + " from " + oldId + " to " + id);
	}

	@Override
	public @Nullable Class<?> getClass(String id) {
		return classes.inverse().get(id);
	}

	@Override
	public @Nullable String getID(Class<?> type) {
		if (classes.containsKey(type))
			return classes.get(type);
		Class<?> closestClass = null;
		String closestId = null;
		for (Entry<Class<?>, String> entry : classes.entrySet()) {
			Class<?> current = entry.getClass();
			if (current.isAssignableFrom(type) && (closestClass == null || closestClass.isAssignableFrom(current))) {
				closestClass = current;
				closestId = entry.getValue();
			}
		}
		return closestId;
	}
	
}
