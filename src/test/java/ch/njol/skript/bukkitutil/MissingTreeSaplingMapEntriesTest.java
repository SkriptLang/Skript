package ch.njol.skript.bukkitutil;

import org.bukkit.TreeType;
import org.junit.Assert;
import org.junit.Test;

public class MissingTreeSaplingMapEntriesTest {

	@Test
	public void test() {
		for (TreeType type : TreeType.values()) {
			Assert.assertNotNull("Tree type " + type + " has no mapped sapling in ItemUtils#getTreeSapling().", ItemUtils.getTreeSapling(type));
		}
	}

}
