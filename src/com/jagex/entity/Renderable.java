package com.jagex.entity;

import com.jagex.entity.model.Model;
import com.jagex.entity.model.VertexNormal;
import com.jagex.link.Cacheable;

public class Renderable extends Cacheable {

	// Class30_Sub2_Sub4

	protected int modelHeight = 1000;
	protected VertexNormal[] normals;

	public int getModelHeight() {
		return modelHeight;
	}
	
	public boolean hasNormals(){
		return normals != null;
	}
	
	public VertexNormal getNormal(int index){
		return normals[index];
	}

	public VertexNormal[] getNormals() {
		return normals;
	}

	public Model model() {
		return null;
	}

	public void render(int x, int y, int orientation, int j, int k, int l, int i1, int height, int key) {
		Model model = model();
		if (model != null) {
			modelHeight = model.modelHeight;
			model.render(x, y, orientation, j, k, l, i1, height, key);
		}
	}

	public void setModelHeight(int modelHeight) {
		this.modelHeight = modelHeight;
	}

	public void setNormals(VertexNormal[] normals) {
		this.normals = normals;
	}
}