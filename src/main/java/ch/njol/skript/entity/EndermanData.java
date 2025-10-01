package ch.njol.skript.entity;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EndermanData extends EntityData<Enderman> {

	private final static ArgsMessage FORMAT = new ArgsMessage("entities.enderman.format");

	static {
		EntityData.register(EndermanData.class, "enderman", Enderman.class, "enderman");
	}
	private ItemType @Nullable [] hand = null;

	public EndermanData() {}

	public EndermanData(ItemType @Nullable [] hand) {
		this.hand = hand;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null) {
			//noinspection unchecked
			hand = ((Literal<ItemType>) exprs[0]).getAll();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Enderman> entityClass, @Nullable Enderman enderman) {
		if (enderman != null) {
			BlockData data = enderman.getCarriedBlock();
			if (data != null) {
				Material type = data.getMaterial();
				hand = new ItemType[] {new ItemType(type)};
			}
		}
		return true;
	}

	@Override
	public void set(Enderman enderman) {
		if (hand != null) {
			ItemType itemType = CollectionUtils.getRandom(hand);
			assert itemType != null;
			ItemStack itemStack = itemType.getBlock().getRandom();
			if (itemStack != null) {
				// 1.13: item->block usually keeps only material
				enderman.setCarriedBlock(Bukkit.createBlockData(itemStack.getType()));
			}
		}

	}

	@Override
	public boolean match(Enderman enderman) {
		return hand == null || SimpleExpression.check(hand, type -> {
			// TODO {Block/Material}Data -> Material conversion is not 100% accurate, needs a better solution
			return type != null && type.isOfType(enderman.getCarriedBlock().getMaterial());
		}, false, false);
	}

	@Override
	public Class<Enderman> getType() {
		return Enderman.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new EndermanData();
	}

	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(hand);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof EndermanData other))
			return false;
		return Arrays.equals(hand, other.hand);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof EndermanData other))
			return false;
		if (hand != null)
			return other.hand != null &&  ItemType.isSubset(hand, other.hand);
		return true;
	}

	@Override
	public String toString(int flags) {
		ItemType[] hand = this.hand;
		if (hand == null)
			return super.toString(flags);
		return FORMAT.toString(super.toString(flags), Classes.toString(hand, false));
	}

	private boolean isSubhand(@Nullable ItemType[] sub) {
		if (hand != null)
			return sub != null && ItemType.isSubset(hand, sub);
		return true;
	}

}
