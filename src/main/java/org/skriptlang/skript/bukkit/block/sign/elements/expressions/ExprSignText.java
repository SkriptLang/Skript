package org.skriptlang.skript.bukkit.block.sign.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
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
@Description("""
	A line of text on a sign. \
	Can be changed, but note that there is a 16 character limit per line. \
	If a sign side is not specified, the front side will be used by default, unless in a sign change event and a block is not explicitly specified.
	""")
@Example("""
	on right click:
		clicked block is tagged as "minecraft:all_signs"
		if line 2 of the clicked block is "[Heal]":
			heal the player
	""")
@Example("""
	on sign change:
		any of the lines contain "bad word"
		cancel the event
		send "<red>You may not write profanity on signs!" to the player
	""")
@Since({"1.3", "INSERT VERSION (sign side support)"})
public class ExprSignText extends SimpleExpression<Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSignText.class, Component.class)
			.supplier(ExprSignText::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns("line %integer% [of [the] (:front|:back) [side]] [of %block%]",
				"[the] (1:1st|1:first|2:2nd|2:second|3:3rd|3:third|4:4th|4:fourth) line [of [the] (:front|:back) [side]] [of %block%]",
				"[all [[of] the]|the] lines [of [the] (:front|:back) [side]] [of %block%]")
			.build());
	}

	private @Nullable Expression<Integer> line;
	private Expression<Block> block;
	private @Nullable Side side;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			line = (Expression<Integer>) exprs[0];
		} else if (matchedPattern == 1) {
			line = new SimpleLiteral<>(parseResult.mark, false);
		}
		block = (Expression<Block>) exprs[exprs.length - 1];
		if (parseResult.hasTag("front")) {
			side = Side.FRONT;
		} else if (parseResult.hasTag("back")) {
			side = Side.BACK;
		}
		return true;
	}

	private int getLine(Event event) {
		if (this.line == null) {
			return 0;
		}
		Integer line = this.line.getSingle(event);
		if (line == null) {
			return -1;
		}
		if (line < 1 || line > 4) {
			error("Signs only have lines from 1 to 4, but tried to obtain line " + line);
			return -1;
		}
		line--; // we accept 1-indexed, convert to 0-indexed
		return line;
	}

	@Override
	protected Component[] get(Event event) {
		int line = getLine(event);
		if (line == -1) {
			return new Component[0];
		}

		Side side = this.side;
		if (getTime() >= 0 && block.isDefault() && event instanceof SignChangeEvent signEvent && (side == null || side == signEvent.getSide())) {
			if (Delay.isDelayed(event)) { // event is delayed, obtain with regular methods on the correct side
				side = signEvent.getSide();
			} else if (this.line == null) {
				return signEvent.lines().toArray(new Component[0]);
			} else {
				return new Component[]{signEvent.line(line)};
			}
		} else if (side == null) {
			side = Side.FRONT;
		}

		Block block = this.block.getSingle(event);
		if (block == null || !(block.getState() instanceof Sign signState)) {
			return new Component[0];
		}
		if (this.line == null) {
			return signState.getSide(side).lines().toArray(new Component[0]);
		}
		return new Component[]{signState.getSide(side).line(line)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (this.line == null) {
			Skript.error("It is only possible to change a specific sign line, not all lines at once.");
			return null;
		}
		// TODO allow add, remove, and remove all (see ExprLore)
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int line = getLine(event);
		if (line == -1) {
			return;
		}

		Side side = this.side;
		if (getTime() >= 0 && block.isDefault() && event instanceof SignChangeEvent signEvent && (side == null || side == signEvent.getSide())) {
			if (Delay.isDelayed(event)) { // event is delayed, modify with regular methods on the correct side
				side = signEvent.getSide();
			} else {
				signEvent.line(line, delta == null ? null : (Component) delta[0]);
				return;
			}
		} else if (side == null) {
			side = Side.FRONT;
		}

		Block block = this.block.getSingle(event);
		if (block == null || !(block.getState() instanceof Sign signState)) {
			return;
		}
		signState.getSide(side).line(line, delta == null ? Component.empty() : (Component) delta[0]);
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
		return new SyntaxStringBuilder(event, debug)
			.appendIf(line == null, "the lines")
			.appendIf(line != null, "line", line)
			.appendIf(side != null, "of the", (side == Side.FRONT ? "front" : "back"), "side")
			.append("of", block)
			.toString();
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, SignChangeEvent.class, block);
	}

}
