package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Cat.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CatData extends EntityData<Cat> {

	private static final Type[] TYPES;

	static {
		EntityData.register(CatData.class, "cat", Cat.class, "cat");
		TYPES = Iterators.toArray(Classes.getExactClassInfo(Type.class).getSupplier().get(), Type.class);
	}

	private @Nullable Type type = null;

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			//noinspection unchecked
			type = ((Literal<Type>) exprs[0]).getSingle();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Cat> entityClass, @Nullable Cat cat) {
		if (cat != null)
			type = cat.getCatType();
		return true;
	}
	
	@Override
	public void set(Cat cat) {
		Type type = this.type;
		if (type == null)
			type = CollectionUtils.getRandom(TYPES);
		assert type != null;
		cat.setCatType(type);
	}
	
	@Override
	protected boolean match(Cat cat) {
		return type == null || cat.getCatType() == type;
	}
	
	@Override
	public Class<? extends Cat> getType() {
		return Cat.class;
	}
	
	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new CatData();
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(type);
	}
	
	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof CatData other))
			return false;
		return type == other.type;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CatData other))
			return false;
		return type == null || type == other.type;
	}

}
