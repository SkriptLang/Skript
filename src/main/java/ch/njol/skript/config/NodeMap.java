package ch.njol.skript.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Map for fast access of entry nodes and section nodes within section nodes.
 * 
 * @author Peter Güttinger
 */
public class NodeMap {
	
	private final Map<String, Node> map = new HashMap<>();
	
	public static boolean inMap(final Node n) {
		return n instanceof EntryNode || n instanceof SectionNode;
	}
	
	private static String getKey(final Node n) {
		final String key = n.getKey();
		if (key == null) {
			assert false : n;
			return "";
		}
		return "" + key.toLowerCase(Locale.ENGLISH);
	}
	
	private static String getKey(final String key) {
		return "" + key.toLowerCase(Locale.ENGLISH);
	}
	
	public void put(final Node n) {
		if (!inMap(n))
			return;
		map.put(getKey(n), n);
	}
	
	@Nullable
	public Node remove(final Node n) {
		return remove(getKey(n));
	}
	
	@Nullable
	public Node remove(final @Nullable String key) {
		if (key == null)
			return null;
		return map.remove(getKey(key));
	}
	
	@Nullable
	public Node get(final @Nullable String key) {
		if (key == null)
			return null;
		return map.get(getKey(key));
	}
	
}
