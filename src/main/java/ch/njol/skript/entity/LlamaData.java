package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.TraderLlama;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LlamaData extends EntityData<Llama> {

	private static final Color[] LLAMA_COLORS = Color.values();

	static {
		EntityData.register(LlamaData.class, "llama", Llama.class, 0,
			"llama", "creamy llama", "white llama", "brown llama", "gray llama",
			"trader llama", "creamy trader llama", "white trader llama", "brown trader llama", "gray trader llama");
	}

	private @Nullable Color color = null;
	private boolean isTrader;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color, boolean isTrader) {
		this.color = color;
		this.isTrader = isTrader;
		super.matchedPattern = (color != null ? (color.ordinal() + 1) : 0) + (isTrader ? 5 : 0);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		isTrader = matchedPattern > 4;
		if (matchedPattern > 5) {
			color = LLAMA_COLORS[matchedPattern - 6];
		} else if (matchedPattern > 0 && matchedPattern < 5) {
			color = LLAMA_COLORS[matchedPattern - 1];
		}
		// Sets 'matchedPattern' of 'EntityData' for proper 'toString'
		super.matchedPattern = (color != null ? (color.ordinal() + 1) : 0) + (isTrader ? 5 : 0);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Llama> entityClass, @Nullable Llama llama) {
		if (entityClass != null)
			isTrader = TraderLlama.class.isAssignableFrom(entityClass);
		if (llama != null) {
			color = llama.getColor();
			isTrader = llama instanceof TraderLlama;
		}
		return true;
	}
	
	@Override
	public void set(Llama entity) {
		Color randomColor = color == null ? CollectionUtils.getRandom(LLAMA_COLORS) : color;
		assert randomColor != null;
		entity.setColor(randomColor);
	}
	
	@Override
	protected boolean match(Llama entity) {
		if (isTrader && !(entity instanceof TraderLlama))
			return false;
		return color == null || color == entity.getColor();
	}
	
	@Override
	public Class<? extends Llama> getType() {
		return isTrader ? TraderLlama.class : Llama.class;
	}
	
	@Override
	public @NotNull EntityData getSuperType() {
		return new LlamaData(color, isTrader);
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		result = prime * result + (isTrader ? 1 : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof LlamaData other))
			return false;
		return isTrader == other.isTrader && other.color == color;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof LlamaData other))
			return false;

		if (isTrader && !other.isTrader)
			return false;
		return color == null || color == other.color;
	}
	
}
