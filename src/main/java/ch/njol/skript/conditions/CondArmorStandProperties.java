package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

@Name("Armor Stand - Has Properties")
@Description("Allows users to check the properties of an armor stand (i.e. whether it's small or a marker).")
@Examples({
	"send true if {_armorstand} is small",
	"if {_armorstands::*} are not markers:",
})
@Since("INSERT VERSION")
@Keywords({"small", "marker"})
public class CondArmorStandProperties extends PropertyCondition<LivingEntity> {

	static {
		register(CondArmorStandProperties.class, "(:small|[a] marker[s])", "livingentities");
	}

	private boolean small;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		small = parseResult.hasTag("small");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof ArmorStand stand)
			return small ? stand.isSmall() : stand.isMarker();
		return false;
	}

	@Override
	public String getPropertyName() {
		return small ? "small" : "a marker";
	}

}
