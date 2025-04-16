package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Panda Gene")
@Description("The main or hidden gene of a panda.")
@Example("""
	if the main gene of last spawned panda is lazy:
		set the main gene of last spawned panda to playful
	""")
@Since("2.11")
public class ExprPandaGene extends SimplePropertyExpression<LivingEntity, Gene> {

	static {
		register(ExprPandaGene.class, Gene.class, "(:main|hidden) gene[s]", "livingentities");
	}

	private boolean mainGene;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mainGene = parseResult.hasTag("main");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Gene convert(LivingEntity entity) {
		if (!(entity instanceof Panda panda))
			return null;
		return mainGene ? panda.getMainGene() : panda.getHiddenGene();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Gene.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		Gene gene = (Gene) delta[0];
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Panda panda))
				continue;
			if (mainGene) {
				panda.setMainGene(gene);
			} else {
				panda.setHiddenGene(gene);
			}
		}
	}

	@Override
	public Class<? extends Gene> getReturnType() {
		return Gene.class;
	}

	@Override
	protected String getPropertyName() {
		return (mainGene ? "main" : "hidden") + "gene";
	}

}
