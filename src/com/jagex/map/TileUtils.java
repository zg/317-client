package com.jagex.map;

import org.major.map.Orientation;

public class TileUtils {

	public static int getObjectXOffset(int x, int y, int width, int length, int rotation) {
		rotation &= 3;
		if (rotation == Orientation.NORTH) {
			return x;
		} else if (rotation == Orientation.EAST) {
			return 7 - y - (width - 1);
		} else if (rotation == Orientation.SOUTH) {
			return 7 - x - (length - 1);
		}

		return y;
	}

	public static int getObjectYOffset(int x, int y, int width, int length, int orientation) {
		orientation &= 3;
		if (orientation == Orientation.NORTH) {
			return y;
		} else if (orientation == Orientation.EAST) {
			return x;
		} else if (orientation == Orientation.SOUTH) {
			return 7 - y - (width - 1);
		}

		return 7 - x - (length - 1);
	}

	public static int getXOffset(int x, int y, int orientation) {
		orientation &= 3;
		if (orientation == Orientation.NORTH) {
			return x;
		} else if (orientation == Orientation.EAST) {
			return y;
		} else if (orientation == Orientation.SOUTH) {
			return 7 - x;
		}

		return 7 - y;
	}

	public static int getYOffset(int x, int y, int orientation) {
		orientation &= 3;
		if (orientation == Orientation.NORTH) {
			return y;
		} else if (orientation == Orientation.EAST) {
			return 7 - x;
		} else if (orientation == Orientation.SOUTH) {
			return 7 - y;
		}

		return x;
	}

}