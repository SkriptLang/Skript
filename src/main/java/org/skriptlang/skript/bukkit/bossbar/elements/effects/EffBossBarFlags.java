package org.skriptlang.skript.bukkit.bossbar.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("unchecked")
@Name("Boss Bar Flags")
@Description("""
	Makes a boss bar have or not have a flag.
	The `play boss music` flag does not actually play any sound or do anything.
	However you can use a resource pack to play your own custom sound.
	""")
@Example("""
	broadcast "The boss has entered a new phase! Watch out for the fog..."
	make {_bossbar} create fog
	wait 30 seconds
	broadcast "The fog has subsided... for now"
	make {_bossbar} no longer create fog
	""")
@Since("INSERT VERSION")
public class EffBossBarFlags extends Effect {

	private static final Patterns<BarFlag> PATTERNS = new Patterns<>(new Object[][]{
		{"make %bossbars% [remove:(not|no longer)] darken the sky", BarFlag.DARKEN_SKY},
		{"stop %bossbar% from darkening the sky", BarFlag.DARKEN_SKY},
		{"make %bossbars% [remove:(not|no longer)] create fog", BarFlag.CREATE_FOG},
		{"stop %bossbar% from creating fog", BarFlag.CREATE_FOG},
		{"make %bossbars% [remove:(not|no longer)] play boss music", BarFlag.PLAY_BOSS_MUSIC},
		{"stop %bossbar% from playing [the] boss music", BarFlag.PLAY_BOSS_MUSIC},
	});

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffBossBarFlags.class)
				.addPatterns(PATTERNS.getPatterns())
				.supplier(EffBossBarFlags::new)
				.build()
		);
	}

	private Expression<BossBar> bars;
	private BarFlag flag;
	private boolean remove;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		bars = (Expression<BossBar>) expressions[0];
		remove = parseResult.hasTag("remove") || (matchedPattern % 2 == 1);
		flag = PATTERNS.getInfo(matchedPattern);
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (BossBar bar : bars.getArray(event)) {
			if (remove) {
				bar.removeFlag(flag);
			} else {
				bar.addFlag(flag);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", bars)
			.appendIf(remove, "no longer")
			.append(switch (flag) {
				case BarFlag.DARKEN_SKY -> "darken the sky";
				case BarFlag.CREATE_FOG -> "create fog";
				case BarFlag.PLAY_BOSS_MUSIC -> "play boss music";
			})
			.toString();
	}

}
