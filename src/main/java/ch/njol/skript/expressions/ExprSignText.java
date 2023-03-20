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
	private Expression<Block> block;
	private boolean delayed;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isLines = (matchedPattern == 0);
		if (!isLines) {
			if (matchedPattern == 1) {
				line = (Expression<Number>) exprs[0];
			} else {
				new SimpleLiteral<>(parseResult.mark, false);
			}
		}
		block = (Expression<Block>) exprs[exprs.length - 1];
		delayed = isDelayed == Kleenean.TRUE;
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		Block block = this.block.getSingle(event);
		if (block == null || !sign.isOfType(block))
			return new String[0];
		if (isLines) {
			if ((event instanceof SignChangeEvent && getTime() >= 0 && block.equals(((SignChangeEvent) event).getBlock()) && !delayed))
				return ((SignChangeEvent) event).getLines();
			return ((Sign) (block.getState())).getLines();
		} else {
			int line = this.line.getOptionalSingle(event).orElse(-1).intValue();
			if (line < 1 || line > 4)
				return new String[0];
			line--;
			if (event instanceof SignChangeEvent && getTime() >= 0 && block == ((SignChangeEvent) event).getBlock() && !delayed) {
				return CollectionUtils.array(((SignChangeEvent) event).getLine(line));
			}
			return CollectionUtils.array(((Sign) (block.getState())).getLine(line));
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (isLines) {
			switch (mode) {
				case ADD:
				case SET:
				case REMOVE:
				case REMOVE_ALL:
				case DELETE:
					return CollectionUtils.array(String[].class);
				case RESET:
					return null;
			}
		}
		switch (mode) {
			case DELETE:
			case SET:
				return CollectionUtils.array(String.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Block block = this.block.getSingle(event);
		if (block == null || !sign.isOfType(block))
			return;
		if (isLines) {
			Sign state = (Sign) block.getState();
			switch (mode) {
				case ADD:
					for (String text : (String[]) delta) {
						text = Utils.replaceChatStyles(text);
						int index = 0;
						while (!state.getLine(index).equals("")) {
							index++;
							if (index > 3)
								break;
						}
						if (index <= 3)
							state.setLine(index, text);
					}
					break;
				case DELETE:
					for (int index = 0; index < 4; index++) {
						state.setLine(index, "");
					}
					break;
				case REMOVE:
				case REMOVE_ALL:
					for (String text : (String[]) delta) {
						text = Utils.replaceChatStyles(text);
						for (int index = 0; index < 4; index++) {
							if (state.getLine(index).equals(text)) {
								state.setLine(index, "");
								if (mode == ChangeMode.REMOVE) {
									break;
								}
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
			int line = this.line.getOptionalSingle(event).orElse(-1).intValue();
			if (line < 1 || line > 4)
				return;
			line--;
			if (event instanceof SignChangeEvent && getTime() >= 0 && block.equals(((SignChangeEvent) event).getBlock()) && !delayed) {
				((SignChangeEvent) event).setLine(line, text);
			} else {
				Sign state = (Sign) block.getState();
				if (mode == ChangeMode.SET) {
					state.setLine(line, text);
				} else {
					state.setLine(line, "");
				}
				state.update(false, false);
			}
		}
	}

	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, SignChangeEvent.class, block);
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
	public String toString(@Nullable Event event, boolean debug) {
		return (isLines ? "lines" : "line " + line.toString(event, debug)) + " of " + block.toString(event, debug);
	}

}
