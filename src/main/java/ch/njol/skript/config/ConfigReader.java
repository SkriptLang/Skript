package ch.njol.skript.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class ConfigReader extends BufferedReader {
	
	@SuppressWarnings("null")
	public final static Charset UTF_8 = Charset.forName("UTF-8");
	
	@Nullable
	private String line;
	private boolean reset = false;
	private int ln = 0;
	
	private boolean hasNonEmptyLine = false;
	
	public ConfigReader(final InputStream source) {
		super(new InputStreamReader(source, UTF_8));
	}
	
	@Override
	@Nullable
	public String readLine() throws IOException {
		if (reset) {
			reset = false;
		} else {
			line = stripUTF8BOM(super.readLine());
			ln++;
		}
		return line;
	}
	
	@Nullable
	private final String stripUTF8BOM(final @Nullable String line) {
		if (!hasNonEmptyLine && line != null && !line.isEmpty()) {
			hasNonEmptyLine = true;
			if (line.startsWith("\uFEFF")) {
				return line.substring(1);
			}
		}
		return line;
	}
	
	@Override
	public void reset() {
		if (reset)
			throw new IllegalStateException("reset was called twice without a readLine inbetween");
		reset = true;
	}
	
	public int getLineNum() {
		return ln;
	}
	
	@Nullable
	public String getLine() {
		return line;
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public void mark(final int readAheadLimit) throws IOException {
		throw new UnsupportedOperationException();
	}
	
}
