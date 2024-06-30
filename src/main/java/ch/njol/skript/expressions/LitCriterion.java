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
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.scoreboard.Criterion;
import ch.njol.skript.util.scoreboard.ScoreUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Criteria;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Peter Güttinger
 */
@NoDoc
public class LitCriterion extends SimpleLiteral<Criterion> {

	private static final Criterion[] CRITERIA;
	private static final Criterion DEFAULT = new Criterion("dummy", "dummy");

	static {
		if (ScoreUtils.ARE_CRITERIA_AVAILABLE) { // todo inline in 2.10?
			List<Criterion> list = new ArrayList<>(50);
			for (Field field : Criteria.class.getFields()) {
				try {
					if (field.getType() != Criteria.class)
						continue;
					Criteria criteria = (Criteria) field.get(null);
					list.add(new Criterion(makeNiceName(field.getName()), criteria.getName(), criteria));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Skript.exception(e, "Can't get criteria patterns.");
				}
			}
			CRITERIA = list.toArray(new Criterion[0]);
		} else { // older versions don't have any list available
			CRITERIA = new Criterion[] {new Criterion("dummy", "dummy")};
		}
		List<String> list = Arrays.stream(CRITERIA)
				.map(criterion -> criterion.pattern() + " criteri(on|a)").collect(Collectors.toList());
		list.add("%*string% criteri(on|a)");
		Skript.registerExpression(LitCriterion.class, Criterion.class, ExpressionType.SIMPLE, list.toArray(new String[0]));
	}

	public LitCriterion() {
		super(DEFAULT, false);
	}

	private Criterion criterion = DEFAULT;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, final ParseResult result) {
		if (pattern < CRITERIA.length) {
			this.criterion = CRITERIA[pattern];
			return true;
		}
		if (!(expressions[0] instanceof Literal<?>))
			return false;
		//noinspection unchecked
		Literal<String> literal = (Literal<String>) expressions[0];
		this.criterion = new Criterion(literal.toString(null, false), literal.getSingle());
		return true;
	}

	@Override
	protected Criterion[] data() {
		return new Criterion[] {criterion};
	}

	@Override
	public String toString(@Nullable Event event, final boolean debug) {
		return criterion.pattern() + " criterion";
	}

	private static String makeNiceName(String original) {
		return original.replace('_', ' ').toLowerCase(Locale.ENGLISH);
	}

}
