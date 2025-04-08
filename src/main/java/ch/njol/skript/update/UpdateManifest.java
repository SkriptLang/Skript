package ch.njol.skript.update;

import java.net.URL;

/**
 * Returned by an update checker when an update is available.
 */
public class UpdateManifest {
	
	/**
	 * Release id, for example "2.3".
	 */
	public final String id;
	
	/**
	 * When the release was published.
	 */
	public final String date;
	
	/**
	 * Patch notes for the update.
	 */
	public final String patchNotes;
	
	/**
	 * Download URL for the update.
	 */
	public final URL downloadUrl;

	public UpdateManifest(String id, String date, String patchNotes, URL downloadUrl) {
		this.id = id;
		this.date = date;
		this.patchNotes = patchNotes;
		this.downloadUrl = downloadUrl;
	}
}
