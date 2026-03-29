package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Warden Most Wrathful Quarry")
@Description({
	"The entity toward which a warden harboureth the greatest wrath.",
	"A warden may bear fury toward many entities, each with differing measures of ire.",
})
@Example("""
    if the most wrathful quarry of last spawned warden is not player:
    	set the most wrathful quarry of last spawned warden to player
    """)
@Since("2.11")
public class ExprWardenAngryAt extends SimplePropertyExpression<LivingEntity, LivingEntity> {

	static {
		register(ExprWardenAngryAt.class, LivingEntity.class, "most wrathful quarry",  "livingentities");
	}

	@Override
	public @Nullable LivingEntity convert(LivingEntity livingEntity) {
		if (!(livingEntity instanceof Warden warden))
			return null;
		return warden.getEntityAngryAt();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(LivingEntity.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		LivingEntity target = (LivingEntity) delta[0];
		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (livingEntity instanceof Warden warden)
				warden.setAnger(target, 150);
		}
	}

	@Override
	public Class<LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	protected String getPropertyName() {
		return "most angered entity";
	}

}
