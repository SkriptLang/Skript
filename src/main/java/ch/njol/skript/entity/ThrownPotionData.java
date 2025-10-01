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
import org.bukkit.Material;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Arrays;
import java.util.function.Consumer;

public class ThrownPotionData extends EntityData<ThrownPotion> {

	static {
		EntityData.register(ThrownPotionData.class, "thrown potion", ThrownPotion.class, "thrown potion");
	}
	
	private static final Adjective m_adjective = new Adjective("entities.thrown potion.adjective");
	private static final boolean LINGERING_POTION_ENTITY_USED = !Skript.isRunningMinecraft(1, 14);
	// LingeringPotion class deprecated and marked for removal
	@SuppressWarnings("removal")
	private static final Class<? extends ThrownPotion> LINGERING_POTION_ENTITY_CLASS =
		LINGERING_POTION_ENTITY_USED ? LingeringPotion.class : ThrownPotion.class;
	private static final Material POTION = Material.POTION;
	private static final Material SPLASH_POTION = Material.SPLASH_POTION;
	private static final Material LINGER_POTION = Material.LINGERING_POTION;

	private ItemType @Nullable [] types;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs.length > 0 && exprs[0] != null) {
			//noinspection unchecked
			ItemType[] itemTypes = ((Literal<ItemType>) exprs[0]).getAll();
			types = Converters.convert(itemTypes, ItemType.class, itemType -> {
				Material material = itemType.getMaterial();
				if (material == POTION) {
					ItemMeta itemMeta = itemType.getItemMeta();
					ItemType splashItem = new ItemType(SPLASH_POTION);
					splashItem.setItemMeta(itemMeta);
					return splashItem;
				} else if (material != SPLASH_POTION && material != LINGER_POTION) {
					return null;
				}
				return itemType;
			});
			return types.length != 0;
		} else {
			types = new ItemType[]{new ItemType(SPLASH_POTION)};
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends ThrownPotion> entityClass, @Nullable ThrownPotion thrownPotion) {
		if (thrownPotion != null) {
			ItemStack itemStack = thrownPotion.getItem();
			types = new ItemType[] {new ItemType(itemStack)};
		}
		return true;
	}

	@Override
	public void set(ThrownPotion thrownPotion) {
		if (types != null) {
			ItemType itemType = CollectionUtils.getRandom(types);
			assert itemType != null;
			ItemStack itemStack = itemType.getRandom();
			if (itemStack == null)
				return; // Missing item, can't make thrown potion of it
			if (LINGERING_POTION_ENTITY_USED && (LINGERING_POTION_ENTITY_CLASS.isInstance(thrownPotion) != (LINGER_POTION == itemStack.getType())))
				return;
			thrownPotion.setItem(itemStack);
		}
		assert false;
	}

	@Override
	protected boolean match(ThrownPotion thrownPotion) {
		if (types != null) {
			for (ItemType itemType : types) {
				if (itemType.isOfType(thrownPotion.getItem()))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public Class<? extends ThrownPotion> getType() {
		return ThrownPotion.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ThrownPotionData();
	}

	@Override
	protected int hashCode_i() {
		return Arrays.hashCode(types);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ThrownPotionData other))
			return false;
		return Arrays.equals(types, other.types);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ThrownPotionData other))
			return false;
		if (types == null)
			return true;
		return other.types != null && ItemType.isSubset(types, other.types);
	}

	@Override
	public @Nullable ThrownPotion spawn(Location location, @Nullable Consumer<ThrownPotion> consumer) {
		ItemType itemType = CollectionUtils.getRandom(types);
		assert itemType != null;
		ItemStack itemStack = itemType.getRandom();
		if (itemStack == null)
			return null;

		// noinspection unchecked,rawtypes
		Class<ThrownPotion> thrownPotionClass = (Class) (itemStack.getType() == LINGER_POTION ? LINGERING_POTION_ENTITY_CLASS : ThrownPotion.class);
		ThrownPotion potion;
		if (consumer != null) {
			potion = EntityData.spawn(location, thrownPotionClass, consumer);
		} else {
			potion = location.getWorld().spawn(location, thrownPotionClass);
		}

		if (potion == null)
			return null;
		potion.setItem(itemStack);
		return potion;
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
