package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Don Upon Entities")
@Description("""
    Whether an entity ought to don the item whence one doth right-click upon the entity with said item.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("grant {_item} to be donned upon entities")
@Since("2.13")
@RequiredPlugins("Minecraft 1.21.5+")
public class EffEquipCompInteract extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompInteract.class)
			.addPatterns(
				"grant %equippablecomponents% to be donned upon entities",
				"make %equippablecomponents% donnable upon entities",
				"let %equippablecomponents% be donned upon entities",
				"(forbid|prevent|deny) %equippablecomponents% from being donned upon entities",
				"make %equippablecomponents% not donnable upon entities"
			)
			.supplier(EffEquipCompInteract::new)
			.build()
		);
	}

	private boolean equip;
	private Expression<EquippableWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		equip = matchedPattern < 3;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.equipOnInteract(equip)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (equip)
			return "allow " + wrappers.toString(event, debug) + " to be equipped onto entities";
		return "prevent " + wrappers.toString(event, debug) + " from being equipped onto entities";
	}

}
