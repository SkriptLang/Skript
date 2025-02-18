package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

/**
 * An update checker that never reports available updates.
 */
public class NoUpdateChecker implements UpdateChecker {

	@SuppressWarnings("null")
	@Override
	public CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel) {
		return CompletableFuture.completedFuture(null);
	}
	
}
