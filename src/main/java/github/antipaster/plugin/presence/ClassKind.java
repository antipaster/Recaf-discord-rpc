package github.antipaster.plugin.presence;

import software.coley.recaf.info.ClassInfo;

public enum ClassKind {
	ANNOTATION("annotation", "Annotation"),
	INTERFACE("interface", "Interface"),
	ENUM("enum", "Enum"),
	ABSTRACT_CLASS("class_abstract", "Abstract class"),
	CLASS("class", "Class");

	private final String imageKey;
	private final String label;

	ClassKind(String imageKey, String label) {
		this.imageKey = imageKey;
		this.label = label;
	}

	public static ClassKind of(ClassInfo info) {
		if (info.hasAnnotationModifier())
			return ANNOTATION;
		if (info.hasInterfaceModifier())
			return INTERFACE;
		if (info.hasEnumModifier())
			return ENUM;
		if (info.hasAbstractModifier())
			return ABSTRACT_CLASS;
		return CLASS;
	}

	public String imageKey() {
		return imageKey;
	}

	public String label() {
		return label;
	}
}
