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
package ch.njol.skript.lang;

public class ExpressionInfo<E extends Expression<T>, T> extends SyntaxElementInfo<E> {

	private final ExpressionType expressionType;
	private final Class<T> returnType;

	public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> c, String originClassPath, ExpressionType expressionType) throws IllegalArgumentException {
		super(patterns, c, originClassPath);
		this.returnType = returnType;
		this.expressionType = expressionType;
	}

	/**
	 * Get the type of this expression.
	 * 
	 * @return The type of this Expression
	 */
	public ExpressionType getExpressionType() {
		return expressionType;
	}

	/**
	 * Get the return type of this expression.
	 * 
	 * @return The return type of this Expression
	 */
	public Class<T> getReturnType() {
		return returnType;
	}

}
