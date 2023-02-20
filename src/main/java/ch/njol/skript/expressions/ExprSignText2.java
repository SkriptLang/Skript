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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Sign Text")
@Description("A line of text on a sign. Can be changed, but remember that there is a 16 character limit per line (including color codes that use 2 characters each).")
@Examples({"on rightclick on sign:",
		"	line 2 of the clicked block is \"[Heal]\":",
		"		heal the player",
		"	set line 3 to \"%player%\""})
@Since("1.3")
public class ExprSignText2 extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprSignText2.class, String.class, ExpressionType.PROPERTY,
				"[all [of]] [the] lines [of %block%]",
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
	
	@SuppressWarnings({"unchecked", "null"})
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
			return CollectionUtils.array(
				((Sign) (block.getState())).getLines()
			);
		} else {
			Number lineNumber = line.getSingle(event);
			if (lineNumber == null || lineNumber.intValue() < 0 || lineNumber.intValue() > 3)
				return new String[0];
			int index = lineNumber.intValue() - 1;
			if (event instanceof SignChangeEvent && getTime() >= 0 && exprBlock.isDefault() && !delayed) {
				return new String[]{((SignChangeEvent) event).getLine(index)};
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
	public boolean isSingle() {
		return !isLines;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "line " + line.toString(e, debug) + " of " + exprBlock.toString(e, debug);
	}

	// TODO allow add, remove, and remove all (see ExprLore)
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		return CollectionUtils.array(String.class);
	}
	
	static boolean hasUpdateBooleanBoolean = true;
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Number l = line.getSingle(e);
		if (l == null)
			return;
		final int line = l.intValue() - 1;
		if (line < 0 || line > 3)
			return;
		final Block b = exprBlock.getSingle(e);
		if (b == null)
			return;
		if (getTime() >= 0 && e instanceof SignChangeEvent && b.equals(((SignChangeEvent) e).getBlock()) && !delayed) {
			switch (mode) {
				case DELETE:
					((SignChangeEvent) e).setLine(line, "");
					break;
				case SET:
					assert delta != null;
					((SignChangeEvent) e).setLine(line, (String) delta[0]);
					break;
			}
		} else {
			if (!sign.isOfType(b))
				return;
			final Sign s = (Sign) b.getState();
			switch (mode) {
				case DELETE:
					s.setLine(line, "");
					break;
				case SET:
					assert delta != null;
					s.setLine(line, (String) delta[0]);
					break;
			}
			if (hasUpdateBooleanBoolean) {
				try {
					s.update(false, false);
				} catch (final NoSuchMethodError err) {
					hasUpdateBooleanBoolean = false;
					s.update();
				}
			} else {
				s.update();
			}
		}
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, SignChangeEvent.class, exprBlock);
	}
	
}
