package com.jagex.entity.model;

import org.major.cache.anim.FrameConstants;

import com.jagex.cache.anim.Frame;
import com.jagex.cache.anim.FrameBase;
import com.jagex.draw.Raster;
import com.jagex.draw.Rasterizer;
import com.jagex.entity.Renderable;
import com.jagex.io.Buffer;
import com.jagex.net.Provider;

public class Model extends Renderable {

	// Class30_Sub2_Sub4_Sub6

	public static boolean aBoolean1684;
	public static int anInt1687;
	public static int[] anIntArray1688 = new int[1000];
	public static int[] COSINE = Rasterizer.COSINE;
	public static Model EMPTY_MODEL = new Model();
	public static int mouseX;
	public static int mouseY;
	public static int[] SINE = Rasterizer.SINE;
	static boolean[] aBooleanArray1663 = new boolean[4096];
	static boolean[] aBooleanArray1664 = new boolean[4096];
	static int[] anIntArray1665 = new int[4096];
	static int[] anIntArray1666 = new int[4096];
	static int[] anIntArray1667 = new int[4096];
	static int[] anIntArray1668 = new int[4096];
	static int[] anIntArray1669 = new int[4096];
	static int[] anIntArray1670 = new int[4096];
	static int[] anIntArray1671 = new int[1500];
	static int[] anIntArray1673 = new int[12];
	static int[] anIntArray1675 = new int[2000];
	static int[] anIntArray1676 = new int[2000];
	static int[] anIntArray1677 = new int[12];
	static int[] anIntArray1678 = new int[10];
	static int[] anIntArray1679 = new int[10];
	static int[] anIntArray1680 = new int[10];
	static int[] anIntArray1691 = Rasterizer.anIntArray1482;
	static int[] anIntArray1692 = Rasterizer.anIntArray1469;
	static int[][] anIntArrayArray1672 = new int[1500][512];
	static int[][] anIntArrayArray1674 = new int[12][2000];
	static int centroidX;
	static int centroidY;
	static int centroidZ;
	static ModelHeader[] headers;
	static Provider provider;
	private static int[] anIntArray1622 = new int[2000];
	private static int[] anIntArray1623 = new int[2000];
	private static int[] anIntArray1624 = new int[2000];
	private static int[] anIntArray1625 = new int[2000];

	public static void clear(int id) {
		headers[id] = null;
	}

	public static void dispose() {
		headers = null;
		aBooleanArray1663 = null;
		aBooleanArray1664 = null;
		anIntArray1665 = null;
		anIntArray1666 = null;
		anIntArray1667 = null;
		anIntArray1668 = null;
		anIntArray1669 = null;
		anIntArray1670 = null;
		anIntArray1671 = null;
		anIntArrayArray1672 = null;
		anIntArray1673 = null;
		anIntArrayArray1674 = null;
		anIntArray1675 = null;
		anIntArray1676 = null;
		anIntArray1677 = null;
		SINE = null;
		COSINE = null;
		anIntArray1691 = null;
		anIntArray1692 = null;
	}

	public static void init(int count, Provider provider) {
		headers = new ModelHeader[count];
		Model.provider = provider;
	}

	public static void load(byte[] data, int id) {
		if (data == null) {
			ModelHeader header = headers[id] = new ModelHeader();
			header.setVertices(0);
			header.setFaceCount(0);
			header.setTexturedFaceCount(0);
			return;
		}

		Buffer buffer = new Buffer(data);
		buffer.setPosition(data.length - 18);
		ModelHeader header = headers[id] = new ModelHeader();
		header.setData(data);
		header.setVertices(buffer.readUShort());
		header.setFaceCount(buffer.readUShort());
		header.setTexturedFaceCount(buffer.readUByte());

		int useTextures = buffer.readUByte();
		int useFacePriority = buffer.readUByte();
		int useTransparency = buffer.readUByte();
		int useFaceSkinning = buffer.readUByte();
		int useVertexSkinning = buffer.readUByte();
		int xDataOffset = buffer.readUShort();
		int yDataOffset = buffer.readUShort();
		int zDataOffset = buffer.readUShort();
		int faceDataLength = buffer.readUShort();

		int offset = 0;
		header.setVertexDirectionOffset(offset);
		offset += header.getVertices();

		header.setFaceTypeOffset(offset);
		offset += header.getFaceCount();

		header.setFacePriorityOffset(offset);

		if (useFacePriority == 255) {
			offset += header.getFaceCount();
		} else {
			header.setFacePriorityOffset(-useFacePriority - 1);
		}

		header.setFaceSkinOffset(offset);
		if (useFaceSkinning == 1) {
			offset += header.getFaceCount();
		} else {
			header.setFaceSkinOffset(-1);
		}

		header.setTexturePointerOffset(offset);
		if (useTextures == 1) {
			offset += header.getFaceCount();
		} else {
			header.setTexturePointerOffset(-1);
		}

		header.setVertexSkinOffset(offset);
		if (useVertexSkinning == 1) {
			offset += header.getVertices();
		} else {
			header.setVertexSkinOffset(-1);
		}

		header.setFaceAlphaOffset(offset);
		if (useTransparency == 1) {
			offset += header.getFaceCount();
		} else {
			header.setFaceAlphaOffset(-1);
		}

		header.setFaceDataOffset(offset);
		offset += faceDataLength;

		header.setColourDataOffset(offset);
		offset += header.getFaceCount() * 2;

		header.setUvMapFaceOffset(offset);
		offset += header.getTexturedFaceCount() * 6;

		header.setXDataOffset(offset);
		offset += xDataOffset;

		header.setYDataOffset(offset);
		offset += yDataOffset;

		header.setZDataOffset(offset);
		offset += zDataOffset;
	}

	public static boolean loaded(int id) {
		if (headers == null) {
			return false;
		}

		ModelHeader header = headers[id];
		if (header == null) {
			provider.provide(id);
			return false;
		}
		return true;
	}

	public static Model lookup(int id) {
		if (headers == null) {
			return null;
		}

		ModelHeader header = headers[id];
		if (header == null) {
			provider.provide(id);
			return null;
		}

		return new Model(id);
	}

	public static int checkedLight(int colour, int light, int index) {
		if ((index & 0x2) != 0) {
			if (light < 0) {
				light = 0;
			} else if (light > 127) {
				light = 127;
			}

			return 127 - light;
		}

		light = light * (colour & 0x7f) >> 7;
		if (light < 2) {
			light = 2;
		} else if (light > 126) {
			light = 126;
		}

		return (colour & 0xff80) + light;
	}

	public boolean aBoolean1659;
	public int minimumX;
	public int maximumX;
	public int maximumZ;
	public int minimumZ;
	public int anInt1650;
	public int minimumY;
	public int anInt1652;
	public int anInt1653;
	public int anInt1654;
	public int[] anIntArray1634;
	public int[] anIntArray1635;
	public int[] anIntArray1636;
	public int[] faceAlphas;
	public int[] faceColours;
	public int[][] faceGroups;
	public int[] facePriorities;
	public int faces;
	public int[] faceBoneValues;
	public int[] faceIndexX;
	public int[] faceIndexY;
	public int[] faceIndexZ;
	public VertexNormal[] normals;
	public int priority;
	public int texturedFaces;
	public int[] texturedFaceIndexX;
	public int[] texturedFaceIndexY;
	public int[] texturedFaceIndexZ;
	public int[] anIntArray489;
	public int[][] vertexGroups;
	public int[] vertexBones;
	public int[] vertexX;
	public int[] vertexY;
	public int[] vertexZ;
	public int vertices;

	public Model(boolean contouredGround, boolean delayShading, Model model) {
		vertices = model.vertices;
		faces = model.faces;
		texturedFaces = model.texturedFaces;

		if (contouredGround) {
			vertexY = new int[vertices];

			for (int vertex = 0; vertex < vertices; vertex++) {
				vertexY[vertex] = model.vertexY[vertex];
			}
		} else {
			vertexY = model.vertexY;
		}

		if (delayShading) {
			anIntArray1634 = new int[faces];
			anIntArray1635 = new int[faces];
			anIntArray1636 = new int[faces];

			for (int k = 0; k < faces; k++) {
				anIntArray1634[k] = model.anIntArray1634[k];
				anIntArray1635[k] = model.anIntArray1635[k];
				anIntArray1636[k] = model.anIntArray1636[k];
			}

			anIntArray489 = new int[faces];
			if (model.anIntArray489 == null) {
				for (int triangle = 0; triangle < faces; triangle++) {
					anIntArray489[triangle] = 0;
				}
			} else {
				for (int index = 0; index < faces; index++) {
					anIntArray489[index] = model.anIntArray489[index];
				}
			}

			super.normals = new VertexNormal[vertices];
			for (int index = 0; index < vertices; index++) {
				VertexNormal parent = super.normals[index] = new VertexNormal();
				VertexNormal copied = ((Renderable) model).getNormal(index);
				parent.setX(copied.getX());
				parent.setY(copied.getY());
				parent.setZ(copied.getZ());
				parent.setFaceCount(copied.getFaceCount());
			}

			normals = model.normals;
		} else {
			anIntArray1634 = model.anIntArray1634;
			anIntArray1635 = model.anIntArray1635;
			anIntArray1636 = model.anIntArray1636;
			anIntArray489 = model.anIntArray489;
		}

		vertexX = model.vertexX;
		vertexZ = model.vertexZ;
		faceColours = model.faceColours;
		faceAlphas = model.faceAlphas;
		facePriorities = model.facePriorities;
		priority = model.priority;
		faceIndexX = model.faceIndexX;
		faceIndexY = model.faceIndexY;
		faceIndexZ = model.faceIndexZ;
		texturedFaceIndexX = model.texturedFaceIndexX;
		texturedFaceIndexY = model.texturedFaceIndexY;
		texturedFaceIndexZ = model.texturedFaceIndexZ;
		super.modelHeight = model.modelHeight;
		anInt1650 = model.anInt1650;
		anInt1653 = model.anInt1653;
		anInt1652 = model.anInt1652;
		minimumX = model.minimumX;
		maximumZ = model.maximumZ;
		minimumZ = model.minimumZ;
		maximumX = model.maximumX;
	}

	public Model(int modelCount, Model[] models) {
		boolean hasTexturePoints = false;
		boolean hasFacePriorities = false;
		boolean hasFaceAlphas = false;
		boolean hasSkinValues = false;
		vertices = 0;
		faces = 0;
		texturedFaces = 0;
		priority = -1;

		for (int index = 0; index < modelCount; index++) {
			Model model = models[index];
			if (model != null) {
				vertices += model.vertices;
				faces += model.faces;
				texturedFaces += model.texturedFaces;
				hasTexturePoints |= model.anIntArray489 != null;

				if (model.facePriorities != null) {
					hasFacePriorities = true;
				} else {
					if (priority == -1) {
						priority = model.priority;
					}
					if (priority != model.priority) {
						hasFacePriorities = true;
					}
				}

				hasFaceAlphas |= model.faceAlphas != null;
				hasSkinValues |= model.faceBoneValues != null;
			}
		}

		vertexX = new int[vertices];
		vertexY = new int[vertices];
		vertexZ = new int[vertices];
		vertexBones = new int[vertices];
		faceIndexX = new int[faces];
		faceIndexY = new int[faces];
		faceIndexZ = new int[faces];
		texturedFaceIndexX = new int[texturedFaces];
		texturedFaceIndexY = new int[texturedFaces];
		texturedFaceIndexZ = new int[texturedFaces];

		if (hasTexturePoints) {
			anIntArray489 = new int[faces];
		}
		if (hasFacePriorities) {
			facePriorities = new int[faces];
		}
		if (hasFaceAlphas) {
			faceAlphas = new int[faces];
		}
		if (hasSkinValues) {
			faceBoneValues = new int[faces];
		}

		faceColours = new int[faces];
		vertices = 0;
		faces = 0;
		texturedFaces = 0;
		int texturedCount = 0;

		for (int index = 0; index < modelCount; index++) {
			Model model = models[index];
			if (model != null) {
				for (int face = 0; face < model.faces; face++) {
					if (hasTexturePoints) {
						if (model.anIntArray489 == null) {
							anIntArray489[faces] = 0;
						} else {
							int point = model.anIntArray489[face];
							if ((point & 2) == 2) {
								point += texturedCount << 2;
							}

							anIntArray489[faces] = point;
						}
					}

					if (hasFacePriorities) {
						if (model.facePriorities == null) {
							facePriorities[faces] = model.priority;
						} else {
							facePriorities[faces] = model.facePriorities[face];
						}
					}

					if (hasFaceAlphas) {
						if (model.faceAlphas == null) {
							faceAlphas[faces] = 0;
						} else {
							faceAlphas[faces] = model.faceAlphas[face];
						}
					}

					if (hasSkinValues && model.faceBoneValues != null) {
						faceBoneValues[faces] = model.faceBoneValues[face];
					}

					faceColours[faces] = model.faceColours[face];
					faceIndexX[faces] = findMatchingVertex(model, model.faceIndexX[face]);
					faceIndexY[faces] = findMatchingVertex(model, model.faceIndexY[face]);
					faceIndexZ[faces] = findMatchingVertex(model, model.faceIndexZ[face]);
					faces++;
				}

				for (int face = 0; face < model.texturedFaces; face++) {
					texturedFaceIndexX[texturedFaces] = findMatchingVertex(model, model.texturedFaceIndexX[face]);
					texturedFaceIndexY[texturedFaces] = findMatchingVertex(model, model.texturedFaceIndexY[face]);
					texturedFaceIndexZ[texturedFaces] = findMatchingVertex(model, model.texturedFaceIndexZ[face]);
					texturedFaces++;
				}

				texturedCount += model.texturedFaces;
			}
		}
	}

	public Model(Model model, boolean shareColours, boolean shareAlphas, boolean shareVertices) {
		vertices = model.vertices;
		faces = model.faces;
		texturedFaces = model.texturedFaces;

		if (shareVertices) {
			vertexX = model.vertexX;
			vertexY = model.vertexY;
			vertexZ = model.vertexZ;
		} else {
			vertexX = new int[vertices];
			vertexY = new int[vertices];
			vertexZ = new int[vertices];

			for (int index = 0; index < vertices; index++) {
				vertexX[index] = model.vertexX[index];
				vertexY[index] = model.vertexY[index];
				vertexZ[index] = model.vertexZ[index];
			}
		}

		if (shareColours) {
			faceColours = model.faceColours;
		} else {
			faceColours = new int[faces];

			for (int face = 0; face < faces; face++) {
				faceColours[face] = model.faceColours[face];
			}
		}

		if (shareAlphas) {
			faceAlphas = model.faceAlphas;
		} else {
			faceAlphas = new int[faces];

			if (model.faceAlphas == null) {
				for (int face = 0; face < faces; face++) {
					faceAlphas[face] = 0;
				}
			} else {
				for (int face = 0; face < faces; face++) {
					faceAlphas[face] = model.faceAlphas[face];
				}
			}
		}

		vertexBones = model.vertexBones;
		faceBoneValues = model.faceBoneValues;
		anIntArray489 = model.anIntArray489;
		faceIndexX = model.faceIndexX;
		faceIndexY = model.faceIndexY;
		faceIndexZ = model.faceIndexZ;
		facePriorities = model.facePriorities;
		priority = model.priority;
		texturedFaceIndexX = model.texturedFaceIndexX;
		texturedFaceIndexY = model.texturedFaceIndexY;
		texturedFaceIndexZ = model.texturedFaceIndexZ;
	}

	public Model(Model[] models, int modelCount) {
		boolean hasTexturePoints = false;
		boolean hasFacePriorities = false;
		boolean hasFaceAlphas = false;
		boolean hasFaceColours = false;
		vertices = 0;
		faces = 0;
		texturedFaces = 0;
		priority = -1;

		for (int index = 0; index < modelCount; index++) {
			Model model = models[index];

			if (model != null) {
				vertices += model.vertices;
				faces += model.faces;
				texturedFaces += model.texturedFaces;
				hasTexturePoints |= model.anIntArray489 != null;

				if (model.facePriorities != null) {
					hasFacePriorities = true;
				} else {
					if (priority == -1) {
						priority = model.priority;
					}
					if (priority != model.priority) {
						hasFacePriorities = true;
					}
				}
				hasFaceAlphas |= model.faceAlphas != null;
				hasFaceColours |= model.faceColours != null;
			}
		}

		vertexX = new int[vertices];
		vertexY = new int[vertices];
		vertexZ = new int[vertices];
		faceIndexX = new int[faces];
		faceIndexY = new int[faces];
		faceIndexZ = new int[faces];
		anIntArray1634 = new int[faces];
		anIntArray1635 = new int[faces];
		anIntArray1636 = new int[faces];
		texturedFaceIndexX = new int[texturedFaces];
		texturedFaceIndexY = new int[texturedFaces];
		texturedFaceIndexZ = new int[texturedFaces];

		if (hasTexturePoints) {
			anIntArray489 = new int[faces];
		}
		if (hasFacePriorities) {
			facePriorities = new int[faces];
		}
		if (hasFaceAlphas) {
			faceAlphas = new int[faces];
		}
		if (hasFaceColours) {
			faceColours = new int[faces];
		}

		vertices = 0;
		faces = 0;
		texturedFaces = 0;
		int i1 = 0;

		for (int id = 0; id < modelCount; id++) {
			Model model = models[id];
			if (model != null) {
				int offset = this.vertices;

				for (int vertex = 0; vertex < model.vertices; vertex++) {
					vertexX[vertices] = model.vertexX[vertex];
					vertexY[vertices] = model.vertexY[vertex];
					vertexZ[vertices] = model.vertexZ[vertex];
					vertices++;
				}

				for (int face = 0; face < model.faces; face++) {
					faceIndexX[faces] = model.faceIndexX[face] + offset;
					faceIndexY[faces] = model.faceIndexY[face] + offset;
					faceIndexZ[faces] = model.faceIndexZ[face] + offset;
					anIntArray1634[faces] = model.anIntArray1634[face];
					anIntArray1635[faces] = model.anIntArray1635[face];
					anIntArray1636[faces] = model.anIntArray1636[face];

					if (hasTexturePoints) {
						if (model.anIntArray489 == null) {
							anIntArray489[faces] = 0;
						} else {
							int point = model.anIntArray489[face];
							if ((point & 2) == 2) {
								point += i1 << 2;
							}

							anIntArray489[faces] = point;
						}
					}

					if (hasFacePriorities) {
						if (model.facePriorities == null) {
							facePriorities[faces] = model.priority;
						} else {
							facePriorities[faces] = model.facePriorities[face];
						}
					}

					if (hasFaceAlphas) {
						if (model.faceAlphas == null) {
							faceAlphas[faces] = 0;
						} else {
							faceAlphas[faces] = model.faceAlphas[face];
						}
					}

					if (hasFaceColours && model.faceColours != null) {
						faceColours[faces] = model.faceColours[face];
					}
					faces++;
				}

				for (int face = 0; face < model.texturedFaces; face++) {
					texturedFaceIndexX[texturedFaces] = model.texturedFaceIndexX[face] + offset;
					texturedFaceIndexY[texturedFaces] = model.texturedFaceIndexY[face] + offset;
					texturedFaceIndexZ[texturedFaces] = model.texturedFaceIndexZ[face] + offset;
					texturedFaces++;
				}

				i1 += model.texturedFaces;
			}
		}

		method466();
	}

	private Model() {
	}

	private Model(int id) {
		ModelHeader header = headers[id];
		vertices = header.getVertices();
		faces = header.getFaceCount();
		texturedFaces = header.getTexturedFaceCount();
		vertexX = new int[vertices];
		vertexY = new int[vertices];
		vertexZ = new int[vertices];
		faceIndexX = new int[faces];
		faceIndexY = new int[faces];
		faceIndexZ = new int[faces];
		texturedFaceIndexX = new int[texturedFaces];
		texturedFaceIndexY = new int[texturedFaces];
		texturedFaceIndexZ = new int[texturedFaces];

		if (header.getVertexBoneOffset() >= 0) {
			vertexBones = new int[vertices];
		}

		if (header.getTexturePointOffset() >= 0) {
			anIntArray489 = new int[faces];
		}

		if (header.getFacePriorityOffset() >= 0) {
			facePriorities = new int[faces];
		} else {
			priority = -header.getFacePriorityOffset() - 1;
		}

		if (header.getFaceAlphaOffset() >= 0) {
			faceAlphas = new int[faces];
		}

		if (header.getFaceBoneOffset() >= 0) {
			faceBoneValues = new int[faces];
		}

		faceColours = new int[faces];
		Buffer directions = new Buffer(header.getData());
		directions.setPosition(header.getVertexDirectionOffset());

		Buffer verticesX = new Buffer(header.getData());
		verticesX.setPosition(header.getXDataOffset());

		Buffer verticesY = new Buffer(header.getData());
		verticesY.setPosition(header.getYDataOffset());

		Buffer verticesZ = new Buffer(header.getData());
		verticesZ.setPosition(header.getZDataOffset());

		Buffer bones = new Buffer(header.getData());
		bones.setPosition(header.getVertexBoneOffset());

		int baseX = 0;
		int baseY = 0;
		int baseZ = 0;

		for (int vertex = 0; vertex < vertices; vertex++) {
			int mask = directions.readUByte();
			int x = 0;
			if ((mask & 1) != 0) {
				x = verticesX.readSmart();
			}

			int y = 0;
			if ((mask & 2) != 0) {
				y = verticesY.readSmart();
			}

			int z = 0;
			if ((mask & 4) != 0) {
				z = verticesZ.readSmart();
			}

			vertexX[vertex] = baseX + x;
			vertexY[vertex] = baseY + y;
			vertexZ[vertex] = baseZ + z;
			baseX = vertexX[vertex];
			baseY = vertexY[vertex];
			baseZ = vertexZ[vertex];

			if (vertexBones != null) {
				vertexBones[vertex] = bones.readUByte();
			}
		}

		Buffer colours = directions;
		colours.setPosition(header.getColourDataOffset());

		Buffer points = verticesX;
		points.setPosition(header.getTexturePointOffset());

		Buffer priorities = verticesY;
		priorities.setPosition(header.getFacePriorityOffset());

		Buffer alphas = verticesZ;
		alphas.setPosition(header.getFaceAlphaOffset());

		bones.setPosition(header.getFaceBoneOffset());

		for (int face = 0; face < faces; face++) {
			faceColours[face] = colours.readUShort();
			if (anIntArray489 != null) {
				anIntArray489[face] = points.readUByte();
			}
			if (facePriorities != null) {
				facePriorities[face] = priorities.readUByte();
			}
			if (faceAlphas != null) {
				faceAlphas[face] = alphas.readUByte();
			}
			if (faceBoneValues != null) {
				faceBoneValues[face] = bones.readUByte();
			}
		}

		Buffer faceData = directions;
		faceData.setPosition(header.getFaceDataOffset());

		Buffer types = verticesX;
		types.setPosition(header.getFaceTypeOffset());

		int faceX = 0;
		int faceY = 0;
		int faceZ = 0;
		int offset = 0;

		for (int vertex = 0; vertex < faces; vertex++) {
			int type = types.readUByte();

			if (type == 1) {
				faceX = faceData.readSmart() + offset;
				offset = faceX;
				faceY = faceData.readSmart() + offset;
				offset = faceY;
				faceZ = faceData.readSmart() + offset;
				offset = faceZ;

				faceIndexX[vertex] = faceX;
				faceIndexY[vertex] = faceY;
				faceIndexZ[vertex] = faceZ;
			} else if (type == 2) {
				faceY = faceZ;
				faceZ = faceData.readSmart() + offset;
				offset = faceZ;

				faceIndexX[vertex] = faceX;
				faceIndexY[vertex] = faceY;
				faceIndexZ[vertex] = faceZ;
			} else if (type == 3) {
				faceX = faceZ;
				faceZ = faceData.readSmart() + offset;
				offset = faceZ;

				faceIndexX[vertex] = faceX;
				faceIndexY[vertex] = faceY;
				faceIndexZ[vertex] = faceZ;
			} else if (type == 4) {
				int temp = faceX;
				faceX = faceY;
				faceY = temp;

				faceZ = faceData.readSmart() + offset;
				offset = faceZ;

				faceIndexX[vertex] = faceX;
				faceIndexY[vertex] = faceY;
				faceIndexZ[vertex] = faceZ;
			}
		}

		Buffer maps = directions;
		maps.setPosition(header.getUvMapFaceOffset());

		for (int index = 0; index < texturedFaces; index++) {
			texturedFaceIndexX[index] = maps.readUShort();
			texturedFaceIndexY[index] = maps.readUShort();
			texturedFaceIndexZ[index] = maps.readUShort();
		}
	}

	public void apply(int frame) {
		if (vertexGroups == null) {
			return;
		} else if (frame == -1) {
			return;
		}

		Frame animation = Frame.lookup(frame);
		if (animation == null) {
			return;
		}

		FrameBase base = animation.getBase();
		centroidX = 0;
		centroidY = 0;
		centroidZ = 0;

		for (int transformation = 0; transformation < animation.getTransformationCount(); transformation++) {
			int group = animation.getTransformationIndex(transformation);
			transform(base.getTransformationType(group), base.getGroups(group), animation.getTransformX(transformation),
					animation.getTransformY(transformation), animation.getTransformZ(transformation));
		}
	}

	public void apply(int primaryId, int secondaryId, int[] interleaveOrder) {
		if (primaryId == -1) {
			return;
		} else if (interleaveOrder == null || secondaryId == -1) {
			apply(primaryId);
			return;
		}

		Frame primary = Frame.lookup(primaryId);
		if (primary == null) {
			return;
		}

		Frame secondary = Frame.lookup(secondaryId);
		if (secondary == null) {
			apply(primaryId);
			return;
		}

		FrameBase base = primary.getBase();
		centroidX = 0;
		centroidY = 0;
		centroidZ = 0;
		int index = 0;

		int next = interleaveOrder[index++];
		for (int transformation = 0; transformation < primary.getTransformationCount(); transformation++) {
			int group;
			for (group = primary.getTransformationIndex(transformation); group > next; next = interleaveOrder[index++]) {

			}
			if (group != next || base.getTransformationType(group) == 0) {
				transform(base.getTransformationType(group), base.getGroups(group), primary.getTransformX(transformation),
						primary.getTransformY(transformation), primary.getTransformZ(transformation));
			}
		}

		centroidX = 0;
		centroidY = 0;
		centroidZ = 0;
		index = 0;
		next = interleaveOrder[index++];

		for (int transformation = 0; transformation < secondary.getTransformationCount(); transformation++) {
			int group;
			for (group = secondary.getTransformationIndex(transformation); group > next; next = interleaveOrder[index++]) {

			}

			if (group == next || base.getTransformationType(group) == 0) {
				transform(base.getTransformationType(group), base.getGroups(group), secondary.getTransformX(transformation),
						secondary.getTransformY(transformation), secondary.getTransformZ(transformation));
			}
		}
	}

	public void computeSphericalBounds() {
		super.modelHeight = 0;
		minimumY = 0;

		for (int i = 0; i < vertices; i++) {
			int y = vertexY[i];
			if (-y > super.modelHeight) {
				super.modelHeight = -y;
			}

			if (y > minimumY) {
				minimumY = y;
			}
		}

		anInt1653 = (int) (Math.sqrt(anInt1650 * anInt1650 + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		anInt1652 = anInt1653 + (int) (Math.sqrt(anInt1650 * anInt1650 + minimumY * minimumY) + 0.98999999999999999D);
	}

	public void invert() {
		for (int vertex = 0; vertex < vertices; vertex++) {
			vertexZ[vertex] = -vertexZ[vertex];
		}

		for (int vertex = 0; vertex < faces; vertex++) {
			int x = faceIndexX[vertex];
			faceIndexX[vertex] = faceIndexZ[vertex];
			faceIndexZ[vertex] = x;
		}
	}

	public final void light(int lighting, int diffusion, int x, int y, int z, boolean immediateShading) {
		int length = (int) Math.sqrt(x * x + y * y + z * z);
		int k1 = diffusion * length >> 8;

		if (anIntArray1634 == null) {
			anIntArray1634 = new int[faces];
			anIntArray1635 = new int[faces];
			anIntArray1636 = new int[faces];
		}

		if (super.normals == null) {
			super.normals = new VertexNormal[vertices];
			for (int index = 0; index < vertices; index++) {
				super.normals[index] = new VertexNormal();
			}
		}

		for (int face = 0; face < faces; face++) {
			int faceX = faceIndexX[face];
			int faceY = faceIndexY[face];
			int faceZ = faceIndexZ[face];
			int j3 = vertexX[faceY] - vertexX[faceX];
			int k3 = vertexY[faceY] - vertexY[faceX];
			int l3 = vertexZ[faceY] - vertexZ[faceX];
			int i4 = vertexX[faceZ] - vertexX[faceX];
			int j4 = vertexY[faceZ] - vertexY[faceX];
			int k4 = vertexZ[faceZ] - vertexZ[faceX];
			int dx = k3 * k4 - j4 * l3;
			int dy = l3 * i4 - k4 * j3;
			int dz;

			for (dz = j3 * j4 - i4 * k3; dx > 8192 || dy > 8192 || dz > 8192 || dx < -8192 || dy < -8192 || dz < -8192; dz >>= 1) {
				dx >>= 1;
				dy >>= 1;
			}

			int deltaLength = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (deltaLength <= 0) {
				deltaLength = 1;
			}

			dx = dx * 256 / deltaLength;
			dy = dy * 256 / deltaLength;
			dz = dz * 256 / deltaLength;

			if (anIntArray489 == null || (anIntArray489[face] & 1) == 0) {
				VertexNormal normal = super.normals[faceX];
				normal.setX(normal.getX() + dx);
				normal.setY(normal.getY() + dy);
				normal.setZ(normal.getZ() + dz);
				normal.setFaceCount(normal.getFaceCount() + 1);
				normal = super.normals[faceY];
				normal.setX(normal.getX() + dx);
				normal.setY(normal.getY() + dy);
				normal.setZ(normal.getZ() + dz);
				normal.setFaceCount(normal.getFaceCount() + 1);
				normal = super.normals[faceZ];
				normal.setX(normal.getX() + dx);
				normal.setY(normal.getY() + dy);
				normal.setZ(normal.getZ() + dz);
				normal.setFaceCount(normal.getFaceCount() + 1);
			} else {
				int l5 = lighting + (x * dx + y * dy + z * dz) / (k1 + k1 / 2);
				anIntArray1634[face] = checkedLight(faceColours[face], l5, anIntArray489[face]);
			}
		}

		if (immediateShading) {
			method480(lighting, k1, x, y, z);
		} else {
			normals = new VertexNormal[vertices];
			for (int index = 0; index < vertices; index++) {
				VertexNormal parent = super.normals[index];
				VertexNormal copied = normals[index] = new VertexNormal();
				copied.setX(parent.getX());
				copied.setY(parent.getY());
				copied.setZ(parent.getZ());
				copied.setFaceCount(parent.getFaceCount());
			}
		}

		if (immediateShading) {
			method466();
		} else {
			method468();
		}
	}

	public void method464(Model model, boolean shareAlphas) {
		vertices = model.vertices;
		faces = model.faces;
		texturedFaces = model.texturedFaces;

		if (anIntArray1622.length < vertices) {
			anIntArray1622 = new int[vertices + 100];
			anIntArray1623 = new int[vertices + 100];
			anIntArray1624 = new int[vertices + 100];
		}

		vertexX = anIntArray1622;
		vertexY = anIntArray1623;
		vertexZ = anIntArray1624;
		for (int vertex = 0; vertex < vertices; vertex++) {
			vertexX[vertex] = model.vertexX[vertex];
			vertexY[vertex] = model.vertexY[vertex];
			vertexZ[vertex] = model.vertexZ[vertex];
		}

		if (shareAlphas) {
			faceAlphas = model.faceAlphas;
		} else {
			if (anIntArray1625.length < faces) {
				anIntArray1625 = new int[faces + 100];
			}
			faceAlphas = anIntArray1625;

			if (model.faceAlphas == null) {
				for (int index = 0; index < faces; index++) {
					faceAlphas[index] = 0;
				}
			} else {
				for (int index = 0; index < faces; index++) {
					faceAlphas[index] = model.faceAlphas[index];
				}
			}
		}

		anIntArray489 = model.anIntArray489;
		faceColours = model.faceColours;
		facePriorities = model.facePriorities;
		priority = model.priority;
		faceGroups = model.faceGroups;
		vertexGroups = model.vertexGroups;
		faceIndexX = model.faceIndexX;
		faceIndexY = model.faceIndexY;
		faceIndexZ = model.faceIndexZ;
		anIntArray1634 = model.anIntArray1634;
		anIntArray1635 = model.anIntArray1635;
		anIntArray1636 = model.anIntArray1636;
		texturedFaceIndexX = model.texturedFaceIndexX;
		texturedFaceIndexY = model.texturedFaceIndexY;
		texturedFaceIndexZ = model.texturedFaceIndexZ;
	}

	public void method466() {
		super.modelHeight = 0;
		anInt1650 = 0;
		minimumY = 0;

		for (int vertex = 0; vertex < vertices; vertex++) {
			int x = vertexX[vertex];
			int y = vertexY[vertex];
			int z = vertexZ[vertex];

			if (-y > super.modelHeight) {
				super.modelHeight = -y;
			}

			if (y > minimumY) {
				minimumY = y;
			}

			int i1 = x * x + z * z;
			if (i1 > anInt1650) {
				anInt1650 = i1;
			}
		}

		anInt1650 = (int) (Math.sqrt(anInt1650) + 0.99D);
		anInt1653 = (int) (Math.sqrt(anInt1650 * anInt1650 + super.modelHeight * super.modelHeight) + 0.99D);
		anInt1652 = anInt1653 + (int) (Math.sqrt(anInt1650 * anInt1650 + minimumY * minimumY) + 0.99D);
	}

	public void method468() {
		super.modelHeight = 0;
		anInt1650 = 0;
		minimumY = 0;
		minimumX = 0xf423f;
		maximumX = 0xfff0bdc1;
		maximumZ = 0xfffe7961;
		minimumZ = 0x1869f;

		for (int vertex = 0; vertex < vertices; vertex++) {
			int x = vertexX[vertex];
			int y = vertexY[vertex];
			int z = vertexZ[vertex];

			if (x < minimumX) {
				minimumX = x;
			}

			if (x > maximumX) {
				maximumX = x;
			}

			if (z < minimumZ) {
				minimumZ = z;
			}

			if (z > maximumZ) {
				maximumZ = z;
			}

			if (-y > super.modelHeight) {
				super.modelHeight = -y;
			}

			if (y > minimumY) {
				minimumY = y;
			}

			int j1 = x * x + z * z;
			if (j1 > anInt1650) {
				anInt1650 = j1;
			}
		}

		anInt1650 = (int) Math.sqrt(anInt1650);
		anInt1653 = (int) Math.sqrt(anInt1650 * anInt1650 + super.modelHeight * super.modelHeight);
		anInt1652 = anInt1653 + (int) Math.sqrt(anInt1650 * anInt1650 + minimumY * minimumY);
	}

	public final void method480(int lighting, int j, int x, int y, int z) {
		for (int face = 0; face < faces; face++) {
			int indexX = faceIndexX[face];
			int indexY = faceIndexY[face];
			int indexZ = faceIndexZ[face];

			if (anIntArray489 == null) {
				int colour = faceColours[face];
				VertexNormal normal = super.normals[indexX];

				int light = lighting + (x * normal.getX() + y * normal.getY() + z * normal.getZ()) / (j * normal.getFaceCount());
				anIntArray1634[face] = checkedLight(colour, light, 0);

				normal = super.normals[indexY];
				light = lighting + (x * normal.getX() + y * normal.getY() + z * normal.getZ()) / (j * normal.getFaceCount());
				anIntArray1635[face] = checkedLight(colour, light, 0);

				normal = super.normals[indexZ];
				light = lighting + (x * normal.getX() + y * normal.getY() + z * normal.getZ()) / (j * normal.getFaceCount());
				anIntArray1636[face] = checkedLight(colour, light, 0);
			} else if ((anIntArray489[face] & 1) == 0) {
				int colour = faceColours[face];
				int point = anIntArray489[face];

				VertexNormal normal = super.normals[indexX];
				int light = lighting + (x * normal.getX() + y * normal.getY() + z * normal.getZ()) / (j * normal.getFaceCount());
				anIntArray1634[face] = checkedLight(colour, light, point);
				normal = super.normals[indexY];

				light = lighting + (x * normal.getX() + y * normal.getY() + z * normal.getZ()) / (j * normal.getFaceCount());
				anIntArray1635[face] = checkedLight(colour, light, point);
				normal = super.normals[indexZ];

				light = lighting + (x * normal.getX() + y * normal.getY() + z * normal.getZ()) / (j * normal.getFaceCount());
				anIntArray1636[face] = checkedLight(colour, light, point);
			}
		}

		super.normals = null;
		normals = null;
		vertexBones = null;
		faceBoneValues = null;
		if (anIntArray489 != null) {
			for (int index = 0; index < faces; index++) {
				if ((anIntArray489[index] & 2) == 2) {
					return;
				}
			}
		}

		faceColours = null;
	}

	public void pitch(int theta) {
		int sin = SINE[theta];
		int cos = COSINE[theta];

		for (int vertex = 0; vertex < vertices; vertex++) {
			int y = vertexY[vertex] * cos - vertexZ[vertex] * sin >> 16;
			vertexZ[vertex] = vertexY[vertex] * sin + vertexZ[vertex] * cos >> 16;
			vertexY[vertex] = y;
		}
	}

	public void recolour(int oldColour, int newColour) {
		for (int index = 0; index < faces; index++) {
			if (faceColours[index] == oldColour) {
				faceColours[index] = newColour;
			}
		}
	}

	public final void render(int i, int roll, int yaw, int pitch, int dx, int j1, int k1) {
		int viewX = Rasterizer.originViewX;
		int viewY = Rasterizer.originViewY;
		int j2 = SINE[i];
		int k2 = COSINE[i];
		int l2 = SINE[roll];
		int i3 = COSINE[roll];
		int j3 = SINE[yaw];
		int k3 = COSINE[yaw];
		int l3 = SINE[pitch];
		int i4 = COSINE[pitch];
		int j4 = j1 * l3 + k1 * i4 >> 16;
		for (int k4 = 0; k4 < vertices; k4++) {
			int x = vertexX[k4];
			int y = vertexY[k4];
			int z = vertexZ[k4];
			if (yaw != 0) {
				int k5 = y * j3 + x * k3 >> 16;
				y = y * k3 - x * j3 >> 16;
				x = k5;
			}
			if (i != 0) {
				int l5 = y * k2 - z * j2 >> 16;
				z = y * j2 + z * k2 >> 16;
				y = l5;
			}
			if (roll != 0) {
				int i6 = z * l2 + x * i3 >> 16;
				z = z * i3 - x * l2 >> 16;
				x = i6;
			}
			x += dx;
			y += j1;
			z += k1;
			int j6 = y * i4 - z * l3 >> 16;
			z = y * l3 + z * i4 >> 16;
			y = j6;
			anIntArray1667[k4] = z - j4;
			anIntArray1665[k4] = viewX + (x << 9) / z;
			anIntArray1666[k4] = viewY + (y << 9) / z;
			if (texturedFaces > 0) {
				anIntArray1668[k4] = x;
				anIntArray1669[k4] = y;
				anIntArray1670[k4] = z;
			}
		}

		try {
			method483(false, false, 0);
		} catch (Exception _ex) {
		}
	}

	@Override
	public final void render(int x, int y, int orientation, int j, int k, int l, int i1, int height, int key) {
		int j2 = y * i1 - x * l >> 16;
		int k2 = height * j + j2 * k >> 16;
		int l2 = anInt1650 * k >> 16;
		int i3 = k2 + l2;

		if (i3 <= 50 || k2 >= 3500) {
			return;
		}

		int j3 = y * l + x * i1 >> 16;
		int k3 = j3 - anInt1650 << 9;
		if (k3 / i3 >= Raster.getCentreX()) {
			return;
		}

		int l3 = j3 + anInt1650 << 9;
		if (l3 / i3 <= -Raster.getCentreX()) {
			return;
		}

		int i4 = height * k - j2 * j >> 16;
		int j4 = anInt1650 * j >> 16;
		int k4 = i4 + j4 << 9;

		if (k4 / i3 <= -Raster.getCentreY()) {
			return;
		}

		int l4 = j4 + (super.modelHeight * k >> 16);
		int i5 = i4 - l4 << 9;
		if (i5 / i3 >= Raster.getCentreY()) {
			return;
		}

		int j5 = l2 + (super.modelHeight * j >> 16);
		boolean flag = false;
		if (k2 - j5 <= 50) {
			flag = true;
		}

		boolean flag1 = false;
		if (key > 0 && aBoolean1684) {
			int k5 = k2 - l2;
			if (k5 <= 50) {
				k5 = 50;
			}

			if (j3 > 0) {
				k3 /= i3;
				l3 /= k5;
			} else {
				l3 /= i3;
				k3 /= k5;
			}

			if (i4 > 0) {
				i5 /= i3;
				k4 /= k5;
			} else {
				k4 /= i3;
				i5 /= k5;
			}

			int i6 = mouseX - Rasterizer.originViewX;
			int k6 = mouseY - Rasterizer.originViewY;

			if (i6 > k3 && i6 < l3 && k6 > i5 && k6 < k4) {
				if (aBoolean1659) {
					anIntArray1688[anInt1687++] = key;
				} else {
					flag1 = true;
				}
			}
		}

		int viewX = Rasterizer.originViewX;
		int viewY = Rasterizer.originViewY;
		int sine = 0;
		int cosine = 0;

		if (orientation != 0) {
			sine = SINE[orientation];
			cosine = COSINE[orientation];
		}

		for (int vertex = 0; vertex < vertices; vertex++) {
			int xVertex = vertexX[vertex];
			int yVertex = vertexY[vertex];
			int zVertex = vertexZ[vertex];
			if (orientation != 0) {
				int j8 = zVertex * sine + xVertex * cosine >> 16;
				zVertex = zVertex * cosine - xVertex * sine >> 16;
				xVertex = j8;
			}

			xVertex += x;
			yVertex += height;
			zVertex += y;
			int k8 = zVertex * l + xVertex * i1 >> 16;
			zVertex = zVertex * i1 - xVertex * l >> 16;
			xVertex = k8;
			k8 = yVertex * k - zVertex * j >> 16;
			zVertex = yVertex * j + zVertex * k >> 16;
			yVertex = k8;
			anIntArray1667[vertex] = zVertex - k2;

			if (zVertex >= 50) {
				anIntArray1665[vertex] = viewX + (xVertex << 9) / zVertex;
				anIntArray1666[vertex] = viewY + (yVertex << 9) / zVertex;
			} else {
				anIntArray1665[vertex] = -5000;
				flag = true;
			}

			if (flag || texturedFaces > 0) {
				anIntArray1668[vertex] = xVertex;
				anIntArray1669[vertex] = yVertex;
				anIntArray1670[vertex] = zVertex;
			}
		}

		try {
			method483(flag, flag1, key);
		} catch (Exception ex) {
		}
	}

	public void rotateClockwise() {
		for (int index = 0; index < vertices; index++) {
			int x = vertexX[index];
			vertexX[index] = vertexZ[index];
			vertexZ[index] = -x;
		}
	}

	public void scale(int x, int y, int z) {
		for (int vertex = 0; vertex < vertices; vertex++) {
			vertexX[vertex] = vertexX[vertex] * x / 128;
			vertexY[vertex] = vertexY[vertex] * z / 128;
			vertexZ[vertex] = vertexZ[vertex] * y / 128;
		}
	}

	public void skin() {
		if (vertexBones != null) {
			int ai[] = new int[256];
			int j = 0;

			for (int l = 0; l < vertices; l++) {
				int j1 = vertexBones[l];
				ai[j1]++;
				if (j1 > j) {
					j = j1;
				}
			}

			vertexGroups = new int[j + 1][];
			for (int k1 = 0; k1 <= j; k1++) {
				vertexGroups[k1] = new int[ai[k1]];
				ai[k1] = 0;
			}

			for (int j2 = 0; j2 < vertices; j2++) {
				int l2 = vertexBones[j2];
				vertexGroups[l2][ai[l2]++] = j2;
			}

			vertexBones = null;
		}

		if (faceBoneValues != null) {
			int ai1[] = new int[256];
			int k = 0;
			for (int i1 = 0; i1 < faces; i1++) {
				int l1 = faceBoneValues[i1];
				ai1[l1]++;
				if (l1 > k) {
					k = l1;
				}
			}

			faceGroups = new int[k + 1][];
			for (int i2 = 0; i2 <= k; i2++) {
				faceGroups[i2] = new int[ai1[i2]];
				ai1[i2] = 0;
			}

			for (int k2 = 0; k2 < faces; k2++) {
				int i3 = faceBoneValues[k2];
				faceGroups[i3][ai1[i3]++] = k2;
			}

			faceBoneValues = null;
		}
	}

	public void translate(int x, int y, int z) {
		for (int vertex = 0; vertex < vertices; vertex++) {
			vertexX[vertex] += x;
			vertexY[vertex] += y;
			vertexZ[vertex] += z;
		}
	}

	private final int findMatchingVertex(Model model, int vertex) {
		int matched = -1;
		int x = model.vertexX[vertex];
		int y = model.vertexY[vertex];
		int z = model.vertexZ[vertex];

		for (int index = 0; index < vertices; index++) {
			if (x == vertexX[index] && y == vertexY[index] && z == vertexZ[index]) {
				matched = index;
				break;
			}
		}

		if (matched == -1) {
			vertexX[vertices] = x;
			vertexY[vertices] = y;
			vertexZ[vertices] = z;

			if (model.vertexBones != null) {
				vertexBones[vertices] = model.vertexBones[vertex];
			}

			matched = vertices++;
		}

		return matched;
	}

	private final boolean insideTriangle(int x, int y, int k, int l, int i1, int j1, int k1, int l1) {
		if (y < k && y < l && y < i1) {
			return false;
		}
		if (y > k && y > l && y > i1) {
			return false;
		}
		if (x < j1 && x < k1 && x < l1) {
			return false;
		}
		return x <= j1 || x <= k1 || x <= l1;
	}

	private final void method483(boolean flag, boolean flag1, int config) {
		for (int j = 0; j < anInt1652; j++) {
			anIntArray1671[j] = 0;
		}

		for (int face = 0; face < faces; face++) {
			if (anIntArray489 == null || anIntArray489[face] != -1) {
				int indexX = faceIndexX[face];
				int indexY = faceIndexY[face];
				int indexZ = faceIndexZ[face];
				int i3 = anIntArray1665[indexX];
				int l3 = anIntArray1665[indexY];
				int k4 = anIntArray1665[indexZ];

				if (flag && (i3 == -5000 || l3 == -5000 || k4 == -5000)) {
					aBooleanArray1664[face] = true;
					int j5 = (anIntArray1667[indexX] + anIntArray1667[indexY] + anIntArray1667[indexZ]) / 3 + anInt1653;
					anIntArrayArray1672[j5][anIntArray1671[j5]++] = face;
				} else {
					if (flag1
							&& insideTriangle(mouseX, mouseY, anIntArray1666[indexX], anIntArray1666[indexY],
									anIntArray1666[indexZ], i3, l3, k4)) {
						anIntArray1688[anInt1687++] = config;
						flag1 = false;
					}
					if ((i3 - l3) * (anIntArray1666[indexZ] - anIntArray1666[indexY])
							- (anIntArray1666[indexX] - anIntArray1666[indexY]) * (k4 - l3) > 0) {
						aBooleanArray1664[face] = false;
						if (i3 < 0 || l3 < 0 || k4 < 0 || i3 > Raster.anInt1385 || l3 > Raster.anInt1385 || k4 > Raster.anInt1385) {
							aBooleanArray1663[face] = true;
						} else {
							aBooleanArray1663[face] = false;
						}
						int k5 = (anIntArray1667[indexX] + anIntArray1667[indexY] + anIntArray1667[indexZ]) / 3 + anInt1653;
						anIntArrayArray1672[k5][anIntArray1671[k5]++] = face;
					}
				}
			}
		}

		if (facePriorities == null) {
			for (int i1 = anInt1652 - 1; i1 >= 0; i1--) {
				int l1 = anIntArray1671[i1];
				if (l1 > 0) {
					int ai[] = anIntArrayArray1672[i1];
					for (int j3 = 0; j3 < l1; j3++) {
						method484(ai[j3]);
					}
				}
			}

			return;
		}
		for (int j1 = 0; j1 < 12; j1++) {
			anIntArray1673[j1] = 0;
			anIntArray1677[j1] = 0;
		}

		for (int i2 = anInt1652 - 1; i2 >= 0; i2--) {
			int k2 = anIntArray1671[i2];
			if (k2 > 0) {
				int ai1[] = anIntArrayArray1672[i2];
				for (int i4 = 0; i4 < k2; i4++) {
					int l4 = ai1[i4];
					int l5 = facePriorities[l4];
					int j6 = anIntArray1673[l5]++;
					anIntArrayArray1674[l5][j6] = l4;
					if (l5 < 10) {
						anIntArray1677[l5] += i2;
					} else if (l5 == 10) {
						anIntArray1675[j6] = i2;
					} else {
						anIntArray1676[j6] = i2;
					}
				}

			}
		}

		int l2 = 0;
		if (anIntArray1673[1] > 0 || anIntArray1673[2] > 0) {
			l2 = (anIntArray1677[1] + anIntArray1677[2]) / (anIntArray1673[1] + anIntArray1673[2]);
		}
		int k3 = 0;
		if (anIntArray1673[3] > 0 || anIntArray1673[4] > 0) {
			k3 = (anIntArray1677[3] + anIntArray1677[4]) / (anIntArray1673[3] + anIntArray1673[4]);
		}
		int j4 = 0;
		if (anIntArray1673[6] > 0 || anIntArray1673[8] > 0) {
			j4 = (anIntArray1677[6] + anIntArray1677[8]) / (anIntArray1673[6] + anIntArray1673[8]);
		}
		int i6 = 0;
		int k6 = anIntArray1673[10];
		int ai2[] = anIntArrayArray1674[10];
		int ai3[] = anIntArray1675;
		if (i6 == k6) {
			i6 = 0;
			k6 = anIntArray1673[11];
			ai2 = anIntArrayArray1674[11];
			ai3 = anIntArray1676;
		}
		int i5;
		if (i6 < k6) {
			i5 = ai3[i6];
		} else {
			i5 = -1000;
		}
		for (int l6 = 0; l6 < 10; l6++) {
			while (l6 == 0 && i5 > l2) {
				method484(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			while (l6 == 3 && i5 > k3) {
				method484(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			while (l6 == 5 && i5 > j4) {
				method484(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			int i7 = anIntArray1673[l6];
			int ai4[] = anIntArrayArray1674[l6];
			for (int j7 = 0; j7 < i7; j7++) {
				method484(ai4[j7]);
			}

		}

		while (i5 != -1000) {
			method484(ai2[i6++]);
			if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
				i6 = 0;
				ai2 = anIntArrayArray1674[11];
				k6 = anIntArray1673[11];
				ai3 = anIntArray1676;
			}
			if (i6 < k6) {
				i5 = ai3[i6];
			} else {
				i5 = -1000;
			}
		}
	}

	private final void method484(int i) {
		if (aBooleanArray1664[i]) {
			method485(i);
			return;
		}
		int j = faceIndexX[i];
		int k = faceIndexY[i];
		int l = faceIndexZ[i];
		Rasterizer.aBoolean1462 = aBooleanArray1663[i];
		if (faceAlphas == null) {
			Rasterizer.currentAlpha = 0;
		} else {
			Rasterizer.currentAlpha = faceAlphas[i];
		}
		int type;
		if (anIntArray489 == null) {
			type = 0;
		} else {
			type = anIntArray489[i] & 3;
		}
		
		if (type == 0) {
			Rasterizer.method374(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k],
					anIntArray1665[l], anIntArray1634[i], anIntArray1635[i], anIntArray1636[i]);
		} else if (type == 1) {
			Rasterizer.method376(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k],
					anIntArray1665[l], anIntArray1691[anIntArray1634[i]]);
		} else if (type == 2) {
			int j1 = anIntArray489[i] >> 2;
			int l1 = texturedFaceIndexX[j1];
			int j2 = texturedFaceIndexY[j1];
			int l2 = texturedFaceIndexZ[j1];
			
			Rasterizer.method378(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k],
					anIntArray1665[l], anIntArray1634[i], anIntArray1635[i], anIntArray1636[i], anIntArray1668[l1],
					anIntArray1668[j2], anIntArray1668[l2], anIntArray1669[l1], anIntArray1669[j2], anIntArray1669[l2],
					anIntArray1670[l1], anIntArray1670[j2], anIntArray1670[l2], faceColours[i]);
		} else if (type == 3) {
			int k1 = anIntArray489[i] >> 2;
			int i2 = texturedFaceIndexX[k1];
			int k2 = texturedFaceIndexY[k1];
			int i3 = texturedFaceIndexZ[k1];
			
			Rasterizer.method378(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k],
					anIntArray1665[l], anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], anIntArray1668[i2],
					anIntArray1668[k2], anIntArray1668[i3], anIntArray1669[i2], anIntArray1669[k2], anIntArray1669[i3],
					anIntArray1670[i2], anIntArray1670[k2], anIntArray1670[i3], faceColours[i]);
		}
	}

	private final void method485(int i) {
		int j = Rasterizer.originViewX;
		int k = Rasterizer.originViewY;
		int l = 0;
		int i1 = faceIndexX[i];
		int j1 = faceIndexY[i];
		int k1 = faceIndexZ[i];
		int l1 = anIntArray1670[i1];
		int i2 = anIntArray1670[j1];
		int j2 = anIntArray1670[k1];
		if (l1 >= 50) {
			anIntArray1678[l] = anIntArray1665[i1];
			anIntArray1679[l] = anIntArray1666[i1];
			anIntArray1680[l++] = anIntArray1634[i];
		} else {
			int k2 = anIntArray1668[i1];
			int k3 = anIntArray1669[i1];
			int k4 = anIntArray1634[i];
			if (j2 >= 50) {
				int k5 = (50 - l1) * anIntArray1692[j2 - l1];
				anIntArray1678[l] = j + (k2 + ((anIntArray1668[k1] - k2) * k5 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (k3 + ((anIntArray1669[k1] - k3) * k5 >> 16) << 9) / 50;
				anIntArray1680[l++] = k4 + ((anIntArray1636[i] - k4) * k5 >> 16);
			}
			if (i2 >= 50) {
				int l5 = (50 - l1) * anIntArray1692[i2 - l1];
				anIntArray1678[l] = j + (k2 + ((anIntArray1668[j1] - k2) * l5 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (k3 + ((anIntArray1669[j1] - k3) * l5 >> 16) << 9) / 50;
				anIntArray1680[l++] = k4 + ((anIntArray1635[i] - k4) * l5 >> 16);
			}
		}
		if (i2 >= 50) {
			anIntArray1678[l] = anIntArray1665[j1];
			anIntArray1679[l] = anIntArray1666[j1];
			anIntArray1680[l++] = anIntArray1635[i];
		} else {
			int l2 = anIntArray1668[j1];
			int l3 = anIntArray1669[j1];
			int l4 = anIntArray1635[i];
			if (l1 >= 50) {
				int i6 = (50 - i2) * anIntArray1692[l1 - i2];
				anIntArray1678[l] = j + (l2 + ((anIntArray1668[i1] - l2) * i6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (l3 + ((anIntArray1669[i1] - l3) * i6 >> 16) << 9) / 50;
				anIntArray1680[l++] = l4 + ((anIntArray1634[i] - l4) * i6 >> 16);
			}
			if (j2 >= 50) {
				int j6 = (50 - i2) * anIntArray1692[j2 - i2];
				anIntArray1678[l] = j + (l2 + ((anIntArray1668[k1] - l2) * j6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (l3 + ((anIntArray1669[k1] - l3) * j6 >> 16) << 9) / 50;
				anIntArray1680[l++] = l4 + ((anIntArray1636[i] - l4) * j6 >> 16);
			}
		}
		if (j2 >= 50) {
			anIntArray1678[l] = anIntArray1665[k1];
			anIntArray1679[l] = anIntArray1666[k1];
			anIntArray1680[l++] = anIntArray1636[i];
		} else {
			int i3 = anIntArray1668[k1];
			int i4 = anIntArray1669[k1];
			int i5 = anIntArray1636[i];
			if (i2 >= 50) {
				int k6 = (50 - j2) * anIntArray1692[i2 - j2];
				anIntArray1678[l] = j + (i3 + ((anIntArray1668[j1] - i3) * k6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (i4 + ((anIntArray1669[j1] - i4) * k6 >> 16) << 9) / 50;
				anIntArray1680[l++] = i5 + ((anIntArray1635[i] - i5) * k6 >> 16);
			}
			if (l1 >= 50) {
				int l6 = (50 - j2) * anIntArray1692[l1 - j2];
				anIntArray1678[l] = j + (i3 + ((anIntArray1668[i1] - i3) * l6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (i4 + ((anIntArray1669[i1] - i4) * l6 >> 16) << 9) / 50;
				anIntArray1680[l++] = i5 + ((anIntArray1634[i] - i5) * l6 >> 16);
			}
		}
		int j3 = anIntArray1678[0];
		int j4 = anIntArray1678[1];
		int j5 = anIntArray1678[2];
		int i7 = anIntArray1679[0];
		int j7 = anIntArray1679[1];
		int k7 = anIntArray1679[2];
		if ((j3 - j4) * (k7 - j7) - (i7 - j7) * (j5 - j4) > 0) {
			Rasterizer.aBoolean1462 = false;
			if (l == 3) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > Raster.anInt1385 || j4 > Raster.anInt1385 || j5 > Raster.anInt1385) {
					Rasterizer.aBoolean1462 = true;
				}
				int l7;
				if (anIntArray489 == null) {
					l7 = 0;
				} else {
					l7 = anIntArray489[i] & 3;
				}
				if (l7 == 0) {
					Rasterizer.method374(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2]);
				} else if (l7 == 1) {
					Rasterizer.method376(i7, j7, k7, j3, j4, j5, anIntArray1691[anIntArray1634[i]]);
				} else if (l7 == 2) {
					int j8 = anIntArray489[i] >> 2;
					int k9 = texturedFaceIndexX[j8];
					int k10 = texturedFaceIndexY[j8];
					int k11 = texturedFaceIndexZ[j8];
					Rasterizer.method378(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2],
							anIntArray1668[k9], anIntArray1668[k10], anIntArray1668[k11], anIntArray1669[k9],
							anIntArray1669[k10], anIntArray1669[k11], anIntArray1670[k9], anIntArray1670[k10],
							anIntArray1670[k11], faceColours[i]);
				} else if (l7 == 3) {
					int k8 = anIntArray489[i] >> 2;
					int l9 = texturedFaceIndexX[k8];
					int l10 = texturedFaceIndexY[k8];
					int l11 = texturedFaceIndexZ[k8];
					Rasterizer.method378(i7, j7, k7, j3, j4, j5, anIntArray1634[i], anIntArray1634[i], anIntArray1634[i],
							anIntArray1668[l9], anIntArray1668[l10], anIntArray1668[l11], anIntArray1669[l9],
							anIntArray1669[l10], anIntArray1669[l11], anIntArray1670[l9], anIntArray1670[l10],
							anIntArray1670[l11], faceColours[i]);
				}
			}
			if (l == 4) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > Raster.anInt1385 || j4 > Raster.anInt1385 || j5 > Raster.anInt1385
						|| anIntArray1678[3] < 0 || anIntArray1678[3] > Raster.anInt1385) {
					Rasterizer.aBoolean1462 = true;
				}
				int i8;
				if (anIntArray489 == null) {
					i8 = 0;
				} else {
					i8 = anIntArray489[i] & 3;
				}
				if (i8 == 0) {
					Rasterizer.method374(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2]);
					Rasterizer.method374(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], anIntArray1680[0],
							anIntArray1680[2], anIntArray1680[3]);
					return;
				}
				if (i8 == 1) {
					int l8 = anIntArray1691[anIntArray1634[i]];
					Rasterizer.method376(i7, j7, k7, j3, j4, j5, l8);
					Rasterizer.method376(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], l8);
					return;
				}
				if (i8 == 2) {
					int i9 = anIntArray489[i] >> 2;
					int i10 = texturedFaceIndexX[i9];
					int i11 = texturedFaceIndexY[i9];
					int i12 = texturedFaceIndexZ[i9];
					Rasterizer.method378(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2],
							anIntArray1668[i10], anIntArray1668[i11], anIntArray1668[i12], anIntArray1669[i10],
							anIntArray1669[i11], anIntArray1669[i12], anIntArray1670[i10], anIntArray1670[i11],
							anIntArray1670[i12], faceColours[i]);
					Rasterizer.method378(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], anIntArray1680[0],
							anIntArray1680[2], anIntArray1680[3], anIntArray1668[i10], anIntArray1668[i11], anIntArray1668[i12],
							anIntArray1669[i10], anIntArray1669[i11], anIntArray1669[i12], anIntArray1670[i10],
							anIntArray1670[i11], anIntArray1670[i12], faceColours[i]);
					return;
				}
				if (i8 == 3) {
					int j9 = anIntArray489[i] >> 2;
					int j10 = texturedFaceIndexX[j9];
					int j11 = texturedFaceIndexY[j9];
					int j12 = texturedFaceIndexZ[j9];
					Rasterizer.method378(i7, j7, k7, j3, j4, j5, anIntArray1634[i], anIntArray1634[i], anIntArray1634[i],
							anIntArray1668[j10], anIntArray1668[j11], anIntArray1668[j12], anIntArray1669[j10],
							anIntArray1669[j11], anIntArray1669[j12], anIntArray1670[j10], anIntArray1670[j11],
							anIntArray1670[j12], faceColours[i]);
					Rasterizer.method378(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], anIntArray1634[i],
							anIntArray1634[i], anIntArray1634[i], anIntArray1668[j10], anIntArray1668[j11], anIntArray1668[j12],
							anIntArray1669[j10], anIntArray1669[j11], anIntArray1669[j12], anIntArray1670[j10],
							anIntArray1670[j11], anIntArray1670[j12], faceColours[i]);
				}
			}
		}
	}

	private void transform(int transformation, int[] groups, int dx, int dy, int dz) {
		int count = groups.length;

		if (transformation == FrameConstants.CENTROID_TRANSFORMATION) {
			int vertices = 0;
			centroidX = 0;
			centroidY = 0;
			centroidZ = 0;

			for (int index = 0; index < count; index++) {
				int group = groups[index];
				if (group < vertexGroups.length) {
					for (int vertex : vertexGroups[group]) {
						centroidX += vertexX[vertex];
						centroidY += vertexY[vertex];
						centroidZ += vertexZ[vertex];
						vertices++;
					}
				}
			}

			if (vertices > 0) {
				centroidX = centroidX / vertices + dx;
				centroidY = centroidY / vertices + dy;
				centroidZ = centroidZ / vertices + dz;
			} else {
				centroidX = dx;
				centroidY = dy;
				centroidZ = dz;
			}
		} else if (transformation == FrameConstants.POSITION_TRANSFORMATION) {
			for (int index = 0; index < count; index++) {
				int group = groups[index];

				if (group < vertexGroups.length) {
					for (int vertex : vertexGroups[group]) {
						vertexX[vertex] += dx;
						vertexY[vertex] += dy;
						vertexZ[vertex] += dz;
					}
				}
			}
		} else if (transformation == FrameConstants.ROTATION_TRANSFORMATION) {
			for (int index = 0; index < count; index++) {
				int group = groups[index];

				if (group < vertexGroups.length) {
					for (int vertex : vertexGroups[group]) {
						vertexX[vertex] -= centroidX;
						vertexY[vertex] -= centroidY;
						vertexZ[vertex] -= centroidZ;
						int pitch = (dx & 0xff) * 8;
						int roll = (dy & 0xff) * 8;
						int yaw = (dz & 0xff) * 8;

						if (yaw != 0) {
							int sin = SINE[yaw];
							int cos = COSINE[yaw];
							int x = vertexY[vertex] * sin + vertexX[vertex] * cos >> 16;
							vertexY[vertex] = vertexY[vertex] * cos - vertexX[vertex] * sin >> 16;
							vertexX[vertex] = x;
						}

						if (pitch != 0) {
							int sin = SINE[pitch];
							int cos = COSINE[pitch];
							int y = vertexY[vertex] * cos - vertexZ[vertex] * sin >> 16;
							vertexZ[vertex] = vertexY[vertex] * sin + vertexZ[vertex] * cos >> 16;
							vertexY[vertex] = y;
						}

						if (roll != 0) {
							int sin = SINE[roll];
							int cos = COSINE[roll];
							int x = vertexZ[vertex] * sin + vertexX[vertex] * cos >> 16;
							vertexZ[vertex] = vertexZ[vertex] * cos - vertexX[vertex] * sin >> 16;
							vertexX[vertex] = x;
						}
						vertexX[vertex] += centroidX;
						vertexY[vertex] += centroidY;
						vertexZ[vertex] += centroidZ;
					}
				}
			}
		} else if (transformation == FrameConstants.SCALE_TRANSFORMATION) {
			for (int index = 0; index < count; index++) {
				int group = groups[index];

				if (group < vertexGroups.length) {
					for (int vertex : vertexGroups[group]) {
						vertexX[vertex] -= centroidX;
						vertexY[vertex] -= centroidY;
						vertexZ[vertex] -= centroidZ;

						vertexX[vertex] = vertexX[vertex] * dx / 128;
						vertexY[vertex] = vertexY[vertex] * dy / 128;
						vertexZ[vertex] = vertexZ[vertex] * dz / 128;

						vertexX[vertex] += centroidX;
						vertexY[vertex] += centroidY;
						vertexZ[vertex] += centroidZ;
					}
				}
			}
		} else if (transformation == FrameConstants.ALPHA_TRANSFORMATION && faceGroups != null && faceAlphas != null) {
			for (int index = 0; index < count; index++) {
				int group = groups[index];

				if (group < faceGroups.length) {
					for (int face : faceGroups[group]) {
						faceAlphas[face] += dx * 8;

						if (faceAlphas[face] < 0) {
							faceAlphas[face] = 0;
						} else if (faceAlphas[face] > 255) {
							faceAlphas[face] = 255;
						}
					}
				}
			}
		}
	}

}