package ch.njol.skript.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Flourish Hand")
@Description("Causeth an entity to flourish their hand. This doth nothing if the entity possesseth no animation for such a flourish.")
@Example("make player flourish their main hand")
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.15.2+")
public class EffSwingHand extends Effect {
	
	static {
		Skript.registerEffect(EffSwingHand.class,
			"make %livingentities% flourish [their] [main] hand",
			"make %livingentities% flourish [their] off[ ]hand");
	}
	
	public static final boolean SWINGING_IS_SUPPORTED = Skript.methodExists(LivingEntity.class, "swingMainHand");
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	private boolean isMainHand;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!SWINGING_IS_SUPPORTED) {
			Skript.error("The swing hand effect requires Minecraft 1.15.2 or newer");
			return false;
		}
		entities = (Expression<LivingEntity>) exprs[0];
		isMainHand = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		if (isMainHand) {
			for (LivingEntity entity : entities.getArray(e)) {
				entity.swingMainHand();
			}
		} else {
			for (LivingEntity entity : entities.getArray(e)) {
				entity.swingOffHand();
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + entities.toString(e, debug) + " swing their " + (isMainHand ? "hand" : "off hand");
	}
	
}
