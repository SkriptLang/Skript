package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
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

	private static final Patterns<Type> CODE_NAMES = new Patterns<>(new Object[][]{
		{"rabbit", null},
		{"white rabbit", Type.WHITE},
		{"black rabbit", Type.BLACK},
		{"black and white rabbit", Type.BLACK_AND_WHITE},
		{"brown rabbit", Type.BROWN},
		{"gold rabbit", Type.GOLD},
		{"salt and pepper rabbit", Type.SALT_AND_PEPPER},
		{"killer rabbit", Type.THE_KILLER_BUNNY}
	});

	public static void register() {
		registerInfo(
			infoBuilder(RabbitData.class, "rabbit")
				.addCodeNames(CODE_NAMES.getPatterns())
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
		super.codeNameIndex = CODE_NAMES.getMatchedPattern(type, 0).orElse(0);
	}

    @Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
        type = CODE_NAMES.getInfo(matchedCodeName);
        return true;
    }

	@Override
    protected boolean init(@Nullable Class<? extends Rabbit> entityClass, @Nullable Rabbit rabbit) {
		if (rabbit != null) {
			type = rabbit.getRabbitType();
			super.codeNameIndex = CODE_NAMES.getMatchedPattern(type, 0).orElse(0);
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
