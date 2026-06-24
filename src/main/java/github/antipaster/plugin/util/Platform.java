package github.antipaster.plugin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Platform {
	private Platform() {
	}

	public static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
	}

	public static List<String> ipcCandidates() {
		return isWindows() ? windowsPipes() : unixSockets();
	}

	private static List<String> windowsPipes() {
		List<String> paths = new ArrayList<>(10);
		for (int i = 0; i < 10; i++)
			paths.add("\\\\.\\pipe\\discord-ipc-" + i);
		return paths;
	}

	private static List<String> unixSockets() {
		List<String> paths = new ArrayList<>();
		for (String base : unixBaseDirectories())
			for (int i = 0; i < 10; i++)
				paths.add(base + "/discord-ipc-" + i);
		return paths;
	}

	private static List<String> unixBaseDirectories() {
		List<String> roots = new ArrayList<>();
		for (String key : List.of("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")) {
			String value = System.getenv(key);
			if (value != null && !value.isBlank())
				roots.add(stripTrailingSlash(value));
		}
		roots.add("/tmp");

		List<String> bases = new ArrayList<>();
		for (String root : roots) {
			bases.add(root);
			bases.add(root + "/app/com.discordapp.Discord");
			bases.add(root + "/snap.discord");
		}
		return bases;
	}

	private static String stripTrailingSlash(String path) {
		return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
	}
}
