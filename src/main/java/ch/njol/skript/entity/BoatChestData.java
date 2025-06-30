package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ChestBoat;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

// For <1.21.3 compatability only. 1.21.3+ boats are SimpleEntityDatas
public class BoatChestData extends EntityData<ChestBoat> {

	private static final Boat.Type[] types = Boat.Type.values();

	static {
		if (!Skript.isRunningMinecraft(1, 21, 2)) {
			// This ensures all boats are registered
			// As well as in the correct order via 'ordinal'
			String[] patterns = new String[types.length + 2];
			patterns[0] = "chest boat";
			patterns[1] = "any chest boat";
			for (Boat.Type boat : types) {
				String boatName;
				if (boat == Boat.Type.BAMBOO) {
					boatName = "bamboo chest raft";
				} else {
					boatName = boat.toString().replace("_", " ").toLowerCase(Locale.ENGLISH) + " chest boat";
				}
				patterns[boat.ordinal() + 2] = boatName;
			}

			EntityData.register(BoatChestData.class, "chest boat", ChestBoat.class, 0, patterns);
		}
	}

	public BoatChestData() {
		this(0);
	}

	public BoatChestData(@Nullable Boat.Type type) {
		this(type != null ? type.ordinal() + 2 : 1);
	}

	private BoatChestData(int type) {
		matchedCodeName = type;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ChestBoat> clazz, @Nullable ChestBoat entity) {
		if (entity != null)
			matchedCodeName = 2 + entity.getBoatType().ordinal();
		return true;
	}

	@Override
	public void set(ChestBoat entity) {
		if (matchedCodeName == 1) // If the type is 'any boat'.
			matchedCodeName += new Random().nextInt(Boat.Type.values().length); // It will spawn a random boat type in case is 'any boat'.
		if (matchedCodeName > 1) // 0 and 1 are excluded
			entity.setBoatType(types[matchedCodeName - 2]); // Removes 2 to fix the index.
	}

	@Override
	protected boolean match(ChestBoat entity) {
		return matchedCodeName <= 1 || entity.getBoatType().ordinal() == matchedCodeName - 2;
	}

	@Override
	public Class<? extends ChestBoat> getType() {
		return ChestBoat.class;
	}

	@Override
	public EntityData getSuperType() {
		return new BoatChestData(matchedCodeName);
	}

	@Override
	protected int hashCode_i() {
		return matchedCodeName <= 1 ? 0 : matchedCodeName;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof BoatChestData boatChestData)
			return matchedCodeName == boatChestData.matchedCodeName;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entity) {
		if (entity instanceof BoatChestData boatChestData)
			return matchedCodeName <= 1 || matchedCodeName == boatChestData.matchedCodeName;
		return false;
	}


	private static final Map<Material, Boat.Type> materialToType = new HashMap<>();
	static {
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
			int ordinal;
			Material material = itemData.getType();
			Boat.Type type = materialToType.get(material);
			// material is a boat AND (data matches any boat OR material and data are same)
			if (type != null) {
				ordinal = type.ordinal();
				if (matchedCodeName <= 1 || matchedCodeName == ordinal + 2)
					return true;
			}
		}
		return false;
	}

}
