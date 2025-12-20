package ch.njol.skript.effects;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

@Name("Handedness")
@Description("Make mobs left or right-handed. This does not affect players.")
@Example("""
	spawn skeleton at spawn of world "world":
		make entity left handed
	""")
@Example("make all zombies in radius 10 of player right handed")
@RequiredPlugins("Minecraft 1.21.9+ (mannequins)")
@Since("2.8.0, INSERT VERSION (mannequins)")
public class EffHandedness extends Effect {

	private static final boolean MANNEQUIN_EXISTS = Skript.classExists("org.bukkit.entity.Mannequin");

	static {
		Skript.registerEffect(EffHandedness.class, "make %livingentities% (:left|right)( |-)handed");
	}

	private boolean leftHanded;
	private Expression<LivingEntity> livingEntities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		leftHanded = parseResult.hasTag("left");
		livingEntities = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		MainHand mainHand = leftHanded ? MainHand.LEFT : MainHand.RIGHT;
		for (LivingEntity livingEntity : livingEntities.getArray(event)) {
			if (livingEntity instanceof Mob mob) {
				mob.setLeftHanded(leftHanded);
			} else if (MANNEQUIN_EXISTS && livingEntity instanceof Mannequin mannequin) {
				mannequin.setMainHand(mainHand);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + livingEntities.toString(event, debug) + " " + (leftHanded ? "left" : "right") + " handed";
	}

}
