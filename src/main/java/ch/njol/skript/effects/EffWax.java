package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.CopperGolem.Oxidizing;
import org.bukkit.entity.CopperGolem.Oxidizing.Waxed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.ReflectUtils;

@Name("Wax")
@Description("""
	Wax or unwax a copper golem or copper block.
	This does not change the weathering copper state of entities and blocks.
	""")
@Example("""
	if last spawned copper golem is not waxed:
		wax last spawned copper golem
	""")
@Example("""
	if {_block} is waxed:
		unwax {_block}
	""")
@RequiredPlugins("Minecraft 1.21.9+ (copper golems)")
@Since("INSERT VERSION")
public class EffWax extends Effect {


	private static final BiMap<Material, Material> WAX_CONVERSION;
	private static final BiMap<Material, Material> UNWAX_CONVERSION = HashBiMap.create();
	private static final boolean COPPER_GOLEM_EXISTS = ReflectUtils.classExists("org.bukkit.entity.CopperGolem");

	static {
		String type = "%blocks%";
		if (COPPER_GOLEM_EXISTS)
			type = "%entities/blocks%";
		Skript.registerEffect(EffWax.class, "[:un]wax " + type);

		for (Material waxed : MaterialTags.WAXED_COPPER_BLOCKS.getValues()) {
			Material unwaxed = Material.valueOf(waxed.name().replaceAll("WAXED_", ""));
			UNWAX_CONVERSION.put(waxed, unwaxed);
		}
		WAX_CONVERSION = UNWAX_CONVERSION.inverse();
	}

	private boolean wax;
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		wax = !parseResult.hasTag("un");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			if (COPPER_GOLEM_EXISTS && object instanceof CopperGolem golem) {
				boolean isWaxed = golem.getOxidizing() instanceof Waxed;
				if (wax == isWaxed)
					continue;
				CopperGolem.Oxidizing oxidizing = wax ? Oxidizing.waxed() : Oxidizing.unset();
				golem.setOxidizing(oxidizing);
			} else if (object instanceof Block block) {
				if (!MaterialTags.COPPER_BLOCKS.isTagged(block))
					continue;
				BiMap<Material, Material> conversion = wax ? WAX_CONVERSION : UNWAX_CONVERSION;
				if (conversion.containsKey(block.getType())) {
					Material material = conversion.get(block.getType());
					assert material != null;
					block.setType(material);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (wax) {
			builder.append("wax");
		} else {
			builder.append("unwax");
		}
		builder.append(objects);
		return builder.toString();
	}

}
