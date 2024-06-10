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
package ch.njol.skript.effects;

import java.util.Arrays;

import ch.njol.skript.expressions.ExprParse;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Change: Set/Add/Remove/Delete/Reset")
@Description("A very general effect that can change many <a href='./expressions'>expressions</a>. Many expressions can only be set and/or deleted, while some can have things added to or removed from them.")
@Examples({"# set:",
		"Set the player's display name to \"&lt;red&gt;%name of player%\"",
		"set the block above the victim to lava",
		"# add:",
		"add 2 to the player's health # preferably use '<a href='#EffHealth'>heal</a>' for this",
		"add argument to {blacklist::*}",
		"give a diamond pickaxe of efficiency 5 to the player",
		"increase the data value of the clicked block by 1",
		"# remove:",
		"remove 2 pickaxes from the victim",
		"subtract 2.5 from {points::%uuid of player%}",
		"# remove all:",
		"remove every iron tool from the player",
		"remove all minecarts from {entitylist::*}",
		"# delete:",
		"delete the block below the player",
		"clear drops",
		"delete {variable}",
		"# reset:",
		"reset walk speed of player",
		"reset chunk at the targeted block"})
@Since("1.0 (set, add, remove, delete), 2.0 (remove all)")
public class EffChange extends Effect {
	private static Patterns<ChangeMode> patterns = new Patterns<>(new Object[][] {
			{"(add|give) %objects% to %~objects%", ChangeMode.ADD},
			{"increase %~objects% by %objects%", ChangeMode.ADD},
			{"give %~objects% %objects%", ChangeMode.ADD},
			
			{"set %~objects% to %objects%", ChangeMode.SET},
			
			{"remove (all|every) %objects% from %~objects%", ChangeMode.REMOVE_ALL},
			
			{"(remove|subtract) %objects% from %~objects%", ChangeMode.REMOVE},
			{"(reduce|decrease) %~objects% by %objects%", ChangeMode.REMOVE},
			
			{"(delete|clear) %~objects%", ChangeMode.DELETE},
			
			{"reset %~objects%", ChangeMode.RESET}
	});
	
	static {
		Skript.registerEffect(EffChange.class, patterns.getPatterns());
	}
	
	@SuppressWarnings("null")
	private Expression<?> expressionToChange;

	@Nullable
	private Expression<?> deltaValuesExpression = null;
	
	@SuppressWarnings("null")
	private ChangeMode changeMode;
	
	private boolean allowMultipleChangeValues;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		changeMode = patterns.getInfo(matchedPattern);
		
		switch (changeMode) {
			case ADD:
				if (matchedPattern == 0) {
					deltaValuesExpression = exprs[0];
					expressionToChange = exprs[1];
				} else {
					deltaValuesExpression = exprs[1];
					expressionToChange = exprs[0];
				}
				break;
			case SET:
				deltaValuesExpression = exprs[1];
				expressionToChange = exprs[0];
				break;
			case REMOVE_ALL:
				deltaValuesExpression = exprs[0];
				expressionToChange = exprs[1];
				break;
			case REMOVE:
				if (matchedPattern == 5) {
					deltaValuesExpression = exprs[0];
					expressionToChange = exprs[1];
				} else {
					deltaValuesExpression = exprs[1];
					expressionToChange = exprs[0];
				}
				break;
			case DELETE:
			case RESET:
				expressionToChange = exprs[0];
				break;
        }

		Class<?>[] acceptableChangeTypes;
		String what;
		CountingLogHandler acceptChangeLogs = new CountingLogHandler(SkriptLogger.SEVERE);
		acceptChangeLogs.start();
		try {
			acceptableChangeTypes = expressionToChange.acceptChange(changeMode);
			ClassInfo<?> returnTypeClassInfo = Classes.getSuperClassInfo(expressionToChange.getReturnType());
			Changer<?> returnTypeChanger = returnTypeClassInfo.getChanger();
			if (returnTypeChanger == null || !Arrays.equals(returnTypeChanger.acceptChange(changeMode), acceptableChangeTypes)) {
				what = expressionToChange.toString(null, false);
			} else {
				what = returnTypeClassInfo.getName().withIndefiniteArticle();
			}
		} finally {
			acceptChangeLogs.stop();
		}
		if (acceptableChangeTypes == null) {
			if (acceptChangeLogs.getCount() > 0)
				return false;
			switch (changeMode) {
				case SET:
					Skript.error(what + " can't be set to anything", ErrorQuality.SEMANTIC_ERROR);
					break;
				case DELETE:
					if (expressionToChange.acceptChange(ChangeMode.RESET) != null)
						Skript.error(what + " can't be deleted/cleared. It can however be reset which might result in the desired effect.", ErrorQuality.SEMANTIC_ERROR);
					else
						Skript.error(what + " can't be deleted/cleared", ErrorQuality.SEMANTIC_ERROR);
					break;
				case REMOVE_ALL:
					if (expressionToChange.acceptChange(ChangeMode.REMOVE) != null) {
						Skript.error(what + " can't have 'all of something' removed from it. Use 'remove' instead of 'remove all' to fix this.", ErrorQuality.SEMANTIC_ERROR);
						break;
					}
					//$FALL-THROUGH$
				case ADD:
				case REMOVE:
					Skript.error(what + " can't have anything " + (changeMode == ChangeMode.ADD ? "added to" : "removed from") + " it", ErrorQuality.SEMANTIC_ERROR);
					break;
				case RESET:
					if (expressionToChange.acceptChange(ChangeMode.DELETE) != null)
						Skript.error(what + " can't be reset. It can however be deleted which might result in the desired effect.", ErrorQuality.SEMANTIC_ERROR);
					else
						Skript.error(what + " can't be reset", ErrorQuality.SEMANTIC_ERROR);
			}
			return false;
		}
		
		final Class<?>[] acceptableChangeComponentTypes = new Class<?>[acceptableChangeTypes.length];
		for (int i = 0; i < acceptableChangeTypes.length; i++)
			acceptableChangeComponentTypes[i] = acceptableChangeTypes[i].isArray() ? acceptableChangeTypes[i].getComponentType() : acceptableChangeTypes[i];
		allowMultipleChangeValues = Arrays.equals(acceptableChangeTypes, acceptableChangeComponentTypes);

		if (deltaValuesExpression != null) {
			Expression<?> convertedChangeDeltaExpression = null;
			final ParseLogHandler parseLogs = SkriptLogger.startParseLogHandler();
			try {
				for (final Class<?> changeType : acceptableChangeTypes) {
					parseLogs.clear();
					Class<?> singleChangeType = changeType;
					if (changeType.isArray())
						singleChangeType = changeType.getComponentType();
					if (singleChangeType.isAssignableFrom(deltaValuesExpression.getReturnType())) {
						convertedChangeDeltaExpression = deltaValuesExpression.getConvertedExpression(Object.class);
						break; // break even if convertedChangeDeltaExpression == null as it won't convert to Object apparently
					}
				}
				if (convertedChangeDeltaExpression == null)
					convertedChangeDeltaExpression = deltaValuesExpression.getConvertedExpression((Class<Object>[]) acceptableChangeComponentTypes);
				if (convertedChangeDeltaExpression == null) {
					if (parseLogs.hasError()) {
						parseLogs.printError();
						return false;
					}
					parseLogs.clear();
					parseLogs.stop();
					if (acceptableChangeComponentTypes.length == 1 && acceptableChangeComponentTypes[0] == Object.class)
						Skript.error("Can't understand this expression: " + this.deltaValuesExpression, ErrorQuality.NOT_AN_EXPRESSION);
					else if (changeMode == ChangeMode.SET)
						Skript.error(what + " can't be set to " + this.deltaValuesExpression + " because the latter is " + SkriptParser.notOfType(acceptableChangeComponentTypes), ErrorQuality.SEMANTIC_ERROR);
					else
						Skript.error(this.deltaValuesExpression + " can't be " + (changeMode == ChangeMode.ADD ? "added to" : "removed from") + " " + what + " because the former is " + SkriptParser.notOfType(acceptableChangeComponentTypes), ErrorQuality.SEMANTIC_ERROR);
					parseLogs.printError();
					return false;
				}
				parseLogs.printLog();
			} finally {
				parseLogs.stop();
			}
			
			Class<?> superTypeOfAcceptableChangeTypes = Utils.getSuperType(acceptableChangeComponentTypes);
			for (int i = 0; i < acceptableChangeTypes.length; i++) {
				if (acceptableChangeComponentTypes[i].isAssignableFrom(convertedChangeDeltaExpression.getReturnType())) {
					allowMultipleChangeValues = !acceptableChangeTypes[i].isArray();
					superTypeOfAcceptableChangeTypes = acceptableChangeComponentTypes[i];
					break;
				}
			}
			assert superTypeOfAcceptableChangeTypes != null;
			deltaValuesExpression = convertedChangeDeltaExpression;

			if (!deltaValuesExpression.isSingle() && !allowMultipleChangeValues) {
				if (changeMode == ChangeMode.SET)
					Skript.error(expressionToChange + " can only be set to one " + Classes.getSuperClassInfo(superTypeOfAcceptableChangeTypes).getName() + ", not more", ErrorQuality.SEMANTIC_ERROR);
				else
					Skript.error("Only one " + Classes.getSuperClassInfo(superTypeOfAcceptableChangeTypes).getName() + " can be " + (changeMode == ChangeMode.ADD ? "added to" : "removed from") + " " + expressionToChange + ", not more", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}

			if (expressionToChange instanceof Variable && !expressionToChange.isSingle() && changeMode == ChangeMode.SET) {
				if (deltaValuesExpression instanceof ExprParse) {
					((ExprParse) deltaValuesExpression).flatten = false;
				} else if (deltaValuesExpression instanceof ExpressionList) {
					for (Expression<?> expression : ((ExpressionList<?>) deltaValuesExpression).getExpressions()) {
						if (expression instanceof ExprParse)
							((ExprParse) expression).flatten = false;
					}
				}
			}

			if (expressionToChange instanceof Variable && !((Variable<?>) expressionToChange).isLocal() && (changeMode == ChangeMode.SET || ((Variable<?>) expressionToChange).isList() && changeMode == ChangeMode.ADD)) {
				final ClassInfo<?> deltaReturnTypeClassInfo = Classes.getSuperClassInfo(deltaValuesExpression.getReturnType());
				if (deltaReturnTypeClassInfo.getC() != Object.class && deltaReturnTypeClassInfo.getSerializer() == null && deltaReturnTypeClassInfo.getSerializeAs() == null && !SkriptConfig.disableObjectCannotBeSavedWarnings.value()) {
					if (getParser().isActive() && !getParser().getCurrentScript().suppressesWarning(ScriptWarning.VARIABLE_SAVE)) {
						Skript.warning(deltaReturnTypeClassInfo.getName().withIndefiniteArticle() + " cannot be saved, i.e. the contents of the variable " + expressionToChange + " will be lost when the server stops.");
					}
				}
			}
		}
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		Object[] deltaValues = null;
		if (deltaValuesExpression != null) {
			deltaValues = deltaValuesExpression.getArray(event);
			deltaValues = deltaValuesExpression.beforeChange(expressionToChange, deltaValues);
		}

		boolean deltaValuesPresent = deltaValues != null && deltaValues.length != 0;
		boolean deltaValuesRequired = changeMode != ChangeMode.DELETE && changeMode != ChangeMode.RESET;
		if (deltaValuesRequired && !deltaValuesPresent) {
			boolean expressionToChangeAllowsDeletion = expressionToChange.acceptChange(ChangeMode.DELETE) != null;
			if (changeMode == ChangeMode.SET && expressionToChangeAllowsDeletion)
				expressionToChange.change(event, null, ChangeMode.DELETE);
			return;
		}
		expressionToChange.change(event, deltaValues, changeMode);
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		assert deltaValuesExpression != null;
		switch (changeMode) {
			case ADD:
				return "add " + deltaValuesExpression.toString(event, debug) + " to " + expressionToChange.toString(event, debug);
			case SET:
				return "set " + expressionToChange.toString(event, debug) + " to " + deltaValuesExpression.toString(event, debug);
			case REMOVE:
				return "remove " + deltaValuesExpression.toString(event, debug) + " from " + expressionToChange.toString(event, debug);
			case REMOVE_ALL:
				return "remove all " + deltaValuesExpression.toString(event, debug) + " from " + expressionToChange.toString(event, debug);
			case DELETE:
				return "delete/clear " + expressionToChange.toString(event, debug);
			case RESET:
				return "reset " + expressionToChange.toString(event, debug);
		}
		assert false;
		return "";
	}
	
}
