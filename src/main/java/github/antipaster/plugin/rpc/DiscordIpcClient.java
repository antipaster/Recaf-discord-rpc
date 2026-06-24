package github.antipaster.plugin.rpc;

import github.antipaster.plugin.util.Json;
import github.antipaster.plugin.util.Platform;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public final class DiscordIpcClient {
	private static final int MAX_FRAME_LENGTH = 64 * 1024;

	private final String applicationId;
	private final long pid = ProcessHandle.current().pid();
	private final AtomicLong nonce = new AtomicLong();

	private Transport transport;
	private volatile boolean connected;

	public DiscordIpcClient(String applicationId) {
		this.applicationId = applicationId;
	}

	public boolean isConnected() {
		return connected;
	}

	public synchronized boolean connect() {
		if (connected)
			return true;
		for (String candidate : Platform.ipcCandidates()) {
			Transport attempt = open(candidate);
			if (attempt == null)
				continue;
			try {
				transport = attempt;
				handshake();
				connected = true;
				return true;
			} catch (IOException e) {
				closeQuietly(attempt);
				transport = null;
			}
		}
		return false;
	}

	public synchronized void sendActivity(RichPresence presence) throws IOException {
		send(Json.object()
				.put("pid", pid)
				.put("activity", presence.toJson()));
	}

	public synchronized void clearActivity() throws IOException {
		send(Json.object().put("pid", pid));
	}

	public synchronized void close() {
		if (transport != null) {
			try {
				writeFrame(Opcode.CLOSE, Json.object().toString());
			} catch (IOException ignored) {
			}
			closeQuietly(transport);
			transport = null;
		}
		connected = false;
	}

	private void send(Json args) throws IOException {
		if (!connected || transport == null)
			throw new IOException("Discord IPC is not connected");
		String payload = Json.object()
				.put("cmd", "SET_ACTIVITY")
				.put("args", args)
				.put("nonce", pid + "-" + nonce.incrementAndGet())
				.toString();
		try {
			writeFrame(Opcode.FRAME, payload);
			readFrame();
		} catch (IOException e) {
			close();
			throw e;
		}
	}

	private void handshake() throws IOException {
		writeFrame(Opcode.HANDSHAKE, Json.object()
				.put("v", 1)
				.put("client_id", applicationId)
				.toString());
		readFrame();
	}

	private void writeFrame(Opcode opcode, String payload) throws IOException {
		byte[] body = payload.getBytes(StandardCharsets.UTF_8);
		ByteBuffer frame = ByteBuffer.allocate(8 + body.length).order(ByteOrder.LITTLE_ENDIAN);
		frame.putInt(opcode.value());
		frame.putInt(body.length);
		frame.put(body);
		transport.write(frame.array());
	}

	private void readFrame() throws IOException {
		byte[] header = new byte[8];
		transport.readFully(header);
		int length = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
		if (length < 0 || length > MAX_FRAME_LENGTH)
			throw new IOException("Unexpected frame length: " + length);
		transport.readFully(new byte[length]);
	}

	private static Transport open(String path) {
		try {
			if (Platform.isWindows())
				return new PipeTransport(path);
			return new SocketTransport(path);
		} catch (IOException e) {
			return null;
		}
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ignored) {
		}
	}

	private interface Transport extends Closeable {
		void write(byte[] data) throws IOException;

		void readFully(byte[] buffer) throws IOException;
	}

	private static final class PipeTransport implements Transport {
		private final RandomAccessFile pipe;

		private PipeTransport(String path) throws IOException {
			this.pipe = new RandomAccessFile(path, "rw");
		}

		@Override
		public void write(byte[] data) throws IOException {
			pipe.write(data);
		}

		@Override
		public void readFully(byte[] buffer) throws IOException {
			pipe.readFully(buffer);
		}

		@Override
		public void close() throws IOException {
			pipe.close();
		}
	}

	private static final class SocketTransport implements Transport {
		private final SocketChannel channel;

		private SocketTransport(String path) throws IOException {
			this.channel = SocketChannel.open(StandardProtocolFamily.UNIX);
			this.channel.connect(UnixDomainSocketAddress.of(path));
		}

		@Override
		public void write(byte[] data) throws IOException {
			ByteBuffer buffer = ByteBuffer.wrap(data);
			while (buffer.hasRemaining())
				channel.write(buffer);
		}

		@Override
		public void readFully(byte[] buffer) throws IOException {
			ByteBuffer target = ByteBuffer.wrap(buffer);
			while (target.hasRemaining()) {
				if (channel.read(target) < 0)
					throw new EOFException();
			}
		}

		@Override
		public void close() throws IOException {
			channel.close();
		}
	}
}
