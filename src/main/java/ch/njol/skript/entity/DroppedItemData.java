package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

public class DroppedItemData extends EntityData<Item> {

	private static final boolean HAS_JAVA_CONSUMER_DROP = Skript.methodExists(World.class, "dropItem", Location.class, ItemStack.class, Consumer.class);
	private static @Nullable Method BUKKIT_CONSUMER_DROP;

	static {
		EntityData.register(DroppedItemData.class, "dropped item", Item.class, "dropped item");

		try {
			BUKKIT_CONSUMER_DROP = World.class.getDeclaredMethod("dropItem", Location.class, ItemStack.class, org.bukkit.util.Consumer.class);
		} catch (NoSuchMethodException | SecurityException ignored) {}
	}
	
	private final static Adjective m_adjective = new Adjective("entities.dropped item.adjective");

	private ItemType @Nullable [] types = null;
	
	public DroppedItemData() {}
	
	public DroppedItemData(ItemType @Nullable [] types) {
		this.types = types;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			//noinspection unchecked
			types = ((Literal<ItemType>) exprs[0]).getAll();
			for (ItemType type : types) {
				if (!type.getMaterial().isItem()) {
					Skript.error("'" + type + "' cannot represent a dropped item");
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Item> entityClass, @Nullable Item item) {
		if (item != null) {
			ItemStack itemStack = item.getItemStack();
			types = new ItemType[] {new ItemType(itemStack)};
		}
		return true;
	}

	@Override
	public void set(Item item) {
		if (types == null)
			return;
		ItemType itemType = CollectionUtils.getRandom(types);
		assert itemType != null;
		ItemStack stack = itemType.getItem().getRandom();
		assert stack != null; // should be true by init checks
		item.setItemStack(stack);
	}

	@Override
	protected boolean match(Item item) {
		if (types != null) {
			for (ItemType itemType : types) {
				if (itemType.isOfType(item.getItemStack()))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public Class<? extends Item> getType() {
		return Item.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new DroppedItemData();
	}

	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof DroppedItemData other))
			return false;
		return Arrays.equals(types, other.types);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> otherData) {
		if (!(otherData instanceof DroppedItemData other))
			return false;
		if (types != null)
			return other.types != null && ItemType.isSubset(types, other.types);
		return true;
	}

	@Override
	public String toString(int flags) {
		if (types == null)
			return super.toString(flags);
		int gender = types[0].getTypes().get(0).getGender();
		return Noun.getArticleWithSpace(gender, flags) +
				m_adjective.toString(gender, flags) +
				" " +
				Classes.toString(types, flags & Language.NO_ARTICLE_MASK, false);
	}

	@Override
	public boolean canSpawn(@Nullable World world) {
		return types != null && types.length > 0 && world != null;
	}

	@Override
	public @Nullable Item spawn(Location location, @Nullable Consumer<Item> consumer) {
		World world = location.getWorld();
		if (!canSpawn(world))
			return null;
		assert types != null && types.length > 0;

		ItemType itemType = CollectionUtils.getRandom(types);
		assert itemType != null;
		ItemStack stack = itemType.getItem().getRandom();
		assert stack != null; // should be true by init checks

		Item item;
		if (consumer == null) {
			item = world.dropItem(location, stack);
		} else if (HAS_JAVA_CONSUMER_DROP) {
			item = world.dropItem(location, stack, consumer);
		} else if (BUKKIT_CONSUMER_DROP != null) {
			try {
				//noinspection removal
				item = (Item) BUKKIT_CONSUMER_DROP.invoke(world, location, stack, (org.bukkit.util.Consumer<Item>) consumer::accept);
			} catch (InvocationTargetException | IllegalAccessException e) {
				if (Skript.testing())
					Skript.exception(e, "Can't spawn " + this.getName());
				return null;
			}
		} else {
			item = world.dropItem(location, stack);
			consumer.accept(item);
		}
		return item;
	}

}
