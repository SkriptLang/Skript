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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.AdventureSetSignLine;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;

@Name("Sign Text")
@Description("A line of text on a sign. Can be changed, but remember that there is a 16 character limit per line (including color codes that use 2 characters each).")
@Examples({
	"on rightclick on sign:",
		"\tline 2 of the clicked block is \"[Heal]\":",
			"\t\theal the player",
		"\tset line 3 to \"%player%\""
})
@Since("1.3, INSERT VERSION (all lines, back side, multiple blocks, Skript's ChatFormat (hex, font, etc))")
@RequiredPlugins({
	"Paper 1.16+ or Adventure API installed to use Skript ChatFormat",
	"Spigot 1.20+ required to use the sign side"
})
public class ExprSignText extends SimpleExpression<String> {

	@Nullable
	private static BungeeComponentSerializer serializer;
	private static final boolean RUNNING_1_20 = Skript.isRunningMinecraft(1, 20);

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			serializer = BungeeComponentSerializer.get();
		String addition = RUNNING_1_20 ? "[[on [the] (front|:back) side] of [sign[s]] %blocks%]" : "[of [sign[s]] %blocks%]";
		Skript.registerExpression(ExprSignText.class, String.class, ExpressionType.PROPERTY,
				"[all [[of] the]|the] lines " + addition,
				RUNNING_1_20 ? "%blocks%'[s] [(front|:back) side] lines" : "%blocks%'[s] lines",

				"[the] line %number% " + addition,
				"[the] (1¦1st|1¦first|2¦2nd|2¦second|3¦3rd|3¦third|4¦4th|4¦fourth) line[s] " + addition);
	}

	@Nullable
	private Expression<Number> line;
	private Expression<Block> blocks;
	private boolean multipleLines;

	@Nullable
	private Side side; // Nullable due to versions before 1.20.

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 2) {
			line = (Expression<Number>) exprs[0];
		} else if (matchedPattern == 3) {
			line = new SimpleLiteral<>(parseResult.mark, false);
		} else {
			multipleLines = true;
		}
		if (RUNNING_1_20)
			side = parseResult.hasTag("back") ? Side.BACK : Side.FRONT;
		blocks = (Expression<Block>) exprs[matchedPattern != 2 ? 0 : 1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		int line = 0;
		if (this.line == null && !multipleLines) {
			return new String[0];
		} else if (!multipleLines) {
			line = this.line.getOptionalSingle(event).orElse(-1).intValue() - 1;
			if (line < 0 || line > 3)
				return new String[0];
		}
		if (getTime() >= 0 && event instanceof SignChangeEvent && blocks.check(event, block -> block.equals(((SignChangeEvent) event).getBlock()))) {
			if (multipleLines)
				return ((SignChangeEvent) event).getLines();
			return new String[] {((SignChangeEvent) event).getLine(line)};
		}
		int finalLine = line;
		return blocks.stream(event)
				.map(Block::getState)
				.filter(Sign.class::isInstance)
				.map(Sign.class::cast)
				.flatMap(sign -> {
					if (RUNNING_1_20) {
						if (multipleLines)
							return Arrays.stream(sign.getSide(side).getLines());
						return Stream.of(sign.getSide(side).getLine(finalLine));
					}
					if (multipleLines)
						return Arrays.stream(sign.getLines());
					return Stream.of(sign.getLine(finalLine));
				})
				.toArray(String[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		boolean acceptsMany = line == null;
		switch (mode) {
			case REMOVE:
			case REMOVE_ALL:
			case DELETE:
			case RESET:
				acceptsMany = false;
			case SET:
			case ADD:
				return CollectionUtils.array(acceptsMany ? String[].class : String.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		int line = 0;
		if (this.line == null && !multipleLines) {
			return;
		} else if (!multipleLines) {
			line = this.line.getOptionalSingle(event).orElse(-1).intValue() - 1;
			if (line < 0 || line > 3)
				return;
		}
		if (getTime() >= 0 && event instanceof SignChangeEvent && blocks.check(event, block -> block.equals(((SignChangeEvent) event).getBlock()))) {
			String[] stringDelta = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);
			SignChangeEvent changeEvent = (SignChangeEvent) event;
			AdventureSetSignLine<Integer, String> ADVENTURE_SET_LINE = null;
			if (serializer != null)
				ADVENTURE_SET_LINE = changeEvent::line;
			change(mode, changeEvent::getLines, changeEvent::getLine, changeEvent::setLine, ADVENTURE_SET_LINE, line, stringDelta);
		}
		int finalLine = line;
		blocks.stream(event)
				.map(Block::getState)
				.filter(Sign.class::isInstance)
				.map(Sign.class::cast)
				.forEach(sign -> {
					String[] strings = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);
					AdventureSetSignLine<Integer, String> ADVENTURE_SET_LINE = null;
					if (serializer != null)
						ADVENTURE_SET_LINE = sign::line;
					if (RUNNING_1_20) {
						SignSide side = sign.getSide(this.side);
						if (serializer != null)
							ADVENTURE_SET_LINE = side::line;
						change(mode, side::getLines, side::getLine, side::setLine, ADVENTURE_SET_LINE, finalLine, strings);
					} else {
						change(mode, sign::getLines, sign::getLine, sign::setLine, ADVENTURE_SET_LINE, finalLine, strings);
					}
					sign.update(true, false);
				});
	}

	/**
	 * Functional interface method to allow for getLines, getLines(int), setLine(int, String), line(int Component) on Sign, SignChangeEvent and SignSide
	 */
	private void change(ChangeMode mode, GetSignLines GET_LINES, GetSignLine<Integer> GET_LINE, SetSignLine<Integer, String> SET_LINE, @Nullable AdventureSetSignLine<Integer, String> ADVENTURE_SET_LINE, int line, String... strings) {
		switch (mode) {
			case ADD:
				List<String> list = Lists.newArrayList(GetSignLines.getLines(GET_LINES));
				if (multipleLines) {
					int last = -1; // Last white space character.
					for (int i = 3; i >= 0; i--) {
						if (list.get(i).trim().isEmpty()) {
							last = i;
						} else {
							break;
						}
					}
					if (last < 0)
						return;
					int index = 0;
					for (int i = last; i < 4; i++) {
						if (index > strings.length - 1)
							continue;
						list.set(last, strings[index++]);
					}

					strings = list.toArray(new String[0]);
					change(ChangeMode.SET, GET_LINES, GET_LINE, SET_LINE, ADVENTURE_SET_LINE, line, strings);
				} else {
					change(ChangeMode.SET, GET_LINES, GET_LINE, SET_LINE, ADVENTURE_SET_LINE, line, GetSignLine.getLine(GET_LINE, line) + StringUtils.join(strings));
				}
				break;
			case RESET:
			case DELETE:
				change(ChangeMode.SET, GET_LINES, GET_LINE, SET_LINE, ADVENTURE_SET_LINE, line, CollectionUtils.array("", "", "", ""));
				break;
			case REMOVE:
			case REMOVE_ALL:
				strings = ChangerUtils.handleStringRemove(StringUtils.join(strings, "\n"), strings[0], mode == ChangeMode.REMOVE_ALL).split("\n");
				//$FALL-THROUGH$
			case SET:
				if (multipleLines) {
					for (int i = 0; i < 4; i++) {
						String value = strings.length > i ? (String) strings[i] : "";
						if (serializer != null && ADVENTURE_SET_LINE != null) {
							AdventureSetSignLine.line(ADVENTURE_SET_LINE, i, serializer.deserialize(BungeeConverter.convert(ChatMessages.parseToArray(value))));
							continue;
						}
						SetSignLine.setLine(SET_LINE, i, value);
					}
					break;
				}
				SetSignLine.setLine(SET_LINE, line, (String) strings[0]);
				break;
			default:
				break;
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, SignChangeEvent.class, blocks);
	}

	@Override
	public boolean isSingle() {
		return !multipleLines;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (line == null)
			return "lines of " + blocks.toString(event, debug);
		return "line " + line.toString(event, debug) + " of " + blocks.toString(event, debug);
	}

	@FunctionalInterface
	private interface SetSignLine<T, S> {

		void setLine(int line, @NotNull String value);

		static void setLine(SetSignLine<Integer, String> setLineMethod, int line, String value) {
			setLineMethod.setLine(line, value);
		}
	}

	@FunctionalInterface
	private interface GetSignLine<T> {

		String getLine(int line);

		static String getLine(GetSignLine<Integer> getLineMethod, int line) {
			return getLineMethod.getLine(line);
		}
	}

	@FunctionalInterface
	private interface GetSignLines {

		String[] getLines();

		static String[] getLines(GetSignLines getLinesMethod) {
			return getLinesMethod.getLines();
		}
	}

}
