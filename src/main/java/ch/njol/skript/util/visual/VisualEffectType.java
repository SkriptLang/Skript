package ch.njol.skript.util.visual;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

public class VisualEffectType {

	private static final String LANGUAGE_NODE = "visual effects";

	private final Enum<?> effect;

	private String pattern;
	private Noun name;

	private boolean colorable = false;
	private BiFunction<Object, Location, Object> dataSupplier = (o, location) -> null;

	private VisualEffectType(Enum<?> effect) {
		this.effect = effect;
	}

	public void setColorable() {
		colorable = true;
	}

	public boolean isColorable() {
		return colorable;
	}

	public void withData(BiFunction<Object, Location, Object> dataSupplier) {
		this.dataSupplier = dataSupplier;
	}

	@Nullable
	public Object getData(Object raw, Location location) {
		return dataSupplier.apply(raw, location);
	}

	public String getId() {
		return effect.getDeclaringClass().getSimpleName() + "." + effect.name();
	}

	public Noun getName() {
		return name;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isEffect() {
		return effect instanceof Effect;
	}

	public boolean isEntityEffect() {
		return effect instanceof EntityEffect;
	}

	public boolean isParticle() {
		return effect instanceof Particle;
	}

	public Effect getEffect() {
		if (!isEffect())
			throw new IllegalStateException();
		return (Effect) effect;
	}

	public EntityEffect getEntityEffect() {
		if (!isEntityEffect())
			throw new IllegalStateException();
		return (EntityEffect) effect;
	}

	public Particle getParticle() {
		if (!isParticle())
			throw new IllegalStateException();
		return (Particle) effect;
	}

	@Nullable
	static VisualEffectType of(Enum<?> effect) {
		Objects.requireNonNull(effect);

		VisualEffectType type = new VisualEffectType(effect);
		String node = LANGUAGE_NODE + "." + type.getId();

		if (!Language.keyExistsDefault(node + ".pattern"))
			return null;

		String pattern = Language.get(node + ".pattern");

		type.name = new Noun(node + ".name");

		String areaPattern = Language.get_(LANGUAGE_NODE + ".area_expression");
		type.pattern = pattern + " " + (areaPattern != null ? areaPattern : "");

		return type;
	}

}
