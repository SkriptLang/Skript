package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
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
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumeEffectExperimentalSyntax;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Consume Effect - Play Sound")
@Description("""
	A consume effect that plays a sound when the item has been consumed.
	Consume effects have to be added to the consumable component of an item.
	NOTE: Consume Effect elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_effect} to a consume effect to play the sound "ui.toast.challenge.complete"
	add {_effect} to the consume effects of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsumeEffectSound extends SimpleExpression<ConsumeEffect> implements ConsumeEffectExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprConsumeEffectSound.class, ConsumeEffect.class)
				.addPatterns("[a] consume effect to play [[the] sound] %string%")
				.supplier(ExprConsumeEffectSound::new)
				.build()
		);
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
