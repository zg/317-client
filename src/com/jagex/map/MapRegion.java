package com.jagex.map;

import com.jagex.cache.def.ObjectDefinition;
import com.jagex.draw.Rasterizer;
import com.jagex.entity.Renderable;
import com.jagex.entity.RenderableObject;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.net.ResourceProvider;

public final class MapRegion {

	public static int currentPlane;
	public static boolean lowMemory = true;
	public static int maximumPlane = 99;
	private static final int[] anIntArray140 = { 16, 32, 64, 128 };
	private static final int[] anIntArray152 = { 1, 2, 4, 8 }; // orientation -> ??
	private static final int BLOCKED_TILE = 1;
	public static final int BRIDGE_TILE = 2;
	private static final int[] COSINE_VERTICES = { 1, 0, -1, 0 };
	private static final int FORCE_LOWEST_PLANE = 8;
	private static int hueOffset = (int) (Math.random() * 17) - 8;
	private static int luminanceOffset = (int) (Math.random() * 33) - 16;
	private static final int[] SINE_VERTICIES = { 0, -1, 0, 1 };

	public static void decode(Buffer buffer, ResourceProvider provider) {
		label0: {
			int id = -1;
			do {
				int offset = buffer.readUSmart();
				if (offset == 0) {
					break label0;
				}

				id += offset;
				ObjectDefinition definition = ObjectDefinition.lookup(id);
				definition.loadModels(provider);

				do {
					int in = buffer.readUSmart();
					if (in == 0) {
						break;
					}
					buffer.readUByte();
				} while (true);
			} while (true);
		}
	}

	public static boolean modelReady(int objectId, int type) {
		ObjectDefinition definition = ObjectDefinition.lookup(objectId);
		if (type == 11) {
			type = 10;
		} else if (type >= 5 && type <= 8) {
			type = 4;
		}

		return definition.ready(type);
	}

	public static boolean objectsReady(byte[] data, int x, int y) {
		boolean ready = true;
		Buffer buffer = new Buffer(data);
		int id = -1;

		while (true) {
			int offset = buffer.readUSmart();
			if (offset == 0) {
				return ready;
			}

			id += offset;
			int position = 0;
			boolean skip = false;

			while (true) {
				int terminate;
				if (skip) {
					terminate = buffer.readUSmart();
					if (terminate == 0) {
						break;
					}

					buffer.readUByte();
				} else {
					terminate = buffer.readUSmart();
					if (terminate == 0) {
						break;
					}

					position += terminate - 1;
					int localY = position & 63;
					int localX = position >> 6 & 63;
					int type = buffer.readUByte() >> 2;
					int viewportX = localX + x;
					int viewportY = localY + y;
					if (viewportX > 0 && viewportY > 0 && viewportX < 103 && viewportY < 103) {
						ObjectDefinition definition = ObjectDefinition.lookup(id);
						if (type != 22 || !lowMemory || definition.isInteractive() || definition.obstructsGround()) {
							ready &= definition.ready();
							skip = true;
						}
					}
				}
			}
		}
	}

	public static void placeObject(CollisionMap map, SceneGraph scene, int id, int x, int y, int z, int type, int orientation,
			int[][][] ai, int plane) {
		int aY = ai[z][x][y];
		int bY = ai[z][x + 1][y];
		int cY = ai[z][x + 1][y + 1];
		int dY = ai[z][x][y + 1];
		int meanY = (aY + bY + cY + dY) / 4;
		ObjectDefinition definition = ObjectDefinition.lookup(id);
		int key = x + (y << 7) + (id << 14) + 0x40000000;

		if (!definition.isInteractive()) {
			key += 0x80000000;
		}

		byte config = (byte) ((orientation << 6) + type);
		if (type == 22) {
			Renderable renderable;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				renderable = definition.modelAt(22, orientation, aY, bY, cY, dY, -1);
			} else {
				renderable = new RenderableObject(id, orientation, 22, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addFloorDecoration(x, y, plane, renderable, key, config, meanY);
			if (definition.isSolid() && definition.isInteractive()) {
				map.block(x, y);
			}
		} else if (type == 10 || type == 11) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(10, orientation, aY, bY, cY, dY, -1);
			} else {
				object = new RenderableObject(id, orientation, 10, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			if (object != null) {
				int yaw = 0;
				if (type == 11) {
					yaw += 256;
				}

				int width;
				int length;

				if (orientation == 1 || orientation == 3) {
					width = definition.getLength();
					length = definition.getWidth();
				} else {
					width = definition.getWidth();
					length = definition.getLength();
				}

				scene.addObject(x, y, plane, width, length, object, key, config, yaw, meanY);
			}

			if (definition.isSolid()) {
				map.flagObject(x, y, definition.getWidth(), definition.getLength(), definition.isImpenetrable(), orientation);
			}
		} else if (type >= 12) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(type, orientation, aY, bY, cY, dY, -1);
			} else {
				object = new RenderableObject(id, orientation, type, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addObject(x, y, plane, 1, 1, object, key, config, 0, meanY);
			if (definition.isSolid()) {
				map.flagObject(x, y, definition.getWidth(), definition.getLength(), definition.isImpenetrable(), orientation);
			}
		} else if (type == 0) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(0, orientation, aY, bY, cY, dY, -1);
			} else {
				object = new RenderableObject(id, orientation, 0, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addWall(key, x, y, plane, anIntArray152[orientation], object, config, null, meanY, 0);
			if (definition.isSolid()) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
		} else if (type == 1) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(1, orientation, aY, bY, cY, dY, -1);
			} else {
				object = new RenderableObject(id, orientation, 1, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addWall(key, x, y, plane, anIntArray140[orientation], object, config, null, meanY, 0);
			if (definition.isSolid()) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
		} else if (type == 2) {
			int corner = orientation + 1 & 3;
			Renderable primary;
			Renderable secondary;

			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				primary = definition.modelAt(2, 4 + orientation, aY, bY, cY, dY, -1);
				secondary = definition.modelAt(2, corner, aY, bY, cY, dY, -1);
			} else {
				primary = new RenderableObject(id, 4 + orientation, 2, aY, bY, cY, dY, definition.getAnimation(), true);
				secondary = new RenderableObject(id, corner, 2, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addWall(key, x, y, plane, anIntArray152[orientation], primary, config, secondary, meanY, anIntArray152[corner]);
			if (definition.isSolid()) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
		} else if (type == 3) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(3, orientation, aY, bY, cY, dY, -1);
			} else {
				object = new RenderableObject(id, orientation, 3, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addWall(key, x, y, plane, anIntArray140[orientation], object, config, null, meanY, 0);
			if (definition.isSolid()) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
		} else if (type == 9) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(type, orientation, aY, bY, cY, dY, -1);
			} else {
				object = new RenderableObject(id, orientation, type, aY, bY, cY, dY, definition.getAnimation(), true);
			}

			scene.addObject(x, y, plane, 1, 1, object, key, config, 0, meanY);
			if (definition.isSolid()) {
				map.flagObject(x, y, definition.getWidth(), definition.getLength(), definition.isImpenetrable(), orientation);
			}
		} else {
			if (definition.contoursGround()) {
				if (orientation == 1) {
					int tmp = dY;
					dY = cY;
					cY = bY;
					bY = aY;
					aY = tmp;
				} else if (orientation == 2) {
					int tmp = dY;
					dY = bY;
					bY = tmp;
					tmp = cY;
					cY = aY;
					aY = tmp;
				} else if (orientation == 3) {
					int tmp = dY;
					dY = aY;
					aY = bY;
					bY = cY;
					cY = tmp;
				}
			}

			if (type == 4) {
				Renderable object;
				if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
					object = definition.modelAt(4, 0, aY, bY, cY, dY, -1);
				} else {
					object = new RenderableObject(id, 0, 4, aY, bY, cY, dY, definition.getAnimation(), true);
				}

				scene.addWallDecoration(key, y, orientation * 512, plane, 0, meanY, object, x, config, 0,
						anIntArray152[orientation]);
			} else if (type == 5) {
				int displacement = 16;
				int existing = scene.getWallKey(x, y, plane);
				if (existing > 0) {
					displacement = ObjectDefinition.lookup(existing >> 14 & 0x7fff).getDecorDisplacement();
				}

				Renderable object;
				if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
					object = definition.modelAt(4, 0, aY, bY, cY, dY, -1);
				} else {
					object = new RenderableObject(id, 0, 4, aY, bY, cY, dY, definition.getAnimation(), true);
				}

				scene.addWallDecoration(key, y, orientation * 512, plane, COSINE_VERTICES[orientation] * displacement, meanY,
						object, x, config, SINE_VERTICIES[orientation] * displacement, anIntArray152[orientation]);
			} else if (type == 6) {
				Renderable object;
				if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
					object = definition.modelAt(4, 0, aY, bY, cY, dY, -1);
				} else {
					object = new RenderableObject(id, 0, 4, aY, bY, cY, dY, definition.getAnimation(), true);
				}

				scene.addWallDecoration(key, y, orientation, plane, 0, meanY, object, x, config, 0, 0b1_0000_0000);
			} else if (type == 7) {
				Renderable object;
				if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
					object = definition.modelAt(4, 0, aY, bY, cY, dY, -1);
				} else {
					object = new RenderableObject(id, 0, 4, aY, bY, cY, dY, definition.getAnimation(), true);
				}

				scene.addWallDecoration(key, y, orientation, plane, 0, meanY, object, x, config, 0, 0b10_0000_0000);
			} else if (type == 8) {
				Renderable object;
				if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
					object = definition.modelAt(4, 0, aY, bY, cY, dY, -1);
				} else {
					object = new RenderableObject(id, 0, 4, aY, bY, cY, dY, definition.getAnimation(), true);
				}

				scene.addWallDecoration(key, y, orientation, plane, 0, meanY, object, x, config, 0, 0b11_0000_0000);
			}
		}
	}

	private static int calculateHeight(int x, int y) {
		int height = interpolatedNoise(x + 45365, y + 0x16713, 4) - 128 + (interpolatedNoise(x + 10294, y + 37821, 2) - 128 >> 1)
				+ (interpolatedNoise(x, y, 1) - 128 >> 2);
		height = (int) (height * 0.3D) + 35;
		
		if (height < 10) {
			height = 10;
		} else if (height > 60) {
			height = 60;
		}

		return height;
	}

	private static int interpolate(int a, int b, int angle, int frequencyReciprocal) {
		int cosine = 0x10000 - Rasterizer.COSINE[angle * 1024 / frequencyReciprocal] >> 1;
		return (a * (0x10000 - cosine) >> 16) + (b * cosine >> 16);
	}

	private static int interpolatedNoise(int x, int y, int frequencyReciprocal) {
		int adj_x = x / frequencyReciprocal;
		int i1 = x & frequencyReciprocal - 1;
		int adj_y = y / frequencyReciprocal;
		int k1 = y & frequencyReciprocal - 1;
		int l1 = smoothNoise(adj_x, adj_y);
		int i2 = smoothNoise(adj_x + 1, adj_y);
		int j2 = smoothNoise(adj_x, adj_y + 1);
		int k2 = smoothNoise(adj_x + 1, adj_y + 1);
		int l2 = interpolate(l1, i2, i1, frequencyReciprocal);
		int i3 = interpolate(j2, k2, i1, frequencyReciprocal);
		return interpolate(l2, i3, k1, frequencyReciprocal);
	}

	private static int light(int colour, int light) {
		if (colour == -1) {
			return 0xbc614e;
		}

		light = light * (colour & 0x7f) / 128;
		if (light < 2) {
			light = 2;
		} else if (light > 126) {
			light = 126;
		}
		
		return (colour & 0xff80) + light;
	}

	private static int perlinNoise(int x, int y) {
		int n = x + y * 57;
		n = n << 13 ^ n;
		n = n * (n * n * 15731 + 0xc0ae5) + 0x5208dd0d & 0x7fffffff;
		return n >> 19 & 0xff;
	}

	private static int smoothNoise(int x, int y) {
		int corners = perlinNoise(x - 1, y - 1) + perlinNoise(x + 1, y - 1) + perlinNoise(x - 1, y + 1)
				+ perlinNoise(x + 1, y + 1);
		int sides = perlinNoise(x - 1, y) + perlinNoise(x + 1, y) + perlinNoise(x, y - 1) + perlinNoise(x, y + 1);
		int center = perlinNoise(x, y);
		return corners / 16 + sides / 8 + center / 4;
	}

	private int[] anIntArray128;
	private int[][] anIntArrayArray139;
	private int[][][] anIntArrayArrayArray135;
	private int[] chromas;
	private byte[][][] tileFlags;
	private int[] hues;
	private int length;
	private int[] luminances;
	private byte[][][] overlayOrientations;
	private byte[][][] overlays;
	private byte[][][] overlayTypes;
	private int[] saturations;
	private byte[][][] shading;
	private byte[][][] underlays;
	private int[][][] tileHeights;
	private int width;

	public MapRegion(byte[][][] flags, int length, int width, int[][][] tileHeights) {
		maximumPlane = 99;
		this.width = width;
		this.length = length;
		this.tileHeights = tileHeights;
		this.tileFlags = flags;
		underlays = new byte[4][width][length];
		overlays = new byte[4][width][length];
		overlayTypes = new byte[4][width][length];
		overlayOrientations = new byte[4][width][length];
		anIntArrayArrayArray135 = new int[4][width + 1][length + 1];
		shading = new byte[4][width + 1][length + 1];
		anIntArrayArray139 = new int[width + 1][length + 1];
		hues = new int[length];
		saturations = new int[length];
		luminances = new int[length];
		chromas = new int[length];
		anIntArray128 = new int[length];
	}

	public final void decodeConstructedLandscapes(byte[] data, CollisionMap[] maps, SceneGraph scene, int plane,
			int topLeftRegionX, int topLeftRegionY, int collisionPlane, int k, int i1, int orientation) {

		decoding: {
			Buffer buffer = new Buffer(data);
			int id = -1;
			do {
				int idOffset = buffer.readUSmart();
				if (idOffset == 0) {
					break decoding;
				}

				id += idOffset;
				int config = 0;

				do {
					int offset = buffer.readUSmart();
					if (offset == 0) {
						break;
					}

					config += offset - 1;
					int x = config & 0x3f;
					int y = config >> 6 & 0x3f;
					int objectPlane = config >> 12;
					int packed = buffer.readUByte();
					int type = packed >> 2;
					int rotation = packed & 3;

					if (objectPlane == plane && y >= i1 && y < i1 + 8 && x >= k && x < k + 8) {
						ObjectDefinition definition = ObjectDefinition.lookup(id);
						int localX = topLeftRegionX
								+ TileUtils.getObjectYOffset(x & 7, y & 7, definition.getWidth(), definition.getLength(),
										orientation);
						int localY = topLeftRegionY
								+ TileUtils.getObjectXOffset(x & 7, y & 7, definition.getWidth(), definition.getLength(),
										orientation);

						if (localX > 0 && localY > 0 && localX < 103 && localY < 103) {
							int mapPlane = objectPlane;
							if ((tileFlags[1][localX][localY] & 2) == 2) {
								mapPlane--;
							}

							CollisionMap map = (mapPlane >= 0) ? maps[mapPlane] : null;
							method175(map, scene, id, localX, localY, collisionPlane, type, rotation + orientation & 3);
						}
					}
				} while (true);
			} while (true);
		}
	}

	public final void decodeConstructedMapData(int plane, int rotation, CollisionMap[] maps, int topLeftRegionX, int minX,
			byte[] data, int minY, int tileZ, int topLeftRegionY) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (topLeftRegionX + x > 0 && topLeftRegionX + x < 103 && topLeftRegionY + y > 0 && topLeftRegionY + y < 103) {
					maps[tileZ].adjacencies[topLeftRegionX + x][topLeftRegionY + y] &= 0xfeffffff;
				}
			}
		}

		Buffer buffer = new Buffer(data);
		for (int z = 0; z < 4; z++) {
			for (int x = 0; x < 64; x++) {
				for (int y = 0; y < 64; y++) {
					if (z == plane && x >= minX && x < minX + 8 && y >= minY && y < minY + 8) {
						decodeMapData(buffer, topLeftRegionX + TileUtils.getXOffset(x & 7, y & 7, rotation), topLeftRegionY
								+ TileUtils.getYOffset(x & 7, y & 7, rotation), tileZ, 0, 0, rotation);
					} else {
						decodeMapData(buffer, -1, -1, 0, 0, 0, 0);
					}
				}
			}
		}
	}

	public final void decodeLandscapes(CollisionMap[] maps, SceneGraph scene, byte[] data, int localX, int localY) {
		decoding: {
			Buffer buffer = new Buffer(data);
			int id = -1;

			do {
				int idOffset = buffer.readUSmart();
				if (idOffset == 0) {
					break decoding;
				}

				id += idOffset;
				int position = 0;

				do {
					int offset = buffer.readUSmart();
					if (offset == 0) {
						break;
					}

					position += offset - 1;
					int yOffset = position & 0x3f;
					int xOffset = position >> 6 & 0x3f;
					int z = position >> 12;
					int config = buffer.readUByte();
					int type = config >> 2;
					int orientation = config & 3;
					int x = xOffset + localX;
					int y = yOffset + localY;

					if (x > 0 && y > 0 && x < 103 && y < 103) {
						int plane = z;
						if ((tileFlags[1][x][y] & 2) == 2) {
							plane--;
						}

						CollisionMap map = (plane >= 0) ? maps[plane] : null;
						method175(map, scene, id, x, y, z, type, orientation);
					}
				} while (true);
			} while (true);
		}
	}

	public final void decodeRegionMapData(byte[] data, int dY, int dX, int regionX, int regionY, CollisionMap[] maps) {
		for (int z = 0; z < 4; z++) {
			for (int localX = 0; localX < 64; localX++) {
				for (int localY = 0; localY < 64; localY++) {
					if (dX + localX > 0 && dX + localX < 103 && dY + localY > 0 && dY + localY < 103) {
						maps[z].adjacencies[dX + localX][dY + localY] &= 0xfeffffff;
					}
				}
			}
		}

		Buffer buffer = new Buffer(data);
		for (int z = 0; z < 4; z++) {
			for (int localX = 0; localX < 64; localX++) {
				for (int localY = 0; localY < 64; localY++) {
					decodeMapData(buffer, localX + dX, localY + dY, z, regionX, regionY, 0);
				}
			}
		}
	}

	/**
	 * Returns the plane that actually contains the collision flag, to adjust for objects such as bridges. TODO better
	 * name
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The correct z coordinate.
	 */
	public int getCollisionPlane(int x, int y, int z) {
		if ((tileFlags[z][x][y] & FORCE_LOWEST_PLANE) != 0) {
			return 0;
		} else if (z > 0 && (tileFlags[1][x][y] & BRIDGE_TILE) != 0) {
			return z - 1;
		}

		return z;
	}

	public final void method171(CollisionMap[] maps, SceneGraph scene) {
		for (int z = 0; z < 4; z++) {
			for (int x = 0; x < 104; x++) {
				for (int y = 0; y < 104; y++) {
					if ((tileFlags[z][x][y] & BLOCKED_TILE) == 1) {
						int plane = z;
						if ((tileFlags[1][x][y] & BRIDGE_TILE) == 2) {
							plane--;
						}

						if (plane >= 0) {
							maps[plane].block(x, y);
						}
					}
				}
			}
		}

		hueOffset += (int) (Math.random() * 5D) - 2;
		if (hueOffset < -8) {
			hueOffset = -8;
		} else if (hueOffset > 8) {
			hueOffset = 8;
		}

		luminanceOffset += (int) (Math.random() * 5D) - 2;
		if (luminanceOffset < -16) {
			luminanceOffset = -16;
		} else if (luminanceOffset > 16) {
			luminanceOffset = 16;
		}

		for (int z = 0; z < 4; z++) {
			byte[][] shading = this.shading[z];
			byte byte0 = 96;
			char c = '\u0300';
			byte byte1 = -50;
			byte byte2 = -10;
			byte byte3 = -50;

			int l3 = c * (int) Math.sqrt(byte1 * byte1 + byte2 * byte2 + byte3 * byte3) >> 8;
			for (int y = 1; y < length - 1; y++) {
				for (int x = 1; x < width - 1; x++) {
					int width_dh = tileHeights[z][x + 1][y] - tileHeights[z][x - 1][y];
					int length_dh = tileHeights[z][x][y + 1] - tileHeights[z][x][y - 1];

					int r = (int) Math.sqrt(width_dh * width_dh + 0x10000 + length_dh * length_dh);
					int k12 = (width_dh << 8) / r;
					int l13 = 0x10000 / r;
					int j15 = (length_dh << 8) / r;
					int j16 = byte0 + (byte1 * k12 + byte2 * l13 + byte3 * j15) / l3;
					int j17 = (shading[x - 1][y] >> 2) + (shading[x + 1][y] >> 3) + (shading[x][y - 1] >> 2)
							+ (shading[x][y + 1] >> 3) + (shading[x][y] >> 1);
					anIntArrayArray139[x][y] = j16 - j17;
				}
			}

			for (int index = 0; index < length; index++) {
				hues[index] = 0;
				saturations[index] = 0;
				luminances[index] = 0;
				chromas[index] = 0;
				anIntArray128[index] = 0;
			}

			for (int centreX = -5; centreX < width + 5; centreX++) {
				for (int y = 0; y < length; y++) {
					int maxX = centreX + 5;
					if (maxX >= 0 && maxX < width) {
						int id = underlays[z][maxX][y] & 0xff;

						if (id > 0) {
							Floor floor = Floor.floors[id - 1];
							hues[y] += floor.getWeightedHue();
							saturations[y] += floor.getSaturation();
							luminances[y] += floor.getLuminance();
							chromas[y] += floor.getChroma();
							anIntArray128[y]++;
						}
					}

					int minX = centreX - 5;
					if (minX >= 0 && minX < width) {
						int id = underlays[z][minX][y] & 0xff;

						if (id > 0) {
							Floor floor = Floor.floors[id - 1];
							hues[y] -= floor.getWeightedHue();
							saturations[y] -= floor.getSaturation();
							luminances[y] -= floor.getLuminance();
							chromas[y] -= floor.getChroma();
							anIntArray128[y]--;
						}
					}
				}

				if (centreX >= 1 && centreX < width - 1) {
					int l9 = 0;
					int j13 = 0;
					int j14 = 0;
					int k15 = 0;
					int k16 = 0;

					for (int centreY = -5; centreY < length + 5; centreY++) {
						int j18 = centreY + 5;
						if (j18 >= 0 && j18 < length) {
							l9 += hues[j18];
							j13 += saturations[j18];
							j14 += luminances[j18];
							k15 += chromas[j18];
							k16 += anIntArray128[j18];
						}

						int k18 = centreY - 5;
						if (k18 >= 0 && k18 < length) {
							l9 -= hues[k18];
							j13 -= saturations[k18];
							j14 -= luminances[k18];
							k15 -= chromas[k18];
							k16 -= anIntArray128[k18];
						}

						if (centreY >= 1
								&& centreY < length - 1
								&& (!lowMemory || (tileFlags[0][centreX][centreY] & 2) != 0 || (tileFlags[z][centreX][centreY] & 0x10) == 0
										&& getCollisionPlane(centreX, centreY, z) == currentPlane)) {
							if (z < maximumPlane) {
								maximumPlane = z;
							}

							int underlay = underlays[z][centreX][centreY] & 0xff;
							int overlay = overlays[z][centreX][centreY] & 0xff;

							if (underlay > 0 || overlay > 0) {
								int centreZ = tileHeights[z][centreX][centreY];
								int eastZ = tileHeights[z][centreX + 1][centreY];
								int northEastZ = tileHeights[z][centreX + 1][centreY + 1];
								int northZ = tileHeights[z][centreX][centreY + 1];
								int j20 = anIntArrayArray139[centreX][centreY];
								int k20 = anIntArrayArray139[centreX + 1][centreY];
								int l20 = anIntArrayArray139[centreX + 1][centreY + 1];
								int i21 = anIntArrayArray139[centreX][centreY + 1];
								int colour = -1;
								int adjustedColour = -1;

								if (underlay > 0) {
									int hue = l9 * 256 / k15;
									int saturation = j13 / k16;
									int luminance = j14 / k16;
									colour = encode(hue, saturation, luminance);
									hue = hue + hueOffset & 0xff;
									luminance += luminanceOffset;

									if (luminance < 0) {
										luminance = 0;
									} else if (luminance > 255) {
										luminance = 255;
									}
									adjustedColour = encode(hue, saturation, luminance);
								}

								if (z > 0) {
									boolean flag = true;
									if (underlay == 0 && overlayTypes[z][centreX][centreY] != 0) {
										flag = false;
									}

									if (overlay > 0 && !Floor.floors[overlay - 1].isShadowing()) {
										flag = false;
									}

									if (flag && centreZ == eastZ && centreZ == northEastZ && centreZ == northZ) {
										anIntArrayArrayArray135[z][centreX][centreY] |= 0x924;
									}
								}

								int i22 = 0;
								if (colour != -1) {
									i22 = Rasterizer.anIntArray1482[light(adjustedColour, 96)];
								}

								if (overlay == 0) {
									scene.method279(z, centreX, centreY, 0, 0, -1, centreZ, eastZ, northEastZ, northZ,
											light(colour, j20), light(colour, k20), light(colour, l20),
											light(colour, i21), 0, 0, 0, 0, i22, 0);
								} else {
									int tileType = overlayTypes[z][centreX][centreY] + 1;
									byte orientation = overlayOrientations[z][centreX][centreY];
									Floor floor = Floor.floors[overlay - 1];
									int texture = floor.getTexture();
									int floorColour;
									int k23;

									if (texture >= 0) {
										k23 = Rasterizer.method369(texture);
										floorColour = -1;
									} else if (floor.getRgb() == 0xff00ff) {
										k23 = 0;
										floorColour = -2;
										texture = -1;
									} else {
										floorColour = encode(floor.getHue(), floor.getSaturation(), floor.getLuminance());
										k23 = Rasterizer.anIntArray1482[checkedLight(floor.getColour(), 96)];
									}

									scene.method279(z, centreX, centreY, tileType, orientation, texture, centreZ, eastZ,
											northEastZ, northZ, light(colour, j20), light(colour, k20),
											light(colour, l20), light(colour, i21), checkedLight(floorColour, j20),
											checkedLight(floorColour, k20), checkedLight(floorColour, l20),
											checkedLight(floorColour, i21), i22, k23);
								}
							}
						}
					}
				}
			}

			for (int y = 1; y < length - 1; y++) {
				for (int x = 1; x < width - 1; x++) {
					scene.setCollisionPlane(x, y, z, getCollisionPlane(x, y, z));
				}
			}
		}

		scene.method305(64, -50, -10, -50, 768);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < length; y++) {
				if ((tileFlags[1][x][y] & BRIDGE_TILE) != 0) {
					scene.method276(x, y);
				}
			}
		}

		int flag = 1;
		int j2 = 2;
		int k2 = 4;
		for (int plane = 0; plane < 4; plane++) {
			if (plane > 0) {
				flag <<= 3;
				j2 <<= 3;
				k2 <<= 3;
			}

			for (int z = 0; z <= plane; z++) {
				for (int y = 0; y <= length; y++) {
					for (int x = 0; x <= width; x++) {
						if ((anIntArrayArrayArray135[z][x][y] & flag) != 0) {
							int currentY = y;
							int l5 = y;
							int i7 = z;
							int k8 = z;

							for (; currentY > 0 && (anIntArrayArrayArray135[z][x][currentY - 1] & flag) != 0; currentY--) {
								;
							}

							for (; l5 < length && (anIntArrayArrayArray135[z][x][l5 + 1] & flag) != 0; l5++) {
								;
							}

							label0: for (; i7 > 0; i7--) {
								for (int j10 = currentY; j10 <= l5; j10++) {
									if ((anIntArrayArrayArray135[i7 - 1][x][j10] & flag) == 0) {
										break label0;
									}
								}
							}

							label1: for (; k8 < plane; k8++) {
								for (int k10 = currentY; k10 <= l5; k10++) {
									if ((anIntArrayArrayArray135[k8 + 1][x][k10] & flag) == 0) {
										break label1;
									}
								}
							}

							int l10 = (k8 + 1 - i7) * (l5 - currentY + 1);
							if (l10 >= 8) {
								char c1 = '\360';
								int k14 = tileHeights[k8][x][currentY] - c1;
								int l15 = tileHeights[i7][x][currentY];
								SceneGraph.method277(plane, x * 128, l15, x * 128, l5 * 128 + 128, k14, currentY * 128, 1);
								for (int l16 = i7; l16 <= k8; l16++) {
									for (int l17 = currentY; l17 <= l5; l17++) {
										anIntArrayArrayArray135[l16][x][l17] &= ~flag;
									}
								}
							}
						}

						if ((anIntArrayArrayArray135[z][x][y] & j2) != 0) {
							int l4 = x;
							int i6 = x;
							int j7 = z;
							int l8 = z;
							for (; l4 > 0 && (anIntArrayArrayArray135[z][l4 - 1][y] & j2) != 0; l4--) {

							}
							for (; i6 < width && (anIntArrayArrayArray135[z][i6 + 1][y] & j2) != 0; i6++) {

							}
							label2: for (; j7 > 0; j7--) {
								for (int i11 = l4; i11 <= i6; i11++) {
									if ((anIntArrayArrayArray135[j7 - 1][i11][y] & j2) == 0) {
										break label2;
									}
								}
							}

							label3: for (; l8 < plane; l8++) {
								for (int j11 = l4; j11 <= i6; j11++) {
									if ((anIntArrayArrayArray135[l8 + 1][j11][y] & j2) == 0) {
										break label3;
									}
								}
							}

							int k11 = (l8 + 1 - j7) * (i6 - l4 + 1);
							if (k11 >= 8) {
								char c2 = '\360';
								int l14 = tileHeights[l8][l4][y] - c2;
								int i16 = tileHeights[j7][l4][y];
								SceneGraph.method277(plane, l4 * 128, i16, i6 * 128 + 128, y * 128, l14, y * 128, 2);
								for (int i17 = j7; i17 <= l8; i17++) {
									for (int i18 = l4; i18 <= i6; i18++) {
										anIntArrayArrayArray135[i17][i18][y] &= ~j2;
									}
								}
							}
						}

						if ((anIntArrayArrayArray135[z][x][y] & k2) != 0) {
							int i5 = x;
							int j6 = x;
							int k7 = y;
							int i9 = y;
							for (; k7 > 0 && (anIntArrayArrayArray135[z][x][k7 - 1] & k2) != 0; k7--) {

							}
							for (; i9 < length && (anIntArrayArrayArray135[z][x][i9 + 1] & k2) != 0; i9++) {

							}
							label4: for (; i5 > 0; i5--) {
								for (int l11 = k7; l11 <= i9; l11++) {
									if ((anIntArrayArrayArray135[z][i5 - 1][l11] & k2) == 0) {
										break label4;
									}
								}
							}

							label5: for (; j6 < width; j6++) {
								for (int i12 = k7; i12 <= i9; i12++) {
									if ((anIntArrayArrayArray135[z][j6 + 1][i12] & k2) == 0) {
										break label5;
									}
								}
							}

							if ((j6 - i5 + 1) * (i9 - k7 + 1) >= 4) {
								int j12 = tileHeights[z][i5][k7];
								SceneGraph.method277(plane, i5 * 128, j12, j6 * 128 + 128, i9 * 128 + 128, j12, k7 * 128, 4);
								for (int k13 = i5; k13 <= j6; k13++) {
									for (int i15 = k7; i15 <= i9; i15++) {
										anIntArrayArrayArray135[z][k13][i15] &= ~k2;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public final void method174(int topLeftRegionX, int topLeftRegionY, int dx, int dy) {
		for (int y = topLeftRegionY; y <= topLeftRegionY + dy; y++) {
			for (int x = topLeftRegionX; x <= topLeftRegionX + dx; x++) {
				if (x >= 0 && x < width && y >= 0 && y < length) {
					shading[0][x][y] = 127;

					if (x == topLeftRegionX && x > 0) {
						tileHeights[0][x][y] = tileHeights[0][x - 1][y];
					}

					if (x == topLeftRegionX + dx && x < width - 1) {
						tileHeights[0][x][y] = tileHeights[0][x + 1][y];
					}

					if (y == topLeftRegionY && y > 0) {
						tileHeights[0][x][y] = tileHeights[0][x][y - 1];
					}

					if (y == topLeftRegionY + dy && y < length - 1) {
						tileHeights[0][x][y] = tileHeights[0][x][y + 1];
					}
				}
			}
		}
	}

	private final void decodeMapData(Buffer buffer, int x, int y, int z, int regionX, int regionY, int rotation) {
		if (x >= 0 && x < 104 && y >= 0 && y < 104) {
			tileFlags[z][x][y] = 0;
			do {
				int type = buffer.readUByte();

				if (type == 0) {
					if (z == 0) {
						tileHeights[0][x][y] = -calculateHeight(0xe3b7b + x + regionX, 0x87cce + y + regionY) * 8;
					} else {
						tileHeights[z][x][y] = tileHeights[z - 1][x][y] - 240;
					}

					return;
				} else if (type == 1) {
					int height = buffer.readUByte();
					if (height == 1) {
						height = 0;
					}

					if (z == 0) {
						tileHeights[0][x][y] = -height * 8;
					} else {
						tileHeights[z][x][y] = tileHeights[z - 1][x][y] - height * 8;
					}

					return;
				} else if (type <= 49) {
					overlays[z][x][y] = buffer.readByte();
					overlayTypes[z][x][y] = (byte) ((type - 2) / 4);
					overlayOrientations[z][x][y] = (byte) (type - 2 + rotation & 3);
				} else if (type <= 81) {
					tileFlags[z][x][y] = (byte) (type - 49);
				} else {
					underlays[z][x][y] = (byte) (type - 81);
				}
			} while (true);
		}

		do {
			int in = buffer.readUByte();
			if (in == 0) {
				break;
			} else if (in == 1) {
				buffer.readUByte();
				return;
			} else if (in <= 49) {
				buffer.readUByte();
			}
		} while (true);
	}

	/**
	 * Encodes the hue, saturation, and luminance into a colour value.
	 * 
	 * @param hue The hue.
	 * @param saturation The saturation.
	 * @param luminance The luminance.
	 * @return The colour.
	 */
	private final int encode(int hue, int saturation, int luminance) {
		if (luminance > 179) {
			saturation /= 2;
		}
		if (luminance > 192) {
			saturation /= 2;
		}
		if (luminance > 217) {
			saturation /= 2;
		}
		if (luminance > 243) {
			saturation /= 2;
		}

		return (hue / 4 << 10) + (saturation / 32 << 7) + luminance / 2;
	}

	private final void method175(CollisionMap map, SceneGraph scene, int id, int x, int y, int z, int type, int orientation) {
		if (lowMemory && (tileFlags[0][x][y] & BRIDGE_TILE) == 0) {
			if ((tileFlags[z][x][y] & 0x10) != 0 || getCollisionPlane(x, y, z) != currentPlane) {
				return;
			}
		}
		
		maximumPlane = Math.min(z, maximumPlane);

		int centre = tileHeights[z][x][y];
		int east = tileHeights[z][x + 1][y];
		int northEast = tileHeights[z][x + 1][y + 1];
		int north = tileHeights[z][x][y + 1];
		int mean = centre + east + northEast + north >> 2;
		ObjectDefinition definition = ObjectDefinition.lookup(id);
		int key = x | (y << 7) | (id << 14) | 0x40000000;

		if (!definition.isInteractive()) {
			key |= 0x80000000; // msb
		}
		byte config = (byte) ((orientation << 6) | type);

		if (type == 22) {
			if (lowMemory && !definition.isInteractive() && !definition.obstructsGround()) {
				return;
			}

			Object object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(22, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, 22, centre, east, northEast, north, definition.getAnimation(),
						true);
			}

			scene.addFloorDecoration(x, y, z, (Renderable) object, key, config, mean);
			if (definition.isSolid() && definition.isInteractive() && map != null) {
				map.block(x, y);
			}
			return;
		}

		if (type == 10 || type == 11) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(10, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, 10, centre, east, northEast, north, definition.getAnimation(),
						true);
			}

			if (object != null) {
				int yaw = 0;
				if (type == 11) {
					yaw += 256;
				}
				int width;
				int length;

				if (orientation == 1 || orientation == 3) {
					width = definition.getLength();
					length = definition.getWidth();
				} else {
					width = definition.getWidth();
					length = definition.getLength();
				}

				if (scene.addObject(x, y, z, width, length, object, key, config, yaw, mean) && definition.castsShadow()) {
					Model model;
					if (object instanceof Model) {
						model = (Model) object;
					} else {
						model = definition.modelAt(10, orientation, centre, east, northEast, north, -1);
					}

					if (model != null) {
						for (int dx = 0; dx <= width; dx++) {
							for (int dy = 0; dy <= length; dy++) {
								int l5 = Math.max(30, model.anInt1650 / 4);

								if (l5 > shading[z][x + dx][y + dy]) {
									shading[z][x + dx][y + dy] = (byte) l5;
								}
							}
						}
					}
				}
			}
			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, definition.getWidth(), definition.getLength(), definition.isImpenetrable(), orientation);
			}
			return;
		}
		if (type >= 12) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(type, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, type, centre, east, northEast, north, definition.getAnimation(),
						true);
			}

			scene.addObject(x, y, z, 1, 1, object, key, config, 0, mean);
			if (type >= 12 && type <= 17 && type != 13 && z > 0) {
				anIntArrayArrayArray135[z][x][y] |= 0x924;
			}

			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, definition.getWidth(), definition.getLength(), definition.isImpenetrable(), orientation);
			}
			return;
		}
		if (type == 0) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(0, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, 0, centre, east, northEast, north, definition.getAnimation(), true);
			}
			scene.addWall(key, x, y, z, anIntArray152[orientation], object, config, null, mean, 0);
			if (orientation == 0) {
				if (definition.castsShadow()) {
					shading[z][x][y] = 50;
					shading[z][x][y + 1] = 50;
				}
				if (definition.occludes()) {
					anIntArrayArrayArray135[z][x][y] |= 0x249;
				}
			} else if (orientation == 1) {
				if (definition.castsShadow()) {
					shading[z][x][y + 1] = 50;
					shading[z][x + 1][y + 1] = 50;
				}
				if (definition.occludes()) {
					anIntArrayArrayArray135[z][x][y + 1] |= 0x492;
				}
			} else if (orientation == 2) {
				if (definition.castsShadow()) {
					shading[z][x + 1][y] = 50;
					shading[z][x + 1][y + 1] = 50;
				}
				if (definition.occludes()) {
					anIntArrayArrayArray135[z][x + 1][y] |= 0x249;
				}
			} else if (orientation == 3) {
				if (definition.castsShadow()) {
					shading[z][x][y] = 50;
					shading[z][x + 1][y] = 50;
				}
				if (definition.occludes()) {
					anIntArrayArrayArray135[z][x][y] |= 0x492;
				}
			}
			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
			if (definition.getDecorDisplacement() != 16) {
				scene.displaceWallDecor(x, y, z, definition.getDecorDisplacement());
			}
			return;
		}
		if (type == 1) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(1, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, 1, centre, east, northEast, north, definition.getAnimation(), true);
			}

			scene.addWall(key, x, y, z, anIntArray140[orientation], object, config, null, mean, 0);
			if (definition.castsShadow()) {
				if (orientation == 0) {
					shading[z][x][y + 1] = 50;
				} else if (orientation == 1) {
					shading[z][x + 1][y + 1] = 50;
				} else if (orientation == 2) {
					shading[z][x + 1][y] = 50;
				} else if (orientation == 3) {
					shading[z][x][y] = 50;
				}
			}
			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
			return;
		}
		if (type == 2) {
			int oppositeOrientation = orientation + 1 & 3;
			Renderable obj11;
			Renderable obj12;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				obj11 = definition.modelAt(2, 4 + orientation, centre, east, northEast, north, -1);
				obj12 = definition.modelAt(2, oppositeOrientation, centre, east, northEast, north, -1);
			} else {
				obj11 = new RenderableObject(id, 4 + orientation, 2, centre, east, northEast, north, definition.getAnimation(),
						true);
				obj12 = new RenderableObject(id, oppositeOrientation, 2, centre, east, northEast, north,
						definition.getAnimation(), true);
			}
			scene.addWall(key, x, y, z, anIntArray152[orientation], obj11, config, obj12, mean,
					anIntArray152[oppositeOrientation]);
			if (definition.occludes()) {
				if (orientation == 0) {
					anIntArrayArrayArray135[z][x][y] |= 0x249;
					anIntArrayArrayArray135[z][x][y + 1] |= 0x492;
				} else if (orientation == 1) {
					anIntArrayArrayArray135[z][x][y + 1] |= 0x492;
					anIntArrayArrayArray135[z][x + 1][y] |= 0x249;
				} else if (orientation == 2) {
					anIntArrayArrayArray135[z][x + 1][y] |= 0x249;
					anIntArrayArrayArray135[z][x][y] |= 0x492;
				} else if (orientation == 3) {
					anIntArrayArrayArray135[z][x][y] |= 0x492;
					anIntArrayArrayArray135[z][x][y] |= 0x249;
				}
			}
			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
			if (definition.getDecorDisplacement() != 16) {
				scene.displaceWallDecor(x, y, z, definition.getDecorDisplacement());
			}
			return;
		}
		if (type == 3) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(3, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, 3, centre, east, northEast, north, definition.getAnimation(), true);
			}

			scene.addWall(key, x, y, z, anIntArray140[orientation], object, config, null, mean, 0);
			if (definition.castsShadow()) {
				if (orientation == 0) {
					shading[z][x][y + 1] = 50;
				} else if (orientation == 1) {
					shading[z][x + 1][y + 1] = 50;
				} else if (orientation == 2) {
					shading[z][x + 1][y] = 50;
				} else if (orientation == 3) {
					shading[z][x][y] = 50;
				}
			}
			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, orientation, type, definition.isImpenetrable());
			}
			return;
		}
		if (type == 9) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(type, orientation, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, orientation, type, centre, east, northEast, north, definition.getAnimation(),
						true);
			}
			scene.addObject(x, y, z, 1, 1, object, key, config, 0, mean);
			if (definition.isSolid() && map != null) {
				map.flagObject(x, y, definition.getWidth(), definition.getLength(), definition.isImpenetrable(), orientation);
			}
			return;
		}

		if (definition.contoursGround()) {
			if (orientation == 1) {
				int tmp = north;
				north = northEast;
				northEast = east;
				east = centre;
				centre = tmp;
			} else if (orientation == 2) {
				int tmp = north;
				north = east;
				east = tmp;
				tmp = northEast;
				northEast = centre;
				centre = tmp;
			} else if (orientation == 3) {
				int tmp = north;
				north = centre;
				centre = east;
				east = northEast;
				northEast = tmp;
			}
		}

		if (type == 4) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(4, 0, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, 0, 4, centre, east, northEast, north, definition.getAnimation(), true);
			}
			scene.addWallDecoration(key, y, orientation * 512, z, 0, mean, object, x, config, 0, anIntArray152[orientation]);
			return;
		}
		if (type == 5) {
			int displacement = 16;
			int existing = scene.getWallKey(x, y, z);
			if (existing > 0) {
				displacement = ObjectDefinition.lookup(existing >> 14 & 0x7fff).getDecorDisplacement();
			}
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(4, 0, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, 0, 4, centre, east, northEast, north, definition.getAnimation(), true);
			}
			scene.addWallDecoration(key, y, orientation * 512, z, COSINE_VERTICES[orientation] * displacement, mean, object, x,
					config, SINE_VERTICIES[orientation] * displacement, anIntArray152[orientation]);
			return;
		}
		if (type == 6) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(4, 0, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, 0, 4, centre, east, northEast, north, definition.getAnimation(), true);
			}
			scene.addWallDecoration(key, y, orientation, z, 0, mean, object, x, config, 0, 256);
			return;
		}
		if (type == 7) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(4, 0, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, 0, 4, centre, east, northEast, north, definition.getAnimation(), true);
			}
			scene.addWallDecoration(key, y, orientation, z, 0, mean, object, x, config, 0, 512);
			return;
		}
		if (type == 8) {
			Renderable object;
			if (definition.getAnimation() == -1 && definition.getMorphisms() == null) {
				object = definition.modelAt(4, 0, centre, east, northEast, north, -1);
			} else {
				object = new RenderableObject(id, 0, 4, centre, east, northEast, north, definition.getAnimation(), true);
			}
			scene.addWallDecoration(key, y, orientation, z, 0, mean, object, x, config, 0, 768);
		}
	}

	private final int checkedLight(int colour, int light) {
		if (colour == -2) {
			return 0xbc614e;
		}

		if (colour == -1) {
			if (light < 0) {
				light = 0;
			} else if (light > 127) {
				light = 127;
			}
			return 127 - light;
		}

		light = light * (colour & 0x7f) / 128;
		if (light < 2) {
			light = 2;
		} else if (light > 126) {
			light = 126;
		}
		return (colour & 0xff80) + light;
	}

}