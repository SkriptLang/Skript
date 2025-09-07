package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;

@SuppressWarnings("UnstableApiUsage")
public class ExprConsumeEffectSound extends SimpleExpression<ConsumeEffect> implements ConsumableExperimentSyntax {

	static {
		Skript.registerExpression(ExprConsumeEffectSound.class, ConsumeEffect.class, ExpressionType.PROPERTY,
			"[a] consume effect to play [[the] sound] %string%");
	}

	private Expression<String> string;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		string = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected ConsumeEffect @Nullable [] get(Event event) {
		String string = this.string.getSingle(event);
		if (string == null)
			return null;

		Sound sound = SoundUtils.getSound(string);
		if (sound == null) {
			error("Could not find a sound with the id '" + string + "'.");
			return null;
		}
		Key key = Registry.SOUNDS.getKey(sound);
		ConsumeEffect effect = ConsumeEffect.playSoundConsumeEffect(key);

		return new ConsumeEffect[] {effect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ConsumeEffect> getReturnType() {
		return ConsumeEffect.class;
	}

	@Override
	public Expression<? extends ConsumeEffect> simplify() {
		if (string instanceof Literal<String>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a consume effect to play the sound", string)
			.toString();
	}

}
