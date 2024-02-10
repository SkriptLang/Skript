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

import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.bukkit.registration.BukkitInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.lang.structure.StructureInfo;

import java.util.Arrays;

/**
 * @deprecated Use {@link SyntaxInfo}
 * @param <E> the syntax element this info is for
 */
public class SyntaxElementInfo<E extends SyntaxElement> {

	// todo: 2.9 make all fields private
	public final Class<E> elementClass;
	public final String[] patterns;
	public final String originClassPath;

	public SyntaxElementInfo(String[] patterns, Class<E> elementClass, String originClassPath) throws IllegalArgumentException {
		this.patterns = patterns;
		this.elementClass = elementClass;
		this.originClassPath = originClassPath;
		try {
			elementClass.getConstructor();
		} catch (final NoSuchMethodException e) {
			// throwing an Exception throws an (empty) ExceptionInInitializerError instead, thus an Error is used
			throw new Error(elementClass + " does not have a public nullary constructor", e);
		} catch (final SecurityException e) {
			throw new IllegalStateException("Skript cannot run properly because a security manager is blocking it!");
		}
	}

	/**
	 * Get the class that represents this element.
	 * @return The Class of the element
	 */
	public Class<E> getElementClass() {
		return elementClass;
	}

	/**
	 * Get the patterns of this syntax element.
	 * @return Array of Skript patterns for this element
	 */
	public String[] getPatterns() {
		return Arrays.copyOf(patterns, patterns.length);
	}

	/**
	 * Get the original classpath for this element.
	 * @return The original ClassPath for this element
	 */
	public String getOriginClassPath() {
		return originClassPath;
	}
	
	@SuppressWarnings("unchecked")
	@Contract("_ -> new")
	public static <I extends SyntaxElementInfo<E>, E extends SyntaxElement> I fromModern(SyntaxInfo<? extends E> info) {
		if (info instanceof BukkitInfos.Event) {
			BukkitInfos.Event<?> event = (BukkitInfos.Event<?>) info;

			// We must first go back to the raw input
			String rawName = event.name().startsWith("On ")
					? event.name().substring(3)
					: "*" + event.name();
			SkriptEventInfo<?> eventInfo = new SkriptEventInfo<>(
					rawName, event.patterns().toArray(new String[0]),
					event.type(), event.origin().name(),
					(Class<? extends Event>[]) event.events().toArray(new Class<?>[0])
			).since(event.since())
					.documentationID(event.documentationId())
					.description(event.description().toArray(new String[0]))
					.examples(event.examples().toArray(new String[0]))
					.keywords(event.keywords().toArray(new String[0]))
					.requiredPlugins(event.requiredPlugins().toArray(new String[0]));

			return (I) eventInfo;
		} else if (info instanceof SyntaxInfo.Structure) {
			SyntaxInfo.Structure<?> structure = (SyntaxInfo.Structure<?>) info;
			return (I) new StructureInfo<>(structure.patterns().toArray(new String[0]), structure.type(),
					structure.origin().name(), structure.entryValidator());
		} else if (info instanceof SyntaxInfo.Expression) {
			return (I) fromModernExpression((SyntaxInfo.Expression<?, ?>) info);
		}
		
		return (I) new SyntaxElementInfo<>(info.patterns().toArray(new String[0]), info.type(), info.origin().name());
	}
	
	@Contract("_ -> new")
	private static <E extends Expression<R>, R> ExpressionInfo<E, R> fromModernExpression(SyntaxInfo.Expression<E, R> info) {
		return new ExpressionInfo<>(
				info.patterns().toArray(new String[0]), info.returnType(),
				info.type(), info.origin().name(), info.expressionType()
		);
	}
	
}
