package github.antipaster.plugin.rpc;

import github.antipaster.plugin.util.Json;

public final class RichPresence {
	private final String details;
	private final String state;
	private final Long startTimestamp;
	private final String largeImage;
	private final String largeText;
	private final String smallImage;
	private final String smallText;

	private RichPresence(Builder builder) {
		this.details = builder.details;
		this.state = builder.state;
		this.startTimestamp = builder.startTimestamp;
		this.largeImage = builder.largeImage;
		this.largeText = builder.largeText;
		this.smallImage = builder.smallImage;
		this.smallText = builder.smallText;
	}

	public static Builder builder() {
		return new Builder();
	}

	public Json toJson() {
		Json activity = Json.object()
				.put("details", details)
				.put("state", state);
		if (startTimestamp != null)
			activity.put("timestamps", Json.object().put("start", startTimestamp));
		Json assets = assets();
		if (assets != null)
			activity.put("assets", assets);
		return activity;
	}

	private Json assets() {
		if (largeImage == null && smallImage == null)
			return null;
		return Json.object()
				.put("large_image", largeImage)
				.put("large_text", largeText)
				.put("small_image", smallImage)
				.put("small_text", smallText);
	}

	public static final class Builder {
		private String details;
		private String state;
		private Long startTimestamp;
		private String largeImage;
		private String largeText;
		private String smallImage;
		private String smallText;

		private Builder() {
		}

		public Builder details(String details) {
			this.details = details;
			return this;
		}

		public Builder state(String state) {
			this.state = state;
			return this;
		}

		public Builder startTimestamp(long epochSeconds) {
			this.startTimestamp = epochSeconds;
			return this;
		}

		public Builder largeImage(String key, String text) {
			this.largeImage = key;
			this.largeText = text;
			return this;
		}

		public Builder smallImage(String key, String text) {
			this.smallImage = key;
			this.smallText = text;
			return this;
		}

		public RichPresence build() {
			return new RichPresence(this);
		}
	}
}
