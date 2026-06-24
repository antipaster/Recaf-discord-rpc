package github.antipaster.plugin.presence;

import github.antipaster.plugin.config.RpcConstants;
import github.antipaster.plugin.rpc.RichPresence;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceFileResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

public final class PresenceFactory {
	private final long startEpochSeconds;

	public PresenceFactory(long startEpochSeconds) {
		this.startEpochSeconds = startEpochSeconds;
	}

	public RichPresence idle() {
		return logo(RpcConstants.IDLE_DETAILS, RpcConstants.IDLE_STATE);
	}

	public RichPresence forWorkspace(Workspace workspace) {
		WorkspaceResource resource = workspace.getPrimaryResource();
		int classes = resource.getJvmClassBundle().size();
		String name = resourceName(resource);
		return logo(name == null ? "Browsing a workspace" : "Browsing " + name,
				classes == 1 ? "1 class" : classes + " classes");
	}

	public RichPresence forClass(ClassInfo info) {
		String internalName = info.getName();
		return classBase(ClassKind.of(info))
				.details("Viewing " + simpleName(internalName))
				.state(packageName(internalName))
				.build();
	}

	public RichPresence forAssembler(ClassInfo info, String member) {
		return classBase(ClassKind.of(info))
				.details("Editing " + simpleName(info.getName()))
				.state(member == null ? "Bytecode assembler" : "Assembler · " + member)
				.build();
	}

	private RichPresence logo(String details, String state) {
		return RichPresence.builder()
				.startTimestamp(startEpochSeconds)
				.details(details)
				.state(state)
				.largeImage(RpcConstants.LOGO_KEY, RpcConstants.LOGO_TEXT)
				.build();
	}

	private RichPresence.Builder classBase(ClassKind kind) {
		return RichPresence.builder()
				.startTimestamp(startEpochSeconds)
				.largeImage(kind.imageKey(), kind.label())
				.smallImage(RpcConstants.LOGO_KEY, RpcConstants.LOGO_TEXT);
	}

	private static String resourceName(WorkspaceResource resource) {
		if (!(resource instanceof WorkspaceFileResource fileResource))
			return null;
		String name = fileResource.getFileInfo().getName();
		int separator = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return separator >= 0 ? name.substring(separator + 1) : name;
	}

	private static String simpleName(String internalName) {
		int slash = internalName.lastIndexOf('/');
		String simple = slash >= 0 ? internalName.substring(slash + 1) : internalName;
		return simple.replace('$', '.');
	}

	private static String packageName(String internalName) {
		int slash = internalName.lastIndexOf('/');
		return slash >= 0 ? internalName.substring(0, slash).replace('/', '.') : "(default package)";
	}
}
