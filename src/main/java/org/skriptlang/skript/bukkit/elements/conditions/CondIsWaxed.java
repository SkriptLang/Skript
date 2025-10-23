package org.skriptlang.skript.bukkit.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.block.Block;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.CopperGolem.Oxidizing.Waxed;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Waxed")
@Description("Whether a copper golem or copper block is waxed.")
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
public class CondIsWaxed extends PropertyCondition<Object> {

	private static final boolean COPPER_GOLEM_EXISTS = Skript.classExists("org.bukkit.entity.CopperGolem");

	public static void register(SyntaxRegistry registry) {
		String type = "blocks";
		if (Skript.classExists("org.bukkit.entity.CopperGolem"))
			type = "entities/blocks";
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsWaxed.class, PropertyType.BE, "waxed", type)
				.supplier(CondIsWaxed::new)
				.build()
		);
	}

	@Override
	public boolean check(Object object) {
		if (COPPER_GOLEM_EXISTS && object instanceof CopperGolem golem) {
			return golem.getOxidizing() instanceof Waxed;
		} else if (object instanceof Block block) {
			return MaterialTags.WAXED_COPPER_BLOCKS.isTagged(block);
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "waxed";
	}

}
