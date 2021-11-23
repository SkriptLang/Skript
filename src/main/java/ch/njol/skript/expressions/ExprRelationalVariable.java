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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.PersistentDataUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Relational Variable")
@Description({"A relational variable is a variable stored on an entity, projectile, item, or certain blocks, and it can only be accessed using that entity.",
			" See <a href='classes.html#persistentdataholder'>persistent data holder</a> for a list of all holders.",
			" Relational Variables will persist through a server restart, however, just like normal variables,",
			" not all values can be stored permanently (e.g. entities). If the value can't be stored permanently,",
			" it will be stored until the server is restarted."
})
@Examples({"set {isAdmin} of player to true",
			"set {oldNames::*} of player to \"Noob_Sl4yer\" and \"Skr1pt_M4st3r\""})
@RequiredPlugins("1.14 or newer")
@Since("2.5")
@SuppressWarnings({"null", "unchecked"})
public class ExprRelationalVariable<T> extends SimpleExpression<T> {

	static {
		// Temporarily disabled until bugs are fixed
		if (false && Skript.isRunningMinecraft(1, 14)) {
			Skript.registerExpression(ExprRelationalVariable.class, Object.class, ExpressionType.PROPERTY,
					"[(relational|relation( |-)based) variable[s]] %objects% of %persistentdataholders/itemtypes/blocks%"
			);
		}
	}

	private ExpressionList<Variable<?>> variables;
	private Expression<Object> holders;

	private ExprRelationalVariable<?> source;
	private Class<? extends T>[] types;
	private Class<T> superType;

	public ExprRelationalVariable() {
		this(null, (Class<? extends T>) Object.class);
	}

	private ExprRelationalVariable(ExprRelationalVariable<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.variables = source.variables;
			this.holders = source.holders;
		}
		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ExpressionList<?> exprList = exprs[0] instanceof ExpressionList ? (ExpressionList<?>) exprs[0] : new ExpressionList<>(new Expression<?>[]{exprs[0]}, Object.class, false);
		for (Expression<?> expr : exprList.getExpressions()) {
			if (!(expr instanceof Variable<?>)) { // Input isn't a variable
				return false;
			} else if (((Variable<?>) expr).isLocal()) { // Input is a variable, but it's local
				Skript.error("Setting a relational variable using a local variable is not supported."
						+ " If you are trying to set a value temporarily, consider using metadata", ErrorQuality.SEMANTIC_ERROR
				);
				return false;
			}
		}
		variables = (ExpressionList<Variable<?>>) exprList;
		holders = (Expression<Object>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	public T[] get(Event e) {
		List<Object> values = new ArrayList<>();
		Object[] holders = this.holders.getArray(e);
		for (Expression<?> expr : variables.getExpressions()) {
			String varName = ((Variable<?>) expr).getName().toString(e);
			if (varName.contains(Variable.SEPARATOR)) { // It's a list
				Collections.addAll(values, PersistentDataUtils.getList(varName, holders));
			} else { // It's a single variable
				Collections.addAll(values, PersistentDataUtils.getSingle(varName, holders));
			}
		}
		try {
			return Converters.convertArray(values.toArray(), types, superType);
		} catch (ClassCastException ex) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return null;
		for (Expression<?> expr : variables.getExpressions()) {
			if (!((Variable<?>) expr).isList()) { // It's a single variable
				if (mode == ChangeMode.REMOVE_ALL)
					return null;
				return CollectionUtils.array(Object.class);
			}
		}
		return CollectionUtils.array(Object[].class);
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.DELETE)
			return;
		Object[] holders = this.holders.getArray(e);
		switch (mode) {
			case SET:
				for (Expression<?> expr : variables.getExpressions()) {
					Variable<?> var = (Variable<?>) expr;
					String varName = var.getName().toString(e);
					if (var.isList()) {
						varName = varName.replace("*", "");
						for (int i = 1; i <= delta.length; i++) // varName + i = var::i (e.g. exampleList::1, exampleList::2, etc.)
							PersistentDataUtils.setList(varName + i, delta[i - 1], holders);
					} else if (varName.contains(Variable.SEPARATOR)) { // Specific index of a list
						PersistentDataUtils.setList(varName, delta[0], holders);
					} else { // It's a single variable
						PersistentDataUtils.setSingle(varName, delta[0], holders);
					}
				}
				break;
			case DELETE:
				for (Expression<?> expr : variables.getExpressions()) {
					String varName = ((Variable<?>) expr).getName().toString(e);
					if (varName.contains(Variable.SEPARATOR)) { // It's a list
						PersistentDataUtils.removeList(varName, holders);
					} else { // It's a single variable
						PersistentDataUtils.removeSingle(varName, holders);
					}
				}
				break;
			case ADD:
				for (Expression<?> expr : variables.getExpressions()) {
					Variable<?> var = (Variable<?>) expr;
					String varName = var.getName().toString(e);
					if (var.isList()) {
						varName = varName.replace("*", "");
						for (Object holder : holders) {
							Set<String> varIndexes = PersistentDataUtils.getListIndexes(varName + "*", holder);
							if (varIndexes == null) {
								// The list is empty, so we don't need to check for the next available index.
								for (int i = 1; i <= delta.length; i++) {
									// varName + i = var::i (e.g. exampleList::1, exampleList::2, etc.)
									PersistentDataUtils.setList(varName + i, delta[i - 1], holder);
								}
							} else {
								int start = 1 + varIndexes.size();
								for (Object value : delta) {
									while (varIndexes.contains(String.valueOf(start)))
										start++;
									PersistentDataUtils.setList(varName + start, value, holder);
									start++;
								}
							}
						}
					} else if (delta[0] instanceof Number) {
						for (Object holder : holders) {
							Object[] n = PersistentDataUtils.getSingle(varName, holder);
							double oldValue = 0;
							if (n.length != 0 && n[0] instanceof Number)
								oldValue = ((Number) n[0]).doubleValue();
							PersistentDataUtils.setSingle(varName, oldValue + ((Number) delta[0]).doubleValue(), holder);
						}
					}
				}
				break;
			case REMOVE:
			case REMOVE_ALL:
				for (Expression<?> expr : variables.getExpressions()) {
					Variable<?> var = (Variable<?>) expr;
					String varName = var.getName().toString(e);
					if (var.isList() || mode == ChangeMode.REMOVE_ALL) {
						for (Object holder : holders) {
							Map<String, Object> varMap = PersistentDataUtils.getListMap(varName, holder);
							int sizeBefore = varMap.size();
							if (varMap != null) {
								for (Object value : delta)
									varMap.entrySet().removeIf(entry -> Relation.EQUAL.is(Comparators.compare(entry.getValue(), value)));
								if (sizeBefore != varMap.size()) // It changed so we should set it
									PersistentDataUtils.setListMap(varName, varMap, holder);
							}
						}
					} else if (delta[0] instanceof Number) {
						for (Object holder : holders) {
							Object[] n = PersistentDataUtils.getSingle(varName, holder);
							double oldValue = 0;
							if (n.length != 0 && n[0] instanceof Number)
								oldValue = ((Number) n[0]).doubleValue();
							PersistentDataUtils.setSingle(varName, oldValue - ((Number) delta[0]).doubleValue(), holder);
						}
					}
				}
				break;
			case RESET:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return variables.isSingle() && holders.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprRelationalVariable<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return variables.toString(e, debug) + " of " + holders.toString(e, debug);
	}

}
