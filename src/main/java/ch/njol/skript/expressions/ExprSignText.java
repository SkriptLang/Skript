/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Sign Text")
@Description("A line of text on a sign. Can be changed, but remember that there is a 16 character limit per line (including color codes that use 2 characters each).")
@Examples({
		"on rightclick on sign:",
		"\tline 2 of the clicked block is \"[Heal]\":",
		"\t\theal the player",
		"\tset line 3 of block at {_sign} to \"%player%\""
})
@Since("1.3")
public class ExprSignText extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprSignText.class, String.class, ExpressionType.PROPERTY,
			"[the] lines [of %block%]",
			"[the] line %number% [of %block%]",
			"[the] (1¦1st|1¦first|2¦2nd|2¦second|3¦3rd|3¦third|4¦4th|4¦fourth) line [of %block%]"
			);
	}

	private static final ItemType sign = Aliases.javaItemType("sign");

	private boolean isLines;
	@SuppressWarnings("null")
	private Expression<Number> line;
	@SuppressWarnings("null")
	private Expression<Block> exprBlock;
	private boolean delayed;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isLines = (matchedPattern == 0);
		if (!isLines)
			line = matchedPattern == 1 ? (Expression<Number>) exprs[0] : new SimpleLiteral<>(parseResult.mark, false);
		exprBlock = (Expression<Block>) exprs[exprs.length - 1];
		delayed = isDelayed == Kleenean.TRUE;
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		Block block = exprBlock.getSingle(event);
		if (isLines) {
			if (block == null || !sign.isOfType(block))
				return new String[0];
			if ((event instanceof SignChangeEvent && getTime() >= 0 && block.equals(((SignChangeEvent) event).getBlock()) && !delayed))
				return ((SignChangeEvent) event).getLines();
			return ((Sign) (block.getState())).getLines();
		} else {
			Number lineNumber = line.getSingle(event);
			if (lineNumber == null || lineNumber.intValue() < 1 || lineNumber.intValue() > 4)
				return new String[0];
			int index = lineNumber.intValue() - 1;
			if (event instanceof SignChangeEvent && getTime() >= 0 && block == ((SignChangeEvent) event).getBlock() && !delayed) {
				return CollectionUtils.array(((SignChangeEvent) event).getLine(index));
			}
			if (block == null || !sign.isOfType(block)) {
				return new String[0];
			}
			return CollectionUtils.array(
				((Sign) (block.getState())).getLine(index)
			);
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (isLines) {
			if (mode == ChangeMode.RESET) {
				return null;
			}
			return CollectionUtils.array(String[].class);
		} else if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(String.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null) {
			if (mode != ChangeMode.DELETE && mode != ChangeMode.RESET)
				return;
		}
		Block block = exprBlock.getSingle(event);
		if (block == null)
			return;
		if (isLines) {
			if (!sign.isOfType(block))
				return;
			Sign state = (Sign) block.getState();
			switch (mode) {
				case ADD:
					for (String text : (String[]) delta) {
						int index = 0;
						while (!state.getLine(index).equals("")) {
							index++;
							if (index > 3)
								break;
						}
						if (index <= 3)
							state.setLine(index, Utils.replaceChatStyles(text));
					}
					break;
				case DELETE:
					for (int index = 0; index < 4; index++) {
						state.setLine(index, "");
					}
					break;
				case REMOVE:
					for (String text : (String[]) delta) {
						for (int index = 0; index <= 3; index++) {
							if (state.getLine(index).equals(Utils.replaceChatStyles(text))) {
								state.setLine(index, "");
								break;
							}
						}
					}
					break;
				case REMOVE_ALL:
					for (String text : (String[]) delta) {
						for (int index = 0; index <= 3; index++) {
							if (state.getLine(index).equals(Utils.replaceChatStyles(text))) {
								state.setLine(index, "");
							}
						}
					}
					break;
				case SET:
					for (int index = 0; index < Math.min(4, delta.length); index++) {
						state.setLine(index, Utils.replaceChatStyles((String) delta[index]));
					}
					break;
			}
			state.update(false, false);
		} else {
			String text = "";
			if (mode == ChangeMode.SET)
				text = Utils.replaceChatStyles((String) delta[0]);
			Number lineNumber = line.getSingle(event);
			if (lineNumber == null || lineNumber.intValue() < 1 || lineNumber.intValue() > 4)
				return;
			int index = lineNumber.intValue() - 1;
			if (event instanceof SignChangeEvent && getTime() >= 0 && block.equals(((SignChangeEvent) event).getBlock()) && !delayed) {
				((SignChangeEvent) event).setLine(index, text);
			} else {
				if (!sign.isOfType(block)) {
					return;
				}
				Sign state = (Sign) block.getState();
				switch (mode) {
					case SET:
						state.setLine(index, text);
						break;
					case DELETE:
						state.setLine(index, "");
						break;
				}
				state.update(false, false);
			}
		}
	}

	@Override
	public boolean isSingle() {
		return !isLines;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, SignChangeEvent.class, exprBlock);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isLines ? "lines" : "line " + line.toString(event, debug)) + " of " + exprBlock.toString(event, debug);
	}

}
