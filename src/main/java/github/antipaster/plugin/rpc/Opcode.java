package github.antipaster.plugin.rpc;

public enum Opcode {
	HANDSHAKE(0),
	FRAME(1),
	CLOSE(2),
	PING(3),
	PONG(4);

	private final int value;

	Opcode(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
