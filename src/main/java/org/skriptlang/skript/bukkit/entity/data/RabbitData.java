package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class RabbitData extends EntityData<Rabbit> {

	private static final Type[] TYPES = Type.values();

	private static final EntityDataPatterns<Type> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "rabbit¦s @a", getPatterns("")),
		new PatternGroup<>(1, "white rabbit¦s @a", Type.WHITE, getPatterns("white")),
		new PatternGroup<>(2, "black rabbit¦s @a", Type.BLACK, getPatterns("black")),
		new PatternGroup<>(3, "black and white rabbit¦s @a", Type.BLACK_AND_WHITE, getPatterns("black [and] white")),
		new PatternGroup<>(4, "brown rabbit¦s @a", Type.BROWN, getPatterns("brown")),
		new PatternGroup<>(5, "gold rabbit¦s @a", Type.GOLD, getPatterns("gold")),
		new PatternGroup<>(6, "salt and pepper rabbit¦s @a", Type.SALT_AND_PEPPER, getPatterns("salt [and] pepper")),
		new PatternGroup<>(7, "killer rabbit¦s @a", Type.THE_KILLER_BUNNY, getPatterns("killer"))
	);

	private static String[] getPatterns(String prefix) {
		if (!prefix.isEmpty()) {
			return new String[] {
				"<age> " + prefix + " rabbit[plural:s]",
				prefix + " <age> rabbit[plural:s]",
				prefix + " rabbit <age>[plural:s]",
				"baby:" + prefix + " (kid[plural:s]|child[plural:ren])"
			};
		}
		return new String[] {
			"<age> rabbit[plural:s]",
			"rabbit <age>[plural:s]",
			"baby:rabbit (kid[plural:s]|child[plural:ren])"
		};
	}

	public static void register() {
		registerInfo(
			infoBuilder(RabbitData.class, "rabbit")
				.dataPatterns(GROUPS)
				.entityType(EntityType.RABBIT)
				.entityClass(Rabbit.class)
				.supplier(RabbitData::new)
				.build()
		);

		Variables.yggdrasil.registerSingleClass(Type.class, "Rabbit.Type");
    }

    private @Nullable Type type = null;
    
    public RabbitData() {}
    
    public RabbitData(@Nullable Type type) {
    	this.type = type;
		super.groupIndex = GROUPS.getIndex(type);
	}

    @Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
        type = GROUPS.getData(matchedGroup);
        return true;
    }

	@Override
    protected boolean init(@Nullable Class<? extends Rabbit> entityClass, @Nullable Rabbit rabbit) {
		if (rabbit != null) {
			type = rabbit.getRabbitType();
			super.groupIndex = GROUPS.getIndex(type);
		}
        return true;
    }

    @Override
    public void set(Rabbit rabbit) {
		Type type = this.type;
		if (type == null)
			type = CollectionUtils.getRandom(TYPES);
		assert type != null;
		rabbit.setRabbitType(type);
    }

	@Override
    protected boolean match(Rabbit rabbit) {
		return dataMatch(type, rabbit.getRabbitType());
    }

    @Override
    public Class<? extends Rabbit> getType() {
        return Rabbit.class;
    }

    @Override
    public @NotNull EntityData<?> getSuperType() {
        return new RabbitData();
    }

    @Override
    protected int hashCode_i() {
        return Objects.hashCode(type);
    }

    @Override
    protected boolean equals_i(EntityData<?> entityData) {
        if (!(entityData instanceof RabbitData other))
            return false;
        return type == other.type;
    }

    @Override
    public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof RabbitData other))
			return false;
        return dataMatch(type, other.type);
    }

}
