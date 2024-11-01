package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operation;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Name("Metadata")
@Description("Metadata is a way to store temporary data on entities, blocks and more that disappears after a server restart.")
@Examples({
	"set metadata value \"healer\" of player to true",
	"broadcast \"%metadata value \"\"healer\"\" of player%\"",
	"clear metadata value \"healer\" of player"
})
@Since("2.2-dev36, INSERT VERSION (add, remove)")
public class ExprMetadata<T> extends SimpleExpression<T> {

	static {
		//noinspection unchecked
		Skript.registerExpression(ExprMetadata.class, Object.class, ExpressionType.PROPERTY,
				"metadata [(value|tag)[s]] %strings% of %metadataholders%",
				"%metadataholders%'[s] metadata [(value|tag)[s]] %string%"
		);
	}

	private final ExprMetadata<?> source;
	private final Class<? extends T>[] types;
	private final Class<T> superType;

	private @UnknownNullability Expression<String> keys;
	private @UnknownNullability Expression<Metadatable> holders;

	public ExprMetadata() {
		//noinspection unchecked
		this(null, (Class<? extends T>) Object.class);
	}

	@SafeVarargs
	private ExprMetadata(ExprMetadata<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.keys = source.keys;
			this.holders = source.holders;
		}
		this.types = types;
		//noinspection unchecked
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		holders = (Expression<Metadatable>) exprs[matchedPattern ^ 1];
		keys = (Expression<String>) exprs[matchedPattern];
		return true;
	}

	@Override
	protected T @Nullable [] get(Event event) {
		List<Object> values = new ArrayList<>();
		String[] keys = this.keys.getArray(event);
		for (Metadatable holder : holders.getArray(event)) {
			for (String key : keys) {
				List<MetadataValue> metadata = holder.getMetadata(key);
				if (!metadata.isEmpty())
					values.add(metadata.get(metadata.size() - 1).value()); // adds the most recent metadata value
			}
		}
		try {
			return Converters.convert(values.toArray(), types, superType);
		} catch (ClassCastException exception) {
			//noinspection unchecked
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE -> CollectionUtils.array(Object.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String[] keys = this.keys.getArray(event);
		for (Metadatable holder : holders.getArray(event)) {
			for (String key : keys) {
                switch (mode) {
                    case SET -> holder.setMetadata(key, new FixedMetadataValue(Skript.getInstance(), delta[0]));
					case ADD, REMOVE -> {
						assert delta != null;
						Operator operator = mode == ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
						List<MetadataValue> metadata = holder.getMetadata(key);
						Object value = metadata.isEmpty() ? null : metadata.get(metadata.size() - 1).value();
						OperationInfo<?, ?, ?> info;
                        if (value != null) {
                            info = Arithmetics.getOperationInfo(operator, value.getClass(), delta[0].getClass());
                            if (info == null)
                                continue;
                        } else {
                            info = Arithmetics.getOperationInfo(operator, delta[0].getClass(), delta[0].getClass());
                            if (info == null)
                                continue;
                            value = Arithmetics.getDefaultValue(info.getLeft());
                            if (value == null)
                                continue;
                        }
                        //noinspection unchecked,rawtypes
						Object newValue = ((Operation) info.getOperation()).calculate(value, delta[0]);
						holder.setMetadata(key, new FixedMetadataValue(Skript.getInstance(), newValue));
					}
                    case DELETE -> holder.removeMetadata(key, Skript.getInstance());
                }
			}
		}
	}

	@Override
	public boolean isSingle() {
		return holders.isSingle() && keys.isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Override
	@SafeVarargs
	public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprMetadata<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "metadata values " + keys.toString(event, debug) + " of " + holders.toString(event, debug);
	}

}
