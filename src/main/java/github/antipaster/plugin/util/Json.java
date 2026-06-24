package github.antipaster.plugin.util;

public final class Json {
	private final StringBuilder builder = new StringBuilder("{");
	private boolean empty = true;

	private Json() {
	}

	public static Json object() {
		return new Json();
	}

	public Json put(String key, String value) {
		if (value == null)
			return this;
		key(key);
		string(value);
		return this;
	}

	public Json put(String key, long value) {
		key(key);
		builder.append(value);
		return this;
	}

	public Json put(String key, Json value) {
		if (value == null)
			return this;
		key(key);
		builder.append(value);
		return this;
	}

	@Override
	public String toString() {
		return builder + "}";
	}

	private void key(String key) {
		if (!empty)
			builder.append(',');
		empty = false;
		string(key);
		builder.append(':');
	}

	private void string(String value) {
		builder.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"' -> builder.append("\\\"");
				case '\\' -> builder.append("\\\\");
				case '\n' -> builder.append("\\n");
				case '\r' -> builder.append("\\r");
				case '\t' -> builder.append("\\t");
				case '\b' -> builder.append("\\b");
				case '\f' -> builder.append("\\f");
				default -> {
					if (c < 0x20)
						builder.append(String.format("\\u%04x", (int) c));
					else
						builder.append(c);
				}
			}
		}
		builder.append('"');
	}
}
