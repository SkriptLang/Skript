package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.TraderLlama;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class LlamaData extends EntityData<Llama> {

	public record LlamaState(Color color, boolean trader) {}

	private static final Color[] LLAMA_COLORS = Color.values();

	private static final EntityDataPatterns<LlamaState> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "llama¦s @a", new LlamaState(null, false), getPatterns("")),
		new PatternGroup<>(1, "creamy llama¦s @a", new LlamaState(Color.CREAMY, false), getPatterns("creamy")),
		new PatternGroup<>(2, "white llama¦s @a", new LlamaState(Color.WHITE, false), getPatterns("white")),
		new PatternGroup<>(3, "brown llama¦s @a", new LlamaState(Color.BROWN, false), getPatterns("brown")),
		new PatternGroup<>(4, "gray llama¦s @a", new LlamaState(Color.GRAY, false), getPatterns("gray")),
		new PatternGroup<>(5, "trader llama¦s @a", new LlamaState(null, true), getPatterns("trader")),
		new PatternGroup<>(6, "creamy trader llama¦s @a", new LlamaState(Color.CREAMY, true), getPatterns("creamy trader")),
		new PatternGroup<>(7, "white trader llama¦s @a", new LlamaState(Color.WHITE, true), getPatterns("white trader")),
		new PatternGroup<>(8, "brown trader llama¦s @a", new LlamaState(Color.BROWN, true), getPatterns("brown trader")),
		new PatternGroup<>(9, "gray trader llama¦s @a", new LlamaState(Color.GRAY, true), getPatterns("gray trader"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> llama[plural:s]";
		String second = "baby:llama cria[plural:s]";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " llama[plural:s]";
			second = "baby:" + prefix + " llama cria[plural:s]";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(LlamaData.class, "llama")
				.dataPatterns(GROUPS)
				.entityType(EntityType.LLAMA)
				.entityClass(Llama.class)
				.supplier(LlamaData::new)
				.build()
		);

		Variables.yggdrasil.registerSingleClass(Color.class, "Llama.Color");
	}

	private @Nullable Color color = null;
	private boolean isTrader;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color, boolean isTrader) {
		this.color = color;
		this.isTrader = isTrader;
		super.groupIndex = GROUPS.getIndex(new LlamaState(color, isTrader));
	}

	public LlamaData(@Nullable LlamaState llamaState) {
		if (llamaState != null) {
			this.color = llamaState.color;
			this.isTrader = llamaState.trader;
			super.groupIndex = GROUPS.getIndex(llamaState);
		} else {
			this.color = null;
			this.isTrader = false;
			super.groupIndex = GROUPS.getIndex(new LlamaState(null, false));
		}
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		LlamaState llamaState = GROUPS.getData(matchedGroup);
		assert llamaState != null;
		color = llamaState.color;
		isTrader = llamaState.trader;
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Llama> entityClass, @Nullable Llama llama) {
		if (entityClass != null)
			isTrader = TraderLlama.class.isAssignableFrom(entityClass);
		if (llama != null) {
			color = llama.getColor();
			isTrader = llama instanceof TraderLlama;
			super.groupIndex = GROUPS.getIndex(new LlamaState(color, isTrader));
		}
		return true;
	}
	
	@Override
	public void set(Llama llama) {
		Color color = this.color;
		if (color == null)
			color = CollectionUtils.getRandom(LLAMA_COLORS);
		assert color != null;
		llama.setColor(color);
	}
	
	@Override
	protected boolean match(Llama llama) {
		if (isTrader && !(llama instanceof TraderLlama))
			return false;
		return dataMatch(color, llama.getColor());
	}
	
	@Override
	public Class<? extends Llama> getType() {
		return isTrader ? TraderLlama.class : Llama.class;
	}
	
	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new LlamaData();
	}
	
	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(color);
		result = prime * result + (isTrader ? 1 : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof LlamaData other))
			return false;
		return isTrader == other.isTrader && other.color == color;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof LlamaData other))
			return false;

		if (isTrader && !other.isTrader)
			return false;
		return dataMatch(color, other.color);
	}
	
}
