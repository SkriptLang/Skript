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
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Metadata")
@Description("Metadata is a way to store temporary data on entities, blocks and more that " +
		"disappears after a server restart.")
@Examples({"set metadata value \"healer\" of player to true",
		"broadcast \"%metadata value \"\"healer\"\" of player%\"",
		"clear metadata value \"healer\" of player"})
@Since("2.2-dev36")
@SuppressWarnings({"unchecked", "null"})
public class ExprMetadata<T> extends SimpleExpression<T> {

	static {
		Skript.registerExpression(ExprMetadata.class, Object.class, ExpressionType.PROPERTY,
				"metadata [(value|tag)[s]] %strings% of %metadataholders%",
				"%metadataholders%'[s] metadata [(value|tag)[s]] %string%"
		);
	}

	private ExprMetadata<?> source;
	@Nullable
	private Expression<String> values;
	@Nullable
	private Expression<Metadatable> holders;
	private Class<? extends T>[] types;
	private Class<T> superType;

	public ExprMetadata() {
		this(null, (Class<? extends T>) Object.class);
	}

	private ExprMetadata(ExprMetadata<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.values = source.values;
			this.holders = source.holders;
		}
		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		holders = (Expression<Metadatable>) exprs[matchedPattern ^ 1];
		values = (Expression<String>) exprs[matchedPattern];
		return true;
	}

	@Override
	@Nullable
	protected T[] get(Event e) {
		List<Object> values = new ArrayList<>();
		for (String value : this.values.getArray(e)) {
			for (Metadatable holder : holders.getArray(e)) {
				List<MetadataValue> metadata = holder.getMetadata(value);
				if (!metadata.isEmpty())
					values.add(metadata.get(metadata.size() - 1).value()); // adds the most recent metadata value
			}
		}
		try {
			return Converters.convert(values.toArray(), types, superType);
		} catch (ClassCastException e1) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.SET)
			return CollectionUtils.array(Object.class);
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		for (String value : values.getArray(e)) {
			for (Metadatable holder : holders.getArray(e)) {
				switch (mode) {
					case SET:
						holder.setMetadata(value, new FixedMetadataValue(Skript.getInstance(), delta[0]));
						break;
					case DELETE:
						holder.removeMetadata(value, Skript.getInstance());
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return holders.isSingle() && values.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprMetadata<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "metadata values " + values.toString(e, debug) + " of " + holders.toString(e, debug);
	}

}