package ch.njol.skript.entity;

import org.bukkit.entity.Rabbit;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;


public class RabbitData extends EntityData<Rabbit> {
	
    static {
    	if(Skript.classExists("org.bukkit.entity.Rabbit")){
	        EntityData.register(RabbitData.class, "rabbit", Rabbit.class, 0, "rabbit", "black rabbit", "black and white rabbit",
	                "brown rabbit", "gold rabbit", "salt and pepper rabbit", "killer rabbit", "white rabbit");
    	}
    }

    private int type = 0;
    
    public RabbitData() {}
    
    public RabbitData(int type) {
    	this.type = type;
    	super.matchedPattern = type;
	}

    @Override
    protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
        type = matchedPattern;
        return true;
    }

    @SuppressWarnings("null")
	@Override
    protected boolean init(Class<? extends Rabbit> c, Rabbit rabbit) {
        type = (rabbit == null) ? 0 : intFromType(rabbit.getRabbitType());
        return true;
    }

    @Override
    public void set(Rabbit entity) {
        if (type != 0) 
        	entity.setRabbitType(typeFromInt(type));
    }

	@Override
    protected boolean match(Rabbit entity) {
        return type == 0 || intFromType(entity.getRabbitType()) == type;
    }

    @Override
    public Class<? extends Rabbit> getType() {
        return Rabbit.class;
    }

    @Override
    public EntityData getSuperType() {
        return new RabbitData(type);
    }

    @Override
    protected int hashCode_i() {
        return type;
    }

    @Override
    protected boolean equals_i(EntityData<?> obj) {
        if (!(obj instanceof RabbitData))
            return false;
        final RabbitData other = (RabbitData) obj;
        return type == other.type;
    }

    @Override
    public boolean isSupertypeOf(EntityData<?> e) {
        return e instanceof RabbitData && (type == 0 || ((RabbitData) e).type == type);
    }
    
	private static Rabbit.Type typeFromInt(int i){
    	switch(i){
    		case 1:
    			return Rabbit.Type.BLACK;
    		case 2:
    			return Rabbit.Type.BLACK_AND_WHITE;
    		case 3:
    			return Rabbit.Type.BROWN;
    		case 4:
    			return Rabbit.Type.GOLD;
    		case 5:
    			return Rabbit.Type.SALT_AND_PEPPER;
    		case 6:
    			return Rabbit.Type.THE_KILLER_BUNNY;
    		case 7:
    			return Rabbit.Type.WHITE;
    		default:
    			break;
    	}
    	return Rabbit.Type.BLACK;
    }
    
    private static int intFromType(Rabbit.Type type){
    	int i = 0;
    	switch(type){
			case BLACK:
				i = 1;
				break;
			case BLACK_AND_WHITE:
				i = 2;
				break;
			case BROWN:
				i = 3;
				break;
			case GOLD:
				i = 4;
				break;
			case SALT_AND_PEPPER:
				i = 5;
				break;
			case THE_KILLER_BUNNY:
				i = 6;
				break;
			case WHITE:
				i = 7;
				break;
			default:
				break;
    	}
    	return i;
    }

}
