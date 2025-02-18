package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.Nullable;

@Name("Arrow Attached Block")
@Description("Returns the attached block of an arrow.")
@Examples("set hit block of last shot arrow to diamond block")
@Since("2.8.0")
public class ExprAttachedBlock extends SimplePropertyExpression<Projectile, Block> {

	private static final boolean HAS_ABSTRACT_ARROW = Skript.classExists("org.bukkit.entity.AbstractArrow");

	static {
		register(ExprAttachedBlock.class, Block.class, "(attached|hit) block", "projectiles");
	}

	@Override
	@Nullable
	public Block convert(Projectile projectile) {
		if (HAS_ABSTRACT_ARROW) {
			if (projectile instanceof AbstractArrow) {
				return ((AbstractArrow) projectile).getAttachedBlock();
			}
		} else if (projectile instanceof Arrow) {
			return ((Arrow) projectile).getAttachedBlock();
		}
		return null;
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String getPropertyName() {
		return "attached block";
	}

}
