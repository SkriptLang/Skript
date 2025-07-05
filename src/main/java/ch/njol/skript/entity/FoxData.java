package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Fox.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FoxData extends EntityData<Fox> {
	
	private static final EntityPatterns<Type> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"fox", null},
		{"red fox", Type.RED},
		{"snow fox", Type.SNOW}
	});
	private static final Type[] TYPES = Type.values();

	static {
		EntityData.register(FoxData.class, "fox", Fox.class, 0, PATTERNS.getPatterns());
	}

	private @Nullable Type type = null;
	
	public FoxData() {}
	
	public FoxData(@Nullable Type type) {
		this.type = type;
		super.dataCodeName = PATTERNS.getMatchedPatterns(type)[0];
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		type = PATTERNS.getInfo(matchedCodeName);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Fox> entityClass, @Nullable Fox fox) {
		if (fox != null) {
			type = fox.getFoxType();
			super.dataCodeName = PATTERNS.getMatchedPatterns(type)[0];
		}
		return true;
	}
	
	@Override
	public void set(Fox fox) {
		Type type = this.type;
		if (type == null)
			type = CollectionUtils.getRandom(TYPES);
		assert type != null;
		fox.setFoxType(type);
	}
	
	@Override
	protected boolean match(Fox fox) {
		return type == null || type == fox.getFoxType();
	}
	
	@Override
	public Class<? extends Fox> getType() {
		return Fox.class;
	}
	
	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new FoxData();
	}
	
	@Override
	protected int hashCode_i() {
		return type != null ? Objects.hashCode(type) : 0;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof FoxData other))
			return false;
		return type == other.type;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof FoxData other))
			return false;
		return type == null || type == other.type;
	}

}
