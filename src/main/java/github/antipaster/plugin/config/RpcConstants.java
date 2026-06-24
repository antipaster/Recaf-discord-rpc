package github.antipaster.plugin.config;

import java.time.Duration;

public final class RpcConstants {
	public static final String APPLICATION_ID = "1519244659799359618";

	public static final String LOGO_KEY = "recaf";
	public static final String LOGO_TEXT = "Recaf";

	public static final String IDLE_DETAILS = "Idle";
	public static final String IDLE_STATE = "No workspace open";

	public static final Duration REFRESH_INTERVAL = Duration.ofSeconds(15);

	private RpcConstants() {
	}
}
