package com.jagex.net;

import com.jagex.GameApplet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class BufferedConnection implements Runnable {

	private GameApplet applet;
	private int bufferIndex;
	private int cycle;
	private boolean erred = false;
	private InputStream input;
	private OutputStream output;
	private byte[] outputBuffer;
	private Socket socket;
	private boolean stopped = false;
	private boolean writing = false;

	public BufferedConnection(GameApplet applet, Socket socket) throws IOException {
		this.applet = applet;
		this.socket = socket;
		this.socket.setSoTimeout(30000);
		this.socket.setTcpNoDelay(true);

		input = this.socket.getInputStream();
		output = this.socket.getOutputStream();
	}

	public int available() throws IOException {
		if (stopped) {
			return 0;
		}

		return input.available();
	}

	public void debug() {
		System.out.println("dummy:" + stopped);
		System.out.println("tcycl:" + cycle);
		System.out.println("tnum:" + bufferIndex);
		System.out.println("writer:" + writing);
		System.out.println("ioerror:" + erred);

		try {
			System.out.println("available:" + available());
		} catch (IOException ex) {
		}
	}

	public int read() throws IOException {
		if (stopped) {
			return 0;
		}

		return input.read();
	}

	public void read(byte[] buffer, int offset, int length) throws IOException {
		if (stopped) {
			return;
		}

		int in;
		for (; length > 0; length -= in) {
			in = input.read(buffer, offset, length);
			if (in <= 0) {
				throw new IOException("EOF");
			}

			offset += in;
		}
	}

	@Override
	public void run() {
		while (writing) {
			int length;
			int offset;
			synchronized (this) {
				if (bufferIndex == cycle) {
					try {
						wait();
					} catch (InterruptedException _ex) {
					}
				}

				if (!writing) {
					return;
				}

				offset = cycle;
				if (bufferIndex >= cycle) {
					length = bufferIndex - cycle;
				} else {
					length = 5000 - cycle;
				}
			}

			if (length > 0) {
				try {
					output.write(outputBuffer, offset, length);
				} catch (IOException ex) {
					erred = true;
				}

				cycle = (cycle + length) % 5000;
				try {
					if (bufferIndex == cycle) {
						output.flush();
					}
				} catch (IOException ex) {
					erred = true;
				}
			}
		}
	}

	public void stop() {
		stopped = true;
		try {
			if (input != null) {
				input.close();
			}

			if (output != null) {
				output.close();
			}

			if (socket != null) {
				socket.close();
			}
		} catch (IOException ex) {
			System.out.println("Error closing stream");
		}

		writing = false;
		synchronized (this) {
			notify();
		}
		outputBuffer = null;
	}

	public void write(byte[] buffer, int length, int offset) throws IOException {
		if (stopped) {
			return;
		}

		if (erred) {
			erred = false;
			throw new IOException("Error in writer thread");
		}

		if (outputBuffer == null) {
			outputBuffer = new byte[5000];
		}

		synchronized (this) {
			for (int i = 0; i < length; i++) {
				outputBuffer[bufferIndex] = buffer[i + offset];
				bufferIndex = (bufferIndex + 1) % 5000;
				if (bufferIndex == (cycle + 4900) % 5000) {
					throw new IOException("buffer overflow");
				}
			}

			if (!writing) {
				writing = true;
				applet.startRunnable(this, 3);
			}
			notify();
		}
	}

}