package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

/**
 * Checks for updates
 */
public interface UpdateChecker {
	
	/**
	 * Checks for updates.
	 * @param manifest Manifest for release which is to be updated.
	 * @param releaseChannel Release channel to use.
	 * @return A future that will contain an update manifest, or null if
	 * there are no updates available currently.
	 */
	CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel);
}
