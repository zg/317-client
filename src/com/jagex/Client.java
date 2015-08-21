package com.jagex;

import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.zip.CRC32;

import org.major.cache.graphics.WidgetConstants;
import org.major.client.Actions;

import com.jagex.cache.Archive;
import com.jagex.cache.Index;
import com.jagex.cache.anim.Animation;
import com.jagex.cache.anim.Frame;
import com.jagex.cache.anim.Graphic;
import com.jagex.cache.def.ItemDefinition;
import com.jagex.cache.def.NpcDefinition;
import com.jagex.cache.def.ObjectDefinition;
import com.jagex.cache.graphics.Font;
import com.jagex.cache.graphics.IndexedImage;
import com.jagex.cache.graphics.Sprite;
import com.jagex.cache.graphics.Widget;
import com.jagex.draw.ProducingGraphicsBuffer;
import com.jagex.draw.Raster;
import com.jagex.draw.Rasterizer;
import com.jagex.entity.AnimableObject;
import com.jagex.entity.GameObject;
import com.jagex.entity.Item;
import com.jagex.entity.Mob;
import com.jagex.entity.Npc;
import com.jagex.entity.Player;
import com.jagex.entity.Projectile;
import com.jagex.entity.RenderableObject;
import com.jagex.entity.model.IdentityKit;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.Deque;
import com.jagex.map.CollisionMap;
import com.jagex.map.Floor;
import com.jagex.map.MapRegion;
import com.jagex.map.SceneGraph;
import com.jagex.map.object.GroundDecoration;
import com.jagex.map.object.SpawnedObject;
import com.jagex.map.object.Wall;
import com.jagex.map.object.WallDecoration;
import com.jagex.net.BufferedConnection;
import com.jagex.net.IsaacCipher;
import com.jagex.net.PacketConstants;
import com.jagex.net.Resource;
import com.jagex.net.ResourceProvider;
import com.jagex.setting.VariableBits;
import com.jagex.setting.VariableParameter;
import com.jagex.sign.SignLink;
import com.jagex.sound.Track;
import com.jagex.util.ChatMessageCodec;
import com.jagex.util.MessageCensor;
import com.jagex.util.MouseCapturer;
import com.jagex.util.SkillConstants;
import com.jagex.util.StringUtils;

@SuppressWarnings("serial")
public final class Client extends GameApplet {

	// client

	public static final int[] BIT_MASKS;
	public static boolean flaggedAccount;
	public static Player localPlayer;
	public static final int[][] PLAYER_BODY_RECOLOURS = {
			{ 6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 2983, 54193 },
			{ 8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003, 25239 },
			{ 25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003 },
			{ 4626, 11146, 6439, 12, 4758, 10270 }, { 4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574 } };
	public static int portOffset;
	public static final int[] SKIN_COLOURS = { 9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 58654, 5027, 1457,
			16565, 34991, 25486 };
	public static int tick;
	static boolean displayFps;
	static int drawTick;

	private static int anInt1005;
	private static int anInt1051;
	private static int anInt1097;
	private static int anInt1117;
	private static int anInt1134;
	private static int anInt1142;
	private static int anInt1155;
	private static int anInt1175;
	private static int anInt1188;
	private static int anInt1226;
	private static int anInt1288;
	private static int anInt849;
	private static int anInt854;
	private static int anInt924;
	private static int anInt940;
	private static int anInt986;
	private static boolean clientLoaded;
	private static final String IP = "127.0.0.1";
	private static boolean lowMemory;

	private static boolean membersServer = true;

	private static int node = 10;

	private static final int[] OBJECT_GROUPS = { 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3 };

	private static final BigInteger RSA_EXPONENT = new BigInteger("65537");
	private static final BigInteger RSA_MODULUS = new BigInteger(
			"143690958001225849100503496893758066948984921380482659564113596152800934352119496873386875214251264258425208995167316497331786595942754290983849878549630226741961610780416197036711585670124061149988186026407785250364328460839202438651793652051153157765358767514800252431284681765433239888090564804146588087023");

	private static final int[] SKILL_EXPERIENCE;
	private static final String VALID_INPUT_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";

	static {
		SKILL_EXPERIENCE = new int[99];
		int value = 0;
		for (int index = 0; index < 99; index++) {
			int level = index + 1;
			value += (int) (level + 300 * Math.pow(2D, level / 7D));
			SKILL_EXPERIENCE[index] = value / 4;
		}

		BIT_MASKS = new int[32];
		value = 2;
		for (int index = 0; index < 32; index++) {
			BIT_MASKS[index] = value - 1;
			value += value;
		}
	}

	public static final String getCombatLevelColour(int user, int opponent) {
		int difference = user - opponent;

		if (difference < -9) {
			return "@red@";
		} else if (difference < -6) {
			return "@or3@";
		} else if (difference < -3) {
			return "@or2@";
		} else if (difference < 0) {
			return "@or1@";
		} else if (difference > 9) {
			return "@gre@";
		} else if (difference > 6) {
			return "@gr3@";
		} else if (difference > 3) {
			return "@gr2@";
		} else if (difference > 0) {
			return "@gr1@";
		}

		return "@yel@";
	}

	public static void main(String[] args) {
		try {
			System.out.println("RS2 user client - release #" + 317);
			if (args.length != 5) {
				System.out.println("Usage: node-id, port-offset, [lowmem/highmem], [free/members], storeid");
				return;
			}

			node = Integer.parseInt(args[0]);
			portOffset = Integer.parseInt(args[1]);
			if (args[2].equals("lowmem")) {
				setLowMemory();
			} else if (args[2].equals("highmem")) {
				setHighMemory();
			} else {
				System.out.println("Usage: node-id, port-offset, [lowmem/highmem], [free/members], storeid");
				return;
			}

			if (args[3].equals("free")) {
				membersServer = false;
			} else if (args[3].equals("members")) {
				membersServer = true;
			} else {
				System.out.println("Usage: node-id, port-offset, [lowmem/highmem], [free/members], storeid");
				return;
			}

			SignLink.setStoreId(Integer.parseInt(args[4]));
			SignLink.startPriv(InetAddress.getLocalHost());
			Client client = new Client();
			client.initFrame(503, 765);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setHighMemory() {
		SceneGraph.lowMemory = false;
		Rasterizer.lowMemory = false;
		lowMemory = false;
		MapRegion.lowMemory = false;
		ObjectDefinition.lowMemory = false;
	}

	public static void setLowMemory() {
		SceneGraph.lowMemory = true;
		Rasterizer.lowMemory = true;
		lowMemory = true;
		MapRegion.lowMemory = true;
		ObjectDefinition.lowMemory = true;
	}

	private static String getFullAmountText(int amount) {
		String string = String.valueOf(amount);
		for (int index = string.length() - 3; index > 0; index -= 3) {
			string = string.substring(0, index) + "," + string.substring(index);
		}

		if (string.length() > 8) {
			string = "@gre@" + string.substring(0, string.length() - 8) + " million @whi@(" + string + ")";
		} else if (string.length() > 4) {
			string = "@cya@" + string.substring(0, string.length() - 4) + "K @whi@(" + string + ")";
		}

		return " " + string;
	}

	private static String getShortenedAmountText(int amount) {
		if (amount < 0x186a0) {
			return String.valueOf(amount);
		} else if (amount < 0x989680) {
			return amount / 1000 + "K";
		}

		return amount / 0xf4240 + "M";
	}

	public Index[] indices = new Index[5];
	public boolean loggedIn;
	public int[] settings = new int[2000];
	long aLong1220;
	int[] removedMobs = new int[1000];
	int duplicateClickCount;
	int lastMouseX;
	int lastMouseY;
	MouseCapturer mouseCapturer;
	int[] npcList = new int[16384];
	int[] playerList = new int[2048];
	String selectedItemName;
	String selectedWidgetName;
	boolean wasFocused = true;
	private boolean aBoolean1017;
	private boolean aBoolean1103;
	private boolean aBoolean1149;
	private boolean aBoolean1233;
	private boolean aBoolean1242;
	private boolean aBoolean1255;
	private volatile boolean aBoolean831;
	private boolean aBoolean848 = true;
	private volatile boolean aBoolean880;
	private volatile boolean aBoolean962;
	private boolean aBoolean972;
	private boolean[] aBooleanArray1128 = new boolean[5];;
	private boolean[] aBooleanArray876 = new boolean[5];
	private byte[] aByteArray912 = new byte[16384];
	private byte[][][] tileFlags;
	private ProducingGraphicsBuffer aClass15_1107;
	private ProducingGraphicsBuffer aClass15_1108;
	private ProducingGraphicsBuffer aClass15_1109;
	private ProducingGraphicsBuffer aClass15_1110;
	private ProducingGraphicsBuffer aClass15_1111;
	private ProducingGraphicsBuffer aClass15_1112;
	private ProducingGraphicsBuffer aClass15_1113;
	private ProducingGraphicsBuffer aClass15_1114;
	private ProducingGraphicsBuffer aClass15_1115;
	private ProducingGraphicsBuffer aClass15_1123;
	private ProducingGraphicsBuffer aClass15_1124;
	private ProducingGraphicsBuffer aClass15_1125;
	private ProducingGraphicsBuffer aClass15_1163;
	private ProducingGraphicsBuffer aClass15_1164;
	private ProducingGraphicsBuffer aClass15_1165;
	private ProducingGraphicsBuffer aClass15_1166;
	private ProducingGraphicsBuffer aClass15_908;
	private ProducingGraphicsBuffer aClass15_909;
	private ProducingGraphicsBuffer aClass15_910;
	private ProducingGraphicsBuffer aClass15_911;
	private Sprite aClass30_Sub2_Sub1_Sub1_1201;
	private Sprite aClass30_Sub2_Sub1_Sub1_1202;
	private Sprite aClass30_Sub2_Sub1_Sub1_1263;
	private Sprite aClass30_Sub2_Sub1_Sub1_931;
	private Sprite aClass30_Sub2_Sub1_Sub1_932;
	private Sprite[] aClass30_Sub2_Sub1_Sub1Array1140 = new Sprite[1000];
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1024;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1025;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1143;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1144;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1145;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1146;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_1147;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_865;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_866;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_867;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_868;
	private IndexedImage aClass30_Sub2_Sub1_Sub2_869;
	private Font aClass30_Sub2_Sub1_Sub4_1273;
	private Widget aClass9_1059 = new Widget();
	private long aLong1172;
	private long aLong953;
	private int anInt1002 = 0x23201b;
	private int anInt1010;
	private int anInt1011;
	private int anInt1014;
	private int anInt1015;
	private int anInt1016;
	private int anInt1026;
	private int anInt1039;
	private int anInt1040;
	private int anInt1041;
	private int anInt1048;
	private int anInt1063 = 0x4d4233;
	private int anInt1064;
	private int nextInventorySlot;
	private int anInt1067;
	private int anInt1071;
	private int anInt1079;

	/**
	 * The id of the Widget whose inventory was modified (i.e. an item in it was swapped) by the player.
	 */
	private int modifiedWidgetId;
	private int selectedInventorySlot;
	private int anInt1086;
	private int anInt1087;
	private int anInt1088;
	private int anInt1089;
	private int anInt1098;
	private int anInt1099;
	private int anInt1100;
	private int anInt1101;
	private int anInt1102;
	private int anInt1131;
	private int anInt1132 = 2;
	private int anInt1137;
	private int anInt1138;
	private int anInt1170;
	private int anInt1171 = 1;
	private int anInt1178 = -1;
	private int anInt1186;
	private int anInt1187;
	private int anInt1195;
	private int anInt1209;
	private int anInt1210 = 2;
	private int anInt1211 = 78;
	private int anInt1213;
	private int anInt1243;
	private int anInt1244;
	private int anInt1245;
	private int anInt1246;
	private int anInt1249;
	private int anInt1253;
	private int anInt1254;
	private int anInt1257;
	private int anInt1264;
	private int anInt1265;
	private int anInt1275;
	private int anInt1278;
	private int anInt1279 = 2;
	private int anInt1283;
	private int anInt1284;
	private int anInt1285;
	private int anInt1289 = -1;
	private int removedMobCount;
	private int anInt858;
	private int anInt859;
	private int anInt860;
	private int anInt861;
	private int anInt862;
	private int anInt874 = -1;
	private int anInt886;
	private int anInt896;
	private int anInt897 = 1;
	private int anInt902 = 0x766654;
	private int anInt913;
	private int anInt914;
	private int anInt915;
	private int anInt916;
	private int anInt917;
	private int anInt927 = 0x332d25;
	private int anInt934;
	private int anInt935;
	private int anInt936;
	private int anInt937;
	private int anInt938;
	private int anInt948;
	private int menuClickX;
	private int menuClickY;
	private int anInt951;
	private int anInt952;
	private int anInt974;
	private int anInt975 = 50;
	private int anInt984;
	private int anInt985 = -1;
	private int anInt988;
	private int anInt989;
	private int anInt992;
	private int anInt995;
	private int anInt996;
	private int anInt997;
	private int anInt998;
	private int anInt999;
	private int[] anIntArray1030 = new int[5];
	private int[] anIntArray1045 = new int[2000];
	private int[] anIntArray1052 = new int[151];
	private int[] anIntArray1057 = new int[33];
	private int[] anIntArray1072 = new int[1000];
	private int[] anIntArray1073 = new int[1000];
	private int[] anIntArray1180;
	private int[] anIntArray1181;
	private int[] anIntArray1182;
	private int[] anIntArray1190;
	private int[] anIntArray1191;
	private int[] anIntArray1203 = new int[5];
	private int[] anIntArray1229 = new int[151];
	private int[] anIntArray828;
	private int[] anIntArray829;
	private int[] anIntArray850;
	private int[] anIntArray851;
	private int[] anIntArray852;
	private int[] anIntArray853;
	private int[] anIntArray873 = new int[5];
	private int[] anIntArray928 = new int[5];
	private int[] anIntArray968 = new int[33];
	private int[] anIntArray969 = new int[256];
	private int[] anIntArray976 = new int[anInt975];
	private int[] anIntArray977 = new int[anInt975];
	private int[] anIntArray978 = new int[anInt975];
	private int[] anIntArray979 = new int[anInt975];
	private int[] anIntArray981 = new int[anInt975];
	private int[] anIntArray982 = new int[anInt975];
	private int[][] anIntArrayArray901 = new int[104][104];
	private int[][] anIntArrayArray929 = new int[104][104];
	private int[][][] tileHeights;
	private int[] archiveCRCs = new int[9];;
	private String aString1004 = "";
	private String aString1121 = "";
	private String aString1212 = "";
	private String[] aStringArray1127 = new String[5];;
	private String[] aStringArray983 = new String[anInt975];
	private boolean avatarChanged;;
	private IndexedImage backBase1;
	private IndexedImage backBase2;
	private int backDialogueId = -1;
	private IndexedImage backHmid1;
	private ProducingGraphicsBuffer backLeft1Buffer;
	private ProducingGraphicsBuffer backLeft2Buffer;
	private ProducingGraphicsBuffer backRight1Buffer;
	private ProducingGraphicsBuffer backRight2Buffer;
	private ProducingGraphicsBuffer backTopBuffer;
	private Font bold;
	private int cameraRoll = 128;
	private int cameraYaw;
	private int[] characterDesignColours = new int[5];
	private int[] characterDesignStyles = new int[7];
	private IndexedImage chatBackground;
	private Buffer chatBuffer = new Buffer(new byte[5000]);
	private String[] chatMessages = new String[100];
	private String[] chatPlayerNames = new String[100];
	private int[] chatTypes = new int[100];
	private String clickToContinueString;
	private CollisionMap[] collisionMaps = new CollisionMap[4];
	private Sprite compass;
	private boolean constructedViewport;
	private Sprite[] crosses = new Sprite[8];
	private int daysSinceLogin;
	private int daysSinceRecoveryChange;
	private IsaacCipher decryption;
	private int destinationX;
	private int destinationY;
	private int dialogueId = -1;
	private int[][] distances = new int[104][104];
	private boolean error;
	private boolean fadeMusic = true;
	private String firstLoginMessage = "";
	private Sprite firstMapmarker;
	private int[] firstMenuOperand = new int[500];
	private int flameTick;
	private int flashingSidebarId = -1;
	private Font frameFont;
	private int friendCount;
	private Sprite friendMapdot;
	private long[] friends = new long[200];
	private int friendServerStatus;
	private String[] friendUsernames = new String[200];
	private int[] friendWorlds = new int[200];
	private boolean gameAlreadyLoaded;
	private Deque[][][] groundItems = new Deque[4][104][104];
	private int hasMembersCredit;
	private Sprite[] headIcons = new Sprite[20];;
	private int hintedNpc;
	private int hintIconDrawType;
	private Sprite[] hitMarks = new Sprite[20];
	private int ignoredCount;
	private long[] ignores = new long[100];
	private Buffer incoming = Buffer.create();
	private Deque incompleteAnimables = new Deque();
	private CRC32 indexCrc = new CRC32();
	private boolean inPlayerOwnedHouse;
	private String input = "";
	private int inputDialogueState;
	private int internalLocalPlayerIndex = 2047;
	private IndexedImage inventoryBackground;
	private int[] inventoryTabIds = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
	private Sprite itemMapdot;
	private Socket jaggrab;
	private int lastInteractedWithPlayer;
	private int lastLoginIP;
	private int lastOpcode;
	private String loadingScreenText;
	private int loadingStage;
	private long loadingStartTime;
	private int localPlayerIndex = -1;
	private int[] localRegionIds;
	private byte[][] localRegionLandscapeData;
	private int[] localRegionLandscapeIds;
	private byte[][] localRegionMapData;
	private int[] localRegionMapIds;
	private int[][][] localRegions = new int[4][13][13];
	private int localX; // the x coordinate relative to the current region
	private int localY; // the y coordinate relative to the current region
	private Buffer login = Buffer.create();
	private int loginFailures;
	private int loginInputLine;
	private int loginScreenStage;
	private boolean maleAvatar = true;
	private IndexedImage mapBackground;
	private Sprite mapEdge;
	private Sprite[] mapFunctions = new Sprite[100];
	private IndexedImage[] mapScenes = new IndexedImage[100];
	private int maximumPlayers = 2048;
	private int member;
	private int menuActionRow;
	private String[] menuActionTexts = new String[500];
	private int[] menuActionTypes = new int[500];
	private boolean menuOpen;
	private boolean messagePromptRaised;
	private int minimapState;
	private int[] mobsAwaitingUpdate = new int[maximumPlayers];
	private int mobsAwaitingUpdateCount;
	private IndexedImage[] modIcons = new IndexedImage[2];
	private int multicombat;
	private int musicId;
	private int nextMusicId = -1;
	private int npcCount;
	private Sprite npcMapdot;
	private Npc[] npcs = new Npc[16384];
	private int onTutorialIsland;
	private int opcode;
	private int openInterfaceId = -1;
	private int openWalkableInterface = -1;
	private boolean oriented;
	private Buffer outgoing = Buffer.create();
	private int overlayInterfaceId = -1;
	private int packetSize;
	private String password = "testing";
	private int plane;
	private int playerCount;
	private Sprite playerMapdot;
	private int playerPrivelage;
	private Player[] players = new Player[maximumPlayers];
	private Buffer[] playerSynchronizationBuffers = new Buffer[maximumPlayers];
	private boolean playingMusic = true;
	private int previousAbsoluteX;
	private int previousAbsoluteY;
	private BufferedConnection primary;
	private int privateChatMode;
	private int privateMessageCount;
	private int[] privateMessageIds = new int[100];
	private Deque projectiles = new Deque();
	private ResourceProvider provider;
	private int publicChatMode;
	private boolean redrawDialogueBox;
	private boolean redrawTabArea;
	private int regionBaseX;
	private int regionBaseY;
	private int regionX;
	private int regionY;
	private boolean reportAbuseMuteToggle;
	private String reportInput = "";
	private int runEnergy;
	private IndexedImage[] runes;
	private SceneGraph scene;
	private int secondLastOpcode;
	private String secondLoginMessage = "";
	private Sprite secondMapmarker;
	private int[] secondMenuOperand = new int[500];
	private int itemSelected;
	private int[] selectedMenuActions = new int[500];
	private int widgetSelected;
	private long serverSeed;
	private IndexedImage[] sideIcons = new IndexedImage[13];
	private int[] currentLevels = new int[SkillConstants.SKILL_COUNT];
	private int[] experiences = new int[SkillConstants.SKILL_COUNT];
	private int[] maximumLevels = new int[SkillConstants.SKILL_COUNT];
	private Font smallFont;
	private int songDelay;
	private Deque spawns = new Deque();
	private int spriteDrawX = -1;
	private int spriteDrawY = -1;
	private int systemUpdateTime;
	private int tabId = -1;
	private Sprite teamMapdot;
	private int[] textColourEffect = new int[anInt975];
	private int[] textColours = { 0xffff00, 0xff0000, 65280, 65535, 0xff00ff, 0xffffff };
	private int thirdLastOpcode;
	private int tickDelta;
	private int timeoutCounter;
	private IndexedImage titleBox;
	private IndexedImage titleButton;
	private Archive titleScreen;
	private int trackCount;
	private int[] trackDelays = new int[50];
	private int[] trackLoops = new int[50];
	private int[] tracks = new int[50];
	private int tradeChatMode;
	private boolean unableToLoad;
	private int unreadMessageCount;
	private boolean useJaggrab;
	private String username = "Major";
	private boolean validLocalMap;
	private int[] waypointX = new int[4000];
	private int[] waypointY = new int[4000];

	private int weight;

	public final void addChatMessage(int type, String message, String name) {
		if (type == 0 && dialogueId != -1) {
			clickToContinueString = message;
			super.lastMetaModifier = 0;
		}

		if (backDialogueId == -1) {
			redrawDialogueBox = true;
		}

		for (int index = 99; index > 0; index--) {
			chatTypes[index] = chatTypes[index - 1];
			chatPlayerNames[index] = chatPlayerNames[index - 1];
			chatMessages[index] = chatMessages[index - 1];
		}

		chatTypes[0] = type;
		chatPlayerNames[0] = name;
		chatMessages[0] = message;
	}

	public final void addFriend(long name) {
		if (name == 0) {
			return;
		}

		if (friendCount >= 100 && member != 1) {
			addChatMessage(0, "Your friendlist is full. Max of 100 for free users, and 200 for members", "");
			return;
		} else if (friendCount >= 200) {
			addChatMessage(0, "Your friendlist is full. Max of 100 for free users, and 200 for members", "");
			return;
		}

		String username = StringUtils.format(StringUtils.decodeBase37(name));
		for (int index = 0; index < friendCount; index++) {
			if (friends[index] == name) {
				addChatMessage(0, username + " is already on your friend list", "");
				return;
			}
		}

		for (int index = 0; index < ignoredCount; index++) {
			if (ignores[index] == name) {
				addChatMessage(0, "Please remove " + username + " from your ignore list first", "");
				return;
			}
		}

		if (username.equals(localPlayer.name)) {
			return;
		}

		friendUsernames[friendCount] = username;
		friends[friendCount] = name;
		friendWorlds[friendCount] = 0;
		friendCount++;
		redrawTabArea = true;
		outgoing.writeOpcode(188);
		outgoing.writeLong(name);
	}

	public final void addIgnore(long name) {
		if (name == 0L) {
			return;
		}

		if (ignoredCount >= 100) {
			addChatMessage(0, "Your ignore list is full. Max of 100 hit", "");
			return;
		}

		String username = StringUtils.format(StringUtils.decodeBase37(name));
		for (int index = 0; index < ignoredCount; index++) {
			if (ignores[index] == name) {
				addChatMessage(0, username + " is already on your ignore list", "");
				return;
			}
		}

		for (int index = 0; index < friendCount; index++) {
			if (friends[index] == name) {
				addChatMessage(0, "Please remove " + username + " from your friend list first", "");
				return;
			}
		}

		ignores[ignoredCount++] = name;
		redrawTabArea = true;
		outgoing.writeOpcode(133);
		outgoing.writeLong(name);
	}

	public final void adjustMidiVolume(boolean flag, int volume) {
		SignLink.setMidiVolume(volume);

		if (flag) {
			SignLink.setMidi("voladjust");
		}
	}

	public final void attemptReconnection() {
		if (anInt1011 > 0) {
			reset();
			return;
		}

		aClass15_1165.initializeRasterizer();
		frameFont.renderCentre(257, 144, "Connection lost", 0);
		frameFont.renderCentre(256, 143, "Connection lost", 0xffffff);
		frameFont.renderCentre(257, 159, "Please wait - attempting to reestablish", 0);
		frameFont.renderCentre(256, 158, "Please wait - attempting to reestablish", 0xffffff);
		aClass15_1165.drawImage(super.graphics, 4, 4);
		minimapState = 0;
		destinationX = 0;
		BufferedConnection old = primary;
		loggedIn = false;
		loginFailures = 0;
		login(username, password, true);

		if (!loggedIn) {
			reset();
		}

		old.stop();
	}

	public final void changeCharacterGender() {
		avatarChanged = true;
		for (int part = 0; part < 7; part++) {
			characterDesignStyles[part] = -1;

			for (int kit = 0; kit < IdentityKit.count; kit++) {
				if (IdentityKit.kits[kit].isValidStyle() || IdentityKit.kits[kit].getPart() != part + (maleAvatar ? 0 : 7)) {
					continue;
				}

				characterDesignStyles[part] = kit;
				break;
			}
		}
	}

	public final void checkTutorialIsland() {
		onTutorialIsland = 0;
		int x = (localPlayer.worldX >> 7) + regionBaseX;
		int y = (localPlayer.worldY >> 7) + regionBaseY;
		if (x >= 3053 && x <= 3156 && y >= 3056 && y <= 3136) {
			onTutorialIsland = 1;
		}
		if (x >= 3072 && x <= 3118 && y >= 9492 && y <= 9535) {
			onTutorialIsland = 1;
		}
		if (onTutorialIsland == 1 && x >= 3139 && x <= 3199 && y >= 3008 && y <= 3062) {
			onTutorialIsland = 0;
		}
	}

	public final void clearTopInterfaces() {
		outgoing.writeOpcode(130);

		if (overlayInterfaceId != -1) {
			overlayInterfaceId = -1;
			redrawTabArea = true;
			aBoolean1149 = false;
			aBoolean1103 = true;
		}

		if (backDialogueId != -1) {
			backDialogueId = -1;
			redrawDialogueBox = true;
			aBoolean1149 = false;
		}

		openInterfaceId = -1;
	}

	public final Archive createArchive(int file, String displayedName, String name, int expectedCRC, int x) {
		byte[] archiveBuffer = null;
		int reconnectionDelay = 5;

		try {
			if (indices[0] != null) {
				archiveBuffer = indices[0].decompress(file);
			}
		} catch (Exception ex) {
		}

		if (archiveBuffer != null) {
			indexCrc.reset();
			indexCrc.update(archiveBuffer);
			int crc = (int) indexCrc.getValue();
			if (crc != expectedCRC) {
				archiveBuffer = null;
			}
		}

		if (archiveBuffer != null) {
			return new Archive(archiveBuffer);
		}

		int errors = 0;
		while (archiveBuffer == null) {
			String message = "Unknown error";
			drawLoadingText(x, "Requesting " + displayedName);
			try (DataInputStream in = requestCacheIndex(name + expectedCRC)) {
				int last = 0;
				byte[] buf = new byte[6];
				in.readFully(buf, 0, 6);
				Buffer buffer = new Buffer(buf);
				buffer.setPosition(3);
				int size = buffer.readTriByte() + 6;
				int offset = 6;
				archiveBuffer = new byte[size];

				for (int i = 0; i < 6; i++) {
					archiveBuffer[i] = buf[i];
				}

				while (offset < size) {
					int length = size - offset;
					if (length > 1000) {
						length = 1000;
					}
					int read = in.read(archiveBuffer, offset, length);
					if (read < 0) {
						message = "Length error: " + offset + "/" + size;
						throw new IOException("EOF");
					}
					offset += read;
					int completed = offset * 100 / size;
					if (completed != last) {
						drawLoadingText(x, "Loading " + displayedName + " - " + completed + "%");
					}
					last = completed;
				}
				in.close();
				try {
					if (indices[0] != null) {
						indices[0].put(archiveBuffer, file, archiveBuffer.length);
					}
				} catch (Exception _ex) {
					indices[0] = null;
				}
				indexCrc.reset();
				indexCrc.update(archiveBuffer);
				int crc = (int) indexCrc.getValue();
				if (crc != expectedCRC) {
					archiveBuffer = null;
					errors++;
					message = "Checksum error: " + crc;
				}
			} catch (IOException ex) {
				if (message.equals("Unknown error")) {
					message = "Connection error";
				}
				archiveBuffer = null;
			} catch (NullPointerException ex) {
				message = "Null error";
				archiveBuffer = null;
				if (!SignLink.isReportError()) {
					return null;
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				message = "Bounds error";
				archiveBuffer = null;
				if (!SignLink.isReportError()) {
					return null;
				}
			} catch (Exception ex) {
				message = "Unexpected error";
				archiveBuffer = null;
				if (!SignLink.isReportError()) {
					return null;
				}
			}

			if (archiveBuffer == null) {
				for (int seconds = reconnectionDelay; seconds > 0; seconds--) {
					if (errors >= 3) {
						drawLoadingText(x, "Game updated - please reload page");
						seconds = 10;
					} else {
						drawLoadingText(x, message + " - Retrying in " + seconds);
					}
					try {
						Thread.sleep(1000L);
					} catch (Exception ex) {
					}
				}

				reconnectionDelay *= 2;
				if (reconnectionDelay > 60) {
					reconnectionDelay = 60;
				}

				useJaggrab = !useJaggrab;
			}
		}

		return new Archive(archiveBuffer);
	}

	public final void createMenu() {
		if (itemSelected == 0 && widgetSelected == 0) {
			menuActionTexts[menuActionRow] = "Walk here";
			menuActionTypes[menuActionRow] = 516;
			firstMenuOperand[menuActionRow] = super.mouseEventX;
			secondMenuOperand[menuActionRow] = super.mouseEventY;
			menuActionRow++;
		}
		int previous = -1;
		for (int k = 0; k < Model.anInt1687; k++) {
			int config = Model.anIntArray1688[k];
			int x = config & 0x7f;
			int y = config >> 7 & 0x7f;
			int type = config >> 29 & 3;
			int id = config >> 14 & 0x7fff;
			if (config == previous) {
				continue;
			}
			previous = config;
			if (type == 2 && scene.getConfig(x, y, plane, config) >= 0) {
				ObjectDefinition definition = ObjectDefinition.lookup(id);
				if (definition.getMorphisms() != null) {
					definition = definition.morph();
				}

				if (definition == null) {
					continue;
				}

				if (itemSelected == 1) {
					menuActionTexts[menuActionRow] = "Use " + selectedItemName + " with @cya@" + definition.getName();
					menuActionTypes[menuActionRow] = 62;
					selectedMenuActions[menuActionRow] = config;
					firstMenuOperand[menuActionRow] = x;
					secondMenuOperand[menuActionRow] = y;
					menuActionRow++;
				} else if (widgetSelected == 1) {
					if ((anInt1138 & 4) == 4) {
						menuActionTexts[menuActionRow] = selectedWidgetName + " @cya@" + definition.getName();
						menuActionTypes[menuActionRow] = 956;
						selectedMenuActions[menuActionRow] = config;
						firstMenuOperand[menuActionRow] = x;
						secondMenuOperand[menuActionRow] = y;
						menuActionRow++;
					}
				} else {
					if (definition.getInteractions() != null) {
						for (int action = 4; action >= 0; action--) {
							if (definition.getInteraction(action) != null) {
								menuActionTexts[menuActionRow] = definition.getInteraction(action) + " @cya@"
										+ definition.getName();

								if (action == 0) {
									menuActionTypes[menuActionRow] = 502;
								} else if (action == 1) {
									menuActionTypes[menuActionRow] = 900;
								} else if (action == 2) {
									menuActionTypes[menuActionRow] = 113;
								} else if (action == 3) {
									menuActionTypes[menuActionRow] = 872;
								} else if (action == 4) {
									menuActionTypes[menuActionRow] = 1062;
								}

								selectedMenuActions[menuActionRow] = config;
								firstMenuOperand[menuActionRow] = x;
								secondMenuOperand[menuActionRow] = y;
								menuActionRow++;
							}
						}
					}
					menuActionTexts[menuActionRow] = "Examine @cya@" + definition.getName();
					menuActionTypes[menuActionRow] = 1226;
					selectedMenuActions[menuActionRow] = definition.getId() << 14;
					firstMenuOperand[menuActionRow] = x;
					secondMenuOperand[menuActionRow] = y;
					menuActionRow++;
				}
			}
			if (type == 1) {
				Npc npc = npcs[id];
				if (npc.getDefinition().getSize() == 1 && (npc.worldX & 0x7f) == 64 && (npc.worldY & 0x7f) == 64) {
					for (int j2 = 0; j2 < npcCount; j2++) {
						Npc localNpc = npcs[npcList[j2]];
						if (localNpc != null && localNpc != npc && localNpc.getDefinition().getSize() == 1
								&& ((Mob) localNpc).worldX == npc.worldX && ((Mob) localNpc).worldY == npc.worldY) {
							createNpcMenu(localNpc.getDefinition(), x, y, npcList[j2]);
						}
					}

					for (int l2 = 0; l2 < playerCount; l2++) {
						Player player = players[playerList[l2]];
						if (player != null && player.worldX == npc.worldX && player.worldY == npc.worldY) {
							createPlayerMenu(x, playerList[l2], player, y);
						}
					}

				}
				createNpcMenu(npc.getDefinition(), x, y, id);
			}
			if (type == 0) {
				Player player = players[id];
				if ((player.worldX & 0x7f) == 64 && (player.worldY & 0x7f) == 64) {
					for (int k2 = 0; k2 < npcCount; k2++) {
						Npc npc = npcs[npcList[k2]];
						if (npc != null && npc.getDefinition().getSize() == 1 && npc.worldX == player.worldX
								&& npc.worldY == player.worldY) {
							createNpcMenu(npc.getDefinition(), x, y, npcList[k2]);
						}
					}

					for (int i3 = 0; i3 < playerCount; i3++) {
						Player localPlayer = players[playerList[i3]];
						if (localPlayer != null && localPlayer != player && localPlayer.worldX == player.worldX
								&& localPlayer.worldY == player.worldY) {
							createPlayerMenu(x, playerList[i3], localPlayer, y);
						}
					}

				}
				createPlayerMenu(x, id, player, y);
			}
			if (type == 3) {
				Deque items = groundItems[plane][x][y];
				if (items != null) {
					for (Item item = (Item) items.getTail(); item != null; item = (Item) items.getPrevious()) {
						ItemDefinition definition = ItemDefinition.lookup(item.getId());

						if (itemSelected == 1) {
							menuActionTexts[menuActionRow] = "Use " + selectedItemName + " with @lre@" + definition.getName();
							menuActionTypes[menuActionRow] = 511;
							selectedMenuActions[menuActionRow] = item.getId();
							firstMenuOperand[menuActionRow] = x;
							secondMenuOperand[menuActionRow] = y;
							menuActionRow++;
						} else if (widgetSelected == 1) {
							if ((anInt1138 & 1) == 1) {
								menuActionTexts[menuActionRow] = selectedWidgetName + " @lre@" + definition.getName();
								menuActionTypes[menuActionRow] = 94;
								selectedMenuActions[menuActionRow] = item.getId();
								firstMenuOperand[menuActionRow] = x;
								secondMenuOperand[menuActionRow] = y;
								menuActionRow++;
							}
						} else {
							for (int selected = 4; selected >= 0; selected--) {
								if (definition.getGroundMenuActions() != null
										&& definition.getGroundMenuActions()[selected] != null) {
									menuActionTexts[menuActionRow] = definition.getGroundMenuActions()[selected] + " @lre@"
											+ definition.getName();
									if (selected == 0) {
										menuActionTypes[menuActionRow] = 652;
									} else if (selected == 1) {
										menuActionTypes[menuActionRow] = 567;
									} else if (selected == 2) {
										menuActionTypes[menuActionRow] = 234;
									} else if (selected == 3) {
										menuActionTypes[menuActionRow] = 244;
									} else if (selected == 4) {
										menuActionTypes[menuActionRow] = 213;
									}

									selectedMenuActions[menuActionRow] = item.getId();
									firstMenuOperand[menuActionRow] = x;
									secondMenuOperand[menuActionRow] = y;
									menuActionRow++;
								} else if (selected == 2) {
									menuActionTexts[menuActionRow] = "Take @lre@" + definition.getName();
									menuActionTypes[menuActionRow] = 234;
									selectedMenuActions[menuActionRow] = item.getId();
									firstMenuOperand[menuActionRow] = x;
									secondMenuOperand[menuActionRow] = y;
									menuActionRow++;
								}
							}

							menuActionTexts[menuActionRow] = "Examine @lre@" + definition.getName();
							menuActionTypes[menuActionRow] = 1448;
							selectedMenuActions[menuActionRow] = item.getId();
							firstMenuOperand[menuActionRow] = x;
							secondMenuOperand[menuActionRow] = y;
							menuActionRow++;
						}
					}
				}
			}
		}
	}

	public final void createNpcMenu(NpcDefinition definition, int x, int y, int index) {
		if (menuActionRow >= 400) {
			return;
		} else if (definition.getMorphisms() != null) {
			definition = definition.morph();
		}

		if (definition == null) {
			return;
		} else if (!definition.isClickable()) {
			return;
		}

		String text = definition.getName();
		if (definition.getCombat() != 0) {
			text = text + getCombatLevelColour(localPlayer.combat, definition.getCombat()) + " (level-" + definition.getCombat()
					+ ")";
		}

		if (itemSelected == 1) {
			menuActionTexts[menuActionRow] = "Use " + selectedItemName + " with @yel@" + text;
			menuActionTypes[menuActionRow] = 582;
			selectedMenuActions[menuActionRow] = index;
			firstMenuOperand[menuActionRow] = x;
			secondMenuOperand[menuActionRow] = y;
			menuActionRow++;
			return;
		}

		if (widgetSelected == 1) {
			if ((anInt1138 & 2) == 2) {
				menuActionTexts[menuActionRow] = selectedWidgetName + " @yel@" + text;
				menuActionTypes[menuActionRow] = 413;
				selectedMenuActions[menuActionRow] = index;
				firstMenuOperand[menuActionRow] = x;
				secondMenuOperand[menuActionRow] = y;
				menuActionRow++;
				return;
			}
		} else {
			if (definition.getInteractions() != null) {
				for (int slot = 4; slot >= 0; slot--) {
					if (definition.getInteraction(slot) != null && !definition.getInteraction(slot).equalsIgnoreCase("attack")) {
						menuActionTexts[menuActionRow] = definition.getInteraction(slot) + " @yel@" + text;

						if (slot == 0) {
							menuActionTypes[menuActionRow] = 20;
						} else if (slot == 1) {
							menuActionTypes[menuActionRow] = 412;
						} else if (slot == 2) {
							menuActionTypes[menuActionRow] = 225;
						} else if (slot == 3) {
							menuActionTypes[menuActionRow] = 965;
						} else if (slot == 4) {
							menuActionTypes[menuActionRow] = 478;
						}

						selectedMenuActions[menuActionRow] = index;
						firstMenuOperand[menuActionRow] = x;
						secondMenuOperand[menuActionRow] = y;
						menuActionRow++;
					}
				}
			}

			if (definition.getInteractions() != null) {
				for (int slot = 4; slot >= 0; slot--) {
					if (definition.getInteraction(slot) != null && definition.getInteraction(slot).equalsIgnoreCase("attack")) {
						int offset = (definition.getCombat() > localPlayer.combat) ? 2000 : 0;

						menuActionTexts[menuActionRow] = definition.getInteraction(slot) + " @yel@" + text;

						if (slot == 0) {
							menuActionTypes[menuActionRow] = 20 + offset;
						} else if (slot == 1) {
							menuActionTypes[menuActionRow] = 412 + offset;
						} else if (slot == 2) {
							menuActionTypes[menuActionRow] = 225 + offset;
						} else if (slot == 3) {
							menuActionTypes[menuActionRow] = 965 + offset;
						} else if (slot == 4) {
							menuActionTypes[menuActionRow] = 478 + offset;
						}

						selectedMenuActions[menuActionRow] = index;
						firstMenuOperand[menuActionRow] = x;
						secondMenuOperand[menuActionRow] = y;
						menuActionRow++;
					}
				}
			}

			menuActionTexts[menuActionRow] = "Examine @yel@" + text;
			menuActionTypes[menuActionRow] = 1025;
			selectedMenuActions[menuActionRow] = index;
			firstMenuOperand[menuActionRow] = x;
			secondMenuOperand[menuActionRow] = y;
			menuActionRow++;
		}
	}

	public final void createPlayerMenu(int i, int j, Player player, int k) {
		if (player == localPlayer) {
			return;
		}

		if (menuActionRow >= 400) {
			return;
		}

		String text;
		if (player.skill == 0) {
			text = player.name + getCombatLevelColour(localPlayer.combat, player.combat) + " (level-" + player.combat + ")";
		} else {
			text = player.name + " (skill-" + player.skill + ")";
		}

		if (itemSelected == 1) {
			menuActionTexts[menuActionRow] = "Use " + selectedItemName + " with @whi@" + text;
			menuActionTypes[menuActionRow] = 491;
			selectedMenuActions[menuActionRow] = j;
			firstMenuOperand[menuActionRow] = i;
			secondMenuOperand[menuActionRow] = k;
			menuActionRow++;
		} else if (widgetSelected == 1) {
			if ((anInt1138 & 8) == 8) {
				menuActionTexts[menuActionRow] = selectedWidgetName + " @whi@" + text;
				menuActionTypes[menuActionRow] = 365;
				selectedMenuActions[menuActionRow] = j;
				firstMenuOperand[menuActionRow] = i;
				secondMenuOperand[menuActionRow] = k;
				menuActionRow++;
			}
		} else {
			for (int index = 4; index >= 0; index--) {
				if (aStringArray1127[index] != null) {
					menuActionTexts[menuActionRow] = aStringArray1127[index] + " @whi@" + text;
					int offset = 0;
					if (aStringArray1127[index].equalsIgnoreCase("attack")) {
						if (player.combat > localPlayer.combat) {
							offset = 2000;
						}

						if (localPlayer.team != 0 && player.team != 0) {
							if (localPlayer.team == player.team) {
								offset = 2000;
							} else {
								offset = 0;
							}
						}
					} else if (aBooleanArray1128[index]) {
						offset = 2000;
					}

					if (index == 0) {
						menuActionTypes[menuActionRow] = 561 + offset;
					} else if (index == 1) {
						menuActionTypes[menuActionRow] = 779 + offset;
					} else if (index == 2) {
						menuActionTypes[menuActionRow] = 27 + offset;
					} else if (index == 3) {
						menuActionTypes[menuActionRow] = 577 + offset;
					} else if (index == 4) {
						menuActionTypes[menuActionRow] = 729 + offset;
					}

					selectedMenuActions[menuActionRow] = j;
					firstMenuOperand[menuActionRow] = i;
					secondMenuOperand[menuActionRow] = k;
					menuActionRow++;
				}
			}
		}

		for (int index = 0; index < menuActionRow; index++) {
			if (menuActionTypes[index] == 516) {
				menuActionTexts[index] = "Walk here @whi@" + text;
				return;
			}
		}
	}

	public void debug() {
		System.out.println("============");
		System.out.println("flame-cycle:" + flameTick);
		if (provider != null) {
			System.out.println("Od-cycle:" + provider.getTick());
		}

		System.out.println("loop-cycle:" + tick);
		System.out.println("draw-cycle:" + drawTick);
		System.out.println("ptype:" + opcode);
		System.out.println("psize:" + packetSize);

		if (primary != null) {
			primary.debug();
		}

		super.debug = true;
	}

	public final void displayErrorMessage() {
		Graphics graphics = getFrame().getGraphics();
		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, 765, 503);
		resetTimeDelta();

		if (error) {
			aBoolean831 = false;
			graphics.setFont(new java.awt.Font("Helvetica", 1, 16));
			graphics.setColor(Color.yellow);
			int y = 35;
			graphics.drawString("Sorry, an error has occured whilst loading RuneScape", 30, y);
			y += 50;
			graphics.setColor(Color.white);
			graphics.drawString("To fix this try the following (in order):", 30, y);
			y += 50;
			graphics.setColor(Color.white);
			graphics.setFont(new java.awt.Font("Helvetica", 1, 12));
			graphics.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, y);
			y += 30;
			graphics.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, y);
			y += 30;
			graphics.drawString("3: Try using a different game-world", 30, y);
			y += 30;
			graphics.drawString("4: Try rebooting your computer", 30, y);
			y += 30;
			graphics.drawString("5: Try selecting a different version of Java from the play-game menu", 30, y);
		}

		if (unableToLoad) {
			aBoolean831 = false;
			graphics.setFont(new java.awt.Font("Helvetica", 1, 20));
			graphics.setColor(Color.white);
			graphics.drawString("Error - unable to load game!", 50, 50);
			graphics.drawString("To play RuneScape make sure you play from", 50, 100);
			graphics.drawString("http://www.runescape.com", 50, 150);
		}

		if (gameAlreadyLoaded) {
			aBoolean831 = false;
			graphics.setColor(Color.yellow);
			int y = 35;
			graphics.drawString("Error a copy of RuneScape already appears to be loaded", 30, y);
			y += 50;
			graphics.setColor(Color.white);
			graphics.drawString("To fix this try the following (in order):", 30, y);
			y += 50;
			graphics.setColor(Color.white);
			graphics.setFont(new java.awt.Font("Helvetica", 1, 12));
			graphics.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, y);
			y += 30;
			graphics.drawString("2: Try rebooting your computer, and reloading", 30, y);
			y += 30;
		}
	}

	public final boolean displayMessageFrom(String name) {
		if (name == null) {
			return false;
		}

		for (int index = 0; index < friendCount; index++) {
			if (name.equalsIgnoreCase(friendUsernames[index])) {
				return true;
			}
		}

		return name.equalsIgnoreCase(localPlayer.name);
	}

	@Override
	public final void draw() {
		if (gameAlreadyLoaded || error || unableToLoad) {
			displayErrorMessage();
			return;
		}
		drawTick++;

		if (!loggedIn) {
			drawLoginScreen(false);
		} else {
			drawGameScreen();
		}
		anInt1213 = 0;
	}

	public final void drawChatMessages() {
		if (anInt1195 == 0) {
			return;
		}
		Font font = frameFont;
		int message = 0;
		if (systemUpdateTime != 0) {
			message = 1;
		}

		for (int index = 0; index < 100; index++) {
			if (chatMessages[index] != null) {
				int type = chatTypes[index];
				String name = chatPlayerNames[index];
				byte privilege = 0;

				if (name != null && name.startsWith("@cr1@")) {
					name = name.substring(5);
					privilege = 1;
				} else if (name != null && name.startsWith("@cr2@")) {
					name = name.substring(5);
					privilege = 2;
				}

				if ((type == 3 || type == 7)
						&& (type == 7 || privateChatMode == 0 || privateChatMode == 1 && displayMessageFrom(name))) {
					int y = 329 - message * 13;
					int x = 4;
					font.render(x, y, "From", 0);
					font.render(x, y - 1, "From", 65535);
					x += font.getColouredTextWidth("From ");

					if (privilege == 1) {
						modIcons[0].draw(x, y - 12);
						x += 14;
					} else if (privilege == 2) {
						modIcons[1].draw(x, y - 12);
						x += 14;
					}

					font.render(x, y, name + ": " + chatMessages[index], 0);
					font.render(x, y - 1, name + ": " + chatMessages[index], 65535);
					if (++message >= 5) {
						return;
					}
				}

				if (type == 5 && privateChatMode < 2) {
					int y = 329 - message * 13;
					font.render(4, y, chatMessages[index], 0);
					font.render(4, y - 1, chatMessages[index], 65535);
					if (++message >= 5) {
						return;
					}
				} else if (type == 6 && privateChatMode < 2) {
					int y = 329 - message * 13;
					font.render(4, y, "To " + name + ": " + chatMessages[index], 0);
					font.render(4, y - 1, "To " + name + ": " + chatMessages[index], 65535);
					if (++message >= 5) {
						return;
					}
				}
			}
		}
	}

	public final void drawDialogueBox() {
		aClass15_1166.initializeRasterizer();
		Rasterizer.scanOffsets = anIntArray1180;
		chatBackground.draw(0, 0);

		if (messagePromptRaised) {
			bold.renderCentre(239, 40, aString1121, 0);
			bold.renderCentre(239, 60, aString1212 + "*", 128);
		} else if (inputDialogueState == 1) {
			bold.renderCentre(239, 40, "Enter amount:", 0);
			bold.renderCentre(239, 60, aString1004 + "*", 128);
		} else if (inputDialogueState == 2) {
			bold.renderCentre(239, 40, "Enter name:", 0);
			bold.renderCentre(239, 60, aString1004 + "*", 128);
		} else if (clickToContinueString != null) {
			bold.renderCentre(239, 40, clickToContinueString, 0);
			bold.renderCentre(239, 60, "Click to continue", 128);
		} else if (backDialogueId != -1) {
			drawWidget(Widget.widgets[backDialogueId], 0, 0, 0);
		} else if (dialogueId != -1) {
			drawWidget(Widget.widgets[dialogueId], 0, 0, 0);
		} else {
			Font typeface = frameFont;
			int count = 0;
			Raster.setBounds(77, 0, 463, 0);

			for (int message = 0; message < 100; message++) {
				if (chatMessages[message] != null) {
					int type = chatTypes[message];
					int y = 70 - count * 14 + anInt1089;
					String username = chatPlayerNames[message];
					byte privilege = 0;
					if (username != null && username.startsWith("@cr1@")) {
						username = username.substring(5);
						privilege = 1;
					}
					if (username != null && username.startsWith("@cr2@")) {
						username = username.substring(5);
						privilege = 2;
					}
					if (type == 0) {
						if (y > 0 && y < 110) {
							typeface.render(4, y, chatMessages[message], 0);
						}
						count++;
					}
					if ((type == 1 || type == 2)
							&& (type == 1 || publicChatMode == 0 || publicChatMode == 1 && displayMessageFrom(username))) {
						if (y > 0 && y < 110) {
							int x = 4;
							if (privilege == 1) {
								modIcons[0].draw(x, y - 12);
								x += 14;
							}
							if (privilege == 2) {
								modIcons[1].draw(x, y - 12);
								x += 14;
							}
							typeface.render(x, y, username + ":", 0);
							x += typeface.getColouredTextWidth(username) + 8;
							typeface.render(x, y, chatMessages[message], 255);
						}
						count++;
					}
					if ((type == 3 || type == 7) && anInt1195 == 0
							&& (type == 7 || privateChatMode == 0 || privateChatMode == 1 && displayMessageFrom(username))) {
						if (y > 0 && y < 110) {
							int k1 = 4;
							typeface.render(k1, y, "From", 0);
							k1 += typeface.getColouredTextWidth("From ");
							if (privilege == 1) {
								modIcons[0].draw(k1, y - 12);
								k1 += 14;
							}
							if (privilege == 2) {
								modIcons[1].draw(k1, y - 12);
								k1 += 14;
							}
							typeface.render(k1, y, username + ":", 0);
							k1 += typeface.getColouredTextWidth(username) + 8;
							typeface.render(k1, y, chatMessages[message], 0x800000);
						}
						count++;
					}
					if (type == 4 && (tradeChatMode == 0 || tradeChatMode == 1 && displayMessageFrom(username))) {
						if (y > 0 && y < 110) {
							typeface.render(4, y, username + " " + chatMessages[message], 0x800080);
						}
						count++;
					}
					if (type == 5 && anInt1195 == 0 && privateChatMode < 2) {
						if (y > 0 && y < 110) {
							typeface.render(4, y, chatMessages[message], 0x800000);
						}
						count++;
					}
					if (type == 6 && anInt1195 == 0 && privateChatMode < 2) {
						if (y > 0 && y < 110) {
							typeface.render(4, y, "To " + username + ":", 0);
							typeface.render(12 + typeface.getColouredTextWidth("To " + username), y, chatMessages[message],
									0x800000);
						}
						count++;
					}
					if (type == 8 && (tradeChatMode == 0 || tradeChatMode == 1 && displayMessageFrom(username))) {
						if (y > 0 && y < 110) {
							typeface.render(4, y, username + " " + chatMessages[message], 0x7e3200);
						}
						count++;
					}
				}
			}

			Raster.setDefaultBounds();
			anInt1211 = count * 14 + 7;
			if (anInt1211 < 78) {
				anInt1211 = 78;
			}
			drawScrollbar(77, anInt1211 - anInt1089 - 77, 0, 463, anInt1211);
			String s;
			if (localPlayer != null && localPlayer.name != null) {
				s = localPlayer.name;
			} else {
				s = StringUtils.format(username);
			}
			typeface.render(4, 90, s + ":", 0);
			typeface.render(6 + typeface.getColouredTextWidth(s + ": "), 90, input + "*", 255);
			Raster.drawHorizontal(0, 77, 479, 0);
		}
		if (menuOpen && anInt948 == 2) {
			method40();
		}
		aClass15_1166.drawImage(super.graphics, 17, 357);
		aClass15_1165.initializeRasterizer();
		Rasterizer.scanOffsets = anIntArray1182;
	}

	public final void drawGameScreen() {
		if (aBoolean1255) {
			aBoolean1255 = false;
			backLeft1Buffer.drawImage(super.graphics, 0, 4);
			backLeft2Buffer.drawImage(super.graphics, 0, 357);
			backRight1Buffer.drawImage(super.graphics, 722, 4);
			backRight2Buffer.drawImage(super.graphics, 743, 205);
			backTopBuffer.drawImage(super.graphics, 0, 0);
			aClass15_908.drawImage(super.graphics, 516, 4);
			aClass15_909.drawImage(super.graphics, 516, 205);
			aClass15_910.drawImage(super.graphics, 496, 357);
			aClass15_911.drawImage(super.graphics, 0, 338);
			redrawTabArea = true;
			redrawDialogueBox = true;
			aBoolean1103 = true;
			aBoolean1233 = true;
			if (loadingStage != 2) {
				aClass15_1165.drawImage(super.graphics, 4, 4);
				aClass15_1164.drawImage(super.graphics, 550, 4);
			}
		}

		if (loadingStage == 2) {
			method146();
		}

		if (menuOpen && anInt948 == 1) {
			redrawTabArea = true;
		}

		if (overlayInterfaceId != -1) {
			boolean redrawRequired = processWidgetAnimations(overlayInterfaceId, tickDelta);
			if (redrawRequired) {
				redrawTabArea = true;
			}
		}
		if (anInt1246 == 2) {
			redrawTabArea = true;
		}
		if (anInt1086 == 2) {
			redrawTabArea = true;
		}
		if (redrawTabArea) {
			drawTabs();
			redrawTabArea = false;
		}
		if (backDialogueId == -1) {
			aClass9_1059.scrollPosition = anInt1211 - anInt1089 - 77;
			if (super.mouseEventX > 448 && super.mouseEventX < 560 && super.mouseEventY > 332) {
				updateScrollbar(aClass9_1059, 463, 77, super.mouseEventX - 17, super.mouseEventY - 357, 0, false, anInt1211);
			}
			int i = anInt1211 - 77 - aClass9_1059.scrollPosition;
			if (i < 0) {
				i = 0;
			}
			if (i > anInt1211 - 77) {
				i = anInt1211 - 77;
			}
			if (anInt1089 != i) {
				anInt1089 = i;
				redrawDialogueBox = true;
			}
		}
		if (backDialogueId != -1) {
			boolean flag2 = processWidgetAnimations(backDialogueId, tickDelta);
			if (flag2) {
				redrawDialogueBox = true;
			}
		}
		if (anInt1246 == 3) {
			redrawDialogueBox = true;
		}
		if (anInt1086 == 3) {
			redrawDialogueBox = true;
		}
		if (clickToContinueString != null) {
			redrawDialogueBox = true;
		}
		if (menuOpen && anInt948 == 2) {
			redrawDialogueBox = true;
		}
		if (redrawDialogueBox) {
			drawDialogueBox();
			redrawDialogueBox = false;
		}
		if (loadingStage == 2) {
			method126();
			aClass15_1164.drawImage(super.graphics, 550, 4);
		}
		if (flashingSidebarId != -1) {
			aBoolean1103 = true;
		}
		if (aBoolean1103) {
			if (flashingSidebarId != -1 && flashingSidebarId == tabId) {
				flashingSidebarId = -1;
				outgoing.writeOpcode(120);
				outgoing.writeByte(tabId);
			}
			aBoolean1103 = false;
			aClass15_1125.initializeRasterizer();
			backHmid1.draw(0, 0);

			if (overlayInterfaceId == -1) {
				if (inventoryTabIds[tabId] != -1) {
					if (tabId == 0) {
						aClass30_Sub2_Sub1_Sub2_1143.draw(22, 10);
					}
					if (tabId == 1) {
						aClass30_Sub2_Sub1_Sub2_1144.draw(54, 8);
					}
					if (tabId == 2) {
						aClass30_Sub2_Sub1_Sub2_1144.draw(82, 8);
					}
					if (tabId == 3) {
						aClass30_Sub2_Sub1_Sub2_1145.draw(110, 8);
					}
					if (tabId == 4) {
						aClass30_Sub2_Sub1_Sub2_1147.draw(153, 8);
					}
					if (tabId == 5) {
						aClass30_Sub2_Sub1_Sub2_1147.draw(181, 8);
					}
					if (tabId == 6) {
						aClass30_Sub2_Sub1_Sub2_1146.draw(209, 9);
					}
				}
				if (inventoryTabIds[0] != -1 && (flashingSidebarId != 0 || tick % 20 < 10)) {
					sideIcons[0].draw(29, 13);
				}
				if (inventoryTabIds[1] != -1 && (flashingSidebarId != 1 || tick % 20 < 10)) {
					sideIcons[1].draw(53, 11);
				}
				if (inventoryTabIds[2] != -1 && (flashingSidebarId != 2 || tick % 20 < 10)) {
					sideIcons[2].draw(82, 11);
				}
				if (inventoryTabIds[3] != -1 && (flashingSidebarId != 3 || tick % 20 < 10)) {
					sideIcons[3].draw(115, 12);
				}
				if (inventoryTabIds[4] != -1 && (flashingSidebarId != 4 || tick % 20 < 10)) {
					sideIcons[4].draw(153, 13);
				}
				if (inventoryTabIds[5] != -1 && (flashingSidebarId != 5 || tick % 20 < 10)) {
					sideIcons[5].draw(180, 11);
				}
				if (inventoryTabIds[6] != -1 && (flashingSidebarId != 6 || tick % 20 < 10)) {
					sideIcons[6].draw(208, 13);
				}
			}
			aClass15_1125.drawImage(super.graphics, 516, 160);
			aClass15_1124.initializeRasterizer();
			backBase2.draw(0, 0);
			if (overlayInterfaceId == -1) {
				if (inventoryTabIds[tabId] != -1) {
					if (tabId == 7) {
						aClass30_Sub2_Sub1_Sub2_865.draw(42, 0);
					}
					if (tabId == 8) {
						aClass30_Sub2_Sub1_Sub2_866.draw(74, 0);
					}
					if (tabId == 9) {
						aClass30_Sub2_Sub1_Sub2_866.draw(102, 0);
					}
					if (tabId == 10) {
						aClass30_Sub2_Sub1_Sub2_867.draw(130, 1);
					}
					if (tabId == 11) {
						aClass30_Sub2_Sub1_Sub2_869.draw(173, 0);
					}
					if (tabId == 12) {
						aClass30_Sub2_Sub1_Sub2_869.draw(201, 0);
					}
					if (tabId == 13) {
						aClass30_Sub2_Sub1_Sub2_868.draw(229, 0);
					}
				}
				if (inventoryTabIds[8] != -1 && (flashingSidebarId != 8 || tick % 20 < 10)) {
					sideIcons[7].draw(74, 2);
				}
				if (inventoryTabIds[9] != -1 && (flashingSidebarId != 9 || tick % 20 < 10)) {
					sideIcons[8].draw(102, 3);
				}
				if (inventoryTabIds[10] != -1 && (flashingSidebarId != 10 || tick % 20 < 10)) {
					sideIcons[9].draw(137, 4);
				}
				if (inventoryTabIds[11] != -1 && (flashingSidebarId != 11 || tick % 20 < 10)) {
					sideIcons[10].draw(174, 2);
				}
				if (inventoryTabIds[12] != -1 && (flashingSidebarId != 12 || tick % 20 < 10)) {
					sideIcons[11].draw(201, 2);
				}
				if (inventoryTabIds[13] != -1 && (flashingSidebarId != 13 || tick % 20 < 10)) {
					sideIcons[12].draw(226, 2);
				}
			}
			aClass15_1124.drawImage(super.graphics, 496, 466);
			aClass15_1165.initializeRasterizer();
		}
		if (aBoolean1233) {
			aBoolean1233 = false;
			aClass15_1123.initializeRasterizer();
			backBase1.draw(0, 0);

			frameFont.shadowCentre(55, 28, "Public chat", true, 0xffffff);
			if (publicChatMode == 0) {
				frameFont.shadowCentre(55, 41, "On", true, 65280);
			} else if (publicChatMode == 1) {
				frameFont.shadowCentre(55, 41, "Friends", true, 0xffff00);
			} else if (publicChatMode == 2) {
				frameFont.shadowCentre(55, 41, "Off", true, 0xff0000);
			} else if (publicChatMode == 3) {
				frameFont.shadowCentre(55, 41, "Hide", true, 65535);
			}

			frameFont.shadowCentre(184, 28, "Private chat", true, 0xffffff);
			if (privateChatMode == 0) {
				frameFont.shadowCentre(184, 41, "On", true, 65280);
			} else if (privateChatMode == 1) {
				frameFont.shadowCentre(184, 41, "Friends", true, 0xffff00);
			} else if (privateChatMode == 2) {
				frameFont.shadowCentre(184, 41, "Off", true, 0xff0000);
			}

			frameFont.shadowCentre(324, 28, "Trade/compete", true, 0xffffff);
			if (tradeChatMode == 0) {
				frameFont.shadowCentre(324, 41, "On", true, 65280);
			} else if (tradeChatMode == 1) {
				frameFont.shadowCentre(324, 41, "Friends", true, 0xffff00);
			} else if (tradeChatMode == 2) {
				frameFont.shadowCentre(324, 41, "Off", true, 0xff0000);
			}

			frameFont.shadowCentre(458, 33, "Report abuse", true, 0xffffff);
			aClass15_1123.drawImage(super.graphics, 0, 453);
			aClass15_1165.initializeRasterizer();
		}
		tickDelta = 0;
	}

	public final void drawHintIcon() {
		if (hintIconDrawType != 2) {
			return;
		}

		method128((anInt934 - regionBaseX << 7) + anInt937, anInt936 * 2, (anInt935 - regionBaseY << 7) + anInt938);
		if (spriteDrawX > -1 && tick % 20 < 10) {
			headIcons[2].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
		}
	}

	@Override
	public final void drawLoadingText(int i, String text) {
		anInt1079 = i;
		loadingScreenText = text;
		resetTitleScreen();

		if (titleScreen == null) {
			super.drawLoadingText(i, text);
			return;
		}

		aClass15_1109.initializeRasterizer();
		char x = '\u0168';
		char c1 = '\310';
		byte byte1 = 20;

		bold.renderCentre(x / 2, c1 / 2 - 26 - byte1, "RuneScape is loading - please wait...", 0xffffff);
		int y = c1 / 2 - 18 - byte1;
		Raster.drawRectangle(x / 2 - 152, y, 304, 34, 0x8c1111);
		Raster.drawRectangle(x / 2 - 151, y + 1, 302, 32, 0);
		Raster.fillRectangle(x / 2 - 150, y + 2, i * 3, 30, 0x8c1111);
		Raster.fillRectangle(x / 2 - 150 + i * 3, y + 2, 300 - i * 3, 30, 0);
		bold.renderCentre(x / 2, c1 / 2 + 5 - byte1, text, 0xffffff);

		aClass15_1109.drawImage(super.graphics, 202, 171);
		if (aBoolean1255) {
			aBoolean1255 = false;
			if (!aBoolean831) {
				aClass15_1110.drawImage(super.graphics, 0, 0);
				aClass15_1111.drawImage(super.graphics, 637, 0);
			}
			aClass15_1107.drawImage(super.graphics, 128, 0);
			aClass15_1108.drawImage(super.graphics, 202, 371);
			aClass15_1112.drawImage(super.graphics, 0, 265);
			aClass15_1113.drawImage(super.graphics, 562, 265);
			aClass15_1114.drawImage(super.graphics, 128, 171);
			aClass15_1115.drawImage(super.graphics, 562, 171);
		}
	}

	public final void drawLoginScreen(boolean flag) {
		resetTitleScreen();
		aClass15_1109.initializeRasterizer();
		titleBox.draw(0, 0);
		char x = '\u0168';
		char c1 = '\310';

		if (loginScreenStage == 0) {
			int y = c1 / 2 + 80;
			smallFont.shadowCentre(x / 2, y, provider.getLoadingMessage(), true, 0x75a9a9);
			y = c1 / 2 - 20;
			bold.shadowCentre(x / 2, y, "Welcome to RuneScape", true, 0xffff00);
			y += 30;
			int buttonX = x / 2 - 80;
			int buttonY = c1 / 2 + 20;
			titleButton.draw(buttonX - 73, buttonY - 20);
			bold.shadowCentre(buttonX, buttonY + 5, "New User", true, 0xffffff);
			buttonX = x / 2 + 80;
			titleButton.draw(buttonX - 73, buttonY - 20);
			bold.shadowCentre(buttonX, buttonY + 5, "Existing User", true, 0xffffff);
		} else if (loginScreenStage == 2) {
			int y = c1 / 2 - 40;
			if (firstLoginMessage.length() > 0) {
				bold.shadowCentre(x / 2, y - 15, firstLoginMessage, true, 0xffff00);
				bold.shadowCentre(x / 2, y, secondLoginMessage, true, 0xffff00);
				y += 30;
			} else {
				bold.shadowCentre(x / 2, y - 7, secondLoginMessage, true, 0xffff00);
				y += 30;
			}
			bold.shadow(x / 2 - 90, y, "Username: " + username + (loginInputLine == 0 & tick % 40 < 20 ? "@yel@|" : ""), true,
					0xffffff);
			y += 15;
			bold.shadow(x / 2 - 88, y, "Password: " + StringUtils.getAsterisks(password)
					+ (loginInputLine == 1 & tick % 40 < 20 ? "@yel@|" : ""), true, 0xffffff);
			y += 15;
			if (!flag) {
				int cancelX = x / 2 - 80;
				int cancelY = c1 / 2 + 50;
				titleButton.draw(cancelX - 73, cancelY - 20);
				bold.shadowCentre(cancelX, cancelY + 5, "Login", true, 0xffffff);
				cancelX = x / 2 + 80;
				titleButton.draw(cancelX - 73, cancelY - 20);
				bold.shadowCentre(cancelX, cancelY + 5, "Cancel", true, 0xffffff);
			}
		} else if (loginScreenStage == 3) {
			bold.shadowCentre(x / 2, c1 / 2 - 60, "Create a free account", true, 0xffff00);
			int y = c1 / 2 - 35;
			bold.shadowCentre(x / 2, y, "To create a new account you need to", true, 0xffffff);
			y += 15;
			bold.shadowCentre(x / 2, y, "go back to the main RuneScape webpage", true, 0xffffff);
			y += 15;
			bold.shadowCentre(x / 2, y, "and choose the red 'create account'", true, 0xffffff);
			y += 15;
			bold.shadowCentre(x / 2, y, "button at the top right of that page.", true, 0xffffff);
			y += 15;
			int cancelX = x / 2;
			int cancelY = c1 / 2 + 50;
			titleButton.draw(cancelX - 73, cancelY - 20);
			bold.shadowCentre(cancelX, cancelY + 5, "Cancel", true, 0xffffff);
		}

		aClass15_1109.drawImage(super.graphics, 202, 171);
		if (aBoolean1255) {
			aBoolean1255 = false;
			aClass15_1107.drawImage(super.graphics, 128, 0);
			aClass15_1108.drawImage(super.graphics, 202, 371);
			aClass15_1112.drawImage(super.graphics, 0, 265);
			aClass15_1113.drawImage(super.graphics, 562, 265);
			aClass15_1114.drawImage(super.graphics, 128, 171);
			aClass15_1115.drawImage(super.graphics, 562, 171);
		}
	}

	public final void drawMinimap(Sprite sprite, int y, int x) {
		int r = x * x + y * y;

		if (r > 4225 && r < 0x15f90) {
			int theta = cameraYaw + anInt1209 & 0x7ff;
			int sin = Model.SINE[theta];
			int cos = Model.COSINE[theta];
			sin = sin * 256 / (anInt1170 + 256);
			cos = cos * 256 / (anInt1170 + 256);
			int l1 = y * sin + x * cos >> 16;
			int i2 = y * cos - x * sin >> 16;
			double d = Math.atan2(l1, i2);
			int j2 = (int) (Math.sin(d) * 63D);
			int k2 = (int) (Math.cos(d) * 57D);
			mapEdge.method353(83 - k2 - 20, 15, 20, 15, 256, 20, d, 94 + j2 + 4 - 10);
		} else {
			drawOnMinimap(sprite, x, y);
		}
	}

	public final void drawOnMinimap(Sprite sprite, int x, int y) {
		int k = cameraYaw + anInt1209 & 0x7ff;
		int r = x * x + y * y;
		if (r > 6400) {
			return;
		}

		int sin = Model.SINE[k];
		int cos = Model.COSINE[k];
		sin = sin * 256 / (anInt1170 + 256);
		cos = cos * 256 / (anInt1170 + 256);
		int k1 = y * sin + x * cos >> 16;
		int l1 = y * cos - x * sin >> 16;

		if (r > 2500) {
			sprite.method354(mapBackground, 83 - l1 - sprite.getResizeHeight() / 2 - 4, 94 + k1 - sprite.getResizeWidth() / 2 + 4);
		} else {
			sprite.drawSprite(94 + k1 - sprite.getResizeWidth() / 2 + 4, 83 - l1 - sprite.getResizeHeight() / 2 - 4);
		}
	}

	public final void drawScreen() {
		if (aClass15_1166 != null) {
			return;
		}
		method118();
		super.frameGraphicsBuffer = null;
		aClass15_1107 = null;
		aClass15_1108 = null;
		aClass15_1109 = null;
		aClass15_1110 = null;
		aClass15_1111 = null;
		aClass15_1112 = null;
		aClass15_1113 = null;
		aClass15_1114 = null;
		aClass15_1115 = null;
		aClass15_1166 = new ProducingGraphicsBuffer(getFrame(), 479, 96);
		aClass15_1164 = new ProducingGraphicsBuffer(getFrame(), 172, 156);
		Raster.reset();
		mapBackground.draw(0, 0);
		aClass15_1163 = new ProducingGraphicsBuffer(getFrame(), 190, 261);
		aClass15_1165 = new ProducingGraphicsBuffer(getFrame(), 512, 334);
		Raster.reset();
		aClass15_1123 = new ProducingGraphicsBuffer(getFrame(), 496, 50);
		aClass15_1124 = new ProducingGraphicsBuffer(getFrame(), 269, 37);
		aClass15_1125 = new ProducingGraphicsBuffer(getFrame(), 249, 45);
		aBoolean1255 = true;
	}

	public final void drawScrollbar(int height, int k, int y, int x, int j1) {
		aClass30_Sub2_Sub1_Sub2_1024.draw(x, y);
		aClass30_Sub2_Sub1_Sub2_1025.draw(x, y + height - 16);
		Raster.fillRectangle(x, y + 16, 16, height - 32, anInt1002);
		int k1 = (height - 32) * height / j1;
		if (k1 < 8) {
			k1 = 8;
		}

		int l1 = (height - 32 - k1) * k / (j1 - height);
		Raster.fillRectangle(x, y + 16 + l1, 16, k1, anInt1063);
		Raster.drawVertical(x, y + 16 + l1, k1, anInt902);
		Raster.drawVertical(x + 1, y + 16 + l1, k1, anInt902);
		Raster.drawHorizontal(x, y + 16 + l1, 16, anInt902);
		Raster.drawHorizontal(x, y + 17 + l1, 16, anInt902);
		Raster.drawVertical(x + 15, y + 16 + l1, k1, anInt927);
		Raster.drawVertical(x + 14, y + 17 + l1, k1 - 1, anInt927);
		Raster.drawHorizontal(x, y + 15 + l1 + k1, 16, anInt927);
		Raster.drawHorizontal(x + 1, y + 14 + l1 + k1, 15, anInt927);
	}

	public final void drawTabs() {
		aClass15_1163.initializeRasterizer();
		Rasterizer.scanOffsets = anIntArray1181;
		inventoryBackground.draw(0, 0);
		if (overlayInterfaceId != -1) {
			drawWidget(Widget.widgets[overlayInterfaceId], 0, 0, 0);
		} else if (inventoryTabIds[tabId] != -1) {
			drawWidget(Widget.widgets[inventoryTabIds[tabId]], 0, 0, 0);
		}
		if (menuOpen && anInt948 == 1) {
			method40();
		}

		aClass15_1163.drawImage(super.graphics, 553, 205);
		aClass15_1165.initializeRasterizer();
		Rasterizer.scanOffsets = anIntArray1182;
	}

	public final void drawTitleBackground() {
		Sprite sprite = new Sprite(titleScreen.getEntry("title.dat"), this);
		aClass15_1110.initializeRasterizer();
		sprite.method346(0, 0);
		aClass15_1111.initializeRasterizer();
		sprite.method346(-637, 0);
		aClass15_1107.initializeRasterizer();
		sprite.method346(-128, 0);
		aClass15_1108.initializeRasterizer();
		sprite.method346(-202, -371);
		aClass15_1109.initializeRasterizer();
		sprite.method346(-202, -171);
		aClass15_1112.initializeRasterizer();
		sprite.method346(0, -265);
		aClass15_1113.initializeRasterizer();
		sprite.method346(-562, -265);
		aClass15_1114.initializeRasterizer();
		sprite.method346(-128, -171);
		aClass15_1115.initializeRasterizer();
		sprite.method346(-562, -171);
		int raster[] = new int[sprite.getWidth()];
		for (int y = 0; y < sprite.getHeight(); y++) {
			for (int x = 0; x < sprite.getWidth(); x++) {
				raster[x] = sprite.getRaster()[sprite.getWidth() - x - 1 + sprite.getWidth() * y];
			}

			for (int x = 0; x < sprite.getWidth(); x++) {
				sprite.getRaster()[x + sprite.getWidth() * y] = raster[x];
			}
		}

		aClass15_1110.initializeRasterizer();
		sprite.method346(382, 0);
		aClass15_1111.initializeRasterizer();
		sprite.method346(-255, 0);
		aClass15_1107.initializeRasterizer();
		sprite.method346(254, 0);
		aClass15_1108.initializeRasterizer();
		sprite.method346(180, -371);
		aClass15_1109.initializeRasterizer();
		sprite.method346(180, -171);
		aClass15_1112.initializeRasterizer();
		sprite.method346(382, -265);
		aClass15_1113.initializeRasterizer();
		sprite.method346(-180, -265);
		aClass15_1114.initializeRasterizer();
		sprite.method346(254, -171);
		aClass15_1115.initializeRasterizer();
		sprite.method346(-180, -171);
		sprite = new Sprite(titleScreen, "logo", 0);
		aClass15_1107.initializeRasterizer();
		sprite.drawSprite(382 - sprite.getWidth() / 2 - 128, 18);
		sprite = null;
		System.gc();
	}

	public final int executeScript(Widget widget, int id) {
		if (widget.scripts == null || id >= widget.scripts.length) {
			return -2;
		}
		try {
			int[] script = widget.scripts[id];
			int accumulator = 0;
			int counter = 0;
			int operator = 0;

			do {
				int instruction = script[counter++];
				int value = 0;
				byte next = 0;

				if (instruction == 0) {
					return accumulator;
				} else if (instruction == 1) {
					value = currentLevels[script[counter++]];
				} else if (instruction == 2) {
					value = maximumLevels[script[counter++]];
				} else if (instruction == 3) {
					value = experiences[script[counter++]];
				} else if (instruction == 4) {
					Widget other = Widget.widgets[script[counter++]];
					int item = script[counter++];

					if (item >= 0 && item < ItemDefinition.getCount()
							&& (!ItemDefinition.lookup(item).isMembers() || membersServer)) {
						for (int slot = 0; slot < other.inventoryIds.length; slot++) {
							if (other.inventoryIds[slot] == item + 1) {
								value += other.inventoryAmounts[slot];
							}
						}
					}
				} else if (instruction == 5) {
					value = settings[script[counter++]];
				} else if (instruction == 6) {
					value = SKILL_EXPERIENCE[maximumLevels[script[counter++]] - 1];
				} else if (instruction == 7) {
					value = settings[script[counter++]] * 100 / 46875;
				} else if (instruction == 8) {
					value = localPlayer.combat;
				} else if (instruction == 9) {
					for (int skill = 0; skill < SkillConstants.SKILL_COUNT; skill++) {
						if (SkillConstants.ENABLED_SKILLS[skill]) {
							value += maximumLevels[skill];
						}
					}
				} else if (instruction == 10) {
					Widget other = Widget.widgets[script[counter++]];
					int item = script[counter++] + 1;

					if (item >= 0 && item < ItemDefinition.getCount()
							&& (!ItemDefinition.lookup(item).isMembers() || membersServer)) {
						for (int stored : other.inventoryIds) {
							if (stored == item) {
								value = 999999999;
								break;
							}
						}
					}
				} else if (instruction == 11) {
					value = runEnergy;
				} else if (instruction == 12) {
					value = weight;
				} else if (instruction == 13) {
					int bool = settings[script[counter++]];
					int shift = script[counter++];
					value = (bool & 1 << shift) == 0 ? 0 : 1;
				} else if (instruction == 14) {
					int index = script[counter++];
					VariableBits bits = VariableBits.bits[index];
					int setting = bits.getSetting();
					int low = bits.getLow();
					int high = bits.getHigh();
					int mask = BIT_MASKS[high - low];
					value = settings[setting] >> low & mask;
				} else if (instruction == 15) {
					next = 1;
				} else if (instruction == 16) {
					next = 2;
				} else if (instruction == 17) {
					next = 3;
				} else if (instruction == 18) {
					value = (localPlayer.worldX >> 7) + regionBaseX;
				} else if (instruction == 19) {
					value = (localPlayer.worldY >> 7) + regionBaseY;
				} else if (instruction == 20) {
					value = script[counter++];
				}

				if (next == 0) {
					if (operator == 0) {
						accumulator += value;
					} else if (operator == 1) {
						accumulator -= value;
					} else if (operator == 2 && value != 0) {
						accumulator /= value;
					} else if (operator == 3) {
						accumulator *= value;
					}

					operator = 0;
				} else {
					operator = next;
				}
			} while (true);
		} catch (Exception ex) {
			return -1;
		}
	}

	@Override
	public final AppletContext getAppletContext() {
		if (SignLink.getApplet() != null) {
			return SignLink.getApplet().getAppletContext();
		}
		return super.getAppletContext();
	}

	@Override
	public final URL getCodeBase() {
		if (SignLink.getApplet() != null) {
			return SignLink.getApplet().getCodeBase();
		}

		try {
			if (super.frame != null) {
				return new URL("http://" + IP + ":" + (80 + portOffset));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return super.getCodeBase();
	}

	/**
	 * Gets a string representing the specified integer, returning "*" if the value is greater than 999,999,999.
	 * 
	 * @param value The value.
	 * @return The string.
	 */
	public final String getDisplayableAmount(int value) {
		if (value < 999999999) {
			return String.valueOf(value);
		}
		return "*";
	}

	@Override
	public final Component getFrame() {
		if (SignLink.getApplet() != null) {
			return SignLink.getApplet();
		} else if (super.frame != null) {
			return super.frame;
		}

		return this;
	}

	public final String getHost() {
		if (SignLink.getApplet() != null) {
			return SignLink.getApplet().getDocumentBase().getHost().toLowerCase();
		} else if (super.frame != null) {
			return "runescape.com";
		}

		return super.getDocumentBase().getHost().toLowerCase();
	}

	public final int getNextFlameColour(int i, int j, int k) {
		int l = 256 - k;
		return ((i & 0xff00ff) * l + (j & 0xff00ff) * k & 0xff00ff00) + ((i & 0xff00) * l + (j & 0xff00) * k & 0xff0000) >> 8;
	}

	@Override
	public final String getParameter(String parameter) {
		if (SignLink.getApplet() != null) {
			return SignLink.getApplet().getParameter(parameter);
		}

		return super.getParameter(parameter);
	}

	@Override
	public final void init() {
		node = Integer.parseInt(getParameter("nodeid"));
		portOffset = Integer.parseInt(getParameter("portoff"));

		String lowMemory = getParameter("lowmem");
		if (lowMemory != null && lowMemory.equals("1")) {
			setLowMemory();
		} else {
			setHighMemory();
		}

		String free = getParameter("free");
		membersServer = (free == null || !free.equals("1"));

		startApplet(503, 765);
	}

	public final boolean isAddFriend(int index) {
		if (index < 0) {
			return false;
		}

		int action = menuActionTypes[index];
		if (action >= 2000) {
			action -= 2000;
		}
		return action == Actions.ADD_FRIEND;
	}

	@Override
	public final void load() {
		drawLoadingText(20, "Starting up");
		if (SignLink.isSunJava()) {
			super.minimumSleepTime = 5;
		}

		if (clientLoaded) {
			gameAlreadyLoaded = true;
			return;
		}

		clientLoaded = true;
		boolean validHost = true;
		String host = getHost();
		host = IP;

		if (host.endsWith("jagex.com")) {
			validHost = true;
		}
		if (host.endsWith("runescape.com")) {
			validHost = true;
		}
		if (host.endsWith("192.168.1.2")) {
			validHost = true;
		}
		if (host.endsWith("192.168.1.229")) {
			validHost = true;
		}
		if (host.endsWith("192.168.1.228")) {
			validHost = true;
		}
		if (host.endsWith("192.168.1.227")) {
			validHost = true;
		}
		if (host.endsWith("192.168.1.226")) {
			validHost = true;
		}
		if (host.endsWith("127.0.0.1")) {
			validHost = true;
		}

		if (!validHost) {
			unableToLoad = true;
			return;
		}

		if (SignLink.getCache() != null) {
			for (int index = 0; index < 5; index++) {
				indices[index] = new Index(SignLink.getIndices()[index], SignLink.getCache(), index + 1, 0x7a120);
			}

		}
		try {
			requestCrcs();
			titleScreen = createArchive(1, "title screen", "title", archiveCRCs[1], 25);
			smallFont = new Font(false, "p11_full", titleScreen);
			frameFont = new Font(false, "p12_full", titleScreen);
			bold = new Font(false, "b12_full", titleScreen);
			aClass30_Sub2_Sub1_Sub4_1273 = new Font(true, "q8_full", titleScreen);
			drawTitleBackground();
			method51();

			Archive config = createArchive(2, "config", "config", archiveCRCs[2], 30);
			Archive widgets = createArchive(3, "interface", "interface", archiveCRCs[3], 35);
			Archive graphics = createArchive(4, "2d graphics", "media", archiveCRCs[4], 40);
			Archive textures = createArchive(6, "textures", "textures", archiveCRCs[6], 45);
			Archive chat = createArchive(7, "chat system", "wordenc", archiveCRCs[7], 50);
			Archive sounds = createArchive(8, "sound effects", "sounds", archiveCRCs[8], 55);

			tileFlags = new byte[4][104][104];
			tileHeights = new int[4][105][105];
			scene = new SceneGraph(104, 104, 4, tileHeights);
			for (int index = 0; index < 4; index++) {
				collisionMaps[index] = new CollisionMap(104, 104);
			}

			aClass30_Sub2_Sub1_Sub1_1263 = new Sprite(512, 512);
			Archive version = createArchive(5, "update list", "versionlist", archiveCRCs[5], 60);
			drawLoadingText(60, "Connecting to update server");
			provider = new ResourceProvider();
			provider.init(version, this);
			Frame.init(provider.frameCount());
			Model.init(provider.getCount(0), provider);

			if (!lowMemory) {
				musicId = 0;
				try {
					musicId = Integer.parseInt(getParameter("music"));
				} catch (Exception ex) {
				}

				fadeMusic = true;
				provider.provide(2, musicId);
				while (provider.remaining() > 0) {
					processLoadedResources();

					try {
						Thread.sleep(100L);
					} catch (Exception ex) {
					}

					if (provider.getErrors() > 3) {
						error("ondemand");
						return;
					}
				}
			}

			drawLoadingText(65, "Requesting animations");
			int remaining = provider.getCount(1);
			for (int file = 0; file < remaining; file++) {
				provider.provide(1, file);
			}

			while (provider.remaining() > 0) {
				int complete = remaining - provider.remaining();
				if (complete > 0) {
					drawLoadingText(65, "Loading animations - " + complete * 100 / remaining + "%");
				}

				processLoadedResources();
				try {
					Thread.sleep(100L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (provider.getErrors() > 3) {
					error("ondemand");
					return;
				}
			}

			drawLoadingText(70, "Requesting models");
			remaining = provider.getCount(0);
			for (int file = 0; file < remaining; file++) {
				int attributes = provider.getModelAttributes(file);

				if ((attributes & 1) != 0) {
					provider.provide(0, file);
				}
			}

			remaining = provider.remaining();
			while (provider.remaining() > 0) {
				int complete = remaining - provider.remaining();
				if (complete > 0) {
					drawLoadingText(70, "Loading models - " + complete * 100 / remaining + "%");
				}
				processLoadedResources();

				try {
					Thread.sleep(100L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (indices[0] != null) {
				drawLoadingText(75, "Requesting maps");
				provider.provide(3, provider.resolve(47, 48, 0));
				provider.provide(3, provider.resolve(47, 48, 1));
				provider.provide(3, provider.resolve(48, 48, 0));
				provider.provide(3, provider.resolve(48, 48, 1));
				provider.provide(3, provider.resolve(49, 48, 0));
				provider.provide(3, provider.resolve(49, 48, 1));
				provider.provide(3, provider.resolve(47, 47, 0));
				provider.provide(3, provider.resolve(47, 47, 1));
				provider.provide(3, provider.resolve(48, 47, 0));
				provider.provide(3, provider.resolve(48, 47, 1));
				provider.provide(3, provider.resolve(48, 148, 0));
				provider.provide(3, provider.resolve(48, 148, 1));
				remaining = provider.remaining();

				while (provider.remaining() > 0) {
					int complete = remaining - provider.remaining();
					if (complete > 0) {
						drawLoadingText(75, "Loading maps - " + complete * 100 / remaining + "%");
					}
					processLoadedResources();

					try {
						Thread.sleep(100L);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			remaining = provider.getCount(0);
			for (int file = 0; file < remaining; file++) {
				int attributes = provider.getModelAttributes(file);
				byte priority = 0;
				if ((attributes & 8) != 0) {
					priority = 10;
				} else if ((attributes & 0x20) != 0) {
					priority = 9;
				} else if ((attributes & 0x10) != 0) {
					priority = 8;
				} else if ((attributes & 0x40) != 0) {
					priority = 7;
				} else if ((attributes & 0x80) != 0) {
					priority = 6;
				} else if ((attributes & 2) != 0) {
					priority = 5;
				} else if ((attributes & 4) != 0) {
					priority = 4;
				} else if ((attributes & 1) != 0) {
					priority = 3;
				}

				if (priority != 0) {
					provider.requestExtra(0, file, priority);
				}
			}

			provider.preloadMaps(membersServer);
			if (!lowMemory) {
				int musicCount = provider.getCount(2);
				for (int music = 1; music < musicCount; music++) {
					if (provider.highPriorityMusic(music)) {
						provider.requestExtra(2, music, (byte) 1);
					}
				}
			}

			drawLoadingText(80, "Unpacking media");
			inventoryBackground = new IndexedImage(graphics, "invback", 0);
			chatBackground = new IndexedImage(graphics, "chatback", 0);
			mapBackground = new IndexedImage(graphics, "mapback", 0);
			backBase1 = new IndexedImage(graphics, "backbase1", 0);
			backBase2 = new IndexedImage(graphics, "backbase2", 0);
			backHmid1 = new IndexedImage(graphics, "backhmid1", 0);

			for (int icon = 0; icon < 13; icon++) {
				sideIcons[icon] = new IndexedImage(graphics, "sideicons", icon);
			}

			compass = new Sprite(graphics, "compass", 0);
			mapEdge = new Sprite(graphics, "mapedge", 0);
			mapEdge.resize();

			try {
				for (int scene = 0; scene < 100; scene++) {
					mapScenes[scene] = new IndexedImage(graphics, "mapscene", scene);
				}
			} catch (Exception ex) {
			}

			try {
				for (int function = 0; function < 100; function++) {
					mapFunctions[function] = new Sprite(graphics, "mapfunction", function);
				}
			} catch (Exception ex) {
			}

			try {
				for (int mark = 0; mark < 20; mark++) {
					hitMarks[mark] = new Sprite(graphics, "hitmarks", mark);
				}
			} catch (Exception ex) {
			}

			try {
				for (int icon = 0; icon < 20; icon++) {
					headIcons[icon] = new Sprite(graphics, "headicons", icon);
				}
			} catch (Exception ex) {
			}

			firstMapmarker = new Sprite(graphics, "mapmarker", 0);
			secondMapmarker = new Sprite(graphics, "mapmarker", 1);
			for (int i = 0; i < 8; i++) {
				crosses[i] = new Sprite(graphics, "cross", i);
			}

			itemMapdot = new Sprite(graphics, "mapdots", 0);
			npcMapdot = new Sprite(graphics, "mapdots", 1);
			playerMapdot = new Sprite(graphics, "mapdots", 2);
			friendMapdot = new Sprite(graphics, "mapdots", 3);
			teamMapdot = new Sprite(graphics, "mapdots", 4);
			aClass30_Sub2_Sub1_Sub2_1024 = new IndexedImage(graphics, "scrollbar", 0);
			aClass30_Sub2_Sub1_Sub2_1025 = new IndexedImage(graphics, "scrollbar", 1);
			aClass30_Sub2_Sub1_Sub2_1143 = new IndexedImage(graphics, "redstone1", 0);
			aClass30_Sub2_Sub1_Sub2_1144 = new IndexedImage(graphics, "redstone2", 0);
			aClass30_Sub2_Sub1_Sub2_1145 = new IndexedImage(graphics, "redstone3", 0);
			aClass30_Sub2_Sub1_Sub2_1146 = new IndexedImage(graphics, "redstone1", 0);
			aClass30_Sub2_Sub1_Sub2_1146.flipHorizontally();
			aClass30_Sub2_Sub1_Sub2_1147 = new IndexedImage(graphics, "redstone2", 0);
			aClass30_Sub2_Sub1_Sub2_1147.flipHorizontally();
			aClass30_Sub2_Sub1_Sub2_865 = new IndexedImage(graphics, "redstone1", 0);
			aClass30_Sub2_Sub1_Sub2_865.flipVertically();
			aClass30_Sub2_Sub1_Sub2_866 = new IndexedImage(graphics, "redstone2", 0);
			aClass30_Sub2_Sub1_Sub2_866.flipVertically();
			aClass30_Sub2_Sub1_Sub2_867 = new IndexedImage(graphics, "redstone3", 0);
			aClass30_Sub2_Sub1_Sub2_867.flipVertically();
			aClass30_Sub2_Sub1_Sub2_868 = new IndexedImage(graphics, "redstone1", 0);
			aClass30_Sub2_Sub1_Sub2_868.flipHorizontally();
			aClass30_Sub2_Sub1_Sub2_868.flipVertically();
			aClass30_Sub2_Sub1_Sub2_869 = new IndexedImage(graphics, "redstone2", 0);
			aClass30_Sub2_Sub1_Sub2_869.flipHorizontally();
			aClass30_Sub2_Sub1_Sub2_869.flipVertically();

			for (int icon = 0; icon < 2; icon++) {
				modIcons[icon] = new IndexedImage(graphics, "mod_icons", icon);
			}

			Sprite sprite = new Sprite(graphics, "backleft1", 0);
			backLeft1Buffer = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backleft2", 0);
			backLeft2Buffer = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backright1", 0);
			backRight1Buffer = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backright2", 0);
			backRight2Buffer = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backtop1", 0);
			backTopBuffer = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backvmid1", 0);
			aClass15_908 = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backvmid2", 0);
			aClass15_909 = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backvmid3", 0);
			aClass15_910 = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);
			sprite = new Sprite(graphics, "backhmid2", 0);
			aClass15_911 = new ProducingGraphicsBuffer(getFrame(), sprite.getWidth(), sprite.getHeight());
			sprite.method346(0, 0);

			int red = (int) (Math.random() * 21D) - 10;
			int green = (int) (Math.random() * 21D) - 10;
			int blue = (int) (Math.random() * 21D) - 10;
			int offset = (int) (Math.random() * 41D) - 20;

			for (int i = 0; i < 100; i++) {
				if (mapFunctions[i] != null) {
					mapFunctions[i].recolour(red + offset, green + offset, blue + offset);
				}
				if (mapScenes[i] != null) {
					mapScenes[i].offsetColour(red + offset, green + offset, blue + offset);
				}
			}

			drawLoadingText(83, "Unpacking textures");
			Rasterizer.loadFloorImages(textures);
			Rasterizer.method372(0.8);
			Rasterizer.method367(20);
			drawLoadingText(86, "Unpacking config");
			Animation.init(config);
			ObjectDefinition.init(config);
			Floor.init(config);
			ItemDefinition.init(config);
			NpcDefinition.init(config);
			IdentityKit.init(config);
			Graphic.init(config);
			VariableParameter.init(config);
			VariableBits.init(config);
			ItemDefinition.membersServer = membersServer;

			if (!lowMemory) {
				drawLoadingText(90, "Unpacking sounds");
				byte[] data = sounds.getEntry("sounds.dat");
				Track.load(new Buffer(data));
			}

			drawLoadingText(95, "Unpacking interfaces");
			Font[] fonts = { smallFont, frameFont, bold, aClass30_Sub2_Sub1_Sub4_1273 };
			Widget.load(widgets, graphics, fonts);

			drawLoadingText(100, "Preparing game engine");
			for (int y = 0; y < 33; y++) {
				int firstX = 999;
				int lastX = 0;

				for (int x = 0; x < 34; x++) {
					if (mapBackground.getRaster()[x + y * mapBackground.getWidth()] == 0) {
						if (firstX == 999) {
							firstX = x;
						}

						continue;
					}

					if (firstX == 999) {
						continue;
					}

					lastX = x;
					break;
				}

				anIntArray968[y] = firstX;
				anIntArray1057[y] = lastX - firstX;
			}

			for (int y = 5; y < 156; y++) {
				int firstX = 999;
				int currentX = 0;

				for (int x = 25; x < 172; x++) {
					if (mapBackground.getRaster()[x + y * mapBackground.getWidth()] == 0 && (x > 34 || y > 34)) {
						if (firstX == 999) {
							firstX = x;
						}

						continue;
					}

					if (firstX == 999) {
						continue;
					}

					currentX = x;
					break;
				}

				anIntArray1052[y - 5] = firstX - 25;
				anIntArray1229[y - 5] = currentX - firstX;
			}

			Rasterizer.reposition(96, 479);
			anIntArray1180 = Rasterizer.scanOffsets;
			Rasterizer.reposition(261, 190);
			anIntArray1181 = Rasterizer.scanOffsets;
			Rasterizer.reposition(334, 512);
			anIntArray1182 = Rasterizer.scanOffsets;
			int ai[] = new int[9];
			for (int i8 = 0; i8 < 9; i8++) {
				int theta = 128 + i8 * 32 + 15;
				int l8 = 600 + theta * 3;
				int i9 = Rasterizer.SINE[theta];
				ai[i8] = l8 * i9 >> 16;
			}

			SceneGraph.method310(500, 800, 512, 334, ai);
			MessageCensor.init(chat);
			mouseCapturer = new MouseCapturer(this);
			startRunnable(mouseCapturer, 10);
			RenderableObject.client = this;
			ObjectDefinition.client = this;
			NpcDefinition.client = this;
		} catch (Exception exception) {
			error = true;
			SignLink.reportError("loaderror " + loadingScreenText + " " + anInt1079);
		}
	}

	public final void loadNextRegion() {
		if (lowMemory && loadingStage == 2 && MapRegion.currentPlane != plane) {
			aClass15_1165.initializeRasterizer();
			frameFont.renderCentre(257, 151, "Loading - please wait.", 0);
			frameFont.renderCentre(256, 150, "Loading - please wait.", 0xFFFFFF);
			aClass15_1165.drawImage(super.graphics, 4, 4);
			loadingStage = 1;
			loadingStartTime = System.currentTimeMillis();
		}

		if (loadingStage == 1) {
			int j = method54();
			if (j != 0 && System.currentTimeMillis() - loadingStartTime > 0x57e40) {
				SignLink.reportError(username + " glcfb " + serverSeed + "," + j + "," + lowMemory + "," + indices[0] + ","
						+ provider.remaining() + "," + plane + "," + regionX + "," + regionY);
				loadingStartTime = System.currentTimeMillis();
			}
		}

		if (loadingStage == 2 && plane != anInt985) {
			anInt985 = plane;
			method24(plane);
		}
	}

	public final void login(String name, String password, boolean reconnecting) {
		SignLink.setError(name);
		try {
			if (!reconnecting) {
				firstLoginMessage = "";
				secondLoginMessage = "Connecting to server...";
				drawLoginScreen(true);
			}

			primary = new BufferedConnection(this, openSocket(43594 + portOffset));
			long encoded = StringUtils.encodeBase37(name);
			int nameHash = (int) (encoded >> 16 & 31L);
			outgoing.setPosition(0);
			outgoing.writeByte(14);
			outgoing.writeByte(nameHash);
			primary.write(outgoing.getPayload(), 2, 0);
			for (int i = 0; i < 8; i++) {
				primary.read();
			}

			int response = primary.read();
			int copy = response;
			if (response == 0) {
				primary.read(incoming.getPayload(), 0, 8);
				incoming.setPosition(0);
				serverSeed = incoming.readLong();
				int[] seed = new int[4];
				seed[0] = (int) (Math.random() * 99999999D);
				seed[1] = (int) (Math.random() * 99999999D);
				seed[2] = (int) (serverSeed >> 32);
				seed[3] = (int) serverSeed;

				outgoing.setPosition(0);
				outgoing.writeByte(10); // Secure id
				outgoing.writeInt(seed[0]);
				outgoing.writeInt(seed[1]);
				outgoing.writeInt(seed[2]);
				outgoing.writeInt(seed[3]);
				outgoing.writeInt(SignLink.getUid());
				outgoing.writeJString(name);
				outgoing.writeJString(password);
				outgoing.encodeRSA(RSA_EXPONENT, RSA_MODULUS);
				login.setPosition(0);

				if (reconnecting) {
					login.writeByte(18);
				} else {
					login.writeByte(16);
				}

				login.writeByte(outgoing.getPosition() + 36 + 1 + 1 + 2);
				login.writeByte(255); // magic number
				login.writeShort(317); // revision
				login.writeByte(lowMemory ? 1 : 0);
				for (int index = 0; index < 9; index++) {
					login.writeInt(archiveCRCs[index]);
				}

				login.writeBytes(outgoing.getPayload(), 0, outgoing.getPosition());
				outgoing.setEncryption(new IsaacCipher(seed));
				for (int index = 0; index < 4; index++) {
					seed[index] += 50;
				}

				decryption = new IsaacCipher(seed);
				primary.write(login.getPayload(), login.getPosition(), 0);
				response = primary.read();
			}

			if (response == 1) {
				try {
					Thread.sleep(2000L);
				} catch (Exception _ex) {
				}

				login(name, password, reconnecting);
			} else if (response == 2) {
				playerPrivelage = primary.read();
				flaggedAccount = primary.read() == 1;
				aLong1220 = 0L;
				duplicateClickCount = 0;
				mouseCapturer.setCapturedCoordinateCount(0);
				super.hasFocus = true;
				wasFocused = true;
				loggedIn = true;
				outgoing.setPosition(0);
				incoming.setPosition(0);
				opcode = -1;
				lastOpcode = -1;
				secondLastOpcode = -1;
				thirdLastOpcode = -1;
				packetSize = 0;
				timeoutCounter = 0;
				systemUpdateTime = 0;
				anInt1011 = 0;
				hintIconDrawType = 0;
				menuActionRow = 0;
				menuOpen = false;
				super.timeIdle = 0;
				for (int index = 0; index < 100; index++) {
					chatMessages[index] = null;
				}

				itemSelected = 0;
				widgetSelected = 0;
				loadingStage = 0;
				trackCount = 0;
				anInt1278 = (int) (Math.random() * 100D) - 50;
				anInt1131 = (int) (Math.random() * 110D) - 55;
				anInt896 = (int) (Math.random() * 80D) - 40;
				anInt1209 = (int) (Math.random() * 120D) - 60;
				anInt1170 = (int) (Math.random() * 30D) - 20;
				cameraYaw = (int) (Math.random() * 20D) - 10 & 0x7ff;
				minimapState = 0;
				anInt985 = -1;
				destinationX = 0;
				destinationY = 0;
				playerCount = 0;
				npcCount = 0;
				for (int index = 0; index < maximumPlayers; index++) {
					players[index] = null;
					playerSynchronizationBuffers[index] = null;
				}

				for (int index = 0; index < 16384; index++) {
					npcs[index] = null;
				}

				localPlayer = players[internalLocalPlayerIndex] = new Player();
				projectiles.clear();
				incompleteAnimables.clear();
				for (int z = 0; z < 4; z++) {
					for (int x = 0; x < 104; x++) {
						for (int y = 0; y < 104; y++) {
							groundItems[z][x][y] = null;
						}
					}
				}

				spawns = new Deque();
				friendServerStatus = 0;
				friendCount = 0;
				dialogueId = -1;
				backDialogueId = -1;
				openInterfaceId = -1;
				overlayInterfaceId = -1;
				openWalkableInterface = -1;
				aBoolean1149 = false;
				tabId = 3;
				inputDialogueState = 0;
				menuOpen = false;
				messagePromptRaised = false;
				clickToContinueString = null;
				multicombat = 0;
				flashingSidebarId = -1;
				maleAvatar = true;
				changeCharacterGender();
				for (int index = 0; index < 5; index++) {
					characterDesignColours[index] = 0;
				}

				for (int index = 0; index < 5; index++) {
					aStringArray1127[index] = null;
					aBooleanArray1128[index] = false;
				}

				anInt1175 = 0;
				anInt1134 = 0;
				anInt986 = 0;
				anInt1288 = 0;
				anInt924 = 0;
				anInt1188 = 0;
				anInt1155 = 0;
				anInt1226 = 0;
				drawScreen();
			} else if (response == 3) {
				firstLoginMessage = "";
				secondLoginMessage = "Invalid username or password.";
			} else if (response == 4) {
				firstLoginMessage = "Your account has been disabled.";
				secondLoginMessage = "Please check your message-centre for details.";
			} else if (response == 5) {
				firstLoginMessage = "Your account is already logged in.";
				secondLoginMessage = "Try again in 60 secs...";
			} else if (response == 6) {
				firstLoginMessage = "RuneScape has been updated!";
				secondLoginMessage = "Please reload this page.";
			} else if (response == 7) {
				firstLoginMessage = "This world is full.";
				secondLoginMessage = "Please use a different world.";
			} else if (response == 8) {
				firstLoginMessage = "Unable to connect.";
				secondLoginMessage = "Login server offline.";
			} else if (response == 9) {
				firstLoginMessage = "Login limit exceeded.";
				secondLoginMessage = "Too many connections from your address.";
			} else if (response == 10) {
				firstLoginMessage = "Unable to connect.";
				secondLoginMessage = "Bad session id.";
			} else if (response == 11) {
				secondLoginMessage = "Login server rejected session.";
				secondLoginMessage = "Please try again.";
			} else if (response == 12) {
				firstLoginMessage = "You need a members account to login to this world.";
				secondLoginMessage = "Please subscribe, or use a different world.";
			} else if (response == 13) {
				firstLoginMessage = "Could not complete login.";
				secondLoginMessage = "Please try using a different world.";
			} else if (response == 14) {
				firstLoginMessage = "The server is being updated.";
				secondLoginMessage = "Please wait 1 minute and try again.";
			} else if (response == 15) {
				loggedIn = true;
				outgoing.setPosition(0);
				incoming.setPosition(0);
				opcode = -1;
				lastOpcode = -1;
				secondLastOpcode = -1;
				thirdLastOpcode = -1;
				packetSize = 0;
				timeoutCounter = 0;
				systemUpdateTime = 0;
				menuActionRow = 0;
				menuOpen = false;
				loadingStartTime = System.currentTimeMillis();
			} else if (response == 16) {
				firstLoginMessage = "Login attempts exceeded.";
				secondLoginMessage = "Please wait 1 minute and try again.";
			} else if (response == 17) {
				firstLoginMessage = "You are standing in a members-only area.";
				secondLoginMessage = "To play on this world move to a free area first";
			} else if (response == 20) {
				firstLoginMessage = "Invalid loginserver requested";
				secondLoginMessage = "Please try using a different world.";
			} else if (response == 21) {
				for (int time = primary.read(); time >= 0; time--) {
					firstLoginMessage = "You have only just left another world";
					secondLoginMessage = "Your profile will be transferred in: " + time + " seconds";
					drawLoginScreen(true);
					try {
						Thread.sleep(1000L);
					} catch (Exception _ex) {
					}
				}

				login(name, password, reconnecting);
			} else if (response == -1) {
				if (copy == 0) {
					if (loginFailures < 2) {
						try {
							Thread.sleep(2000L);
						} catch (Exception _ex) {
						}
						loginFailures++;
						login(name, password, reconnecting);
						return;
					}

					firstLoginMessage = "No response from loginserver";
					secondLoginMessage = "Please wait 1 minute and try again.";
					return;
				}

				firstLoginMessage = "No response from server";
				secondLoginMessage = "Please try using a different world.";
				return;
			}

			firstLoginMessage = "Unexpected server response";
			secondLoginMessage = "Please try using a different world.";
			return;
		} catch (IOException ex) {
			firstLoginMessage = "";
		}

		secondLoginMessage = "Error connecting to server.";
	}

	@Override
	public final void method10() {
		aBoolean1255 = true;
	}

	public final void method100(Mob mob) {
		if (mob.rotation == 0) {
			return;
		}

		if (mob.interactingMob != -1 && mob.interactingMob < 32768) {
			Npc npc = npcs[mob.interactingMob];
			if (npc != null) {
				int x = mob.worldX - npc.worldX;
				int y = mob.worldY - npc.worldY;
				if (x != 0 || y != 0) {
					mob.nextStepOrientation = (int) (Math.atan2(x, y) * 325.949D) & 0x7ff;
				}
			}
		}

		if (mob.interactingMob >= 32768) {
			int index = mob.interactingMob - 32768;
			if (index == localPlayerIndex) {
				index = internalLocalPlayerIndex;
			}

			Player player = players[index];
			if (player != null) {
				int x = mob.worldX - player.worldX;
				int y = mob.worldY - player.worldY;
				if (x != 0 || y != 0) {
					mob.nextStepOrientation = (int) (Math.atan2(x, y) * 325.949D) & 0x7ff;
				}
			}
		}

		if ((mob.faceX != 0 || mob.faceY != 0) && (mob.remainingPath == 0 || mob.anInt1503 > 0)) {
			int x = mob.worldX - (mob.faceX - regionBaseX - regionBaseX) * 64;
			int y = mob.worldY - (mob.faceY - regionBaseY - regionBaseY) * 64;

			if (x != 0 || y != 0) {
				mob.nextStepOrientation = (int) (Math.atan2(x, y) * 325.949D) & 0x7ff;
			}

			mob.faceX = 0;
			mob.faceY = 0;
		}

		int yawDelta = mob.nextStepOrientation - mob.orientation & 0x7ff;
		if (yawDelta != 0) {
			if (yawDelta < mob.rotation || yawDelta > 0x800 - mob.rotation) {
				mob.orientation = mob.nextStepOrientation;
			} else if (yawDelta > 0x400) {
				mob.orientation -= mob.rotation;
			} else {
				mob.orientation += mob.rotation;
			}
			mob.orientation &= 0x7ff;

			if (mob.movementAnimation == mob.idleAnimation && mob.orientation != mob.nextStepOrientation) {
				mob.movementAnimation = (mob.turnAnimation != -1) ? mob.turnAnimation : mob.walkingAnimation;
			}
		}
	}

	public final void drawWidget(Widget widget, int x, int y, int scroll) {
		if (widget.group != 0 || widget.children == null) {
			return;
		} else if (widget.hidden && anInt1026 != widget.id && anInt1048 != widget.id && anInt1039 != widget.id) {
			return;
		}

		int clipLeft = Raster.getClipLeft();
		int clipBottom = Raster.getClipBottom();
		int clipRight = Raster.getClipRight();
		int clipTop = Raster.getClipTop();
		Raster.setBounds(y + widget.height, x, x + widget.width, y);
		int children = widget.children.length;

		for (int childIndex = 0; childIndex < children; childIndex++) {
			int currentX = widget.childX[childIndex] + x;
			int currentY = widget.childY[childIndex] + y - scroll;
			Widget child = Widget.widgets[widget.children[childIndex]];

			currentX += child.horizontalDrawOffset;
			currentY += child.verticalDrawOffset;
			if (child.contentType > 0) {
				method75(child);
			}

			if (child.group == Widget.TYPE_CONTAINER) {
				if (child.scrollPosition > child.scrollLimit - child.height) {
					child.scrollPosition = child.scrollLimit - child.height;
				}
				if (child.scrollPosition < 0) {
					child.scrollPosition = 0;
				}

				drawWidget(child, currentX, currentY, child.scrollPosition);
				if (child.scrollLimit > child.height) {
					drawScrollbar(child.height, child.scrollPosition, currentY, currentX + child.width, child.scrollLimit);
				}
			} else if (child.group != Widget.TYPE_MODEL_LIST) {
				if (child.group == Widget.TYPE_INVENTORY) {
					int item = 0;

					for (int childY = 0; childY < child.height; childY++) {
						for (int childX = 0; childX < child.width; childX++) {
							int componentX = currentX + childX * (32 + child.spritePaddingX);
							int componentY = currentY + childY * (32 + child.spritePaddingY);

							if (item < 20) {
								componentX += child.spriteX[item];
								componentY += child.spriteY[item];
							}

							if (child.inventoryIds[item] > 0) {
								int dx = 0;
								int dy = 0;
								int itemId = child.inventoryIds[item] - 1;
								if (componentX > Raster.getClipLeft() - 32 && componentX < Raster.getClipRight()
										&& componentY > Raster.getClipBottom() - 32 && componentY < Raster.getClipTop()
										|| anInt1086 != 0 && selectedInventorySlot == item) {
									int colour = 0;
									if (itemSelected == 1 && anInt1283 == item && anInt1284 == child.id) {
										colour = 0xffffff;
									}

									Sprite sprite = ItemDefinition.sprite(itemId, child.inventoryAmounts[item], colour);
									if (sprite != null) {
										if (anInt1086 != 0 && selectedInventorySlot == item && modifiedWidgetId == child.id) {
											dx = super.mouseEventX - anInt1087;
											dy = super.mouseEventY - anInt1088;
											if (dx < 5 && dx > -5) {
												dx = 0;
											}
											if (dy < 5 && dy > -5) {
												dy = 0;
											}
											if (anInt989 < 5) {
												dx = 0;
												dy = 0;
											}

											sprite.drawSprite(componentX + dx, componentY + dy, 128);
											if (componentY + dy < Raster.getClipBottom() && widget.scrollPosition > 0) {
												int difference = tickDelta * (Raster.getClipBottom() - componentY - dy) / 3;
												if (difference > tickDelta * 10) {
													difference = tickDelta * 10;
												}
												if (difference > widget.scrollPosition) {
													difference = widget.scrollPosition;
												}
												widget.scrollPosition -= difference;
												anInt1088 += difference;
											}

											if (componentY + dy + 32 > Raster.getClipTop()
													&& widget.scrollPosition < widget.scrollLimit - widget.height) {
												int difference = tickDelta * (componentY + dy + 32 - Raster.getClipTop()) / 3;
												if (difference > tickDelta * 10) {
													difference = tickDelta * 10;
												}
												if (difference > widget.scrollLimit - widget.height - widget.scrollPosition) {
													difference = widget.scrollLimit - widget.height - widget.scrollPosition;
												}
												widget.scrollPosition += difference;
												anInt1088 -= difference;
											}
										} else if (anInt1246 != 0 && anInt1245 == item && anInt1244 == child.id) {
											sprite.drawSprite(componentX, componentY, 128);
										} else {
											sprite.drawSprite(componentX, componentY);
										}
										if (sprite.getResizeWidth() == 33 || child.inventoryAmounts[item] != 1) {
											int amount = child.inventoryAmounts[item];
											smallFont.render(componentX + 1 + dx, componentY + 10 + dy,
													getShortenedAmountText(amount), 0);
											smallFont.render(componentX + dx, componentY + 9 + dy,
													getShortenedAmountText(amount), 0xffff00);
										}
									}
								}
							} else if (child.sprites != null && item < 20) {
								Sprite sprite = child.sprites[item];
								if (sprite != null) {
									sprite.drawSprite(componentX, componentY);
								}
							}
							item++;
						}
					}
				} else if (child.group == Widget.TYPE_RECTANGLE) {
					boolean hover = false;
					if (anInt1039 == child.id || anInt1048 == child.id || anInt1026 == child.id) {
						hover = true;
					}

					int colour;
					if (scriptStateChanged(child)) {
						colour = child.secondaryColour;
						if (hover && child.secondaryHoverColour != 0) {
							colour = child.secondaryHoverColour;
						}
					} else {
						colour = child.defaultColour;
						if (hover && child.defaultHoverColour != 0) {
							colour = child.defaultHoverColour;
						}
					}

					if (child.alpha == 0) {
						if (child.filled) {
							Raster.fillRectangle(currentX, currentY, child.width, child.height, colour);
						} else {
							Raster.drawRectangle(currentX, currentY, child.width, child.height, colour);
						}
					} else if (child.filled) {
						Raster.fillRectangle(currentX, currentY, child.width, child.height, colour, 256 - (child.alpha & 0xff));
					} else {
						Raster.drawRectangle(currentX, currentY, child.width, child.height, colour, 256 - (child.alpha & 0xff));
					}
				} else if (child.group == Widget.TYPE_TEXT) {
					Font font = child.font;
					String text = child.defaultText;
					boolean hover = (anInt1039 == child.id || anInt1048 == child.id || anInt1026 == child.id);

					int colour;

					if (scriptStateChanged(child)) {
						colour = child.secondaryColour;
						if (hover && child.secondaryHoverColour != 0) {
							colour = child.secondaryHoverColour;
						}

						if (child.secondaryText.length() > 0) {
							text = child.secondaryText;
						}
					} else {
						colour = child.defaultColour;
						if (hover && child.defaultHoverColour != 0) {
							colour = child.defaultHoverColour;
						}
					}

					if (child.optionType == Widget.OPTION_CONTINUE && aBoolean1149) {
						text = "Please wait...";
						colour = child.defaultColour;
					}

					if (Raster.width == 479) {
						if (colour == 0xffff00) {
							colour = 255;
						} else if (colour == 49152) {
							colour = 0xffffff;
						}
					}

					for (int drawY = currentY + font.getVerticalSpace(); text.length() > 0; drawY += font.getVerticalSpace()) {
						if (text.indexOf("%") != -1) {
							do {
								int index = text.indexOf("%1");
								if (index == -1) {
									break;
								}

								text = text.substring(0, index) + getDisplayableAmount(executeScript(child, 0))
										+ text.substring(index + 2);
							} while (true);
							do {
								int index = text.indexOf("%2");
								if (index == -1) {
									break;
								}

								text = text.substring(0, index) + getDisplayableAmount(executeScript(child, 1))
										+ text.substring(index + 2);
							} while (true);
							do {
								int index = text.indexOf("%3");
								if (index == -1) {
									break;
								}

								text = text.substring(0, index) + getDisplayableAmount(executeScript(child, 2))
										+ text.substring(index + 2);
							} while (true);
							do {
								int index = text.indexOf("%4");
								if (index == -1) {
									break;
								}

								text = text.substring(0, index) + getDisplayableAmount(executeScript(child, 3))
										+ text.substring(index + 2);
							} while (true);
							do {
								int index = text.indexOf("%5");
								if (index == -1) {
									break;
								}

								text = text.substring(0, index) + getDisplayableAmount(executeScript(child, 4))
										+ text.substring(index + 2);
							} while (true);
						}

						int line = text.indexOf("\\n");
						String drawn;
						if (line != -1) {
							drawn = text.substring(0, line);
							text = text.substring(line + 2);
						} else {
							drawn = text;
							text = "";
						}

						if (child.centeredText) {
							font.shadowCentre(currentX + child.width / 2, drawY, drawn, child.shadowedText, colour);
						} else {
							font.shadow(currentX, drawY, drawn, child.shadowedText, colour);
						}
					}
				} else if (child.group == Widget.TYPE_SPRITE) {
					Sprite sprite = scriptStateChanged(child) ? child.secondarySprite : child.defaultSprite;

					if (sprite != null) {
						sprite.drawSprite(currentX, currentY);
					}
				} else if (child.group == Widget.TYPE_MODEL) {
					int originX = Rasterizer.originViewX;
					int originY = Rasterizer.originViewY;
					Rasterizer.originViewX = currentX + child.width / 2;
					Rasterizer.originViewY = currentY + child.height / 2;
					int sine = Rasterizer.SINE[child.spritePitch] * child.spriteScale >> 16;
					int cosine = Rasterizer.COSINE[child.spritePitch] * child.spriteScale >> 16;
					boolean updated = scriptStateChanged(child);
					int id = updated ? child.secondaryAnimationId : child.defaultAnimationId;

					Model model;
					if (id == -1) {
						model = child.getAnimatedModel(-1, -1, updated);
					} else {
						Animation animation = Animation.animations[id];
						model = child.getAnimatedModel(animation.getPrimaryFrame(child.currentFrame),
								animation.getSecondaryFrame(child.currentFrame), updated);
					}

					if (model != null) {
						model.render(0, child.spriteRoll, 0, child.spritePitch, 0, sine, cosine);
					}

					Rasterizer.originViewX = originX;
					Rasterizer.originViewY = originY;
				} else if (child.group == Widget.TYPE_ITEM_LIST) {
					Font font = child.font;
					int slot = 0;
					for (int dy = 0; dy < child.height; dy++) {
						for (int dx = 0; dx < child.width; dx++) {
							if (child.inventoryIds[slot] > 0) {
								ItemDefinition definition = ItemDefinition.lookup(child.inventoryIds[slot] - 1);
								String name = definition.getName();

								if (definition.isStackable() || child.inventoryAmounts[slot] != 1) {
									name = name + " x" + getFullAmountText(child.inventoryAmounts[slot]);
								}

								int drawX = currentX + dx * (115 + child.spritePaddingX);
								int drawY = currentY + dy * (12 + child.spritePaddingY);

								if (child.centeredText) {
									font.shadowCentre(drawX + child.width / 2, drawY, name, child.shadowedText,
											child.defaultColour);
								} else {
									font.shadow(drawX, drawY, name, child.shadowedText, child.defaultColour);
								}
							}
							slot++;
						}
					}
				}
			}
		}

		Raster.setBounds(clipTop, clipLeft, clipRight, clipBottom);
	}

	public final void method106(IndexedImage image) {
		int j = 256;
		for (int k = 0; k < anIntArray1190.length; k++) {
			anIntArray1190[k] = 0;
		}

		for (int l = 0; l < 5000; l++) {
			int i1 = (int) (Math.random() * 128D * j);
			anIntArray1190[i1] = (int) (Math.random() * 256D);
		}

		for (int j1 = 0; j1 < 20; j1++) {
			for (int k1 = 1; k1 < j - 1; k1++) {
				for (int i2 = 1; i2 < 127; i2++) {
					int k2 = i2 + (k1 << 7);
					anIntArray1191[k2] = (anIntArray1190[k2 - 1] + anIntArray1190[k2 + 1] + anIntArray1190[k2 - 128] + anIntArray1190[k2 + 128]) / 4;
				}

			}

			int ai[] = anIntArray1190;
			anIntArray1190 = anIntArray1191;
			anIntArray1191 = ai;
		}

		if (image != null) {
			int l1 = 0;

			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					if (image.getRaster()[l1++] != 0) {
						int i3 = x + 16 + image.getDrawOffsetX();
						int j3 = y + 16 + image.getDrawOffsetY();
						int k3 = i3 + (j3 << 7);
						anIntArray1190[k3] = 0;
					}
				}
			}
		}
	}

	public final void method108() {
		try {
			int j = localPlayer.worldX + anInt1278;
			int k = localPlayer.worldY + anInt1131;

			if (anInt1014 - j < -500 || anInt1014 - j > 500 || anInt1015 - k < -500 || anInt1015 - k > 500) {
				anInt1014 = j;
				anInt1015 = k;
			}

			if (anInt1014 != j) {
				anInt1014 += (j - anInt1014) / 16;
			}

			if (anInt1015 != k) {
				anInt1015 += (k - anInt1015) / 16;
			}

			if (super.keyStatuses[1] == 1) {
				anInt1186 += (-24 - anInt1186) / 2;
			} else if (super.keyStatuses[2] == 1) {
				anInt1186 += (24 - anInt1186) / 2;
			} else {
				anInt1186 /= 2;
			}

			if (super.keyStatuses[3] == 1) {
				anInt1187 += (12 - anInt1187) / 2;
			} else if (super.keyStatuses[4] == 1) {
				anInt1187 += (-12 - anInt1187) / 2;
			} else {
				anInt1187 /= 2;
			}
			cameraYaw = cameraYaw + anInt1186 / 2 & 0x7ff;
			cameraRoll += anInt1187 / 2;
			if (cameraRoll < 128) {
				cameraRoll = 128;
			}
			if (cameraRoll > 383) {
				cameraRoll = 383;
			}
			int l = anInt1014 >> 7;
			int i1 = anInt1015 >> 7;
			int j1 = method42(anInt1014, anInt1015, plane);
			int k1 = 0;
			if (l > 3 && i1 > 3 && l < 100 && i1 < 100) {
				for (int x = l - 4; x <= l + 4; x++) {
					for (int y = i1 - 4; y <= i1 + 4; y++) {
						int z = plane;
						if (z < 3 && (tileFlags[1][x][y] & MapRegion.BRIDGE_TILE) == 2) {
							z++;
						}
						int i3 = j1 - tileHeights[z][x][y];
						if (i3 > k1) {
							k1 = i3;
						}
					}
				}
			}

			anInt1005++;
			if (anInt1005 > 1512) {
				anInt1005 = 0;
				outgoing.writeOpcode(77);
				outgoing.writeByte(0);
				int i2 = outgoing.getPosition();
				outgoing.writeByte((int) (Math.random() * 256D));
				outgoing.writeByte(101);
				outgoing.writeByte(233);
				outgoing.writeShort(45092);
				if ((int) (Math.random() * 2D) == 0) {
					outgoing.writeShort(35784);
				}
				outgoing.writeByte((int) (Math.random() * 256D));
				outgoing.writeByte(64);
				outgoing.writeByte(38);
				outgoing.writeShort((int) (Math.random() * 65536D));
				outgoing.writeShort((int) (Math.random() * 65536D));
				outgoing.writeSizeByte(outgoing.getPosition() - i2);
			}
			int j2 = k1 * 192;
			if (j2 > 0x17f00) {
				j2 = 0x17f00;
			}
			if (j2 < 32768) {
				j2 = 32768;
			}
			if (j2 > anInt984) {
				anInt984 += (j2 - anInt984) / 24;
				return;
			}
			if (j2 < anInt984) {
				anInt984 += (j2 - anInt984) / 80;
				return;
			}
		} catch (Exception _ex) {
			SignLink.reportError("glfc_ex " + localPlayer.worldX + "," + localPlayer.worldY + "," + anInt1014 + "," + anInt1015
					+ "," + regionX + "," + regionY + "," + regionBaseX + "," + regionBaseY);
			throw new RuntimeException("eek");
		}
	}

	public final void method112() {
		drawChatMessages();
		if (anInt917 == 1) {
			crosses[anInt916 / 100].drawSprite(anInt914 - 8 - 4, anInt915 - 8 - 4);
			anInt1142++;
			if (anInt1142 > 67) {
				anInt1142 = 0;
				outgoing.writeOpcode(78);
			}
		}
		if (anInt917 == 2) {
			crosses[4 + anInt916 / 100].drawSprite(anInt914 - 8 - 4, anInt915 - 8 - 4);
		}
		if (openWalkableInterface != -1) {
			processWidgetAnimations(openWalkableInterface, tickDelta);
			drawWidget(Widget.widgets[openWalkableInterface], 0, 0, 0);
		}
		if (openInterfaceId != -1) {
			processWidgetAnimations(openInterfaceId, tickDelta);
			drawWidget(Widget.widgets[openInterfaceId], 0, 0, 0);
		}
		checkTutorialIsland();
		if (!menuOpen) {
			method82();
			method125();
		} else if (anInt948 == 0) {
			method40();
		}
		if (multicombat == 1) {
			headIcons[1].drawSprite(472, 296);
		}
		if (displayFps) {
			char c = '\u01FB';
			int k = 20;
			int i1 = 0xffff00;
			if (super.fps < 15) {
				i1 = 0xff0000;
			}
			frameFont.renderLeft(c, k, "Fps:" + super.fps, i1);
			k += 15;
			Runtime runtime = Runtime.getRuntime();
			int memory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024);
			i1 = 0xffff00;
			if (memory > 0x2000000 && lowMemory) {
				i1 = 0xff0000;
			}
			frameFont.renderLeft(c, k, "Mem:" + memory + "k", 0xffff00);
			k += 15;
		}
		if (systemUpdateTime != 0) {
			int seconds = systemUpdateTime / 50;
			int minutes = seconds / 60;
			seconds %= 60;

			if (seconds < 10) {
				frameFont.render(4, 329, "System update in: " + minutes + ":0" + seconds, 0xffff00);
			} else {
				frameFont.render(4, 329, "System update in: " + minutes + ":" + seconds, 0xffff00);
			}

			anInt849++;
			if (anInt849 > 75) {
				anInt849 = 0;
				outgoing.writeOpcode(148);
			}
		}
	}

	public final void method116() {
		int width = bold.getColouredTextWidth("Choose Option");
		for (int index = 0; index < menuActionRow; index++) {
			int actionWidth = bold.getColouredTextWidth(menuActionTexts[index]);

			if (actionWidth > width) {
				width = actionWidth;
			}
		}

		width += 8;
		int l = 15 * menuActionRow + 21;
		if (super.lastClickX > 4 && super.lastClickY > 4 && super.lastClickX < 516 && super.lastClickY < 338) {
			int x = super.lastClickX - 4 - width / 2;
			if (x + width > 512) {
				x = 512 - width;
			}
			if (x < 0) {
				x = 0;
			}

			int y = super.lastClickY - 4;
			if (y + l > 334) {
				y = 334 - l;
			}
			if (y < 0) {
				y = 0;
			}

			menuOpen = true;
			anInt948 = 0;
			menuClickX = x;
			menuClickY = y;
			anInt951 = width;
			anInt952 = 15 * menuActionRow + 22;
		}
		if (super.lastClickX > 553 && super.lastClickY > 205 && super.lastClickX < 743 && super.lastClickY < 466) {
			int x = super.lastClickX - 553 - width / 2;
			if (x < 0) {
				x = 0;
			} else if (x + width > 190) {
				x = 190 - width;
			}
			int y = super.lastClickY - 205;
			if (y < 0) {
				y = 0;
			} else if (y + l > 261) {
				y = 261 - l;
			}
			menuOpen = true;
			anInt948 = 1;
			menuClickX = x;
			menuClickY = y;
			anInt951 = width;
			anInt952 = 15 * menuActionRow + 22;
		}
		if (super.lastClickX > 17 && super.lastClickY > 357 && super.lastClickX < 496 && super.lastClickY < 453) {
			int x = super.lastClickX - 17 - width / 2;
			if (x < 0) {
				x = 0;
			} else if (x + width > 479) {
				x = 479 - width;
			}
			int y = super.lastClickY - 357;
			if (y < 0) {
				y = 0;
			} else if (y + l > 96) {
				y = 96 - l;
			}
			menuOpen = true;
			anInt948 = 2;
			menuClickX = x;
			menuClickY = y;
			anInt951 = width;
			anInt952 = 15 * menuActionRow + 22;
		}
	}

	public final void method118() {
		aBoolean831 = false;
		while (aBoolean962) {
			aBoolean831 = false;
			try {
				Thread.sleep(50L);
			} catch (Exception ex) {
			}
		}

		titleBox = null;
		titleButton = null;
		runes = null;
		anIntArray850 = null;
		anIntArray851 = null;
		anIntArray852 = null;
		anIntArray853 = null;
		anIntArray1190 = null;
		anIntArray1191 = null;
		anIntArray828 = null;
		anIntArray829 = null;
		aClass30_Sub2_Sub1_Sub1_1201 = null;
		aClass30_Sub2_Sub1_Sub1_1202 = null;
	}

	public final int method120() {
		int z = 3;
		if (anInt861 < 310) {
			int x = anInt858 >> 7;
			int y = anInt860 >> 7;
			int localX = localPlayer.worldX >> 7;
			int localY = localPlayer.worldY >> 7;
			if ((tileFlags[plane][x][y] & 4) != 0) {
				z = plane;
			}

			int dx;
			if (localX > x) {
				dx = localX - x;
			} else {
				dx = x - localX;
			}

			int dy;
			if (localY > y) {
				dy = localY - y;
			} else {
				dy = y - localY;
			}

			if (dx > dy) {
				int i2 = dy * 0x10000 / dx;
				int k2 = 32768;
				while (x != localX) {
					if (x < localX) {
						x++;
					} else if (x > localX) {
						x--;
					}
					if ((tileFlags[plane][x][y] & 4) != 0) {
						z = plane;
					}
					k2 += i2;
					if (k2 >= 0x10000) {
						k2 -= 0x10000;
						if (y < localY) {
							y++;
						} else if (y > localY) {
							y--;
						}
						if ((tileFlags[plane][x][y] & 4) != 0) {
							z = plane;
						}
					}
				}
			} else {
				int j2 = dx * 0x10000 / dy;
				int l2 = 32768;
				while (y != localY) {
					if (y < localY) {
						y++;
					} else if (y > localY) {
						y--;
					}
					if ((tileFlags[plane][x][y] & 4) != 0) {
						z = plane;
					}
					l2 += j2;
					if (l2 >= 0x10000) {
						l2 -= 0x10000;
						if (x < localX) {
							x++;
						} else if (x > localX) {
							x--;
						}
						if ((tileFlags[plane][x][y] & 4) != 0) {
							z = plane;
						}
					}
				}
			}
		}
		if ((tileFlags[plane][localPlayer.worldX >> 7][localPlayer.worldY >> 7] & 4) != 0) {
			z = plane;
		}
		return z;
	}

	public final int method121() {
		int j = method42(anInt858, anInt860, plane);
		if (j - anInt859 < 800 && (tileFlags[plane][anInt858 >> 7][anInt860 >> 7] & 4) != 0) {
			return plane;
		}
		return 3;
	}

	public final void method125() {
		if (menuActionRow < 2 && itemSelected == 0 && widgetSelected == 0) {
			return;
		}

		String option;
		if (itemSelected == 1 && menuActionRow < 2) {
			option = "Use " + selectedItemName + " with...";
		} else if (widgetSelected == 1 && menuActionRow < 2) {
			option = selectedWidgetName + "...";
		} else {
			option = menuActionTexts[menuActionRow - 1];
		}

		if (menuActionRow > 2) {
			option = option + "@whi@ / " + (menuActionRow - 2) + " more options";
		}
		bold.renderRandom(option, 4, 15, 0xffffff, true, tick / 1000);
	}

	public final void method126() {
		aClass15_1164.initializeRasterizer();
		if (minimapState == 2) {
			byte map[] = mapBackground.getRaster();
			int[] raster = Raster.raster;
			int k2 = map.length;
			for (int i5 = 0; i5 < k2; i5++) {
				if (map[i5] == 0) {
					raster[i5] = 0;
				}
			}

			compass.method352(33, cameraYaw, anIntArray1057, 256, anIntArray968, 25, 0, 0, 33, 25);
			aClass15_1165.initializeRasterizer();
			return;
		}

		int i = cameraYaw + anInt1209 & 0x7ff;
		int j = 48 + localPlayer.worldX / 32;
		int l2 = 464 - localPlayer.worldY / 32;
		aClass30_Sub2_Sub1_Sub1_1263.method352(151, i, anIntArray1229, 256 + anInt1170, anIntArray1052, l2, 5, 25, 146, j);
		compass.method352(33, cameraYaw, anIntArray1057, 256, anIntArray968, 25, 0, 0, 33, 25);

		for (int j5 = 0; j5 < anInt1071; j5++) {
			int k = anIntArray1072[j5] * 4 + 2 - localPlayer.worldX / 32;
			int i3 = anIntArray1073[j5] * 4 + 2 - localPlayer.worldY / 32;
			drawOnMinimap(aClass30_Sub2_Sub1_Sub1Array1140[j5], k, i3);
		}

		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				Deque items = groundItems[plane][x][y];
				if (items != null) {
					int drawX = x * 4 + 2 - localPlayer.worldX / 32;
					int drawY = y * 4 + 2 - localPlayer.worldY / 32;
					drawOnMinimap(itemMapdot, drawX, drawY);
				}
			}
		}

		for (int index = 0; index < npcCount; index++) {
			Npc npc = npcs[npcList[index]];
			if (npc != null && npc.isVisible()) {
				NpcDefinition definition = npc.getDefinition();
				if (definition.getMorphisms() != null) {
					definition = definition.morph();
				}

				if (definition != null && definition.drawMinimapDot() && definition.isClickable()) {
					int x = npc.worldX / 32 - localPlayer.worldX / 32;
					int y = npc.worldY / 32 - localPlayer.worldY / 32;
					drawOnMinimap(npcMapdot, x, y);
				}
			}
		}

		for (int j6 = 0; j6 < playerCount; j6++) {
			Player player = players[playerList[j6]];
			if (player != null && player.isVisible()) {
				int x = player.worldX / 32 - localPlayer.worldX / 32;
				int y = player.worldY / 32 - localPlayer.worldY / 32;
				boolean friend = false;
				long username = StringUtils.encodeBase37(player.name);
				for (int index = 0; index < friendCount; index++) {
					if (username != friends[index] || friendWorlds[index] == 0) {
						continue;
					}

					friend = true;
					break;
				}

				boolean team = localPlayer.team != 0 && localPlayer.team == player.team;

				if (friend) {
					drawOnMinimap(friendMapdot, x, y);
				} else if (team) {
					drawOnMinimap(teamMapdot, x, y);
				} else {
					drawOnMinimap(playerMapdot, x, y);
				}
			}
		}

		if (hintIconDrawType != 0 && tick % 20 < 10) {
			if (hintIconDrawType == 1 && hintedNpc >= 0 && hintedNpc < npcs.length) {
				Npc npc = npcs[hintedNpc];
				if (npc != null) {
					int k1 = npc.worldX / 32 - localPlayer.worldX / 32;
					int i4 = npc.worldY / 32 - localPlayer.worldY / 32;
					drawMinimap(secondMapmarker, i4, k1);
				}
			}
			if (hintIconDrawType == 2) {
				int l1 = (anInt934 - regionBaseX) * 4 + 2 - localPlayer.worldX / 32;
				int j4 = (anInt935 - regionBaseY) * 4 + 2 - localPlayer.worldY / 32;
				drawMinimap(secondMapmarker, j4, l1);
			}
			if (hintIconDrawType == 10 && lastInteractedWithPlayer >= 0 && lastInteractedWithPlayer < players.length) {
				Player player = players[lastInteractedWithPlayer];
				if (player != null) {
					int i2 = player.worldX / 32 - localPlayer.worldX / 32;
					int k4 = player.worldY / 32 - localPlayer.worldY / 32;
					drawMinimap(secondMapmarker, k4, i2);
				}
			}
		}
		if (destinationX != 0) {
			int j2 = destinationX * 4 + 2 - localPlayer.worldX / 32;
			int l4 = destinationY * 4 + 2 - localPlayer.worldY / 32;
			drawOnMinimap(firstMapmarker, j2, l4);
		}

		Raster.fillRectangle(97, 78, 3, 3, 0xffffff);
		aClass15_1165.initializeRasterizer();
	}

	public final void method127(Mob mob, int i) {
		method128(mob.worldX, i, mob.worldY);
	}

	public final void method128(int i, int j, int l) {
		if (i < 128 || l < 128 || i > 13056 || l > 13056) {
			spriteDrawX = -1;
			spriteDrawY = -1;
			return;
		}
		
		int i1 = method42(i, l, plane) - j;
		i -= anInt858;
		i1 -= anInt859;
		l -= anInt860;
		int j1 = Model.SINE[anInt861];
		int k1 = Model.COSINE[anInt861];
		int l1 = Model.SINE[anInt862];
		int i2 = Model.COSINE[anInt862];
		int j2 = l * l1 + i * i2 >> 16;
		l = l * i2 - i * l1 >> 16;
		i = j2;
		j2 = i1 * k1 - l * j1 >> 16;
		l = i1 * j1 + l * k1 >> 16;
		i1 = j2;

		if (l >= 50) {
			spriteDrawX = Rasterizer.originViewX + (i << 9) / l;
			spriteDrawY = Rasterizer.originViewY + (i1 << 9) / l;
		} else {
			spriteDrawX = -1;
			spriteDrawY = -1;
		}
	}

	public final void method129() {
		if (anInt1195 == 0) {
			return;
		}

		int messageSlot = 0;
		if (systemUpdateTime != 0) {
			messageSlot = 1;
		}

		for (int index = 0; index < 100; index++) {
			if (chatMessages[index] != null) {
				int type = chatTypes[index];
				String username = chatPlayerNames[index];
				if (username != null && username.startsWith("@cr1@")) {
					username = username.substring(5);
				} else if (username != null && username.startsWith("@cr2@")) {
					username = username.substring(5);
				}

				if ((type == 3 || type == 7)
						&& (type == 7 || privateChatMode == 0 || privateChatMode == 1 && displayMessageFrom(username))) {
					int y = 329 - messageSlot * 13;
					if (super.mouseEventX > 4 && super.mouseEventY > y - 6 && super.mouseEventY <= y + 7) {
						int width = frameFont.getColouredTextWidth("From:  " + username + chatMessages[index]) + 25;
						if (width > 450) {
							width = 450;
						}

						if (super.mouseEventX < 4 + width) {
							if (playerPrivelage >= 1) {
								menuActionTexts[menuActionRow] = "Report abuse @whi@" + username;
								menuActionTypes[menuActionRow] = 2606;
								menuActionRow++;
							}

							menuActionTexts[menuActionRow] = "Add ignore @whi@" + username;
							menuActionTypes[menuActionRow] = 2042;
							menuActionRow++;
							menuActionTexts[menuActionRow] = "Add friend @whi@" + username;
							menuActionTypes[menuActionRow] = 2337;
							menuActionRow++;
						}
					}
					
					if (++messageSlot >= 5) {
						return;
					}
				}
				
				if ((type == 5 || type == 6) && privateChatMode < 2 && ++messageSlot >= 5) {
					return;
				}
			}
		}
	}

	public final void method133() {
		int c = 256;
		if (anInt1040 > 0) {
			for (int i = 0; i < 256; i++) {
				if (anInt1040 > 768) {
					anIntArray850[i] = getNextFlameColour(anIntArray851[i], anIntArray852[i], 1024 - anInt1040);
				} else if (anInt1040 > 256) {
					anIntArray850[i] = anIntArray852[i];
				} else {
					anIntArray850[i] = getNextFlameColour(anIntArray852[i], anIntArray851[i], 256 - anInt1040);
				}
			}

		} else if (anInt1041 > 0) {
			for (int j = 0; j < 256; j++) {
				if (anInt1041 > 768) {
					anIntArray850[j] = getNextFlameColour(anIntArray851[j], anIntArray853[j], 1024 - anInt1041);
				} else if (anInt1041 > 256) {
					anIntArray850[j] = anIntArray853[j];
				} else {
					anIntArray850[j] = getNextFlameColour(anIntArray853[j], anIntArray851[j], 256 - anInt1041);
				}
			}

		} else {
			for (int k = 0; k < 256; k++) {
				anIntArray850[k] = anIntArray851[k];
			}

		}
		for (int l = 0; l < 33920; l++) {
			aClass15_1110.setPixel(l, aClass30_Sub2_Sub1_Sub1_1201.getPixel(l));;
		}

		int i1 = 0;
		int j1 = 1152;
		for (int k1 = 1; k1 < c - 1; k1++) {
			int l1 = anIntArray969[k1] * (c - k1) / c;
			int j2 = 22 + l1;
			if (j2 < 0) {
				j2 = 0;
			}
			i1 += j2;
			for (int l2 = j2; l2 < 128; l2++) {
				int j3 = anIntArray828[i1++];
				if (j3 != 0) {
					int l3 = j3;
					int j4 = 256 - j3;
					j3 = anIntArray850[j3];
					int current = aClass15_1110.getPixel(j1);
					aClass15_1110.setPixel(j1++, ((j3 & 0xff00ff) * l3 + (current & 0xff00ff) * j4 & 0xff00ff00)
							+ ((j3 & 0xff00) * l3 + (current & 0xff00) * j4 & 0xff0000) >> 8);
				} else {
					j1++;
				}
			}

			j1 += j2;
		}

		aClass15_1110.drawImage(super.graphics, 0, 0);
		for (int i2 = 0; i2 < 33920; i2++) {
			aClass15_1111.setPixel(i2, aClass30_Sub2_Sub1_Sub1_1202.getPixel(i2));
		}

		i1 = 0;
		j1 = 1176;
		for (int k2 = 1; k2 < c - 1; k2++) {
			int i3 = anIntArray969[k2] * (c - k2) / c;
			int k3 = 103 - i3;
			j1 += i3;
			for (int i4 = 0; i4 < k3; i4++) {
				int k4 = anIntArray828[i1++];
				if (k4 != 0) {
					int i5 = k4;
					int j5 = 256 - k4;
					k4 = anIntArray850[k4];
					int current = aClass15_1111.getPixel(j1);
					aClass15_1111.setPixel(j1++, ((k4 & 0xff00ff) * i5 + (current & 0xff00ff) * j5 & 0xff00ff00)
							+ ((k4 & 0xff00) * i5 + (current & 0xff00) * j5 & 0xff0000) >> 8);
				} else {
					j1++;
				}
			}

			i1 += 128 - k3;
			j1 += 128 - k3 - i3;
		}

		aClass15_1111.drawImage(super.graphics, 637, 0);
	}

	public final void method136() {
		aBoolean962 = true;
		long start = System.currentTimeMillis();
		int count = 0;
		int sleep = 20;

		while (aBoolean831) {
			flameTick++;
			method58();
			method58();
			method133();

			if (++count > 10) {
				long current = System.currentTimeMillis();
				int delta = (int) (current - start) / 10 - sleep;
				sleep = 40 - delta;
				if (sleep < 5) {
					sleep = 5;
				}
				count = 0;
				start = current;
			}

			try {
				Thread.sleep(sleep);
			} catch (InterruptedException ex) {
			}
		}

		aBoolean962 = false;
	}

	public final void method144(int j, int k, int l, int i1, int j1, int k1) {
		int l1 = 2048 - k & 0x7ff;
		int i2 = 2048 - j1 & 0x7ff;
		int j2 = 0;
		int k2 = 0;
		int l2 = j;

		if (l1 != 0) {
			int sin = Model.SINE[l1];
			int cos = Model.COSINE[l1];
			int i4 = k2 * cos - l2 * sin >> 16;
			l2 = k2 * sin + l2 * cos >> 16;
			k2 = i4;
		}

		if (i2 != 0) {
			int sin = Model.SINE[i2];
			int cos = Model.COSINE[i2];
			int j4 = l2 * sin + j2 * cos >> 16;
			l2 = l2 * cos - j2 * sin >> 16;
			j2 = j4;
		}

		anInt858 = l - j2;
		anInt859 = i1 - k2;
		anInt860 = k1 - l2;
		anInt861 = k;
		anInt862 = j1;
	}

	public final void method146() {
		anInt1265++;
		processPlayerAdditions(true);
		processNpcAdditions(true);
		processPlayerAdditions(false);
		processNpcAdditions(false);
		processProjectiles();
		processAnimableObjects();

		if (!oriented) {
			int i = cameraRoll;
			if (anInt984 / 256 > i) {
				i = anInt984 / 256;
			}
			if (aBooleanArray876[4] && anIntArray1203[4] + 128 > i) {
				i = anIntArray1203[4] + 128;
			}
			int k = cameraYaw + anInt896 & 0x7ff;
			method144(600 + i * 3, i, anInt1014, method42(localPlayer.worldX, localPlayer.worldY, plane) - 50, k, anInt1015);
		}

		int j;
		if (!oriented) {
			j = method120();
		} else {
			j = method121();
		}

		int l = anInt858;
		int i1 = anInt859;
		int j1 = anInt860;
		int k1 = anInt861;
		int l1 = anInt862;
		for (int i2 = 0; i2 < 5; i2++) {
			if (aBooleanArray876[i2]) {
				int j2 = (int) (Math.random() * (anIntArray873[i2] * 2 + 1) - anIntArray873[i2] + Math.sin(anIntArray1030[i2]
						* (anIntArray928[i2] / 100D))
						* anIntArray1203[i2]);

				if (i2 == 0) {
					anInt858 += j2;
				} else if (i2 == 1) {
					anInt859 += j2;
				} else if (i2 == 2) {
					anInt860 += j2;
				} else if (i2 == 3) {
					anInt862 = anInt862 + j2 & 0x7ff;
				} else if (i2 == 4) {
					anInt861 += j2;

					if (anInt861 < 128) {
						anInt861 = 128;
					} else if (anInt861 > 383) {
						anInt861 = 383;
					}
				}
			}
		}

		int k2 = Rasterizer.anInt1481;
		Model.aBoolean1684 = true;
		Model.anInt1687 = 0;
		Model.mouseX = super.mouseEventX - 4;
		Model.mouseY = super.mouseEventY - 4;
		Raster.reset();
		scene.method313(anInt858, anInt860, anInt862, anInt859, j, anInt861);
		scene.method288();
		updateMobs();
		drawHintIcon();
		method37(k2);
		method112();
		aClass15_1165.drawImage(super.graphics, 4, 4);
		anInt858 = l;
		anInt859 = i1;
		anInt860 = j1;
		anInt861 = k1;
		anInt862 = l1;
	}

	public final void method20() {
		if (anInt1086 != 0) {
			return;
		}

		int meta = super.lastMetaModifier;
		if (widgetSelected == 1 && lastClickX >= 516 && lastClickY >= 160 && lastClickX <= 765 && lastClickY <= 205) {
			meta = 0;
		}

		if (menuOpen) {
			if (meta != 1) {
				int x = mouseEventX;
				int y = mouseEventY;

				if (anInt948 == 0) {
					x -= 4;
					y -= 4;
				} else if (anInt948 == 1) {
					x -= 553;
					y -= 205;
				} else if (anInt948 == 2) {
					x -= 17;
					y -= 357;
				}

				if (x < menuClickX - 10 || x > menuClickX + anInt951 + 10 || y < menuClickY - 10
						|| y > menuClickY + anInt952 + 10) {
					menuOpen = false;

					if (anInt948 == 1) {
						redrawTabArea = true;
					} else if (anInt948 == 2) {
						redrawDialogueBox = true;
					}
				}
			}

			if (meta == 1) {
				int menuX = menuClickX;
				int menuY = menuClickY;
				int dx = anInt951;
				int x = super.lastClickX;
				int y = super.lastClickY;

				if (anInt948 == 0) {
					x -= 4;
					y -= 4;
				} else if (anInt948 == 1) {
					x -= 553;
					y -= 205;
				} else if (anInt948 == 2) {
					x -= 17;
					y -= 357;
				}

				int id = -1;
				for (int row = 0; row < menuActionRow; row++) {
					int k3 = menuY + 31 + (menuActionRow - 1 - row) * 15;
					if (x > menuX && x < menuX + dx && y > k3 - 13 && y < k3 + 3) {
						id = row;
					}
				}

				if (id != -1) {
					processMenuActions(id);
				}
				menuOpen = false;

				if (anInt948 == 1) {
					redrawTabArea = true;
				} else if (anInt948 == 2) {
					redrawDialogueBox = true;
					return;
				}
			}
		} else {
			if (meta == 1 && menuActionRow > 0) {
				int action = menuActionTypes[menuActionRow - 1];
				if (action == 632 || action == 78 || action == 867 || action == 431 || action == 53 || action == 74
						|| action == 454 || action == 539 || action == 493 || action == 847 || action == 447
						|| action == Actions.EXAMINE_ITEM) {
					int item = firstMenuOperand[menuActionRow - 1];
					int id = secondMenuOperand[menuActionRow - 1];
					Widget widget = Widget.widgets[id];

					if (widget.swappableItems || widget.replaceItems) {
						aBoolean1242 = false;
						anInt989 = 0;
						modifiedWidgetId = id;
						selectedInventorySlot = item;
						anInt1086 = 2;
						anInt1087 = super.lastClickX;
						anInt1088 = super.lastClickY;

						if (Widget.widgets[id].parent == openInterfaceId) {
							anInt1086 = 1;
						}
						if (Widget.widgets[id].parent == backDialogueId) {
							anInt1086 = 3;
						}
						return;
					}
				}
			}

			if (meta == 1 && (anInt1253 == 1 || isAddFriend(menuActionRow - 1)) && menuActionRow > 2) {
				meta = 2;
			}

			if (meta == 1 && menuActionRow > 0) {
				processMenuActions(menuActionRow - 1);
			} else if (meta == 2 && menuActionRow > 0) {
				method116();
			}
		}
	}

	public final void method22() {
		try {
			anInt985 = -1;
			incompleteAnimables.clear();
			projectiles.clear();
			Rasterizer.method366();
			unlinkCaches();
			scene.reset();
			System.gc();
			for (int plane = 0; plane < 4; plane++) {
				collisionMaps[plane].init();
			}

			for (int z = 0; z < 4; z++) {
				for (int x = 0; x < 104; x++) {
					for (int y = 0; y < 104; y++) {
						tileFlags[z][x][y] = 0;
					}
				}
			}

			MapRegion viewport = new MapRegion(tileFlags, 104, 104, tileHeights);
			int count = localRegionMapData.length;
			outgoing.writeOpcode(0);

			if (!constructedViewport) {
				for (int id = 0; id < count; id++) {
					int dX = (localRegionIds[id] >> 8) * 64 - regionBaseX;
					int dY = (localRegionIds[id] & 0xff) * 64 - regionBaseY;
					byte[] data = localRegionMapData[id];
					if (data != null) {
						viewport.decodeRegionMapData(data, dY, dX, (regionX - 6) * 8, (regionY - 6) * 8, collisionMaps);
					}
				}

				for (int i = 0; i < count; i++) {
					int x = (localRegionIds[i] >> 8) * 64 - regionBaseX;
					int y = (localRegionIds[i] & 0xff) * 64 - regionBaseY;
					byte[] data = localRegionMapData[i];
					if (data == null && regionY < 800) {
						viewport.method174(x, y, 64, 64);
					}
				}

				anInt1097++;
				if (anInt1097 > 160) {
					anInt1097 = 0;
					outgoing.writeOpcode(238);
					outgoing.writeByte(96);
				}

				outgoing.writeOpcode(0);
				for (int i = 0; i < count; i++) {
					byte[] landscape = localRegionLandscapeData[i];
					if (landscape != null) {
						int x = (localRegionIds[i] >> 8) * 64 - regionBaseX;
						int y = (localRegionIds[i] & 0xff) * 64 - regionBaseY;
						viewport.decodeLandscapes(collisionMaps, scene, landscape, x, y);
					}
				}
			}

			if (constructedViewport) {
				for (int z = 0; z < 4; z++) {
					for (int x = 0; x < 13; x++) {
						for (int y = 0; y < 13; y++) {
							int data = localRegions[z][x][y];

							if (data != -1) {
								int plane = data >> 24 & 3;
								int mapX = data >> 14 & 0x3ff;
								int mapY = data >> 3 & 0x7ff;
								int orientation = data >> 1 & 3;
								int regionId = (mapX / 8 << 8) + mapY / 8;

								for (int index = 0; index < localRegionIds.length; index++) {
									if (localRegionIds[index] != regionId || localRegionMapData[index] == null) {
										continue;
									}
									viewport.decodeConstructedMapData(plane, orientation, collisionMaps, x * 8, (mapX & 7) * 8,
											localRegionMapData[index], (mapY & 7) * 8, z, y * 8);
									break;
								}
							}
						}
					}
				}

				for (int regionX = 0; regionX < 13; regionX++) {
					for (int regionY = 0; regionY < 13; regionY++) {
						int region = localRegions[0][regionX][regionY];
						if (region == -1) {
							viewport.method174(regionX * 8, regionY * 8, 8, 8);
						}
					}
				}

				outgoing.writeOpcode(0);
				for (int collisionPlane = 0; collisionPlane < 4; collisionPlane++) {
					for (int localRegionX = 0; localRegionX < 13; localRegionX++) {
						for (int localRegionY = 0; localRegionY < 13; localRegionY++) {
							int data = localRegions[collisionPlane][localRegionX][localRegionY];

							if (data != -1) {
								int plane = data >> 24 & 3;
								int mapX = data >> 14 & 0x3ff; // absolute x coordinate in the region, [0, 63]
								int mapY = data >> 3 & 0x7ff; // absolute y coordinate in the region, [0, 63]
								int orientation = data >> 1 & 3;
								int regionId = (mapX / 8 << 8) | mapY / 8;

								for (int index = 0; index < localRegionIds.length; index++) {
									if (localRegionIds[index] != regionId || localRegionLandscapeData[index] == null) {
										continue;
									}

									viewport.decodeConstructedLandscapes(localRegionLandscapeData[index], collisionMaps, scene,
											plane, localRegionX * 8, localRegionY * 8, collisionPlane, (mapY & 7) * 8,
											(mapX & 7) * 8, orientation);
									break;
								}
							}
						}
					}
				}
			}

			outgoing.writeOpcode(0);
			viewport.method171(collisionMaps, scene);
			aClass15_1165.initializeRasterizer();
			outgoing.writeOpcode(0);

			int maximum = MapRegion.maximumPlane;
			if (maximum > plane) {
				maximum = plane;
			} else if (maximum < plane - 1) {
				maximum = plane - 1;
			}

			if (lowMemory) {
				scene.fill(MapRegion.maximumPlane);
			} else {
				scene.fill(0);
			}

			for (int x = 0; x < 104; x++) {
				for (int y = 0; y < 104; y++) {
					processGroundItems(x, y);
				}
			}

			anInt1051++;
			if (anInt1051 > 98) {
				anInt1051 = 0;
				outgoing.writeOpcode(150);
			}
			method63();
		} catch (Exception exception) {
		}

		ObjectDefinition.baseModels.clear();
		if (super.frame != null) {
			outgoing.writeOpcode(210);
			outgoing.writeInt(0x3f008edd);
		}

		if (lowMemory && SignLink.getCache() != null) {
			int modelCount = provider.getCount(0);
			for (int id = 0; id < modelCount; id++) {
				int attributes = provider.getModelAttributes(id);
				if ((attributes & 0x79) == 0) {
					Model.clear(id);
				}
			}

		}

		System.gc();
		Rasterizer.method367(20);
		provider.clearExtras();

		int minX = (regionX - 6) / 8 - 1;
		int maxX = (regionX + 6) / 8 + 1;
		int minY = (regionY - 6) / 8 - 1;
		int maxY = (regionY + 6) / 8 + 1;
		if (inPlayerOwnedHouse) {
			minX = minY = 49;
			maxX = maxY = 50;
		}

		for (int regionX = minX; regionX <= maxX; regionX++) {
			for (int regionY = minY; regionY <= maxY; regionY++) {
				if (regionX == minX || regionX == maxX || regionY == minY || regionY == maxY) {
					int map = provider.resolve(regionX, regionY, 0);
					if (map != -1) {
						provider.loadExtra(3, map);
					}

					int landscape = provider.resolve(regionX, regionY, 1);
					if (landscape != -1) {
						provider.loadExtra(3, landscape);
					}
				}
			}
		}
	}

	public final void method24(int plane) {
		int raster[] = aClass30_Sub2_Sub1_Sub1_1263.getRaster();
		int pixels = raster.length;
		for (int i = 0; i < pixels; i++) {
			raster[i] = 0;
		}

		for (int y = 1; y < 103; y++) {
			int i1 = 24628 + (103 - y) * 512 * 4;
			for (int x = 1; x < 103; x++) {
				if ((tileFlags[plane][x][y] & 0x18) == 0) {
					scene.method309(raster, x, y, plane, i1, 512);
				}

				if (plane < 3 && (tileFlags[plane + 1][x][y] & 8) != 0) {
					scene.method309(raster, x, y, plane + 1, i1, 512);
				}
				i1 += 4;
			}
		}

		int j1 = (238 + (int) (Math.random() * 20) - 10 << 16) + (238 + (int) (Math.random() * 20) - 10 << 8) + 238
				+ (int) (Math.random() * 20) - 10;
		int l1 = 238 + (int) (Math.random() * 20) - 10 << 16;
		aClass30_Sub2_Sub1_Sub1_1263.initRaster();

		for (int y = 1; y < 103; y++) {
			for (int x = 1; x < 103; x++) {
				if ((tileFlags[plane][x][y] & 0x18) == 0) {
					method50(x, y, plane, j1, l1);
				}
				if (plane < 3 && (tileFlags[plane + 1][x][y] & 8) != 0) {
					method50(x, y, plane + 1, j1, l1);
				}
			}
		}

		aClass15_1165.initializeRasterizer();
		anInt1071 = 0;
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				int id = scene.getFloorDecorationKey(x, y, this.plane);
				if (id != 0) {
					id = id >> 14 & 0x7fff;
					int function = ObjectDefinition.lookup(id).getMinimapFunction();

					if (function >= 0) {
						int viewportX = x;
						int viewportY = y;

						if (function != 22 && function != 29 && function != 34 && function != 36 && function != 46
								&& function != 47 && function != 48) {
							byte maxX = 104, maxY = 104;
							int[][] adjacencies = collisionMaps[this.plane].adjacencies;

							for (int i4 = 0; i4 < 10; i4++) {
								int random = (int) (Math.random() * 4);

								if (random == 0 && viewportX > 0 && viewportX > x - 3
										&& (adjacencies[viewportX - 1][viewportY] & 0x1280108) == 0) {
									viewportX--;
								}
								if (random == 1 && viewportX < maxX - 1 && viewportX < x + 3
										&& (adjacencies[viewportX + 1][viewportY] & 0x1280180) == 0) {
									viewportX++;
								}
								if (random == 2 && viewportY > 0 && viewportY > y - 3
										&& (adjacencies[viewportX][viewportY - 1] & 0x1280102) == 0) {
									viewportY--;
								}
								if (random == 3 && viewportY < maxY - 1 && viewportY < y + 3
										&& (adjacencies[viewportX][viewportY + 1] & 0x1280120) == 0) {
									viewportY++;
								}
							}
						}

						aClass30_Sub2_Sub1_Sub1Array1140[anInt1071] = mapFunctions[function];
						anIntArray1072[anInt1071] = viewportX;
						anIntArray1073[anInt1071] = viewportY;
						anInt1071++;
					}
				}
			}
		}
	}

	public final void method29(int i, Widget widget, int clickX, int l, int clickY, int j1) {
		if (widget.group != 0 || widget.children == null || widget.hidden) {
			return;
		} else if (clickX < i || clickY < l || clickX > i + widget.width || clickY > l + widget.height) {
			return;
		}

		int childCount = widget.children.length;
		for (int childIndex = 0; childIndex < childCount; childIndex++) {
			int i2 = widget.childX[childIndex] + i;
			int j2 = widget.childY[childIndex] + l - j1;
			Widget child = Widget.widgets[widget.children[childIndex]];
			i2 += child.horizontalDrawOffset;
			j2 += child.verticalDrawOffset;

			if ((child.hoverId >= 0 || child.defaultHoverColour != 0) && clickX >= i2 && clickY >= j2
					&& clickX < i2 + child.width && clickY < j2 + child.height) {
				if (child.hoverId >= 0) {
					anInt886 = child.hoverId;
				} else {
					anInt886 = child.id;
				}
			}

			if (child.group == Widget.TYPE_CONTAINER) {
				method29(i2, child, clickX, j2, clickY, child.scrollPosition);
				if (child.scrollLimit > child.height) {
					updateScrollbar(child, i2 + child.width, child.height, clickX, clickY, j2, true, child.scrollLimit);
				}
			} else {
				if (child.optionType == Widget.OPTION_OK && clickX >= i2 && clickY >= j2 && clickX < i2 + child.width
						&& clickY < j2 + child.height) {
					boolean flag = false;
					if (child.contentType != 0) {
						flag = processFriendListClick(child);
					}

					if (!flag) {
						menuActionTexts[menuActionRow] = child.hover;
						menuActionTypes[menuActionRow] = 315;
						secondMenuOperand[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if (child.optionType == Widget.OPTION_USABLE && widgetSelected == 0 && clickX >= i2 && clickY >= j2
						&& clickX < i2 + child.width && clickY < j2 + child.height) {
					String circumfix = child.optionCircumfix;
					if (circumfix.indexOf(" ") != -1) {
						circumfix = circumfix.substring(0, circumfix.indexOf(" "));
					}

					menuActionTexts[menuActionRow] = circumfix + " @gre@" + child.optionText;
					menuActionTypes[menuActionRow] = Actions.USABLE_WIDGET;
					secondMenuOperand[menuActionRow] = child.id;
					menuActionRow++;
				}

				if (child.optionType == Widget.OPTION_CLOSE && clickX >= i2 && clickY >= j2 && clickX < i2 + child.width
						&& clickY < j2 + child.height) {
					menuActionTexts[menuActionRow] = "Close";
					menuActionTypes[menuActionRow] = Actions.CLOSE_WIDGETS;
					secondMenuOperand[menuActionRow] = child.id;
					menuActionRow++;
				}

				if (child.optionType == Widget.OPTION_TOGGLE_SETTING && clickX >= i2 && clickY >= j2 && clickX < i2 + child.width
						&& clickY < j2 + child.height) {
					menuActionTexts[menuActionRow] = child.hover;
					menuActionTypes[menuActionRow] = Actions.TOGGLE_SETTING_WIDGET;
					secondMenuOperand[menuActionRow] = child.id;
					menuActionRow++;
				}

				if (child.optionType == Widget.OPTION_RESET_SETTING && clickX >= i2 && clickY >= j2 && clickX < i2 + child.width
						&& clickY < j2 + child.height) {
					menuActionTexts[menuActionRow] = child.hover;
					menuActionTypes[menuActionRow] = Actions.RESET_SETTING_WIDGET;
					secondMenuOperand[menuActionRow] = child.id;
					menuActionRow++;
				}

				if (child.optionType == Widget.OPTION_CONTINUE && !aBoolean1149 && clickX >= i2 && clickY >= j2
						&& clickX < i2 + child.width && clickY < j2 + child.height) {
					menuActionTexts[menuActionRow] = child.hover;
					menuActionTypes[menuActionRow] = Actions.CLICK_TO_CONTINUE;
					secondMenuOperand[menuActionRow] = child.id;
					menuActionRow++;
				}

				if (child.group == Widget.TYPE_INVENTORY) {
					int index = 0;
					for (int l2 = 0; l2 < child.height; l2++) {
						for (int i3 = 0; i3 < child.width; i3++) {
							int x = i2 + i3 * (32 + child.spritePaddingX);
							int y = j2 + l2 * (32 + child.spritePaddingY);
							if (index < 20) {
								x += child.spriteX[index];
								y += child.spriteY[index];
							}

							if (clickX >= x && clickY >= y && clickX < x + 32 && clickY < y + 32) {
								nextInventorySlot = index;
								anInt1067 = child.id;
								if (child.inventoryIds[index] > 0) {
									ItemDefinition definition = ItemDefinition.lookup(child.inventoryIds[index] - 1);
									if (itemSelected == 1 && child.hasActions) {
										if (child.id != anInt1284 || index != anInt1283) {
											menuActionTexts[menuActionRow] = "Use " + selectedItemName + " with @lre@"
													+ definition.getName();
											menuActionTypes[menuActionRow] = 870;
											selectedMenuActions[menuActionRow] = definition.getId();
											firstMenuOperand[menuActionRow] = index;
											secondMenuOperand[menuActionRow] = child.id;
											menuActionRow++;
										}
									} else if (widgetSelected == 1 && child.hasActions) {
										if ((anInt1138 & 0x10) == 16) {
											menuActionTexts[menuActionRow] = selectedWidgetName + " @lre@" + definition.getName();
											menuActionTypes[menuActionRow] = 543;
											selectedMenuActions[menuActionRow] = definition.getId();
											firstMenuOperand[menuActionRow] = index;
											secondMenuOperand[menuActionRow] = child.id;
											menuActionRow++;
										}
									} else {
										if (child.hasActions) {
											for (int action = 4; action >= 3; action--) {
												if (definition.getInventoryMenuActions() != null
														&& definition.getInventoryMenuAction(action) != null) {
													menuActionTexts[menuActionRow] = definition.getInventoryMenuAction(action)
															+ " @lre@" + definition.getName();
													if (action == 3) {
														menuActionTypes[menuActionRow] = 493;
													} else if (action == 4) {
														menuActionTypes[menuActionRow] = 847;
													}

													selectedMenuActions[menuActionRow] = definition.getId();
													firstMenuOperand[menuActionRow] = index;
													secondMenuOperand[menuActionRow] = child.id;
													menuActionRow++;
												} else if (action == 4) {
													menuActionTexts[menuActionRow] = "Drop @lre@" + definition.getName();
													menuActionTypes[menuActionRow] = 847;
													selectedMenuActions[menuActionRow] = definition.getId();
													firstMenuOperand[menuActionRow] = index;
													secondMenuOperand[menuActionRow] = child.id;
													menuActionRow++;
												}
											}
										}

										if (child.usableItems) {
											menuActionTexts[menuActionRow] = "Use @lre@" + definition.getName();
											menuActionTypes[menuActionRow] = 447;
											selectedMenuActions[menuActionRow] = definition.getId();
											firstMenuOperand[menuActionRow] = index;
											secondMenuOperand[menuActionRow] = child.id;
											menuActionRow++;
										}

										if (child.hasActions && definition.getInventoryMenuActions() != null) {
											for (int action = 2; action >= 0; action--) {
												if (definition.getInventoryMenuAction(action) != null) {
													menuActionTexts[menuActionRow] = definition.getInventoryMenuAction(action)
															+ " @lre@" + definition.getName();
													if (action == 0) {
														menuActionTypes[menuActionRow] = 74;
													} else if (action == 1) {
														menuActionTypes[menuActionRow] = 454;
													} else if (action == 2) {
														menuActionTypes[menuActionRow] = 539;
													}

													selectedMenuActions[menuActionRow] = definition.getId();
													firstMenuOperand[menuActionRow] = index;
													secondMenuOperand[menuActionRow] = child.id;
													menuActionRow++;
												}
											}
										}

										if (child.actions != null) {
											for (int action = 4; action >= 0; action--) {
												if (child.actions[action] != null) {
													menuActionTexts[menuActionRow] = child.actions[action] + " @lre@"
															+ definition.getName();
													if (action == 0) {
														menuActionTypes[menuActionRow] = 632;
													} else if (action == 1) {
														menuActionTypes[menuActionRow] = 78;
													} else if (action == 2) {
														menuActionTypes[menuActionRow] = 867;
													} else if (action == 3) {
														menuActionTypes[menuActionRow] = 431;
													} else if (action == 4) {
														menuActionTypes[menuActionRow] = 53;
													}

													selectedMenuActions[menuActionRow] = definition.getId();
													firstMenuOperand[menuActionRow] = index;
													secondMenuOperand[menuActionRow] = child.id;
													menuActionRow++;
												}
											}
										}

										menuActionTexts[menuActionRow] = "Examine @lre@" + definition.getName();
										menuActionTypes[menuActionRow] = Actions.EXAMINE_ITEM;
										selectedMenuActions[menuActionRow] = definition.getId();
										firstMenuOperand[menuActionRow] = index;
										secondMenuOperand[menuActionRow] = child.id;
										menuActionRow++;
									}
								}
							}
							index++;
						}
					}
				}
			}
		}
	}

	public final void method37(int j) {
		if (!lowMemory) {
			if (Rasterizer.anIntArray1480[17] >= j) {
				IndexedImage image = Rasterizer.textures[17];
				int pixels = image.getWidth() * image.getHeight() - 1;
				int j1 = image.getWidth() * tickDelta * 2;
				byte[] raster = image.getRaster();
				byte abyte3[] = aByteArray912;
				for (int i2 = 0; i2 <= pixels; i2++) {
					abyte3[i2] = raster[i2 - j1 & pixels];
				}

				image.setRaster(abyte3);
				aByteArray912 = raster;
				Rasterizer.method370(17);
				anInt854++;
				if (anInt854 > 1235) {
					anInt854 = 0;
					outgoing.writeOpcode(226);
					outgoing.writeByte(0);
					int l2 = outgoing.getPosition();
					outgoing.writeShort(58722);
					outgoing.writeByte(240);
					outgoing.writeShort((int) (Math.random() * 65536D));
					outgoing.writeByte((int) (Math.random() * 256D));
					if ((int) (Math.random() * 2D) == 0) {
						outgoing.writeShort(51825);
					}
					outgoing.writeByte((int) (Math.random() * 256D));
					outgoing.writeShort((int) (Math.random() * 65536D));
					outgoing.writeShort(7130);
					outgoing.writeShort((int) (Math.random() * 65536D));
					outgoing.writeShort(61657);
					outgoing.writeSizeByte(outgoing.getPosition() - l2);
				}
			}
			if (Rasterizer.anIntArray1480[24] >= j) {
				IndexedImage image = Rasterizer.textures[24];
				int length = image.getWidth() * image.getHeight() - 1;
				int k1 = image.getWidth() * tickDelta * 2;
				byte[] raster = image.getRaster();
				byte[] abyte4 = aByteArray912;
				for (int j2 = 0; j2 <= length; j2++) {
					abyte4[j2] = raster[j2 - k1 & length];
				}

				image.setRaster(abyte4);
				aByteArray912 = raster;
				Rasterizer.method370(24);
			}
			if (Rasterizer.anIntArray1480[34] >= j) {
				IndexedImage image = Rasterizer.textures[34];
				int i1 = image.getWidth() * image.getHeight() - 1;
				int l1 = image.getWidth() * tickDelta * 2;
				byte raster[] = image.getRaster();
				byte abyte5[] = aByteArray912;
				for (int k2 = 0; k2 <= i1; k2++) {
					abyte5[k2] = raster[k2 - l1 & i1];
				}

				image.setRaster(abyte5);
				aByteArray912 = raster;
				Rasterizer.method370(34);
			}
		}
	}

	public final void method39() {
		int i = anInt1098 * 128 + 64;
		int j = anInt1099 * 128 + 64;
		int k = method42(i, j, plane) - anInt1100;
		if (anInt858 < i) {
			anInt858 += anInt1101 + (i - anInt858) * anInt1102 / 1000;
			if (anInt858 > i) {
				anInt858 = i;
			}
		}
		if (anInt858 > i) {
			anInt858 -= anInt1101 + (anInt858 - i) * anInt1102 / 1000;
			if (anInt858 < i) {
				anInt858 = i;
			}
		}
		if (anInt859 < k) {
			anInt859 += anInt1101 + (k - anInt859) * anInt1102 / 1000;
			if (anInt859 > k) {
				anInt859 = k;
			}
		}
		if (anInt859 > k) {
			anInt859 -= anInt1101 + (anInt859 - k) * anInt1102 / 1000;
			if (anInt859 < k) {
				anInt859 = k;
			}
		}
		if (anInt860 < j) {
			anInt860 += anInt1101 + (j - anInt860) * anInt1102 / 1000;
			if (anInt860 > j) {
				anInt860 = j;
			}
		}
		if (anInt860 > j) {
			anInt860 -= anInt1101 + (anInt860 - j) * anInt1102 / 1000;
			if (anInt860 < j) {
				anInt860 = j;
			}
		}
		i = anInt995 * 128 + 64;
		j = anInt996 * 128 + 64;
		k = method42(i, j, plane) - anInt997;
		int l = i - anInt858;
		int i1 = k - anInt859;
		int j1 = j - anInt860;
		int k1 = (int) Math.sqrt(l * l + j1 * j1);
		int l1 = (int) (Math.atan2(i1, k1) * 325.949D) & 0x7ff;
		int i2 = (int) (Math.atan2(l, j1) * -325.949D) & 0x7ff;
		if (l1 < 128) {
			l1 = 128;
		}
		if (l1 > 383) {
			l1 = 383;
		}
		if (anInt861 < l1) {
			anInt861 += anInt998 + (l1 - anInt861) * anInt999 / 1000;
			if (anInt861 > l1) {
				anInt861 = l1;
			}
		}
		if (anInt861 > l1) {
			anInt861 -= anInt998 + (anInt861 - l1) * anInt999 / 1000;
			if (anInt861 < l1) {
				anInt861 = l1;
			}
		}
		int j2 = i2 - anInt862;
		if (j2 > 1024) {
			j2 -= 2048;
		}
		if (j2 < -1024) {
			j2 += 2048;
		}
		if (j2 > 0) {
			anInt862 += anInt998 + j2 * anInt999 / 1000;
			anInt862 &= 0x7ff;
		}
		if (j2 < 0) {
			anInt862 -= anInt998 + -j2 * anInt999 / 1000;
			anInt862 &= 0x7ff;
		}
		int k2 = i2 - anInt862;
		if (k2 > 1024) {
			k2 -= 2048;
		}
		if (k2 < -1024) {
			k2 += 2048;
		}
		if (k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0) {
			anInt862 = i2;
		}
	}

	public final void method40() {
		int x = menuClickX;
		int y = menuClickY;
		int width = anInt951;
		int height = anInt952;
		int colour = 0x5d5447;
		Raster.fillRectangle(x, y, width, height, colour);
		Raster.fillRectangle(x + 1, y + 1, width - 2, 16, 0);
		Raster.drawRectangle(x + 1, y + 18, width - 2, height - 19, 0);
		bold.render(x + 3, y + 14, "Choose Option", colour);
		int lastX = super.mouseEventX;
		int lastY = super.mouseEventY;

		if (anInt948 == 0) {
			lastX -= 4;
			lastY -= 4;
		} else if (anInt948 == 1) {
			lastX -= 553;
			lastY -= 205;
		} else if (anInt948 == 2) {
			lastX -= 17;
			lastY -= 357;
		}

		for (int i = 0; i < menuActionRow; i++) {
			int textY = y + 31 + (menuActionRow - 1 - i) * 15;
			int textColour = 0xffffff;
			if (lastX > x && lastX < x + width && lastY > textY - 13 && lastY < textY + 3) {
				textColour = 0xffff00;
			}
			bold.shadow(x + 3, textY, menuActionTexts[i], true, textColour);
		}
	}

	public final int method42(int x, int y, int z) {
		int worldX = x >> 7;
		int worldY = y >> 7;
		if (worldX < 0 || worldY < 0 || worldX > 103 || worldY > 103) {
			return 0;
		}
		
		int plane = z;
		if (plane < 3 && (tileFlags[1][worldX][worldY] & MapRegion.BRIDGE_TILE) != 0) {
			plane++;
		}
		
		int sizeX = x & 0x7f;
		int sizeY = y & 0x7f;
		int i2 = tileHeights[plane][worldX][worldY] * (128 - sizeX)
				+ tileHeights[plane][worldX + 1][worldY] * sizeX >> 7;
		int j2 = tileHeights[plane][worldX][worldY + 1] * (128 - sizeX)
				+ tileHeights[plane][worldX + 1][worldY + 1] * sizeX >> 7;
		return i2 * (128 - sizeY) + j2 * sizeY >> 7;
	}

	public final boolean method48(Widget widget) {
		int contentType = widget.contentType;
		if (friendServerStatus == 2) {
			if (contentType == 201) {
				redrawDialogueBox = true;
				inputDialogueState = 0;
				messagePromptRaised = true;
				aString1212 = "";
				anInt1064 = 1;
				aString1121 = "Enter name of friend to add to list";
			}
			if (contentType == 202) {
				redrawDialogueBox = true;
				inputDialogueState = 0;
				messagePromptRaised = true;
				aString1212 = "";
				anInt1064 = 2;
				aString1121 = "Enter name of friend to delete from list";
			}
		}
		if (contentType == 205) {
			anInt1011 = 250;
			return true;
		}
		if (contentType == 501) {
			redrawDialogueBox = true;
			inputDialogueState = 0;
			messagePromptRaised = true;
			aString1212 = "";
			anInt1064 = 4;
			aString1121 = "Enter name of player to add to list";
		}
		if (contentType == 502) {
			redrawDialogueBox = true;
			inputDialogueState = 0;
			messagePromptRaised = true;
			aString1212 = "";
			anInt1064 = 5;
			aString1121 = "Enter name of player to delete from list";
		}

		if (contentType >= 300 && contentType <= 313) {
			int part = (contentType - 300) / 2;
			int shift = contentType & 1;
			int style = characterDesignStyles[part];

			if (style != -1) {
				do {
					if (shift == 0 && --style < 0) {
						style = IdentityKit.count - 1;
					}
					if (shift == 1 && ++style >= IdentityKit.count) {
						style = 0;
					}
				} while (IdentityKit.kits[style].isValidStyle()
						|| IdentityKit.kits[style].getPart() != part + (maleAvatar ? 0 : 7));

				characterDesignStyles[part] = style;
				avatarChanged = true;
			}
		}

		if (contentType >= 314 && contentType <= 323) {
			int part = (contentType - 314) / 2;
			int k1 = contentType & 1;
			int colour = characterDesignColours[part];
			if (k1 == 0 && --colour < 0) {
				colour = PLAYER_BODY_RECOLOURS[part].length - 1;
			}
			if (k1 == 1 && ++colour >= PLAYER_BODY_RECOLOURS[part].length) {
				colour = 0;
			}
			characterDesignColours[part] = colour;
			avatarChanged = true;
		}

		if (contentType == 324 && !maleAvatar) {
			maleAvatar = true;
			changeCharacterGender();
		}

		if (contentType == 325 && maleAvatar) {
			maleAvatar = false;
			changeCharacterGender();
		}

		if (contentType == 326) {
			outgoing.writeOpcode(101);
			outgoing.writeByte(maleAvatar ? 0 : 1);
			for (int index = 0; index < 7; index++) {
				outgoing.writeByte(characterDesignStyles[index]);
			}

			for (int index = 0; index < 5; index++) {
				outgoing.writeByte(characterDesignColours[index]);
			}

			return true;
		}

		if (contentType == 613) {
			reportAbuseMuteToggle = !reportAbuseMuteToggle;
		}

		if (contentType >= 601 && contentType <= 612) {
			clearTopInterfaces();
			if (reportInput.length() > 0) {
				outgoing.writeOpcode(218);
				outgoing.writeLong(StringUtils.encodeBase37(reportInput));
				outgoing.writeByte(contentType - 601);
				outgoing.writeByte(reportAbuseMuteToggle ? 1 : 0);
			}
		}

		return false;
	}

	public final void method50(int x, int y, int z, int nullColour, int defaultColour) {
		int key = scene.getWallKey(x, y, z);

		if (key != 0) {
			int config = scene.getConfig(x, y, z, key);
			int orientation = config >> 6 & 3;
			int type = config & 0x1f;

			int colour = nullColour;
			if (key > 0) {
				colour = defaultColour;
			}

			int[] raster = aClass30_Sub2_Sub1_Sub1_1263.getRaster();
			int k4 = 24624 + x * 4 + (103 - y) * 512 * 4;
			int id = key >> 14 & 0x7fff;
			ObjectDefinition definition = ObjectDefinition.lookup(id);

			if (definition.getMapscene() != -1) {
				IndexedImage image = mapScenes[definition.getMapscene()];
				if (image != null) {
					int dx = (definition.getWidth() * 4 - image.getWidth()) / 2;
					int dy = (definition.getLength() * 4 - image.getHeight()) / 2;
					image.draw(48 + x * 4 + dx, 48 + (104 - y - definition.getLength()) * 4 + dy);
				}
			} else {
				if (type == 0 || type == 2) {
					if (orientation == 0) {
						raster[k4] = colour;
						raster[k4 + 512] = colour;
						raster[k4 + 1024] = colour;
						raster[k4 + 1536] = colour;
					} else if (orientation == 1) {
						raster[k4] = colour;
						raster[k4 + 1] = colour;
						raster[k4 + 2] = colour;
						raster[k4 + 3] = colour;
					} else if (orientation == 2) {
						raster[k4 + 3] = colour;
						raster[k4 + 3 + 512] = colour;
						raster[k4 + 3 + 1024] = colour;
						raster[k4 + 3 + 1536] = colour;
					} else if (orientation == 3) {
						raster[k4 + 1536] = colour;
						raster[k4 + 1536 + 1] = colour;
						raster[k4 + 1536 + 2] = colour;
						raster[k4 + 1536 + 3] = colour;
					}
				}
				if (type == 3) {
					if (orientation == 0) {
						raster[k4] = colour;
					} else if (orientation == 1) {
						raster[k4 + 3] = colour;
					} else if (orientation == 2) {
						raster[k4 + 3 + 1536] = colour;
					} else if (orientation == 3) {
						raster[k4 + 1536] = colour;
					}
				}
				if (type == 2) {
					if (orientation == 3) {
						raster[k4] = colour;
						raster[k4 + 512] = colour;
						raster[k4 + 1024] = colour;
						raster[k4 + 1536] = colour;
					} else if (orientation == 0) {
						raster[k4] = colour;
						raster[k4 + 1] = colour;
						raster[k4 + 2] = colour;
						raster[k4 + 3] = colour;
					} else if (orientation == 1) {
						raster[k4 + 3] = colour;
						raster[k4 + 3 + 512] = colour;
						raster[k4 + 3 + 1024] = colour;
						raster[k4 + 3 + 1536] = colour;
					} else if (orientation == 2) {
						raster[k4 + 1536] = colour;
						raster[k4 + 1536 + 1] = colour;
						raster[k4 + 1536 + 2] = colour;
						raster[k4 + 1536 + 3] = colour;
					}
				}
			}
		}

		key = scene.getInteractableObjectKey(x, y, z);
		if (key != 0) {
			int config = scene.getConfig(x, y, z, key);
			int orientation = config >> 6 & 3;
			int type = config & 0x1f;
			int id = key >> 14 & 0x7fff;
			ObjectDefinition definition = ObjectDefinition.lookup(id);

			if (definition.getMapscene() != -1) {
				IndexedImage image = mapScenes[definition.getMapscene()];
				if (image != null) {
					int j5 = (definition.getWidth() * 4 - image.getWidth()) / 2;
					int k5 = (definition.getLength() * 4 - image.getHeight()) / 2;
					image.draw(48 + x * 4 + j5, 48 + (104 - y - definition.getLength()) * 4 + k5);
				}
			} else if (type == 9) {
				int colour = 0xeeeeee;
				if (key > 0) {
					colour = 0xee0000;
				}

				int raster[] = aClass30_Sub2_Sub1_Sub1_1263.getRaster();
				int index = 24624 + x * 4 + (103 - y) * 512 * 4;
				if (orientation == 0 || orientation == 2) {
					raster[index + 1536] = colour;
					raster[index + 1024 + 1] = colour;
					raster[index + 512 + 2] = colour;
					raster[index + 3] = colour;
				} else {
					raster[index] = colour;
					raster[index + 512 + 1] = colour;
					raster[index + 1024 + 2] = colour;
					raster[index + 1536 + 3] = colour;
				}
			}
		}

		key = scene.getFloorDecorationKey(x, y, z);
		if (key != 0) {
			int id = key >> 14 & 0x7fff;
			ObjectDefinition definition = ObjectDefinition.lookup(id);
			if (definition.getMapscene() != -1) {
				IndexedImage image = mapScenes[definition.getMapscene()];
				if (image != null) {
					int i4 = (definition.getWidth() * 4 - image.getWidth()) / 2;
					int j4 = (definition.getLength() * 4 - image.getHeight()) / 2;
					image.draw(48 + x * 4 + i4, 48 + (104 - y - definition.getLength()) * 4 + j4);
				}
			}
		}
	}

	public final void method51() {
		titleBox = new IndexedImage(titleScreen, "titlebox", 0);
		titleButton = new IndexedImage(titleScreen, "titlebutton", 0);
		runes = new IndexedImage[12];
		int icon = 0;
		try {
			icon = Integer.parseInt(getParameter("fl_icon"));
		} catch (Exception ex) {
		}

		if (icon == 0) {
			for (int id = 0; id < 12; id++) {
				runes[id] = new IndexedImage(titleScreen, "runes", id);
			}
		} else {
			for (int id = 0; id < 12; id++) {
				runes[id] = new IndexedImage(titleScreen, "runes", 12 + (id & 3));
			}

		}
		aClass30_Sub2_Sub1_Sub1_1201 = new Sprite(128, 265);
		aClass30_Sub2_Sub1_Sub1_1202 = new Sprite(128, 265);
		for (int i1 = 0; i1 < 33920; i1++) {
			aClass30_Sub2_Sub1_Sub1_1201.getRaster()[i1] = aClass15_1110.getPixels()[i1];
		}

		for (int j1 = 0; j1 < 33920; j1++) {
			aClass30_Sub2_Sub1_Sub1_1202.getRaster()[j1] = aClass15_1111.getPixels()[j1];
		}

		anIntArray851 = new int[256];
		for (int k1 = 0; k1 < 64; k1++) {
			anIntArray851[k1] = k1 * 0x40000;
		}

		for (int l1 = 0; l1 < 64; l1++) {
			anIntArray851[l1 + 64] = 0xff0000 + 1024 * l1;
		}

		for (int i2 = 0; i2 < 64; i2++) {
			anIntArray851[i2 + 128] = 0xffff00 + 4 * i2;
		}

		for (int j2 = 0; j2 < 64; j2++) {
			anIntArray851[j2 + 192] = 0xffffff;
		}

		anIntArray852 = new int[256];
		for (int k2 = 0; k2 < 64; k2++) {
			anIntArray852[k2] = k2 * 1024;
		}

		for (int l2 = 0; l2 < 64; l2++) {
			anIntArray852[l2 + 64] = 65280 + 4 * l2;
		}

		for (int i3 = 0; i3 < 64; i3++) {
			anIntArray852[i3 + 128] = 65535 + 0x40000 * i3;
		}

		for (int j3 = 0; j3 < 64; j3++) {
			anIntArray852[j3 + 192] = 0xffffff;
		}

		anIntArray853 = new int[256];
		for (int k3 = 0; k3 < 64; k3++) {
			anIntArray853[k3] = k3 * 4;
		}

		for (int l3 = 0; l3 < 64; l3++) {
			anIntArray853[l3 + 64] = 255 + 0x40000 * l3;
		}

		for (int i4 = 0; i4 < 64; i4++) {
			anIntArray853[i4 + 128] = 0xff00ff + 1024 * i4;
		}

		for (int j4 = 0; j4 < 64; j4++) {
			anIntArray853[j4 + 192] = 0xffffff;
		}

		anIntArray850 = new int[256];
		anIntArray1190 = new int[32768];
		anIntArray1191 = new int[32768];
		method106(null);
		anIntArray828 = new int[32768];
		anIntArray829 = new int[32768];
		drawLoadingText(10, "Connecting to fileserver");

		if (!aBoolean831) {
			aBoolean880 = true;
			aBoolean831 = true;
			startRunnable(this, 2);
		}
	}

	public final int method54() {
		for (int i = 0; i < localRegionMapData.length; i++) {
			if (localRegionMapData[i] == null && localRegionMapIds[i] != -1) {
				return -1;
			} else if (localRegionLandscapeData[i] == null && localRegionLandscapeIds[i] != -1) {
				return -2;
			}
		}

		boolean ready = true;
		for (int i = 0; i < localRegionMapData.length; i++) {
			byte[] data = localRegionLandscapeData[i];
			if (data != null) {
				int x = (localRegionIds[i] >> 8) * 64 - regionBaseX;
				int y = (localRegionIds[i] & 0xff) * 64 - regionBaseY;

				if (constructedViewport) {
					x = y = 10;
				}
				ready &= MapRegion.objectsReady(data, x, y);
			}
		}

		if (!ready) {
			return -3;
		} else if (validLocalMap) {
			return -4;
		}

		loadingStage = 2;
		MapRegion.currentPlane = plane;
		method22();
		outgoing.writeOpcode(121);
		return 0;
	}

	public final void method58() {
		char c = 256;
		for (int j = 10; j < 117; j++) {
			int random = (int) (Math.random() * 100);
			if (random < 50) {
				anIntArray828[j + (c - 2 << 7)] = 255;
			}
		}

		for (int l = 0; l < 100; l++) {
			int i1 = (int) (Math.random() * 124D) + 2;
			int k1 = (int) (Math.random() * 128D) + 128;
			int k2 = i1 + (k1 << 7);
			anIntArray828[k2] = 192;
		}

		for (int j1 = 1; j1 < c - 1; j1++) {
			for (int l1 = 1; l1 < 127; l1++) {
				int l2 = l1 + (j1 << 7);
				anIntArray829[l2] = (anIntArray828[l2 - 1] + anIntArray828[l2 + 1] + anIntArray828[l2 - 128] + anIntArray828[l2 + 128]) / 4;
			}

		}

		anInt1275 += 128;
		if (anInt1275 > anIntArray1190.length) {
			anInt1275 -= anIntArray1190.length;
			int i2 = (int) (Math.random() * 12D);
			method106(runes[i2]);
		}
		for (int j2 = 1; j2 < c - 1; j2++) {
			for (int i3 = 1; i3 < 127; i3++) {
				int k3 = i3 + (j2 << 7);
				int i4 = anIntArray829[k3 + 128] - anIntArray1190[k3 + anInt1275 & anIntArray1190.length - 1] / 5;
				if (i4 < 0) {
					i4 = 0;
				}
				anIntArray828[k3] = i4;
			}

		}

		for (int j3 = 0; j3 < c - 1; j3++) {
			anIntArray969[j3] = anIntArray969[j3 + 1];
		}

		anIntArray969[c - 1] = (int) (Math.sin(tick / 14D) * 16D + Math.sin(tick / 15D) * 14D + Math.sin(tick / 16D) * 12D);
		if (anInt1040 > 0) {
			anInt1040 -= 4;
		}
		if (anInt1041 > 0) {
			anInt1041 -= 4;
		}
		if (anInt1040 == 0 && anInt1041 == 0) {
			int l3 = (int) (Math.random() * 2000);
			if (l3 == 0) {
				anInt1040 = 1024;
			} else if (l3 == 1) {
				anInt1041 = 1024;
			}
		}
	}

	public final void method75(Widget widget) {
		int index = widget.contentType;
		if (index >= 1 && index <= 100 || index >= 701 && index <= 800) {
			if (index == 1 && friendServerStatus == 0) {
				widget.defaultText = "Loading friend list";
				widget.optionType = 0;
				return;
			}
			if (index == 1 && friendServerStatus == 1) {
				widget.defaultText = "Connecting to friendserver";
				widget.optionType = 0;
				return;
			}
			if (index == 2 && friendServerStatus != 2) {
				widget.defaultText = "Please wait...";
				widget.optionType = 0;
				return;
			}
			int count = friendCount;
			if (friendServerStatus != 2) {
				count = 0;
			}

			if (index > 700) {
				index -= 601;
			} else {
				index--;
			}

			if (index >= count) {
				widget.defaultText = "";
				widget.optionType = 0;
				return;
			}

			widget.defaultText = friendUsernames[index];
			widget.optionType = 1;
			return;
		}

		if (index >= 101 && index <= 200 || index >= 801 && index <= 900) {
			int l = friendCount;
			if (friendServerStatus != 2) {
				l = 0;
			}
			if (index > 800) {
				index -= 701;
			} else {
				index -= 101;
			}
			if (index >= l) {
				widget.defaultText = "";
				widget.optionType = 0;
				return;
			}
			if (friendWorlds[index] == 0) {
				widget.defaultText = "@red@Offline";
			} else if (friendWorlds[index] == node) {
				widget.defaultText = "@gre@World-" + (friendWorlds[index] - 9);
			} else {
				widget.defaultText = "@yel@World-" + (friendWorlds[index] - 9);
			}
			widget.optionType = 1;
			return;
		}
		if (index == 203) {
			int count = friendCount;
			if (friendServerStatus != 2) {
				count = 0;
			}

			widget.scrollLimit = count * 15 + 20;
			if (widget.scrollLimit <= widget.height) {
				widget.scrollLimit = widget.height + 1;
			}
			return;
		}
		if (index >= 401 && index <= 500) {
			if ((index -= 401) == 0 && friendServerStatus == 0) {
				widget.defaultText = "Loading ignore list";
				widget.optionType = 0;
				return;
			}
			if (index == 1 && friendServerStatus == 0) {
				widget.defaultText = "Please wait...";
				widget.optionType = 0;
				return;
			}
			int count = ignoredCount;
			if (friendServerStatus == 0) {
				count = 0;
			}
			if (index >= count) {
				widget.defaultText = "";
				widget.optionType = 0;
				return;
			}
			widget.defaultText = StringUtils.format(StringUtils.decodeBase37(ignores[index]));
			widget.optionType = 1;
			return;
		}
		if (index == 503) {
			widget.scrollLimit = ignoredCount * 15 + 20;
			if (widget.scrollLimit <= widget.height) {
				widget.scrollLimit = widget.height + 1;
			}
			return;
		}
		if (index == 327) {
			widget.spritePitch = 150;
			widget.spriteRoll = (int) (Math.sin(tick / 40D) * 256D) & 0x7ff;
			if (avatarChanged) {
				for (int k1 = 0; k1 < 7; k1++) {
					int style = characterDesignStyles[k1];
					if (style >= 0 && !IdentityKit.kits[style].bodyLoaded()) {
						return;
					}
				}

				avatarChanged = false;
				Model[] models = new Model[7];
				int modelCount = 0;
				for (int i = 0; i < 7; i++) {
					int style = characterDesignStyles[i];
					if (style >= 0) {
						models[modelCount++] = IdentityKit.kits[style].bodyModel();
					}
				}

				Model model = new Model(modelCount, models);
				for (int part = 0; part < 5; part++) {
					if (characterDesignColours[part] != 0) {
						model.recolour(PLAYER_BODY_RECOLOURS[part][0], PLAYER_BODY_RECOLOURS[part][characterDesignColours[part]]);
						if (part == 1) {
							model.recolour(SKIN_COLOURS[0], SKIN_COLOURS[characterDesignColours[part]]);
						}
					}
				}

				model.skin();
				model.apply(Animation.animations[localPlayer.idleAnimation].getPrimaryFrame(0));
				model.light(64, 850, -30, -50, -30, true);
				widget.defaultMediaType = 5;
				widget.defaultMedia = 0;
				Widget.clearModels(0, 5, model);
			}
			return;
		}
		if (index == 324) {
			if (aClass30_Sub2_Sub1_Sub1_931 == null) {
				aClass30_Sub2_Sub1_Sub1_931 = widget.defaultSprite;
				aClass30_Sub2_Sub1_Sub1_932 = widget.secondarySprite;
			}
			if (maleAvatar) {
				widget.defaultSprite = aClass30_Sub2_Sub1_Sub1_932;
			} else {
				widget.defaultSprite = aClass30_Sub2_Sub1_Sub1_931;
			}
		}
		if (index == 325) {
			if (aClass30_Sub2_Sub1_Sub1_931 == null) {
				aClass30_Sub2_Sub1_Sub1_931 = widget.defaultSprite;
				aClass30_Sub2_Sub1_Sub1_932 = widget.secondarySprite;
			}
			if (maleAvatar) {
				widget.defaultSprite = aClass30_Sub2_Sub1_Sub1_931;
			} else {
				widget.defaultSprite = aClass30_Sub2_Sub1_Sub1_932;
			}
		}
		if (index == 600) {
			widget.defaultText = reportInput;
			if (tick % 20 < 10) {
				widget.defaultText += "|";
				return;
			}
			widget.defaultText += " ";
			return;
		}
		if (index == 613) {
			if (playerPrivelage >= 1) {
				if (reportAbuseMuteToggle) {
					widget.defaultColour = 0xff0000;
					widget.defaultText = "Moderator option: Mute player for 48 hours: <ON>";
				} else {
					widget.defaultColour = 0xffffff;
					widget.defaultText = "Moderator option: Mute player for 48 hours: <OFF>";
				}
			} else {
				widget.defaultText = "";
			}
		}
		if (index == 650 || index == 655) {
			if (lastLoginIP != 0) {
				String time;
				if (daysSinceLogin == 0) {
					time = "earlier today";
				} else if (daysSinceLogin == 1) {
					time = "yesterday";
				} else {
					time = daysSinceLogin + " days ago";
				}
				widget.defaultText = "You last logged in " + time + " from: " + SignLink.getDns();
			} else {
				widget.defaultText = "";
			}
		}
		if (index == 651) {
			if (unreadMessageCount == 0) {
				widget.defaultText = "0 unread messages";
				widget.defaultColour = 0xffff00;
			}
			if (unreadMessageCount == 1) {
				widget.defaultText = "1 unread message";
				widget.defaultColour = 65280;
			}
			if (unreadMessageCount > 1) {
				widget.defaultText = unreadMessageCount + " unread messages";
				widget.defaultColour = 65280;
			}
		}
		if (index == 652) {
			if (daysSinceRecoveryChange == 201) {
				if (hasMembersCredit == 1) {
					widget.defaultText = "@yel@This is a non-members world: @whi@Since you are a member we";
				} else {
					widget.defaultText = "";
				}
			} else if (daysSinceRecoveryChange == 200) {
				widget.defaultText = "You have not yet set any password recovery questions.";
			} else {
				String s1;
				if (daysSinceRecoveryChange == 0) {
					s1 = "Earlier today";
				} else if (daysSinceRecoveryChange == 1) {
					s1 = "Yesterday";
				} else {
					s1 = daysSinceRecoveryChange + " days ago";
				}
				widget.defaultText = s1 + " you changed your recovery questions";
			}
		}
		if (index == 653) {
			if (daysSinceRecoveryChange == 201) {
				if (hasMembersCredit == 1) {
					widget.defaultText = "@whi@recommend you use a members world instead. You may use";
				} else {
					widget.defaultText = "";
				}
			} else if (daysSinceRecoveryChange == 200) {
				widget.defaultText = "We strongly recommend you do so now to secure your account.";
			} else {
				widget.defaultText = "If you do not remember making this change then cancel it immediately";
			}
		}
		if (index == 654) {
			if (daysSinceRecoveryChange == 201) {
				if (hasMembersCredit == 1) {
					widget.defaultText = "@whi@this world but member benefits are unavailable whilst here.";
					return;
				}
				widget.defaultText = "";
				return;
			}
			if (daysSinceRecoveryChange == 200) {
				widget.defaultText = "Do this from the 'account management' area on our front webpage";
				return;
			}
			widget.defaultText = "Do this from the 'account management' area on our front webpage";
		}
	}

	public final void method82() {
		if (anInt1086 != 0) {
			return;
		}

		menuActionTexts[0] = "Cancel";
		menuActionTypes[0] = 1107;
		menuActionRow = 1;
		method129();
		anInt886 = 0;

		if (super.mouseEventX > 4 && super.mouseEventY > 4 && super.mouseEventX < 516 && super.mouseEventY < 338) {
			if (openInterfaceId != -1) {
				method29(4, Widget.widgets[openInterfaceId], super.mouseEventX, 4, super.mouseEventY, 0);
			} else {
				createMenu();
			}
		}

		if (anInt886 != anInt1026) {
			anInt1026 = anInt886;
		}
		anInt886 = 0;

		if (super.mouseEventX > 553 && super.mouseEventY > 205 && super.mouseEventX < 743 && super.mouseEventY < 466) {
			if (overlayInterfaceId != -1) {
				method29(553, Widget.widgets[overlayInterfaceId], super.mouseEventX, 205, super.mouseEventY, 0);
			} else if (inventoryTabIds[tabId] != -1) {
				method29(553, Widget.widgets[inventoryTabIds[tabId]], super.mouseEventX, 205, super.mouseEventY, 0);
			}
		}

		if (anInt886 != anInt1048) {
			redrawTabArea = true;
			anInt1048 = anInt886;
		}
		anInt886 = 0;
		if (super.mouseEventX > 17 && super.mouseEventY > 357 && super.mouseEventX < 496 && super.mouseEventY < 453) {
			if (backDialogueId != -1) {
				method29(17, Widget.widgets[backDialogueId], super.mouseEventX, 357, super.mouseEventY, 0);
			} else if (super.mouseEventY < 434 && super.mouseEventX < 426) {
				processChatMessageClick(super.mouseEventY - 357);
			}
		}
		if (backDialogueId != -1 && anInt886 != anInt1039) {
			redrawDialogueBox = true;
			anInt1039 = anInt886;
		}

		boolean flag = false;
		while (!flag) {
			flag = true;
			for (int j = 0; j < menuActionRow - 1; j++) {
				if (menuActionTypes[j] < 1000 && menuActionTypes[j + 1] > 1000) {
					String text = menuActionTexts[j];
					menuActionTexts[j] = menuActionTexts[j + 1];
					menuActionTexts[j + 1] = text;
					int k = menuActionTypes[j];
					menuActionTypes[j] = menuActionTypes[j + 1];
					menuActionTypes[j + 1] = k;
					k = firstMenuOperand[j];
					firstMenuOperand[j] = firstMenuOperand[j + 1];
					firstMenuOperand[j + 1] = k;
					k = secondMenuOperand[j];
					secondMenuOperand[j] = secondMenuOperand[j + 1];
					secondMenuOperand[j + 1] = k;
					k = selectedMenuActions[j];
					selectedMenuActions[j] = selectedMenuActions[j + 1];
					selectedMenuActions[j + 1] = k;
					flag = false;
				}
			}
		}
	}

	public final void midiSave(byte[] data, boolean fade) {
		SignLink.setMidiFade(fade ? 1 : 0);
		SignLink.midiSave(data, data.length);
	}

	public final void nextForcedMovementStep(Mob character) {
		if (character.endForceMovement == tick
				|| character.emoteAnimation == -1
				|| character.animationDelay != 0
				|| character.emoteTimeRemaining + 1 > Animation.animations[character.emoteAnimation]
						.duration(character.displayedEmoteFrames)) {
			int remaining = character.endForceMovement - character.startForceMovement;
			int elapsed = tick - character.startForceMovement;
			int absInitialX = character.initialX * 128 + character.size * 64;
			int absInitialY = character.initialY * 128 + character.size * 64;
			int absDestinationX = character.destinationX * 128 + character.size * 64;
			int absDestinationY = character.destinationY * 128 + character.size * 64;
			character.worldX = (absInitialX * (remaining - elapsed) + absDestinationX * elapsed) / remaining;
			character.worldY = (absInitialY * (remaining - elapsed) + absDestinationY * elapsed) / remaining;
		}
		character.anInt1503 = 0;
		if (character.direction == 0) {
			character.nextStepOrientation = 1024;
		}
		if (character.direction == 1) {
			character.nextStepOrientation = 1536;
		}
		if (character.direction == 2) {
			character.nextStepOrientation = 0;
		}
		if (character.direction == 3) {
			character.nextStepOrientation = 512;
		}
		character.orientation = character.nextStepOrientation;
	}

	public final void nextPreForcedStep(Mob character) {
		int remaining = character.startForceMovement - tick;
		int x = character.initialX * 128 + character.size * 64;
		int y = character.initialY * 128 + character.size * 64;
		character.worldX += (x - character.worldX) / remaining;
		character.worldY += (y - character.worldY) / remaining;
		character.anInt1503 = 0;
		if (character.direction == 0) {
			character.nextStepOrientation = 1024;
		}
		if (character.direction == 1) {
			character.nextStepOrientation = 1536;
		}
		if (character.direction == 2) {
			character.nextStepOrientation = 0;
		}
		if (character.direction == 3) {
			character.nextStepOrientation = 512;
		}
	}

	public final void nextStep(Mob mob) {
		mob.movementAnimation = mob.idleAnimation;
		if (mob.remainingPath == 0) {
			mob.anInt1503 = 0;
			return;
		}
		if (mob.emoteAnimation != -1 && mob.animationDelay == 0) {
			Animation animation = Animation.animations[mob.emoteAnimation];
			if (mob.anInt1542 > 0 && animation.getAnimatingPrecedence() == 0) {
				mob.anInt1503++;
				return;
			}
			if (mob.anInt1542 <= 0 && animation.getWalkingPrecedence() == 0) {
				mob.anInt1503++;
				return;
			}
		}
		int x = mob.worldX;
		int y = mob.worldY;
		int nextX = mob.pathX[mob.remainingPath - 1] * 128 + mob.size * 64;
		int nextY = mob.pathY[mob.remainingPath - 1] * 128 + mob.size * 64;
		if (nextX - x > 256 || nextX - x < -256 || nextY - y > 256 || nextY - y < -256) {
			mob.worldX = nextX;
			mob.worldY = nextY;
			return;
		}

		if (x < nextX) {
			if (y < nextY) {
				mob.nextStepOrientation = 0x500;
			} else if (y > nextY) {
				mob.nextStepOrientation = 0x700;
			} else {
				mob.nextStepOrientation = 0x600;
			}
		} else if (x > nextX) {
			if (y < nextY) {
				mob.nextStepOrientation = 0x300;
			} else if (y > nextY) {
				mob.nextStepOrientation = 0x100;
			} else {
				mob.nextStepOrientation = 0x200;
			}
		} else if (y < nextY) {
			mob.nextStepOrientation = 0x400;
		} else {
			mob.nextStepOrientation = 0;
		}

		int rotation = mob.nextStepOrientation - mob.orientation & 0x7ff;
		if (rotation > 0x400) {
			rotation -= 0x800;
		}

		int animation = mob.halfTurnAnimation;
		if (rotation >= -0x100 && rotation <= 0x100) {
			animation = mob.walkingAnimation;
		} else if (rotation >= 0x100 && rotation < 0x300) {
			animation = mob.quarterAnticlockwiseTurnAnimation;
		} else if (rotation >= -0x300 && rotation <= -0x100) {
			animation = mob.quarterClockwiseTurnAnimation;
		}
		if (animation == -1) {
			animation = mob.walkingAnimation;
		}

		mob.movementAnimation = animation;
		int positionDelta = 4;
		if (mob.orientation != mob.nextStepOrientation && mob.interactingMob == -1 && mob.rotation != 0) {
			positionDelta = 2;
		}
		if (mob.remainingPath > 2) {
			positionDelta = 6;
		}
		if (mob.remainingPath > 3) {
			positionDelta = 8;
		}
		if (mob.anInt1503 > 0 && mob.remainingPath > 1) {
			positionDelta = 8;
			mob.anInt1503--;
		}
		if (mob.pathRun[mob.remainingPath - 1]) {
			positionDelta *= 2;
		}
		if (positionDelta >= 8 && mob.movementAnimation == mob.walkingAnimation && mob.runAnimation != -1) {
			mob.movementAnimation = mob.runAnimation;
		}

		if (x < nextX) {
			mob.worldX += positionDelta;
			if (mob.worldX > nextX) {
				mob.worldX = nextX;
			}
		} else if (x > nextX) {
			mob.worldX -= positionDelta;
			if (mob.worldX < nextX) {
				mob.worldX = nextX;
			}
		}
		if (y < nextY) {
			mob.worldY += positionDelta;
			if (mob.worldY > nextY) {
				mob.worldY = nextY;
			}
		} else if (y > nextY) {
			mob.worldY -= positionDelta;
			if (mob.worldY < nextY) {
				mob.worldY = nextY;
			}
		}
		if (mob.worldX == nextX && mob.worldY == nextY) {
			mob.remainingPath--;
			if (mob.anInt1542 > 0) {
				mob.anInt1542--;
			}
		}
	}

	public final Socket openSocket(int port) throws IOException {
		if (SignLink.getApplet() != null) {
			return SignLink.openSocket(port);
		}

		return new Socket(InetAddress.getByName(getCodeBase().getHost()), port);
	}

	public final boolean parseFrame() {
		if (primary == null) {
			return false;
		}

		try {
			int available = primary.available();
			if (available == 0) {
				return false;
			}

			if (opcode == -1) {
				primary.read(incoming.getPayload(), 0, 1);
				opcode = incoming.getPayload()[0] & 0xff;
				if (decryption != null) {
					opcode = opcode - decryption.nextKey() & 0xff;
				}

				packetSize = PacketConstants.PACKET_LENGTHS[opcode];
				available--;
			}

			if (packetSize == -1) {
				if (available > 0) {
					primary.read(incoming.getPayload(), 0, 1);
					packetSize = incoming.getPayload()[0] & 0xff;
					available--;
				} else {
					return false;
				}
			}

			if (packetSize == -2) {
				if (available > 1) {
					primary.read(incoming.getPayload(), 0, 2);
					incoming.setPosition(0);
					packetSize = incoming.readUShort();
					available -= 2;
				} else {
					return false;
				}
			}

			if (available < packetSize) {
				return false;
			}

			incoming.setPosition(0);
			primary.read(incoming.getPayload(), 0, packetSize);
			timeoutCounter = 0;
			thirdLastOpcode = secondLastOpcode;
			secondLastOpcode = lastOpcode;
			lastOpcode = opcode;

			if (opcode == 81) {
				synchronizePlayers(packetSize, incoming);
				validLocalMap = false;
				opcode = -1;
				return true;
			}
			if (opcode == 176) {
				daysSinceRecoveryChange = incoming.readNegUByte();
				unreadMessageCount = incoming.readUShortA();
				hasMembersCredit = incoming.readUByte();
				lastLoginIP = incoming.readIMEInt();
				daysSinceLogin = incoming.readUShort();
				if (lastLoginIP != 0 && openInterfaceId == -1) {
					SignLink.dnsLookup(StringUtils.decodeIp(lastLoginIP));
					clearTopInterfaces();
					char c = '\u028A';
					if (daysSinceRecoveryChange != 201 || hasMembersCredit == 1) {
						c = '\u028F';
					}
					reportInput = "";
					reportAbuseMuteToggle = false;
					for (Widget widget : Widget.widgets) {
						if (widget == null || widget.contentType != c) {
							continue;
						}
						openInterfaceId = widget.parent;
						break;
					}

				}
				opcode = -1;
				return true;
			}
			if (opcode == 64) {
				localX = incoming.readNegUByte();
				localY = incoming.readUByteS();
				for (int x = localX; x < localX + 8; x++) {
					for (int y = localY; y < localY + 8; y++) {
						if (groundItems[plane][x][y] != null) {
							groundItems[plane][x][y] = null;
							processGroundItems(x, y);
						}
					}
				}

				for (SpawnedObject object = (SpawnedObject) spawns.getFront(); object != null; object = (SpawnedObject) spawns
						.getNext()) {
					if (object.getX() >= localX && object.getX() < localX + 8 && object.getY() >= localY
							&& object.getY() < localY + 8 && object.getPlane() == plane) {
						object.setLongetivity(0);
					}
				}

				opcode = -1;
				return true;
			}
			if (opcode == 185) {
				int id = incoming.readLEUShortA();
				Widget.widgets[id].defaultMediaType = 3;
				if (localPlayer.npcDefinition == null) {
					Widget.widgets[id].defaultMedia = (localPlayer.appearanceColours[0] << 25)
							+ (localPlayer.appearanceColours[4] << 20) + (localPlayer.appearanceModels[0] << 15)
							+ (localPlayer.appearanceModels[8] << 10) + (localPlayer.appearanceModels[11] << 5)
							+ localPlayer.appearanceModels[1];
				} else {
					Widget.widgets[id].defaultMedia = (int) (0x12345678L + localPlayer.npcDefinition.getId());
				}
				opcode = -1;
				return true;
			}
			if (opcode == 107) {
				oriented = false;
				for (int l = 0; l < 5; l++) {
					aBooleanArray876[l] = false;
				}

				opcode = -1;
				return true;
			}
			if (opcode == 72) {
				int id = incoming.readLEUShort();
				Widget widget = Widget.widgets[id];
				for (int slot = 0; slot < widget.inventoryIds.length; slot++) {
					widget.inventoryIds[slot] = -1;
					widget.inventoryIds[slot] = 0;
				}

				opcode = -1;
				return true;
			}
			if (opcode == 214) {
				ignoredCount = packetSize / 8;
				for (int i = 0; i < ignoredCount; i++) {
					ignores[i] = incoming.readLong();
				}

				opcode = -1;
				return true;
			}
			if (opcode == 166) {
				oriented = true;
				anInt1098 = incoming.readUByte(); // local X
				anInt1099 = incoming.readUByte(); // local Y
				anInt1100 = incoming.readUShort(); // height offset
				anInt1101 = incoming.readUByte();
				anInt1102 = incoming.readUByte();

				if (anInt1102 >= 100) {
					anInt858 = anInt1098 * 128 + 64; // absolute x
					anInt860 = anInt1099 * 128 + 64; // absolute y
					anInt859 = method42(anInt858, anInt860, plane) - anInt1100; // height
				}
				opcode = -1;
				return true;
			}
			if (opcode == 134) {
				redrawTabArea = true;
				int skill = incoming.readUByte();
				int experience = incoming.readMEInt();
				int level = incoming.readUByte();
				experiences[skill] = experience;
				currentLevels[skill] = level;
				maximumLevels[skill] = 1;
				for (int i = 0; i < 98; i++) {
					if (experience >= SKILL_EXPERIENCE[i]) {
						maximumLevels[skill] = i + 2;
					}
				}

				opcode = -1;
				return true;
			}
			if (opcode == 71) {
				int id = incoming.readUShort();
				int slot = incoming.readUByteA();
				if (id == 65535) {
					id = -1;
				}
				inventoryTabIds[slot] = id;
				redrawTabArea = true;
				aBoolean1103 = true;
				opcode = -1;
				return true;
			}
			if (opcode == 74) {
				int music = incoming.readLEUShort();
				if (music == 65535) {
					music = -1;
				}
				if (music != nextMusicId && playingMusic && !lowMemory && songDelay == 0) {
					musicId = music;
					fadeMusic = true;
					provider.provide(2, musicId);
				}
				nextMusicId = music;
				opcode = -1;
				return true;
			}
			if (opcode == 121) {
				int id = incoming.readLEUShortA();
				int delay = incoming.readUShortA();
				if (playingMusic && !lowMemory) {
					musicId = id;
					fadeMusic = false;
					provider.provide(2, musicId);
					songDelay = delay;
				}
				opcode = -1;
				return true;
			}
			if (opcode == 109) {
				reset();
				opcode = -1;
				return false;
			}
			if (opcode == 70) {
				int horizontalOffset = incoming.readShort();
				int verticalOffset = incoming.readLEShort();
				int id = incoming.readLEUShort();
				Widget widget = Widget.widgets[id];
				widget.horizontalDrawOffset = horizontalOffset;
				widget.verticalDrawOffset = verticalOffset;
				opcode = -1;
				return true;
			}
			if (opcode == 73 || opcode == 241) {
				int regionX = this.regionX;
				int regionY = this.regionY;

				if (opcode == 73) {
					regionX = incoming.readUShortA();
					regionY = incoming.readUShort();
					constructedViewport = false;
				} else if (opcode == 241) {
					regionY = incoming.readUShortA();
					incoming.enableBitAccess();
					for (int z = 0; z < 4; z++) {
						for (int x = 0; x < 13; x++) {
							for (int y = 0; y < 13; y++) {
								int visible = incoming.readBits(1);
								if (visible == 1) {
									localRegions[z][x][y] = incoming.readBits(26);
								} else {
									localRegions[z][x][y] = -1;
								}
							}
						}
					}

					incoming.disableBitAccess();
					regionX = incoming.readUShort();
					constructedViewport = true;
				}
				if (this.regionX == regionX && this.regionY == regionY && loadingStage == 2) {
					opcode = -1;
					return true;
				}

				this.regionX = regionX;
				this.regionY = regionY;
				regionBaseX = (this.regionX - 6) * 8;
				regionBaseY = (this.regionY - 6) * 8;
				inPlayerOwnedHouse = false;

				if ((this.regionX / 8 == 48 || this.regionX / 8 == 49) && this.regionY / 8 == 48) {
					inPlayerOwnedHouse = true;
				}
				if (this.regionX / 8 == 48 && this.regionY / 8 == 148) {
					inPlayerOwnedHouse = true;
				}

				loadingStage = 1;
				loadingStartTime = System.currentTimeMillis();
				aClass15_1165.initializeRasterizer();
				frameFont.renderCentre(257, 151, "Loading - please wait.", 0);
				frameFont.renderCentre(256, 150, "Loading - please wait.", 0xffffff);
				aClass15_1165.drawImage(super.graphics, 4, 4);

				if (opcode == 73) {
					int regionCount = 0;
					for (int x = (this.regionX - 6) / 8; x <= (this.regionX + 6) / 8; x++) {
						for (int y = (this.regionY - 6) / 8; y <= (this.regionY + 6) / 8; y++) {
							regionCount++;
						}
					}

					localRegionMapData = new byte[regionCount][];
					localRegionLandscapeData = new byte[regionCount][];
					localRegionIds = new int[regionCount];
					localRegionMapIds = new int[regionCount];
					localRegionLandscapeIds = new int[regionCount];
					regionCount = 0;

					for (int x = (this.regionX - 6) / 8; x <= (this.regionX + 6) / 8; x++) {
						for (int y = (this.regionY - 6) / 8; y <= (this.regionY + 6) / 8; y++) {
							localRegionIds[regionCount] = (x << 8) + y;
							if (inPlayerOwnedHouse && (y == 49 || y == 149 || y == 147 || x == 50 || x == 49 && y == 47)) {
								localRegionMapIds[regionCount] = -1;
								localRegionLandscapeIds[regionCount] = -1;
								regionCount++;
							} else {
								int map = localRegionMapIds[regionCount] = provider.resolve(x, y, 0);
								if (map != -1) {
									provider.provide(3, map);
								}

								int landscape = localRegionLandscapeIds[regionCount] = provider.resolve(x, y, 1);
								if (landscape != -1) {
									provider.provide(3, landscape);
								}

								regionCount++;
							}
						}
					}
				}

				if (opcode == 241) {
					int regionCount = 0;
					int regionIds[] = new int[676];
					for (int z = 0; z < 4; z++) {
						for (int x = 0; x < 13; x++) {
							for (int y = 0; y < 13; y++) {
								int data = localRegions[z][x][y];

								if (data != -1) {
									int constructedRegionX = data >> 14 & 0x3ff;
									int constructedRegionY = data >> 3 & 0x7ff;
									int region = (constructedRegionX / 8 << 8) + constructedRegionY / 8;

									for (int index = 0; index < regionCount; index++) {
										if (regionIds[index] != region) {
											continue;
										}
										region = -1;
										break;
									}

									if (region != -1) {
										regionIds[regionCount++] = region;
									}
								}
							}
						}
					}

					localRegionMapData = new byte[regionCount][];
					localRegionLandscapeData = new byte[regionCount][];
					localRegionIds = new int[regionCount];
					localRegionMapIds = new int[regionCount];
					localRegionLandscapeIds = new int[regionCount];

					for (int index = 0; index < regionCount; index++) {
						int id = localRegionIds[index] = regionIds[index];
						int constructedRegionX = id >> 8 & 0xff;
						int constructedRegionY = id & 0xff;
						int map = localRegionMapIds[index] = provider.resolve(constructedRegionX, constructedRegionY, 0);

						if (map != -1) {
							provider.provide(3, map);
						}

						int landscape = localRegionLandscapeIds[index] = provider.resolve(constructedRegionX, constructedRegionY,
								1);
						if (landscape != -1) {
							provider.provide(3, landscape);
						}
					}
				}

				int dx = regionBaseX - previousAbsoluteX;
				int dy = regionBaseY - previousAbsoluteY;
				previousAbsoluteX = regionBaseX;
				previousAbsoluteY = regionBaseY;

				for (int index = 0; index < 16384; index++) {
					Npc npc = npcs[index];
					if (npc != null) {
						for (int point = 0; point < 10; point++) {
							npc.pathX[point] -= dx;
							npc.pathY[point] -= dy;
						}

						npc.worldX -= dx * 128;
						npc.worldY -= dy * 128;
					}
				}

				for (int index = 0; index < maximumPlayers; index++) {
					Player player = players[index];
					if (player != null) {
						for (int point = 0; point < 10; point++) {
							player.pathX[point] -= dx;
							player.pathY[point] -= dy;
						}

						player.worldX -= dx * 128;
						player.worldY -= dy * 128;
					}
				}

				validLocalMap = true;
				byte startX = 0;
				byte endX = 104;
				byte stepX = 1;

				if (dx < 0) {
					startX = 103;
					endX = -1;
					stepX = -1;
				}

				byte startY = 0;
				byte endY = 104;
				byte stepY = 1;

				if (dy < 0) {
					startY = 103;
					endY = -1;
					stepY = -1;
				}

				for (int x = startX; x != endX; x += stepX) {
					for (int y = startY; y != endY; y += stepY) {
						int shiftedX = x + dx;
						int shiftedY = y + dy;
						for (int plane = 0; plane < 4; plane++) {
							if (shiftedX >= 0 && shiftedY >= 0 && shiftedX < 104 && shiftedY < 104) {
								groundItems[plane][x][y] = groundItems[plane][shiftedX][shiftedY];
							} else {
								groundItems[plane][x][y] = null;
							}
						}
					}
				}

				for (SpawnedObject object = (SpawnedObject) spawns.getFront(); object != null; object = (SpawnedObject) spawns
						.getNext()) {
					object.setX(object.getX() - dx);
					object.setY(object.getY() - dy);
					if (object.getX() < 0 || object.getY() < 0 || object.getX() >= 104 || object.getY() >= 104) {
						object.unlink();
					}
				}

				if (destinationX != 0) {
					destinationX -= dx;
					destinationY -= dy;
				}

				oriented = false;
				opcode = -1;
				return true;
			}
			if (opcode == 208) {
				int id = incoming.readLEShort();
				if (id >= 0) {
					resetAnimation(id);
				}
				openWalkableInterface = id;
				opcode = -1;
				return true;
			}
			if (opcode == 99) {
				minimapState = incoming.readUByte();
				opcode = -1;
				return true;
			}
			if (opcode == 75) {
				int npc = incoming.readLEUShortA();
				int id = incoming.readLEUShortA();
				Widget.widgets[id].defaultMediaType = 2;
				Widget.widgets[id].defaultMedia = npc;
				opcode = -1;
				return true;
			}
			if (opcode == 114) {
				systemUpdateTime = incoming.readLEUShort() * 30;
				opcode = -1;
				return true;
			}
			if (opcode == 60) {
				localY = incoming.readUByte();
				localX = incoming.readNegUByte();
				while (incoming.getPosition() < packetSize) {
					int id = incoming.readUByte();
					parseRegionPackets(incoming, id);
				}
				opcode = -1;
				return true;
			}
			if (opcode == 35) {
				int parameter = incoming.readUByte();
				int jitter = incoming.readUByte();
				int amplitude = incoming.readUByte();
				int frequency = incoming.readUByte();
				aBooleanArray876[parameter] = true;
				anIntArray873[parameter] = jitter;
				anIntArray1203[parameter] = amplitude;
				anIntArray928[parameter] = frequency;
				anIntArray1030[parameter] = 0;
				opcode = -1;
				return true;
			}
			if (opcode == 174) {
				int id = incoming.readUShort();
				int loop = incoming.readUByte();
				int delay = incoming.readUShort();
				if (aBoolean848 && !lowMemory && trackCount < 50) {
					tracks[trackCount] = id;
					trackLoops[trackCount] = loop;
					trackDelays[trackCount] = delay + Track.delays[id];
					trackCount++;
				}
				opcode = -1;
				return true;
			}
			if (opcode == 104) {
				int slot = incoming.readNegUByte();
				int primary = incoming.readUByteA();
				String message = incoming.readString();
				if (slot >= 1 && slot <= 5) {
					if (message.equalsIgnoreCase("null")) {
						message = null;
					}
					aStringArray1127[slot - 1] = message;
					aBooleanArray1128[slot - 1] = primary == 0;
				}
				opcode = -1;
				return true;
			}
			if (opcode == 78) {
				destinationX = 0;
				opcode = -1;
				return true;
			}
			if (opcode == 253) {
				String message = incoming.readString();
				if (message.endsWith(":tradereq:")) {
					String name = message.substring(0, message.indexOf(":"));
					long encodedName = StringUtils.encodeBase37(name);
					boolean ignored = false;
					for (int i = 0; i < ignoredCount; i++) {
						if (ignores[i] != encodedName) {
							continue;
						}
						ignored = true;
						break;
					}

					if (!ignored && onTutorialIsland == 0) {
						addChatMessage(4, "wishes to trade with you.", name);
					}
				} else if (message.endsWith(":duelreq:")) {
					String name = message.substring(0, message.indexOf(":"));
					long encodedName = StringUtils.encodeBase37(name);
					boolean ignored = false;
					for (int i = 0; i < ignoredCount; i++) {
						if (ignores[i] != encodedName) {
							continue;
						}
						ignored = true;
						break;
					}

					if (!ignored && onTutorialIsland == 0) {
						addChatMessage(8, "wishes to duel with you.", name);
					}
				} else if (message.endsWith(":chalreq:")) {
					String name = message.substring(0, message.indexOf(":"));
					long encodedName = StringUtils.encodeBase37(name);
					boolean ignored = false;
					for (int i = 0; i < ignoredCount; i++) {
						if (ignores[i] != encodedName) {
							continue;
						}
						ignored = true;
						break;
					}

					if (!ignored && onTutorialIsland == 0) {
						String chatMessage = message.substring(message.indexOf(":") + 1, message.length() - 9);
						addChatMessage(8, chatMessage, name);
					}
				} else {
					addChatMessage(0, message, "");
				}
				opcode = -1;
				return true;
			}
			if (opcode == 1) {
				for (int index = 0; index < players.length; index++) {
					if (players[index] != null) {
						players[index].emoteAnimation = -1;
					}
				}

				for (int index = 0; index < npcs.length; index++) {
					if (npcs[index] != null) {
						npcs[index].emoteAnimation = -1;
					}
				}

				opcode = -1;
				return true;
			}
			if (opcode == 50) {
				long encodedName = incoming.readLong();
				int world = incoming.readUByte();
				String name = StringUtils.format(StringUtils.decodeBase37(encodedName));
				for (int player = 0; player < friendCount; player++) {
					if (encodedName != friends[player]) {
						continue;
					}
					if (friendWorlds[player] != world) {
						friendWorlds[player] = world;
						redrawTabArea = true;
						if (world > 0) {
							addChatMessage(5, name + " has logged in.", "");
						}
						if (world == 0) {
							addChatMessage(5, name + " has logged out.", "");
						}
					}
					name = null;
					break;
				}

				if (name != null && friendCount < 200) {
					friends[friendCount] = encodedName;
					friendUsernames[friendCount] = name;
					friendWorlds[friendCount] = world;
					friendCount++;
					redrawTabArea = true;
				}
				for (boolean flag6 = false; !flag6;) {
					flag6 = true;
					for (int i = 0; i < friendCount - 1; i++) {
						if (friendWorlds[i] != node && friendWorlds[i + 1] == node || friendWorlds[i] == 0
								&& friendWorlds[i + 1] != 0) {
							int j31 = friendWorlds[i];
							friendWorlds[i] = friendWorlds[i + 1];
							friendWorlds[i + 1] = j31;
							String s10 = friendUsernames[i];
							friendUsernames[i] = friendUsernames[i + 1];
							friendUsernames[i + 1] = s10;
							long l32 = friends[i];
							friends[i] = friends[i + 1];
							friends[i + 1] = l32;
							redrawTabArea = true;
							flag6 = false;
						}
					}
				}

				opcode = -1;
				return true;
			}
			if (opcode == 110) {
				if (tabId == 12) {
					redrawTabArea = true;
				}
				runEnergy = incoming.readUByte();
				opcode = -1;
				return true;
			}
			if (opcode == 254) {
				hintIconDrawType = incoming.readUByte();
				if (hintIconDrawType == 1) {
					hintedNpc = incoming.readUShort();
				} else if (hintIconDrawType >= 2 && hintIconDrawType <= 6) {
					if (hintIconDrawType == 2) {
						anInt937 = 64;
						anInt938 = 64;
					} else if (hintIconDrawType == 3) {
						anInt937 = 0;
						anInt938 = 64;
					} else if (hintIconDrawType == 4) {
						anInt937 = 128;
						anInt938 = 64;
					} else if (hintIconDrawType == 5) {
						anInt937 = 64;
						anInt938 = 0;
					} else if (hintIconDrawType == 6) {
						anInt937 = 64;
						anInt938 = 128;
					}

					hintIconDrawType = 2;
					anInt934 = incoming.readUShort();
					anInt935 = incoming.readUShort();
					anInt936 = incoming.readUByte();
				} else if (hintIconDrawType == 10) {
					lastInteractedWithPlayer = incoming.readUShort();
				}

				opcode = -1;
				return true;
			}
			if (opcode == 248) {
				int id = incoming.readUShortA();
				int overlay = incoming.readUShort();
				if (backDialogueId != -1) {
					backDialogueId = -1;
					redrawDialogueBox = true;
				}
				if (inputDialogueState != 0) {
					inputDialogueState = 0;
					redrawDialogueBox = true;
				}
				openInterfaceId = id;
				overlayInterfaceId = overlay;
				redrawTabArea = true;
				aBoolean1103 = true;
				aBoolean1149 = false;
				opcode = -1;
				return true;
			}
			if (opcode == 79) {
				int id = incoming.readLEUShort();
				int scrollPosition = incoming.readUShortA();
				Widget widget = Widget.widgets[id];
				if (widget != null && widget.group == 0) {
					if (scrollPosition < 0) {
						scrollPosition = 0;
					}
					if (scrollPosition > widget.scrollLimit - widget.height) {
						scrollPosition = widget.scrollLimit - widget.height;
					}
					widget.scrollPosition = scrollPosition;
				}
				opcode = -1;
				return true;
			}
			if (opcode == 68) {
				for (int i = 0; i < settings.length; i++) {
					if (settings[i] != anIntArray1045[i]) {
						settings[i] = anIntArray1045[i];
						updateVarp(i);
						redrawTabArea = true;
					}
				}

				opcode = -1;
				return true;
			}
			if (opcode == 196) {
				long name = incoming.readLong();
				int messageId = incoming.readInt();
				int privilege = incoming.readUByte();
				boolean invalid = false;
				for (int i = 0; i < 100; i++) {
					if (privateMessageIds[i] != messageId) {
						continue;
					}
					invalid = true;
					break;
				}

				if (privilege <= 1) {
					for (int i = 0; i < ignoredCount; i++) {
						if (ignores[i] != name) {
							continue;
						}
						invalid = true;
						break;
					}

				}
				if (!invalid && onTutorialIsland == 0) {
					try {
						privateMessageIds[privateMessageCount] = messageId;
						privateMessageCount = (privateMessageCount + 1) % 100;
						String message = ChatMessageCodec.decode(incoming, packetSize - 13);
						if (privilege != 3) {
							message = MessageCensor.apply(message);
						}
						if (privilege == 2 || privilege == 3) {
							addChatMessage(7, message, "@cr2@" + StringUtils.format(StringUtils.decodeBase37(name)));
						} else if (privilege == 1) {
							addChatMessage(7, message, "@cr1@" + StringUtils.format(StringUtils.decodeBase37(name)));
						} else {
							addChatMessage(3, message, StringUtils.format(StringUtils.decodeBase37(name)));
						}
					} catch (Exception exception1) {
						SignLink.reportError("cde1");
					}
				}
				opcode = -1;
				return true;
			}
			if (opcode == 85) {
				localY = incoming.readNegUByte();
				localX = incoming.readNegUByte();
				opcode = -1;
				return true;
			}
			if (opcode == 24) {
				flashingSidebarId = incoming.readUByteS();
				if (flashingSidebarId == tabId) {
					if (flashingSidebarId == 3) {
						tabId = 1;
					} else {
						tabId = 3;
					}
					redrawTabArea = true;
				}
				opcode = -1;
				return true;
			}
			if (opcode == 246) {
				int widget = incoming.readLEUShort();
				int scale = incoming.readUShort();
				int item = incoming.readUShort();
				if (item == 65535) {
					Widget.widgets[widget].defaultMediaType = 0;
					opcode = -1;
					return true;
				}

				ItemDefinition definition = ItemDefinition.lookup(item);
				Widget.widgets[widget].defaultMediaType = 4;
				Widget.widgets[widget].defaultMedia = item;
				Widget.widgets[widget].spritePitch = definition.getSpritePitch();
				Widget.widgets[widget].spriteRoll = definition.getSpriteCameraRoll();
				Widget.widgets[widget].spriteScale = definition.getSpriteScale() * 100 / scale;
				opcode = -1;
				return true;
			}
			if (opcode == 171) {
				boolean flag1 = incoming.readUByte() == 1;
				int id = incoming.readUShort();
				Widget.widgets[id].hidden = flag1;
				opcode = -1;
				return true;
			}
			if (opcode == 142) {
				int id = incoming.readLEUShort();
				resetAnimation(id);
				if (backDialogueId != -1) {
					backDialogueId = -1;
					redrawDialogueBox = true;
				}

				if (inputDialogueState != 0) {
					inputDialogueState = 0;
					redrawDialogueBox = true;
				}

				overlayInterfaceId = id;
				redrawTabArea = true;
				aBoolean1103 = true;
				openInterfaceId = -1;
				aBoolean1149 = false;
				opcode = -1;
				return true;
			}
			if (opcode == 126) {
				String text = incoming.readString();
				int id = incoming.readUShortA();
				Widget.widgets[id].defaultText = text;

				if (Widget.widgets[id].parent == inventoryTabIds[tabId]) {
					redrawTabArea = true;
				}

				opcode = -1;
				return true;
			}
			if (opcode == 206) {
				publicChatMode = incoming.readUByte();
				privateChatMode = incoming.readUByte();
				tradeChatMode = incoming.readUByte();
				aBoolean1233 = true;
				redrawDialogueBox = true;
				opcode = -1;
				return true;
			}
			if (opcode == 240) {
				if (tabId == 12) {
					redrawTabArea = true;
				}
				weight = incoming.readShort();
				opcode = -1;
				return true;
			}
			if (opcode == 8) {
				int id = incoming.readLEUShortA();
				int model = incoming.readUShort();
				Widget.widgets[id].defaultMediaType = 1;
				Widget.widgets[id].defaultMedia = model;
				opcode = -1;
				return true;
			}
			if (opcode == 122) {
				int id = incoming.readLEUShortA();
				int colour = incoming.readLEUShortA();
				int red = colour >> 10 & 0x1f;
				int green = colour >> 5 & 0x1f;
				int blue = colour & 0x1f;

				Widget.widgets[id].defaultColour = (red << 19) + (green << 11) + (blue << 3);
				opcode = -1;
				return true;
			}
			if (opcode == 53) {
				redrawTabArea = true;
				int widgetId = incoming.readUShort();
				Widget widget = Widget.widgets[widgetId];
				int itemCount = incoming.readUShort();
				for (int slot = 0; slot < itemCount; slot++) {
					int amount = incoming.readUByte();
					if (amount == 255) {
						amount = incoming.readIMEInt();
					}
					widget.inventoryIds[slot] = incoming.readLEUShortA();
					widget.inventoryAmounts[slot] = amount;
				}

				for (int slot = itemCount; slot < widget.inventoryIds.length; slot++) {
					widget.inventoryIds[slot] = 0;
					widget.inventoryAmounts[slot] = 0;
				}
				opcode = -1;
				return true;
			}
			if (opcode == 230) {
				int scale = incoming.readUShortA();
				int id = incoming.readUShort();
				int pitch = incoming.readUShort();
				int roll = incoming.readLEUShortA();
				Widget.widgets[id].spritePitch = pitch;
				Widget.widgets[id].spriteRoll = roll;
				Widget.widgets[id].spriteScale = scale;
				opcode = -1;
				return true;
			}
			if (opcode == 221) {
				friendServerStatus = incoming.readUByte();
				redrawTabArea = true;
				opcode = -1;
				return true;
			}
			if (opcode == 177) {
				oriented = true;
				anInt995 = incoming.readUByte(); // local x
				anInt996 = incoming.readUByte(); // local y
				anInt997 = incoming.readUShort(); // height offset
				anInt998 = incoming.readUByte();
				anInt999 = incoming.readUByte(); // some sort of magnitude?

				if (anInt999 >= 100) {
					int x = anInt995 * 128 + 64;
					int y = anInt996 * 128 + 64;
					int height = method42(x, y, plane) - anInt997;
					int dx = x - anInt858;
					int dz = height - anInt859;
					int dy = y - anInt860;
					int r = (int) Math.sqrt(dx * dx + dy * dy);

					anInt861 = (int) (Math.atan2(dz, r) * 325.94900000000001D) & 0x7ff; // some angle
					anInt862 = (int) (Math.atan2(dx, dy) * -325.94900000000001D) & 0x7ff; // some angle

					if (anInt861 < 128) {
						anInt861 = 128;
					} else if (anInt861 > 383) {
						anInt861 = 383;
					}
				}

				opcode = -1;
				return true;
			}
			if (opcode == 249) {
				member = incoming.readUByteA();
				localPlayerIndex = incoming.readLEUShortA();
				opcode = -1;
				return true;
			}
			if (opcode == 65) {
				synchronizeNpcs(incoming, packetSize);
				opcode = -1;
				return true;
			}
			if (opcode == 27) {
				messagePromptRaised = false;
				inputDialogueState = 1;
				aString1004 = "";
				redrawDialogueBox = true;
				opcode = -1;
				return true;
			}
			if (opcode == 187) {
				messagePromptRaised = false;
				inputDialogueState = 2;
				aString1004 = "";
				redrawDialogueBox = true;
				opcode = -1;
				return true;
			}
			if (opcode == 97) {
				int id = incoming.readUShort();
				resetAnimation(id);
				if (overlayInterfaceId != -1) {
					overlayInterfaceId = -1;
					redrawTabArea = true;
					aBoolean1103 = true;
				}
				if (backDialogueId != -1) {
					backDialogueId = -1;
					redrawDialogueBox = true;
				}
				if (inputDialogueState != 0) {
					inputDialogueState = 0;
					redrawDialogueBox = true;
				}
				openInterfaceId = id;
				aBoolean1149 = false;
				opcode = -1;
				return true;
			}
			if (opcode == 218) {
				dialogueId = incoming.readLEShortA();
				redrawDialogueBox = true;
				opcode = -1;
				return true;
			}
			if (opcode == 87) {
				int id = incoming.readLEUShort();
				int value = incoming.readMEInt();
				anIntArray1045[id] = value;
				if (settings[id] != value) {
					settings[id] = value;
					updateVarp(id);
					redrawTabArea = true;
					if (dialogueId != -1) {
						redrawDialogueBox = true;
					}
				}
				opcode = -1;
				return true;
			}
			if (opcode == 36) {
				int id = incoming.readLEUShort();
				byte value = incoming.readByte();
				anIntArray1045[id] = value;
				if (settings[id] != value) {
					settings[id] = value;
					updateVarp(id);
					redrawTabArea = true;
					if (dialogueId != -1) {
						redrawDialogueBox = true;
					}
				}
				opcode = -1;
				return true;
			}
			if (opcode == 61) {
				multicombat = incoming.readUByte();
				opcode = -1;
				return true;
			}
			if (opcode == 200) {
				int id = incoming.readUShort();
				int animation = incoming.readShort();
				Widget widget = Widget.widgets[id];
				widget.defaultAnimationId = animation;

				if (animation == -1) {
					widget.currentFrame = 0;
					widget.lastFrameTime = 0;
				}

				opcode = -1;
				return true;
			}
			if (opcode == 219) {
				if (overlayInterfaceId != -1) {
					overlayInterfaceId = -1;
					redrawTabArea = true;
					aBoolean1103 = true;
				}
				if (backDialogueId != -1) {
					backDialogueId = -1;
					redrawDialogueBox = true;
				}
				if (inputDialogueState != 0) {
					inputDialogueState = 0;
					redrawDialogueBox = true;
				}
				openInterfaceId = -1;
				aBoolean1149 = false;
				opcode = -1;
				return true;
			}
			if (opcode == 34) {
				redrawTabArea = true;
				int widgetId = incoming.readUShort();
				Widget widget = Widget.widgets[widgetId];
				while (incoming.getPosition() < packetSize) {
					int slot = incoming.readUSmart();
					int id = incoming.readUShort();
					int amount = incoming.readUByte();
					if (amount == 255) {
						amount = incoming.readInt();
					}
					if (slot >= 0 && slot < widget.inventoryIds.length) {
						widget.inventoryIds[slot] = id;
						widget.inventoryAmounts[slot] = amount;
					}
				}
				opcode = -1;
				return true;
			}
			if (opcode == 105 || opcode == 84 || opcode == 147 || opcode == 215 || opcode == 4 || opcode == 117 || opcode == 156
					|| opcode == 44 || opcode == 160 || opcode == 101 || opcode == 151) {
				parseRegionPackets(incoming, opcode);
				opcode = -1;
				return true;
			}
			if (opcode == 106) { // tab interface
				tabId = incoming.readNegUByte();
				redrawTabArea = true;
				aBoolean1103 = true;
				opcode = -1;
				return true;
			}
			if (opcode == 164) {
				int id = incoming.readLEUShort();
				resetAnimation(id);
				if (overlayInterfaceId != -1) {
					overlayInterfaceId = -1;
					redrawTabArea = true;
					aBoolean1103 = true;
				}

				backDialogueId = id;
				redrawDialogueBox = true;
				openInterfaceId = -1;
				aBoolean1149 = false;
				opcode = -1;
				return true;
			}
			System.out.println("T1 - unrecognised packet error - opcode: " + opcode + ", packet size:" + packetSize
					+ ", previous opcode: " + secondLastOpcode + ", third last opcode: " + thirdLastOpcode);

			// SignLink.reportError("T1 - " + opcode + "," + packetSize + " - " + anInt842 + "," + anInt843);
			reset();
		} catch (IOException ex) {
			attemptReconnection();
			ex.printStackTrace();
		} catch (Exception exception) {
			System.out.println("T2 - exception - opcode " + opcode + ", previous opcode: " + secondLastOpcode
					+ ", third last opcode: " + thirdLastOpcode + "\n" + exception.getMessage() + "\n"
					+ exception.getStackTrace());

			String s2 = "T2 - " + opcode + "," + secondLastOpcode + "," + thirdLastOpcode + " - " + packetSize + ","
					+ (regionBaseX + localPlayer.pathX[0]) + "," + (regionBaseY + localPlayer.pathY[0]) + " - ";
			for (int j15 = 0; j15 < packetSize && j15 < 50; j15++) {
				s2 = s2 + incoming.getPayload()[j15] + ",";
			}

			exception.printStackTrace();

			SignLink.reportError(s2);
			reset();
		}
		return true;
	}

	public final void parseRegionPackets(Buffer buffer, int opcode) {
		if (opcode == 84) {
			int offset = buffer.readUByte();
			int x = localX + (offset >> 4 & 7);
			int y = localY + (offset & 7);
			int id = buffer.readUShort();
			int oldAmount = buffer.readUShort();
			int newAmount = buffer.readUShort();
			if (x >= 0 && y >= 0 && x < 104 && y < 104) {
				Deque deque = groundItems[plane][x][y];
				if (deque != null) {
					for (Item item = (Item) deque.getFront(); item != null; item = (Item) deque.getNext()) {
						if (item.getId() == (id & 0x7fff) && item.getAmount() == oldAmount) {
							item.setAmount(newAmount);
							break;
						}
					}

					processGroundItems(x, y); // update the tile
				}
			}
			return;
		}
		if (opcode == 105) {
			int positionOffset = buffer.readUByte();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int id = buffer.readUShort();
			int data = buffer.readUByte();
			int positionDelta = data >> 4 & 0xf;
			int loop = data & 7;
			if (localPlayer.pathX[0] >= x - positionDelta && localPlayer.pathX[0] <= x + positionDelta
					&& localPlayer.pathY[0] >= y - positionDelta && localPlayer.pathY[0] <= y + positionDelta && aBoolean848
					&& !lowMemory && trackCount < 50) {
				tracks[trackCount] = id;
				trackLoops[trackCount] = loop;
				trackDelays[trackCount] = Track.delays[id];
				trackCount++;
			}
		}
		if (opcode == 215) {
			int id = buffer.readUShortA();
			int positionOffset = buffer.readUByteS();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int index = buffer.readUShortA();
			int amount = buffer.readUShort();
			if (x >= 0 && y >= 0 && x < 104 && y < 104 && index != localPlayerIndex) {
				Item item = new Item();
				item.setId(id);
				item.setAmount(amount);
				if (groundItems[plane][x][y] == null) {
					groundItems[plane][x][y] = new Deque();
				}

				groundItems[plane][x][y].pushBack(item);
				processGroundItems(x, y);
			}
			return;
		}
		if (opcode == 156) {
			int positionOffset = buffer.readUByteA();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int id = buffer.readUShort();
			if (x >= 0 && y >= 0 && x < 104 && y < 104) {
				Deque items = groundItems[plane][x][y];
				if (items != null) {
					for (Item item = (Item) items.getFront(); item != null; item = (Item) items.getNext()) {
						if (item.getId() == (id & 0x7fff)) {
							item.unlink();
							break;
						}
					}

					if (items.getFront() == null) {
						groundItems[plane][x][y] = null;
					}
					processGroundItems(x, y);
				}
			}
			return;
		}
		if (opcode == 160) {
			int positionOffset = buffer.readUByteS();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int packed = buffer.readUByteS();
			int type = packed >> 2;
			int orientation = packed & 3;
			int group = OBJECT_GROUPS[type];
			int animation = buffer.readUShortA();

			if (x >= 0 && y >= 0 && x < 103 && y < 103) {
				int j18 = tileHeights[plane][x][y];
				int i19 = tileHeights[plane][x + 1][y];
				int l19 = tileHeights[plane][x + 1][y + 1];
				int k20 = tileHeights[plane][x][y + 1];

				if (group == 0) {
					Wall wall = scene.getTileWall(x, y, plane);
					if (wall != null) {
						int id = wall.getKey() >> 14 & 0x7fff;
						if (type == 2) {
							wall.setPrimary(new RenderableObject(id, 4 + orientation, type, j18, i19, l19, k20, animation, false));
							wall.setSecondary(new RenderableObject(id, orientation + 1 & 3, type, j18, i19, l19, k20, animation,
									false));
						} else {
							wall.setPrimary(new RenderableObject(id, orientation, type, j18, i19, l19, k20, animation, false));
						}
					}
				} else if (group == 1) {
					WallDecoration decoration = scene.getTileWallDecoration(x, y, plane);
					if (decoration != null) {
						decoration.setRenderable(new RenderableObject(decoration.getKey() >> 14 & 0x7fff, 0, 4, j18, i19, l19,
								k20, animation, false));
					}
				} else if (group == 2) {
					GameObject object = scene.firstGameObject(x, y, plane);
					if (type == 11) {
						type = 10;
					}
					if (object != null) {
						object.renderable = new RenderableObject(object.key >> 14 & 0x7fff, orientation, type, j18, i19, l19,
								k20, animation, false);
					}
				} else if (group == 3) {
					GroundDecoration decoration = scene.getTileFloorDecoration(x, y, plane);
					if (decoration != null) {
						decoration.setRenderable(new RenderableObject(decoration.getKey() >> 14 & 0x7fff, orientation, 22, j18,
								i19, l19, k20, animation, false));
					}
				}
			}
			return;
		}
		if (opcode == 147) {
			int positionOffset = buffer.readUByteS();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int index = buffer.readUShort();
			byte byte0 = buffer.readByteS();
			int delay = buffer.readLEUShort();
			byte byte1 = buffer.readNegByte();
			int length = buffer.readUShort(); // time in ticks until the player transforms again
			int packed = buffer.readUByteS();
			int type = packed >> 2;
			int orientation = packed & 3;
			int group = OBJECT_GROUPS[type];
			byte byte2 = buffer.readByte();
			int id = buffer.readUShort();
			byte byte3 = buffer.readNegByte();
			Player player = (index == localPlayerIndex) ? localPlayer : players[index];

			if (player != null) {
				ObjectDefinition definition = ObjectDefinition.lookup(id);
				int i22 = tileHeights[plane][x][y];
				int j22 = tileHeights[plane][x + 1][y];
				int k22 = tileHeights[plane][x + 1][y + 1];
				int l22 = tileHeights[plane][x][y + 1];
				Model model = definition.modelAt(type, orientation, i22, j22, k22, l22, -1);
				if (model != null) {
					spawnObject(-1, x, y, plane, group, 0, length + 1, 0, delay + 1);
					player.objectAppearanceStartTick = delay + tick;
					player.objectAppearanceEndTick = length + tick;
					player.objectModel = model;
					int dx = definition.getWidth();
					int dy = definition.getLength();
					if (orientation == 1 || orientation == 3) {
						dx = definition.getLength();
						dy = definition.getWidth();
					}

					player.anInt1711 = x * 128 + dx * 64;
					player.anInt1713 = y * 128 + dy * 64;
					player.anInt1712 = method42(player.anInt1711, player.anInt1713, plane);
					if (byte2 > byte0) {
						byte byte4 = byte2;
						byte2 = byte0;
						byte0 = byte4;
					}
					if (byte3 > byte1) {
						byte byte5 = byte3;
						byte3 = byte1;
						byte1 = byte5;
					}
					player.anInt1719 = x + byte2;
					player.anInt1721 = x + byte0;
					player.anInt1720 = y + byte3;
					player.anInt1722 = y + byte1;
				}
			}
		}
		if (opcode == 151) { // add game object
			int positionOffset = buffer.readUByteA();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int id = buffer.readLEUShort();
			int info = buffer.readUByteS();
			int type = info >> 2;
			int orientation = info & 3;
			int group = OBJECT_GROUPS[type];

			if (x >= 0 && y >= 0 && x < 104 && y < 104) {
				spawnObject(id, x, y, plane, group, orientation, -1, type, 0);
			}
			return;
		}
		if (opcode == 4) {
			int positionOffset = buffer.readUByte();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);
			int graphic = buffer.readUShort();
			int renderOffset = buffer.readUByte();
			int delay = buffer.readUShort();
			if (x >= 0 && y >= 0 && x < 104 && y < 104) {
				x = x * 128 + 64;
				y = y * 128 + 64;
				AnimableObject object = new AnimableObject(x, y, plane, method42(x, y, plane) - renderOffset, graphic, delay,
						tick);
				incompleteAnimables.pushBack(object);
			}
			return;
		}
		if (opcode == 44) {
			int id = buffer.readLEUShortA();
			int amount = buffer.readUShort();
			int positionOffset = buffer.readUByte();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);

			if (x >= 0 && y >= 0 && x < 104 && y < 104) {
				Item item = new Item();
				item.setId(id);
				item.setAmount(amount);
				if (groundItems[plane][x][y] == null) {
					groundItems[plane][x][y] = new Deque();
				}
				groundItems[plane][x][y].pushBack(item);
				processGroundItems(x, y);
			}
			return;
		}
		if (opcode == 101) {
			int info = buffer.readNegUByte();
			int group = info >> 2;
			int orientation = info & 3;
			int type = OBJECT_GROUPS[group];
			int positionOffset = buffer.readUByte();
			int x = localX + (positionOffset >> 4 & 7);
			int y = localY + (positionOffset & 7);

			if (x >= 0 && y >= 0 && x < 104 && y < 104) {
				spawnObject(-1, x, y, plane, type, orientation, -1, group, 0);
			}
			return;
		}
		if (opcode == 117) {
			int positionOffset = buffer.readUByte();
			int sourceX = localX + (positionOffset >> 4 & 7);
			int sourceY = localY + (positionOffset & 7);
			int destinationX = sourceX + buffer.readByte();
			int destinationY = sourceY + buffer.readByte();
			int target = buffer.readShort();
			int graphic = buffer.readUShort();
			int sourceElevationOffset = buffer.readUByte() * 4;
			int destinationElevation = buffer.readUByte() * 4;
			int ticksToEnd = buffer.readUShort();
			int ticksToStart = buffer.readUShort();
			int elevationPitch = buffer.readUByte();
			int leapScale = buffer.readUByte();
			if (sourceX >= 0 && sourceY >= 0 && sourceX < 104 && sourceY < 104 && destinationX >= 0 && destinationY >= 0
					&& destinationX < 104 && destinationY < 104 && graphic != 65535) {
				sourceX = sourceX * 128 + 64;
				sourceY = sourceY * 128 + 64;
				destinationX = destinationX * 128 + 64;
				destinationY = destinationY * 128 + 64;
				Projectile projectile = new Projectile(sourceX, sourceY, method42(sourceX, sourceY, plane)
						- sourceElevationOffset, destinationElevation, elevationPitch, ticksToStart + tick, ticksToEnd + tick,
						leapScale, plane, target, graphic);
				projectile.target(destinationX, destinationY, method42(destinationX, destinationY, plane) - destinationElevation,
						ticksToEnd + tick);
				projectiles.pushBack(projectile);
			}
		}
	}

	public final void processAnimableObjects() {
		AnimableObject object = (AnimableObject) incompleteAnimables.getFront();

		for (; object != null; object = (AnimableObject) incompleteAnimables.getNext()) {
			if (object.getZ() != plane || object.isTransformationCompleted()) {
				object.unlink();
			} else if (tick >= object.getTick()) {
				object.nextAnimationStep(tickDelta);
				if (object.isTransformationCompleted()) {
					object.unlink();
				} else {
					scene.addEntity(object.getX(), object.getY(), object.getZ(), object, 0, -1, object.getRenderHeight(), 60,
							false);
				}
			}
		}
	}

	public final void processChatMessageClick(int j) {
		int l = 0;
		for (int index = 0; index < 100; index++) {
			if (chatMessages[index] == null) {
				continue;
			}

			int type = chatTypes[index];
			int k1 = 70 - l * 14 + anInt1089 + 4;
			if (k1 < -20) {
				break;
			}

			String name = chatPlayerNames[index];
			if (name != null && name.startsWith("@cr1@")) {
				name = name.substring(5);
			}
			if (name != null && name.startsWith("@cr2@")) {
				name = name.substring(5);
			}
			if (type == 0) {
				l++;
			}

			if ((type == 1 || type == 2) && (type == 1 || publicChatMode == 0 || publicChatMode == 1 && displayMessageFrom(name))) {
				if (j > k1 - 14 && j <= k1 && !name.equals(localPlayer.name)) {
					if (playerPrivelage >= 1) {
						menuActionTexts[menuActionRow] = "Report abuse @whi@" + name;
						menuActionTypes[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionTexts[menuActionRow] = "Add ignore @whi@" + name;
					menuActionTypes[menuActionRow] = Actions.ADD_IGNORE;
					menuActionRow++;
					menuActionTexts[menuActionRow] = "Add friend @whi@" + name;
					menuActionTypes[menuActionRow] = Actions.ADD_FRIEND;
					menuActionRow++;
				}
				l++;
			}

			if ((type == 3 || type == 7) && anInt1195 == 0
					&& (type == 7 || privateChatMode == 0 || privateChatMode == 1 && displayMessageFrom(name))) {
				if (j > k1 - 14 && j <= k1) {
					if (playerPrivelage >= 1) {
						menuActionTexts[menuActionRow] = "Report abuse @whi@" + name;
						menuActionTypes[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionTexts[menuActionRow] = "Add ignore @whi@" + name;
					menuActionTypes[menuActionRow] = Actions.ADD_IGNORE;
					menuActionRow++;
					menuActionTexts[menuActionRow] = "Add friend @whi@" + name;
					menuActionTypes[menuActionRow] = Actions.ADD_FRIEND;
					menuActionRow++;
				}
				l++;
			}

			if (type == 4 && (tradeChatMode == 0 || tradeChatMode == 1 && displayMessageFrom(name))) {
				if (j > k1 - 14 && j <= k1) {
					menuActionTexts[menuActionRow] = "Accept trade @whi@" + name;
					menuActionTypes[menuActionRow] = Actions.ACCEPT_TRADE;
					menuActionRow++;
				}
				l++;
			}

			if ((type == 5 || type == 6) && anInt1195 == 0 && privateChatMode < 2) {
				l++;
			}

			if (type == 8 && (tradeChatMode == 0 || tradeChatMode == 1 && displayMessageFrom(name))) {
				if (j > k1 - 14 && j <= k1) {
					menuActionTexts[menuActionRow] = "Accept challenge @whi@" + name;
					menuActionTypes[menuActionRow] = Actions.ACCEPT_CHALLENGE;
					menuActionRow++;
				}
				l++;
			}
		}
	}

	public final boolean processClickedObject(int key, int y, int x) {
		int id = key >> 14 & 0x7fff;
		int config = scene.getConfig(x, y, plane, key);
		if (config == -1) {
			return false;
		}

		int type = config & 0x1f;
		int orientation = config >> 6 & 3;

		if (type == 10 || type == 11 || type == 22) {
			ObjectDefinition definition = ObjectDefinition.lookup(id);
			int width;
			int length;

			if (orientation == 0 || orientation == 2) {
				width = definition.getWidth();
				length = definition.getLength();
			} else {
				width = definition.getLength();
				length = definition.getWidth();
			}

			int surroundings = definition.getSurroundings();
			if (orientation != 0) {
				surroundings = (surroundings << orientation & 0xF) + (surroundings >> 4 - orientation);
			}

			walk(2, 0, length, 0, localPlayer.pathY[0], width, surroundings, y, localPlayer.pathX[0], false, x);
		} else {
			walk(2, orientation, 0, type + 1, localPlayer.pathY[0], 0, 0, y, localPlayer.pathX[0], false, x);
		}

		anInt914 = super.lastClickX;
		anInt915 = super.lastClickY;
		anInt917 = 2;
		anInt916 = 0;
		return true;
	}

	public final boolean processFriendListClick(Widget widget) {
		int row = widget.contentType;
		if (row >= 1 && row <= 200 || row >= 701 && row <= 900) {
			if (row >= 801) {
				row -= 701;
			} else if (row >= 701) {
				row -= 601;
			} else if (row >= 101) {
				row -= 101;
			} else {
				row--;
			}

			menuActionTexts[menuActionRow] = "Remove @whi@" + friendUsernames[row];
			menuActionTypes[menuActionRow] = Actions.REMOVE_FRIEND;
			menuActionRow++;
			menuActionTexts[menuActionRow] = "Message @whi@" + friendUsernames[row];
			menuActionTypes[menuActionRow] = Actions.PRIVATE_MESSAGE;
			menuActionRow++;
			return true;
		}

		if (row >= 401 && row <= 500) {
			menuActionTexts[menuActionRow] = "Remove @whi@" + widget.defaultText;
			menuActionTypes[menuActionRow] = Actions.REMOVE_IGNORE;
			menuActionRow++;
			return true;
		}

		return false;
	}

	public final void processGroundItems(int x, int y) {
		Deque deque = groundItems[plane][x][y];
		if (deque == null) {
			scene.clearGroundItem(x, y, plane);
			return;
		}
		int maxValue = 0xfa0a1f01;
		Item mostValuable = null;
		for (Item item = (Item) deque.getFront(); item != null; item = (Item) deque.getNext()) {
			ItemDefinition definition = ItemDefinition.lookup(item.getId());
			int value = definition.getValue();
			if (definition.isStackable()) {
				value *= item.getAmount() + 1;
			}
			if (value > maxValue) {
				maxValue = value;
				mostValuable = item;
			}
		}

		deque.pushFront(mostValuable);
		Item first = null;
		Item second = null;
		for (Item item = (Item) deque.getFront(); item != null; item = (Item) deque.getNext()) {
			if (item.getId() != mostValuable.getId() && first == null) {
				first = item;
			}
			if (item.getId() != mostValuable.getId() && item.getId() != first.getId() && second == null) {
				second = item;
			}
		}

		int key = x + (y << 7) + 0x60000000;
		scene.addGroundItem(x, y, plane, key, mostValuable, first, second, method42(x * 128 + 64, y * 128 + 64, plane));
	}

	public final void processKeyInput() {
		do {
			int key = nextPressedKey();
			if (key == -1) {
				break;
			}
			if (openInterfaceId != -1 && openInterfaceId == anInt1178) {
				if (key == 8 && reportInput.length() > 0) {
					reportInput = reportInput.substring(0, reportInput.length() - 1);
				}
				if ((key >= 97 && key <= 122 || key >= 65 && key <= 90 || key >= 48 && key <= 57 || key == 32)
						&& reportInput.length() < 12) {
					reportInput += (char) key;
				}
			} else if (messagePromptRaised) {
				if (key >= 32 && key <= 122 && aString1212.length() < 80) {
					aString1212 += (char) key;
					redrawDialogueBox = true;
				}
				if (key == 8 && aString1212.length() > 0) {
					aString1212 = aString1212.substring(0, aString1212.length() - 1);
					redrawDialogueBox = true;
				}
				if (key == 13 || key == 10) {
					messagePromptRaised = false;
					redrawDialogueBox = true;
					if (anInt1064 == 1) {
						long friend = StringUtils.encodeBase37(aString1212);
						addFriend(friend);
					}
					if (anInt1064 == 2 && friendCount > 0) {
						long friend = StringUtils.encodeBase37(aString1212);
						removeFriend(friend);
					}
					if (anInt1064 == 3 && aString1212.length() > 0) {
						outgoing.writeOpcode(126);
						outgoing.writeByte(0);
						int k = outgoing.getPosition();
						outgoing.writeLong(aLong953);
						ChatMessageCodec.encode(aString1212, outgoing);
						outgoing.writeSizeByte(outgoing.getPosition() - k);
						aString1212 = ChatMessageCodec.verify(aString1212);
						aString1212 = MessageCensor.apply(aString1212);
						addChatMessage(6, aString1212, StringUtils.format(StringUtils.decodeBase37(aLong953)));
						if (privateChatMode == 2) {
							privateChatMode = 1;
							aBoolean1233 = true;
							outgoing.writeOpcode(95);
							outgoing.writeByte(publicChatMode);
							outgoing.writeByte(privateChatMode);
							outgoing.writeByte(tradeChatMode);
						}
					}
					if (anInt1064 == 4 && ignoredCount < 100) {
						long l2 = StringUtils.encodeBase37(aString1212);
						addIgnore(l2);
					}
					if (anInt1064 == 5 && ignoredCount > 0) {
						long l3 = StringUtils.encodeBase37(aString1212);
						removeIgnore(l3);
					}
				}
			} else if (inputDialogueState == 1) {
				if (key >= 48 && key <= 57 && aString1004.length() < 10) {
					aString1004 += (char) key;
					redrawDialogueBox = true;
				}
				if (key == 8 && aString1004.length() > 0) {
					aString1004 = aString1004.substring(0, aString1004.length() - 1);
					redrawDialogueBox = true;
				}
				if (key == 13 || key == 10) {
					if (aString1004.length() > 0) {
						int amount = 0;
						try {
							amount = Integer.parseInt(aString1004);
						} catch (Exception _ex) {
						}
						outgoing.writeOpcode(208);
						outgoing.writeInt(amount);
					}
					inputDialogueState = 0;
					redrawDialogueBox = true;
				}
			} else if (inputDialogueState == 2) {
				if (key >= 32 && key <= 122 && aString1004.length() < 12) {
					aString1004 += (char) key;
					redrawDialogueBox = true;
				}
				if (key == 8 && aString1004.length() > 0) {
					aString1004 = aString1004.substring(0, aString1004.length() - 1);
					redrawDialogueBox = true;
				}
				if (key == 13 || key == 10) {
					if (aString1004.length() > 0) {
						outgoing.writeOpcode(60);
						outgoing.writeLong(StringUtils.encodeBase37(aString1004));
					}
					inputDialogueState = 0;
					redrawDialogueBox = true;
				}
			} else if (backDialogueId == -1) {
				if (key >= 32 && key <= 122 && input.length() < 80) {
					input += (char) key;
					redrawDialogueBox = true;
				}
				if (key == 8 && input.length() > 0) {
					input = input.substring(0, input.length() - 1);
					redrawDialogueBox = true;
				}
				if ((key == 13 || key == 10) && input.length() > 0) {
					if (playerPrivelage == 2) {
						if (input.equals("::clientdrop")) {
							attemptReconnection();
						}
						if (input.equals("::lag")) {
							debug();
						}

						if (input.equals("::prefetchmusic")) {
							for (int file = 0; file < provider.getCount(2); file++) {
								provider.requestExtra(2, file, (byte) 1);
							}
						}
						if (input.equals("::fpson")) {
							displayFps = true;
						}
						if (input.equals("::fpsoff")) {
							displayFps = false;
						}
						if (input.equals("::noclip")) {
							for (int z = 0; z < 4; z++) {
								for (int x = 1; x < 103; x++) {
									for (int y = 1; y < 103; y++) {
										collisionMaps[z].adjacencies[x][y] = 0;
									}
								}
							}
						}
						if (input.equals("::pripos")) {
							System.out.println(localPlayer.worldX);
							System.out.println(localPlayer.worldY);
						}
					}
					if (input.startsWith("::")) {
						outgoing.writeOpcode(103);
						outgoing.writeByte(input.length() - 1);
						outgoing.writeJString(input.substring(2));
					} else {
						String s = input.toLowerCase();
						int colour = 0;
						if (s.startsWith("yellow:")) {
							colour = 0;
							input = input.substring(7);
						} else if (s.startsWith("red:")) {
							colour = 1;
							input = input.substring(4);
						} else if (s.startsWith("green:")) {
							colour = 2;
							input = input.substring(6);
						} else if (s.startsWith("cyan:")) {
							colour = 3;
							input = input.substring(5);
						} else if (s.startsWith("purple:")) {
							colour = 4;
							input = input.substring(7);
						} else if (s.startsWith("white:")) {
							colour = 5;
							input = input.substring(6);
						} else if (s.startsWith("flash1:")) {
							colour = 6;
							input = input.substring(7);
						} else if (s.startsWith("flash2:")) {
							colour = 7;
							input = input.substring(7);
						} else if (s.startsWith("flash3:")) {
							colour = 8;
							input = input.substring(7);
						} else if (s.startsWith("glow1:")) {
							colour = 9;
							input = input.substring(6);
						} else if (s.startsWith("glow2:")) {
							colour = 10;
							input = input.substring(6);
						} else if (s.startsWith("glow3:")) {
							colour = 11;
							input = input.substring(6);
						}
						s = input.toLowerCase();
						int effect = 0;
						if (s.startsWith("wave:")) {
							effect = 1;
							input = input.substring(5);
						} else if (s.startsWith("wave2:")) {
							effect = 2;
							input = input.substring(6);
						} else if (s.startsWith("shake:")) {
							effect = 3;
							input = input.substring(6);
						} else if (s.startsWith("scroll:")) {
							effect = 4;
							input = input.substring(7);
						} else if (s.startsWith("slide:")) {
							effect = 5;
							input = input.substring(6);
						}
						outgoing.writeOpcode(4);
						outgoing.writeByte(0);
						int start = outgoing.getPosition();
						outgoing.writeByteS(effect);
						outgoing.writeByteS(colour);
						chatBuffer.setPosition(0);
						ChatMessageCodec.encode(input, chatBuffer);
						outgoing.writeReverseDataA(chatBuffer.getPayload(), 0, chatBuffer.getPosition());
						outgoing.writeSizeByte(outgoing.getPosition() - start);
						input = ChatMessageCodec.verify(input);
						input = MessageCensor.apply(input);
						localPlayer.spokenText = input;
						localPlayer.textColour = colour;
						localPlayer.textEffect = effect;
						localPlayer.textCycle = 150;
						if (playerPrivelage == 2) {
							addChatMessage(2, localPlayer.spokenText, "@cr2@" + localPlayer.name);
						} else if (playerPrivelage == 1) {
							addChatMessage(2, localPlayer.spokenText, "@cr1@" + localPlayer.name);
						} else {
							addChatMessage(2, localPlayer.spokenText, localPlayer.name);
						}
						if (publicChatMode == 2) {
							publicChatMode = 3;
							aBoolean1233 = true;
							outgoing.writeOpcode(95);
							outgoing.writeByte(publicChatMode);
							outgoing.writeByte(privateChatMode);
							outgoing.writeByte(tradeChatMode);
						}
					}
					input = "";
					redrawDialogueBox = true;
				}
			}
		} while (true);
	}

	public final void processLoadedResources() {
		do {
			Resource request;
			do {
				request = provider.next();
				if (request == null) {
					return;
				}

				if (request.getType() == 0) {
					Model.load(request.getData(), request.getFile());
					if ((provider.getModelAttributes(request.getFile()) & 0x62) != 0) {
						redrawTabArea = true;

						if (backDialogueId != -1) {
							redrawDialogueBox = true;
						}
					}
				}

				if (request.getType() == 1 && request.getData() != null) {
					Frame.load(request.getData());
				} else if (request.getType() == 2 && request.getFile() == musicId && request.getData() != null) {
					midiSave(request.getData(), fadeMusic);
				}

				if (request.getType() == 3 && loadingStage == 1) {
					for (int index = 0; index < localRegionMapData.length; index++) {
						if (localRegionMapIds[index] == request.getFile()) {
							localRegionMapData[index] = request.getData();

							if (request.getData() == null) {
								localRegionMapIds[index] = -1;
							}

							break;
						}

						if (localRegionLandscapeIds[index] != request.getFile()) {
							continue;
						}

						localRegionLandscapeData[index] = request.getData();
						if (request.getData() == null) {
							localRegionLandscapeIds[index] = -1;
						}

						break;
					}

				}
			} while (request.getType() != 93 || !provider.landscapePresent(request.getFile()));

			MapRegion.decode(new Buffer(request.getData()), provider);
		} while (true);
	}

	public final void processMenuActions(int id) {
		if (id < 0) {
			return;
		} else if (inputDialogueState != 0) {
			inputDialogueState = 0;
			redrawDialogueBox = true;
		}

		int first = firstMenuOperand[id];
		int second = secondMenuOperand[id];
		int action = menuActionTypes[id];
		int clicked = selectedMenuActions[id];

		if (action >= 2000) {
			action -= 2000;
		}

		if (action == 582) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(57); // item on npc
				outgoing.writeShortA(anInt1285); // id
				outgoing.writeShortA(clicked); // npc index
				outgoing.writeLEShort(anInt1283); // slot
				outgoing.writeShortA(anInt1284); // container widget id
			}
		} else if (action == 234) {
			boolean flag1 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag1) {
				flag1 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(236);
			outgoing.writeLEShort(second + regionBaseY);
			outgoing.writeShort(clicked);
			outgoing.writeLEShort(first + regionBaseX);
		}
		if (action == 62 && processClickedObject(clicked, second, first)) {
			outgoing.writeOpcode(192);
			outgoing.writeShort(anInt1284);
			outgoing.writeLEShort(clicked >> 14 & 0x7fff); // object
			outgoing.writeLEShortA(second + regionBaseY); // y
			outgoing.writeLEShort(anInt1283); // slot
			outgoing.writeLEShortA(first + regionBaseX); // x
			outgoing.writeShort(anInt1285); // id
		}
		if (action == 511) {
			boolean flag2 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag2) {
				flag2 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(25);
			outgoing.writeLEShort(anInt1284);
			outgoing.writeShortA(anInt1285);
			outgoing.writeShort(clicked);
			outgoing.writeShortA(second + regionBaseY);
			outgoing.writeLEShortA(anInt1283);
			outgoing.writeShort(first + regionBaseX);
		}
		if (action == 74) {
			outgoing.writeOpcode(122);
			outgoing.writeLEShortA(second);
			outgoing.writeShortA(first);
			outgoing.writeLEShort(clicked);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 315) {
			Widget widget = Widget.widgets[second];
			boolean flag8 = true;
			if (widget.contentType > 0) {
				flag8 = method48(widget);
			}

			if (flag8) {
				outgoing.writeOpcode(185);
				outgoing.writeShort(second);
			}
		}
		if (action == 561) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				anInt1188 += clicked;
				if (anInt1188 >= 90) {
					outgoing.writeOpcode(136);
					anInt1188 = 0;
				}
				outgoing.writeOpcode(128);
				outgoing.writeShort(clicked);
			}
		}
		if (action == 20) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(155);
				outgoing.writeLEShort(clicked);
			}
		}
		if (action == 779) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(153);
				outgoing.writeLEShort(clicked);
			}
		}
		if (action == 516) {
			if (!menuOpen) {
				scene.method312(super.lastClickY - 4, super.lastClickX - 4);
			} else {
				scene.method312(second - 4, first - 4);
			}
		}
		if (action == 1062) {
			anInt924 += regionBaseX;
			if (anInt924 >= 113) {
				outgoing.writeOpcode(183);
				outgoing.writeTriByte(0xe63271);
				anInt924 = 0;
			}
			processClickedObject(clicked, second, first);
			outgoing.writeOpcode(228);
			outgoing.writeShortA(clicked >> 14 & 0x7fff);
			outgoing.writeShortA(second + regionBaseY);
			outgoing.writeShort(first + regionBaseX);
		}
		if (action == Actions.CLICK_TO_CONTINUE && !aBoolean1149) {
			outgoing.writeOpcode(40);
			outgoing.writeShort(second);
			aBoolean1149 = true;
		}
		if (action == 431) {
			outgoing.writeOpcode(129);
			outgoing.writeShortA(first);
			outgoing.writeShort(second);
			outgoing.writeShortA(clicked);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}

		if (action == Actions.ADD_FRIEND || action == Actions.ADD_IGNORE || action == Actions.REMOVE_FRIEND
				|| action == Actions.REMOVE_IGNORE) {
			String s = menuActionTexts[id];
			int k1 = s.indexOf("@whi@");
			if (k1 != -1) {
				long l3 = StringUtils.encodeBase37(s.substring(k1 + 5).trim());
				if (action == Actions.ADD_FRIEND) {
					addFriend(l3);
				}
				if (action == Actions.ADD_IGNORE) {
					addIgnore(l3);
				}
				if (action == Actions.REMOVE_FRIEND) {
					removeFriend(l3);
				}
				if (action == Actions.REMOVE_IGNORE) {
					removeIgnore(l3);
				}
			}
		}
		if (action == 53) {
			outgoing.writeOpcode(135);
			outgoing.writeLEShort(first);
			outgoing.writeShortA(second);
			outgoing.writeLEShort(clicked);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 539) {
			outgoing.writeOpcode(16);
			outgoing.writeShortA(clicked);
			outgoing.writeLEShortA(first);
			outgoing.writeLEShortA(second);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == Actions.ACCEPT_TRADE || action == Actions.ACCEPT_CHALLENGE) {
			String name = menuActionTexts[id];
			int colour = name.indexOf("@whi@");
			if (colour != -1) {
				name = name.substring(colour + 5).trim();
				String username = StringUtils.format(StringUtils.decodeBase37(StringUtils.encodeBase37(name)));
				boolean found = false;
				for (int index = 0; index < playerCount; index++) {
					Player player = players[playerList[index]];
					if (player == null || player.name == null || !player.name.equalsIgnoreCase(username)) {
						continue;
					}

					walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
					if (action == Actions.ACCEPT_TRADE) {
						outgoing.writeOpcode(139);
						outgoing.writeLEShort(playerList[index]);
					} else if (action == Actions.ACCEPT_CHALLENGE) {
						anInt1188 += clicked;
						if (anInt1188 >= 90) {
							outgoing.writeOpcode(136);
							anInt1188 = 0;
						}

						outgoing.writeOpcode(128);
						outgoing.writeShort(playerList[index]);
					}

					found = true;
					break;
				}

				if (!found) {
					addChatMessage(0, "Unable to find " + username, "");
				}
			}
		}
		if (action == 870) {
			outgoing.writeOpcode(53); // item on item
			outgoing.writeShort(first); // target slot
			outgoing.writeShortA(anInt1283); // current slot
			outgoing.writeLEShortA(clicked); // target id
			outgoing.writeShort(anInt1284); // target interface
			outgoing.writeLEShort(anInt1285); // used id
			outgoing.writeShort(second); // used interface
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 847) {
			outgoing.writeOpcode(87);
			outgoing.writeShortA(clicked);
			outgoing.writeShort(second);
			outgoing.writeShortA(first);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == Actions.USABLE_WIDGET) {
			Widget widget = Widget.widgets[second];
			widgetSelected = 1;
			anInt1137 = second;
			anInt1138 = widget.optionAttributes;
			itemSelected = 0;
			redrawTabArea = true;

			String prefix = widget.optionCircumfix;
			if (prefix.indexOf(" ") != -1) {
				prefix = prefix.substring(0, prefix.indexOf(" "));
			}

			String suffix = widget.optionCircumfix;
			if (suffix.indexOf(" ") != -1) {
				suffix = suffix.substring(suffix.indexOf(" ") + 1);
			}

			selectedWidgetName = prefix + " " + widget.optionText + " " + suffix;
			if (anInt1138 == 16) {
				redrawTabArea = true;
				tabId = 3;
				aBoolean1103 = true;
			}
			return;
		}
		if (action == 78) {
			outgoing.writeOpcode(117);
			outgoing.writeLEShortA(second);
			outgoing.writeLEShortA(clicked);
			outgoing.writeLEShort(first);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 27) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				anInt986 += clicked;
				if (anInt986 >= 54) {
					outgoing.writeOpcode(189);
					outgoing.writeByte(234);
					anInt986 = 0;
				}
				outgoing.writeOpcode(73);
				outgoing.writeLEShort(clicked);
			}
		}
		if (action == 213) {
			boolean flag3 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag3) {
				flag3 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(79);
			outgoing.writeLEShort(second + regionBaseY);
			outgoing.writeShort(clicked);
			outgoing.writeShortA(first + regionBaseX);
		}
		if (action == 632) {
			outgoing.writeOpcode(145);
			outgoing.writeShortA(second);
			outgoing.writeShortA(first);
			outgoing.writeShortA(clicked);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 493) {
			outgoing.writeOpcode(75);
			outgoing.writeLEShortA(second);
			outgoing.writeLEShort(first);
			outgoing.writeShortA(clicked);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 652) {
			boolean flag4 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag4) {
				flag4 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(156);
			outgoing.writeShortA(first + regionBaseX);
			outgoing.writeLEShort(second + regionBaseY);
			outgoing.writeLEShortA(clicked);
		}
		if (action == 94) {
			boolean flag5 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag5) {
				flag5 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(181);
			outgoing.writeLEShort(second + regionBaseY); // y
			outgoing.writeShort(clicked); // item id
			outgoing.writeLEShort(first + regionBaseX); // x
			outgoing.writeShortA(anInt1137); // spell id
		}
		if (action == 225) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				anInt1226 += clicked;
				if (anInt1226 >= 85) {
					outgoing.writeOpcode(230);
					outgoing.writeByte(239);
					anInt1226 = 0;
				}
				outgoing.writeOpcode(17);
				outgoing.writeLEShortA(clicked);
			}
		}
		if (action == 965) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				anInt1134++;
				if (anInt1134 >= 96) {
					outgoing.writeOpcode(152);
					outgoing.writeByte(88);
					anInt1134 = 0;
				}
				outgoing.writeOpcode(21);
				outgoing.writeShort(clicked);
			}
		}
		if (action == 413) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(131);
				outgoing.writeLEShortA(clicked);
				outgoing.writeShortA(anInt1137);
			}
		}
		if (action == Actions.CLOSE_WIDGETS) {
			clearTopInterfaces();
		}
		if (action == 1025) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				NpcDefinition definition = npc.getDefinition();
				if (definition.getMorphisms() != null) {
					definition = definition.morph();
				}
				if (definition != null) {
					String description;
					if (definition.getDescription() != null) {
						description = new String(definition.getDescription());
					} else {
						description = "It's a " + definition.getName() + ".";
					}
					addChatMessage(0, description, "");
				}
			}
		}
		if (action == 900) {
			processClickedObject(clicked, second, first);
			outgoing.writeOpcode(252);
			outgoing.writeLEShortA(clicked >> 14 & 0x7fff);
			outgoing.writeLEShort(second + regionBaseY);
			outgoing.writeShortA(first + regionBaseX);
		}
		if (action == 412) { // opcode 72, note the action
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(72);
				outgoing.writeShortA(clicked);
			}
		}
		if (action == 365) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(249);
				outgoing.writeShortA(clicked);
				outgoing.writeLEShort(anInt1137);
			}
		}
		if (action == 729) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(39);
				outgoing.writeLEShort(clicked);
			}
		}
		if (action == 577) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(139);
				outgoing.writeLEShort(clicked);
			}
		}
		if (action == 956 && processClickedObject(clicked, second, first)) {
			outgoing.writeOpcode(35);
			outgoing.writeLEShort(first + regionBaseX);
			outgoing.writeShortA(anInt1137);
			outgoing.writeShortA(second + regionBaseY);
			outgoing.writeLEShort(clicked >> 14 & 0x7fff);
		}
		if (action == 567) {
			boolean flag6 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag6) {
				flag6 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(23);
			outgoing.writeLEShort(second + regionBaseY);
			outgoing.writeLEShort(clicked);
			outgoing.writeLEShort(first + regionBaseX);
		}
		if (action == 867) {
			if ((clicked & 3) == 0) {
				anInt1175++;
			}
			if (anInt1175 >= 59) {
				outgoing.writeOpcode(200);
				outgoing.writeShort(25501);
				anInt1175 = 0;
			}
			outgoing.writeOpcode(43);
			outgoing.writeLEShort(second);
			outgoing.writeShortA(clicked);
			outgoing.writeShortA(first);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 543) {
			outgoing.writeOpcode(237);
			outgoing.writeShort(first);
			outgoing.writeShortA(clicked);
			outgoing.writeShort(second);
			outgoing.writeShortA(anInt1137);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 606) {
			String s2 = menuActionTexts[id];
			int j2 = s2.indexOf("@whi@");
			if (j2 != -1) {
				if (openInterfaceId == -1) {
					clearTopInterfaces();
					reportInput = s2.substring(j2 + 5).trim();
					reportAbuseMuteToggle = false;
					for (Widget element : Widget.widgets) {
						if (element == null || element.contentType != 600) {
							continue;
						}
						anInt1178 = openInterfaceId = element.parent;
						break;
					}

				} else {
					addChatMessage(0, "Please close the interface you have open before using 'report abuse'", "");
				}
			}
		}
		if (action == 491) {
			Player player = players[clicked];
			if (player != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, player.pathY[0], localPlayer.pathX[0], false, player.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				outgoing.writeOpcode(14);
				outgoing.writeShortA(anInt1284); // widget id
				outgoing.writeShort(clicked); // index
				outgoing.writeShort(anInt1285); // item id
				outgoing.writeLEShort(anInt1283); // item slot
			}
		}
		if (action == Actions.PRIVATE_MESSAGE) {
			String text = menuActionTexts[id];
			int name = text.indexOf("@whi@");

			if (name != -1) {
				long encoded = StringUtils.encodeBase37(text.substring(name + 5).trim());
				int friend = -1;
				for (int index = 0; index < friendCount; index++) {
					if (friends[index] != encoded) {
						continue;
					}

					friend = index;
					break;
				}

				if (friend != -1 && friendWorlds[friend] > 0) {
					redrawDialogueBox = true;
					inputDialogueState = 0;
					messagePromptRaised = true;
					aString1212 = "";
					anInt1064 = 3;
					aLong953 = friends[friend];
					aString1121 = "Enter message to send to " + friendUsernames[friend];
				}
			}
		}
		if (action == 454) {
			outgoing.writeOpcode(41);
			outgoing.writeShort(clicked);
			outgoing.writeShortA(first);
			outgoing.writeShortA(second);
			anInt1243 = 0;
			anInt1244 = second;
			anInt1245 = first;
			anInt1246 = 2;
			if (Widget.widgets[second].parent == openInterfaceId) {
				anInt1246 = 1;
			}
			if (Widget.widgets[second].parent == backDialogueId) {
				anInt1246 = 3;
			}
		}
		if (action == 478) {
			Npc npc = npcs[clicked];
			if (npc != null) {
				walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, npc.pathY[0], localPlayer.pathX[0], false, npc.pathX[0]);
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 2;
				anInt916 = 0;
				if ((clicked & 3) == 0) {
					anInt1155++;
				}
				if (anInt1155 >= 53) {
					outgoing.writeOpcode(85);
					outgoing.writeByte(66);
					anInt1155 = 0;
				}
				outgoing.writeOpcode(18);
				outgoing.writeLEShort(clicked);
			}
		}
		if (action == 113) {
			processClickedObject(clicked, second, first);
			outgoing.writeOpcode(70);
			outgoing.writeLEShort(first + regionBaseX);
			outgoing.writeShort(second + regionBaseY);
			outgoing.writeLEShortA(clicked >> 14 & 0x7fff);
		}
		if (action == 872) {
			processClickedObject(clicked, second, first);
			outgoing.writeOpcode(234);
			outgoing.writeLEShortA(first + regionBaseX);
			outgoing.writeShortA(clicked >> 14 & 0x7fff);
			outgoing.writeLEShortA(second + regionBaseY);
		}
		if (action == 502) {
			processClickedObject(clicked, second, first);
			outgoing.writeOpcode(132);
			outgoing.writeLEShortA(first + regionBaseX);
			outgoing.writeShort(clicked >> 14 & 0x7fff);
			outgoing.writeShortA(second + regionBaseY);
		}
		if (action == Actions.EXAMINE_ITEM) {
			ItemDefinition definition = ItemDefinition.lookup(clicked);
			Widget widget = Widget.widgets[second];
			String description;

			if (widget != null && widget.inventoryAmounts[first] >= 0x186a0) {
				description = widget.inventoryAmounts[first] + " x " + definition.getName();
			} else if (definition.getDescription() != null) {
				description = new String(definition.getDescription());
			} else {
				description = "It's a " + definition.getName() + ".";
			}
			addChatMessage(0, description, "");
		}
		if (action == Actions.TOGGLE_SETTING_WIDGET) {
			outgoing.writeOpcode(185);
			outgoing.writeShort(second);
			Widget widget = Widget.widgets[second];

			if (widget.scripts != null && widget.scripts[0][0] == 5) { // mov_setting instr
				int setting = widget.scripts[0][1];
				settings[setting] = 1 - settings[setting];
				updateVarp(setting);
				redrawTabArea = true;
			}
		}
		if (action == Actions.RESET_SETTING_WIDGET) {
			outgoing.writeOpcode(185);
			outgoing.writeShort(second);
			Widget widget = Widget.widgets[second];

			if (widget.scripts != null && widget.scripts[0][0] == 5) {
				int operand = widget.scripts[0][1];
				if (settings[operand] != widget.scriptDefaults[0]) {
					settings[operand] = widget.scriptDefaults[0];
					updateVarp(operand);
					redrawTabArea = true;
				}
			}
		}
		if (action == 447) {
			itemSelected = 1;
			anInt1283 = first;
			anInt1284 = second;
			anInt1285 = clicked;
			selectedItemName = ItemDefinition.lookup(clicked).getName();
			widgetSelected = 0;
			redrawTabArea = true;
			return;
		}
		if (action == 1226) {
			int object = clicked >> 14 & 0x7fff;
			ObjectDefinition definition = ObjectDefinition.lookup(object);

			String name;
			if (definition.getDescription() != null) {
				name = new String(definition.getDescription());
			} else {
				name = "It's a " + definition.getName() + ".";
			}

			addChatMessage(0, name, "");
		}
		if (action == 244) {
			boolean flag7 = walk(2, 0, 0, 0, localPlayer.pathY[0], 0, 0, second, localPlayer.pathX[0], false, first);
			if (!flag7) {
				flag7 = walk(2, 0, 1, 0, localPlayer.pathY[0], 1, 0, second, localPlayer.pathX[0], false, first);
			}
			anInt914 = super.lastClickX;
			anInt915 = super.lastClickY;
			anInt917 = 2;
			anInt916 = 0;
			outgoing.writeOpcode(253);
			outgoing.writeLEShort(first + regionBaseX);
			outgoing.writeLEShortA(second + regionBaseY);
			outgoing.writeShortA(clicked);
		} else if (action == 1448) {
			ItemDefinition definition = ItemDefinition.lookup(clicked);
			String description;
			if (definition.getDescription() != null) {
				description = new String(definition.getDescription());
			} else {
				description = "It's a " + definition.getName() + ".";
			}
			addChatMessage(0, description, "");
		}

		itemSelected = 0;
		widgetSelected = 0;
		redrawTabArea = true;
	}

	public final void processMinimapClick() {
		if (minimapState != 0) {
			return;
		}

		if (super.lastMetaModifier == 1) {
			int clickX = super.lastClickX - 25 - 550;
			int clickY = super.lastClickY - 5 - 4;
			if (clickX >= 0 && clickY >= 0 && clickX < 146 && clickY < 151) {
				clickX -= 73;
				clickY -= 75;
				int angle = cameraYaw + anInt1209 & 0x7ff;
				int sin = Rasterizer.SINE[angle];
				int cos = Rasterizer.COSINE[angle];
				sin = sin * (anInt1170 + 256) >> 8;
				cos = cos * (anInt1170 + 256) >> 8;
				int dx = clickY * sin + clickX * cos >> 11;
				int dy = clickY * cos - clickX * sin >> 11;
				int x = localPlayer.worldX + dx >> 7;
				int y = localPlayer.worldY - dy >> 7;
				boolean flag1 = walk(1, 0, 0, 0, localPlayer.pathY[0], 0, 0, y, localPlayer.pathX[0], true, x);
				if (flag1) {
					outgoing.writeByte(clickX);
					outgoing.writeByte(clickY);
					outgoing.writeShort(cameraYaw);
					outgoing.writeByte(57);
					outgoing.writeByte(anInt1209);
					outgoing.writeByte(anInt1170);
					outgoing.writeByte(89);
					outgoing.writeShort(localPlayer.worldX);
					outgoing.writeShort(localPlayer.worldY);
					outgoing.writeByte(anInt1264);
					outgoing.writeByte(63);
				}
			}

			anInt1117++;
			if (anInt1117 > 1151) {
				anInt1117 = 0;
				outgoing.writeOpcode(246);
				outgoing.writeByte(0);
				int l = outgoing.getPosition();
				if ((int) (Math.random() * 2D) == 0) {
					outgoing.writeByte(101);
				}
				outgoing.writeByte(197);
				outgoing.writeShort((int) (Math.random() * 65536D));
				outgoing.writeByte((int) (Math.random() * 256D));
				outgoing.writeByte(67);
				outgoing.writeShort(14214);
				if ((int) (Math.random() * 2D) == 0) {
					outgoing.writeShort(29487);
				}
				outgoing.writeShort((int) (Math.random() * 65536D));
				if ((int) (Math.random() * 2D) == 0) {
					outgoing.writeByte(220);
				}
				outgoing.writeByte(180);
				outgoing.writeSizeByte(outgoing.getPosition() - l);
			}
		}
	}

	public final void processMovement(Mob mob) {
		if (mob.worldX < 128 || mob.worldY < 128 || mob.worldX >= 13184 || mob.worldY >= 13184) {
			mob.emoteAnimation = -1;
			mob.graphic = -1;
			mob.startForceMovement = 0;
			mob.endForceMovement = 0;
			mob.worldX = mob.pathX[0] * 128 + mob.size * 64;
			mob.worldY = mob.pathY[0] * 128 + mob.size * 64;
			mob.resetPath();
		}
		if (mob == localPlayer && (mob.worldX < 1536 || mob.worldY < 1536 || mob.worldX >= 11776 || mob.worldY >= 11776)) {
			mob.emoteAnimation = -1;
			mob.graphic = -1;
			mob.startForceMovement = 0;
			mob.endForceMovement = 0;
			mob.worldX = mob.pathX[0] * 128 + mob.size * 64;
			mob.worldY = mob.pathY[0] * 128 + mob.size * 64;
			mob.resetPath();
		}
		if (mob.startForceMovement > tick) {
			nextPreForcedStep(mob); // TODO two periods of motion instead of one??
		} else if (mob.endForceMovement >= tick) {
			nextForcedMovementStep(mob);
		} else {
			nextStep(mob);
		}
		method100(mob);
		updateAnimation(mob);
	}

	public final void processNpcAdditions(boolean priority) {
		for (int index = 0; index < npcCount; index++) {
			Npc npc = npcs[npcList[index]];
			int key = 0x20000000 + (npcList[index] << 14);
			if (npc == null || !npc.isVisible() || npc.getDefinition().isPriorityRender() != priority) {
				continue;
			}

			int viewportX = npc.worldX >> 7;
			int viewportY = npc.worldY >> 7;
			if (viewportX < 0 || viewportX >= 104 || viewportY < 0 || viewportY >= 104) {
				continue;
			}

			if (npc.size == 1 && (npc.worldX & 0x7f) == 64 && (npc.worldY & 0x7f) == 64) {
				if (anIntArrayArray929[viewportX][viewportY] == anInt1265) {
					continue;
				}
				anIntArrayArray929[viewportX][viewportY] = anInt1265;
			}

			if (!npc.getDefinition().isClickable()) {
				key += 0x80000000;
			}

			scene.addEntity(npc.worldX, npc.worldY, plane, npc, npc.orientation, key, method42(npc.worldX, npc.worldY, plane),
					(npc.size - 1) * 64 + 60, npc.animationStretches);
		}
	}

	public final void processNpcMovement() {
		for (int i = 0; i < npcCount; i++) {
			int index = npcList[i];
			Npc npc = npcs[index];
			if (npc != null) {
				processMovement(npc);
			}
		}
	}

	public final void processPlayerAdditions(boolean priority) {
		if (localPlayer.worldX >> 7 == destinationX && localPlayer.worldY >> 7 == destinationY) {
			destinationX = 0;
		}

		int count = playerCount;
		if (priority) {
			count = 1;
		}

		for (int index = 0; index < count; index++) {
			Player player;
			int key;

			if (priority) {
				player = localPlayer;
				key = internalLocalPlayerIndex << 14;
			} else {
				player = players[playerList[index]];
				key = playerList[index] << 14;
			}

			if (player == null || !player.isVisible()) {
				continue;
			}

			player.aBoolean1699 = false;
			if ((lowMemory && playerCount > 50 || playerCount > 200) && !priority
					&& player.movementAnimation == player.idleAnimation) {
				player.aBoolean1699 = true;
			}

			int viewportX = player.worldX >> 7;
			int viewportY = player.worldY >> 7;
			if (viewportX < 0 || viewportX >= 104 || viewportY < 0 || viewportY >= 104) {
				continue;
			}

			if (player.objectModel != null && tick >= player.objectAppearanceStartTick && tick < player.objectAppearanceEndTick) {
				player.aBoolean1699 = false;
				player.anInt1709 = method42(player.worldX, player.worldY, plane);
				scene.addRenderable(plane, player.worldY, player, player.orientation, player.anInt1722, player.worldX,
						player.anInt1709, player.anInt1719, player.anInt1721, key, player.anInt1720);
				continue;
			}

			if ((player.worldX & 0x7f) == 64 && (player.worldY & 0x7f) == 64) {
				if (anIntArrayArray929[viewportX][viewportY] == anInt1265) {
					continue;
				}
				anIntArrayArray929[viewportX][viewportY] = anInt1265;
			}

			player.anInt1709 = method42(player.worldX, player.worldY, plane);
			scene.addEntity(player.worldX, player.worldY, plane, player, player.orientation, key, player.anInt1709, 60,
					player.animationStretches);
		}
	}

	public final void processPlayerMovement() {
		for (int index = -1; index < playerCount; index++) {
			int playerIndex;

			if (index == -1) {
				playerIndex = internalLocalPlayerIndex;
			} else {
				playerIndex = playerList[index];
			}

			Player player = players[playerIndex];
			if (player != null) {
				processMovement(player);
			}
		}
	}

	public final void processProjectiles() {
		for (Projectile projectile = (Projectile) projectiles.getFront(); projectile != null; projectile = (Projectile) projectiles
				.getNext()) {
			if (projectile.plane != plane || tick > projectile.startTick) {
				projectile.unlink();
			} else if (tick >= projectile.endTick) {
				if (projectile.target > 0) {
					Npc npc = npcs[projectile.target - 1];
					if (npc != null && npc.worldX >= 0 && npc.worldX < 13312 && npc.worldY >= 0 && npc.worldY < 13312) {
						projectile.target(npc.worldX, npc.worldY, method42(npc.worldX, npc.worldY, projectile.plane)
								- projectile.destinationElevation, tick);
					}
				}
				if (projectile.target < 0) {
					int index = -projectile.target - 1;
					Player player;
					if (index == localPlayerIndex) {
						player = localPlayer;
					} else {
						player = players[index];
					}

					if (player != null && player.worldX >= 0 && player.worldX < 13312 && player.worldY >= 0
							&& player.worldY < 13312) {
						projectile.target(player.worldX, player.worldY, method42(player.worldX, player.worldY, projectile.plane)
								- projectile.destinationElevation, tick);
					}
				}
				projectile.update(tickDelta);
				scene.addEntity((int) projectile.x, (int) projectile.y, plane, projectile, projectile.yaw, -1,
						(int) projectile.z, 60, false);
			}
		}
	}

	public final void processTabClick() {
		if (super.lastMetaModifier == 1) {
			if (super.lastClickX >= 539 && super.lastClickX <= 573 && super.lastClickY >= 169 && super.lastClickY < 205
					&& inventoryTabIds[0] != -1) {
				redrawTabArea = true;
				tabId = 0;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 569 && super.lastClickX <= 599 && super.lastClickY >= 168 && super.lastClickY < 205
					&& inventoryTabIds[1] != -1) {
				redrawTabArea = true;
				tabId = 1;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 597 && super.lastClickX <= 627 && super.lastClickY >= 168 && super.lastClickY < 205
					&& inventoryTabIds[2] != -1) {
				redrawTabArea = true;
				tabId = 2;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 625 && super.lastClickX <= 669 && super.lastClickY >= 168 && super.lastClickY < 203
					&& inventoryTabIds[3] != -1) {
				redrawTabArea = true;
				tabId = 3;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 666 && super.lastClickX <= 696 && super.lastClickY >= 168 && super.lastClickY < 205
					&& inventoryTabIds[4] != -1) {
				redrawTabArea = true;
				tabId = 4;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 694 && super.lastClickX <= 724 && super.lastClickY >= 168 && super.lastClickY < 205
					&& inventoryTabIds[5] != -1) {
				redrawTabArea = true;
				tabId = 5;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 722 && super.lastClickX <= 756 && super.lastClickY >= 169 && super.lastClickY < 205
					&& inventoryTabIds[6] != -1) {
				redrawTabArea = true;
				tabId = 6;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 540 && super.lastClickX <= 574 && super.lastClickY >= 466 && super.lastClickY < 502
					&& inventoryTabIds[7] != -1) {
				redrawTabArea = true;
				tabId = 7;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 572 && super.lastClickX <= 602 && super.lastClickY >= 466 && super.lastClickY < 503
					&& inventoryTabIds[8] != -1) {
				redrawTabArea = true;
				tabId = 8;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 599 && super.lastClickX <= 629 && super.lastClickY >= 466 && super.lastClickY < 503
					&& inventoryTabIds[9] != -1) {
				redrawTabArea = true;
				tabId = 9;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 627 && super.lastClickX <= 671 && super.lastClickY >= 467 && super.lastClickY < 502
					&& inventoryTabIds[10] != -1) {
				redrawTabArea = true;
				tabId = 10;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 669 && super.lastClickX <= 699 && super.lastClickY >= 466 && super.lastClickY < 503
					&& inventoryTabIds[11] != -1) {
				redrawTabArea = true;
				tabId = 11;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 696 && super.lastClickX <= 726 && super.lastClickY >= 466 && super.lastClickY < 503
					&& inventoryTabIds[12] != -1) {
				redrawTabArea = true;
				tabId = 12;
				aBoolean1103 = true;
			}
			if (super.lastClickX >= 724 && super.lastClickX <= 758 && super.lastClickY >= 466 && super.lastClickY < 502
					&& inventoryTabIds[13] != -1) {
				redrawTabArea = true;
				tabId = 13;
				aBoolean1103 = true;
			}
		}
	}

	public final void processTrackUpdates() {
		for (int index = 0; index < trackCount; index++) {
			if (trackDelays[index] <= 0) {
				boolean replay = false;

				try {
					if (tracks[index] == anInt874 && trackLoops[index] == anInt1289) {
						if (!waveReplay()) {
							replay = true;
						}
					} else {
						Buffer buffer = Track.data(trackLoops[index], tracks[index]);
						if (System.currentTimeMillis() + (buffer.getPosition() / 22) > aLong1172 + (anInt1257 / 22)) {
							anInt1257 = buffer.getPosition();
							aLong1172 = System.currentTimeMillis();

							if (waveSave(buffer.getPayload(), buffer.getPosition())) {
								anInt874 = tracks[index];
								anInt1289 = trackLoops[index];
							} else {
								replay = true;
							}
						}
					}
				} catch (Exception exception) {
				}

				if (!replay || trackDelays[index] == -5) {
					trackCount--;
					for (int next = index; next < trackCount; next++) {
						tracks[next] = tracks[next + 1];
						trackLoops[next] = trackLoops[next + 1];
						trackDelays[next] = trackDelays[next + 1];
					}

					index--;
				} else {
					trackDelays[index] = -5;
				}
			} else {
				trackDelays[index]--;
			}
		}

		if (songDelay > 0) {
			songDelay -= 20;
			if (songDelay < 0) {
				songDelay = 0;
			}

			if (songDelay == 0 && playingMusic && !lowMemory) {
				musicId = nextMusicId;
				fadeMusic = true;
				provider.provide(2, musicId);
			}
		}
	}

	public final boolean processWidgetAnimations(int id, int tickDelta) {
		boolean redrawRequired = false;
		Widget widget = Widget.widgets[id];
		for (int childId : widget.children) {
			if (childId == -1) {
				break;
			}

			Widget child = Widget.widgets[childId];
			if (child.group == Widget.TYPE_MODEL_LIST) {
				redrawRequired |= processWidgetAnimations(child.id, tickDelta);
			}

			if (child.group == Widget.TYPE_MODEL && (child.defaultAnimationId != -1 || child.secondaryAnimationId != -1)) {
				boolean updated = scriptStateChanged(child);
				int animationId = updated ? child.secondaryAnimationId : child.defaultAnimationId;

				if (animationId != -1) {
					Animation animation = Animation.animations[animationId];

					for (child.lastFrameTime += tickDelta; child.lastFrameTime > animation.duration(child.currentFrame);) {
						child.lastFrameTime -= animation.duration(child.currentFrame) + 1;
						child.currentFrame++;

						if (child.currentFrame >= animation.getFrameCount()) {
							child.currentFrame -= animation.getLoopOffset();
							if (child.currentFrame < 0 || child.currentFrame >= animation.getFrameCount()) {
								child.currentFrame = 0;
							}
						}

						redrawRequired = true;
					}
				}
			}
		}

		return redrawRequired;
	}

	@Override
	public final void pulse() {
		if (gameAlreadyLoaded || error || unableToLoad) {
			return;
		}
		tick++;

		if (!loggedIn) {
			pulseLoginScreen();
		} else {
			pulseGame();
		}

		processLoadedResources();
	}

	public final void pulseGame() {
		if (systemUpdateTime > 1) {
			systemUpdateTime--;
		}
		if (anInt1011 > 0) {
			anInt1011--;
		}

		for (int times = 0; times < 5; times++) {
			if (!parseFrame()) {
				break;
			}
		}

		if (!loggedIn) {
			return;
		}

		synchronized (mouseCapturer.getSynchronizedObject()) {
			if (flaggedAccount) {
				if (super.lastMetaModifier != 0 || mouseCapturer.getCapturedCoordinateCount() >= 40) {
					outgoing.writeOpcode(45);
					outgoing.writeByte(0);
					int off = outgoing.getPosition();
					int sent = 0;

					for (int coordinate = 0; coordinate < mouseCapturer.getCapturedCoordinateCount(); coordinate++) {
						if (off - outgoing.getPosition() >= 240) {
							break;
						}

						sent++;
						int y = mouseCapturer.getCoordinatesY()[coordinate];
						if (y < 0) {
							y = 0;
						} else if (y > 502) {
							y = 502;
						}

						int x = mouseCapturer.getCoordinatesX()[coordinate];
						if (x < 0) {
							x = 0;
						} else if (x > 764) {
							x = 764;
						}

						int point = y * 765 + x;
						if (mouseCapturer.getCoordinatesY()[coordinate] == -1
								&& mouseCapturer.getCoordinatesX()[coordinate] == -1) {
							x = -1;
							y = -1;
							point = 0x7ffff;
						}

						if (x == lastMouseX && y == lastMouseY) {
							if (duplicateClickCount < 2047) {
								duplicateClickCount++;
							}
						} else {
							int dx = x - lastMouseX;
							lastMouseX = x;
							int dy = y - lastMouseY;
							lastMouseY = y;
							if (duplicateClickCount < 8 && dx >= -32 && dx <= 31 && dy >= -32 && dy <= 31) {
								dx += 32;
								dy += 32;
								outgoing.writeShort((duplicateClickCount << 12) + (dx << 6) + dy);
								duplicateClickCount = 0;
							} else if (duplicateClickCount < 8) {
								outgoing.writeTriByte(0x800000 + (duplicateClickCount << 19) + point);
								duplicateClickCount = 0;
							} else {
								outgoing.writeInt(0xc0000000 + (duplicateClickCount << 19) + point);
								duplicateClickCount = 0;
							}
						}
					}

					outgoing.writeSizeByte(outgoing.getPosition() - off);
					if (sent >= mouseCapturer.getCapturedCoordinateCount()) {
						mouseCapturer.setCapturedCoordinateCount(0);
					} else {
						mouseCapturer.setCapturedCoordinateCount(mouseCapturer.getCapturedCoordinateCount() - sent);
						for (int i5 = 0; i5 < mouseCapturer.getCapturedCoordinateCount(); i5++) {
							mouseCapturer.getCoordinatesX()[i5] = mouseCapturer.getCoordinatesX()[i5 + sent];
							mouseCapturer.getCoordinatesY()[i5] = mouseCapturer.getCoordinatesY()[i5 + sent];
						}

					}
				}
			} else {
				mouseCapturer.setCapturedCoordinateCount(0);
			}
		}

		if (super.lastMetaModifier != 0) {
			int time = (int) ((super.lastMouseClick - aLong1220) / 50);
			if (time > 4095) {
				time = 4095;
			}

			aLong1220 = super.lastMouseClick;
			int y = super.lastClickY;
			if (y < 0) {
				y = 0;
			} else if (y > 502) {
				y = 502;
			}

			int x = super.lastClickX;
			if (x < 0) {
				x = 0;
			} else if (x > 764) {
				x = 764;
			}

			int key = y * 765 + x;
			int meta = 0;
			if (super.lastMetaModifier == 2) {
				meta = 1;
			}

			outgoing.writeOpcode(241);
			outgoing.writeInt((time << 20) + (meta << 19) + key);
		}

		if (anInt1016 > 0) {
			anInt1016--;
		}

		if (super.keyStatuses[1] == 1 || super.keyStatuses[2] == 1 || super.keyStatuses[3] == 1 || super.keyStatuses[4] == 1) {
			aBoolean1017 = true;
		}

		if (aBoolean1017 && anInt1016 <= 0) {
			anInt1016 = 20;
			aBoolean1017 = false;
			outgoing.writeOpcode(86);
			outgoing.writeShort(cameraRoll);
			outgoing.writeShortA(cameraYaw);
		}

		if (super.hasFocus && !wasFocused) {
			wasFocused = true;
			outgoing.writeOpcode(3);
			outgoing.writeByte(1);
		}

		if (!super.hasFocus && wasFocused) {
			wasFocused = false;
			outgoing.writeOpcode(3);
			outgoing.writeByte(0);
		}

		loadNextRegion();
		method115();
		processTrackUpdates();
		timeoutCounter++;
		if (timeoutCounter > 750) {
			attemptReconnection();
		}

		processPlayerMovement();
		processNpcMovement();
		pulseMobChatText();
		tickDelta++;
		if (anInt917 != 0) {
			anInt916 += 20;
			if (anInt916 >= 400) {
				anInt917 = 0;
			}
		}

		if (anInt1246 != 0) {
			anInt1243++;
			if (anInt1243 >= 15) {
				if (anInt1246 == 2) {
					redrawTabArea = true;
				}
				if (anInt1246 == 3) {
					redrawDialogueBox = true;
				}
				anInt1246 = 0;
			}
		}
		if (anInt1086 != 0) {
			anInt989++;
			if (super.mouseEventX > anInt1087 + 5 || super.mouseEventX < anInt1087 - 5 || super.mouseEventY > anInt1088 + 5
					|| super.mouseEventY < anInt1088 - 5) {
				aBoolean1242 = true;
			}
			if (super.metaModifierHeld == 0) {
				if (anInt1086 == 2) {
					redrawTabArea = true;
				}
				if (anInt1086 == 3) {
					redrawDialogueBox = true;
				}
				anInt1086 = 0;
				if (aBoolean1242 && anInt989 >= 5) {
					anInt1067 = -1;
					method82();
					if (anInt1067 == modifiedWidgetId && nextInventorySlot != selectedInventorySlot) {
						Widget widget = Widget.widgets[modifiedWidgetId];
						int insertionMode = WidgetConstants.SWAP_MODE;

						if (anInt913 == 1 && widget.contentType == WidgetConstants.BANK_CONTENT_TYPE) {
							insertionMode = WidgetConstants.INSERT_MODE;
						}
						if (widget.inventoryIds[nextInventorySlot] <= 0) {
							insertionMode = WidgetConstants.SWAP_MODE;
						}

						if (widget.replaceItems) {
							int original = selectedInventorySlot;
							int target = nextInventorySlot;
							widget.inventoryIds[target] = widget.inventoryIds[original];
							widget.inventoryAmounts[target] = widget.inventoryAmounts[original];
							widget.inventoryIds[original] = -1;
							widget.inventoryAmounts[original] = 0;
						} else if (insertionMode == WidgetConstants.INSERT_MODE) {
							int slot = selectedInventorySlot;

							for (int last = nextInventorySlot; slot != last;) {
								if (slot > last) {
									widget.swapInventoryItems(slot, slot - 1);
									slot--;
								} else if (slot < last) {
									widget.swapInventoryItems(slot, slot + 1);
									slot++;
								}
							}
						} else {
							widget.swapInventoryItems(selectedInventorySlot, nextInventorySlot);
						}

						outgoing.writeOpcode(214);
						outgoing.writeLEShortA(modifiedWidgetId);
						outgoing.writeNegatedByte(insertionMode);
						outgoing.writeLEShortA(selectedInventorySlot);
						outgoing.writeLEShort(nextInventorySlot);
					}
				} else if ((anInt1253 == 1 || isAddFriend(menuActionRow - 1)) && menuActionRow > 2) {
					method116();
				} else if (menuActionRow > 0) {
					processMenuActions(menuActionRow - 1);
				}

				anInt1243 = 10;
				super.lastMetaModifier = 0;
			}
		}

		if (SceneGraph.anInt470 != -1) {
			int k = SceneGraph.anInt470;
			int k1 = SceneGraph.anInt471;
			boolean flag = walk(0, 0, 0, 0, localPlayer.pathY[0], 0, 0, k1, localPlayer.pathX[0], true, k);
			SceneGraph.anInt470 = -1;
			if (flag) {
				anInt914 = super.lastClickX;
				anInt915 = super.lastClickY;
				anInt917 = 1;
				anInt916 = 0;
			}
		}

		if (super.lastMetaModifier == 1 && clickToContinueString != null) {
			clickToContinueString = null;
			redrawDialogueBox = true;
			super.lastMetaModifier = 0;
		}

		method20();
		processMinimapClick();
		processTabClick();
		updateChatMode();
		if (super.metaModifierHeld == 1 || super.lastMetaModifier == 1) {
			anInt1213++;
		}
		if (loadingStage == 2) {
			method108();
		}
		if (loadingStage == 2 && oriented) {
			method39();
		}
		for (int i1 = 0; i1 < 5; i1++) {
			anIntArray1030[i1]++;
		}

		processKeyInput();
		super.timeIdle++;
		if (super.timeIdle > 4500) {
			anInt1011 = 250;
			super.timeIdle -= 500;
			outgoing.writeOpcode(202);
		}
		anInt988++;
		if (anInt988 > 500) {
			anInt988 = 0;
			int l1 = (int) (Math.random() * 8D);
			if ((l1 & 1) == 1) {
				anInt1278 += anInt1279;
			}
			if ((l1 & 2) == 2) {
				anInt1131 += anInt1132;
			}
			if ((l1 & 4) == 4) {
				anInt896 += anInt897;
			}
		}
		if (anInt1278 < -50) {
			anInt1279 = 2;
		}
		if (anInt1278 > 50) {
			anInt1279 = -2;
		}
		if (anInt1131 < -55) {
			anInt1132 = 2;
		}
		if (anInt1131 > 55) {
			anInt1132 = -2;
		}
		if (anInt896 < -40) {
			anInt897 = 1;
		}
		if (anInt896 > 40) {
			anInt897 = -1;
		}
		anInt1254++;
		if (anInt1254 > 500) {
			anInt1254 = 0;
			int i2 = (int) (Math.random() * 8D);
			if ((i2 & 1) == 1) {
				anInt1209 += anInt1210;
			}
			if ((i2 & 2) == 2) {
				anInt1170 += anInt1171;
			}
		}
		if (anInt1209 < -60) {
			anInt1210 = 2;
		}
		if (anInt1209 > 60) {
			anInt1210 = -2;
		}
		if (anInt1170 < -20) {
			anInt1171 = 1;
		}
		if (anInt1170 > 10) {
			anInt1171 = -1;
		}
		anInt1010++;
		if (anInt1010 > 50) {
			outgoing.writeOpcode(0);
		}
		try {
			if (primary != null && outgoing.getPosition() > 0) {
				primary.write(outgoing.getPayload(), outgoing.getPosition(), 0);
				outgoing.setPosition(0);
				anInt1010 = 0;
				return;
			}
		} catch (IOException _ex) {
			attemptReconnection();
		} catch (Exception exception) {
			reset();
		}
	}

	public final void pulseLoginScreen() {
		if (loginScreenStage == 0) {
			int loginButtonX = super.frameWidth / 2 - 80;
			int loginButtonY = super.frameHeight / 2 + 20;
			loginButtonY += 20;
			if (super.lastMetaModifier == 1 && super.lastClickX >= loginButtonX - 75 && super.lastClickX <= loginButtonX + 75
					&& super.lastClickY >= loginButtonY - 20 && super.lastClickY <= loginButtonY + 20) {
				loginScreenStage = 3;
				loginInputLine = 0;
			}
			loginButtonX = super.frameWidth / 2 + 80;
			if (super.lastMetaModifier == 1 && super.lastClickX >= loginButtonX - 75 && super.lastClickX <= loginButtonX + 75
					&& super.lastClickY >= loginButtonY - 20 && super.lastClickY <= loginButtonY + 20) {
				firstLoginMessage = "";
				secondLoginMessage = "Enter your username & password.";
				loginScreenStage = 2;
				loginInputLine = 0;
				return;
			}
		} else {
			if (loginScreenStage == 2) {
				int j = super.frameHeight / 2 - 40;
				j += 30;
				j += 25;
				if (super.lastMetaModifier == 1 && super.lastClickY >= j - 15 && super.lastClickY < j) {
					loginInputLine = 0;
				}
				j += 15;
				if (super.lastMetaModifier == 1 && super.lastClickY >= j - 15 && super.lastClickY < j) {
					loginInputLine = 1;
				}
				j += 15;
				int i1 = super.frameWidth / 2 - 80;
				int k1 = super.frameHeight / 2 + 50;
				k1 += 20;
				if (super.lastMetaModifier == 1 && super.lastClickX >= i1 - 75 && super.lastClickX <= i1 + 75
						&& super.lastClickY >= k1 - 20 && super.lastClickY <= k1 + 20) {
					loginFailures = 0;
					login(username, password, false);
					if (loggedIn) {
						return;
					}
				}
				i1 = super.frameWidth / 2 + 80;
				if (super.lastMetaModifier == 1 && super.lastClickX >= i1 - 75 && super.lastClickX <= i1 + 75
						&& super.lastClickY >= k1 - 20 && super.lastClickY <= k1 + 20) {
					loginScreenStage = 0;
					username = "Major";
					password = "testing";
				}
				do {
					int key = nextPressedKey();
					if (key == -1) {
						break;
					}
					boolean validCharacter = false;
					for (int i2 = 0; i2 < VALID_INPUT_CHARACTERS.length(); i2++) {
						if (key != VALID_INPUT_CHARACTERS.charAt(i2)) {
							continue;
						}
						validCharacter = true;
						break;
					}

					if (loginInputLine == 0) {
						if (key == 8 && username.length() > 0) {
							username = username.substring(0, username.length() - 1);
						}
						if (key == 9 || key == 10 || key == 13) {
							loginInputLine = 1;
						}
						if (validCharacter) {
							username += (char) key;
						}
						if (username.length() > 12) {
							username = username.substring(0, 12);
						}
					} else if (loginInputLine == 1) {
						if (key == 8 && password.length() > 0) {
							password = password.substring(0, password.length() - 1);
						}
						if (key == 9 || key == 10 || key == 13) {
							loginInputLine = 0;
						}
						if (validCharacter) {
							password += (char) key;
						}
						if (password.length() > 20) {
							password = password.substring(0, 20);
						}
					}
				} while (true);
				return;
			}
			if (loginScreenStage == 3) {
				int cancelX = super.frameWidth / 2;
				int cancelY = super.frameHeight / 2 + 50;
				cancelY += 20;
				if (super.lastMetaModifier == 1 && super.lastClickX >= cancelX - 75 && super.lastClickX <= cancelX + 75
						&& super.lastClickY >= cancelY - 20 && super.lastClickY <= cancelY + 20) {
					loginScreenStage = 0;
				}
			}
		}
	}

	public final void pulseMobChatText() {
		for (int i = -1; i < playerCount; i++) {
			int index;
			if (i == -1) {
				index = internalLocalPlayerIndex;
			} else {
				index = playerList[i];
			}
			Player player = players[index];
			if (player != null && player.textCycle > 0) {
				player.textCycle--;
				if (player.textCycle == 0) {
					player.spokenText = null;
				}
			}
		}

		for (int k = 0; k < npcCount; k++) {
			int index = npcList[k];
			Npc npc = npcs[index];
			if (npc != null && npc.textCycle > 0) {
				npc.textCycle--;
				if (npc.textCycle == 0) {
					npc.spokenText = null;
				}
			}
		}
	}

	public final void removeFriend(long l) {
		if (l == 0L) {
			return;
		}
		for (int i = 0; i < friendCount; i++) {
			if (friends[i] != l) {
				continue;
			}
			friendCount--;
			redrawTabArea = true;
			for (int j = i; j < friendCount; j++) {
				friendUsernames[j] = friendUsernames[j + 1];
				friendWorlds[j] = friendWorlds[j + 1];
				friends[j] = friends[j + 1];
			}

			outgoing.writeOpcode(215);
			outgoing.writeLong(l);
			break;
		}
	}

	public final void removeIgnore(long name) {
		try {
			if (name == 0L) {
				return;
			}

			for (int index = 0; index < ignoredCount; index++) {
				if (ignores[index] == name) {
					ignoredCount--;
					redrawTabArea = true;
					for (int i = index; i < ignoredCount; i++) {
						ignores[i] = ignores[i + 1];
					}

					outgoing.writeOpcode(74);
					outgoing.writeLong(name);
					return;
				}
			}

		} catch (RuntimeException runtimeexception) {
			SignLink.reportError("47229, " + name + ", " + runtimeexception.toString());
		}
	}

	public final DataInputStream requestCacheIndex(String request) throws IOException {
		if (!useJaggrab) {
			if (SignLink.getApplet() != null) {
				return SignLink.openUrl(request);
			}
			return new DataInputStream(new URL(getCodeBase(), request).openStream());
		}

		if (jaggrab != null) {
			try {
				jaggrab.close();
			} catch (Exception _ex) {
			}
			jaggrab = null;
		}

		jaggrab = openSocket(43595);
		jaggrab.setSoTimeout(10000);
		InputStream in = jaggrab.getInputStream();
		OutputStream out = jaggrab.getOutputStream();

		out.write(("JAGGRAB /" + request + "\n\n").getBytes());
		return new DataInputStream(in);
	}

	public final void requestCrcs() {
		int delay = 5;
		archiveCRCs[8] = 0;
		int k = 0;
		while (archiveCRCs[8] == 0) {
			String error = "Unknown problem";
			drawLoadingText(20, "Connecting to web server");
			try (DataInputStream in = requestCacheIndex("crc" + (int) (Math.random() * 99999999D) + "-" + 317)) {
				Buffer buffer = new Buffer(new byte[40]);
				in.readFully(buffer.getPayload(), 0, 40);
				in.close();
				for (int index = 0; index < 9; index++) {
					archiveCRCs[index] = buffer.readInt();
				}

				int expected = buffer.readInt();
				int calculated = 1234;
				for (int index = 0; index < 9; index++) {
					calculated = (calculated << 1) + archiveCRCs[index];
				}

				if (expected != calculated) {
					error = "checksum problem";
					archiveCRCs[8] = 0;
				}
			} catch (EOFException ex) {
				error = "EOF problem";
				archiveCRCs[8] = 0;
			} catch (IOException ex) {
				ex.printStackTrace();
				error = "connection problem";
				archiveCRCs[8] = 0;
			} catch (Exception ex) {
				error = "logic problem";
				archiveCRCs[8] = 0;
				if (!SignLink.isReportError()) {
					return;
				}
			}

			if (archiveCRCs[8] == 0) {
				k++;
				for (int remaining = delay; remaining > 0; remaining--) {
					if (k >= 10) {
						drawLoadingText(10, "Game updated - please reload page");
						remaining = 10;
					} else {
						drawLoadingText(10, error + " - Will retry in " + remaining + " secs.");
					}
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException ex) {
					}
				}

				delay *= 2;
				if (delay > 60) {
					delay = 60;
				}
				useJaggrab = !useJaggrab;
			}
		}
	}

	public final void reset() {
		if (primary != null) {
			primary.stop();
		}

		primary = null;
		loggedIn = false;
		loginScreenStage = 0;
		username = "Major";
		password = "testing";
		unlinkCaches();
		scene.reset();

		for (int plane = 0; plane < 4; plane++) {
			collisionMaps[plane].init();
		}

		System.gc();
		stopMidi();
		nextMusicId = -1;
		musicId = -1;
		songDelay = 0;
	}

	public final void resetAnimation(int id) {
		Widget widget = Widget.widgets[id];
		for (int childId : widget.children) {
			if (childId == -1) {
				break;
			}

			Widget child = Widget.widgets[childId];
			if (child.group == Widget.TYPE_MODEL_LIST) {
				resetAnimation(child.id);
			}

			child.currentFrame = 0;
			child.lastFrameTime = 0;
		}
	}

	public final void resetTitleScreen() {
		if (aClass15_1107 != null) {
			return;
		}

		super.frameGraphicsBuffer = null;
		aClass15_1166 = null;
		aClass15_1164 = null;
		aClass15_1163 = null;
		aClass15_1165 = null;
		aClass15_1123 = null;
		aClass15_1124 = null;
		aClass15_1125 = null;
		aClass15_1110 = new ProducingGraphicsBuffer(getFrame(), 128, 265);
		Raster.reset();
		aClass15_1111 = new ProducingGraphicsBuffer(getFrame(), 128, 265);
		Raster.reset();
		aClass15_1107 = new ProducingGraphicsBuffer(getFrame(), 509, 171);
		Raster.reset();
		aClass15_1108 = new ProducingGraphicsBuffer(getFrame(), 360, 132);
		Raster.reset();
		aClass15_1109 = new ProducingGraphicsBuffer(getFrame(), 360, 200);
		Raster.reset();
		aClass15_1112 = new ProducingGraphicsBuffer(getFrame(), 202, 238);
		Raster.reset();
		aClass15_1113 = new ProducingGraphicsBuffer(getFrame(), 203, 238);
		Raster.reset();
		aClass15_1114 = new ProducingGraphicsBuffer(getFrame(), 74, 94);
		Raster.reset();
		aClass15_1115 = new ProducingGraphicsBuffer(getFrame(), 75, 94);
		Raster.reset();

		if (titleScreen != null) {
			drawTitleBackground();
			method51();
		}
		aBoolean1255 = true;
	}

	@Override
	public final void run() {
		if (aBoolean880) {
			method136();
		} else {
			super.run();
		}
	}

	public final boolean scriptStateChanged(Widget widget) {
		if (widget.scriptOperators == null) {
			return false;
		}

		for (int id = 0; id < widget.scriptOperators.length; id++) {
			int result = executeScript(widget, id);
			int defaultValue = widget.scriptDefaults[id];
			int operator = widget.scriptOperators[id];

			if (operator == 1) {
				if (result != defaultValue) {
					return false;
				}
			} else if (operator == 2) {
				if (result >= defaultValue) {
					return false;
				}
			} else if (operator == 3) {
				if (result <= defaultValue) {
					return false;
				}
			} else if (operator == 4) {
				if (result == defaultValue) {
					return false;
				}
			}
		}

		return true;
	}

	public final void setWaveVolume(int volume) {
		SignLink.setWaveVolume(volume);
	}

	@Override
	public final void shutdown() {
		SignLink.setReportError(false);
		try {
			if (primary != null) {
				primary.stop();
			}
		} catch (Exception _ex) {
		}
		primary = null;
		stopMidi();
		if (mouseCapturer != null) {
			mouseCapturer.setRunning(false);
		}
		mouseCapturer = null;
		provider.stop();
		provider = null;
		chatBuffer = null;
		outgoing = null;
		login = null;
		incoming = null;
		localRegionIds = null;
		localRegionMapData = null;
		localRegionLandscapeData = null;
		localRegionMapIds = null;
		localRegionLandscapeIds = null;
		tileHeights = null;
		tileFlags = null;
		scene = null;
		collisionMaps = null;
		anIntArrayArray901 = null;
		distances = null;
		waypointX = null;
		waypointY = null;
		aByteArray912 = null;
		aClass15_1163 = null;
		aClass15_1164 = null;
		aClass15_1165 = null;
		aClass15_1166 = null;
		aClass15_1123 = null;
		aClass15_1124 = null;
		aClass15_1125 = null;
		backLeft1Buffer = null;
		backLeft2Buffer = null;
		backRight1Buffer = null;
		backRight2Buffer = null;
		backTopBuffer = null;
		aClass15_908 = null;
		aClass15_909 = null;
		aClass15_910 = null;
		aClass15_911 = null;
		inventoryBackground = null;
		mapBackground = null;
		chatBackground = null;
		backBase1 = null;
		backBase2 = null;
		backHmid1 = null;
		sideIcons = null;
		aClass30_Sub2_Sub1_Sub2_1143 = null;
		aClass30_Sub2_Sub1_Sub2_1144 = null;
		aClass30_Sub2_Sub1_Sub2_1145 = null;
		aClass30_Sub2_Sub1_Sub2_1146 = null;
		aClass30_Sub2_Sub1_Sub2_1147 = null;
		aClass30_Sub2_Sub1_Sub2_865 = null;
		aClass30_Sub2_Sub1_Sub2_866 = null;
		aClass30_Sub2_Sub1_Sub2_867 = null;
		aClass30_Sub2_Sub1_Sub2_868 = null;
		aClass30_Sub2_Sub1_Sub2_869 = null;
		compass = null;
		hitMarks = null;
		headIcons = null;
		crosses = null;
		itemMapdot = null;
		npcMapdot = null;
		playerMapdot = null;
		friendMapdot = null;
		teamMapdot = null;
		mapScenes = null;
		mapFunctions = null;
		anIntArrayArray929 = null;
		players = null;
		playerList = null;
		mobsAwaitingUpdate = null;
		playerSynchronizationBuffers = null;
		removedMobs = null;
		npcs = null;
		npcList = null;
		groundItems = null;
		spawns = null;
		projectiles = null;
		incompleteAnimables = null;
		firstMenuOperand = null;
		secondMenuOperand = null;
		menuActionTypes = null;
		selectedMenuActions = null;
		menuActionTexts = null;
		settings = null;
		anIntArray1072 = null;
		anIntArray1073 = null;
		aClass30_Sub2_Sub1_Sub1Array1140 = null;
		aClass30_Sub2_Sub1_Sub1_1263 = null;
		friendUsernames = null;
		friends = null;
		friendWorlds = null;
		aClass15_1110 = null;
		aClass15_1111 = null;
		aClass15_1107 = null;
		aClass15_1108 = null;
		aClass15_1109 = null;
		aClass15_1112 = null;
		aClass15_1113 = null;
		aClass15_1114 = null;
		aClass15_1115 = null;
		method118();
		ObjectDefinition.dispose();
		NpcDefinition.reset();
		ItemDefinition.dispose();
		Floor.floors = null;
		IdentityKit.kits = null;
		Widget.widgets = null;
		Animation.animations = null;
		Graphic.graphics = null;
		Graphic.models = null;
		VariableParameter.parameters = null;
		super.frameGraphicsBuffer = null;
		Player.models = null;
		Rasterizer.dispose();
		SceneGraph.dispose();
		Model.dispose();
		Frame.clearFrames();
		System.gc();
	}

	@Override
	public final void startRunnable(Runnable runnable, int priority) {
		if (priority > 10) {
			priority = 10;
		}
		if (SignLink.getApplet() != null) {
			SignLink.startThread(runnable, priority);
			return;
		}
		super.startRunnable(runnable, priority);
	}

	public final void stopMidi() {
		SignLink.setMidiFade(0);
		SignLink.setMidi("stop");
	}

	public final void unlinkCaches() {
		ObjectDefinition.baseModels.clear();
		ObjectDefinition.models.clear();
		NpcDefinition.modelCache.clear();
		ItemDefinition.models.clear();
		ItemDefinition.sprites.clear();
		Player.models.clear();
		Graphic.models.clear();
	}

	public final void updateAnimation(Mob character) {
		character.animationStretches = false;
		if (character.movementAnimation != -1) {
			Animation animation = Animation.animations[character.movementAnimation];
			character.anInt1519++;
			if (character.displayedMovementFrames < animation.getFrameCount()
					&& character.anInt1519 > animation.duration(character.displayedMovementFrames)) {
				character.anInt1519 = 0;
				character.displayedMovementFrames++;
			}
			if (character.displayedMovementFrames >= animation.getFrameCount()) {
				character.anInt1519 = 0;
				character.displayedMovementFrames = 0;
			}
		}
		if (character.graphic != -1 && tick >= character.graphicDelay) {
			if (character.currentAnimation < 0) {
				character.currentAnimation = 0;
			}
			Animation graphic = Graphic.graphics[character.graphic].getAnimation();
			for (character.anInt1522++; character.currentAnimation < graphic.getFrameCount()
					&& character.anInt1522 > graphic.duration(character.currentAnimation); character.currentAnimation++) {
				character.anInt1522 -= graphic.duration(character.currentAnimation);
			}

			if (character.currentAnimation >= graphic.getFrameCount()
					&& (character.currentAnimation < 0 || character.currentAnimation >= graphic.getFrameCount())) {
				character.graphic = -1;
			}
		}
		if (character.emoteAnimation != -1 && character.animationDelay <= 1) {
			Animation emoteAnimation = Animation.animations[character.emoteAnimation];
			if (emoteAnimation.getAnimatingPrecedence() == 1 && character.anInt1542 > 0 && character.startForceMovement <= tick
					&& character.endForceMovement < tick) {
				character.animationDelay = 1;
				return;
			}
		}
		if (character.emoteAnimation != -1 && character.animationDelay == 0) {
			Animation emote = Animation.animations[character.emoteAnimation];
			for (character.emoteTimeRemaining++; character.displayedEmoteFrames < emote.getFrameCount()
					&& character.emoteTimeRemaining > emote.duration(character.displayedEmoteFrames); character.displayedEmoteFrames++) {
				character.emoteTimeRemaining -= emote.duration(character.displayedEmoteFrames);
			}

			if (character.displayedEmoteFrames >= emote.getFrameCount()) {
				character.displayedEmoteFrames -= emote.getLoopOffset();
				character.currentAnimationLoops++;
				if (character.currentAnimationLoops >= emote.getMaximumLoops()) {
					character.emoteAnimation = -1;
				}
				if (character.displayedEmoteFrames < 0 || character.displayedEmoteFrames >= emote.getFrameCount()) {
					character.emoteAnimation = -1;
				}
			}
			character.animationStretches = emote.stretches();
		}

		if (character.animationDelay > 0) {
			character.animationDelay--;
		}
	}

	public final void updateMobs() {
		anInt974 = 0;

		for (int index = -1; index < playerCount + npcCount; index++) {
			Mob mob;
			if (index == -1) {
				mob = localPlayer;
			} else if (index < playerCount) {
				mob = players[playerList[index]];
			} else {
				mob = npcs[npcList[index - playerCount]];
			}

			if (mob == null || !mob.isVisible()) {
				continue;
			}

			if (mob instanceof Npc) {
				NpcDefinition definition = ((Npc) mob).getDefinition();
				if (definition == null) {
					continue;
				} else if (definition.getMorphisms() != null) {
					definition = definition.morph();
				}
			}

			if (index < playerCount) {
				int l = 30;
				Player player = (Player) mob;
				if (player.headIcon != 0) {
					method127(mob, mob.height + 15);

					if (spriteDrawX > -1) {
						for (int icon = 0; icon < 8; icon++) {
							if ((player.headIcon & 1 << icon) != 0) {
								headIcons[icon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
								l -= 25;
							}
						}
					}
				}

				if (index >= 0 && hintIconDrawType == 10 && lastInteractedWithPlayer == playerList[index]) {
					method127(mob, mob.height + 15);
					if (spriteDrawX > -1) {
						headIcons[7].drawSprite(spriteDrawX - 12, spriteDrawY - l);
					}
				}
			} else {
				NpcDefinition definition = ((Npc) mob).getDefinition();
				if (definition.getHeadIcon() >= 0 && definition.getHeadIcon() < headIcons.length) {
					method127(mob, mob.height + 15);
					if (spriteDrawX > -1) {
						headIcons[definition.getHeadIcon()].drawSprite(spriteDrawX - 12, spriteDrawY - 30);
					}
				}

				if (hintIconDrawType == 1 && hintedNpc == npcList[index - playerCount] && tick % 20 < 10) {
					method127(mob, mob.height + 15);
					if (spriteDrawX > -1 && headIcons[2] != null) {
						headIcons[2].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
					}
				}
			}

			if (mob.spokenText != null
					&& (index >= playerCount || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1
							&& displayMessageFrom(((Player) mob).name))) {
				method127(mob, mob.height);
				if (spriteDrawX > -1 && anInt974 < anInt975) {
					anIntArray979[anInt974] = bold.getTextWidth(mob.spokenText) / 2;
					anIntArray978[anInt974] = bold.getVerticalSpace();
					anIntArray976[anInt974] = spriteDrawX;
					anIntArray977[anInt974] = spriteDrawY;
					textColourEffect[anInt974] = mob.textColour;
					anIntArray981[anInt974] = mob.textEffect;
					anIntArray982[anInt974] = mob.textCycle;
					aStringArray983[anInt974++] = mob.spokenText;
					if (anInt1249 == 0 && mob.textEffect >= 1 && mob.textEffect <= 3) {
						anIntArray978[anInt974] += 10;
						anIntArray977[anInt974] += 5;
					}
					if (anInt1249 == 0 && mob.textEffect == 4) {
						anIntArray979[anInt974] = 60;
					}
					if (anInt1249 == 0 && mob.textEffect == 5) {
						anIntArray978[anInt974] += 5;
					}
				}
			}
			if (mob.cycleStatus > tick) {
				method127(mob, mob.height + 15);
				if (spriteDrawX > -1) {
					int i1 = mob.currentHealth * 30 / mob.maximumHealth;
					if (i1 > 30) {
						i1 = 30;
					}
					Raster.fillRectangle(spriteDrawX - 15, spriteDrawY - 3, i1, 5, 65280);
					Raster.fillRectangle(spriteDrawX - 15 + i1, spriteDrawY - 3, 30 - i1, 5, 0xff0000);
				}
			}
			for (int j1 = 0; j1 < 4; j1++) {
				if (mob.hitCycles[j1] > tick) {
					method127(mob, mob.height / 2);
					if (spriteDrawX > -1) {
						if (j1 == 1) {
							spriteDrawY -= 20;
						}
						if (j1 == 2) {
							spriteDrawX -= 15;
							spriteDrawY -= 10;
						}
						if (j1 == 3) {
							spriteDrawX += 15;
							spriteDrawY -= 10;
						}
						hitMarks[mob.hitTypes[j1]].drawSprite(spriteDrawX - 12, spriteDrawY - 12);
						smallFont.renderCentre(spriteDrawX, spriteDrawY + 4, String.valueOf(mob.hitDamages[j1]), 0);
						smallFont.renderCentre(spriteDrawX - 1, spriteDrawY + 3, String.valueOf(mob.hitDamages[j1]), 0xffffff);
					}
				}
			}
		}

		for (int message = 0; message < anInt974; message++) {
			int k1 = anIntArray976[message];
			int l1 = anIntArray977[message];
			int j2 = anIntArray979[message];
			int k2 = anIntArray978[message];
			boolean flag = true;
			while (flag) {
				flag = false;
				for (int l2 = 0; l2 < message; l2++) {
					if (l1 + 2 > anIntArray977[l2] - anIntArray978[l2] && l1 - k2 < anIntArray977[l2] + 2
							&& k1 - j2 < anIntArray976[l2] + anIntArray979[l2] && k1 + j2 > anIntArray976[l2] - anIntArray979[l2]
							&& anIntArray977[l2] - anIntArray978[l2] < l1) {
						l1 = anIntArray977[l2] - anIntArray978[l2];
						flag = true;
					}
				}

			}
			spriteDrawX = anIntArray976[message];
			spriteDrawY = anIntArray977[message] = l1;
			String chatMessage = aStringArray983[message];
			if (anInt1249 == 0) {
				int textColour = 0xffff00;
				if (textColourEffect[message] < 6) {
					textColour = textColours[textColourEffect[message]];
				}
				if (textColourEffect[message] == 6) {
					textColour = anInt1265 % 20 >= 10 ? 0xffff00 : 0xff0000;
				}
				if (textColourEffect[message] == 7) {
					textColour = anInt1265 % 20 >= 10 ? 65535 : 255;
				}
				if (textColourEffect[message] == 8) {
					textColour = anInt1265 % 20 >= 10 ? 0x80ff80 : 45056;
				}
				if (textColourEffect[message] == 9) {
					int j3 = 150 - anIntArray982[message];
					if (j3 < 50) {
						textColour = 0xff0000 + 1280 * j3;
					} else if (j3 < 100) {
						textColour = 0xffff00 - 0x50000 * (j3 - 50);
					} else if (j3 < 150) {
						textColour = 65280 + 5 * (j3 - 100);
					}
				}
				if (textColourEffect[message] == 10) {
					int k3 = 150 - anIntArray982[message];
					if (k3 < 50) {
						textColour = 0xff0000 + 5 * k3;
					} else if (k3 < 100) {
						textColour = 0xff00ff - 0x50000 * (k3 - 50);
					} else if (k3 < 150) {
						textColour = 255 + 0x50000 * (k3 - 100) - 5 * (k3 - 100);
					}
				}
				if (textColourEffect[message] == 11) {
					int l3 = 150 - anIntArray982[message];
					if (l3 < 50) {
						textColour = 0xffffff - 0x50005 * l3;
					} else if (l3 < 100) {
						textColour = 65280 + 0x50005 * (l3 - 50);
					} else if (l3 < 150) {
						textColour = 0xffffff - 0x50000 * (l3 - 100);
					}
				}
				if (anIntArray981[message] == 0) {
					bold.renderCentre(spriteDrawX, spriteDrawY + 1, chatMessage, 0);
					bold.renderCentre(spriteDrawX, spriteDrawY, chatMessage, textColour);
				}
				if (anIntArray981[message] == 1) {
					bold.wave(chatMessage, spriteDrawX, spriteDrawY + 1, 0, anInt1265);
					bold.wave(chatMessage, spriteDrawX, spriteDrawY, textColour, anInt1265);
				}
				if (anIntArray981[message] == 2) {
					bold.wave2(chatMessage, spriteDrawX, spriteDrawY + 1, 0, anInt1265);
					bold.wave2(chatMessage, spriteDrawX, spriteDrawY, textColour, anInt1265);
				}
				if (anIntArray981[message] == 3) {
					bold.shake(chatMessage, spriteDrawX, spriteDrawY + 1, 0, 150 - anIntArray982[message], anInt1265);
					bold.shake(chatMessage, spriteDrawX, spriteDrawY, textColour, 150 - anIntArray982[message], anInt1265);
				}
				if (anIntArray981[message] == 4) {
					int i4 = bold.getTextWidth(chatMessage);
					int k4 = (150 - anIntArray982[message]) * (i4 + 100) / 150;
					Raster.setBounds(334, spriteDrawX - 50, spriteDrawX + 50, 0);
					bold.render(spriteDrawX + 50 - k4, spriteDrawY + 1, chatMessage, 0);
					bold.render(spriteDrawX + 50 - k4, spriteDrawY, chatMessage, textColour);
					Raster.setDefaultBounds();
				}
				if (anIntArray981[message] == 5) {
					int j4 = 150 - anIntArray982[message];
					int l4 = 0;
					if (j4 < 25) {
						l4 = j4 - 25;
					} else if (j4 > 125) {
						l4 = j4 - 125;
					}
					Raster.setBounds(spriteDrawY + 5, 0, 512, spriteDrawY - bold.getVerticalSpace() - 1);
					bold.renderCentre(spriteDrawX, spriteDrawY + 1 + l4, chatMessage, 0);
					bold.renderCentre(spriteDrawX, spriteDrawY + l4, chatMessage, textColour);
					Raster.setDefaultBounds();
				}
			} else {
				bold.renderCentre(spriteDrawX, spriteDrawY + 1, chatMessage, 0);
				bold.renderCentre(spriteDrawX, spriteDrawY, chatMessage, 0xffff00);
			}
		}

	}

	public final void updateChatMode() {
		if (super.lastMetaModifier == 1) {
			if (super.lastClickX >= 6 && super.lastClickX <= 106 && super.lastClickY >= 467 && super.lastClickY <= 499) {
				publicChatMode = (publicChatMode + 1) % 4;
				aBoolean1233 = true;
				outgoing.writeOpcode(95);
				outgoing.writeByte(publicChatMode);
				outgoing.writeByte(privateChatMode);
				outgoing.writeByte(tradeChatMode);
			}
			if (super.lastClickX >= 135 && super.lastClickX <= 235 && super.lastClickY >= 467 && super.lastClickY <= 499) {
				privateChatMode = (privateChatMode + 1) % 3;
				aBoolean1233 = true;
				outgoing.writeOpcode(95);
				outgoing.writeByte(publicChatMode);
				outgoing.writeByte(privateChatMode);
				outgoing.writeByte(tradeChatMode);
			}
			if (super.lastClickX >= 273 && super.lastClickX <= 373 && super.lastClickY >= 467 && super.lastClickY <= 499) {
				tradeChatMode = (tradeChatMode + 1) % 3;
				aBoolean1233 = true;
				outgoing.writeOpcode(95);
				outgoing.writeByte(publicChatMode);
				outgoing.writeByte(privateChatMode);
				outgoing.writeByte(tradeChatMode);
			}
			if (super.lastClickX >= 412 && super.lastClickX <= 512 && super.lastClickY >= 467 && super.lastClickY <= 499) {
				if (openInterfaceId == -1) {
					clearTopInterfaces();
					reportInput = "";
					reportAbuseMuteToggle = false;
					for (Widget widget : Widget.widgets) {
						if (widget == null || widget.contentType != 600) {
							continue;
						}
						anInt1178 = openInterfaceId = widget.parent;
						break;
					}

				} else {
					addChatMessage(0, "Please close the interface you have open before using 'report abuse'", "");
				}
			}
			anInt940++;
			if (anInt940 > 1386) {
				anInt940 = 0;
				outgoing.writeOpcode(165);
				outgoing.writeByte(0);
				int start = outgoing.getPosition();
				outgoing.writeByte(139);
				outgoing.writeByte(150);
				outgoing.writeShort(32131);
				outgoing.writeByte((int) (Math.random() * 256));
				outgoing.writeShort(3250);
				outgoing.writeByte(177);
				outgoing.writeShort(24859);
				outgoing.writeByte(119);
				if ((int) (Math.random() * 2) == 0) {
					outgoing.writeShort(47234);
				}
				if ((int) (Math.random() * 2) == 0) {
					outgoing.writeByte(21);
				}
				outgoing.writeSizeByte(outgoing.getPosition() - start);
			}
		}
	}

	public final void updateScrollbar(Widget widget, int i, int j, int k, int l, int i1, boolean redraw, int j1) {
		if (aBoolean972) {
			anInt992 = 32;
		} else {
			anInt992 = 0;
		}

		aBoolean972 = false;
		if (k >= i && k < i + 16 && l >= i1 && l < i1 + 16) {
			widget.scrollPosition -= anInt1213 * 4;
			if (redraw) {
				redrawTabArea = true;
			}
		} else if (k >= i && k < i + 16 && l >= i1 + j - 16 && l < i1 + j) {
			widget.scrollPosition += anInt1213 * 4;
			if (redraw) {
				redrawTabArea = true;
			}
		} else if (k >= i - anInt992 && k < i + 16 + anInt992 && l >= i1 + 16 && l < i1 + j - 16 && anInt1213 > 0) {
			int l1 = (j - 32) * j / j1;
			if (l1 < 8) {
				l1 = 8;
			}
			int i2 = l - i1 - 16 - l1 / 2;
			int j2 = j - 32 - l1;
			widget.scrollPosition = (j1 - j) * i2 / j2;
			if (redraw) {
				redrawTabArea = true;
			}

			aBoolean972 = true;
		}
	}

	public final void updateVarp(int id) {
		int parameter = VariableParameter.parameters[id].getParameter();
		if (parameter == 0) {
			return;
		}

		int state = settings[id];
		if (parameter == 1) {
			if (state == 1) {
				Rasterizer.method372(0.9);
			} else if (state == 2) {
				Rasterizer.method372(0.8);
			} else if (state == 3) {
				Rasterizer.method372(0.7);
			} else if (state == 4) {
				Rasterizer.method372(0.6);
			}

			ItemDefinition.sprites.clear();
			aBoolean1255 = true;
		}
		if (parameter == 3) {
			boolean previousPlayingMusic = playingMusic;
			if (state == 0) {
				adjustMidiVolume(playingMusic, 0);
				playingMusic = true;
			} else if (state == 1) {
				adjustMidiVolume(playingMusic, -400);
				playingMusic = true;
			} else if (state == 2) {
				adjustMidiVolume(playingMusic, -800);
				playingMusic = true;
			} else if (state == 3) {
				adjustMidiVolume(playingMusic, -1200);
				playingMusic = true;
			} else if (state == 4) {
				playingMusic = false;
			}

			if (playingMusic != previousPlayingMusic && !lowMemory) {
				if (playingMusic) {
					musicId = nextMusicId;
					fadeMusic = true;
					provider.provide(2, musicId);
				} else {
					stopMidi();
				}
				songDelay = 0;
			}
		} else if (parameter == 4) {
			if (state == 0) {
				aBoolean848 = true;
				setWaveVolume(0);
			} else if (state == 1) {
				aBoolean848 = true;
				setWaveVolume(-400);
			} else if (state == 2) {
				aBoolean848 = true;
				setWaveVolume(-800);
			} else if (state == 3) {
				aBoolean848 = true;
				setWaveVolume(-1200);
			} else if (state == 4) {
				aBoolean848 = false;
			}
		} else if (parameter == 5) {
			anInt1253 = state;
		} else if (parameter == 6) {
			anInt1249 = state;
		} else if (parameter == 8) {
			anInt1195 = state;
			redrawDialogueBox = true;
		} else if (parameter == 9) {
			anInt913 = state;
		}
	}

	public final boolean walk(int movementType, int orientation, int height, int type, int initialY, int width, int surroundings,
			int finalY, int initialX, boolean flag, int finalX) {
		byte mapWidth = 104;
		byte mapLength = 104;
		for (int x = 0; x < mapWidth; x++) {
			for (int y = 0; y < mapLength; y++) {
				anIntArrayArray901[x][y] = 0;
				distances[x][y] = 0x5f5e0ff;
			}
		}

		int currentX = initialX;
		int currentY = initialY;
		anIntArrayArray901[initialX][initialY] = 99;
		distances[initialX][initialY] = 0;
		int nextIndex = 0;
		int currentIndex = 0;
		waypointX[nextIndex] = initialX;
		waypointY[nextIndex++] = initialY;
		boolean reached = false;
		int waypoints = waypointX.length;
		int[][] adjacencies = collisionMaps[plane].adjacencies;

		while (currentIndex != nextIndex) {
			currentX = waypointX[currentIndex];
			currentY = waypointY[currentIndex];
			currentIndex = (currentIndex + 1) % waypoints;

			if (currentX == finalX && currentY == finalY) {
				reached = true;
				break;
			}

			if (type != 0) {
				if ((type < 5 || type == 10)
						&& collisionMaps[plane].reachedWall(currentX, currentY, finalX, finalY, orientation, type - 1)) {
					reached = true;
					break;
				}
				if (type < 10
						&& collisionMaps[plane].reachedDecoration(currentY, currentX, finalX, finalY, type - 1, orientation)) {
					reached = true;
					break;
				}
			}

			if (width != 0 && height != 0
					&& collisionMaps[plane].reachedObject(currentX, currentY, finalX, finalY, height, surroundings, width)) {
				reached = true;
				break;
			}

			int distance = distances[currentX][currentY] + 1;
			if (currentX > 0 && anIntArrayArray901[currentX - 1][currentY] == 0
					&& (adjacencies[currentX - 1][currentY] & 0x1280108) == 0) {
				waypointX[nextIndex] = currentX - 1;
				waypointY[nextIndex] = currentY;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX - 1][currentY] = 2;
				distances[currentX - 1][currentY] = distance;
			}
			if (currentX < mapWidth - 1 && anIntArrayArray901[currentX + 1][currentY] == 0
					&& (adjacencies[currentX + 1][currentY] & 0x1280180) == 0) {
				waypointX[nextIndex] = currentX + 1;
				waypointY[nextIndex] = currentY;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX + 1][currentY] = 8;
				distances[currentX + 1][currentY] = distance;
			}
			if (currentY > 0 && anIntArrayArray901[currentX][currentY - 1] == 0
					&& (adjacencies[currentX][currentY - 1] & 0x1280102) == 0) {
				waypointX[nextIndex] = currentX;
				waypointY[nextIndex] = currentY - 1;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX][currentY - 1] = 1;
				distances[currentX][currentY - 1] = distance;
			}
			if (currentY < mapLength - 1 && anIntArrayArray901[currentX][currentY + 1] == 0
					&& (adjacencies[currentX][currentY + 1] & 0x1280120) == 0) {
				waypointX[nextIndex] = currentX;
				waypointY[nextIndex] = currentY + 1;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX][currentY + 1] = 4;
				distances[currentX][currentY + 1] = distance;
			}
			if (currentX > 0 && currentY > 0 && anIntArrayArray901[currentX - 1][currentY - 1] == 0
					&& (adjacencies[currentX - 1][currentY - 1] & 0x128010e) == 0
					&& (adjacencies[currentX - 1][currentY] & 0x1280108) == 0
					&& (adjacencies[currentX][currentY - 1] & 0x1280102) == 0) {
				waypointX[nextIndex] = currentX - 1;
				waypointY[nextIndex] = currentY - 1;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX - 1][currentY - 1] = 3;
				distances[currentX - 1][currentY - 1] = distance;
			}
			if (currentX < mapWidth - 1 && currentY > 0 && anIntArrayArray901[currentX + 1][currentY - 1] == 0
					&& (adjacencies[currentX + 1][currentY - 1] & 0x1280183) == 0
					&& (adjacencies[currentX + 1][currentY] & 0x1280180) == 0
					&& (adjacencies[currentX][currentY - 1] & 0x1280102) == 0) {
				waypointX[nextIndex] = currentX + 1;
				waypointY[nextIndex] = currentY - 1;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX + 1][currentY - 1] = 9;
				distances[currentX + 1][currentY - 1] = distance;
			}
			if (currentX > 0 && currentY < mapLength - 1 && anIntArrayArray901[currentX - 1][currentY + 1] == 0
					&& (adjacencies[currentX - 1][currentY + 1] & 0x1280138) == 0
					&& (adjacencies[currentX - 1][currentY] & 0x1280108) == 0
					&& (adjacencies[currentX][currentY + 1] & 0x1280120) == 0) {
				waypointX[nextIndex] = currentX - 1;
				waypointY[nextIndex] = currentY + 1;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX - 1][currentY + 1] = 6;
				distances[currentX - 1][currentY + 1] = distance;
			}
			if (currentX < mapWidth - 1 && currentY < mapLength - 1 && anIntArrayArray901[currentX + 1][currentY + 1] == 0
					&& (adjacencies[currentX + 1][currentY + 1] & 0x12801e0) == 0
					&& (adjacencies[currentX + 1][currentY] & 0x1280180) == 0
					&& (adjacencies[currentX][currentY + 1] & 0x1280120) == 0) {
				waypointX[nextIndex] = currentX + 1;
				waypointY[nextIndex] = currentY + 1;
				nextIndex = (nextIndex + 1) % waypoints;
				anIntArrayArray901[currentX + 1][currentY + 1] = 12;
				distances[currentX + 1][currentY + 1] = distance;
			}
		}
		anInt1264 = 0;
		if (!reached) {
			if (flag) {
				int i5 = 100;
				for (int delta = 1; delta < 2; delta++) {
					for (int x = finalX - delta; x <= finalX + delta; x++) {
						for (int y = finalY - delta; y <= finalY + delta; y++) {
							if (x >= 0 && y >= 0 && x < 104 && y < 104 && distances[x][y] < i5) {
								i5 = distances[x][y];
								currentX = x;
								currentY = y;
								anInt1264 = 1;
								reached = true;
							}
						}
					}

					if (reached) {
						break;
					}
				}
			}
			if (!reached) {
				return false;
			}
		}
		currentIndex = 0;
		waypointX[currentIndex] = currentX;
		waypointY[currentIndex++] = currentY;
		int l5;
		for (int j5 = l5 = anIntArrayArray901[currentX][currentY]; currentX != initialX || currentY != initialY; j5 = anIntArrayArray901[currentX][currentY]) {
			if (j5 != l5) {
				l5 = j5;
				waypointX[currentIndex] = currentX;
				waypointY[currentIndex++] = currentY;
			}
			if ((j5 & 2) != 0) {
				currentX++;
			} else if ((j5 & 8) != 0) {
				currentX--;
			}
			if ((j5 & 1) != 0) {
				currentY++;
			} else if ((j5 & 4) != 0) {
				currentY--;
			}
		}

		if (currentIndex > 0) {
			int waypointCount = currentIndex;
			if (waypointCount > 25) {
				waypointCount = 25;
			}
			currentIndex--;
			int x = waypointX[currentIndex];
			int y = waypointY[currentIndex];
			anInt1288 += waypointCount;
			if (anInt1288 >= 92) {
				outgoing.writeOpcode(36);
				outgoing.writeInt(0);
				anInt1288 = 0;
			}
			if (movementType == 0) {
				outgoing.writeOpcode(164);
				outgoing.writeByte(waypointCount + waypointCount + 3);
			}
			if (movementType == 1) {
				outgoing.writeOpcode(248);
				outgoing.writeByte(waypointCount + waypointCount + 3 + 14);
			}
			if (movementType == 2) {
				outgoing.writeOpcode(98);
				outgoing.writeByte(waypointCount + waypointCount + 3);
			}
			outgoing.writeLEShortA(x + regionBaseX);
			destinationX = waypointX[0];
			destinationY = waypointY[0];
			for (int j7 = 1; j7 < waypointCount; j7++) {
				currentIndex--;
				outgoing.writeByte(waypointX[currentIndex] - x);
				outgoing.writeByte(waypointY[currentIndex] - y);
			}

			outgoing.writeLEShort(y + regionBaseY);
			outgoing.writeNegatedByte(super.keyStatuses[5] != 1 ? 0 : 1);
			return true;
		}
		return movementType != 1;
	}

	public final boolean waveReplay() {
		return SignLink.waveReplay();
	}

	public final boolean waveSave(byte buffer[], int length) {
		if (buffer == null) {
			return true;
		}

		return SignLink.waveSave(buffer, length);
	}

	private final void error(String s) {
		System.err.println("ERROR: " + s);
		try {
			getAppletContext().showDocument(new URL(getCodeBase(), "loaderror_" + s + ".html"));
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		do {
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
		} while (true);
	}

	private final void method115() {
		if (loadingStage == 2) {
			for (SpawnedObject spawn = (SpawnedObject) spawns.getFront(); spawn != null; spawn = (SpawnedObject) spawns.getNext()) {
				if (spawn.getLongetivity() > 0) {
					spawn.setLongetivity(spawn.getLongetivity() - 1);
				}

				if (spawn.getLongetivity() == 0) {
					if (spawn.getPreviousId() < 0 || MapRegion.modelReady(spawn.getPreviousId(), spawn.getPreviousType())) {
						removeObject(spawn.getX(), spawn.getY(), spawn.getPlane(), spawn.getGroup(),
								spawn.getPreviousOrientation(), spawn.getPreviousType(), spawn.getPreviousId());
						spawn.unlink();
					}
				} else {
					if (spawn.getDelay() > 0) {
						spawn.setDelay(spawn.getDelay() - 1);
					}
					if (spawn.getDelay() == 0 && spawn.getX() >= 1 && spawn.getY() >= 1 && spawn.getX() <= 102
							&& spawn.getY() <= 102 && (spawn.getId() < 0 || MapRegion.modelReady(spawn.getId(), spawn.getType()))) {
						removeObject(spawn.getX(), spawn.getY(), spawn.getPlane(), spawn.getGroup(), spawn.getOrientation(),
								spawn.getType(), spawn.getId());
						spawn.setDelay(-1);
						if (spawn.getId() == spawn.getPreviousId() && spawn.getPreviousId() == -1) {
							spawn.unlink();
						} else if (spawn.getId() == spawn.getPreviousId()
								&& spawn.getOrientation() == spawn.getPreviousOrientation()
								&& spawn.getType() == spawn.getPreviousType()) {
							spawn.unlink();
						}
					}
				}
			}
		}
	}

	private final void method63() {
		SpawnedObject spawn = (SpawnedObject) spawns.getFront();
		for (; spawn != null; spawn = (SpawnedObject) spawns.getNext()) {
			if (spawn.getLongetivity() == -1) {
				spawn.setDelay(0);
				method89(spawn);
			} else {
				spawn.unlink();
			}
		}

	}

	private final void method89(SpawnedObject spawn) {
		int key = 0;
		int id = -1;
		int type = 0;
		int orientation = 0;

		if (spawn.getGroup() == 0) {
			key = scene.getWallKey(spawn.getX(), spawn.getY(), spawn.getPlane());
		} else if (spawn.getGroup() == 1) {
			key = scene.getWallDecorationKey(spawn.getX(), spawn.getY(), spawn.getPlane());
		} else if (spawn.getGroup() == 2) {
			key = scene.getInteractableObjectKey(spawn.getX(), spawn.getY(), spawn.getPlane());
		} else if (spawn.getGroup() == 3) {
			key = scene.getFloorDecorationKey(spawn.getX(), spawn.getY(), spawn.getPlane());
		}

		if (key != 0) {
			int config = scene.getConfig(spawn.getX(), spawn.getY(), spawn.getPlane(), key);
			id = key >> 14 & 0x7fff;
			type = config & 0x1f;
			orientation = config >> 6;
		}

		spawn.setPreviousId(id);
		spawn.setPreviousType(type);
		spawn.setPreviousOrientation(orientation);
	}

	private final void parsePlayerSynchronizationMask(Buffer buffer) {
		for (int i = 0; i < mobsAwaitingUpdateCount; i++) {
			int index = mobsAwaitingUpdate[i];
			Player player = players[index];
			int mask = buffer.readUByte();
			if ((mask & 0x40) != 0) {
				mask += buffer.readUByte() << 8;
			}

			processPlayerSynchronizationMask(buffer, player, index, mask);
		}
	}

	private final void processNpcSynchronizationMask(Buffer buffer) {
		for (int i = 0; i < mobsAwaitingUpdateCount; i++) {
			int index = mobsAwaitingUpdate[i];
			Npc npc = npcs[index];
			int mask = buffer.readUByte();

			if ((mask & 0x10) != 0) {
				int animation = buffer.readLEUShort();
				if (animation == 65535) {
					animation = -1;
				}

				int delay = buffer.readUByte();

				if (animation == npc.emoteAnimation && animation != -1) {
					int replayMode = Animation.animations[animation].getReplayMode();

					if (replayMode == 1) {
						npc.displayedEmoteFrames = 0;
						npc.emoteTimeRemaining = 0;
						npc.animationDelay = delay;
						npc.currentAnimationLoops = 0;
					} else if (replayMode == 2) {
						npc.currentAnimationLoops = 0;
					}
				} else if (animation == -1
						|| npc.emoteAnimation == -1
						|| Animation.animations[animation].getPriority() >= Animation.animations[npc.emoteAnimation]
								.getPriority()) {
					npc.emoteAnimation = animation;
					npc.displayedEmoteFrames = 0;
					npc.emoteTimeRemaining = 0;
					npc.animationDelay = delay;
					npc.currentAnimationLoops = 0;
					npc.anInt1542 = npc.remainingPath;
				}
			}

			if ((mask & 8) != 0) {
				int damage = buffer.readUByteA();
				int type = buffer.readNegUByte();
				npc.damage(damage, type, tick);
				npc.cycleStatus = tick + 300;
				npc.currentHealth = buffer.readUByteA();
				npc.maximumHealth = buffer.readUByte();
			}

			if ((mask & 0x80) != 0) {
				npc.graphic = buffer.readUShort();
				int info = buffer.readInt();
				npc.graphicHeight = info >> 16;
				npc.graphicDelay = tick + (info & 0xffff);
				npc.currentAnimation = 0;
				npc.anInt1522 = 0;

				if (npc.graphicDelay > tick) {
					npc.currentAnimation = -1;
				}

				if (npc.graphic == 65535) {
					npc.graphic = -1;
				}
			}

			if ((mask & 0x20) != 0) {
				npc.interactingMob = buffer.readUShort();
				if (npc.interactingMob == 65535) {
					npc.interactingMob = -1;
				}
			}

			if ((mask & 1) != 0) {
				npc.spokenText = buffer.readString();
				npc.textCycle = 100;
			}

			if ((mask & 0x40) != 0) {
				int damage = buffer.readNegUByte();
				int type = buffer.readUByteS();
				npc.damage(damage, type, tick);
				npc.cycleStatus = tick + 300;
				npc.currentHealth = buffer.readUByteS();
				npc.maximumHealth = buffer.readNegUByte();
			}

			if ((mask & 2) != 0) {
				npc.setDefinition(NpcDefinition.lookup(buffer.readLEUShortA()));
				npc.size = npc.getDefinition().getSize();
				npc.rotation = npc.getDefinition().getRotation();
				npc.walkingAnimation = npc.getDefinition().getWalkingAnimation();
				npc.halfTurnAnimation = npc.getDefinition().getHalfTurnAnimation();
				npc.quarterClockwiseTurnAnimation = npc.getDefinition().getRotateClockwiseAnimation();
				npc.quarterAnticlockwiseTurnAnimation = npc.getDefinition().getRotateAntiClockwiseAnimation();
				npc.idleAnimation = npc.getDefinition().getIdleAnimation();
			}

			if ((mask & 4) != 0) {
				npc.faceX = buffer.readLEUShort();
				npc.faceY = buffer.readLEUShort();
			}
		}
	}

	private final void processPlayerSynchronizationMask(Buffer buffer, Player player, int index, int mask) {
		if ((mask & 0x400) != 0) {
			player.initialX = buffer.readUByteS();
			player.initialY = buffer.readUByteS();
			player.destinationX = buffer.readUByteS();
			player.destinationY = buffer.readUByteS();
			player.startForceMovement = buffer.readLEUShortA() + tick;
			player.endForceMovement = buffer.readUShortA() + tick;
			player.direction = buffer.readUByteS();
			player.resetPath();
		}

		if ((mask & 0x100) != 0) {
			player.graphic = buffer.readLEUShort();
			int info = buffer.readInt();
			player.graphicHeight = info >> 16;
			player.graphicDelay = tick + (info & 0xffff);
			player.currentAnimation = 0;
			player.anInt1522 = 0;

			if (player.graphicDelay > tick) {
				player.currentAnimation = -1;
			}
			if (player.graphic == 65535) {
				player.graphic = -1;
			}
		}

		if ((mask & 8) != 0) {
			int animation = buffer.readLEUShort();
			if (animation == 65535) {
				animation = -1;
			}

			int delay = buffer.readNegUByte();
			if (animation == player.emoteAnimation && animation != -1) {
				int replayMode = Animation.animations[animation].getReplayMode();
				if (replayMode == 1) {
					player.displayedEmoteFrames = 0;
					player.emoteTimeRemaining = 0;
					player.animationDelay = delay;
					player.currentAnimationLoops = 0;
				} else if (replayMode == 2) {
					player.currentAnimationLoops = 0;
				}
			} else if (animation == -1 || player.emoteAnimation == -1
					|| Animation.animations[animation].getPriority() >= Animation.animations[player.emoteAnimation].getPriority()) {
				player.emoteAnimation = animation;
				player.displayedEmoteFrames = 0;
				player.emoteTimeRemaining = 0;
				player.animationDelay = delay;
				player.currentAnimationLoops = 0;
				player.anInt1542 = player.remainingPath;
			}
		}

		if ((mask & 4) != 0) {
			player.spokenText = buffer.readString();

			if (player.spokenText.charAt(0) == '~') {
				player.spokenText = player.spokenText.substring(1);
				addChatMessage(2, player.spokenText, player.name);
			} else if (player == localPlayer) {
				addChatMessage(2, player.spokenText, player.name);
			}
			player.textColour = 0;
			player.textEffect = 0;
			player.textCycle = 150;
		}

		if ((mask & 0x80) != 0) {
			int textInfo = buffer.readLEUShort();
			int privilege = buffer.readUByte();
			int offset = buffer.readNegUByte();
			int off = buffer.getPosition();

			if (player.name != null && player.visible) {
				long name = StringUtils.encodeBase37(player.name);
				boolean ignored = false;
				if (privilege <= 1) {
					for (int i4 = 0; i4 < ignoredCount; i4++) {
						if (ignores[i4] != name) {
							continue;
						}
						ignored = true;
						break;
					}
				}

				if (!ignored && onTutorialIsland == 0) {
					try {
						chatBuffer.setPosition(0);
						buffer.readReverseData(chatBuffer.getPayload(), offset, 0);
						chatBuffer.setPosition(0);
						String text = ChatMessageCodec.decode(chatBuffer, offset);
						text = MessageCensor.apply(text);
						player.spokenText = text;
						player.textColour = textInfo >> 8;
						player.textEffect = textInfo & 0xff;
						player.textCycle = 150;

						if (privilege == 2 || privilege == 3) {
							addChatMessage(1, text, "@cr2@" + player.name);
						} else if (privilege == 1) {
							addChatMessage(1, text, "@cr1@" + player.name);
						} else {
							addChatMessage(2, text, player.name);
						}
					} catch (Exception exception) {
						SignLink.reportError("cde2");
					}
				}
			}

			buffer.setPosition(off + offset);
		}

		if ((mask & 1) != 0) {
			player.interactingMob = buffer.readLEUShort();
			if (player.interactingMob == 65535) {
				player.interactingMob = -1;
			}
		}

		if ((mask & 0x10) != 0) {
			int length = buffer.readNegUByte();
			byte[] data = new byte[length];
			Buffer appearanceBuffer = new Buffer(data);
			buffer.readData(data, 0, length);
			playerSynchronizationBuffers[index] = appearanceBuffer;
			player.updateAppearance(appearanceBuffer);
		}

		if ((mask & 2) != 0) {
			player.faceX = buffer.readLEUShortA();
			player.faceY = buffer.readLEUShort();
		}

		if ((mask & 0x20) != 0) {
			int damage = buffer.readUByte();
			int type = buffer.readUByteA();
			player.damage(damage, type, tick);
			player.cycleStatus = tick + 300;
			player.currentHealth = buffer.readNegUByte();
			player.maximumHealth = buffer.readUByte();
		}

		if ((mask & 0x200) != 0) {
			int damage = buffer.readUByte();
			int type = buffer.readUByteS();
			player.damage(damage, type, tick);
			player.cycleStatus = tick + 300;
			player.currentHealth = buffer.readUByte();
			player.maximumHealth = buffer.readNegUByte();
		}
	}

	private final void removeObject(int x, int y, int z, int group, int previousOrientation, int previousType, int previousId) {
		if (x >= 1 && y >= 1 && x <= 102 && y <= 102) {
			if (lowMemory && z != plane) {
				return;
			}

			int key = 0;
			if (group == 0) {
				key = scene.getWallKey(x, y, z);
			} else if (group == 1) {
				key = scene.getWallDecorationKey(x, y, z);
			} else if (group == 2) {
				key = scene.getInteractableObjectKey(x, y, z);
			} else if (group == 3) {
				key = scene.getFloorDecorationKey(x, y, z);
			}

			if (key != 0) {
				int config = scene.getConfig(x, y, z, key);
				int id = key >> 14 & 0x7fff;
				int objectType = config & 0x1f;
				int orientation = config >> 6;

				if (group == 0) {
					scene.removeWall(x, y, z);
					ObjectDefinition definition = ObjectDefinition.lookup(id);

					if (definition.isSolid()) {
						collisionMaps[z].removeObject(x, y, orientation, objectType, definition.isImpenetrable());
					}
				} else if (group == 1) {
					scene.removeWallDecoration(x, y, z);
				} else if (group == 2) {
					scene.removeObject(x, y, z);
					ObjectDefinition definition = ObjectDefinition.lookup(id);
					if (x + definition.getWidth() > 103 || y + definition.getWidth() > 103 || x + definition.getLength() > 103
							|| y + definition.getLength() > 103) {
						return;
					}
					if (definition.isSolid()) {
						collisionMaps[z].removeObject(orientation, definition.getWidth(), x, y, definition.getLength(),
								definition.isImpenetrable());
					}
				} else if (group == 3) {
					scene.removeFloorDecoration(x, y, z);
					ObjectDefinition definition = ObjectDefinition.lookup(id);
					if (definition.isSolid() && definition.isInteractive()) {
						collisionMaps[z].removeFloorDecoration(x, y);
					}
				}
			}

			if (previousId >= 0) {
				int plane = z;
				if (plane < 3 && (tileFlags[1][x][y] & 2) == 2) {
					plane++;
				}

				MapRegion.placeObject(collisionMaps[z], scene, previousId, x, y, plane, previousType, previousOrientation,
						tileHeights, z);
			}
		}
	}

	private final void spawnObject(int id, int x, int y, int plane, int group, int orientation, int longetivity, int type,
			int delay) {
		SpawnedObject object = null;
		for (SpawnedObject node = (SpawnedObject) spawns.getFront(); node != null; node = (SpawnedObject) spawns.getNext()) {
			if (node.getPlane() != plane || node.getX() != x || node.getY() != y || node.getGroup() != group) {
				continue;
			}

			object = node;
			break;
		}

		if (object == null) {
			object = new SpawnedObject();
			object.setPlane(plane);
			object.setGroup(group);
			object.setX(x);
			object.setY(y);
			method89(object);
			spawns.pushBack(object);
		}

		object.setId(id);
		object.setType(type);
		object.setOrientation(orientation);
		object.setDelay(delay);
		object.setLongetivity(longetivity);
	}

	private final void synchronizeLocalPlayerMovement(Buffer buffer) {
		buffer.enableBitAccess();
		int update = buffer.readBits(1);
		if (update == 0) {
			return;
		}

		int type = buffer.readBits(2);
		if (type == 0) {
			mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = internalLocalPlayerIndex;
		} else if (type == 1) {
			int direction = buffer.readBits(3);
			localPlayer.walk(direction, false);
			int updateRequired = buffer.readBits(1);

			if (updateRequired == 1) {
				mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = internalLocalPlayerIndex;
			}
		} else if (type == 2) {
			int firstDirection = buffer.readBits(3);
			localPlayer.walk(firstDirection, true);
			int secondDirection = buffer.readBits(3);
			localPlayer.walk(secondDirection, true);
			int updateRequired = buffer.readBits(1);

			if (updateRequired == 1) {
				mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = internalLocalPlayerIndex;
			}
		} else if (type == 3) {
			plane = buffer.readBits(2);
			int teleport = buffer.readBits(1);
			int updateRequired = buffer.readBits(1);

			if (updateRequired == 1) {
				mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = internalLocalPlayerIndex;
			}

			int y = buffer.readBits(7);
			int x = buffer.readBits(7);
			localPlayer.move(x, y, teleport == 1);
		}
	}

	private final void synchronizeNpcs(Buffer buffer, int packetSize) {
		removedMobCount = 0;
		mobsAwaitingUpdateCount = 0;
		updateNpcMovement(buffer);
		updateNpcList(buffer, packetSize);
		processNpcSynchronizationMask(buffer);
		for (int i = 0; i < removedMobCount; i++) {
			int index = removedMobs[i];
			if (npcs[index].time != tick) {
				npcs[index].setDefinition(null);
				npcs[index] = null;
			}
		}

		if (buffer.getPosition() != packetSize) {
			SignLink.reportError(username + " size mismatch in getnpcpos - pos:" + buffer.getPosition() + " psize:" + packetSize);
			throw new RuntimeException("eek");
		}
		for (int i1 = 0; i1 < npcCount; i1++) {
			if (npcs[npcList[i1]] == null) {
				SignLink.reportError(username + " null entry in npc list - pos:" + i1 + " size:" + npcCount);
				throw new RuntimeException("eek");
			}
		}

	}

	private final void synchronizeOtherPlayerMovement(Buffer buffer) {
		int count = buffer.readBits(8);
		if (count < playerCount) {
			for (int index = count; index < playerCount; index++) {
				removedMobs[removedMobCount++] = playerList[index];
			}
		}

		if (count > playerCount) {
			SignLink.reportError(username + " Too many players");
			throw new RuntimeException("eek");
		}

		playerCount = 0;
		for (int i = 0; i < count; i++) {
			int index = playerList[i];
			Player player = players[index];

			int updateRequired = buffer.readBits(1);
			if (updateRequired == 0) {
				playerList[playerCount++] = index;
				player.time = tick;
			} else {
				int movementType = buffer.readBits(2);

				if (movementType == 0) {
					playerList[playerCount++] = index;
					player.time = tick;
					mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
				} else if (movementType == 1) {
					playerList[playerCount++] = index;
					player.time = tick;
					int direction = buffer.readBits(3);
					player.walk(direction, false);
					int update = buffer.readBits(1);

					if (update == 1) {
						mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
					}
				} else if (movementType == 2) {
					playerList[playerCount++] = index;
					player.time = tick;
					int firstDirection = buffer.readBits(3);
					player.walk(firstDirection, true);
					int secondDirection = buffer.readBits(3);
					player.walk(secondDirection, true);
					int update = buffer.readBits(1);

					if (update == 1) {
						mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
					}
				} else if (movementType == 3) {
					removedMobs[removedMobCount++] = index;
				}
			}
		}
	}

	private final void synchronizePlayers(int frameSize, Buffer buffer) {
		removedMobCount = 0;
		mobsAwaitingUpdateCount = 0;
		synchronizeLocalPlayerMovement(buffer);
		synchronizeOtherPlayerMovement(buffer);
		updatePlayerList(buffer, frameSize);
		parsePlayerSynchronizationMask(buffer);

		for (int i = 0; i < removedMobCount; i++) {
			int index = removedMobs[i];
			if (players[index].time != tick) {
				players[index] = null;
			}
		}

		if (buffer.getPosition() != frameSize) {
			SignLink.reportError("Error packet size mismatch in getplayer pos:" + buffer.getPosition() + " psize:" + frameSize);
			throw new RuntimeException("eek");
		}

		for (int i = 0; i < playerCount; i++) {
			if (players[playerList[i]] == null) {
				SignLink.reportError(username + " null entry in pl list - pos:" + i + " size:" + playerCount);
				throw new RuntimeException("eek");
			}
		}
	}

	private final void updateNpcList(Buffer buffer, int packetSize) {
		while (buffer.getBitPosition() + 21 < packetSize * 8) {
			int index = buffer.readBits(14);
			if (index == 16383) {
				break;
			}

			if (npcs[index] == null) {
				npcs[index] = new Npc();
			}

			Npc npc = npcs[index];
			npcList[npcCount++] = index;
			npc.time = tick;

			int y = buffer.readBits(5);
			if (y > 15) {
				y -= 32;
			}

			int x = buffer.readBits(5);
			if (x > 15) {
				x -= 32;
			}

			int teleport = buffer.readBits(1);
			npc.setDefinition(NpcDefinition.lookup(buffer.readBits(12)));
			int updateRequired = buffer.readBits(1);
			if (updateRequired == 1) {
				mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
			}

			npc.size = npc.getDefinition().getSize();
			npc.rotation = npc.getDefinition().getRotation();
			npc.walkingAnimation = npc.getDefinition().getWalkingAnimation();
			npc.halfTurnAnimation = npc.getDefinition().getHalfTurnAnimation();
			npc.quarterClockwiseTurnAnimation = npc.getDefinition().getRotateClockwiseAnimation();
			npc.quarterAnticlockwiseTurnAnimation = npc.getDefinition().getRotateAntiClockwiseAnimation();
			npc.idleAnimation = npc.getDefinition().getIdleAnimation();
			npc.move(localPlayer.pathX[0] + x, localPlayer.pathY[0] + y, teleport == 1);
		}
		buffer.disableBitAccess();
	}

	private final void updateNpcMovement(Buffer buffer) {
		buffer.enableBitAccess();
		int count = buffer.readBits(8);
		if (count < npcCount) {
			for (int index = count; index < npcCount; index++) {
				removedMobs[removedMobCount++] = npcList[index];
			}

		}
		if (count > npcCount) {
			SignLink.reportError(username + " Too many npcs");
			throw new RuntimeException("eek");
		}

		npcCount = 0;
		for (int i = 0; i < count; i++) {
			int index = npcList[i];
			Npc npc = npcs[index];
			int update = buffer.readBits(1);

			if (update == 0) {
				npcList[npcCount++] = index;
				npc.time = tick;
			} else {
				int type = buffer.readBits(2);
				if (type == 0) {
					npcList[npcCount++] = index;
					npc.time = tick;
					mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
				} else if (type == 1) {
					npcList[npcCount++] = index;
					npc.time = tick;
					int direction = buffer.readBits(3);
					npc.walk(direction, false);
					int updateRequired = buffer.readBits(1);
					if (updateRequired == 1) {
						mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
					}
				} else if (type == 2) {
					npcList[npcCount++] = index;
					npc.time = tick;
					int first = buffer.readBits(3);
					npc.walk(first, true);
					int second = buffer.readBits(3);
					npc.walk(second, true);
					int updateRequired = buffer.readBits(1);

					if (updateRequired == 1) {
						mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
					}
				} else if (type == 3) {
					removedMobs[removedMobCount++] = index;
				}
			}
		}

	}

	private final void updatePlayerList(Buffer buffer, int packetSize) {
		while (buffer.getBitPosition() + 10 < packetSize * 8) {
			int index = buffer.readBits(11);
			if (index == 2047) {
				break;
			}

			if (players[index] == null) {
				players[index] = new Player();
				if (playerSynchronizationBuffers[index] != null) {
					players[index].updateAppearance(playerSynchronizationBuffers[index]);
				}
			}

			playerList[playerCount++] = index;
			Player player = players[index];
			player.time = tick;
			int update = buffer.readBits(1);
			if (update == 1) {
				mobsAwaitingUpdate[mobsAwaitingUpdateCount++] = index;
			}

			int discardWalkingQueue = buffer.readBits(1);
			int y = buffer.readBits(5);
			if (y > 15) {
				y -= 32;
			}

			int x = buffer.readBits(5);
			if (x > 15) {
				x -= 32;
			}

			player.move(localPlayer.pathX[0] + x, localPlayer.pathY[0] + y, discardWalkingQueue == 1);
		}

		buffer.disableBitAccess();
	}

}