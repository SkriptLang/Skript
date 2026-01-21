package org.skriptlang.skript.bukkit.block.sign;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Sign Text")
@Description("A line of text on a sign. Can be changed, but note that there is a 16 character limit per line.")
@Example("""
	on right click:
		clicked block is tagged as "minecraft:all_signs"
		if line 2 of the clicked block is "[Heal]":
			heal the player
	""")
@Since("1.3")
// TODO SignSide support
public class ExprSignText extends SimpleExpression<Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSignText.class, Component.class)
			.supplier(ExprSignText::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns("[the] line %integer% [of %block%]",
				"[the] (1:1st|1:first|2:2nd|2:second|3:3rd|3:third|4:4th|4:fourth) line [of %block%]")
			.build());
	}

	private Expression<Integer> line;
	private Expression<Block> block;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 0) {
			line = (Expression<Integer>) exprs[0];
		} else {
			line = new SimpleLiteral<>(parseResult.mark, false);
		}
		block = (Expression<Block>) exprs[exprs.length - 1];
		return true;
	}
	
	@Override
	protected Component[] get(Event event) {
		Integer line = this.line.getSingle(event);
		if (line == null || line < 0 || line > 3) {
			return new Component[0];
		}
		if (getTime() >= 0 && block.isDefault() && event instanceof SignChangeEvent signEvent && !Delay.isDelayed(event)) {
			return new Component[]{signEvent.line(line)};
		}
		Block block = this.block.getSingle(event);
		if (block == null || !(block.getState() instanceof Sign signState)) {
			return new Component[0];
		}
		return new Component[]{signState.getSide(Side.FRONT).line(line)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		// TODO allow add, remove, and remove all (see ExprLore)
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Integer line = this.line.getSingle(event);
		if (line == null || line < 0 || line > 3) {
			return;
		}

		if (getTime() >= 0 && block.isDefault() && event instanceof SignChangeEvent signEvent && !Delay.isDelayed(event)) {
			signEvent.line(line, delta == null ? null : (Component) delta[0]);
			return;
		}

		Block block = this.block.getSingle(event);
		if (block == null || !(block.getState() instanceof Sign signState)) {
			return;
		}
		signState.getSide(Side.FRONT).line(line, delta == null ? Component.empty() : (Component) delta[0]);
		signState.update(false, false);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "line " + line.toString(event, debug) + " of " + block.toString(event, debug);
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, SignChangeEvent.class, block);
	}

}
