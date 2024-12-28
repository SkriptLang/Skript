package ch.njol.skript.lang.function;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Contains a set of functions.
 */
public class Namespace {
	
	/**
	 * Origin of functions in namespace.
	 */
	public enum Origin {
		/**
		 * Functions implemented in Java.
		 */
		JAVA,
		
		/**
		 * Script functions.
		 */
		SCRIPT
	}
	
	/**
	 * Key to a namespace.
	 */
	public static class Key {
		
		private final Origin origin;

		private final @Nullable String scriptName;

		public Key(Origin origin, @Nullable String scriptName) {
			super();
			this.origin = origin;
			this.scriptName = scriptName;
		}
		
		public Origin getOrigin() {
			return origin;
		}

		public @Nullable String getScriptName() {
			return scriptName;
		}

		@Override
		public int hashCode() {
			int result = origin.hashCode();
			result = 31 * result + (scriptName != null ? scriptName.hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object)
				return true;
			if (object == null || getClass() != object.getClass())
				return false;

			Key other = (Key) object;

			if (origin != other.origin)
				return false;
			return Objects.equals(scriptName, other.scriptName);
		}
	}

	/**
	 * The key used in the signature and function maps
	 */
	private static class Info {

		/**
		 * Name of the function
		 */
		private final String name;

		/**
		 * Whether the function is local
		 */
		private final boolean local;

		public Info(String name, boolean local) {
			this.name = name;
			this.local = local;
		}

		public String getName() {
			return name;
		}

		public boolean isLocal() {
			return local;
		}

		@Override
		public int hashCode() {
			int result = getName().hashCode();
			result = 31 * result + (isLocal() ? 1 : 0);
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Info))
				return false;

			Info info = (Info) o;

			if (isLocal() != info.isLocal())
				return false;
			return getName().equals(info.getName());
		}
	}
	
	/**
	 * Signatures of known functions.
	 */
	private final Map<Info, Signature<?>> signatures;

	/**
	 * Known functions. Populated as function bodies are loaded.
	 */
	private final Map<Info, Function<?>> functions;

	public Namespace() {
		this.signatures = new HashMap<>();
		this.functions = new HashMap<>();
	}

	public @Nullable Signature<?> getSignature(String name, boolean local) {
		return signatures.get(new Info(name, local));
	}

	public @Nullable Signature<?> getSignature(String name) {
		Signature<?> signature = getSignature(name, true);
		return signature == null ? getSignature(name, false) : signature;
	}

	public void addSignature(Signature<?> sign) {
		Info info = new Info(sign.getName(), sign.local);
		if (signatures.containsKey(info))
			throw new IllegalArgumentException("function name already used");
		signatures.put(info, sign);
	}

	public boolean removeSignature(Signature<?> sign) {
		Info info = new Info(sign.getName(), sign.local);
		if (signatures.get(info) != sign)
			return false;
		signatures.remove(info);
		return true;
	}
	
	@SuppressWarnings("null")
	public Collection<Signature<?>> getSignatures() {
		return signatures.values();
	}

	public @Nullable Function<?> getFunction(String name, boolean local) {
		return functions.get(new Info(name, local));
	}

	public @Nullable Function<?> getFunction(String name) {
		Function<?> function = getFunction(name, true);
		return function == null ? getFunction(name, false) : function;
	}

	public void addFunction(Function<?> func) {
		Info info = new Info(func.getName(), func.getSignature().local);
		assert signatures.containsKey(info) : "missing signature for function";
		functions.put(info, func);
	}

	@SuppressWarnings("null")
	public Collection<Function<?>> getFunctions() {
		return functions.values();
	}
}
