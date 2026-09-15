package ch.njol.skript.variables;

/**
 * MySQL variable storage selected by {@code type: MySQL} inside {@code databases:}.
 * Uses the shared variable-name routing and serialization system. The inherited
 * backend manages the JDBC connection, transactions and recovery journal; it does
 * not monitor changes made by other servers.
 */
public class MySQLStorage extends PooledMySQLStorage {

	MySQLStorage(String type) {
		super(type);
	}

}
