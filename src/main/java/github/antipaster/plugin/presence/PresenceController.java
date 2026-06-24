package github.antipaster.plugin.presence;

import github.antipaster.plugin.config.RpcConstants;
import github.antipaster.plugin.rpc.DiscordIpcClient;
import github.antipaster.plugin.rpc.RichPresence;
import jakarta.enterprise.inject.Instance;
import javafx.scene.Node;
import software.coley.bentofx.dockable.Dockable;
import software.coley.bentofx.dockable.DockableCloseListener;
import software.coley.bentofx.dockable.DockableSelectListener;
import software.coley.bentofx.event.EventBus;
import software.coley.bentofx.path.DockablePath;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.path.ClassMemberPathNode;
import software.coley.recaf.path.PathNode;
import software.coley.recaf.services.navigation.ClassNavigable;
import software.coley.recaf.services.workspace.WorkspaceCloseListener;
import software.coley.recaf.services.workspace.WorkspaceManager;
import software.coley.recaf.services.workspace.WorkspaceOpenListener;
import software.coley.recaf.ui.docking.DockingManager;
import software.coley.recaf.ui.pane.editing.assembler.AssemblerPane;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.workspace.model.Workspace;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PresenceController {
	private final WorkspaceManager workspaceManager;
	private final Instance<DockingManager> dockingManager;
	private final DiscordIpcClient client;
	private final PresenceFactory factory;

	private final WorkspaceOpenListener workspaceOpenListener = this::onWorkspaceOpened;
	private final WorkspaceCloseListener workspaceCloseListener = this::onWorkspaceClosed;
	private final DockableSelectListener dockableSelectListener = this::onDockableSelected;
	private final DockableCloseListener dockableCloseListener = this::onDockableClosed;

	private volatile EventBus eventBus;
	private volatile Node shownNode;
	private volatile ScheduledExecutorService scheduler;
	private volatile RichPresence current;
	private String lastSentJson;

	public PresenceController(WorkspaceManager workspaceManager, Instance<DockingManager> dockingManager) {
		this.workspaceManager = workspaceManager;
		this.dockingManager = dockingManager;
		this.client = new DiscordIpcClient(RpcConstants.APPLICATION_ID);
		this.factory = new PresenceFactory(appStartEpochSeconds());
	}

	public void start() {
		current = workspaceOrIdle();
		workspaceManager.addWorkspaceOpenListener(workspaceOpenListener);
		workspaceManager.addWorkspaceCloseListener(workspaceCloseListener);
		FxThreadUtil.run(() -> {
			eventBus = dockingManager.get().getBento().events();
			eventBus.addDockableSelectListener(dockableSelectListener);
			eventBus.addDockableCloseListener(dockableCloseListener);
		});
		scheduler = Executors.newSingleThreadScheduledExecutor(PresenceController::newThread);
		scheduler.scheduleWithFixedDelay(() -> push(true), 0, RpcConstants.REFRESH_INTERVAL.toSeconds(), TimeUnit.SECONDS);
	}

	public void stop() {
		workspaceManager.removeWorkspaceOpenListener(workspaceOpenListener);
		workspaceManager.removeWorkspaceCloseListener(workspaceCloseListener);
		FxThreadUtil.run(() -> {
			if (eventBus != null) {
				eventBus.removeDockableSelectListener(dockableSelectListener);
				eventBus.removeDockableCloseListener(dockableCloseListener);
			}
		});
		if (scheduler != null)
			scheduler.shutdownNow();
		client.close();
	}

	private void onWorkspaceOpened(Workspace workspace) {
		shownNode = null;
		update(factory.forWorkspace(workspace));
	}

	private void onWorkspaceClosed(Workspace workspace) {
		shownNode = null;
		update(factory.idle());
	}

	private void onDockableSelected(DockablePath path, Dockable dockable) {
		Node node = dockable.getNode();
		shownNode = node;
		update(presenceFor(node));
	}

	private void onDockableClosed(DockablePath path, Dockable dockable) {
		if (dockable.getNode() == shownNode) {
			shownNode = null;
			update(workspaceOrIdle());
		}
	}

	private RichPresence presenceFor(Node node) {
		if (node instanceof AssemblerPane assembler) {
			ClassInfo info = assembler.getClassPath().getValue();
			if (isOwnClass(info))
				return factory.forAssembler(info, memberName(assembler.getPath()));
		} else if (node instanceof ClassNavigable classNavigable) {
			ClassInfo info = classNavigable.getClassPath().getValue();
			if (isOwnClass(info))
				return factory.forClass(info);
		}
		return workspaceOrIdle();
	}

	private boolean isOwnClass(ClassInfo info) {
		return workspaceManager.hasCurrentWorkspace()
				&& workspaceManager.getCurrent().getPrimaryResource().getJvmClassBundle().containsKey(info.getName());
	}

	private void update(RichPresence next) {
		current = next;
		submit(() -> push(false));
	}

	private void push(boolean force) {
		if (!client.isConnected()) {
			if (!client.connect())
				return;
			force = true;
		}
		RichPresence presence = current;
		String json = presence.toJson().toString();
		if (!force && json.equals(lastSentJson))
			return;
		try {
			client.sendActivity(presence);
			lastSentJson = json;
		} catch (IOException e) {
			lastSentJson = null;
		}
	}

	private RichPresence workspaceOrIdle() {
		return workspaceManager.hasCurrentWorkspace()
				? factory.forWorkspace(workspaceManager.getCurrent())
				: factory.idle();
	}

	private void submit(Runnable task) {
		ScheduledExecutorService executor = scheduler;
		if (executor == null)
			return;
		try {
			executor.execute(task);
		} catch (RejectedExecutionException ignored) {
		}
	}

	private static String memberName(PathNode<?> path) {
		if (path instanceof ClassMemberPathNode memberPath)
			return memberPath.getValue().getName();
		return null;
	}

	private static long appStartEpochSeconds() {
		return ProcessHandle.current().info().startInstant()
				.map(Instant::getEpochSecond)
				.orElseGet(() -> Instant.now().getEpochSecond());
	}

	private static Thread newThread(Runnable runnable) {
		Thread thread = new Thread(runnable, "discord-rpc");
		thread.setDaemon(true);
		return thread;
	}
}
