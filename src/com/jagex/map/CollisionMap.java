package com.jagex.map;

import org.major.map.Orientation;

public class CollisionMap {

	private static final int BLOCKED_TILE = 0x200000;
	private static final int OBJECT_TILE = 0x100;
	private static final int WALL_EAST = 0x8;
	private static final int WALL_NORTH = 0x2;
	private static final int WALL_NORTHEAST = 0x4;
	private static final int WALL_NORTHWEST = 0x1;
	private static final int WALL_SOUTH = 0x20;
	private static final int WALL_SOUTHEAST = 0x10;
	private static final int WALL_SOUTHWEST = 0x40;
	private static final int WALL_WEST = 0x80;

	public int[][] adjacencies;
	public int height;
	public int width;
	public int xOffset;

	public int yOffset;

	public CollisionMap(int width, int height) {
		xOffset = 0;
		yOffset = 0;
		this.width = width;
		this.height = height;
		adjacencies = new int[width][height];
		init();
	}

	public void flagObject(int x, int y, int orientation, int group, boolean impenetrable) {
		x -= xOffset;
		y -= yOffset;

		if (group == 0) {
			if (orientation == Orientation.NORTH) {
				flag(x, y, WALL_WEST);
				flag(x - 1, y, WALL_EAST);
			} else if (orientation == Orientation.EAST) {
				flag(x, y, WALL_NORTH);
				flag(x, y + 1, WALL_SOUTH);
			} else if (orientation == Orientation.SOUTH) {
				flag(x, y, WALL_EAST);
				flag(x + 1, y, WALL_WEST);
			} else if (orientation == Orientation.WEST) {
				flag(x, y, WALL_SOUTH);
				flag(x, y - 1, WALL_NORTH);
			}
		}

		if (group == 1 || group == 3) {
			if (orientation == Orientation.NORTH) {
				flag(x, y, WALL_NORTHWEST);
				flag(x - 1, y + 1, WALL_SOUTHEAST);
			} else if (orientation == Orientation.EAST) {
				flag(x, y, WALL_NORTHEAST);
				flag(x + 1, y + 1, WALL_SOUTHWEST);
			} else if (orientation == Orientation.SOUTH) {
				flag(x, y, WALL_SOUTHEAST);
				flag(x + 1, y - 1, WALL_NORTHWEST);
			} else if (orientation == Orientation.WEST) {
				flag(x, y, WALL_SOUTHWEST);
				flag(x - 1, y - 1, WALL_NORTHEAST);
			}
		}

		if (group == 2) {
			if (orientation == Orientation.NORTH) {
				flag(x, y, WALL_WEST | WALL_NORTH);
				flag(x - 1, y, WALL_EAST);
				flag(x, y + 1, WALL_SOUTH);
			} else if (orientation == Orientation.EAST) {
				flag(x, y, WALL_EAST | WALL_NORTH);
				flag(x, y + 1, WALL_SOUTH);
				flag(x + 1, y, WALL_WEST);
			} else if (orientation == Orientation.SOUTH) {
				flag(x, y, WALL_EAST | WALL_SOUTH);
				flag(x + 1, y, WALL_WEST);
				flag(x, y - 1, WALL_NORTH);
			} else if (orientation == Orientation.WEST) {
				flag(x, y, WALL_WEST | WALL_SOUTH);
				flag(x, y - 1, WALL_NORTH);
				flag(x - 1, y, WALL_EAST);
			}
		}

		if (impenetrable) {
			if (group == 0) {
				if (orientation == Orientation.NORTH) {
					flag(x, y, 0x10000); // 0x80 << 9
					flag(x - 1, y, 0x1000); // 0x8 << 9
				} else if (orientation == Orientation.EAST) {
					flag(x, y, 0x400);
					flag(x, y + 1, 0x4000);
				} else if (orientation == Orientation.SOUTH) {
					flag(x, y, 0x1000);
					flag(x + 1, y, 0x10000);
				} else if (orientation == Orientation.WEST) {
					flag(x, y, 0x4000);
					flag(x, y - 1, 0x400);
				}
			}

			if (group == 1 || group == 3) {
				if (orientation == Orientation.NORTH) {
					flag(x, y, 0x200);
					flag(x - 1, y + 1, 0x2000);
				} else if (orientation == Orientation.EAST) {
					flag(x, y, 0x800);
					flag(x + 1, y + 1, 0x8000);
				} else if (orientation == Orientation.SOUTH) {
					flag(x, y, 0x2000);
					flag(x + 1, y - 1, 0x200);
				} else if (orientation == Orientation.WEST) {
					flag(x, y, 0x8000);
					flag(x - 1, y - 1, 0x800);
				}
			}

			if (group == 2) {
				if (orientation == Orientation.NORTH) {
					flag(x, y, 0x10400);
					flag(x - 1, y, 0x1000);
					flag(x, y + 1, 0x4000);
				} else if (orientation == Orientation.EAST) {
					flag(x, y, 0x1400);
					flag(x, y + 1, 0x4000);
					flag(x + 1, y, 0x10000);
				} else if (orientation == Orientation.SOUTH) {
					flag(x, y, 0x5000);
					flag(x + 1, y, 0x10000);
					flag(x, y - 1, 0x400);
				} else if (orientation == Orientation.WEST) {
					flag(x, y, 0x14000);
					flag(x, y - 1, 0x400);
					flag(x - 1, y, 0x1000);
				}
			}
		}
	}

	public void flagObject(int initialX, int initialY, int width, int height, boolean impenetrable, int orientation) {
		int value = OBJECT_TILE;
		if (impenetrable) {
			value += 0x20000;
		}

		initialX -= xOffset;
		initialY -= yOffset;

		if (orientation == 1 || orientation == 3) {
			int tmp = width;
			width = height;
			height = tmp;
		}

		for (int x = initialX; x < initialX + width; x++) {
			if (x >= 0 && x < this.width) {
				for (int y = initialY; y < initialY + height; y++) {
					if (y >= 0 && y < this.height) {
						flag(x, y, value);
					}
				}
			}
		}
	}

	public void init() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					adjacencies[x][y] = 0xFFFFFF;
				} else {
					adjacencies[x][y] = 0x1000000;
				}
			}
		}
	}

	public boolean reachedDecoration(int initialY, int initialX, int finalX, int finalY, int type, int orientation) {
		if (initialX == finalX && initialY == finalY) {
			return true;
		}

		initialX -= xOffset;
		initialY -= yOffset;
		finalX -= xOffset;
		finalY -= yOffset;

		if (type == 6 || type == 7) {
			if (type == 7) {
				orientation = orientation + 2 & 3;
			}

			if (orientation == Orientation.NORTH) {
				if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_WEST) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & WALL_NORTH) == 0) {
					return true;
				}
			} else if (orientation == Orientation.EAST) {
				if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_EAST) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & WALL_NORTH) == 0) {
					return true;
				}
			} else if (orientation == Orientation.SOUTH) {
				if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_EAST) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & WALL_SOUTH) == 0) {
					return true;
				}
			} else if (orientation == Orientation.WEST) {
				if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_WEST) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & WALL_SOUTH) == 0) {
					return true;
				}
			}
		}

		if (type == 8) {
			if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & WALL_SOUTH) == 0) {
				return true;
			} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & WALL_NORTH) == 0) {
				return true;
			} else if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_EAST) == 0) {
				return true;
			} else if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_WEST) == 0) {
				return true;
			}
		}

		return false;
	}

	public boolean reachedObject(int x, int y, int finalX, int finalY, int height, int surroundings, int width) {
		int maxX = finalX + width - 1;
		int maxY = finalY + height - 1;

		if (x >= finalX && x <= maxX && y >= finalY && y <= maxY) {
			return true;
		} else if (x == finalX - 1 && y >= finalY && y <= maxY && (adjacencies[x - xOffset][y - yOffset] & WALL_EAST) == 0
				&& (surroundings & WALL_EAST) == 0) {
			return true;
		} else if (x == maxX + 1 && y >= finalY && y <= maxY && (adjacencies[x - xOffset][y - yOffset] & WALL_WEST) == 0
				&& (surroundings & WALL_NORTH) == 0) {
			return true;
		} else if (y == finalY - 1 && x >= finalX && x <= maxX && (adjacencies[x - xOffset][y - yOffset] & WALL_NORTH) == 0
				&& (surroundings & WALL_NORTHEAST) == 0) {
			return true;
		}

		return y == maxY + 1 && x >= finalX && x <= maxX && (adjacencies[x - xOffset][y - yOffset] & WALL_SOUTH) == 0
				&& (surroundings & WALL_NORTHWEST) == 0;
	}

	public boolean reachedWall(int initialX, int initialY, int finalX, int finalY, int orientation, int type) {
		if (initialX == finalX && initialY == finalY) {
			return true;
		}

		initialX -= xOffset;
		initialY -= yOffset;
		finalX -= xOffset;
		finalY -= yOffset;

		if (type == 0) {
			if (orientation == Orientation.NORTH) {
				if (initialX == finalX - 1 && initialY == finalY) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & 0x1280120) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == Orientation.EAST) {
				if (initialX == finalX && initialY == finalY + 1) {
					return true;
				} else if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280108) == 0) {
					return true;
				} else if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280180) == 0) {
					return true;
				}
			} else if (orientation == Orientation.SOUTH) {
				if (initialX == finalX + 1 && initialY == finalY) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & 0x1280120) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == Orientation.WEST) {
				if (initialX == finalX && initialY == finalY - 1) {
					return true;
				} else if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280108) == 0) {
					return true;
				} else if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280180) == 0) {
					return true;
				}
			}
		}

		if (type == 2) {
			if (orientation == Orientation.NORTH) {
				if (initialX == finalX - 1 && initialY == finalY) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1) {
					return true;
				} else if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280180) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == Orientation.EAST) {
				// UNLOADED_TILE | BLOCKED_TILE | UNKNOWN | OBJECT_TILE | WALL_EAST
				if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280108) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1) {
					return true;
				} else if (initialX == finalX + 1 && initialY == finalY) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == Orientation.SOUTH) {
				if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280108) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & 0x1280120) == 0) {
					return true;
				} else if (initialX == finalX + 1 && initialY == finalY) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1) {
					return true;
				}
			} else if (orientation == Orientation.WEST) {
				if (initialX == finalX - 1 && initialY == finalY) {
					return true;
				} else if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & 0x1280120) == 0) {
					return true;
				} else if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & 0x1280180) == 0) {
					return true;
				} else if (initialX == finalX && initialY == finalY - 1) {
					return true;
				}
			}
		}

		if (type == 9) {
			if (initialX == finalX && initialY == finalY + 1 && (adjacencies[initialX][initialY] & WALL_SOUTH) == 0) {
				return true;
			} else if (initialX == finalX && initialY == finalY - 1 && (adjacencies[initialX][initialY] & WALL_NORTH) == 0) {
				return true;
			} else if (initialX == finalX - 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_EAST) == 0) {
				return true;
			} else if (initialX == finalX + 1 && initialY == finalY && (adjacencies[initialX][initialY] & WALL_WEST) == 0) {
				return true;
			}
		}

		return false;
	}

	public void removeFloorDecoration(int x, int y) {
		x -= xOffset;
		y -= yOffset;
		adjacencies[x][y] &= 0xDFFFFF;
	}

	public void removeObject(int x, int y, int orientation, int group, boolean impenetrable) {
		x -= xOffset;
		y -= yOffset;

		if (group == 0) {// wall
			if (orientation == Orientation.NORTH) {
				removeFlag(x, y, WALL_WEST);
				removeFlag(x - 1, y, WALL_EAST);
			} else if (orientation == Orientation.EAST) {
				removeFlag(x, y, WALL_NORTH);
				removeFlag(x, y + 1, WALL_SOUTH);
			} else if (orientation == Orientation.SOUTH) {
				removeFlag(x, y, WALL_EAST);
				removeFlag(x + 1, y, WALL_WEST);
			} else if (orientation == Orientation.WEST) {
				removeFlag(x, y, WALL_SOUTH);
				removeFlag(x, y - 1, WALL_NORTH);
			}
		}

		if (group == 1 || group == 3) { // wall decor/floor decor
			if (orientation == Orientation.NORTH) {
				removeFlag(x, y, WALL_NORTHWEST);
				removeFlag(x - 1, y + 1, WALL_SOUTHEAST);
			} else if (orientation == Orientation.EAST) {
				removeFlag(x, y, WALL_NORTHEAST);
				removeFlag(x + 1, y + 1, WALL_SOUTHWEST);
			} else if (orientation == Orientation.SOUTH) {
				removeFlag(x, y, WALL_SOUTHEAST);
				removeFlag(x + 1, y - 1, WALL_NORTHWEST);
			} else if (orientation == Orientation.WEST) {
				removeFlag(x, y, WALL_SOUTHWEST);
				removeFlag(x - 1, y - 1, WALL_NORTHEAST);
			}
		}

		if (group == 2) { // interactable object
			if (orientation == Orientation.NORTH) {
				removeFlag(x, y, WALL_NORTH | WALL_WEST);
				removeFlag(x - 1, y, WALL_EAST);
				removeFlag(x, y + 1, WALL_SOUTH);
			} else if (orientation == Orientation.EAST) {
				removeFlag(x, y, WALL_NORTH | WALL_EAST);
				removeFlag(x, y + 1, WALL_SOUTH);
				removeFlag(x + 1, y, WALL_WEST);
			} else if (orientation == Orientation.SOUTH) {
				removeFlag(x, y, WALL_EAST | WALL_SOUTH);
				removeFlag(x + 1, y, WALL_WEST);
				removeFlag(x, y - 1, WALL_NORTH);
			} else if (orientation == Orientation.WEST) {
				removeFlag(x, y, WALL_SOUTH | WALL_WEST);
				removeFlag(x, y - 1, WALL_NORTH);
				removeFlag(x - 1, y, WALL_EAST);
			}
		}

		if (impenetrable) {
			if (group == 0) {
				if (orientation == Orientation.NORTH) {
					removeFlag(x, y, 0x10000);
					removeFlag(x - 1, y, 0x1000);
				} else if (orientation == Orientation.EAST) {
					removeFlag(x, y, 0x400);
					removeFlag(x, y + 1, 0x4000);
				} else if (orientation == Orientation.SOUTH) {
					removeFlag(x, y, 0x1000);
					removeFlag(x + 1, y, 0x10000);
				} else if (orientation == Orientation.WEST) {
					removeFlag(x, y, 0x4000);
					removeFlag(x, y - 1, 0x400);
				}
			}

			if (group == 1 || group == 3) {
				if (orientation == Orientation.NORTH) {
					removeFlag(x, y, 0x200);
					removeFlag(x - 1, y + 1, 0x2000);
				} else if (orientation == Orientation.EAST) {
					removeFlag(x, y, 0x800);
					removeFlag(x + 1, y + 1, 0x8000);
				} else if (orientation == Orientation.SOUTH) {
					removeFlag(x, y, 0x2000);
					removeFlag(x + 1, y - 1, 0x200);
				} else if (orientation == Orientation.WEST) {
					removeFlag(x, y, 0x8000);
					removeFlag(x - 1, y - 1, 0x800);
				}
			}

			if (group == 2) {
				if (orientation == Orientation.NORTH) {
					removeFlag(x, y, 0x10400);
					removeFlag(x - 1, y, 0x1000);
					removeFlag(x, y + 1, 0x4000);
				} else if (orientation == Orientation.EAST) {
					removeFlag(x, y, 0x1400);
					removeFlag(x, y + 1, 0x4000);
					removeFlag(x + 1, y, 0x10000);
				} else if (orientation == Orientation.SOUTH) {
					removeFlag(x, y, 0x5000);
					removeFlag(x + 1, y, 0x10000);
					removeFlag(x, y - 1, 0x400);
				} else if (orientation == Orientation.WEST) {
					removeFlag(x, y, 0x14000);
					removeFlag(x, y - 1, 0x400);
					removeFlag(x - 1, y, 0x1000);
				}
			}
		}
	}

	public void removeObject(int orientation, int width, int initialX, int initialY, int length, boolean impenetrable) {
		int value = OBJECT_TILE;
		if (impenetrable) {
			value += 0x20000;
		}
		initialX -= xOffset;
		initialY -= yOffset;
		if (orientation == Orientation.EAST || orientation == Orientation.WEST) {
			int temp = width;
			width = length;
			length = temp;
		}

		for (int x = initialX; x < initialX + width; x++) {
			if (x >= 0 && x < this.width) {
				for (int y = initialY; y < initialY + length; y++) {
					if (y >= 0 && y < height) {
						removeFlag(x, y, value);
					}
				}
			}
		}
	}

	public void block(int x, int y) {
		x -= xOffset;
		y -= yOffset;
		adjacencies[x][y] |= BLOCKED_TILE;
	}

	private void flag(int x, int y, int value) {
		adjacencies[x][y] |= value;
	}

	private void removeFlag(int x, int y, int value) {
		adjacencies[x][y] &= 0xFFFFFF - value;
	}

}