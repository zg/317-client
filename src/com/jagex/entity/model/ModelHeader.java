package com.jagex.entity.model;

public class ModelHeader {

	private int colourDataOffset;

	private byte[] data;
	private int texturedFaces;
	private int texturePointerOffset;
	private int faceAlphaOffset;
	private int faceDataOffset;
	private int facePriorityOffset;
	private int faces;
	private int faceSkinOffset;
	private int faceTypeOffset;
	private int uvMapFaceOffset;
	private int vertexDirectionOffset;
	private int vertexSkinOffset;
	private int vertices;
	private int xDataOffset;
	private int yDataOffset;
	private int zDataOffset;

	public int getColourDataOffset() {
		return colourDataOffset;
	}

	public byte[] getData() {
		return data;
	}

	public int getTexturedFaceCount() {
		return texturedFaces;
	}

	public int getTexturePointOffset() {
		return texturePointerOffset;
	}

	public int getFaceAlphaOffset() {
		return faceAlphaOffset;
	}

	public int getFaceDataOffset() {
		return faceDataOffset;
	}

	public int getFacePriorityOffset() {
		return facePriorityOffset;
	}

	public int getFaceCount() {
		return faces;
	}

	public int getFaceBoneOffset() {
		return faceSkinOffset;
	}

	public int getFaceTypeOffset() {
		return faceTypeOffset;
	}

	public int getUvMapFaceOffset() {
		return uvMapFaceOffset;
	}

	public int getVertexDirectionOffset() {
		return vertexDirectionOffset;
	}

	public int getVertexBoneOffset() {
		return vertexSkinOffset;
	}

	public int getVertices() {
		return vertices;
	}

	public int getXDataOffset() {
		return xDataOffset;
	}

	public int getYDataOffset() {
		return yDataOffset;
	}

	public int getZDataOffset() {
		return zDataOffset;
	}

	public void setColourDataOffset(int colourDataOffset) {
		this.colourDataOffset = colourDataOffset;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setTexturedFaceCount(int texturedTriangles) {
		this.texturedFaces = texturedTriangles;
	}

	public void setTexturePointerOffset(int texturePointerOffset) {
		this.texturePointerOffset = texturePointerOffset;
	}

	public void setFaceAlphaOffset(int triangleAlphaOffset) {
		this.faceAlphaOffset = triangleAlphaOffset;
	}

	public void setFaceDataOffset(int triangleDataOffset) {
		this.faceDataOffset = triangleDataOffset;
	}

	public void setFacePriorityOffset(int trianglePriorityOffset) {
		this.facePriorityOffset = trianglePriorityOffset;
	}

	public void setFaceCount(int triangles) {
		this.faces = triangles;
	}

	public void setFaceSkinOffset(int triangleSkinOffset) {
		this.faceSkinOffset = triangleSkinOffset;
	}

	public void setFaceTypeOffset(int triangleTypeOffset) {
		this.faceTypeOffset = triangleTypeOffset;
	}

	public void setUvMapFaceOffset(int uvMapTriangleOffset) {
		this.uvMapFaceOffset = uvMapTriangleOffset;
	}

	public void setVertexDirectionOffset(int vertexDirectionOffset) {
		this.vertexDirectionOffset = vertexDirectionOffset;
	}

	public void setVertexSkinOffset(int vertexSkinOffset) {
		this.vertexSkinOffset = vertexSkinOffset;
	}

	public void setVertices(int vertices) {
		this.vertices = vertices;
	}

	public void setXDataOffset(int xDataOffset) {
		this.xDataOffset = xDataOffset;
	}

	public void setYDataOffset(int yDataOffset) {
		this.yDataOffset = yDataOffset;
	}

	public void setZDataOffset(int zDataOffset) {
		this.zDataOffset = zDataOffset;
	}

}