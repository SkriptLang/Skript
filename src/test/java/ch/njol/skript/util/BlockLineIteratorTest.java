package ch.njol.skript.util;

import org.bukkit.Location;
import org.joml.Vector3d;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class BlockLineIteratorTest {

	@Test
	public void testStepsToNextFace() {
		Vector3d start = new Vector3d(0,0,0);
		Vector3d center = new Vector3d(0.5,0.5,0.5);
		Vector3d step = new Vector3d(0,0,0);
		assertEquals(Double.POSITIVE_INFINITY, BlockLineIterator.stepsToNextFace(start, step, center), Math.ulp(1d));
		step.x = 1;
		assertEquals(1.0, BlockLineIterator.stepsToNextFace(start, step, center), Math.ulp(1d));
		step.y = 1;
		step.normalize();
		assertEquals(Math.sqrt(2), BlockLineIterator.stepsToNextFace(start, step, center), Math.ulp(1d));
		step.x = 1;
		step.y = 1;
		step.z = 1;
		step.normalize();
		assertEquals(Math.sqrt(3), BlockLineIterator.stepsToNextFace(start, step, center), Math.ulp(1d));
		start.x = 0.99;
		assertEquals(Math.sqrt(3) / 100, BlockLineIterator.stepsToNextFace(start, step, center), Math.ulp(1d));
	}

	@Test
	public void testOvershoot() {
		Location start = new Location(null, 0,0,0);
		Location end = new Location(null, 0,10,0);
		var iterator = new BlockLineIterator(start, end);
		iterator.step.mul(-1);
		// step is now wrong way, so it should trigger overshoot protection
		Assert.assertThrows(NoSuchElementException.class, iterator::next);
	}

}
