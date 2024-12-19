package ch.njol.skript.aliases;

public class InvalidMinecraftIdException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The Minecraft id that was deemed invalid
	 */
	private final String id;
	
	public InvalidMinecraftIdException(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
}
