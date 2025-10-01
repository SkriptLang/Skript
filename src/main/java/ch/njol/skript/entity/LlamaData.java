package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.TraderLlama;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LlamaData extends EntityData<Llama> {

	public record LlamaState(Color color, boolean trader) {}

	private static final Patterns<LlamaState> PATTERNS =  new Patterns<>(new Object[][]{
		{"llama", new LlamaState(null, false)},
		{"creamy llama", new LlamaState(Color.CREAMY, false)},
		{"white llama", new LlamaState(Color.WHITE, false)},
		{"brown llama", new LlamaState(Color.BROWN, false)},
		{"gray llama", new LlamaState(Color.GRAY, false)},
		{"trader llama", new LlamaState(null, true)},
		{"creamy trader llama", new LlamaState(Color.CREAMY, true)},
		{"white trader llama", new LlamaState(Color.WHITE, true)},
		{"brown trader llama", new LlamaState(Color.BROWN, true)},
		{"gray trader llama", new LlamaState(Color.GRAY, true)}
	});
	private static final Color[] LLAMA_COLORS = Color.values();

	static {
		EntityData.register(LlamaData.class, "llama", Llama.class, 0, PATTERNS.getPatterns());

		Variables.yggdrasil.registerSingleClass(Color.class, "Llama.Color");
	}

	private @Nullable Color color = null;
	private boolean isTrader;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color, boolean isTrader) {
		this.color = color;
		this.isTrader = isTrader;
		super.codeNameIndex = PATTERNS.getMatchedPattern(new LlamaState(color, isTrader), 0).orElse(0);
	}

	public LlamaData(@Nullable LlamaState llamaState) {
		if (llamaState != null) {
			this.color = llamaState.color;
			this.isTrader = llamaState.trader;
			super.codeNameIndex = PATTERNS.getMatchedPattern(llamaState, 0).orElse(0);
		} else {
			this.color = null;
			this.isTrader = false;
			super.codeNameIndex = PATTERNS.getMatchedPattern(new LlamaState(this.color, this.isTrader), 0).orElse(0);
		}
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		LlamaState llamaState = PATTERNS.getInfo(matchedCodeName);
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
			super.codeNameIndex = PATTERNS.getMatchedPattern(new LlamaState(color, isTrader), 0).orElse(0);
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
