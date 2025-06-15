package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.TraderLlama;
import org.jetbrains.annotations.Nullable;

public class LlamaData<E extends Llama> extends EntityData<E> {

	static {
		//noinspection unchecked
		EntityData.register(LlamaData.class, "llama", Llama.class, 0,
			"llama", "creamy llama",
			"white llama", "brown llama", "gray llama");
	}

	private @Nullable Color color = null;
	
	public LlamaData() {}
	
	public LlamaData(@Nullable Color color) {
		this.color = color;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0 && matchedPattern < 5)
			color = Color.values()[matchedPattern - 1];
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends E> entityClass, @Nullable E llama) {
		if (llama != null)
			color = llama.getColor();
		return true;
	}
	
	@Override
	public void set(E entity) {
		Color randomColor = color == null ? CollectionUtils.getRandom(Color.values()) : color;
		assert randomColor != null;
		entity.setColor(randomColor);
	}
	
	@Override
	protected boolean match(E entity) {
		return color == null || color == entity.getColor();
	}
	
	@Override
	public Class<E> getType() {
		//noinspection unchecked
		return (Class<E>) Llama.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new LlamaData(color);
	}
	
	@Override
	protected int hashCode_i() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color != null ? color.hashCode() : 0);
		return result;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof LlamaData other))
			return false;
		return other.color == color;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof LlamaData other))
			return false;
		return color == null || other.color == color;
	}

	public @Nullable Color getColor() {
		return color;
	}

	public static class TraderLlamaData extends LlamaData<TraderLlama> {

		static {
			EntityData.register(TraderLlamaData.class, "trader llama", TraderLlama.class, 0,
				"trader llama", "creamy trader llama", "white trader llama", "brown trader llama", "gray trader llama");
		}

		public TraderLlamaData() {
			super();
		}

		public TraderLlamaData(@Nullable Color color) {
			super(color);
		}

		@Override
		public Class<TraderLlama> getType() {
			return TraderLlama.class;
		}

		@Override
		public EntityData getSuperType() {
			return new TraderLlamaData();
		}

		@Override
		protected int hashCode_i() {
			return super.hashCode_i() + 1;
		}

		@Override
		protected boolean equals_i(EntityData<?> data) {
			if (!(data instanceof TraderLlamaData other))
				return false;
			return super.equals_i(data);
		}

		@Override
		public boolean isSupertypeOf(EntityData<?> data) {
			if (!(data instanceof TraderLlamaData other))
				return false;
			return super.isSupertypeOf(data);
		}
	}
	
}
