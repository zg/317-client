package com.jagex.map;

import com.jagex.entity.GameObject;
import com.jagex.link.Linkable;
import com.jagex.map.object.GroundDecoration;
import com.jagex.map.object.Wall;
import com.jagex.map.object.WallDecoration;

final class SceneTile extends Linkable {

	public ShapedTile shape;
	public SimpleTile simple;
	boolean aBoolean1322;
	boolean aBoolean1323;
	boolean aBoolean1324;
	SceneTile aClass30_Sub3_1329;
	int anInt1310;
	int anInt1325;
	int anInt1326;
	int anInt1327;
	int anInt1328;
	int collisionPlane;
	GameObject[] gameObjects;
	GroundDecoration groundDecoration;
	GroundItem groundItem;
	int[] objectAttributes;
	int attributes;
	int objectCount;
	int plane;
	int positionX;
	int positionY;
	Wall wall;
	WallDecoration wallDecoration;

	public SceneTile(int x, int y, int z) {
		gameObjects = new GameObject[5];
		objectAttributes = new int[5];
		anInt1310 = plane = z;
		positionX = x;
		positionY = y;
	}

}