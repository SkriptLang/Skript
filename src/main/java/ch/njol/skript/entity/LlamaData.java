package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.TraderLlama;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LlamaData extends EntityData<Llama> {

	private static final EntityPatterns<Color> PATTERNS =  new EntityPatterns<>(new Object[][]{
		{"llama", null},
		{"creamy llama", Color.CREAMY},
		{"white llama", Color.WHITE},
		{"brown llama", Color.BROWN},
		{"gray llama", Color.GRAY},
		{"trader llama", null},
		{"creamy trader llama", Color.CREAMY},
		{"white trader llama", Color.WHITE},
		{"brown trader llama", Color.BROWN},
		{"gray trader llama", Color.GRAY}
	});
	private static final Color[] LLAMA_COLORS = Color.values();

	static {
		EntityData.register(LlamaData.class, "llama", Llama.class, 0, PATTERNS.getPatterns());
	}

	private @Nullable Color color = null;
	private boolean isTrader;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color, boolean isTrader) {
		this.color = color;
		this.isTrader = isTrader;
		super.dataCodeName = PATTERNS.getMatchedPatterns(color)[!isTrader ? 0 : 1];
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		isTrader = matchedCodeName > 4;
		color = PATTERNS.getInfo(matchedCodeName);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Llama> entityClass, @Nullable Llama llama) {
		if (entityClass != null)
			isTrader = TraderLlama.class.isAssignableFrom(entityClass);
		if (llama != null) {
			color = llama.getColor();
			isTrader = llama instanceof TraderLlama;
			super.dataCodeName = PATTERNS.getMatchedPatterns(color)[!isTrader ? 0 : 1];
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
		return color == null || color == llama.getColor();
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
		result = prime * result + (color != null ? Objects.hashCode(color) : 0);
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
		return color == null || color == other.color;
	}
	
}
