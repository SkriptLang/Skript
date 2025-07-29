package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public class FallingBlockData extends EntityData<FallingBlock> {

	private final static Message m_not_a_block_error = new Message("entities.falling block.not a block error");
	private final static Adjective m_adjective = new Adjective("entities.falling block.adjective");

	static {
		EntityData.register(FallingBlockData.class, "falling block", FallingBlock.class, "falling block");
	}

	private ItemType @Nullable [] types = null;
	
	public FallingBlockData() {}
	
	public FallingBlockData(ItemType @Nullable [] types) {
		this.types = types;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 1) {
			assert exprs[0] != null;
			//noinspection unchecked
			ItemType[] itemTypes = ((Literal<ItemType>) exprs[0]).getAll();
			types = Arrays.stream(itemTypes)
				.map(itemType -> {
					ItemType clone = itemType.getBlock().clone();
					Iterator<ItemData> iterator = clone.iterator();
					while (iterator.hasNext()) {
						Material material = iterator.next().getType();
						if (!material.isBlock())
							iterator.remove();
					}
					if (clone.numTypes() == 0)
						return null;
					clone.setAmount(-1);
					clone.setAll(false);
					clone.clearEnchantments();
					return clone;
				})
				.filter(Objects::nonNull)
				.toArray(ItemType[]::new);
			if (types.length == 0) {
				Skript.error(m_not_a_block_error.toString());
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends FallingBlock> entityClass, @Nullable FallingBlock fallingBlock) {
		if (fallingBlock != null) // TODO material data support
			types = new ItemType[] {new ItemType(fallingBlock.getBlockData())};
		return true;
	}

	@Override
	public void set(FallingBlock fallingBlock) {
		assert false;
	}

	@Override
	protected boolean match(FallingBlock fallingBlock) {
		if (types != null) {
			for (ItemType itemType : types) {
				if (itemType.isOfType(fallingBlock.getBlockData()))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public Class<? extends FallingBlock> getType() {
		return FallingBlock.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new FallingBlockData();
	}

	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof FallingBlockData other))
			return false;
		return Arrays.equals(types, other.types);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof FallingBlockData other))
			return false;
		if (types != null) {
			if (other.types != null)
				return ItemType.isSubset(types, other.types);
			return false;
		}
		return true;
	}

	@Override
	public @Nullable FallingBlock spawn(Location loc, @Nullable Consumer<FallingBlock> consumer) {
		ItemType t = types == null ? new ItemType(Material.STONE) : CollectionUtils.getRandom(types);
		assert t != null;
		Material material = t.getMaterial();
		if (!material.isBlock()) {
			assert false : t;
			return null;
		}

		FallingBlock fallingBlock = loc.getWorld().spawnFallingBlock(loc, material.createBlockData());
		if (consumer != null)
			consumer.accept(fallingBlock);

		return fallingBlock;
	}

	@Override
	public String toString(int flags) {
		ItemType[] types = this.types;
		if (types == null)
			return super.toString(flags);
		StringBuilder builder = new StringBuilder();
		builder.append(Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags));
		builder.append(m_adjective.toString(types[0].getTypes().get(0).getGender(), flags));
		builder.append(" ");
		builder.append(Classes.toString(types, flags & Language.NO_ARTICLE_MASK, false));
		return builder.toString();
	}

}
