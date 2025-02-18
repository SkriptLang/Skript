package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

// For <1.21.3 compatability only. 1.21.3+ boats are SimpleEntityDatas
public class BoatData extends EntityData<Boat> {

	private static final Boat.Type[] types = Boat.Type.values();

	static {
		if (!Skript.isRunningMinecraft(1, 21, 2)) {
			// This ensures all boats are registered
			// As well as in the correct order via 'ordinal'
			String[] patterns = new String[types.length + 2];
			patterns[0] = "boat";
			patterns[1] = "any boat";
			for (Boat.Type boat : types) {
				String boatName;
				if (boat == Boat.Type.BAMBOO) {
					boatName = "bamboo raft";
				} else {
					boatName = boat.toString().replace("_", " ").toLowerCase(Locale.ENGLISH) + " boat";
				}
				patterns[boat.ordinal() + 2] = boatName;
			}
			EntityData.register(BoatData.class, "boat", Boat.class, 0, patterns);
		}
	}
	
	public BoatData(){
		this(0);
	}

	public BoatData(@Nullable Boat.Type type){
		this(type != null ? type.ordinal() + 2 : 1);
	}
	
	private BoatData(int type) {
		matchedPattern = type;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Boat> clazz, @Nullable Boat entity) {
		if (entity != null)
			matchedPattern = 2 + entity.getBoatType().ordinal();
		return true;
	}

	@Override
	public void set(Boat entity) {
		if (matchedPattern == 1) // If the type is 'any boat'.
			matchedPattern += new Random().nextInt(Boat.Type.values().length); // It will spawn a random boat type in case is 'any boat'.
		if (matchedPattern > 1) // 0 and 1 are excluded
			entity.setBoatType(types[matchedPattern - 2]); // Removes 2 to fix the index.
	}

	@Override
	protected boolean match(Boat entity) {
		return matchedPattern <= 1 || entity.getBoatType().ordinal() == matchedPattern - 2;
	}

	@Override
	public Class<? extends Boat> getType() {
		return Boat.class;
	}

	@Override
	public EntityData getSuperType() {
		return new BoatData(matchedPattern);
	}

	@Override
	protected int hashCode_i() {
		return matchedPattern <= 1 ? 0 : matchedPattern;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof BoatData boatData)
			return matchedPattern == boatData.matchedPattern;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entity) {
		if (entity instanceof BoatData boatData)
			return matchedPattern <= 1 || matchedPattern == boatData.matchedPattern;
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
			int ordinal;
			Material material = itemData.getType();
			Boat.Type type = materialToType.get(material);
			// material is a boat AND (data matches any boat OR material and data are same)
			if (type != null) {
				ordinal = type.ordinal();
				if (matchedPattern <= 1 || matchedPattern == ordinal + 2)
					return true;
			}
		}
		return false;
	}

}
