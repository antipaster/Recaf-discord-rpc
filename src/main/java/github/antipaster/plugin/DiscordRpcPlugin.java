package github.antipaster.plugin;

import github.antipaster.plugin.presence.PresenceController;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.plugin.Plugin;
import software.coley.recaf.plugin.PluginInformation;
import software.coley.recaf.services.workspace.WorkspaceManager;
import software.coley.recaf.ui.docking.DockingManager;

@Dependent
@PluginInformation(id = "##ID##", version = "##VERSION##", name = "##NAME##", description = "##DESC##")
public class DiscordRpcPlugin implements Plugin {
	private static final Logger logger = Logging.get(DiscordRpcPlugin.class);

	private final PresenceController controller;

	@Inject
	public DiscordRpcPlugin(WorkspaceManager workspaceManager, Instance<DockingManager> dockingManager) {
		this.controller = new PresenceController(workspaceManager, dockingManager);
	}

	@Override
	public void onEnable() {
		controller.start();
		logger.info("Discord RPC plugin enabled");
	}

	@Override
	public void onDisable() {
		controller.stop();
		logger.info("Discord RPC plugin disabled");
	}
}
