package ch.njol.skript.lang;

public interface Unit extends Cloneable {

	int getAmount();

	void setAmount(double amount);

	@Override
	String toString();

	String toString(int flags);

	Unit clone();

}
