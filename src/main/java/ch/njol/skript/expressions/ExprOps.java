package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("All Ops")
@Description("The list of operators on this server.")
@Examples("set {_ops::*} to all ops")
@Since("INSERT VERSION")

public class ExprOps extends SimpleExpression<OfflinePlayer> {
    static {
        Skript.registerExpression(ExprOps.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[all [[of] the]|the] [server] op[erator]s");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected OfflinePlayer[] get(Event e) {
        return Bukkit.getOperators().toArray(new OfflinePlayer[0]);
    }

    @Nullable
    @Override
    public Class<?>[] acceptChange(ChangeMode mode) {
        switch (mode) {
            case ADD:
            case SET:
            case REMOVE:
            case RESET:
            case DELETE:
                return CollectionUtils.array(OfflinePlayer[].class);
        }
        return null;
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
        switch (mode) {
            case SET:
                for (OfflinePlayer p : Bukkit.getOperators())
                    p.setOp(false);
            case ADD:
                for (Object p : delta)
                    ((OfflinePlayer) p).setOp(true);
                break;
            case REMOVE:
                for (Object p : delta)
                    ((OfflinePlayer) p).setOp(false);
                break;
            case DELETE:
            case RESET:
                for (OfflinePlayer p : Bukkit.getOperators())
                    p.setOp(false);
                break;
            default:
                assert false;
        }
    }


    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends OfflinePlayer> getReturnType() {
        return OfflinePlayer.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "all ops";
    }
}
