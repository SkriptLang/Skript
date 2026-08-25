package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.registrations.Classes;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent.CommandSuggestion;
import org.skriptlang.skript.bukkit.command.elements.structures.util.ScriptSuggestionProvider;
import org.skriptlang.skript.bukkit.command.elements.structures.util.ScriptSuggestionProvider.FilteringMode;
import org.skriptlang.skript.bukkit.types.OfflinePlayerClassInfo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A custom argument for wrapping {@link ch.njol.skript.classes.ClassInfo}s with a {@link ch.njol.skript.classes.Parser}.
 * This is natively a {@link StringArgumentType} with conversion occurring during execution.
 * @param <T> The real type of the argument.
 */
@ApiStatus.Internal
public class ScriptArgumentType<T> implements CustomArgumentType.Converted<Object, String> {

	/**
	 * Data about an available native argument type.
	 * A native argument type is one that is recognized by the client, providing enhanced validation features.
	 * @param supportsPlural Whether this argument type supports returning multiple values.
	 * @param supportsRange Whether this argument type supports minimum or maximum values.
	 * @param mapper A function for obtaining the native argument type from an argument data.
	 */
	public record NativeArgumentData(
		boolean supportsPlural,
		boolean supportsRange,
		Function<ArgumentData<?>, ArgumentType<?>> mapper
	) {

		public NativeArgumentData {
			if (!supportsPlural) {
				final var inputMapper = mapper;
				mapper = data -> data.isSingle() ? inputMapper.apply(data) : null;
			}
		}

		public NativeArgumentData(Function<ArgumentData<?>, ArgumentType<?>> mapper) {
			this(false, false, mapper);
		}

	}

	/**
	 * Pre-defined mappings of types that are acceptable to map to other native argument types.
	 */
	private static final Map<Class<?>, NativeArgumentData> ARGUMENT_TYPE_MAPPINGS = Map.of(
		Boolean.class, new NativeArgumentData(ignored -> BoolArgumentType.bool()),
		Long.class, new NativeArgumentData(false, true, data -> {
			Long min = (Long) data.min();
			Long max = (Long) data.max();
			if (min == null) {
				if (max == null) {
					return LongArgumentType.longArg();
				}
				return LongArgumentType.longArg(Long.MIN_VALUE, max);
			} else if (max == null) {
				return LongArgumentType.longArg(min);
			}
			return LongArgumentType.longArg(min, max);
		}),
		Number.class, new NativeArgumentData(false, true, data -> {
			Number min = (Number) data.min();
			Number max = (Number) data.max();
			if (min == null) {
				if (max == null) {
					return DoubleArgumentType.doubleArg();
				}
				return DoubleArgumentType.doubleArg(Double.MIN_VALUE, max.doubleValue());
			} else if (max == null) {
				return DoubleArgumentType.doubleArg(min.doubleValue());
			}
			return DoubleArgumentType.doubleArg(min.doubleValue(), max.doubleValue());
		}),
		Player.class, new NativeArgumentData(true, false, data ->
			data.isSingle() ? ArgumentTypes.player() : ArgumentTypes.players()),
		Entity.class, new NativeArgumentData(true, false, data ->
			data.isSingle() ? ArgumentTypes.entity() : ArgumentTypes.entities()),
		GameMode.class, new NativeArgumentData(ignored -> ArgumentTypes.gameMode()),
		World.class, new NativeArgumentData(ignored -> ArgumentTypes.world()),
		UUID.class, new NativeArgumentData(ignored -> ArgumentTypes.uuid()),
		BlockData.class, new NativeArgumentData(ignored -> new Converted<BlockData, BlockState>() {
			@Override
			public @NotNull ArgumentType<BlockState> getNativeType() {
				return ArgumentTypes.blockState();
			}
			@Override
			public @NotNull BlockData convert(@NotNull BlockState blockState) {
				return blockState.getBlockData();
			}
		}),
		ItemStack.class, new NativeArgumentData(ignored -> ArgumentTypes.itemStack())
	);

	public static @Nullable NativeArgumentData getNativeData(ClassInfo<?> classInfo) {
		NativeArgumentData nativeArgumentData = ARGUMENT_TYPE_MAPPINGS.get(classInfo.getC());
		if (nativeArgumentData != null) {
			return nativeArgumentData;
		}
		if (classInfo instanceof RegistryClassInfo<?> registryClassInfo) {
			RegistryKey<?> key = registryClassInfo.registryKey();
			if (key != null) {
				return new NativeArgumentData(ignored -> ArgumentTypes.resource(key));
			}
		}
		return null;
	}

	private static final DynamicCommandExceptionType ERROR_PARSER_ERROR = new DynamicCommandExceptionType(
		input -> new LiteralMessage((String) input));

	private static final Dynamic2CommandExceptionType ERROR_INVALID_INPUT = new Dynamic2CommandExceptionType(
		(input, type) -> new LiteralMessage("'%s' is not a valid %s.".formatted(input, type)));

	protected final ArgumentData<T> argument;
	protected final StringArgumentType nativeType;

	public ScriptArgumentType(@NotNull ArgumentData<T> argument, @NotNull StringArgumentType nativeType) {
		this.argument = argument;
		this.nativeType = nativeType;
	}

	@Override
	public @NotNull Object convert(@NotNull String input) throws CommandSyntaxException {
		assert argument.type().getParser() != null;

		Literal<T> result;
		try (ParseLogHandler logHandler = new ParseLogHandler().start()) {
			//noinspection unchecked
			result = (Literal<T>) new SkriptParser(input, SkriptParser.PARSE_LITERALS, ParseContext.COMMAND)
				.parseExpression(argument.type().getC());
			if (result != null && argument.isSingle() && !result.canBeSingle()) { // provided many values but expected one
				result = null;
				Skript.error("Expected one " + argument.type().getName().getSingular() + " but got many.");
			}
			if (result == null) {
				if (logHandler.hasError()) {
					//noinspection ConstantConditions - getError is NotNull by hasError check
					throw ERROR_PARSER_ERROR.create(logHandler.getError().getMessage());
				} else {
					throw ERROR_INVALID_INPUT.create(input, argument.type().getName().getSingular());
				}
			}
		}

		return argument.isSingle() ? result.getSingle() : result.getArray();
	}

	@Override
	public @NotNull ArgumentType<String> getNativeType() {
		return nativeType;
	}

	/**
	 * A {@link ScriptArgumentType} that also provides custom suggestions using a {@link ClassInfo}'s {@link ClassInfo#getSupplier()}.
	 */
	public static class Suggesting<T> extends ScriptArgumentType<T> {

		public Suggesting(@NotNull ArgumentData<T> argument, @NotNull StringArgumentType nativeType) {
			super(argument, nativeType);
		}

		@Override
		public @NotNull Object convert(@NotNull String input) throws CommandSyntaxException {
			input = input.replace('_', ' ');
			return super.convert(input);
		}

		@Override
		public @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext context, @NotNull SuggestionsBuilder builder) {
			return listSuggestions(context, builder, FilteringMode.STARTS_WITH);
		}

		public @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<?> context, @NotNull SuggestionsBuilder builder,
		                                                               @NotNull FilteringMode filteringMode) {
			Supplier<Iterator<T>> supplier = argument.type().getSupplier();
			assert supplier != null;
			Iterator<T> iterator = supplier.get();
			while (iterator.hasNext()) {
				String suggestion = Classes.toString(iterator.next()).toLowerCase(Locale.ENGLISH)
					.replace(' ', '_');
				ScriptSuggestionProvider.suggest(builder, new CommandSuggestion(suggestion, null), filteringMode);
			}
			return builder.buildFuture();
		}

	}

	/**
	 * By returning an argument resolver, full resolution of a value can be delayed until it is actually needed.
	 * This is valuable for arguments that involve complex lookups (or whose values may not be known until execution actually occurs).
	 * @param <T> Result type of value being resolved.
	 */
	public interface ScriptArgumentResolver<T> {

		/**
		 * Resolves the input this resolver represents.
		 * @return One or more resolved values.
		 */
		T[] resolve();

	}

	/**
	 * A custom argument type for OfflinePlayer that also supports selectors.
	 * May return an {@link OfflinePlayer} or
	 *  {@link io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver}.
	 */
	public static final class OfflinePlayerArgument implements CustomArgumentType<Object, Object> {

		private record OfflinePlayerResolver(String[] inputs) implements ScriptArgumentResolver<OfflinePlayer> {

			@Override
			public OfflinePlayer[] resolve() {
				return Arrays.stream(inputs)
					.map(OfflinePlayerClassInfo::parseValidated)
					.toArray(OfflinePlayer[]::new);
			}

		}

		private final StringArgumentType nativeType;
		private final ArgumentType<?> playerType;
		private final boolean isSingle;

		public OfflinePlayerArgument(@NotNull ArgumentData<OfflinePlayer> argument, @NotNull StringArgumentType nativeType) {
			this.nativeType = nativeType;
			if (argument.isSingle()) {
				playerType = ArgumentTypes.player();
				isSingle = true;
			} else {
				playerType = ArgumentTypes.players();
				isSingle = false;
			}
		}

		@Override
		public @NotNull Object parse(@NotNull StringReader reader) throws CommandSyntaxException {
			throw new UnsupportedOperationException("This method will never be called.");
		}

		@Override
		public <S> @NotNull Object parse(StringReader reader, @NotNull S source) throws CommandSyntaxException {
			int cursor = reader.getCursor();
			try {
				String fullInput = nativeType.parse(reader, source);
				String[] inputs = null;
				if (isSingle) {
					inputs = new String[]{fullInput};
				} else {
					try (ParseLogHandler logHandler = new ParseLogHandler().start()) {
						//noinspection unchecked
						Literal<String> literal = (Literal<String>) new SkriptParser(fullInput, SkriptParser.PARSE_LITERALS, ParseContext.COMMAND)
							.parseExpressionList(logHandler, String.class);
						if (literal != null) {
							inputs = literal.getArray();
						}
					}
				}
				if (inputs != null) {
					for (String input : inputs) {
						if (!OfflinePlayerClassInfo.isValidInput(input)) {
							inputs = null;
							break;
						}
					}
					if (inputs != null) {
						return new OfflinePlayerResolver(inputs);
					}
				}
			} catch (CommandSyntaxException ignored) { }
			reader.setCursor(cursor);
			return playerType.parse(reader, source);
		}

		@Override
		public @NotNull ArgumentType<Object> getNativeType() {
			//noinspection unchecked
			return (ArgumentType<Object>) (isSingle ? playerType : nativeType);
		}

	}

}
