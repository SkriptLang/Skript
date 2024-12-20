package ch.njol.skript.entity;

import ch.njol.skript.registrations.Classes;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Cat;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

import java.util.Objects;

public class CatData extends EntityData<Cat> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Cat")) {
			EntityData.register(CatData.class, "cat", Cat.class, "cat");
			types = Iterators.toArray(Classes.getExactClassInfo(Cat.Type.class).getSupplier().get(), Cat.Type.class);
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private static Cat.Type[] types;
	
	private Cat.@Nullable Type race = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null)
			race = ((Literal<Cat.Type>) exprs[0]).getSingle();
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Cat> c, @Nullable Cat cat) {
		race = (cat == null) ? Cat.Type.TABBY : cat.getCatType();
		return true;
	}
	
	@Override
	public void set(Cat entity) {
		Cat.Type type = race != null ? race : CollectionUtils.getRandom(types);
		assert type != null;
		entity.setCatType(type);
	}
	
	@Override
	protected boolean match(Cat entity) {
		return race == null || entity.getCatType() == race;
	}
	
	@Override
	public Class<? extends Cat> getType() {
		return Cat.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new CatData();
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(race);
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		return data instanceof CatData ? race == ((CatData) data).race : false;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		return data instanceof CatData ? race == null || race == ((CatData) data).race : false;
	}
}
