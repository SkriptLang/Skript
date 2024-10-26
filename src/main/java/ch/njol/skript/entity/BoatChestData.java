package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ChestBoat;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Random;

public class BoatChestData extends EntityData<ChestBoat> {

	private static final Boat.Type[] types = Boat.Type.values();

	static {
		// This ensures all boats are registered
		// As well as in the correct order via 'ordinal'
		String[] patterns = new String[types.length + 2];
		patterns[0] = "chest boat";
		patterns[1] = "any chest boat";
		for (Boat.Type boat : types) {
			String boatName;
			if (boat == Boat.Type.BAMBOO)
				boatName = "bamboo chest raft";
			else
				boatName = boat.toString().replace("_", " ").toLowerCase(Locale.ENGLISH) + " chest boat";
			patterns[boat.ordinal() + 2] = boatName;
		}

		if (Skript.classExists("org.bukkit.entity.ChestBoat")) {
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
		matchedPattern = type;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ChestBoat> clazz, @Nullable ChestBoat entity) {
		if (entity != null)
			matchedPattern = 2 + entity.getBoatType().ordinal();
		return true;
	}

	@Override
	public void set(ChestBoat entity) {
		if (matchedPattern == 1) // If the type is 'any boat'.
			matchedPattern += new Random().nextInt(Boat.Type.values().length); // It will spawn a random boat type in case is 'any boat'.
		if (matchedPattern > 1) // 0 and 1 are excluded
			entity.setBoatType(Boat.Type.values()[matchedPattern - 2]); // Removes 2 to fix the index.
	}

	@Override
	protected boolean match(ChestBoat entity) {
		return matchedPattern <= 1 || entity.getBoatType().ordinal() == matchedPattern - 2;
	}

	@Override
	public Class<? extends ChestBoat> getType() {
		return ChestBoat.class;
	}

	@Override
	public EntityData getSuperType() {
		return new BoatChestData(matchedPattern);
	}

	@Override
	protected int hashCode_i() {
		return matchedPattern <= 1 ? 0 : matchedPattern;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (obj instanceof BoatChestData boatChestData)
			return matchedPattern == boatChestData.matchedPattern;
		return false;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entity) {
		if (entity instanceof BoatChestData boatChestData)
			return matchedPattern <= 1 || matchedPattern == boatChestData.matchedPattern;
		return false;
	}

	public boolean isOfItemType(ItemType itemType) {
		int ordinal = -1;

		Material material = itemType.getMaterial();
		if (material == Material.OAK_CHEST_BOAT) {
			ordinal = 0;
		} else {
			for (Boat.Type boat : types) {
				if (material.name().contains(boat.toString())) {
					ordinal = boat.ordinal();
					break;
				}
			}
		}
		return hashCode_i() == ordinal + 2 || (matchedPattern + ordinal == 0) || ordinal == 0;
	}

}
