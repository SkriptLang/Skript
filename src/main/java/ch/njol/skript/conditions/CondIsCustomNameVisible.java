package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Custom Name Visible")
@Description("Check if an entity's custom name is visible.")
@Examples({
	"send true if target's custom name is visible"
})
@Since("INSERT VERSION")
public class CondIsCustomNameVisible extends PropertyCondition<Entity> {

	static {
		Skript.registerCondition(CondIsCustomNameVisible.class,
			"%entities%'s custom name (:is|isn't) visible",
			"custom name of %entities% (:is|isn't) visible");
	}

	@Override
	public boolean check(Entity value) {
		return value.isCustomNameVisible() && !isNegated();
	}

	@Override
	protected String getPropertyName() {
		return "custom name";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "custom name";
	}


}
