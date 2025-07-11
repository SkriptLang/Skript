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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperimentSyntax;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource.DamageSourceSectionEvent;

@Name("Damage Source - Direct Entity")
@Description({
	"The direct entity of a damage source.",
	"The direct entity is the entity that directly caused the damage. (e.g. the arrow that was shot)",
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
		set {_direct} to the direct entity of event-damage source
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class ExprDirectEntity extends SimplePropertyExpression<DamageSource, Entity> implements DamageSourceExperimentSyntax {

	static {
		registerDefault(ExprDirectEntity.class, Entity.class, "direct entity", "damagesources");
	}

	private boolean isEvent;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = getParser().isCurrentEvent(DamageSourceSectionEvent.class);
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Entity convert(DamageSource damageSource) {
		return damageSource.getDirectEntity();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!isEvent) {
			Skript.error("You cannot change the attributes of a damage source outside a 'custom damage source' section.");
		} else if (!getExpr().isSingle() || !getExpr().isDefault()) {
			Skript.error("You can only change the attributes of the damage source being created in this section.");
		} else if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(Entity.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof DamageSourceSectionEvent sectionEvent))
			return;

		sectionEvent.directEntity = delta == null ? null : (Entity) delta[0];
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "direct entity";
	}

}
