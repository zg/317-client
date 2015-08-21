package com.jagex.map;

import com.jagex.draw.Raster;
import com.jagex.draw.Rasterizer;
import com.jagex.entity.GameObject;
import com.jagex.entity.Renderable;
import com.jagex.entity.model.Model;
import com.jagex.entity.model.VertexNormal;
import com.jagex.link.Deque;
import com.jagex.map.object.GroundDecoration;
import com.jagex.map.object.Wall;
import com.jagex.map.object.WallDecoration;

public class SceneGraph {

	public static int anInt470 = -1;
	public static int anInt471 = -1;
	public static int anInt475;
	public static boolean lowMemory = true;
	public final static int PLANE_COUNT = 4;
	static boolean aBoolean467;
	static boolean aBooleanArrayArray492[][];
	static boolean[][][][] aBooleanArrayArrayArrayArray491 = new boolean[8][32][51][51];
	static Deque aClass19_477 = new Deque();
	static SceneCluster[] aClass47Array476 = new SceneCluster[500];
	static int anInt446;
	static int anInt447;
	static int anInt448;
	static int anInt449;
	static int anInt450;
	static int anInt451;
	static int anInt452;
	static int anInt453;
	static int anInt454;
	static int anInt458;
	static int anInt459;
	static int anInt460;
	static int anInt461;
	static int anInt468;
	static int anInt469;
	static int anInt493;
	static int anInt494;
	static int anInt495;
	static int anInt496;
	static int anInt497;
	static int anInt498;
	static final int[] anIntArray463 = { 53, -53, -53, 53 };
	static final int[] anIntArray464 = { -53, -53, 53, 53 };
	static final int[] anIntArray465 = { -45, 45, 45, -45 };
	static final int[] anIntArray466 = { 45, 45, -45, -45 };
	static final int[] anIntArray478 = { 19, 55, 38, 155, 255, 110, 137, 205, 76 };
	static final int[] anIntArray479 = { 160, 192, 80, 96, 0, 144, 80, 48, 160 };
	static final int[] anIntArray480 = { 76, 8, 137, 4, 0, 1, 38, 2, 19 };
	static final int[] anIntArray481 = { 0, 0, 2, 0, 0, 2, 1, 1, 0 };
	static final int[] anIntArray482 = { 2, 0, 0, 2, 0, 0, 0, 4, 4 };
	static final int[] anIntArray483 = { 0, 4, 4, 8, 0, 0, 8, 0, 0 };
	static final int[] anIntArray484 = { 1, 1, 0, 0, 0, 8, 0, 0, 8 };
	static final int[] anIntArray485 = { 41, 39248, 41, 4643, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 43086, 41, 41, 41, 41,
			41, 41, 41, 8602, 41, 28992, 41, 41, 41, 41, 41, 5056, 41, 41, 41, 7079, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
			3131, 41, 41, 41 };
	static int[] clusterCounts = new int[PLANE_COUNT];
	static SceneCluster[][] clusters = new SceneCluster[PLANE_COUNT][500];
	static int heightOffset;
	static GameObject[] interactables = new GameObject[100];
	static int xOffset;
	static int yOffset;

	public static void dispose() {
		interactables = null;
		clusterCounts = null;
		clusters = null;
		aClass19_477 = null;
		aBooleanArrayArrayArrayArray491 = null;
		aBooleanArrayArray492 = null;
	}

	public static void method277(int plane, int j, int k, int l, int i1, int j1, int l1, int i2) {
		SceneCluster cluster = new SceneCluster();
		cluster.anInt787 = j / 128;
		cluster.anInt788 = l / 128;
		cluster.anInt789 = l1 / 128;
		cluster.anInt790 = i1 / 128;
		cluster.anInt791 = i2;
		cluster.anInt792 = j;
		cluster.anInt793 = l;
		cluster.anInt794 = l1;
		cluster.anInt795 = i1;
		cluster.anInt796 = j1;
		cluster.anInt797 = k;
		clusters[plane][clusterCounts[plane]++] = cluster;
	}

	public static void method310(int i, int j, int k, int l, int ai[]) {
		anInt495 = 0;
		anInt496 = 0;
		anInt497 = k;
		anInt498 = l;
		anInt493 = k / 2;
		anInt494 = l / 2;
		boolean[][][][] aflag = new boolean[9][32][53][53];

		for (int i1 = 128; i1 <= 384; i1 += 32) {
			for (int j1 = 0; j1 < 2048; j1 += 64) {
				anInt458 = Model.SINE[i1];
				anInt459 = Model.COSINE[i1];
				anInt460 = Model.SINE[j1];
				anInt461 = Model.COSINE[j1];
				int l1 = (i1 - 128) / 32;
				int j2 = j1 / 64;
				for (int l2 = -26; l2 <= 26; l2++) {
					for (int j3 = -26; j3 <= 26; j3++) {
						int k3 = l2 * 128;
						int i4 = j3 * 128;
						boolean flag2 = false;
						for (int k4 = -i; k4 <= j; k4 += 128) {
							if (!method311(ai[l1] + k4, i4, k3)) {
								continue;
							}
							flag2 = true;
							break;
						}
						aflag[l1][j2][l2 + 25 + 1][j3 + 25 + 1] = flag2;
					}
				}
			}
		}

		for (int k1 = 0; k1 < 8; k1++) {
			for (int i2 = 0; i2 < 32; i2++) {
				for (int k2 = -25; k2 < 25; k2++) {
					for (int i3 = -25; i3 < 25; i3++) {
						boolean flag1 = false;
						label0: for (int l3 = -1; l3 <= 1; l3++) {
							for (int j4 = -1; j4 <= 1; j4++) {
								if (aflag[k1][i2][k2 + l3 + 25 + 1][i3 + j4 + 25 + 1]) {
									flag1 = true;
								} else if (aflag[k1][(i2 + 1) % 31][k2 + l3 + 25 + 1][i3 + j4 + 25 + 1]) {
									flag1 = true;
								} else if (aflag[k1 + 1][i2][k2 + l3 + 25 + 1][i3 + j4 + 25 + 1]) {
									flag1 = true;
								} else {
									if (!aflag[k1 + 1][(i2 + 1) % 31][k2 + l3 + 25 + 1][i3 + j4 + 25 + 1]) {
										continue;
									}
									flag1 = true;
								}
								break label0;
							}
						}
						aBooleanArrayArrayArrayArray491[k1][i2][k2 + 25][i3 + 25] = flag1;
					}
				}
			}
		}
	}

	public static boolean method311(int i, int j, int k) {
		int l = j * anInt460 + k * anInt461 >> 16;
		int i1 = j * anInt461 - k * anInt460 >> 16;
		int j1 = i * anInt458 + i1 * anInt459 >> 16;
		int k1 = i * anInt459 - i1 * anInt458 >> 16;
		if (j1 < 50 || j1 > 3500) {
			return false;
		}
		int l1 = anInt493 + (l << 9) / j1;
		int i2 = anInt494 + (k1 << 9) / j1;
		return l1 >= anInt495 && l1 <= anInt497 && i2 >= anInt496 && i2 <= anInt498;
	}

	GameObject aClass28Array444[];
	int anInt443;
	int anInt488;
	int anIntArray486[];
	int anIntArray487[];
	int anIntArrayArray489[][] = { new int[16], { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1 }, { 1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 },
			{ 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1 },
			{ 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1 } };
	int anIntArrayArray490[][] = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
			{ 12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3 }, { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 },
			{ 3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12 } };
	int tileHeights[][][];
	int anIntArrayArrayArray445[][][];
	int length;
	int activePlane;
	int planeCount;
	SceneTile[][][] tiles;
	int width;

	public SceneGraph(int width, int length, int planes, int[][][] tileHeights) {
		aClass28Array444 = new GameObject[5000];
		anIntArray486 = new int[10000];
		anIntArray487 = new int[10000];
		planeCount = planes;
		this.width = width;
		this.length = length;
		tiles = new SceneTile[planes][width][length];
		anIntArrayArrayArray445 = new int[planes][width + 1][length + 1];
		this.tileHeights = tileHeights;
		reset();
	}

	public boolean addEntity(int x, int y, int plane, Renderable renderable, int yaw, int key, int renderHeight, int delta,
			boolean accountForYaw) {
		if (renderable == null) {
			return true;
		}

		int minX = x - delta;
		int minY = y - delta;
		int maxX = x + delta;
		int maxY = y + delta;

		if (accountForYaw) {
			if (yaw > 640 && yaw < 1408) {
				maxY += 128;
			}
			if (yaw > 1152 && yaw < 1920) {
				maxX += 128;
			}
			if (yaw > 1664 || yaw < 384) {
				minY -= 128;
			}
			if (yaw > 128 && yaw < 896) {
				minX -= 128;
			}
		}

		minX /= 128;
		minY /= 128;
		maxX /= 128;
		maxY /= 128;
		return addRenderable(plane, minX, minY, maxX - minX + 1, maxY - minY + 1, x, y, renderHeight, renderable, yaw, true, key,
				(byte) 0);
	}

	public void addFloorDecoration(int x, int y, int z, Renderable renderable, int key, byte config, int meanY) {
		if (renderable == null) {
			return;
		}
		GroundDecoration decoration = new GroundDecoration();
		decoration.setRenderable(renderable);
		decoration.setX(x * 128 + 64);
		decoration.setY(y * 128 + 64);
		decoration.setHeight(meanY);
		decoration.setKey(key);
		decoration.setConfig(config);
		if (tiles[z][x][y] == null) {
			tiles[z][x][y] = new SceneTile(x, y, z);
		}

		tiles[z][x][y].groundDecoration = decoration;
	}

	public void addGroundItem(int x, int y, int z, int key, Renderable primary, Renderable secondary, Renderable tertiary,
			int plane) {
		GroundItem item = new GroundItem();
		item.setPrimary(primary);
		item.setX(x * 128 + 64);
		item.setY(y * 128 + 64);
		item.setPlane(plane);
		item.setKey(key);
		item.setTertiary(secondary);
		item.setSecondary(tertiary);
		int itemHeight = 0;
		SceneTile tile = tiles[z][x][y];

		if (tile != null) {
			for (int i = 0; i < tile.objectCount; i++) {
				if (tile.gameObjects[i].renderable instanceof Model) {
					int l1 = ((Model) tile.gameObjects[i].renderable).anInt1654;
					if (l1 > itemHeight) {
						itemHeight = l1;
					}
				}
			}

		}
		item.setItemHeight(itemHeight);
		if (tiles[z][x][y] == null) {
			tiles[z][x][y] = new SceneTile(x, y, z);
		}
		tiles[z][x][y].groundItem = item;
	}

	public boolean addObject(int x, int y, int plane, int width, int length, Renderable renderable, int key, byte config,
			int yaw, int j) {
		if (renderable == null) {
			return true;
		}

		int absoluteX = x * 128 + 64 * width;
		int absoluteY = y * 128 + 64 * length;
		return addRenderable(plane, x, y, width, length, absoluteX, absoluteY, j, renderable, yaw, false, key, config);
	}

	public boolean addRenderable(int plane, int worldY, Renderable renderable, int orientation, int i1, int j1, int k1, int l1,
			int i2, int j2, int k2) {
		if (renderable == null) {
			return true;
		}

		return addRenderable(plane, l1, k2, i2 - l1 + 1, i1 - k2 + 1, j1, worldY, k1, renderable, orientation, true, j2, (byte) 0);
	}

	public void addWall(int key, int x, int y, int plane, int i, Renderable primary, byte config, Renderable secondary,
			int height, int j1) {
		if (primary == null && secondary == null) {
			return;
		}

		Wall wall = new Wall();
		wall.setKey(key);
		wall.setConfig(config);
		wall.setPositionX(x * 128 + 64);
		wall.setPositionY(y * 128 + 64);
		wall.setHeight(height);
		wall.setPrimary(primary);
		wall.setSecondary(secondary);
		wall.anInt276 = i;
		wall.anInt277 = j1;

		for (int z = plane; z >= 0; z--) {
			if (tiles[z][x][y] == null) {
				tiles[z][x][y] = new SceneTile(x, y, z);
			}
		}

		tiles[plane][x][y].wall = wall;
	}

	public void addWallDecoration(int key, int y, int orientation, int plane, int xDisplacement, int height,
			Renderable renderable, int x, byte config, int yDisplacement, int attributes) {
		if (renderable == null) {
			return;
		}

		WallDecoration decoration = new WallDecoration();
		decoration.setKey(key);
		decoration.setConfig(config);
		decoration.setX(x * 128 + 64 + xDisplacement);
		decoration.setY(y * 128 + 64 + yDisplacement);
		decoration.setHeight(height);
		decoration.setRenderable(renderable);
		decoration.setAttributes(attributes);
		decoration.setOrientation(orientation);

		for (int z = plane; z >= 0; z--) {
			if (tiles[z][x][y] == null) {
				tiles[z][x][y] = new SceneTile(x, y, z);
			}
		}

		tiles[plane][x][y].wallDecoration = decoration;
	}

	public void clearGroundItem(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return;
		}

		tile.groundItem = null;
	}

	public void displaceWallDecor(int x, int y, int z, int displacement) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return;
		}

		WallDecoration decoration = tile.wallDecoration;
		if (decoration == null) {
			return;
		}

		int absX = x * 128 + 64;
		int absY = y * 128 + 64;
		decoration.setX(absX + (decoration.getX() - absX) * displacement / 16);
		decoration.setY(absY + (decoration.getY() - absY) * displacement / 16);
	}

	public void fill(int plane) {
		this.activePlane = plane;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < length; y++) {
				if (tiles[plane][x][y] == null) {
					tiles[plane][x][y] = new SceneTile(x, y, plane);
				}
			}
		}
	}

	public GameObject firstGameObject(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return null;
		}

		for (int index = 0; index < tile.objectCount; index++) {
			GameObject interactable = tile.gameObjects[index];
			if ((interactable.key >> 29 & 3) == 2 && interactable.positionX == x && interactable.positionY == y) {
				return interactable;
			}
		}
		return null;
	}

	public int getConfig(int x, int y, int z, int key) {
		SceneTile tile = tiles[z][x][y];

		if (tile == null) {
			return -1;
		} else if (tile.wall != null && tile.wall.getKey() == key) {
			return tile.wall.getConfig() & 0xff;
		} else if (tile.wallDecoration != null && tile.wallDecoration.getKey() == key) {
			return tile.wallDecoration.getConfig() & 0xff;
		} else if (tile.groundDecoration != null && tile.groundDecoration.getKey() == key) {
			return tile.groundDecoration.getConfig() & 0xff;
		}

		for (int index = 0; index < tile.objectCount; index++) {
			if (tile.gameObjects[index].key == key) {
				return tile.gameObjects[index].config & 0xff;
			}
		}

		return -1;
	}

	public int getFloorDecorationKey(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null || tile.groundDecoration == null) {
			return 0;
		}

		return tile.groundDecoration.getKey();
	}

	public int getInteractableObjectKey(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return 0;
		}

		for (int index = 0; index < tile.objectCount; index++) {
			GameObject object = tile.gameObjects[index];

			if ((object.key >> 29 & 3) == 2 && object.positionX == x && object.positionY == y) {
				return object.key;
			}
		}

		return 0;
	}

	public GroundDecoration getTileFloorDecoration(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null || tile.groundDecoration == null) {
			return null;
		}

		return tile.groundDecoration;
	}

	public Wall getTileWall(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return null;
		}

		return tile.wall;
	}

	public WallDecoration getTileWallDecoration(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return null;
		}

		return tile.wallDecoration;
	}

	public int getWallDecorationKey(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null || tile.wallDecoration == null) {
			return 0;
		}

		return tile.wallDecoration.getKey();
	}

	public int getWallKey(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null || tile.wall == null) {
			return 0;
		}

		return tile.wall.getKey();
	}

	public void method276(int x, int y) {
		SceneTile tile = tiles[0][x][y];
		for (int z = 0; z < 3; z++) {
			SceneTile above = tiles[z][x][y] = tiles[z + 1][x][y];

			if (above != null) {
				above.plane--;

				for (int index = 0; index < above.objectCount; index++) {
					GameObject object = above.gameObjects[index];
					if ((object.key >> 29 & 3) == 2 && object.positionX == x && object.positionY == y) {
						object.plane--;
					}
				}
			}
		}

		if (tiles[0][x][y] == null) {
			tiles[0][x][y] = new SceneTile(x, y, 0);
		}

		tiles[0][x][y].aClass30_Sub3_1329 = tile;
		tiles[3][x][y] = null;
	}

	public void method279(int plane, int x, int y, int type, int orientation, int texture, int centreZ, int eastZ,
			int northEastZ, int northZ, int k2, int l2, int i3, int j3, int k3, int l3, int i4, int j4, int k4, int l4) {
		if (type == 0) {
			SimpleTile tile = new SimpleTile(k2, l2, i3, j3, -1, k4, false);
			for (int z = plane; z >= 0; z--) {
				if (tiles[z][x][y] == null) {
					tiles[z][x][y] = new SceneTile(x, y, z);
				}
			}

			tiles[plane][x][y].simple = tile;
		} else if (type == 1) {
			SimpleTile tile = new SimpleTile(k3, l3, i4, j4, texture, l4, centreZ == eastZ && centreZ == northEastZ
					&& centreZ == northZ);
			for (int z = plane; z >= 0; z--) {
				if (tiles[z][x][y] == null) {
					tiles[z][x][y] = new SceneTile(x, y, z);
				}
			}

			tiles[plane][x][y].simple = tile;
		} else {
			ShapedTile tile = new ShapedTile(y, k3, j3, northEastZ, texture, i4, orientation, k2, k4, i3, northZ, eastZ,
					centreZ, type, j4, l3, l2, x, l4);
			for (int z = plane; z >= 0; z--) {
				if (tiles[z][x][y] == null) {
					tiles[z][x][y] = new SceneTile(x, y, z);
				}
			}

			tiles[plane][x][y].shape = tile;
		}
	}

	public void method288() {
		for (int index = 0; index < anInt443; index++) {
			GameObject object = aClass28Array444[index];
			removeInteractable(object);
			aClass28Array444[index] = null;
		}

		anInt443 = 0;
	}

	public void method305(int lighting, int drawX, int drawY, int drawZ, int l) {
		int length = (int) Math.sqrt(drawX * drawX + drawY * drawY + drawZ * drawZ);
		int k1 = l * length >> 8;

		for (int z = 0; z < planeCount; z++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < this.length; y++) {
					SceneTile tile = tiles[z][x][y];

					if (tile != null) {
						Wall wall = tile.wall;

						if (wall != null && wall.getPrimary() != null && wall.getPrimary().hasNormals()) {
							method307(z, 1, 1, x, y, (Model) wall.getPrimary());

							if (wall.getSecondary() != null && wall.getSecondary().hasNormals()) {
								method307(z, 1, 1, x, y, (Model) wall.getSecondary());
								mergeNormals((Model) wall.getPrimary(), (Model) wall.getSecondary(), 0, 0, 0, false);
								((Model) wall.getSecondary()).method480(lighting, k1, drawX, drawY, drawZ);
							}
							((Model) wall.getPrimary()).method480(lighting, k1, drawX, drawY, drawZ);
						}

						for (int index = 0; index < tile.objectCount; index++) {
							GameObject object = tile.gameObjects[index];

							if (object != null && object.renderable != null && object.renderable.hasNormals()) {
								method307(z, object.maxX - object.positionX + 1, object.maxY - object.positionY + 1, x, y,
										(Model) object.renderable);
								((Model) object.renderable).method480(lighting, k1, drawX, drawY, drawZ);
							}
						}

						GroundDecoration decoration = tile.groundDecoration;
						if (decoration != null && decoration.getRenderable().hasNormals()) {
							method306((Model) decoration.getRenderable(), x, y, z);
							((Model) decoration.getRenderable()).method480(lighting, k1, drawX, drawY, drawZ);
						}
					}
				}
			}
		}
	}

	public void method309(int[] raster, int x, int y, int plane, int count, int j) {
		SceneTile tile = tiles[plane][x][y];
		if (tile == null) {
			return;
		}

		SimpleTile genericTile = tile.simple;
		if (genericTile != null) {
			int colour = genericTile.anInt722;
			if (colour == 0) {
				return;
			}

			for (int times = 0; times < 4; times++) {
				raster[count] = colour;
				raster[count + 1] = colour;
				raster[count + 2] = colour;
				raster[count + 3] = colour;
				count += j;
			}

			return;
		}

		ShapedTile shaped = tile.shape;
		if (shaped == null) {
			return;
		}
		int l1 = shaped.anInt684;
		int orientation = shaped.orientation;
		int defaultColour = shaped.anInt686;
		int primaryColour = shaped.anInt687;
		int[] primary = anIntArrayArray489[l1];
		int[] indices = anIntArrayArray490[orientation];
		int l2 = 0;

		if (defaultColour != 0) {
			for (int i3 = 0; i3 < 4; i3++) {
				raster[count] = primary[indices[l2++]] != 0 ? primaryColour : defaultColour;
				raster[count + 1] = primary[indices[l2++]] != 0 ? primaryColour : defaultColour;
				raster[count + 2] = primary[indices[l2++]] != 0 ? primaryColour : defaultColour;
				raster[count + 3] = primary[indices[l2++]] != 0 ? primaryColour : defaultColour;
				count += j;
			}
			return;
		}

		for (int j3 = 0; j3 < 4; j3++) {
			if (primary[indices[l2++]] != 0) {
				raster[count] = primaryColour;
			}
			if (primary[indices[l2++]] != 0) {
				raster[count + 1] = primaryColour;
			}
			if (primary[indices[l2++]] != 0) {
				raster[count + 2] = primaryColour;
			}
			if (primary[indices[l2++]] != 0) {
				raster[count + 3] = primaryColour;
			}
			count += j;
		}
	}

	public void method312(int i, int j) {
		aBoolean467 = true;
		anInt468 = j;
		anInt469 = i;
		anInt470 = -1;
		anInt471 = -1;
	}

	public void method313(int i, int j, int k, int l, int i1, int j1) {
		if (i < 0) {
			i = 0;
		} else if (i >= width * 128) {
			i = width * 128 - 1;
		}
		if (j < 0) {
			j = 0;
		} else if (j >= length * 128) {
			j = length * 128 - 1;
		}

		anInt448++;
		anInt458 = Model.SINE[j1];
		anInt459 = Model.COSINE[j1];
		anInt460 = Model.SINE[k];
		anInt461 = Model.COSINE[k];
		aBooleanArrayArray492 = aBooleanArrayArrayArrayArray491[(j1 - 128) / 32][k / 64];
		xOffset = i;
		heightOffset = l;
		yOffset = j;
		anInt453 = i / 128;
		anInt454 = j / 128;
		anInt447 = i1;
		anInt449 = anInt453 - 25;

		if (anInt449 < 0) {
			anInt449 = 0;
		}
		anInt451 = anInt454 - 25;
		if (anInt451 < 0) {
			anInt451 = 0;
		}
		anInt450 = anInt453 + 25;
		if (anInt450 > width) {
			anInt450 = width;
		}
		anInt452 = anInt454 + 25;
		if (anInt452 > length) {
			anInt452 = length;
		}
		method319();
		anInt446 = 0;

		for (int z = activePlane; z < planeCount; z++) {
			SceneTile[][] tiles = this.tiles[z];
			for (int x = anInt449; x < anInt450; x++) {
				for (int y = anInt451; y < anInt452; y++) {
					SceneTile tile = tiles[x][y];
					if (tile != null) {
						if (tile.collisionPlane > i1 || !aBooleanArrayArray492[x - anInt453 + 25][y - anInt454 + 25]
								&& tileHeights[z][x][y] - l < 2000) {
							tile.aBoolean1322 = false;
							tile.aBoolean1323 = false;
							tile.anInt1325 = 0;
						} else {
							tile.aBoolean1322 = true;
							tile.aBoolean1323 = true;
							if (tile.objectCount > 0) {
								tile.aBoolean1324 = true;
							} else {
								tile.aBoolean1324 = false;
							}
							anInt446++;
						}
					}
				}
			}
		}

		for (int z = activePlane; z < planeCount; z++) {
			SceneTile[][] tiles = this.tiles[z];
			for (int dx = -25; dx <= 0; dx++) {
				int maxX = anInt453 + dx;
				int minX = anInt453 - dx;
				if (maxX >= anInt449 || minX < anInt450) {
					for (int dy = -25; dy <= 0; dy++) {
						int maxY = anInt454 + dy;
						int minY = anInt454 - dy;

						if (maxX >= anInt449) {
							if (maxY >= anInt451) {
								SceneTile tile = tiles[maxX][maxY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}

							if (minY < anInt452) {
								SceneTile tile = tiles[maxX][minY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
						}

						if (minX < anInt450) {
							if (maxY >= anInt451) {
								SceneTile tile = tiles[minX][maxY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}

							if (minY < anInt452) {
								SceneTile tile = tiles[minX][minY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
						}

						if (anInt446 == 0) {
							aBoolean467 = false;
							return;
						}
					}
				}
			}
		}

		for (int j2 = activePlane; j2 < planeCount; j2++) {
			SceneTile[][] tiles = this.tiles[j2];
			for (int j3 = -25; j3 <= 0; j3++) {
				int l3 = anInt453 + j3;
				int j4 = anInt453 - j3;

				if (l3 >= anInt449 || j4 < anInt450) {
					for (int l4 = -25; l4 <= 0; l4++) {
						int j5 = anInt454 + l4;
						int k5 = anInt454 - l4;

						if (l3 >= anInt449) {
							if (j5 >= anInt451) {
								SceneTile tile = tiles[l3][j5];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}

							if (k5 < anInt452) {
								SceneTile tile = tiles[l3][k5];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
						}

						if (j4 < anInt450) {
							if (j5 >= anInt451) {
								SceneTile tile = tiles[j4][j5];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}

							if (k5 < anInt452) {
								SceneTile tile = tiles[j4][k5];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
						}

						if (anInt446 == 0) {
							aBoolean467 = false;
							return;
						}
					}
				}
			}
		}

		aBoolean467 = false;
	}

	public void method314(SceneTile newTile, boolean flag) {
		aClass19_477.pushBack(newTile);

		do {
			SceneTile front;

			do {
				front = (SceneTile) aClass19_477.popFront();
				if (front == null) {
					return;
				}
			} while (!front.aBoolean1323);

			int x = front.positionX;
			int y = front.positionY;
			int plane = front.plane;
			int l = front.anInt1310;
			SceneTile[][] planeTiles = this.tiles[plane];

			if (front.aBoolean1322) {
				if (flag) {
					if (plane > 0) {
						SceneTile tile = this.tiles[plane - 1][x][y];
						if (tile != null && tile.aBoolean1323) {
							continue;
						}
					}

					if (x <= anInt453 && x > anInt449) {
						SceneTile tile = planeTiles[x - 1][y];
						if (tile != null && tile.aBoolean1323 && (tile.aBoolean1322 || (front.attributes & 1) == 0)) {
							continue;
						}
					}

					if (x >= anInt453 && x < anInt450 - 1) {
						SceneTile tile = planeTiles[x + 1][y];
						if (tile != null && tile.aBoolean1323 && (tile.aBoolean1322 || (front.attributes & 4) == 0)) {
							continue;
						}
					}

					if (y <= anInt454 && y > anInt451) {
						SceneTile tile = planeTiles[x][y - 1];
						if (tile != null && tile.aBoolean1323 && (tile.aBoolean1322 || (front.attributes & 8) == 0)) {
							continue;
						}
					}

					if (y >= anInt454 && y < anInt452 - 1) {
						SceneTile tile = planeTiles[x][y + 1];
						if (tile != null && tile.aBoolean1323 && (tile.aBoolean1322 || (front.attributes & 2) == 0)) {
							continue;
						}
					}
				} else {
					flag = true;
				}

				front.aBoolean1322 = false;
				if (front.aClass30_Sub3_1329 != null) {
					SceneTile tile = front.aClass30_Sub3_1329;

					if (tile.simple != null) {
						if (!method320(x, y, 0)) {
							method315(tile.simple, 0, anInt458, anInt459, anInt460, anInt461, x, y);
						}
					} else if (tile.shape != null && !method320(x, y, 0)) {
						method316(x, anInt458, anInt460, tile.shape, anInt459, y, anInt461);
					}

					Wall wall = tile.wall;
					if (wall != null) {
						wall.getPrimary().render(wall.getPositionX() - xOffset, wall.getPositionY() - yOffset, 0, anInt458,
								anInt459, anInt460, anInt461, wall.getHeight() - heightOffset, wall.getKey());
					}

					for (int index = 0; index < tile.objectCount; index++) {
						GameObject object = tile.gameObjects[index];

						if (object != null) {
							object.renderable.render(object.centreX - xOffset, object.centreY - yOffset, object.yaw, anInt458,
									anInt459, anInt460, anInt461, object.renderHeight - heightOffset, object.key);
						}
					}
				}

				boolean flag1 = false;
				if (front.simple != null) {
					if (!method320(x, y, l)) {
						flag1 = true;
						method315(front.simple, l, anInt458, anInt459, anInt460, anInt461, x, y);
					}
				} else if (front.shape != null && !method320(x, y, l)) {
					flag1 = true;
					method316(x, anInt458, anInt460, front.shape, anInt459, y, anInt461);
				}

				int j1 = 0;
				int j2 = 0;
				Wall wall = front.wall;
				WallDecoration decoration = front.wallDecoration;

				if (wall != null || decoration != null) {
					if (anInt453 == x) {
						j1++;
					} else if (anInt453 < x) {
						j1 += 2;
					}
					if (anInt454 == y) {
						j1 += 3;
					} else if (anInt454 > y) {
						j1 += 6;
					}
					j2 = anIntArray478[j1];
					front.anInt1328 = anIntArray480[j1];
				}

				if (wall != null) {
					if ((wall.anInt276 & anIntArray479[j1]) != 0) {
						if (wall.anInt276 == 16) {
							front.anInt1325 = 3;
							front.anInt1326 = anIntArray481[j1];
							front.anInt1327 = 3 - front.anInt1326;
						} else if (wall.anInt276 == 32) {
							front.anInt1325 = 6;
							front.anInt1326 = anIntArray482[j1];
							front.anInt1327 = 6 - front.anInt1326;
						} else if (wall.anInt276 == 64) {
							front.anInt1325 = 12;
							front.anInt1326 = anIntArray483[j1];
							front.anInt1327 = 12 - front.anInt1326;
						} else {
							front.anInt1325 = 9;
							front.anInt1326 = anIntArray484[j1];
							front.anInt1327 = 9 - front.anInt1326;
						}
					} else {
						front.anInt1325 = 0;
					}
					if ((wall.anInt276 & j2) != 0 && !method321(x, y, l, wall.anInt276)) {
						wall.getPrimary().render(wall.getPositionX() - xOffset, wall.getPositionY() - yOffset, 0, anInt458,
								anInt459, anInt460, anInt461, wall.getHeight() - heightOffset, wall.getKey());
					}
					if ((wall.anInt277 & j2) != 0 && !method321(x, y, l, wall.anInt277)) {
						wall.getSecondary().render(wall.getPositionX() - xOffset, wall.getPositionY() - yOffset, 0, anInt458,
								anInt459, anInt460, anInt461, wall.getHeight() - heightOffset, wall.getKey());
					}
				}

				if (decoration != null && !method322(l, x, y, decoration.getRenderable().getModelHeight())) {
					if ((decoration.getAttributes() & j2) != 0) {
						decoration.getRenderable().render(decoration.getX() - xOffset, decoration.getY() - yOffset,
								decoration.getOrientation(), anInt458, anInt459, anInt460, anInt461,
								decoration.getHeight() - heightOffset, decoration.getKey());
					} else if ((decoration.getAttributes() & 0b11_0000_0000) != 0) { // type 6, 7, or 8
						int dx = decoration.getX() - xOffset;
						int height = decoration.getHeight() - heightOffset;
						int dy = decoration.getY() - yOffset;
						int orientation = decoration.getOrientation();
						int width;

						if (orientation == 1 || orientation == 2) {
							width = -dx;
						} else {
							width = dx;
						}

						int length;
						if (orientation == 2 || orientation == 3) {
							length = -dy;
						} else {
							length = dy;
						}

						if ((decoration.getAttributes() & 0b1_0000_0000) != 0 && length < width) { // type 6
							int renderX = dx + anIntArray463[orientation];
							int renderY = dy + anIntArray464[orientation];
							decoration.getRenderable().render(renderX, renderY, orientation * 512 + 256, anInt458, anInt459,
									anInt460, anInt461, height, decoration.getKey());
						}

						if ((decoration.getAttributes() & 0b10_0000_0000) != 0 && length > width) { // type 7
							int renderX = dx + anIntArray465[orientation];
							int renderY = dy + anIntArray466[orientation];
							decoration.getRenderable().render(renderX, renderY, orientation * 512 + 1280 & 0x7ff, anInt458,
									anInt459, anInt460, anInt461, height, decoration.getKey());
						}
					}
				}

				if (flag1) {
					GroundDecoration decor = front.groundDecoration;
					if (decor != null) {
						decor.getRenderable().render(decor.getX() - xOffset, decor.getY() - yOffset, 0, anInt458, anInt459,
								anInt460, anInt461, decor.getHeight() - heightOffset, decor.getKey());
					}

					GroundItem item = front.groundItem;
					if (item != null && item.getItemHeight() == 0) {
						if (item.getTertiary() != null) {
							item.getTertiary().render(item.getX() - xOffset, item.getY() - yOffset, 0, anInt458, anInt459,
									anInt460, anInt461, item.getPlane() - heightOffset, item.getKey());
						}

						if (item.getSecondary() != null) {
							item.getSecondary().render(item.getX() - xOffset, item.getY() - yOffset, 0, anInt458, anInt459,
									anInt460, anInt461, item.getPlane() - heightOffset, item.getKey());
						}

						if (item.getPrimary() != null) {
							item.getPrimary().render(item.getX() - xOffset, item.getY() - yOffset, 0, anInt458, anInt459,
									anInt460, anInt461, item.getPlane() - heightOffset, item.getKey());
						}
					}
				}
				int k4 = front.attributes;
				if (k4 != 0) {
					if (x < anInt453 && (k4 & 4) != 0) {
						SceneTile tile = planeTiles[x + 1][y];
						if (tile != null && tile.aBoolean1323) {
							aClass19_477.pushBack(tile);
						}
					}

					if (y < anInt454 && (k4 & 2) != 0) {
						SceneTile tile = planeTiles[x][y + 1];
						if (tile != null && tile.aBoolean1323) {
							aClass19_477.pushBack(tile);
						}
					}

					if (x > anInt453 && (k4 & 1) != 0) {
						SceneTile tile = planeTiles[x - 1][y];
						if (tile != null && tile.aBoolean1323) {
							aClass19_477.pushBack(tile);
						}
					}

					if (y > anInt454 && (k4 & 8) != 0) {
						SceneTile tile = planeTiles[x][y - 1];
						if (tile != null && tile.aBoolean1323) {
							aClass19_477.pushBack(tile);
						}
					}
				}
			}

			if (front.anInt1325 != 0) {
				boolean flag2 = true;
				for (int index = 0; index < front.objectCount; index++) {
					if (front.gameObjects[index].anInt528 == anInt448
							|| (front.objectAttributes[index] & front.anInt1325) != front.anInt1326) {
						continue;
					}

					flag2 = false;
					break;
				}

				if (flag2) {
					Wall wall = front.wall;
					if (!method321(x, y, l, wall.anInt276)) {
						wall.getPrimary().render(wall.getPositionX() - xOffset, wall.getPositionY() - yOffset, 0, anInt458,
								anInt459, anInt460, anInt461, wall.getHeight() - heightOffset, wall.getKey());
					}

					front.anInt1325 = 0;
				}
			}

			if (front.aBoolean1324) {
				try {
					int count = front.objectCount;
					front.aBoolean1324 = false;
					int l1 = 0;
					label0: for (int index = 0; index < count; index++) {
						GameObject object = front.gameObjects[index];
						if (object.anInt528 == anInt448) {
							continue;
						}

						for (int objectX = object.positionX; objectX <= object.maxX; objectX++) {
							for (int objectY = object.positionY; objectY <= object.maxY; objectY++) {
								SceneTile objectTile = planeTiles[objectX][objectY];

								if (objectTile.aBoolean1322) {
									front.aBoolean1324 = true;
								} else {
									if (objectTile.anInt1325 == 0) {
										continue;
									}
									int l6 = 0;
									if (objectX > object.positionX) {
										l6++;
									}
									if (objectX < object.maxX) {
										l6 += 4;
									}
									if (objectY > object.positionY) {
										l6 += 8;
									}
									if (objectY < object.maxY) {
										l6 += 2;
									}
									if ((l6 & objectTile.anInt1325) != front.anInt1327) {
										continue;
									}

									front.aBoolean1324 = true;
								}
								continue label0;
							}

						}

						interactables[l1++] = object;
						int i5 = anInt453 - object.positionX;
						int i6 = object.maxX - anInt453;

						if (i6 > i5) {
							i5 = i6;
						}

						int i7 = anInt454 - object.positionY;
						int j8 = object.maxY - anInt454;
						if (j8 > i7) {
							object.anInt527 = i5 + j8;
						} else {
							object.anInt527 = i5 + i7;
						}
					}

					while (l1 > 0) {
						int i3 = -50;
						int l3 = -1;
						for (int j5 = 0; j5 < l1; j5++) {
							GameObject object = interactables[j5];
							if (object.anInt528 != anInt448) {
								if (object.anInt527 > i3) {
									i3 = object.anInt527;
									l3 = j5;
								} else if (object.anInt527 == i3) {
									int j7 = object.centreX - xOffset;
									int k8 = object.centreY - yOffset;
									int l9 = interactables[l3].centreX - xOffset;
									int l10 = interactables[l3].centreY - yOffset;
									if (j7 * j7 + k8 * k8 > l9 * l9 + l10 * l10) {
										l3 = j5;
									}
								}
							}
						}

						if (l3 == -1) {
							break;
						}

						GameObject object = interactables[l3];
						object.anInt528 = anInt448;
						if (!method323(l, object.positionX, object.maxX, object.positionY, object.maxY,
								object.renderable.getModelHeight())) {
							object.renderable.render(object.centreX - xOffset, object.centreY - yOffset, object.yaw, anInt458,
									anInt459, anInt460, anInt461, object.renderHeight - heightOffset, object.key);
						}

						for (int k7 = object.positionX; k7 <= object.maxX; k7++) {
							for (int l8 = object.positionY; l8 <= object.maxY; l8++) {
								SceneTile class30_sub3_22 = planeTiles[k7][l8];

								if (class30_sub3_22.anInt1325 != 0) {
									aClass19_477.pushBack(class30_sub3_22);
								} else if ((k7 != x || l8 != y) && class30_sub3_22.aBoolean1323) {
									aClass19_477.pushBack(class30_sub3_22);
								}
							}

						}

					}
					if (front.aBoolean1324) {
						continue;
					}
				} catch (Exception _ex) {
					front.aBoolean1324 = false;
				}
			}

			if (!front.aBoolean1323 || front.anInt1325 != 0) {
				continue;
			}

			if (x <= anInt453 && x > anInt449) {
				SceneTile class30_sub3_8 = planeTiles[x - 1][y];
				if (class30_sub3_8 != null && class30_sub3_8.aBoolean1323) {
					continue;
				}
			}

			if (x >= anInt453 && x < anInt450 - 1) {
				SceneTile class30_sub3_9 = planeTiles[x + 1][y];
				if (class30_sub3_9 != null && class30_sub3_9.aBoolean1323) {
					continue;
				}
			}

			if (y <= anInt454 && y > anInt451) {
				SceneTile class30_sub3_10 = planeTiles[x][y - 1];
				if (class30_sub3_10 != null && class30_sub3_10.aBoolean1323) {
					continue;
				}
			}

			if (y >= anInt454 && y < anInt452 - 1) {
				SceneTile class30_sub3_11 = planeTiles[x][y + 1];
				if (class30_sub3_11 != null && class30_sub3_11.aBoolean1323) {
					continue;
				}
			}

			front.aBoolean1323 = false;
			anInt446--;
			GroundItem item = front.groundItem;

			if (item != null && item.getItemHeight() != 0) {
				if (item.getTertiary() != null) {
					item.getTertiary().render(item.getX() - xOffset, item.getY() - yOffset, 0, anInt458, anInt459, anInt460,
							anInt461, item.getPlane() - heightOffset - item.getItemHeight(), item.getKey());
				}

				if (item.getSecondary() != null) {
					item.getSecondary().render(item.getX() - xOffset, item.getY() - yOffset, 0, anInt458, anInt459, anInt460,
							anInt461, item.getPlane() - heightOffset - item.getItemHeight(), item.getKey());
				}

				if (item.getPrimary() != null) {
					item.getPrimary().render(item.getX() - xOffset, item.getY() - yOffset, 0, anInt458, anInt459, anInt460,
							anInt461, item.getPlane() - heightOffset - item.getItemHeight(), item.getKey());
				}
			}

			if (front.anInt1328 != 0) {
				WallDecoration decor = front.wallDecoration;
				if (decor != null && !method322(l, x, y, decor.getRenderable().getModelHeight())) {
					if ((decor.getAttributes() & front.anInt1328) != 0) {
						decor.getRenderable().render(decor.getX() - xOffset, decor.getY() - yOffset, decor.getOrientation(),
								anInt458, anInt459, anInt460, anInt461, decor.getHeight() - heightOffset, decor.getKey());
					} else if ((decor.getAttributes() & 0x300) != 0) {
						int l2 = decor.getX() - xOffset;
						int j3 = decor.getHeight() - heightOffset;
						int i4 = decor.getY() - yOffset;
						int orientation = decor.getOrientation();
						int j6;
						if (orientation == 1 || orientation == 2) {
							j6 = -l2;
						} else {
							j6 = l2;
						}
						int l7;
						if (orientation == 2 || orientation == 3) {
							l7 = -i4;
						} else {
							l7 = i4;
						}
						if ((decor.getAttributes() & 0x100) != 0 && l7 >= j6) {
							int i9 = l2 + anIntArray463[orientation];
							int i10 = i4 + anIntArray464[orientation];
							decor.getRenderable().render(i9, i10, orientation * 512 + 256, anInt458, anInt459, anInt460,
									anInt461, j3, decor.getKey());
						}
						if ((decor.getAttributes() & 0x200) != 0 && l7 <= j6) {
							int j9 = l2 + anIntArray465[orientation];
							int j10 = i4 + anIntArray466[orientation];
							decor.getRenderable().render(j9, j10, orientation * 512 + 1280 & 0x7ff, anInt458, anInt459, anInt460,
									anInt461, j3, decor.getKey());
						}
					}
				}
				Wall wall = front.wall;
				if (wall != null) {
					if ((wall.anInt277 & front.anInt1328) != 0 && !method321(x, y, l, wall.anInt277)) {
						wall.getSecondary().render(wall.getPositionX() - xOffset, wall.getPositionY() - yOffset, 0, anInt458,
								anInt459, anInt460, anInt461, wall.getHeight() - heightOffset, wall.getKey());
					}
					if ((wall.anInt276 & front.anInt1328) != 0 && !method321(x, y, l, wall.anInt276)) {
						wall.getPrimary().render(wall.getPositionX() - xOffset, wall.getPositionY() - yOffset, 0, anInt458,
								anInt459, anInt460, anInt461, wall.getHeight() - heightOffset, wall.getKey());
					}
				}
			}
			if (plane < planeCount - 1) {
				SceneTile class30_sub3_12 = tiles[plane + 1][x][y];
				if (class30_sub3_12 != null && class30_sub3_12.aBoolean1323) {
					aClass19_477.pushBack(class30_sub3_12);
				}
			}
			if (x < anInt453) {
				SceneTile class30_sub3_13 = planeTiles[x + 1][y];
				if (class30_sub3_13 != null && class30_sub3_13.aBoolean1323) {
					aClass19_477.pushBack(class30_sub3_13);
				}
			}
			if (y < anInt454) {
				SceneTile class30_sub3_14 = planeTiles[x][y + 1];
				if (class30_sub3_14 != null && class30_sub3_14.aBoolean1323) {
					aClass19_477.pushBack(class30_sub3_14);
				}
			}
			if (x > anInt453) {
				SceneTile class30_sub3_15 = planeTiles[x - 1][y];
				if (class30_sub3_15 != null && class30_sub3_15.aBoolean1323) {
					aClass19_477.pushBack(class30_sub3_15);
				}
			}
			if (y > anInt454) {
				SceneTile class30_sub3_16 = planeTiles[x][y - 1];
				if (class30_sub3_16 != null && class30_sub3_16.aBoolean1323) {
					aClass19_477.pushBack(class30_sub3_16);
				}
			}
		} while (true);
	}

	public void method315(SimpleTile tile, int plane, int j, int k, int l, int i1, int x, int y) {
		int l1;
		int i2 = l1 = (x << 7) - xOffset;
		int j2;
		int k2 = j2 = (y << 7) - yOffset;
		int l2;
		int i3 = l2 = i2 + 128;
		int j3;
		int k3 = j3 = k2 + 128;
		int l3 = tileHeights[plane][x][y] - heightOffset;
		int i4 = tileHeights[plane][x + 1][y] - heightOffset;
		int j4 = tileHeights[plane][x + 1][y + 1] - heightOffset;
		int k4 = tileHeights[plane][x][y + 1] - heightOffset;
		int l4 = k2 * l + i2 * i1 >> 16;
		k2 = k2 * i1 - i2 * l >> 16;
		i2 = l4;
		l4 = l3 * k - k2 * j >> 16;
		k2 = l3 * j + k2 * k >> 16;
		l3 = l4;
		if (k2 < 50) {
			return;
		}
		l4 = j2 * l + i3 * i1 >> 16;
		j2 = j2 * i1 - i3 * l >> 16;
		i3 = l4;
		l4 = i4 * k - j2 * j >> 16;
		j2 = i4 * j + j2 * k >> 16;
		i4 = l4;
		if (j2 < 50) {
			return;
		}
		l4 = k3 * l + l2 * i1 >> 16;
		k3 = k3 * i1 - l2 * l >> 16;
		l2 = l4;
		l4 = j4 * k - k3 * j >> 16;
		k3 = j4 * j + k3 * k >> 16;
		j4 = l4;
		if (k3 < 50) {
			return;
		}
		l4 = j3 * l + l1 * i1 >> 16;
		j3 = j3 * i1 - l1 * l >> 16;
		l1 = l4;
		l4 = k4 * k - j3 * j >> 16;
		j3 = k4 * j + j3 * k >> 16;
		k4 = l4;
		if (j3 < 50) {
			return;
		}
		int i5 = Rasterizer.originViewX + (i2 << 9) / k2;
		int j5 = Rasterizer.originViewY + (l3 << 9) / k2;
		int k5 = Rasterizer.originViewX + (i3 << 9) / j2;
		int l5 = Rasterizer.originViewY + (i4 << 9) / j2;
		int i6 = Rasterizer.originViewX + (l2 << 9) / k3;
		int j6 = Rasterizer.originViewY + (j4 << 9) / k3;
		int k6 = Rasterizer.originViewX + (l1 << 9) / j3;
		int l6 = Rasterizer.originViewY + (k4 << 9) / j3;
		Rasterizer.currentAlpha = 0;
		if ((i6 - k6) * (l5 - l6) - (j6 - l6) * (k5 - k6) > 0) {
			Rasterizer.aBoolean1462 = false;
			if (i6 < 0 || k6 < 0 || k5 < 0 || i6 > Raster.anInt1385 || k6 > Raster.anInt1385 || k5 > Raster.anInt1385) {
				Rasterizer.aBoolean1462 = true;
			}
			if (aBoolean467 && method318(anInt468, anInt469, j6, l6, l5, i6, k6, k5)) {
				anInt470 = x;
				anInt471 = y;
			}
			if (tile.texture == -1) {
				if (tile.anInt718 != 0xbc614e) {
					Rasterizer.method374(j6, l6, l5, i6, k6, k5, tile.anInt718, tile.anInt719, tile.anInt717);
				}
			} else if (!lowMemory) {
				if (tile.flat) {
					Rasterizer.method378(j6, l6, l5, i6, k6, k5, tile.anInt718, tile.anInt719, tile.anInt717, i2, i3, l1, l3, i4,
							k4, k2, j2, j3, tile.texture);
				} else {
					Rasterizer.method378(j6, l6, l5, i6, k6, k5, tile.anInt718, tile.anInt719, tile.anInt717, l2, l1, i3, j4, k4,
							i4, k3, j3, j2, tile.texture);
				}
			} else {
				int i7 = anIntArray485[tile.texture];
				Rasterizer.method374(j6, l6, l5, i6, k6, k5, method317(i7, tile.anInt718), method317(i7, tile.anInt719),
						method317(i7, tile.anInt717));
			}
		}

		if ((i5 - k5) * (l6 - l5) - (j5 - l5) * (k6 - k5) > 0) {
			Rasterizer.aBoolean1462 = false;
			if (i5 < 0 || k5 < 0 || k6 < 0 || i5 > Raster.anInt1385 || k5 > Raster.anInt1385 || k6 > Raster.anInt1385) {
				Rasterizer.aBoolean1462 = true;
			}
			if (aBoolean467 && method318(anInt468, anInt469, j5, l5, l6, i5, k5, k6)) {
				anInt470 = x;
				anInt471 = y;
			}
			if (tile.texture == -1) {
				if (tile.anInt716 != 0xbc614e) {
					Rasterizer.method374(j5, l5, l6, i5, k5, k6, tile.anInt716, tile.anInt717, tile.anInt719);
					return;
				}
			} else {
				if (!lowMemory) {
					Rasterizer.method378(j5, l5, l6, i5, k5, k6, tile.anInt716, tile.anInt717, tile.anInt719, i2, i3, l1, l3, i4,
							k4, k2, j2, j3, tile.texture);
					return;
				}
				int j7 = anIntArray485[tile.texture];
				Rasterizer.method374(j5, l5, l6, i5, k5, k6, method317(j7, tile.anInt716), method317(j7, tile.anInt717),
						method317(j7, tile.anInt719));
			}
		}
	}

	public void method316(int x, int j, int k, ShapedTile tile, int l, int y, int j1) {
		int k1 = tile.anIntArray673.length;
		for (int l1 = 0; l1 < k1; l1++) {
			int i2 = tile.anIntArray673[l1] - xOffset;
			int k2 = tile.anIntArray674[l1] - heightOffset;
			int i3 = tile.anIntArray675[l1] - yOffset;
			int k3 = i3 * k + i2 * j1 >> 16;
			i3 = i3 * j1 - i2 * k >> 16;
			i2 = k3;
			k3 = k2 * l - i3 * j >> 16;
			i3 = k2 * j + i3 * l >> 16;
			k2 = k3;
			if (i3 < 50) {
				return;
			}
			if (tile.anIntArray682 != null) {
				ShapedTile.anIntArray690[l1] = i2;
				ShapedTile.anIntArray691[l1] = k2;
				ShapedTile.anIntArray692[l1] = i3;
			}
			ShapedTile.anIntArray688[l1] = Rasterizer.originViewX + (i2 << 9) / i3;
			ShapedTile.anIntArray689[l1] = Rasterizer.originViewY + (k2 << 9) / i3;
		}

		Rasterizer.currentAlpha = 0;
		k1 = tile.anIntArray679.length;
		for (int j2 = 0; j2 < k1; j2++) {
			int l2 = tile.anIntArray679[j2];
			int j3 = tile.anIntArray680[j2];
			int l3 = tile.anIntArray681[j2];
			int i4 = ShapedTile.anIntArray688[l2];
			int j4 = ShapedTile.anIntArray688[j3];
			int k4 = ShapedTile.anIntArray688[l3];
			int l4 = ShapedTile.anIntArray689[l2];
			int i5 = ShapedTile.anIntArray689[j3];
			int j5 = ShapedTile.anIntArray689[l3];
			if ((i4 - j4) * (j5 - i5) - (l4 - i5) * (k4 - j4) > 0) {
				Rasterizer.aBoolean1462 = false;
				if (i4 < 0 || j4 < 0 || k4 < 0 || i4 > Raster.anInt1385 || j4 > Raster.anInt1385 || k4 > Raster.anInt1385) {
					Rasterizer.aBoolean1462 = true;
				}
				if (aBoolean467 && method318(anInt468, anInt469, l4, i5, j5, i4, j4, k4)) {
					anInt470 = x;
					anInt471 = y;
				}
				if (tile.anIntArray682 == null || tile.anIntArray682[j2] == -1) {
					if (tile.anIntArray676[j2] != 0xbc614e) {
						Rasterizer.method374(l4, i5, j5, i4, j4, k4, tile.anIntArray676[j2], tile.anIntArray677[j2],
								tile.anIntArray678[j2]);
					}
				} else if (!lowMemory) {
					if (tile.flat) {
						Rasterizer.method378(l4, i5, j5, i4, j4, k4, tile.anIntArray676[j2], tile.anIntArray677[j2],
								tile.anIntArray678[j2], ShapedTile.anIntArray690[0], ShapedTile.anIntArray690[1],
								ShapedTile.anIntArray690[3], ShapedTile.anIntArray691[0], ShapedTile.anIntArray691[1],
								ShapedTile.anIntArray691[3], ShapedTile.anIntArray692[0], ShapedTile.anIntArray692[1],
								ShapedTile.anIntArray692[3], tile.anIntArray682[j2]);
					} else {
						Rasterizer.method378(l4, i5, j5, i4, j4, k4, tile.anIntArray676[j2], tile.anIntArray677[j2],
								tile.anIntArray678[j2], ShapedTile.anIntArray690[l2], ShapedTile.anIntArray690[j3],
								ShapedTile.anIntArray690[l3], ShapedTile.anIntArray691[l2], ShapedTile.anIntArray691[j3],
								ShapedTile.anIntArray691[l3], ShapedTile.anIntArray692[l2], ShapedTile.anIntArray692[j3],
								ShapedTile.anIntArray692[l3], tile.anIntArray682[j2]);
					}
				} else {
					int k5 = anIntArray485[tile.anIntArray682[j2]];
					Rasterizer.method374(l4, i5, j5, i4, j4, k4, method317(k5, tile.anIntArray676[j2]),
							method317(k5, tile.anIntArray677[j2]), method317(k5, tile.anIntArray678[j2]));
				}
			}
		}
	}

	public int method317(int colour, int light) {
		light = 127 - light;
		light = light * (colour & 0x7f) / 160;
		
		if (light < 2) {
			light = 2;
		} else if (light > 126) {
			light = 126;
		}
		
		return (colour & 0xff80) + light;
	}

	public boolean method318(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
		if (j < k && j < l && j < i1) {
			return false;
		} else if (j > k && j > l && j > i1) {
			return false;
		} else if (i < j1 && i < k1 && i < l1) {
			return false;
		} else if (i > j1 && i > k1 && i > l1) {
			return false;
		}

		int i2 = (j - k) * (k1 - j1) - (i - j1) * (l - k);
		int j2 = (j - i1) * (j1 - l1) - (i - l1) * (k - i1);
		int k2 = (j - l) * (l1 - k1) - (i - k1) * (i1 - l);
		return i2 * k2 > 0 && k2 * j2 > 0;
	}

	public void removeFloorDecoration(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return;
		}
		tile.groundDecoration = null;
	}

	public void removeObject(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return;
		}

		for (int index = 0; index < tile.objectCount; index++) {
			GameObject object = tile.gameObjects[index];
			if ((object.key >> 29 & 3) == 2 && object.positionX == x && object.positionY == y) {
				removeInteractable(object);
				return;
			}
		}
	}

	public void removeWall(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return;
		}
		tile.wall = null;
	}

	public void removeWallDecoration(int x, int y, int z) {
		SceneTile tile = tiles[z][x][y];
		if (tile == null) {
			return;
		}
		tile.wallDecoration = null;
	}

	public void reset() {
		for (int z = 0; z < planeCount; z++) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < length; y++) {
					tiles[z][x][y] = null;
				}
			}
		}

		for (int i = 0; i < PLANE_COUNT; i++) {
			for (int j = 0; j < clusterCounts[i]; j++) {
				clusters[i][j] = null;
			}

			clusterCounts[i] = 0;
		}

		for (int k1 = 0; k1 < anInt443; k1++) {
			aClass28Array444[k1] = null;
		}

		anInt443 = 0;
		for (int l1 = 0; l1 < interactables.length; l1++) {
			interactables[l1] = null;
		}
	}

	/**
	 * Sets the collision plane of a tile (i.e. the plane that it derives its collision data from).
	 * 
	 * @param x The x coordinate of the tile.
	 * @param y The y coordinate of the tile.
	 * @param z The plane of the tile.
	 * @param collisionPlane The collision plane of the tile.
	 */
	public void setCollisionPlane(int x, int y, int plane, int collisionPlane) {
		SceneTile tile = tiles[plane][x][y];
		if (tile == null) {
			return;
		}

		tiles[plane][x][y].collisionPlane = collisionPlane;
	}

	private boolean addRenderable(int plane, int minX, int minY, int deltaX, int deltaY, int centreX, int centreY,
			int renderHeight, Renderable renderable, int yaw, boolean flag, int key, byte config) {
		for (int x = minX; x < minX + deltaX; x++) {
			for (int y = minY; y < minY + deltaY; y++) {
				if (x < 0 || y < 0 || x >= width || y >= this.length) {
					return false;
				}
				SceneTile tile = tiles[plane][x][y];
				if (tile != null && tile.objectCount >= 5) {
					return false;
				}
			}
		}

		GameObject object = new GameObject();
		object.key = key;
		object.config = config;
		object.plane = plane;
		object.centreX = centreX;
		object.centreY = centreY;
		object.renderHeight = renderHeight;
		object.renderable = renderable;
		object.yaw = yaw;
		object.positionX = minX;
		object.positionY = minY;
		object.maxX = minX + deltaX - 1;
		object.maxY = minY + deltaY - 1;

		for (int x = minX; x < minX + deltaX; x++) {
			for (int y = minY; y < minY + deltaY; y++) {
				int attributes = 0;

				if (x > minX) {
					attributes++;
				}
				if (y < minY + deltaY - 1) {
					attributes += 0b10;
				}
				if (x < minX + deltaX - 1) {
					attributes |= 0b100;
				}
				if (y > minY) {
					attributes += 0b1000;
				}

				for (int z = plane; z >= 0; z--) {
					if (tiles[z][x][y] == null) {
						tiles[z][x][y] = new SceneTile(x, y, z);
					}
				}

				SceneTile tile = tiles[plane][x][y];
				tile.gameObjects[tile.objectCount] = object;
				tile.objectAttributes[tile.objectCount] = attributes;
				tile.attributes |= attributes;
				tile.objectCount++;
			}

		}

		if (flag) {
			aClass28Array444[anInt443++] = object;
		}

		return true;
	}

	private void mergeNormals(Model first, Model second, int dx, int dy, int dz, boolean flag) {
		anInt488++;
		int count = 0;
		int[] secondX = second.vertexX;
		int secondVertices = second.vertices;

		for (int vertexA = 0; vertexA < first.vertices; vertexA++) {
			VertexNormal parentNormalA = ((Renderable) first).getNormal(vertexA);
			VertexNormal normalA = first.normals[vertexA];

			if (normalA.getFaceCount() != 0) {
				int y = first.vertexY[vertexA] - dy;
				if (y <= second.minimumY) {
					int x = first.vertexX[vertexA] - dx;

					if (x >= second.minimumX && x <= second.maximumX) {
						int z = first.vertexZ[vertexA] - dz;

						if (z >= second.minimumZ && z <= second.maximumZ) {
							for (int vertexB = 0; vertexB < secondVertices; vertexB++) {
								VertexNormal parentNormalB = ((Renderable) second).getNormal(vertexB);
								VertexNormal normalB = second.normals[vertexB];

								if (x == secondX[vertexB] && z == second.vertexZ[vertexB] && y == second.vertexY[vertexB]
										&& normalB.getFaceCount() != 0) {
									parentNormalA.setX(parentNormalA.getX() + normalB.getX());
									parentNormalA.setY(parentNormalA.getY() + normalB.getY());
									parentNormalA.setZ(parentNormalA.getZ() + normalB.getZ());
									parentNormalA.setFaceCount(parentNormalA.getFaceCount() + normalB.getFaceCount());

									parentNormalB.setX(parentNormalB.getX() + normalA.getX());
									parentNormalB.setY(parentNormalB.getY() + normalA.getY());
									parentNormalB.setZ(parentNormalB.getZ() + normalA.getZ());
									parentNormalB.setFaceCount(parentNormalB.getFaceCount() + normalA.getFaceCount());

									count++;
									anIntArray486[vertexA] = anInt488;
									anIntArray487[vertexB] = anInt488;
								}
							}
						}
					}
				}
			}
		}

		if (count < 3 || !flag) {
			return;
		}

		for (int k1 = 0; k1 < first.faces; k1++) {
			if (anIntArray486[first.faceIndexX[k1]] == anInt488 && anIntArray486[first.faceIndexY[k1]] == anInt488
					&& anIntArray486[first.faceIndexZ[k1]] == anInt488) {
				first.anIntArray489[k1] = -1;
			}
		}

		for (int l1 = 0; l1 < second.faces; l1++) {
			if (anIntArray487[second.faceIndexX[l1]] == anInt488 && anIntArray487[second.faceIndexY[l1]] == anInt488
					&& anIntArray487[second.faceIndexZ[l1]] == anInt488) {
				second.anIntArray489[l1] = -1;
			}
		}
	}

	private void method306(Model model, int x, int y, int z) {
		if (x < width) {
			SceneTile tile = tiles[z][x + 1][y];
			if (tile != null && tile.groundDecoration != null && tile.groundDecoration.getRenderable().hasNormals()) {
				mergeNormals(model, (Model) tile.groundDecoration.getRenderable(), 128, 0, 0, true);
			}
		}

		if (y < width) {
			SceneTile tile = tiles[z][x][y + 1];
			if (tile != null && tile.groundDecoration != null && tile.groundDecoration.getRenderable().hasNormals()) {
				mergeNormals(model, (Model) tile.groundDecoration.getRenderable(), 0, 0, 128, true);
			}
		}

		if (x < width && y < length) {
			SceneTile tile = tiles[z][x + 1][y + 1];
			if (tile != null && tile.groundDecoration != null && tile.groundDecoration.getRenderable().hasNormals()) {
				mergeNormals(model, (Model) tile.groundDecoration.getRenderable(), 128, 0, 128, true);
			}
		}

		if (x < width && y > 0) {
			SceneTile tile = tiles[z][x + 1][y - 1];
			if (tile != null && tile.groundDecoration != null && tile.groundDecoration.getRenderable().hasNormals()) {
				mergeNormals(model, (Model) tile.groundDecoration.getRenderable(), 128, 0, -128, true);
			}
		}
	}

	private void method307(int plane, int j, int k, int l, int i1, Model model) {
		boolean flag = true;
		int initialX = l;
		int finalX = l + j;
		int initialY = i1 - 1;
		int finalY = i1 + k;

		for (int z = plane; z <= plane + 1; z++) {
			if (z != planeCount) {
				for (int x = initialX; x <= finalX; x++) {
					if (x >= 0 && x < width) {
						for (int y = initialY; y <= finalY; y++) {
							if (y >= 0 && y < length && (!flag || x >= finalX || y >= finalY || y < i1 && x != l)) {
								SceneTile tile = tiles[z][x][y];
								if (tile != null) {
									int i3 = (tileHeights[z][x][y] + tileHeights[z][x + 1][y]
											+ tileHeights[z][x][y + 1] + tileHeights[z][x + 1][y + 1])
											/ 4
											- (tileHeights[plane][l][i1] + tileHeights[plane][l + 1][i1]
													+ tileHeights[plane][l][i1 + 1] + tileHeights[plane][l + 1][i1 + 1])
											/ 4;
									Wall wall = tile.wall;
									if (wall != null && wall.getPrimary() != null && wall.getPrimary().hasNormals()) {
										mergeNormals(model, (Model) wall.getPrimary(), (x - l) * 128 + (1 - j) * 64, i3, (y - i1)
												* 128 + (1 - k) * 64, flag);
									}
									if (wall != null && wall.getSecondary() != null && wall.getSecondary().hasNormals()) {
										mergeNormals(model, (Model) wall.getSecondary(), (x - l) * 128 + (1 - j) * 64, i3,
												(y - i1) * 128 + (1 - k) * 64, flag);
									}

									for (int j3 = 0; j3 < tile.objectCount; j3++) {
										GameObject object = tile.gameObjects[j3];
										if (object != null && object.renderable != null && object.renderable.hasNormals()) {
											int k3 = object.maxX - object.positionX + 1;
											int l3 = object.maxY - object.positionY + 1;
											mergeNormals(model, (Model) object.renderable, (object.positionX - l) * 128
													+ (k3 - j) * 64, i3, (object.positionY - i1) * 128 + (l3 - k) * 64, flag);
										}
									}
								}
							}
						}
					}
				}
				initialX--;
				flag = false;
			}
		}
	}

	private void method319() {
		int j = clusterCounts[anInt447];
		SceneCluster[] clusters = SceneGraph.clusters[anInt447];
		anInt475 = 0;
		for (int k = 0; k < j; k++) {
			SceneCluster cluster = clusters[k];
			if (cluster.anInt791 == 1) {
				int l = cluster.anInt787 - anInt453 + 25;
				if (l < 0 || l > 50) {
					continue;
				}
				int k1 = cluster.anInt789 - anInt454 + 25;
				if (k1 < 0) {
					k1 = 0;
				}
				int j2 = cluster.anInt790 - anInt454 + 25;
				if (j2 > 50) {
					j2 = 50;
				}
				boolean flag = false;
				while (k1 <= j2) {
					if (aBooleanArrayArray492[l][k1++]) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					continue;
				}
				int j3 = xOffset - cluster.anInt792;
				if (j3 > 32) {
					cluster.anInt798 = 1;
				} else {
					if (j3 >= -32) {
						continue;
					}
					cluster.anInt798 = 2;
					j3 = -j3;
				}
				cluster.anInt801 = (cluster.anInt794 - yOffset << 8) / j3;
				cluster.anInt802 = (cluster.anInt795 - yOffset << 8) / j3;
				cluster.anInt803 = (cluster.anInt796 - heightOffset << 8) / j3;
				cluster.anInt804 = (cluster.anInt797 - heightOffset << 8) / j3;
				aClass47Array476[anInt475++] = cluster;
				continue;
			}
			if (cluster.anInt791 == 2) {
				int i1 = cluster.anInt789 - anInt454 + 25;
				if (i1 < 0 || i1 > 50) {
					continue;
				}
				int l1 = cluster.anInt787 - anInt453 + 25;
				if (l1 < 0) {
					l1 = 0;
				}
				int k2 = cluster.anInt788 - anInt453 + 25;
				if (k2 > 50) {
					k2 = 50;
				}
				boolean flag1 = false;
				while (l1 <= k2) {
					if (aBooleanArrayArray492[l1++][i1]) {
						flag1 = true;
						break;
					}
				}
				if (!flag1) {
					continue;
				}
				int k3 = yOffset - cluster.anInt794;
				if (k3 > 32) {
					cluster.anInt798 = 3;
				} else {
					if (k3 >= -32) {
						continue;
					}
					cluster.anInt798 = 4;
					k3 = -k3;
				}
				cluster.anInt799 = (cluster.anInt792 - xOffset << 8) / k3;
				cluster.anInt800 = (cluster.anInt793 - xOffset << 8) / k3;
				cluster.anInt803 = (cluster.anInt796 - heightOffset << 8) / k3;
				cluster.anInt804 = (cluster.anInt797 - heightOffset << 8) / k3;
				aClass47Array476[anInt475++] = cluster;
			} else if (cluster.anInt791 == 4) {
				int j1 = cluster.anInt796 - heightOffset;
				if (j1 > 128) {
					int i2 = cluster.anInt789 - anInt454 + 25;
					if (i2 < 0) {
						i2 = 0;
					}
					int l2 = cluster.anInt790 - anInt454 + 25;
					if (l2 > 50) {
						l2 = 50;
					}
					if (i2 <= l2) {
						int i3 = cluster.anInt787 - anInt453 + 25;
						if (i3 < 0) {
							i3 = 0;
						}
						int l3 = cluster.anInt788 - anInt453 + 25;
						if (l3 > 50) {
							l3 = 50;
						}
						boolean flag2 = false;
						label0: for (int i4 = i3; i4 <= l3; i4++) {
							for (int j4 = i2; j4 <= l2; j4++) {
								if (!aBooleanArrayArray492[i4][j4]) {
									continue;
								}
								flag2 = true;
								break label0;
							}

						}

						if (flag2) {
							cluster.anInt798 = 5;
							cluster.anInt799 = (cluster.anInt792 - xOffset << 8) / j1;
							cluster.anInt800 = (cluster.anInt793 - xOffset << 8) / j1;
							cluster.anInt801 = (cluster.anInt794 - yOffset << 8) / j1;
							cluster.anInt802 = (cluster.anInt795 - yOffset << 8) / j1;
							aClass47Array476[anInt475++] = cluster;
						}
					}
				}
			}
		}
	}

	private boolean method320(int x, int y, int z) {
		int l = anIntArrayArrayArray445[z][x][y];
		if (l == -anInt448) {
			return false;
		} else if (l == anInt448) {
			return true;
		}

		int worldX = x << 7;
		int worldY = y << 7;

		if (method324(worldX + 1, worldY + 1, tileHeights[z][x][y])
				&& method324(worldX + 128 - 1, worldY + 1, tileHeights[z][x + 1][y])
				&& method324(worldX + 128 - 1, worldY + 128 - 1, tileHeights[z][x + 1][y + 1])
				&& method324(worldX + 1, worldY + 128 - 1, tileHeights[z][x][y + 1])) {
			anIntArrayArrayArray445[z][x][y] = anInt448;
			return true;
		}

		anIntArrayArrayArray445[z][x][y] = -anInt448;
		return false;
	}

	private boolean method321(int x, int y, int z, int l) {
		if (!method320(x, y, z)) {
			return false;
		}

		int worldX = x << 7;
		int worldY = y << 7;
		int k1 = tileHeights[z][x][y] - 1;
		int l1 = k1 - 120;
		int i2 = k1 - 230;
		int j2 = k1 - 238;

		if (l < 16) {
			if (l == 1) {
				if (worldX > xOffset) {
					if (!method324(worldX, worldY, k1)) {
						return false;
					} else if (!method324(worldX, worldY + 128, k1)) {
						return false;
					}
				}

				if (z > 0) {
					if (!method324(worldX, worldY, l1)) {
						return false;
					} else if (!method324(worldX, worldY + 128, l1)) {
						return false;
					}
				}
				if (!method324(worldX, worldY, i2)) {
					return false;
				}
				return method324(worldX, worldY + 128, i2);
			}
			if (l == 2) {
				if (worldY < yOffset) {
					if (!method324(worldX, worldY + 128, k1)) {
						return false;
					} else if (!method324(worldX + 128, worldY + 128, k1)) {
						return false;
					}
				}

				if (z > 0) {
					if (!method324(worldX, worldY + 128, l1)) {
						return false;
					} else if (!method324(worldX + 128, worldY + 128, l1)) {
						return false;
					}
				}
				if (!method324(worldX, worldY + 128, i2)) {
					return false;
				}
				return method324(worldX + 128, worldY + 128, i2);
			}
			if (l == 4) {
				if (worldX < xOffset) {
					if (!method324(worldX + 128, worldY, k1)) {
						return false;
					} else if (!method324(worldX + 128, worldY + 128, k1)) {
						return false;
					}
				}

				if (z > 0) {
					if (!method324(worldX + 128, worldY, l1)) {
						return false;
					} else if (!method324(worldX + 128, worldY + 128, l1)) {
						return false;
					}
				}
				if (!method324(worldX + 128, worldY, i2)) {
					return false;
				}
				return method324(worldX + 128, worldY + 128, i2);
			}
			if (l == 8) {
				if (worldY > yOffset) {
					if (!method324(worldX, worldY, k1)) {
						return false;
					} else if (!method324(worldX + 128, worldY, k1)) {
						return false;
					}
				}

				if (z > 0) {
					if (!method324(worldX, worldY, l1)) {
						return false;
					} else if (!method324(worldX + 128, worldY, l1)) {
						return false;
					}
				}
				if (!method324(worldX, worldY, i2)) {
					return false;
				}

				return method324(worldX + 128, worldY, i2);
			}
		}

		if (!method324(worldX + 64, worldY + 64, j2)) {
			return false;
		} else if (l == 16) {
			return method324(worldX, worldY + 128, i2);
		} else if (l == 32) {
			return method324(worldX + 128, worldY + 128, i2);
		} else if (l == 64) {
			return method324(worldX + 128, worldY, i2);
		} else if (l == 128) {
			return method324(worldX, worldY, i2);
		}
		System.out.println("Warning unsupported wall type");
		return true;
	}

	private boolean method322(int plane, int x, int y, int l) {
		if (!method320(x, y, plane)) {
			return false;
		}

		int absoluteX = x << 7;
		int absoluteY = y << 7;
		return method324(absoluteX + 1, absoluteY + 1, tileHeights[plane][x][y] - l)
				&& method324(absoluteX + 128 - 1, absoluteY + 1, tileHeights[plane][x + 1][y] - l)
				&& method324(absoluteX + 128 - 1, absoluteY + 128 - 1, tileHeights[plane][x + 1][y + 1] - l)
				&& method324(absoluteX + 1, absoluteY + 128 - 1, tileHeights[plane][x][y + 1] - l);
	}

	private boolean method323(int plane, int minX, int maxX, int minY, int maxY, int j1) {
		if (minX == maxX && minY == maxY) {
			if (!method320(minX, minY, plane)) {
				return false;
			}

			int worldX = minX << 7;
			int worldY = minY << 7;

			return method324(worldX + 1, worldY + 1, tileHeights[plane][minX][minY] - j1)
					&& method324(worldX + 128 - 1, worldY + 1, tileHeights[plane][minX + 1][minY] - j1)
					&& method324(worldX + 128 - 1, worldY + 128 - 1, tileHeights[plane][minX + 1][minY + 1] - j1)
					&& method324(worldX + 1, worldY + 128 - 1, tileHeights[plane][minX][minY + 1] - j1);
		}

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				if (anIntArrayArrayArray445[plane][x][y] == -anInt448) {
					return false;
				}
			}
		}

		int minWorldX = (minX << 7) + 1;
		int minWorldY = (minY << 7) + 2;
		int i3 = tileHeights[plane][minX][minY] - j1;
		if (!method324(minWorldX, minWorldY, i3)) {
			return false;
		}

		int maxWorldX = (maxX << 7) - 1;
		if (!method324(maxWorldX, minWorldY, i3)) {
			return false;
		}

		int maxWorldY = (maxY << 7) - 1;
		if (!method324(minWorldX, maxWorldY, i3)) {
			return false;
		}

		return method324(maxWorldX, maxWorldY, i3);
	}

	private boolean method324(int worldX, int worldY, int j) {
		for (int l = 0; l < anInt475; l++) {
			SceneCluster cluster = aClass47Array476[l];

			if (cluster.anInt798 == 1) {
				int dx = cluster.anInt792 - worldX;

				if (dx > 0) {
					int j2 = cluster.anInt794 + (cluster.anInt801 * dx >> 8);
					int k3 = cluster.anInt795 + (cluster.anInt802 * dx >> 8);
					int l4 = cluster.anInt796 + (cluster.anInt803 * dx >> 8);
					int i6 = cluster.anInt797 + (cluster.anInt804 * dx >> 8);

					if (worldY >= j2 && worldY <= k3 && j >= l4 && j <= i6) {
						return true;
					}
				}
			} else if (cluster.anInt798 == 2) {
				int dx = worldX - cluster.anInt792;

				if (dx > 0) {
					int k2 = cluster.anInt794 + (cluster.anInt801 * dx >> 8);
					int l3 = cluster.anInt795 + (cluster.anInt802 * dx >> 8);
					int i5 = cluster.anInt796 + (cluster.anInt803 * dx >> 8);
					int j6 = cluster.anInt797 + (cluster.anInt804 * dx >> 8);

					if (worldY >= k2 && worldY <= l3 && j >= i5 && j <= j6) {
						return true;
					}
				}
			} else if (cluster.anInt798 == 3) {
				int dy = cluster.anInt794 - worldY;

				if (dy > 0) {
					int l2 = cluster.anInt792 + (cluster.anInt799 * dy >> 8);
					int i4 = cluster.anInt793 + (cluster.anInt800 * dy >> 8);
					int j5 = cluster.anInt796 + (cluster.anInt803 * dy >> 8);
					int k6 = cluster.anInt797 + (cluster.anInt804 * dy >> 8);

					if (worldX >= l2 && worldX <= i4 && j >= j5 && j <= k6) {
						return true;
					}
				}
			} else if (cluster.anInt798 == 4) {
				int dy = worldY - cluster.anInt794;
				if (dy > 0) {
					int i3 = cluster.anInt792 + (cluster.anInt799 * dy >> 8);
					int j4 = cluster.anInt793 + (cluster.anInt800 * dy >> 8);
					int k5 = cluster.anInt796 + (cluster.anInt803 * dy >> 8);
					int l6 = cluster.anInt797 + (cluster.anInt804 * dy >> 8);

					if (worldX >= i3 && worldX <= j4 && j >= k5 && j <= l6) {
						return true;
					}
				}
			} else if (cluster.anInt798 == 5) {
				int i2 = j - cluster.anInt796;
				if (i2 > 0) {
					int j3 = cluster.anInt792 + (cluster.anInt799 * i2 >> 8);
					int k4 = cluster.anInt793 + (cluster.anInt800 * i2 >> 8);
					int l5 = cluster.anInt794 + (cluster.anInt801 * i2 >> 8);
					int i7 = cluster.anInt795 + (cluster.anInt802 * i2 >> 8);

					if (worldX >= j3 && worldX <= k4 && worldY >= l5 && worldY <= i7) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private void removeInteractable(GameObject object) {
		for (int x = object.positionX; x <= object.maxX; x++) {
			for (int y = object.positionY; y <= object.maxY; y++) {
				SceneTile tile = tiles[object.plane][x][y];

				if (tile != null) {
					for (int index = 0; index < tile.objectCount; index++) {
						if (tile.gameObjects[index] != object) {
							continue;
						}

						tile.objectCount--;
						for (int remaining = index; remaining < tile.objectCount; remaining++) {
							tile.gameObjects[remaining] = tile.gameObjects[remaining + 1];
							tile.objectAttributes[remaining] = tile.objectAttributes[remaining + 1];
						}

						tile.gameObjects[tile.objectCount] = null;
						break;
					}

					tile.attributes = 0;
					for (int index = 0; index < tile.objectCount; index++) {
						tile.attributes |= tile.objectAttributes[index];
					}
				}
			}
		}
	}

}