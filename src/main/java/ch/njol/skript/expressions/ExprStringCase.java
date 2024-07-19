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

import org.apache.commons.lang.WordUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Case Text")
@Description("Copy of given text in Lowercase, Uppercase, Proper Case, camelCase, PascalCase, Snake_Case, and Kebab-Case")
@Examples({"\"Oops!\" in lowercase # oops!",
	"\"oops!\" in uppercase # OOPS!",
	"\"hellO i'm steve!\" in proper case # HellO I'm Steve!",
	"\"hellO i'm steve!\" in strict proper case # Hello I'm Steve!",
	"\"spAwn neW boSs ()\" in camel case # spAwnNeWBoSs()",
	"\"spAwn neW boSs ()\" in strict camel case # spawnNewBoss()",
	"\"geneRate ranDom numBer ()\" in pascal case # GeneRateRanDomNumBer()",
	"\"geneRate ranDom numBer ()\" in strict pascal case # GenerateRandomNumber()",
	"\"Hello Player!\" in snake case # Hello_Player!",
	"\"Hello Player!\" in lower snake case # hello_player!",
	"\"Hello Player!\" in upper snake case # HELLO_PLAYER!",
	"\"What is your name?\" in kebab case # What-is-your-name?",
	"\"What is your name?\" in lower kebab case # what-is-your-name?",
	"\"What is your name?\" in upper kebab case # WHAT-IS-YOUR-NAME?"})
@Since("2.2-dev16 (lowercase and uppercase), 2.5 (advanced cases)")
public class ExprStringCase extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprStringCase.class, String.class, ExpressionType.SIMPLE,
				"%strings% in (0¦upper|1¦lower)[ ]case", 
				"(0¦upper|1¦lower)[ ]case %strings%",
				"capitali(s|z)ed %strings%",
				"%strings% in [(0¦lenient|1¦strict) ](proper|title)[ ]case",
				"[(0¦lenient|1¦strict) ](proper|title)[ ]case %strings%",
				"%strings% in [(0¦lenient|1¦strict) ]camel[ ]case",
				"[(0¦lenient|1¦strict) ]camel[ ]case %strings%",
				"%strings% in [(0¦lenient|1¦strict) ]pascal[ ]case",
				"[(0¦lenient|1¦strict) ]pascal[ ]case %strings%",
				"%strings% in [(1¦lower|2¦upper|3¦capital|4¦screaming)[ ]]snake[ ]case",
				"[(1¦lower|2¦upper|3¦capital|4¦screaming)[ ]]snake[ ]case %strings%",
				"%strings% in [(1¦lower|2¦upper|3¦capital)[ ]]kebab[ ]case",
				"[(1¦lower|2¦upper|3¦capital)[ ]]kebab[ ]case %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> expr;
	
	/* 0: No Change, 1: Upper Case, 2: Lower Case, 3: Strict */
	private int casemode = 0; // Defaults to No Change 
	
	/* 0: Basic Case Change, 1: Proper Case, 2: Camel Case, 3: Pascal Case, 4: Snake Case, 5: Kebab Case */
	private int type = 0; // Defaults to Basic Case Change 
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		expr = (Expression<String>) exprs[0];
		if (matchedPattern <= 1) { // Basic Case Change 
			casemode = (parseResult.mark == 0) ? 1 : 2;
		} else if (matchedPattern == 2) { // Basic Case Change 
			casemode = 1;
		} else if (matchedPattern <= 4) { // Proper Case 
			type = 1;
			if (parseResult.mark != 0)
				casemode = 3;
		} else if (matchedPattern <= 6) { // Camel Case 
			type = 2;
			if (parseResult.mark != 0)
				casemode = 3;
		} else if (matchedPattern <= 8) { // Pascal Case 
			type = 3;
			if (parseResult.mark != 0)
				casemode = 3;
		} else if (matchedPattern <= 10) { // Snake Case 
			type = 4;
			if (parseResult.mark != 0)
				casemode = (parseResult.mark == 1) ? 2 : 1;
		} else if (matchedPattern <= 12) { // Kebab Case 
			type = 5;
			if (parseResult.mark != 0)
				casemode = (parseResult.mark == 1) ? 2 : 1;
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected String[] get(Event e) {
		String[] strs = expr.getArray(e);
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] != null) {
				switch (type) {
					case 0: // Basic Case Change 
						strs[i] = (casemode == 1) ? strs[i].toUpperCase() : strs[i].toLowerCase();
						break;
					case 1: // Proper Case 
						strs[i] = (casemode == 3) ? WordUtils.capitalizeFully(strs[i]) : WordUtils.capitalize(strs[i]);
						break;
					case 2: // Camel Case 
						strs[i] = toCamelCase(strs[i], casemode == 3);
						break;
					case 3: // Pascal Case 
						strs[i] = toPascalCase(strs[i], casemode == 3);
						break;
					case 4: // Snake Case 
						strs[i] = toSnakeCase(strs[i], casemode);
						break;
					case 5: // Kebab Case 
						strs[i] = toKebabCase(strs[i], casemode);
						break;
				}
			}
		}
		return strs;
	}
	
	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String mode = "";
		switch (type) {
			case 0: // Basic Case Change 
				mode = (casemode == 1) ? "uppercase" : "lowercase";
				break;
			case 1: // Proper Case 
				mode = ((casemode == 3) ? "strict" : "lenient") + " proper case";
				break;
			case 2: // Camel Case 
				mode = ((casemode == 3) ? "strict" : "lenient") + " camel case";
				break;
			case 3: // Pascal Case 
				mode = ((casemode == 3) ? "strict" : "lenient") + " pascal case";
				break;
			case 4: // Snake Case 
				mode = ((casemode == 0) ? "" : ((casemode == 1)) ? "upper " : "lower ") + "snake case";
				break;
			case 5: // Kebab Case 
				mode = ((casemode == 0) ? "" : ((casemode == 1)) ? "upper " : "lower ") + "kebab case";
		}
		return mode + " " + expr.toString(event, debug);
	}
	
	@SuppressWarnings("null")
	private static String toCamelCase(String str, boolean strict) {
		String[] words = str.split(" "); // Splits at spaces 
		String buf = words.length > 0 ? (strict ? words[0].toLowerCase() : WordUtils.uncapitalize(words[0])) : "";
		for (int i = 1; i < words.length; i++)
			buf += strict ? WordUtils.capitalizeFully(words[i]) : WordUtils.capitalize(words[i]);
		return buf;
	}
	
	private static String toPascalCase(String str, boolean strict) {
		String[] words = str.split(" "); // Splits at spaces 
		String buf = "";
		for (int i = 0; i < words.length; i++)
			buf += strict ? WordUtils.capitalizeFully(words[i]) : WordUtils.capitalize(words[i]);
		return buf;
	}
	
	@SuppressWarnings("null")
	private static String toSnakeCase(String str, int mode) {
		if (mode == 0)
			return str.replace(' ', '_');
		StringBuilder sb = new StringBuilder();
		for (int c : (Iterable<Integer>) str.codePoints()::iterator) { // Handles Unicode ! 
			sb.appendCodePoint((c == ' ') ? '_' : ((mode == 1) ? Character.toUpperCase(c) : Character.toLowerCase(c)));
		}
		return sb.toString();
	}
	
	@SuppressWarnings("null")
	private static String toKebabCase(String str, int mode) {
		if (mode == 0)
			return str.replace(' ', '-');
		StringBuilder sb = new StringBuilder();
		for (int c : (Iterable<Integer>) str.codePoints()::iterator) { // Handles Unicode! 
			sb.appendCodePoint((c == ' ') ? '-' : ((mode == 1) ? Character.toUpperCase(c) : Character.toLowerCase(c)));
		}
		return sb.toString();
	}
	
}
