package ch.njol.yggdrasil;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class SimpleClassResolver implements ClassResolver {
	
	private final BiMap<Class<?>, String> classes = HashBiMap.create();
	
	public void registerClass(Class<?> type, String id) {
		String oldId = classes.put(type, id);
		if (oldId != null && !oldId.equals(id))
			throw new YggdrasilException("Changed id of " + type + " from " + oldId + " to " + id);
	}
	
	@Override
	@Nullable
	public Class<?> getClass(String id) {
		return classes.inverse().get(id);
	}
	
	@Override
	@Nullable
	public String getID(Class<?> type) {
		return classes.get(type);
	}
	
}
