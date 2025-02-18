package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Force Attack")
@Description("Makes a living entity attack an entity with a melee attack.")
@Examples({"spawn a wolf at player's location",
	"make last spawned wolf attack player"})
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.15.2+")
public class EffForceAttack extends Effect {
	
	static {
		Skript.registerEffect(EffForceAttack.class,
			"make %livingentities% attack %entity%",
			"force %livingentities% to attack %entity%");
	}
	
	private static final boolean ATTACK_IS_SUPPORTED = Skript.methodExists(LivingEntity.class, "attack", Entity.class);
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	private Expression<Entity> target;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!ATTACK_IS_SUPPORTED) {
			Skript.error("The force attack effect requires Minecraft 1.15.2 or newer");
			return false;
		}
		entities = (Expression<LivingEntity>) exprs[0];
		target = (Expression<Entity>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		Entity target = this.target.getSingle(e);
		if (target != null) {
			for (LivingEntity entity : entities.getArray(e)) {
				entity.attack(target);
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + entities.toString(e, debug) + " attack " + target.toString(e, debug);
	}
	
}
