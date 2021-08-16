/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.update;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * Uses Github API to check for updates.
 */
public class GithubChecker implements UpdateChecker {
	
	/**
	 * Github API response for GSON deserialization.
	 */
	public static class ResponseEntry {
		public String url;
	    public String assets_url;
	    public String upload_url;
	    public String html_url;
	    public int id;
	    public String tag_name;
	    public String target_commitish;
	    public String name;
	    public boolean draft;
	    
	    public boolean prerelease;
	    public String created_at;
	    public String published_at;
	    
	    public static class AssetsEntry {
	    	public int size;
	    	public int download_count;
	    	public String browser_download_url;
	    }
	    
	    public List<AssetsEntry> assets;
	    public String body; // Description of release
	    
	    @Override
	    public String toString() {
	    	return tag_name;
	    }
	    
	    public static class Author {
	    	public String login;
	    	public int id;
	    }
	    
	    public Author author;
	}
	
	/**
	 * Used for deserializing Github API output.
	 */
	private final Gson gson;
	
	public GithubChecker() {
		this.gson = new Gson();
	}
	
	private List<ResponseEntry> deserialize(String str) {
		assert str != null : "Cannot deserialize null string";
		@SuppressWarnings("serial")
		Type listType = new TypeToken<List<ResponseEntry>>() {}.getType();
		List<ResponseEntry> responses = gson.fromJson(str, listType);
		assert responses != null;
		
		return responses;
	}

	@Override
	public CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel) {
		CompletableFuture<UpdateManifest> future = CompletableFuture.supplyAsync(() -> {
			URL url;
			try {
				url = new URL(manifest.updateSource);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			try (Scanner scan = new Scanner(url.openStream(), "UTF-8")) {
				// Get list of releases from Github API
				String out = scan.useDelimiter("\\A").next();
				assert out != null;
				return deserialize(out);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).thenApply((releases) -> {
			/**
			 * Latest release in the channel we're using.
			 */
			ResponseEntry latest = null;
			
			/**
			 * Current release, if found.
			 */
			ResponseEntry current = null;

			// Go through all releases, attempting to find current one
			for (ResponseEntry release : releases) {
				String name = release.tag_name;
				assert name != null;
				
				// Check if this is a suitable latest release
				if (latest == null && channel.check(name)) {
					latest = release;
				}
				
				// Check whether this is a current release
				if (manifest.id.equals(name)) {
					current = release;
					break; // Update can't be older than current release
				}
			}
			
			if (latest == null) {
				return null; // No updates for this channel available
			}
			
			if (current != null && latest.id == current.id) {
				return null; // Already running latest in this channel
			}
			
			// Validate the latest release
			if (latest.assets.isEmpty()) {
				return null; // Update not (yet?) downloadable
			}
			
			try {
				String name = latest.tag_name;
				assert name != null;
				String createdAt = latest.created_at;
				assert createdAt != null;
				String patchNotes = latest.body;
				assert patchNotes != null;
				URL download;
				if (manifest.downloadSource != null) {
					download = new URL(manifest.downloadSource);
				} else {
					download = new URL(latest.assets.get(0).browser_download_url);
				}
				
				return new UpdateManifest(name, createdAt, patchNotes, download);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		});
		assert future != null;
		return future;
	}
	
}
