package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperimentSyntax;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource.DamageSourceSectionEvent;

@Name("Damage Source - Damage Type")
@Description({
	"The type of damage of a damage source.",
	"Attributes of a damage source cannot be changed once created, only while within the 'custom damage source' section."
})
@Example("""
	set {_source} to a custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
	damage all players by 5 using {_source}
	""")
@Example("""
	on death:
		set {_type} to the damage type of event-damage source
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class ExprDamageType extends SimplePropertyExpression<DamageSource, DamageType> implements DamageSourceExperimentSyntax {

	static {
		registerDefault(ExprDamageType.class, DamageType.class, "damage type", "damagesources");
	}

	private boolean isEvent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = getParser().isCurrentEvent(DamageSourceSectionEvent.class);
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable DamageType convert(DamageSource damageSource) {
		return damageSource.getDamageType();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You cannot change the attributes of a damage source outside a 'custom damage source' section.");
		} else if (!getExpr().isSingle() || !getExpr().isDefault()) {
			Skript.error("You can only change the attributes of the damage source being created in this section.");
		} else if (mode == ChangeMode.SET) {
			return CollectionUtils.array(DamageType.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		if (!(event instanceof DamageSourceSectionEvent sectionEvent))
			return;

		sectionEvent.damageType = (DamageType) delta[0];
	}

	@Override
	public Class<DamageType> getReturnType() {
		return DamageType.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage type";
	}

}
