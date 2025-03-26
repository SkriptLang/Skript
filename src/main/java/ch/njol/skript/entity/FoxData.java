package ch.njol.skript.entity;

import org.bukkit.entity.Fox;
import org.bukkit.entity.Fox.Type;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class FoxData extends EntityData<Fox> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Fox"))
			EntityData.register(FoxData.class, "fox", Fox.class, 1,
					"fox", "red fox", "snow fox");
	}
	
	@Nullable
	private Type type = null;
	
	public FoxData() {}
	
	public FoxData(@Nullable Type type) {
		this.type = type;
		super.matchedPattern = type == Type.SNOW ? 2 : 1;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0)
			type = Type.values()[matchedPattern - 1];
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Fox> c, @Nullable Fox fox) {
		if (fox != null)
			type = fox.getFoxType();
		return true;
	}
	
	@Override
	public void set(Fox entity) {
		if (type != null)
			entity.setFoxType(type);
	}
	
	@Override
	protected boolean match(Fox entity) {
		return type == null || type == entity.getFoxType();
	}
	
	@Override
	public Class<? extends Fox> getType() {
		return Fox.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new FoxData(type);
	}
	
	@Override
	protected int hashCode_i() {
		return type != null ? type.hashCode() : 0;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof FoxData))
			return false;
		return type == ((FoxData) data).type;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof FoxData))
			return false;
		return type == null || type == ((FoxData) data).type;
	}
}
