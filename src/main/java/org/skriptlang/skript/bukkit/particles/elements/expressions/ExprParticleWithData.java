package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.bukkit.particles.registration.DataParticles;
import org.skriptlang.skript.bukkit.particles.registration.EffectInfo;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

import static org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder;

@Name("Particles with Data")
@Description("""
	Creates particles that require some extra information, such as colors, locations, or block data.
	Particles not present here do not require data and can be found in the Particle type.
	Data requirements vary from version to version, so these docs are only accurate for the most recent Minecraft \
	version at time of release.
	For example, between 1.21.8 and 1.21.9, the 'flash' particle became colourable and now requires a colour data.
	""")
@Example("set {blood-effect} to a red dust particle of size 1")
@Example("draw 3 blue trail particles moving to player's target over 3 seconds at player")
@Since("INSERT VERSION")
public class ExprParticleWithData extends SimpleExpression<ParticleEffect> {

	private static Patterns<EffectInfo<Particle, Object>> PATTERNS;

	public static void register(@NotNull SyntaxRegistry registry, @NotNull Origin origin) {
		// create Patterns object
		Object[][] patterns = new Object[DataParticles.getParticleInfos().size()][2];
		int i = 0;
		for (var particleInfo : DataParticles.getParticleInfos()) {
			patterns[i][0] = "[%-*number%|a[n]] " + particleInfo.pattern();
			patterns[i][1] = particleInfo;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		registry.register(SyntaxRegistry.EXPRESSION, builder(ExprParticleWithData.class, ParticleEffect.class)
			.addPatterns(PATTERNS.getPatterns())
			.supplier(ExprParticleWithData::new)
			.priority(SyntaxInfo.COMBINED)
			.origin(origin)
			.build());
	}

	private ParseResult parseResult;
	private Expression<?>[] expressions;
	private EffectInfo<Particle, Object> effectInfo;
	private Expression<Number> count;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.parseResult = parseResult;
		// exclude count expr
		this.expressions = Arrays.copyOfRange(expressions, 1, expressions.length);
		this.count = (Expression<Number>) expressions[0];
		effectInfo = PATTERNS.getInfo(matchedPattern);
		return effectInfo != null;
	}

	@Override
	protected ParticleEffect @Nullable [] get(Event event) {
		Object data = effectInfo.dataSupplier().getData(event, expressions, parseResult);
		if (data == null) {
			error("Could not obtain required data for " + ParticleEffect.toString(effectInfo.effect(), 0));
			return null;
		}
		ParticleEffect effect = ParticleEffect.of(effectInfo.effect());
		effect.data(data);
		if (this.count != null) {
			Number count = this.count.getSingle(event);
			if (count != null) {
				effect.count(Math.clamp(count.intValue(), 0, 16_384)); // drawing more than the maximum display count of 16,384 is likely unintended and can crash users.
			} else {
				warning("The 'count' value for the '" + Classes.toString(effect) + "' particle was either not set or not a number (" + this.count.toString(event, false) + "); defaulting to 1.");
			}
		}
		return new ParticleEffect[] {effect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ParticleEffect> getReturnType() {
		return ParticleEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder ssb = new SyntaxStringBuilder(event, debug);
		if (count != null)
			ssb.append(count);
		return effectInfo.toStringFunction().toString(expressions, parseResult, ssb).toString();
	}

}
