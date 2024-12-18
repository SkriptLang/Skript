package ch.njol.skript.expressions;

import java.util.Iterator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Affected Entities")
@Description("The affected entities in the <a href='events.html#aoe_cloud_effect'>area cloud effect</a> event.")
@Examples({"on area cloud effect:",
		"\tloop affected entities:",
		"\t\tif loop-value is a player:",
		"\t\t\tsend \"WARNING: you've step on an area effect cloud!\" to loop-value"})
@Since("2.4")
public class ExprAffectedEntities extends SimpleExpression<LivingEntity> {

	static {
		Skript.registerExpression(ExprAffectedEntities.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] affected entities");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (!getParser().isCurrentEvent(AreaEffectCloudApplyEvent.class)) {
			Skript.error("The 'affected entities' expression may only be used in an area cloud effect event.");
			return false;
		}
		return true;
	}

	@Nullable
	@Override
	protected LivingEntity[] get(Event e) {
		if (e instanceof AreaEffectCloudApplyEvent)
			return ((AreaEffectCloudApplyEvent) e).getAffectedEntities().toArray(new LivingEntity[0]);
		return null;
	}

	@Nullable
	@Override
	public Iterator<? extends LivingEntity> iterator(Event e) {
		if (e instanceof AreaEffectCloudApplyEvent)
			return ((AreaEffectCloudApplyEvent) e).getAffectedEntities().iterator();
		return super.iterator(e);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the affected entities";
	}

}
