package ch.njol.skript.entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

import ch.njol.skript.Skript;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;

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

	private ItemType @Nullable [] types;
	
	public DroppedItemData() {}
	
	public DroppedItemData(ItemType @Nullable [] types) {
		this.types = types;
	}
	
	@Override
	protected boolean init(Literal<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		if (expressions.length > 0 && expressions[0] != null) {
			types = (ItemType[]) expressions[0].getAll();
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
	protected boolean init(@Nullable Class<? extends Item> clazz, @Nullable Item itemEntity) {
		if (itemEntity != null) {
			final ItemStack i = itemEntity.getItemStack();
			types = new ItemType[] {new ItemType(i)};
		}
		return true;
	}
	
	@Override
	protected boolean match(Item entity) {
		if (types != null) {
			for (ItemType t : types) {
				if (t.isOfType(entity.getItemStack()))
					return true;
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void set(final Item entity) {
		if (types == null)
			return;
		final ItemType t = CollectionUtils.getRandom(types);
		assert t != null;
		ItemStack stack = t.getItem().getRandom();
		assert stack != null; // should be true by init checks
		entity.setItemStack(stack);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> otherData) {
		if (!(otherData instanceof DroppedItemData))
			return false;
		DroppedItemData otherItemData = (DroppedItemData) otherData;
		if (types != null)
			return otherItemData.types != null && ItemType.isSubset(types, otherItemData.types);
		return true;
	}
	
	@Override
	public Class<? extends Item> getType() {
		return Item.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new DroppedItemData(types);
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
	@Deprecated
	protected boolean deserialize(String s) {
		throw new UnsupportedOperationException("old serialization is no longer supported");
	}
	
	@Override
	protected boolean equals_i(EntityData<?> otherData) {
		if (!(otherData instanceof DroppedItemData))
			return false;
		return Arrays.equals(types, ((DroppedItemData) otherData).types);
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

		final ItemType itemType = CollectionUtils.getRandom(types);
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
				// noinspection deprecation
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

	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}
	
}
