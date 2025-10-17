package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.paperutil.CopperState;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.MaterialTags;
import io.papermc.paper.world.WeatheringCopperState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.CopperGolem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.ReflectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Name("Weathering Copper State")
@Description("""
	The weathering copper state of a copper golem or copper block.
	Changing the copper state does not change the waxed state.
	""")
@Example("""
	if the copper state of last spawned copper golem is weathered:
		set the copper state of last spawned golem to normal
	""")
@Example("""
	if the weathering copper state of {_block} is not oxidized:
		set the weathering copper state of {_block} to oxidized
	""")
@RequiredPlugins("Minecraft 1.21.9+ (copper golems)")
@Since("INSERT VERSION")
public class ExprCopperState extends SimplePropertyExpression<Object, Object> {

	// TODO: Remove 'CopperState' and change all instances of 'Enum<?>' to 'WeatheringCopperState'

	private static final CopperStateMaterialMap STATE_MATERIALS = new CopperStateMaterialMap();
	private static final List<String> STATE_REPLACEMENTS = new ArrayList<>();
	private static final boolean COPPER_GOLEM_EXISTS = ReflectUtils.classExists("org.bukkit.entity.CopperGolem");

	static {
		String type = "blocks";
		if (Skript.classExists("org.bukkit.entity.CopperGolem"))
			type = "entities/blocks";

		register(ExprCopperState.class, Object.class, "[weathering] copper state[s]", type);

		for (Enum<?> state : CopperState.getValues()) {
			if (state.name().equals("UNAFFECTED"))
				continue;
			STATE_REPLACEMENTS.add(state.name() + "_");
		}

		for (Material material : MaterialTags.COPPER_BLOCKS.getValues()) {
			Enum<?> state = getMaterialCopperState(material);
			assert state != null;
			String blockType = getBlockType(material);
			STATE_MATERIALS.putMaterial(blockType, state, material);
		}
	}

	/**
	 * Gets the string of the block type {@code material} relates to.
	 * i.e. stair, slab, door, trapdoor, etc.
	 * @param material The {@link Material} to get the block type from.
	 * @return The resulting string block type.
	 */
	public static String getBlockType(Material material) {
		String type = material.name();
		for (String replace : STATE_REPLACEMENTS)
			type = type.replaceAll(replace, "");
		if (type.equals("COPPER_BLOCK")) {
			type = "COPPER";
		} else if (type.equals("WAXED_COPPER_BLOCK")) {
			type = "WAXED_COPPER";
		}
		return type;
	}

	/**
	 * Gets the {@link CopperState} or 'WeatheringCopperState' a {@link Material} belongs to.
	 * @param material The {@link Material} to check.
	 * @return The resulting {@link CopperState} or 'WeatheringCopperState' if found, otherwise {@code null}.
	 */
	public static @Nullable Enum<?> getMaterialCopperState(Material material) {
		if (!MaterialTags.COPPER_BLOCKS.isTagged(material))
			return null;
		if (MaterialTags.EXPOSED_COPPER_BLOCKS.isTagged(material)) {
			return CopperState.get(CopperState.EXPOSED);
		}  else if (MaterialTags.WEATHERED_COPPER_BLOCKS.isTagged(material)) {
			return CopperState.get(CopperState.WEATHERED);
		} else if (MaterialTags.OXIDIZED_COPPER_BLOCKS.isTagged(material)) {
			return CopperState.get(CopperState.OXIDIZED);
		}
		return CopperState.get(CopperState.UNAFFECTED);
	}

	@Override
	public @Nullable Object convert(Object object) {
		if (COPPER_GOLEM_EXISTS && object instanceof CopperGolem golem) {
			return golem.getWeatheringState();
		} else if (object instanceof Block block) {
			return getMaterialCopperState(block.getType());
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(CopperState.getStateClass());
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Enum<?> state = CopperState.get(CopperState.UNAFFECTED);
		if (delta != null)
			state = (Enum<?>) delta[0];

		for (Object object : getExpr().getArray(event)) {
			if (COPPER_GOLEM_EXISTS && object instanceof CopperGolem golem) {
				golem.setWeatheringState((WeatheringCopperState) state);
			} else if (object instanceof Block block) {
				Material material = getConvertedCopperMaterial(block.getType(), state);
				if (material != null)
					block.setType(material);
			}
		}
	}

	/**
	 * Gets the {@link Material} that is the same block type as {@code material} and in the {@code state}.
	 * @param material The current {@link Material} to convert from.
	 * @param state The {@link CopperState} or 'WeatheringCopperState' to get the same block type as {@code material}.
	 * @return The resulting {@link Material} if found, otherwise {@code null}.
	 */
	public static @Nullable Material getConvertedCopperMaterial(Material material, Enum<?> state) {
		if (!MaterialTags.COPPER_BLOCKS.isTagged(material))
			return null;
		String type = getBlockType(material);
		return STATE_MATERIALS.getMaterial(type, state);
	}

	@Override
	public Class<?> getReturnType() {
		return CopperState.getStateClass();
	}

	@Override
	protected String getPropertyName() {
		return "weathering copper state";
	}

	/**
	 * Map for storing a block type, a {@link CopperState} or 'WeatheringCopperState' and the {@link Material} that is the block type and state.
	 */
	private static class CopperStateMaterialMap extends HashMap<String, Material[]> {

		public void putMaterial(String key, Enum<?> state, Material material) {
			computeIfAbsent(key, array -> new Material[CopperState.getValues().length])[state.ordinal()] = material;
		}

		public @Nullable Material getMaterial(String key, Enum<?> state) {
			if (!containsKey(key))
				return null;
			Material[] materials = get(key);
			return materials[state.ordinal()];
		}

	}

}
