package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Boat.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

// For <1.21.3 compatability only. 1.21.3+ boats are SimpleEntityDatas
public class BoatData extends EntityData<Boat> {

	private static final Boat.Type[] types = Boat.Type.values();

	private static EntityDataPatterns<Boat.Type> GROUPS;

	public static void register() {
		if (!Skript.isRunningMinecraft(1, 21, 2)) {

			List<PatternGroup<Type>> groups = new ArrayList<>();
			groups.add(
				new PatternGroup<>(0, "boat¦s @a", "[any] boat[plural:s]")
			);

			for (Boat.Type type : types) {
				String name;
				String pattern;
				if (type == Type.BAMBOO) {
					name = "bamboo raft¦s @a";
					pattern = "bamboo (raft|boat)[plural:s]";
				} else {
					String boat = type.toString().replace("_", " ").toLowerCase(Locale.ENGLISH);
					name = boat + " boat¦s @a";
					pattern = boat + " boat[plural:s]";
				}
				groups.add(
					new PatternGroup<>(type.ordinal() + 1, name, type, pattern)
				);
			}

			//noinspection unchecked
			GROUPS = new EntityDataPatterns<>(groups.toArray(PatternGroup[]::new));

			registerInfo(
				infoBuilder(BoatData.class, "boat")
					.dataPatterns(GROUPS)
					.entityClass(Boat.class)
					.supplier(BoatData::new)
					.build()
			);
		}
	}
	
	public BoatData() {
		this(null);
	}

	public BoatData(@Nullable Boat.Type type){
		super.groupIndex = GROUPS.getIndex(type);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Boat> entityClass, @Nullable Boat boat) {
		if (boat != null)
			super.groupIndex = GROUPS.getIndex(boat.getBoatType());
		return true;
	}

	@Override
	public void set(Boat boat) {
		if (super.groupIndex == 0) // If the type is 'any boat'.
			super.groupIndex += new Random().nextInt(Boat.Type.values().length); // It will spawn a random boat type in case is 'any boat'.

		Boat.Type type = GROUPS.getData(super.groupIndex);
		assert type != null;
		boat.setBoatType(type);
	}

	@Override
	protected boolean match(Boat boat) {
		return super.groupIndex == 0 || super.groupIndex == GROUPS.getIndex(boat.getBoatType());
	}

	@Override
	public Class<? extends Boat> getType() {
		return Boat.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new BoatData();
	}

	@Override
	protected int hashCode_i() {
		return super.groupIndex;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (entityData instanceof BoatData other)
			return super.groupIndex == other.groupIndex;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof BoatData other)
			return super.groupIndex == 0 || super.groupIndex == other.groupIndex;
		return false;
	}

	private static final Map<Material, Boat.Type> materialToType = new HashMap<>();
	static {
		materialToType.put(Material.OAK_BOAT, Boat.Type.OAK);
		materialToType.put(Material.BIRCH_BOAT, Boat.Type.BIRCH);
		materialToType.put(Material.SPRUCE_BOAT, Boat.Type.SPRUCE);
		materialToType.put(Material.JUNGLE_BOAT, Boat.Type.JUNGLE);
		materialToType.put(Material.DARK_OAK_BOAT, Boat.Type.DARK_OAK);
		materialToType.put(Material.ACACIA_BOAT, Boat.Type.ACACIA);
		materialToType.put(Material.MANGROVE_BOAT, Boat.Type.MANGROVE);
		materialToType.put(Material.CHERRY_BOAT, Boat.Type.CHERRY);
		materialToType.put(Material.BAMBOO_RAFT, Boat.Type.BAMBOO);
		// 'oak chest boat is a boat' should pass
		materialToType.put(Material.OAK_CHEST_BOAT, Boat.Type.OAK);
		materialToType.put(Material.BIRCH_CHEST_BOAT, Boat.Type.BIRCH);
		materialToType.put(Material.SPRUCE_CHEST_BOAT, Boat.Type.SPRUCE);
		materialToType.put(Material.JUNGLE_CHEST_BOAT, Boat.Type.JUNGLE);
		materialToType.put(Material.DARK_OAK_CHEST_BOAT, Boat.Type.DARK_OAK);
		materialToType.put(Material.ACACIA_CHEST_BOAT, Boat.Type.ACACIA);
		materialToType.put(Material.MANGROVE_CHEST_BOAT, Boat.Type.MANGROVE);
		materialToType.put(Material.CHERRY_CHEST_BOAT, Boat.Type.CHERRY);
		materialToType.put(Material.BAMBOO_CHEST_RAFT, Boat.Type.BAMBOO);
	}

	public boolean isOfItemType(ItemType itemType) {
		for (ItemData itemData : itemType.getTypes()) {
			Material material = itemData.getType();
			Boat.Type type = materialToType.get(material);
			// material is a boat AND (data matches any boat OR material and data are same)
			if (type != null) {
				if (super.groupIndex <= 1 || super.groupIndex == GROUPS.getIndex(type))
					return true;
			}
		}
		return false;
	}

}
