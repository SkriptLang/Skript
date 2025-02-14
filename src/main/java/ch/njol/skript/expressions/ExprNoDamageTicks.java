package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wither;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("No Damage Ticks")
@Description("The number of ticks that an entity is invulnerable to damage for.")
@Examples({"on damage:",
		"	set victim's invulnerability ticks to 20 #Victim will not take damage for the next second"})
@Since("2.5")
public class ExprNoDamageTicks extends SimplePropertyExpression<LivingEntity, Long> {
	
	static {
		register(ExprNoDamageTicks.class, Long.class, "(invulnerability|invincibility|no damage) tick[s]", "livingentities");
	}

	@Override
	public Long convert(LivingEntity e) {
		return (long) (e instanceof Wither wither ? wither.getInvulnerableTicks() : e.getNoDamageTicks());
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		int ticks = delta == null ? 0 : delta[0] instanceof Number number ? number.intValue() : 0;
		for (LivingEntity livingEntity : getExpr().getArray(e)) {
			Integer noDamageTicks = switch (mode) {
				case REMOVE_ALL -> null;
				case DELETE, RESET, SET -> ticks;
				default -> {
					int currentTicks;
					if (livingEntity instanceof Wither wither) currentTicks = wither.getInvulnerableTicks();
					else currentTicks = livingEntity.getNoDamageTicks();
					yield currentTicks + (mode == ChangeMode.ADD ? ticks : -ticks);
				}
			};
			if (noDamageTicks == null) 
				continue;
			if (noDamageTicks < 0) 
				noDamageTicks = 0;
			if (livingEntity instanceof Wither wither)
				wither.setInvulnerableTicks(noDamageTicks);
			else 
				livingEntity.setNoDamageTicks(noDamageTicks);
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "no damage ticks";
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
}
