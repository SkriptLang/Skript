package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Villager Profession")
@Description("Represents the profession of a villager/zombie villager.")
@Examples({
	"set {_p} to villager profession of event-entity",
	"villager profession of event-entity = nitwit profession",
	"set villager profession of {_villager} to librarian profession",
	"delete villager profession of event-entity"
})
@Since("INSERT VERSION")
public class ExprVillagerProfession extends SimplePropertyExpression<LivingEntity, Profession> {

	static {
		register(ExprVillagerProfession.class, Profession.class, "villager profession", "livingentities");
	}

	@Override
	public @Nullable Profession convert(LivingEntity from) {
		if (from instanceof Villager villager)
			return villager.getProfession();
		else if (from instanceof ZombieVillager zombie)
			return zombie.getVillagerProfession();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Profession.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Profession profession = delta != null && delta[0] instanceof Profession pro ? pro : Profession.NONE;

		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (livingEntity instanceof Villager villager)
				villager.setProfession(profession);
			else if (livingEntity instanceof ZombieVillager zombie)
				zombie.setVillagerProfession(profession);
		}
	}

	@Override
	protected String getPropertyName() {
		return "villager profession";
	}

	@Override
	public Class<? extends Profession> getReturnType() {
		return Profession.class;
	}

}
