package com.jagex.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

import com.jagex.Client;
import com.jagex.cache.Archive;
import com.jagex.io.Buffer;
import com.jagex.link.Deque;
import com.jagex.link.Queue;
import com.jagex.sign.SignLink;

public class ResourceProvider extends Provider implements Runnable {

	private int[] areas;
	private Client client;
	/** Resources that have been completed requests and retrieved. */
	private Deque complete = new Deque();
	private int completedRequests;
	private CRC32 crc = new CRC32();
	private int[][] crcs = new int[4][];

	private Resource current;
	private int deadTime;
	private int errors;
	private boolean expectingData = false;
	/** Low priority resources. */
	private Deque extras = new Deque();
	private int extrasRequested;
	private int[] frames;

	private byte[] gzipBuffer = new byte[65000];
	private int idleTime;
	private InputStream inputStream;
	private int[] landscapes;
	private long lastRequestTime;
	private String loadingMessage = "";
	/** Resources that are mandatory and must be retrieved immediately. */
	private Deque mandatoryRequests = new Deque();

	private int[] mapFiles;
	private int maximumPriority;
	private int[] membersArea;
	private byte[] models;
	private int[] musicPriorities;
	private OutputStream outputStream;
	private byte[] payload = new byte[500];
	private byte[][] priorities = new byte[4][];
	private int read;
	private int remainingData;
	private int requestCount;
	/** Resources that have been requested but are not yet complete. */
	private Deque requested = new Deque();

	private int requestedCount;
	private Queue requests = new Queue();
	private boolean running = true;
	private Socket socket;
	private int tick;

	/** Resources that have yet to be requested. */
	private Deque unrequested = new Deque();
	private int[][] versions = new int[4][];

	public final void clearExtras() {
		synchronized (extras) {
			extras.clear();
		}
	}

	public final int frameCount() {
		return frames.length;
	}

	public final int getCount(int type) {
		return versions[type].length;
	}

	public int getErrors() {
		return errors;
	}

	public String getLoadingMessage() {
		return loadingMessage;
	}

	public final int getModelAttributes(int file) {
		return models[file] & 0xFF;
	}

	public int getTick() {
		return tick;
	}

	public final boolean highPriorityMusic(int file) {
		return musicPriorities[file] == 1;
	}

	public final void init(Archive archive, Client client) {
		String[] versions = { "model_version", "anim_version", "midi_version", "map_version" };
		for (int type = 0; type < 4; type++) {
			byte[] data = archive.getEntry(versions[type]);
			int count = data.length / 2;
			Buffer buffer = new Buffer(data);
			this.versions[type] = new int[count];
			priorities[type] = new byte[count];

			for (int file = 0; file < count; file++) {
				this.versions[type][file] = buffer.readUShort();
			}
		}

		String[] crcs = { "model_crc", "anim_crc", "midi_crc", "map_crc" };
		for (int type = 0; type < 4; type++) {
			byte[] data = archive.getEntry(crcs[type]);
			int count = data.length / 4;
			Buffer buffer = new Buffer(data);
			this.crcs[type] = new int[count];

			for (int file = 0; file < count; file++) {
				this.crcs[type][file] = buffer.readInt();
			}
		}

		byte[] indices = archive.getEntry("model_index");
		int count = this.versions[0].length;
		models = new byte[count];

		for (int model = 0; model < count; model++) {
			models[model] = (model < indices.length) ? indices[model] : 0;
		}

		indices = archive.getEntry("map_index");
		Buffer buffer = new Buffer(indices);
		count = indices.length / 7;
		areas = new int[count];
		mapFiles = new int[count];
		landscapes = new int[count];
		membersArea = new int[count];

		for (int region = 0; region < count; region++) {
			areas[region] = buffer.readUShort();
			mapFiles[region] = buffer.readUShort();
			landscapes[region] = buffer.readUShort();
			membersArea[region] = buffer.readUByte();
		}

		indices = archive.getEntry("anim_index");
		buffer = new Buffer(indices);
		count = indices.length / 2;
		frames = new int[count];

		for (int frame = 0; frame < count; frame++) {
			frames[frame] = buffer.readUShort();
		}

		indices = archive.getEntry("midi_index");
		buffer = new Buffer(indices);
		count = indices.length;
		musicPriorities = new int[count];
		for (int music = 0; music < count; music++) {
			musicPriorities[music] = buffer.readUByte();
		}

		this.client = client;
		running = true;
		client.startRunnable(this, 2);
	}

	public final boolean landscapePresent(int landscape) {
		for (int index = 0; index < areas.length; index++) {
			if (landscapes[index] == landscape) {
				return true;
			}
		}

		return false;
	}

	public final void loadExtra(int type, int file) {
		if (client.indices[0] == null) {
			return;
		} else if (versions[type][file] == 0) {
			return;
		} else if (priorities[type][file] == 0) {
			return;
		} else if (maximumPriority == 0) {
			return;
		}

		Resource node = new Resource();
		node.setType(type);
		node.setFile(file);
		node.setMandatory(false);

		synchronized (extras) {
			extras.pushBack(node);
		}
	}

	public final Resource next() {
		Resource request;
		synchronized (complete) {
			request = (Resource) complete.popFront();
		}

		if (request == null) {
			return null;
		}

		synchronized (requests) {
			request.unlinkCacheable();
		}

		if (request.getData() == null) {
			return request;
		}

		int read = 0;
		try {
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(request.getData()));
			do {
				if (read == gzipBuffer.length) {
					throw new RuntimeException("buffer overflow!");
				}

				int in = gis.read(gzipBuffer, read, gzipBuffer.length - read);
				if (in == -1) {
					break;
				}

				read += in;
			} while (true);
		} catch (IOException ex) {
			throw new RuntimeException("error unzipping");
		}

		request.setData(new byte[read]);
		for (int i = 0; i < read; i++) {
			request.getData()[i] = gzipBuffer[i];
		}

		return request;
	}

	public final void preloadMaps(boolean members) {
		for (int area = 0; area < areas.length; area++) {
			if (members || membersArea[area] != 0) {
				requestExtra(3, landscapes[area], (byte) 2);
				requestExtra(3, mapFiles[area], (byte) 2);
			}
		}
	}

	@Override
	public final void provide(int file) {
		provide(0, file);
	}

	public final void provide(int type, int file) {
		if (type < 0 || type > versions.length || file < 0 || file > versions[type].length) {
			return;
		}

		if (versions[type][file] == 0) {
			return;
		}

		synchronized (requests) {
			for (Resource node = (Resource) requests.peek(); node != null; node = (Resource) requests.getNext()) {
				if (node.getType() == type && node.getFile() == file) {
					return;
				}
			}

			Resource node = new Resource();
			node.setType(type);
			node.setFile(file);
			node.setMandatory(true);
			synchronized (mandatoryRequests) {
				mandatoryRequests.pushBack(node);
			}

			requests.push(node);
		}
	}

	public final int remaining() {
		synchronized (requests) {
			return requests.size();
		}
	}

	public final void requestExtra(int type, int file, byte priority) {
		if (client.indices[0] == null) {
			return;
		}
		if (versions[type][file] == 0) {
			return;
		}

		byte[] data = client.indices[type + 1].decompress(file);
		if (verify(data, crcs[type][file], versions[type][file])) {
			return;
		}

		priorities[type][file] = priority;
		if (priority > maximumPriority) {
			maximumPriority = priority;
		}
		requestCount++;
	}

	public final int resolve(int regionX, int regionY, int type) {
		int code = (regionX << 8) + regionY;
		for (int area = 0; area < areas.length; area++) {
			if (areas[area] == code) {
				return type == 0 ? mapFiles[area] : landscapes[area];
			}
		}

		return -1;
	}

	@Override
	public final void run() {
		try {
			while (running) {
				tick++;
				int sleepTime = 20;
				if (maximumPriority == 0 && client.indices[0] != null) {
					sleepTime = 50;
				}

				try {
					Thread.sleep(sleepTime);
				} catch (Exception ex) {
				}

				expectingData = true;
				for (int i = 0; i < 100; i++) {
					if (!expectingData) {
						break;
					}

					expectingData = false;
					loadMandatory();
					requestMandatory();

					if (requestedCount == 0 && i >= 5) {
						break;
					}

					loadExtra();
					if (inputStream != null) {
						respond();
					}
				}

				boolean idle = false;
				for (Resource request = (Resource) requested.getFront(); request != null; request = (Resource) requested
						.getNext()) {
					if (request.isMandatory()) {
						idle = true;
						request.setAge(request.getAge() + 1);
						if (request.getAge() > 50) {
							request.setAge(0);
							request(request);
						}
					}
				}

				if (!idle) {
					for (Resource request = (Resource) requested.getFront(); request != null; request = (Resource) requested
							.getNext()) {
						idle = true;
						request.setAge(request.getAge() + 1);
						if (request.getAge() > 50) {
							request.setAge(0);
							request(request);
						}
					}
				}

				if (idle) {
					idleTime++;
					if (idleTime > 750) {
						try {
							socket.close();
						} catch (Exception _ex) {
						}
						socket = null;
						inputStream = null;
						outputStream = null;
						remainingData = 0;
					}
				} else {
					idleTime = 0;
					loadingMessage = "";
				}

				if (client.loggedIn && socket != null && outputStream != null
						&& (maximumPriority > 0 || client.indices[0] == null)) {
					deadTime++;

					if (deadTime > 500) {
						deadTime = 0;
						payload[0] = 0;
						payload[1] = 0;
						payload[2] = 0;
						payload[3] = 10;

						try {
							outputStream.write(payload, 0, 4);
						} catch (IOException ex) {
							idleTime = 5000;
						}
					}
				}
			}
		} catch (Exception exception) {
			SignLink.reportError("od_ex " + exception.getMessage());
		}
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public void setLoadingMessage(String loadingMessage) {
		this.loadingMessage = loadingMessage;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public final void stop() {
		running = false;
	}

	private final void loadExtra() {
		while (requestedCount == 0 && extrasRequested < 10) {
			if (maximumPriority == 0) {
				break;
			}

			Resource request;
			synchronized (extras) {
				request = (Resource) extras.popFront();
			}

			while (request != null) {
				if (priorities[request.getType()][request.getFile()] != 0) {
					priorities[request.getType()][request.getFile()] = 0;
					requested.pushBack(request);
					request(request);
					expectingData = true;

					if (completedRequests < requestCount) {
						completedRequests++;
					}

					loadingMessage = "Loading extra files - " + completedRequests * 100 / requestCount + "%";
					extrasRequested++;

					if (extrasRequested == 10) {
						return;
					}
				}

				synchronized (extras) {
					request = (Resource) extras.popFront();
				}
			}

			for (int type = 0; type < 4; type++) {
				byte[] data = priorities[type];
				int size = data.length;

				for (int file = 0; file < size; file++) {
					if (data[file] == maximumPriority) {
						data[file] = 0;
						Resource newRequest = new Resource();
						newRequest.setType(type);
						newRequest.setFile(file);
						newRequest.setMandatory(false);

						requested.pushBack(newRequest);
						request(newRequest);
						expectingData = true;

						if (completedRequests < requestCount) {
							completedRequests++;
						}

						loadingMessage = "Loading extra files - " + completedRequests * 100 / requestCount + "%";
						extrasRequested++;

						if (extrasRequested == 10) {
							return;
						}
					}
				}
			}
			maximumPriority--;
		}
	}

	private final void loadMandatory() {
		Resource request;
		synchronized (mandatoryRequests) {
			request = (Resource) mandatoryRequests.popFront();
		}

		while (request != null) {
			expectingData = true;
			byte[] data = null;

			if (client.indices[0] != null) {
				data = client.indices[request.getType() + 1].decompress(request.getFile());
			}

			if (!verify(data, crcs[request.getType()][request.getFile()], versions[request.getType()][request.getFile()])) {
				data = null;
			}

			synchronized (mandatoryRequests) {
				if (data == null) {
					unrequested.pushBack(request);
				} else {
					request.setData(data);

					synchronized (complete) {
						complete.pushBack(request);
					}
				}

				request = (Resource) mandatoryRequests.popFront();
			}
		}
	}

	private final void request(Resource request) {
		try {
			if (socket == null) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastRequestTime < 4000L) {
					return;
				}

				lastRequestTime = currentTime;
				socket = client.openSocket(43594 + Client.portOffset);
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				outputStream.write(15);

				for (int i = 0; i < 8; i++) {
					inputStream.read();
				}

				idleTime = 0;
			}

			payload[0] = (byte) request.getType();
			payload[1] = (byte) (request.getFile() >> 8);
			payload[2] = (byte) request.getFile();

			if (request.isMandatory()) {
				payload[3] = 2;
			} else if (!client.loggedIn) {
				payload[3] = 1;
			} else {
				payload[3] = 0;
			}

			outputStream.write(payload, 0, 4);
			deadTime = 0;
			errors = -10000;
			return;
		} catch (IOException ioexception) {
		}

		try {
			socket.close();
		} catch (Exception ex) {
		}

		socket = null;
		inputStream = null;
		outputStream = null;
		remainingData = 0;
		errors++;
	}

	private final void requestMandatory() {
		requestedCount = 0;
		extrasRequested = 0;
		for (Resource request = (Resource) requested.getFront(); request != null; request = (Resource) requested.getNext()) {
			if (request.isMandatory()) {
				requestedCount++;
			} else {
				extrasRequested++;
			}
		}

		while (requestedCount < 10) {
			Resource request = (Resource) unrequested.popFront();
			if (request == null) {
				break;
			}

			if (priorities[request.getType()][request.getFile()] != 0) {
				completedRequests++;
			}

			priorities[request.getType()][request.getFile()] = 0;
			requested.pushBack(request);
			requestedCount++;
			request(request);
			expectingData = true;
		}
	}

	private final void respond() {
		try {
			int available = inputStream.available();
			if (remainingData == 0 && available >= 6) {
				expectingData = true;
				for (int skip = 0; skip < 6; skip += inputStream.read(payload, skip, 6 - skip)) {

				}

				int type = payload[0] & 0xff;
				int file = ((payload[1] & 0xff) << 8) + (payload[2] & 0xff);
				int length = ((payload[3] & 0xff) << 8) + (payload[4] & 0xff);
				int sector = payload[5] & 0xff;
				current = null;

				for (Resource request = (Resource) requested.getFront(); request != null; request = (Resource) requested
						.getNext()) {
					if (request.getType() == type && request.getFile() == file) {
						current = request;
					}

					if (current != null) {
						request.setAge(0);
					}
				}

				if (current != null) {
					idleTime = 0;
					if (length == 0) {
						SignLink.reportError("Rej: " + type + "," + file);
						current.setData(null);

						if (current.isMandatory()) {
							synchronized (complete) {
								complete.pushBack(current);
							}
						} else {
							current.unlink();
						}

						current = null;
					} else {
						if (current.getData() == null && sector == 0) {
							current.setData(new byte[length]);
						}

						if (current.getData() == null && sector != 0) {
							throw new IOException("missing start of file");
						}
					}
				}

				read = sector * 500;
				remainingData = 500;

				if (remainingData > length - sector * 500) {
					remainingData = length - sector * 500;
				}
			}

			if (remainingData > 0 && available >= remainingData) {
				expectingData = true;
				byte[] data = payload;
				int read = 0;
				if (current != null) {
					data = current.getData();
					read = this.read;
				}

				for (int skip = 0; skip < remainingData; skip += inputStream.read(data, skip + read, remainingData - skip)) {

				}

				if (remainingData + this.read >= data.length && current != null) {
					if (client.indices[0] != null) {
						client.indices[current.getType() + 1].put(data, current.getFile(), data.length);
					}

					if (!current.isMandatory() && current.getType() == 3) {
						current.setMandatory(true);
						current.setType(93);
					}

					if (current.isMandatory()) {
						synchronized (complete) {
							complete.pushBack(current);
						}
					} else {
						current.unlink();
					}
				}
				remainingData = 0;
				return;
			}
		} catch (IOException e) {
			try {
				socket.close();
			} catch (Exception ex) {
			}

			socket = null;
			inputStream = null;
			outputStream = null;
			remainingData = 0;
		}
	}

	private final boolean verify(byte[] data, int cacheCrc, int cacheVersion) {
		if (data == null || data.length < 2) {
			return false;
		}

		int length = data.length - 2;
		int version = ((data[length] & 0xff) << 8) + (data[length + 1] & 0xff);
		crc.reset();
		crc.update(data, 0, length);
		int calculated = (int) crc.getValue();

		return version == cacheVersion && calculated == cacheCrc;
	}

}