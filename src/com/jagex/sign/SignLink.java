package com.jagex.sign;

import java.applet.Applet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public final class SignLink implements Runnable {

	private static boolean active;
	private static Applet applet = null;
	private static RandomAccessFile cache = null;
	private static String dns = null;
	private static String dnsRequest = null;
	private static String error = "";
	private static RandomAccessFile[] indices = new RandomAccessFile[5];
	private static String midi = null;
	private static int midiFade;
	private static boolean midiPlay;
	private static int midiPos;
	private static int midiVolume;
	private static boolean reportError = true;
	private static byte[] saveBuffer = null;
	private static int saveLength;
	private static String saveRequest = null;
	private static Socket socket = null;
	private static InetAddress socketAddress;
	private static int socketRequest;
	private static int storeId = 32;
	private static boolean sunJava;
	private static int threadLiveId;
	private static Runnable threadRequest = null;
	private static int threadRequestPriority = 1;
	private static int uid;
	private static String urlRequest = null;
	private static DataInputStream urlStream = null;
	private static String wave = null;
	private static boolean wavePlay;
	private static int wavePosition;
	private static int waveVolume;

	public static synchronized void dnsLookup(String request) {
		dns = request;
		dnsRequest = request;
	}

	public static String findCacheDirectory() {
		String[] directories = { "c:/windows/", "c:/winnt/", "d:/windows/", "d:/winnt/", "e:/windows/", "e:/winnt/",
				"f:/windows/", "f:/winnt/", "c:/", "~/", "/tmp/", "", "c:/rscache", "/rscache" };
		if (storeId < 32 || storeId > 34) {
			storeId = 32;
		}

		String sub = ".file_store_" + storeId;
		for (String directory : directories) {
			try {
				if (directory.length() > 0) {
					File file = new File(directory);
					if (!file.exists()) {
						continue;
					}
				}

				File out = new File(directory + sub);
				if (out.exists() || out.mkdir()) {
					System.out.println(directory + sub + "/");
					return directory + sub + "/";
				}
			} catch (Exception ex) {
			}
		}

		return null;
	}

	public static Applet getApplet() {
		return applet;
	}

	public static RandomAccessFile getCache() {
		return cache;
	}

	public static String getDns() {
		return dns;
	}

	public static String getError() {
		return error;
	}

	public static RandomAccessFile[] getIndices() {
		return indices;
	}

	public static String getMidi() {
		return midi;
	}

	public static int getMidiFade() {
		return midiFade;
	}

	public static int getMidiVolume() {
		return midiVolume;
	}

	public static int getStoreId() {
		return storeId;
	}

	public static int getUid() {
		return uid;
	}

	public static String getWave() {
		return wave;
	}

	public static int getWaveVolume() {
		return waveVolume;
	}

	public static boolean isReportError() {
		return reportError;
	}

	public static boolean isSunJava() {
		return sunJava;
	}

	public static synchronized void midiSave(byte[] buffer, int length) {
		if (length > 0x1e8480) {
			return;
		} else if (saveRequest != null) {
			return;
		}

		midiPos = (midiPos + 1) % 5;
		saveLength = length;
		saveBuffer = buffer;
		midiPlay = true;
		saveRequest = "jingle" + midiPos + ".mid";
	}

	public static synchronized Socket openSocket(int port) throws IOException {
		for (socketRequest = port; socketRequest != 0;) {
			try {
				Thread.sleep(50L);
			} catch (Exception ex) {
			}
		}

		if (socket == null) {
			throw new IOException("could not open socket");
		}
		return socket;
	}

	public static synchronized DataInputStream openUrl(String url) throws IOException {
		for (urlRequest = url; urlRequest != null;) {
			try {
				Thread.sleep(50L);
			} catch (Exception ex) {
			}
		}

		if (urlStream == null) {
			throw new IOException("could not open: " + url);
		}
		return urlStream;
	}

	public static void reportError(String error) {
		if (!reportError) {
			return;
		} else if (!active) {
			return;
		}

		System.out.println("Error: " + error);

		// try {
		// error = error.replace(':', '_');
		// error = error.replace('@', '_');
		// error = error.replace('&', '_');
		// error = error.replace('#', '_');
		// DataInputStream datainputstream = openUrl("reporterror" + 317 + ".cgi?error=" + errorName + " " + error);
		// datainputstream.readLine();
		// datainputstream.close();
		// return;
		// } catch (IOException ex) {
		// return;
		// }

	}

	public static void setApplet(Applet applet) {
		SignLink.applet = applet;
	}

	public static void setCache(RandomAccessFile cache) {
		SignLink.cache = cache;
	}

	public static void setDns(String dns) {
		SignLink.dns = dns;
	}

	public static void setError(String error) {
		SignLink.error = error;
	}

	public static void setIndices(RandomAccessFile[] indices) {
		SignLink.indices = indices;
	}

	public static void setMidi(String midi) {
		SignLink.midi = midi;
	}

	public static void setMidiFade(int midiFade) {
		SignLink.midiFade = midiFade;
	}

	public static void setMidiVolume(int midiVolume) {
		SignLink.midiVolume = midiVolume;
	}

	public static void setReportError(boolean reportError) {
		SignLink.reportError = reportError;
	}

	public static void setStoreId(int storeId) {
		SignLink.storeId = storeId;
	}

	public static void setSunJava(boolean sunJava) {
		SignLink.sunJava = sunJava;
	}

	public static void setUid(int uid) {
		SignLink.uid = uid;
	}

	public static void setWave(String wave) {
		SignLink.wave = wave;
	}

	public static void setWaveVolume(int waveVolume) {
		SignLink.waveVolume = waveVolume;
	}

	public static void startPriv(InetAddress address) {
		threadLiveId = (int) (Math.random() * 99999999D);
		if (active) {
			try {
				Thread.sleep(500L);
			} catch (Exception ex) {
			}
			active = false;
		}

		socketRequest = 0;
		threadRequest = null;
		dnsRequest = null;
		saveRequest = null;
		urlRequest = null;
		socketAddress = address;

		Thread thread = new Thread(new SignLink());
		thread.setDaemon(true);
		thread.start();

		while (!active) {
			try {
				Thread.sleep(50L);
			} catch (Exception ex) {
			}
		}
	}

	public static synchronized void startThread(Runnable runnable, int priority) {
		threadRequestPriority = priority;
		threadRequest = runnable;
	}

	public static synchronized boolean waveReplay() {
		if (saveRequest != null) {
			return false;
		}

		saveBuffer = null;
		wavePlay = true;
		saveRequest = "sound" + wavePosition + ".wav";
		return true;
	}

	public static synchronized boolean waveSave(byte[] buffer, int length) {
		if (length > 0x1e8480 || saveRequest != null) {
			return false;
		}

		wavePosition = (wavePosition + 1) % 5;
		saveLength = length;
		saveBuffer = buffer;
		wavePlay = true;
		saveRequest = "sound" + wavePosition + ".wav";
		return true;
	}

	private static int getUid(String prefix) {
		try {
			File uid = new File(prefix + "uid.dat");
			if (!uid.exists() || uid.length() < 4) {
				try (DataOutputStream os = new DataOutputStream(new FileOutputStream(prefix + "uid.dat"))) {
					os.writeInt((int) (Math.random() * 99999999));
				}
			}
		} catch (IOException ex) {
		}

		try (DataInputStream is = new DataInputStream(new FileInputStream(prefix + "uid.dat"))) {
			return is.readInt() + 1;
		} catch (IOException ex) {
			return 0;
		}
	}

	@Override
	public final void run() {
		active = true;
		String directory = findCacheDirectory();
		uid = getUid(directory);

		try {
			File file = new File(directory + "main_file_cache.dat");
			if (file.exists() && file.length() > 0x3200000) {
				file.delete();
			}

			cache = new RandomAccessFile(directory + "main_file_cache.dat", "rw");
			for (int index = 0; index < 5; index++) {
				indices[index] = new RandomAccessFile(directory + "main_file_cache.idx" + index, "rw");
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}

		for (int id = threadLiveId; threadLiveId == id;) {
			if (socketRequest != 0) {
				try {
					socket = new Socket(socketAddress, socketRequest);
				} catch (Exception ex) {
					socket = null;
				}

				socketRequest = 0;
			} else if (threadRequest != null) {
				Thread thread = new Thread(threadRequest);
				thread.setDaemon(true);
				thread.start();
				thread.setPriority(threadRequestPriority);
				threadRequest = null;
			} else if (dnsRequest != null) {
				try {
					dns = InetAddress.getByName(dnsRequest).getHostName();
				} catch (Exception ex) {
					dns = "unknown";
				}

				dnsRequest = null;
			} else if (saveRequest != null) {
				if (saveBuffer != null) {
					try (FileOutputStream fos = new FileOutputStream(directory + saveRequest)) {
						fos.write(saveBuffer, 0, saveLength);
					} catch (IOException ex) {
					}
				}

				if (wavePlay) {
					wave = directory + saveRequest;
					wavePlay = false;
				}

				if (midiPlay) {
					midi = directory + saveRequest;
					midiPlay = false;
				}

				saveRequest = null;
			} else if (urlRequest != null) {
				try {
					urlStream = new DataInputStream(new URL(applet.getCodeBase(), urlRequest).openStream());
				} catch (Exception ex) {
					urlStream = null;
				}
				urlRequest = null;
			}

			try {
				Thread.sleep(50L);
			} catch (Exception ex) {
			}
		}
	}

}