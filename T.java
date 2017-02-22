import java.applet.Applet;
import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Random;

// ///////////////////////////////////// //
// Treasure Hunt 4k, Harm Boschloo 2014  //
// Java4k 2014 Contest http://java4k.com //
// ///////////////////////////////////// //

// Game story/description:
// Find all three treasures.
// Watch out for monstrous sea creatures.
//
// Check the beaches for buried items that may help you in your quest!
// You can spot them from your ship.
// There are eight buried items:
// two that slow down monsters (temporarily), 
// two that increase your view area,
// and four maps that show a quarter of the total map.
//
// Maps are generated randomly. 
// The seed is shown in the URL.
// You can type in your own seed if you want.
// The page needs to reload for this to work.

// Instructions/Input:
// Click to focus.
// Press the arrow keys to move around. 
// Press space to go on land or board your ship.
// Press space to dig for treasure.
// Hold 'm' to see your map.
// Press 'F1' to restart the current map.
// Press 'F2' for a new map.

@SuppressWarnings("serial")
public class T extends Applet implements Runnable {
	private static boolean[] keys = new boolean[65536];

	@Override
	public boolean handleEvent(Event e) {
		return keys[e.key] = (e.id == Event.KEY_PRESS || e.id == Event.KEY_ACTION);
	}

	@Override
	public void run() {
		final boolean DEBUG = false;
		final boolean LOCK_FRAME_RATE = true;

		// math
		final double TAU = 6.2832;

		// game state
		final int GENERATE_TERRAIN = 0;
		final int INITIALIZE_GAME = 1;
		final int PLAYING = 2;

		// colors
		final Color COLOR_CLEAR = new Color(0, 0, 0, 0);
		final Color COLOR_BLACK = new Color(0, 0, 0);
		final Color COLOR_CROSS = new Color(255, 0, 0);
		final Color COLOR_PLAYER_HEAD = new Color(200, 0, 0);
		final Color COLOR_PLAYER_BODY = new Color(255, 150, 0);
		final Color COLOR_SHIP_OUTER = new Color(30, 15, 0);
		final Color COLOR_SHIP_INNER = new Color(50, 30, 0);
		final Color COLOR_SEA_MONSTER = new Color(0, 0, 0, 50);
		final Color COLOR_SEA_MONSTER_REPEL_ITEM = new Color(255, 0, 0, 50);
		final Color COLOR_MAP_BACKGROUND_ITEM = new Color(0, 0, 0, 100);
		final Color COLOR_MAP_AND_LOOKOUT_ITEM = new Color(0, 0, 255, 100);
		final Color COLOR_TREASURE_FOUND = new Color(255, 255, 0);

		// terrain colors
		final Color[] COLOR_MAP = { new Color(0, 0, 100), new Color(0, 0, 200),
				new Color(0, 0, 255), new Color(100, 100, 255),
				new Color(255, 255, 0), new Color(0, 255, 0),
				new Color(0, 200, 0), new Color(0, 150, 0),
				new Color(150, 75, 0), new Color(50, 50, 50),
				new Color(100, 100, 100), new Color(150, 150, 150),
				new Color(200, 200, 200), new Color(255, 255, 255) };

		final int TERRAIN_WATER_1 = 0;
		final int TERRAIN_WATER_2 = 1;
		// final int TERRAIN_WATER_3 = 2;
		final int TERRAIN_SHALLOWS = 3;
		final int TERRAIN_BEACH = 4;
		// final int TERRAIN_GRASS_1 = 5;
		// final int TERRAIN_GRASS_2 = 6;
		// final int TERRAIN_GRASS_3 = 7;
		// final int TERRAIN_DIRT = 8;
		// final int TERRAIN_ROCK_1 = 9;
		// final int TERRAIN_ROCK_2 = 10;
		// final int TERRAIN_ROCK_3 = 11;
		// final int TERRAIN_ROCK_4 = 12;
		final int TERRAIN_SNOW = 13;

		// terrain
		final int TERRAIN_BASE_SIZE = 512;
		final int TERRAIN_BASE_HEIGHT_REDUCTION = 12;
		final int TERRAIN_SIZE = 4 * TERRAIN_BASE_SIZE;
		final int[][] terrainData = new int[TERRAIN_SIZE][TERRAIN_SIZE];

		final double WORLD_SIZE = 65536;
		final double TERRAIN_TO_WORLD = WORLD_SIZE / TERRAIN_SIZE;

		final int NUMBER_OF_OBJECTS = 50;
		final int PLAYER = 0;
		final int SHIP = 1;
		final int SEA_MONSTERS = 2;
		final int PLAYER_TYPE = 1;
		final int SHIP_TYPE = 2;
		final int SEA_MONSTER_TYPE = 3;
		final int PLAYER_WIDTH = 24;
		final int PLAYER_LENGTH = 12;
		final int SHIP_WIDTH = 160;
		final int SHIP_LENGTH = 352;
		final int SHIP_INNER_WIDTH = 64;
		final int SHIP_INNER_LENGTH = 256;
		final int SEA_MONSTER_SIZE = 512;
		final int SEA_MONSTER_SPEED = 64;

		final int PLAYER_ON_SHIP = 0;
		final int PLAYER_ON_LAND = 1;
		final int PLAYER_DEAD = 2;
		final int PLAYER_RICH = 3;

		// keys
		final int KEY_UP = Event.UP;
		final int KEY_DOWN = Event.DOWN;
		final int KEY_LEFT = Event.LEFT;
		final int KEY_RIGHT = Event.RIGHT;
		final int KEY_ACTION = ' ';
		final int KEY_MAP = 'm';
		final int KEY_RESTART = Event.F1;
		final int KEY_NEW_MAP = Event.F2;

		final double ON_SHIP_SCALE = 0.075;
		final double TREASURE_MAP_SCALE = 0.05;
		final double ON_LAND_SCALE = 0.5;
		final double FINISHED_SCALE = 2.0;
		final double LOOKOUT_SCALE_MULTIPLYER = 0.8;
		final double SEA_MONSTER_GAIN_MAX = 1.0;
		final double SEA_MONSTER_GAIN_REDUCTION = 0.6;
		// at 25 Hz it takes 240 seconds to increment to 0.6
		final double SEA_MONSTER_GAIN_INCREMENT = 0.0001;

		// objects
		final double[] objectX = new double[NUMBER_OF_OBJECTS];
		final double[] objectY = new double[NUMBER_OF_OBJECTS];
		final double[] objectAngle = new double[NUMBER_OF_OBJECTS];
		final double[] objectDX = new double[NUMBER_OF_OBJECTS];
		final double[] objectDY = new double[NUMBER_OF_OBJECTS];
		final int[] objectType = new int[NUMBER_OF_OBJECTS];

		// items
		final int ITEM_TREASURE = 0;
		final int ITEM_SEA_MONSTER_REPEL = 1;
		final int ITEM_MAP_NW = 2;
		final int ITEM_MAP_NE = 3;
		final int ITEM_MAP_SW = 4;
		final int ITEM_MAP_SE = 5;
		final int ITEM_LOOKOUT = 6;
		final int NUMBER_OF_ITEMS = 11;
		final int[] items = { ITEM_TREASURE, ITEM_TREASURE, ITEM_TREASURE,
				ITEM_SEA_MONSTER_REPEL, ITEM_SEA_MONSTER_REPEL, ITEM_MAP_NW,
				ITEM_MAP_NE, ITEM_MAP_SW, ITEM_MAP_SE, ITEM_LOOKOUT,
				ITEM_LOOKOUT };
		final int[] itemX = new int[NUMBER_OF_ITEMS];
		final int[] itemY = new int[NUMBER_OF_ITEMS];
		final int[] itemsFound = new int[NUMBER_OF_ITEMS];

		// graphics
		final Rectangle rectangle = new Rectangle(0, 0, 0, 0);
		final AffineTransform IDENTITY = new AffineTransform();
		AffineTransform transform;
		final BufferedImage heightMapBase = new BufferedImage(
				TERRAIN_BASE_SIZE, TERRAIN_BASE_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		final BufferedImage patch = new BufferedImage(TERRAIN_BASE_SIZE,
				TERRAIN_BASE_SIZE, BufferedImage.TYPE_INT_ARGB);
		final BufferedImage heightMap = new BufferedImage(TERRAIN_SIZE,
				TERRAIN_SIZE, BufferedImage.TYPE_INT_ARGB);
		final BufferedImage terrainMap = new BufferedImage(TERRAIN_SIZE,
				TERRAIN_SIZE, BufferedImage.TYPE_INT_RGB);
		final BufferedImage map = new BufferedImage(TERRAIN_SIZE, TERRAIN_SIZE,
				BufferedImage.TYPE_INT_RGB);
		final BufferedImage terrainImage = new BufferedImage(TERRAIN_SIZE,
				TERRAIN_SIZE, BufferedImage.TYPE_INT_RGB);
		final BufferedImage treasureMaps = new BufferedImage(200, 600,
				BufferedImage.TYPE_INT_RGB);
		final BufferedImage screen = new BufferedImage(800, 600,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D heightMapBaseGraphics = (Graphics2D) heightMapBase
				.getGraphics();
		final Graphics2D patchGraphics = (Graphics2D) patch.getGraphics();
		final Graphics2D heightMapGraphics = (Graphics2D) heightMap
				.getGraphics();
		final Graphics2D mapGraphics = (Graphics2D) map.getGraphics();
		final Graphics2D terrainImageGraphics = (Graphics2D) terrainImage
				.getGraphics();
		final Graphics2D treasureMapsGraphics = (Graphics2D) treasureMaps
				.getGraphics();
		final Graphics2D screenGraphics = (Graphics2D) screen.getGraphics();
		final Graphics2D appletGraphics = (Graphics2D) getGraphics();

		// fps, debug
		long fps = 0;
		long fpsAccumulatedTime = 0;
		int fpsFrameCount = 0;

		// initialize random numbers, from the url's anchor name if possible
		final Random r = new Random();
		long seed = 0;
		try {
			seed = Long.parseLong(getDocumentBase().getRef());
		} catch (final Exception e) {
		}

		int i, j, s, v, w, h, x, y = 0, dx, dy, playerState, gameState;
		double worldX, worldY, ox, oy, odx, ody, worldScaleTarget, worldScaleGain, worldScale, seaMonsterGain;

		// ////////////////// //
		// Terrain Generation //
		// ////////////////// //
		do {
			// clear screen
			appletGraphics.setColor(COLOR_BLACK);
			appletGraphics.fillRect(0, 0, 800, 600);

			// set seed
			if (seed == 0) {
				seed = r.nextLong();
				try {
					getAppletContext()
							.showDocument(
									new URL(getDocumentBase().toString().split(
											"#", 2)[0]
											+ "#" + seed));
				} catch (final Exception e) {
				}
			}
			r.setSeed(seed);

			// base height map using grouped ovals
			heightMapBaseGraphics.setBackground(COLOR_CLEAR);
			heightMapBaseGraphics.clearRect(0, 0, TERRAIN_BASE_SIZE,
					TERRAIN_BASE_SIZE);
			patchGraphics.setBackground(COLOR_CLEAR);
			for (i = 0; i < 30; ++i) {
				patchGraphics.clearRect(0, 0, TERRAIN_BASE_SIZE,
						TERRAIN_BASE_SIZE);
				s = (int) ((0.25 + r.nextDouble() * 0.75) * TERRAIN_BASE_SIZE / 5);
				for (j = 0; j < 10; ++j) {
					v = (int) (256 * r.nextDouble() / TERRAIN_BASE_HEIGHT_REDUCTION);
					patchGraphics.setColor(new Color(255, 255, 255, v));
					w = (int) ((0.3 + r.nextDouble() * 0.5) * s);
					h = (int) ((0.3 + r.nextDouble() * 0.5) * w);
					patchGraphics.translate((int) ((s - w) * r.nextDouble())
							+ w / 2, (int) ((s - w) * r.nextDouble()) + w / 2);
					patchGraphics.rotate(TAU * r.nextDouble());
					patchGraphics.fillOval(-w / 2, -h / 2, w, h);
					patchGraphics.scale(0.5, 0.5);
					patchGraphics.rotate(r.nextDouble() / 5);
					patchGraphics.fillOval(-w / 2, -h / 2, w, h);
					patchGraphics.scale(0.5, 0.5);
					patchGraphics.rotate(r.nextDouble() / 5);
					patchGraphics.fillOval(-w / 2, -h / 2, w, h);
					patchGraphics.setTransform(IDENTITY);
				}
				rectangle.setBounds((int) (TERRAIN_BASE_SIZE * r.nextDouble()),
						(int) (TERRAIN_BASE_SIZE * r.nextDouble()),
						TERRAIN_BASE_SIZE, TERRAIN_BASE_SIZE);
				heightMapBaseGraphics.setPaint(new TexturePaint(patch,
						rectangle));
				heightMapBaseGraphics.fillRect(0, 0, TERRAIN_BASE_SIZE,
						TERRAIN_BASE_SIZE);
			}

			// smooth and add some noise
			// this mixed the terrain up a bit
			// and gives it a nice look
			s = TERRAIN_BASE_SIZE;
			for (i = 0; i < 5; ++i) {
				for (x = 0; x < s; ++x) {
					for (y = 0; y < s; ++y) {
						h = (heightMapBase.getRGB(x, y) >> 24) & 0xFF;
						if (h > 0) {
							for (dx = -1; dx <= 1; dx += 2) {
								for (dy = -1; dy <= 1; dy += 2) {
									h += ((heightMapBase.getRGB((x + dx + s)
											% s, (y + dy + s) % s)) >> 24) & 0xFF;
								}
							}
							// add some noise and take average
							h += (int) (-4 * r.nextDouble());
							if (h < 0) {
								h = 0;
							}
							heightMapBase.setRGB(x, y,
									((h / 5) << 24) | 0xFFFFFF);
							// since we're writing back to the same image
							// the smoothing is a bit strange, but that's good
							// this gives a nice asymmetric effect
						}
					}
				}
			}

			// scale up, overlap images for some blur
			heightMapGraphics.setBackground(COLOR_CLEAR);
			heightMapGraphics.clearRect(0, 0, TERRAIN_SIZE, TERRAIN_SIZE);
			for (x = -1; x <= 1; x += 2) {
				for (y = -1; y <= 1; y += 2) {
					rectangle.setBounds(x, y, TERRAIN_SIZE, TERRAIN_SIZE);
					heightMapGraphics.setPaint(new TexturePaint(heightMapBase,
							rectangle));
					heightMapGraphics
							.fillRect(0, 0, TERRAIN_SIZE, TERRAIN_SIZE);
				}
			}

			// smooth final height map
			s = TERRAIN_SIZE;
			for (x = 0; x < s; ++x) {
				for (y = 0; y < s; ++y) {
					h = (heightMap.getRGB(x, y) >> 24) & 0xFF;
					if (h > 0) {
						for (dx = -1; dx <= 1; dx += 2) {
							for (dy = -1; dy <= 1; dy += 2) {
								h += ((heightMap.getRGB((x + dx + s) % s, (y
										+ dy + s)
										% s)) >> 24) & 0xFF;
							}
						}
						heightMap.setRGB(x, y, ((h / 5) << 24) | 0xFFFFFF);
						// since we're writing back to the same image
						// the smoothing is a bit strange, but that's good
						// this gives a nice asymmetric effect
					}
				}
			}

			// create terrain map from height map
			for (x = 0; x < s; ++x) {
				for (y = 0; y < s; ++y) {
					terrainData[x][y] = (int) (((heightMap.getRGB(x, y) >> 24) & 0xFF) / 256.0 * COLOR_MAP.length);
					terrainMap.setRGB(x, y,
							COLOR_MAP[terrainData[x][y]].getRGB());
				}
			}

			// add arctic regions
			for (x = 0; x < TERRAIN_SIZE; ++x) {
				h = (int) (0.3 * TERRAIN_SIZE + 0.01 * TERRAIN_SIZE
						* Math.sin(2 * TAU * x / TERRAIN_SIZE) + 0.01
						* TERRAIN_SIZE * Math.sin(3 * TAU * x / TERRAIN_SIZE) + 0.01
						* TERRAIN_SIZE * Math.sin(4 * TAU * x / TERRAIN_SIZE));

				for (y = 0; y < TERRAIN_SIZE; ++y) {
					if (y > h / 2 && y < TERRAIN_SIZE - h / 2) {
						y = TERRAIN_SIZE - h / 2;
					}

					// make snowy
					if (terrainData[x][y] >= TERRAIN_BEACH) {
						terrainMap
								.setRGB(x,
										y,
										(Math.min(
												255,
												(((terrainMap.getRGB(x, y) >> 16) & 0xFF) + 120)) << 16)
												| (Math.min(
														255,
														(((terrainMap.getRGB(x,
																y) >> 8) & 0xFF) + 120)) << 8)
												| (Math.min(
														255,
														((terrainMap.getRGB(x,
																y) & 0xFF) + 120))));
					}

					// add ice sheet
					if (terrainData[x][y] == TERRAIN_SHALLOWS
							&& (y < h / 3 || y > TERRAIN_SIZE - h / 3)) {
						terrainMap.setRGB(x, y,
								COLOR_MAP[TERRAIN_SNOW].getRGB());
						terrainData[x][y] = TERRAIN_SNOW;
					}

					// add iceberg
					if (terrainData[x][y] < TERRAIN_SHALLOWS
							&& r.nextDouble() < 0.0002) {
						s = (int) (5 + r.nextDouble() * 10);
						v = r.nextDouble() < 0.5 ? 1 : -1;
						for (dx = 0; dx < s; ++dx) {
							for (dy = v * dx; dy < v * dx + s; ++dy) {
								i = (x + dx + TERRAIN_SIZE) % TERRAIN_SIZE;
								j = (y + dy + TERRAIN_SIZE) % TERRAIN_SIZE;
								if (terrainData[i][j] < TERRAIN_SHALLOWS) {
									terrainMap.setRGB(i, j,
											COLOR_MAP[TERRAIN_SNOW].getRGB());
									terrainData[i][j] = TERRAIN_SNOW;
								}
							}
						}
					}
				}
			}

			// initialize items
			for (i = 0; i < NUMBER_OF_ITEMS; ++i) {
				if (items[i] == ITEM_TREASURE) {
					do {
						itemX[i] = (int) (r.nextDouble() * TERRAIN_SIZE);
						itemY[i] = (int) (r.nextDouble() * TERRAIN_SIZE);
					} while (terrainData[itemX[i]][itemY[i]] < TERRAIN_BEACH);
				} else {
					do {
						itemX[i] = (int) (r.nextDouble() * TERRAIN_SIZE);
						itemY[i] = (int) (r.nextDouble() * TERRAIN_SIZE);
					} while (terrainData[itemX[i]][itemY[i]] != TERRAIN_BEACH);
					// show on terrain
					y = itemY[i];
					for (x = itemX[i] - 1; x <= itemX[i] + 1; ++x) {
						x = (x + TERRAIN_SIZE) % TERRAIN_SIZE;
						if (terrainData[x][y] == TERRAIN_BEACH) {
							terrainMap.setRGB(x, y,
									new Color(terrainMap.getRGB(x, y)).darker()
											.getRGB());
						}
					}
					x = itemX[i];
					for (y = itemY[i] - 1; y <= itemY[i] + 1; ++y) {
						y = (y + TERRAIN_SIZE) % TERRAIN_SIZE;
						if (terrainData[x][y] == TERRAIN_BEACH) {
							terrainMap.setRGB(x, y,
									new Color(terrainMap.getRGB(x, y)).darker()
											.getRGB());
						}
					}
				}
			}

			// draw treasure maps
			// first three items are the treasures
			// big red cross tells where the treasure is located
			for (i = 0; i < 3; ++i) {
				treasureMapsGraphics.translate(100, 100 + 200 * i);
				rectangle
						.setBounds(
								(int) (-itemX[i] * TREASURE_MAP_SCALE * TERRAIN_TO_WORLD),
								(int) (-itemY[i] * TREASURE_MAP_SCALE * TERRAIN_TO_WORLD),
								(int) (TERRAIN_SIZE * TREASURE_MAP_SCALE * TERRAIN_TO_WORLD),
								(int) (TERRAIN_SIZE * TREASURE_MAP_SCALE * TERRAIN_TO_WORLD));
				treasureMapsGraphics.setPaint(new TexturePaint(terrainMap,
						rectangle));
				treasureMapsGraphics.fillRect(-100, -100, 200, 200);
				treasureMapsGraphics.rotate(TAU / 8);
				treasureMapsGraphics.setColor(COLOR_CROSS);
				treasureMapsGraphics.fillRect(-20, -2, 40, 4);
				treasureMapsGraphics.fillRect(-2, -20, 4, 40);
				treasureMapsGraphics.setTransform(IDENTITY);
			}

			// /////////////////// //
			// Game Initialization //
			// /////////////////// //
			do {
				r.setSeed(seed);

				// items
				for (i = 0; i < NUMBER_OF_ITEMS; ++i) {
					itemsFound[i] = 0;
				}

				// ship
				do {
					x = (int) (r.nextDouble() * TERRAIN_SIZE);
					y = (int) (r.nextDouble() * TERRAIN_SIZE);
				} while (terrainData[x][y] != TERRAIN_WATER_2);
				objectX[SHIP] = x * TERRAIN_TO_WORLD;
				objectY[SHIP] = y * TERRAIN_TO_WORLD;
				objectType[SHIP] = SHIP_TYPE;
				objectAngle[SHIP] = -TAU / 4;

				// sea monsters
				for (i = SEA_MONSTERS; i < NUMBER_OF_OBJECTS; ++i) {
					objectType[i] = SEA_MONSTER_TYPE;
					do {
						x = (int) (r.nextDouble() * TERRAIN_SIZE);
						y = (int) (r.nextDouble() * TERRAIN_SIZE);
					} while (terrainData[x][y] != TERRAIN_WATER_1);
					objectX[i] = x * TERRAIN_TO_WORLD;
					objectY[i] = y * TERRAIN_TO_WORLD;
					objectDX[i] = 0;
					objectDY[i] = 0;
				}
				seaMonsterGain = SEA_MONSTER_GAIN_MAX;

				// player
				objectType[PLAYER] = PLAYER_TYPE;

				// misc
				worldX = 0;
				worldY = 0;
				worldScaleTarget = ON_SHIP_SCALE;
				worldScaleGain = 1;
				worldScale = ON_SHIP_SCALE / 10;
				playerState = PLAYER_ON_SHIP;

				// redraw terrain image and clear map
				terrainImageGraphics.drawImage(terrainMap, 0, 0, null);
				mapGraphics.setColor(COLOR_BLACK);
				mapGraphics.fillRect(0, 0, TERRAIN_SIZE, TERRAIN_SIZE);

				// used for map
				v = -1;
				w = -1;

				// for AppletViewer only
				if (DEBUG) {
					setSize(800, 600);
					requestFocus();
				}

				// ///////// //
				// Game Loop //
				// ///////// //
				gameState = PLAYING;
				do {
					final long frameStartTime = System.nanoTime();

					// handle restart key
					if (keys[KEY_RESTART]) {
						keys[KEY_RESTART] = false;
						gameState = INITIALIZE_GAME;
					}

					// handle new map key
					if (keys[KEY_NEW_MAP]) {
						keys[KEY_NEW_MAP] = false;
						gameState = GENERATE_TERRAIN;
						seed = 0;
					}

					// handle keys when on ship
					if (playerState == PLAYER_ON_SHIP) {
						// handle ship movement
						if (keys[KEY_UP]) {
							objectDX[SHIP] = 60 * Math.sin(TAU / 4
									+ objectAngle[SHIP]);
							objectDY[SHIP] = 60 * Math.sin(objectAngle[SHIP]);
						}
						if (keys[KEY_DOWN]) {
							objectDX[SHIP] = -30
									* Math.sin(TAU / 4 + objectAngle[SHIP]);
							objectDY[SHIP] = -30 * Math.sin(objectAngle[SHIP]);
						}
						if (keys[KEY_LEFT]) {
							objectAngle[SHIP] -= 0.15;
						}
						if (keys[KEY_RIGHT]) {
							objectAngle[SHIP] += 0.15;
						}

						// place player on ship
						objectX[PLAYER] = objectX[SHIP];
						objectY[PLAYER] = objectY[SHIP];
						objectAngle[PLAYER] = objectAngle[SHIP];

						// update camera position
						worldX = objectX[SHIP];
						worldY = objectY[SHIP];

						// handle action key to go on land
						if (keys[KEY_ACTION]) {
							keys[KEY_ACTION] = false;
							// check terrain in front of ship
							ox = (objectX[SHIP] + SHIP_LENGTH / 2
									* Math.sin(TAU / 4 + objectAngle[SHIP]));
							oy = (objectY[SHIP] + SHIP_LENGTH / 2
									* Math.sin(objectAngle[SHIP]));
							x = (int) (((ox + WORLD_SIZE) % WORLD_SIZE) / TERRAIN_TO_WORLD);
							y = (int) (((oy + WORLD_SIZE) % WORLD_SIZE) / TERRAIN_TO_WORLD);
							if (terrainData[x][y] >= TERRAIN_BEACH) {
								objectX[PLAYER] = ox;
								objectY[PLAYER] = oy;
								playerState = PLAYER_ON_LAND;
								worldScaleTarget = ON_LAND_SCALE
										* worldScaleGain;
							}
						}
					}

					// handle keys when on land
					if (playerState == PLAYER_ON_LAND) {
						// handle action key to board ship or to dig
						if (keys[KEY_ACTION]) {
							keys[KEY_ACTION] = false;
							// board ship if close
							if ((objectX[PLAYER] - objectX[SHIP])
									* (objectX[PLAYER] - objectX[SHIP])
									+ (objectY[PLAYER] - objectY[SHIP])
									* (objectY[PLAYER] - objectY[SHIP]) <= (SHIP_LENGTH / 1.8)
									* (SHIP_LENGTH / 1.8)) {
								playerState = PLAYER_ON_SHIP;
								worldScaleTarget = ON_SHIP_SCALE
										* worldScaleGain;
							}
							if (playerState == PLAYER_ON_LAND) {
								// dig for items, show on terrain
								for (dx = 0; dx <= 1; ++dx) {
									for (dy = 0; dy <= 1; ++dy) {
										x = (int) (((objectX[PLAYER]
												+ (dx - 0.5) * TERRAIN_TO_WORLD + WORLD_SIZE) % WORLD_SIZE) / TERRAIN_TO_WORLD);
										y = (int) (((objectY[PLAYER]
												+ (dy - 0.5) * TERRAIN_TO_WORLD + WORLD_SIZE) % WORLD_SIZE) / TERRAIN_TO_WORLD);
										terrainImage.setRGB(x, y, new Color(
												terrainImage.getRGB(x, y))
												.darker().getRGB());
										for (i = 0; i < NUMBER_OF_ITEMS; ++i) {
											if (itemsFound[i] == 0
													&& itemX[i] == x
													&& itemY[i] == y) {
												itemsFound[i] = 1;

												if (items[i] == ITEM_LOOKOUT) {
													worldScaleGain *= LOOKOUT_SCALE_MULTIPLYER;
													worldScaleTarget *= LOOKOUT_SCALE_MULTIPLYER;
												}
												if (items[i] == ITEM_SEA_MONSTER_REPEL) {
													seaMonsterGain -= SEA_MONSTER_GAIN_REDUCTION;
												}
												if (items[i] == ITEM_MAP_NW) {
													mapGraphics.drawImage(
															terrainMap, 0, 0,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															0, 0,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															null);
												}
												if (items[i] == ITEM_MAP_NE) {
													mapGraphics.drawImage(
															terrainMap,
															TERRAIN_SIZE / 2,
															0, TERRAIN_SIZE,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															0, TERRAIN_SIZE,
															TERRAIN_SIZE / 2,
															null);
												}
												if (items[i] == ITEM_MAP_SW) {
													mapGraphics.drawImage(
															terrainMap, 0,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE, 0,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE, null);
												}
												if (items[i] == ITEM_MAP_SE) {
													mapGraphics.drawImage(
															terrainMap,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE,
															TERRAIN_SIZE,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE / 2,
															TERRAIN_SIZE,
															TERRAIN_SIZE, null);
												}
											}
										}
									}
								}

								// check if all treasures are found
								if (itemsFound[0] == 1 && itemsFound[1] == 1
										&& itemsFound[2] == 1) {
									playerState = PLAYER_RICH;
									worldScaleTarget = FINISHED_SCALE;
								}
							}
						}

						// handle player movement
						if (keys[KEY_UP]) {
							objectDX[PLAYER] = 10 * Math.sin(TAU / 4
									+ objectAngle[PLAYER]);
							objectDY[PLAYER] = 10 * Math
									.sin(objectAngle[PLAYER]);
						}
						if (keys[KEY_DOWN]) {
							objectDX[PLAYER] = -5
									* Math.sin(TAU / 4 + objectAngle[PLAYER]);
							objectDY[PLAYER] = -5
									* Math.sin(objectAngle[PLAYER]);
						}
						if (keys[KEY_LEFT]) {
							objectAngle[PLAYER] -= 0.2;
						}
						if (keys[KEY_RIGHT]) {
							objectAngle[PLAYER] += 0.2;
						}

						// update camera position
						worldX = objectX[PLAYER];
						worldY = objectY[PLAYER];
					}

					// update camera scale
					worldScale += (worldScaleTarget - worldScale) * 0.2;

					// update map
					if (v != (int) (worldX / TERRAIN_TO_WORLD)
							|| w != (int) (worldY / TERRAIN_TO_WORLD)) {
						v = (int) (worldX / TERRAIN_TO_WORLD);
						w = (int) (worldY / TERRAIN_TO_WORLD);
						for (x = (int) ((worldX - 300 / worldScaleTarget) / TERRAIN_TO_WORLD); x < (int) ((worldX + 300 / worldScaleTarget) / TERRAIN_TO_WORLD); ++x) {
							for (y = (int) ((worldY - 300 / worldScaleTarget) / TERRAIN_TO_WORLD); y < (int) ((worldY + 300 / worldScaleTarget) / TERRAIN_TO_WORLD); ++y) {
								map.setRGB(
										(x % TERRAIN_SIZE + TERRAIN_SIZE)
												% TERRAIN_SIZE,
										(y % TERRAIN_SIZE + TERRAIN_SIZE)
												% TERRAIN_SIZE,
										terrainMap
												.getRGB((x % TERRAIN_SIZE + TERRAIN_SIZE)
														% TERRAIN_SIZE,
														(y % TERRAIN_SIZE + TERRAIN_SIZE)
																% TERRAIN_SIZE));
							}
						}
					}

					// draw terrain
					screenGraphics.translate(500, 300);
					rectangle
							.setBounds(
									(int) (-worldX * worldScale),
									(int) (-worldY * worldScale),
									(int) (TERRAIN_SIZE * worldScale * TERRAIN_TO_WORLD),
									(int) (TERRAIN_SIZE * worldScale * TERRAIN_TO_WORLD));
					screenGraphics.setPaint(new TexturePaint(terrainImage,
							rectangle));
					screenGraphics.fillRect(-300, -300, 600, 600);

					// update and draw objects
					screenGraphics.scale(worldScale, worldScale);
					screenGraphics.translate(-worldX, -worldY);
					transform = screenGraphics.getTransform();
					for (i = NUMBER_OF_OBJECTS - 1; i >= 0; --i) {
						// get next object position
						ox = objectX[i] + objectDX[i];
						oy = objectY[i] + objectDY[i];
						while (ox - worldX < -WORLD_SIZE / 2) {
							ox += WORLD_SIZE;
						}
						while (oy - worldY < -WORLD_SIZE / 2) {
							oy += WORLD_SIZE;
						}
						while (ox - worldX > WORLD_SIZE / 2) {
							ox -= WORLD_SIZE;
						}
						while (oy - worldY > WORLD_SIZE / 2) {
							oy -= WORLD_SIZE;
						}
						x = (int) (((ox % WORLD_SIZE + WORLD_SIZE) % WORLD_SIZE) / TERRAIN_TO_WORLD);
						y = (int) (((oy % WORLD_SIZE + WORLD_SIZE) % WORLD_SIZE) / TERRAIN_TO_WORLD);

						// handle collisions, update, draw
						if (objectType[i] == PLAYER_TYPE) {
							// update player
							// player can only move on land
							if (terrainData[x][y] >= TERRAIN_BEACH) {
								objectX[i] = ox;
								objectY[i] = oy;
							}
							objectDX[i] = 0;
							objectDY[i] = 0;

							// draw player
							screenGraphics.translate(objectX[i], objectY[i]);
							screenGraphics.rotate(objectAngle[i]);
							screenGraphics.setColor(COLOR_PLAYER_BODY);
							screenGraphics.fillRect(-PLAYER_LENGTH / 2,
									-PLAYER_WIDTH / 2, PLAYER_LENGTH,
									PLAYER_WIDTH);
							screenGraphics.setColor(COLOR_PLAYER_HEAD);
							screenGraphics.fillRect(-PLAYER_LENGTH / 2,
									-PLAYER_WIDTH / 6, PLAYER_LENGTH,
									PLAYER_WIDTH / 3);
						}
						if (objectType[i] == SHIP_TYPE) {
							// update ship
							// ship can only move in water
							if (terrainData[x][y] < TERRAIN_BEACH) {
								objectX[i] = ox;
								objectY[i] = oy;
							}
							objectDX[i] = 0;
							objectDY[i] = 0;

							// draw ship
							screenGraphics.translate(objectX[i], objectY[i]);
							screenGraphics.rotate(objectAngle[i]);
							screenGraphics.setColor(COLOR_SHIP_OUTER);
							screenGraphics.fillRect(-SHIP_LENGTH / 2,
									-SHIP_WIDTH / 2, SHIP_LENGTH, SHIP_WIDTH);
							screenGraphics.setColor(COLOR_SHIP_INNER);
							screenGraphics.fillRect(-SHIP_INNER_LENGTH / 2,
									-SHIP_INNER_WIDTH / 2, SHIP_INNER_LENGTH,
									SHIP_INNER_WIDTH);
						}
						if (objectType[i] == SEA_MONSTER_TYPE) {
							// update sea monster
							// sea monsters can only move in deep water
							if (terrainData[x][y] == TERRAIN_WATER_1) {
								objectX[i] = ox;
								objectY[i] = oy;
							} else {
								objectDX[i] = seaMonsterGain
										* (-SEA_MONSTER_SPEED + r.nextDouble()
												* 2 * SEA_MONSTER_SPEED);
								objectDY[i] = seaMonsterGain
										* (-SEA_MONSTER_SPEED + r.nextDouble()
												* 2 * SEA_MONSTER_SPEED);
							}
							odx = objectX[PLAYER] - ox;
							ody = objectY[PLAYER] - oy;
							if (odx * odx + ody * ody < (SEA_MONSTER_SIZE / 2)
									* (SEA_MONSTER_SIZE / 2)) {
								playerState = PLAYER_DEAD;
							}
							if (odx * odx + ody * ody < 10000 * 10000
									&& r.nextDouble() < 0.02) {
								objectDX[i] = seaMonsterGain
										* (odx % SEA_MONSTER_SPEED);
								objectDY[i] = seaMonsterGain
										* (ody % SEA_MONSTER_SPEED);
								objectAngle[i] += r.nextDouble() - 0.5;
							}

							// draw sea monster
							screenGraphics.translate(objectX[i], objectY[i]);
							screenGraphics.rotate(objectAngle[i]);
							screenGraphics.setColor(COLOR_SEA_MONSTER);
							screenGraphics.fillRect(-SEA_MONSTER_SIZE / 2,
									-SEA_MONSTER_SIZE / 2, SEA_MONSTER_SIZE,
									SEA_MONSTER_SIZE);
							screenGraphics.fillRect(-SEA_MONSTER_SIZE / 4,
									-SEA_MONSTER_SIZE / 4,
									SEA_MONSTER_SIZE / 2, SEA_MONSTER_SIZE / 2);
							screenGraphics.fillRect(-SEA_MONSTER_SIZE / 8,
									-SEA_MONSTER_SIZE / 8,
									SEA_MONSTER_SIZE / 4, SEA_MONSTER_SIZE / 4);
						}

						screenGraphics.setTransform(transform);
					}
					screenGraphics.setTransform(IDENTITY);

					// draw treasure maps
					screenGraphics.drawImage(treasureMaps, 0, 0, null);

					// draw items
					for (i = 0; i < NUMBER_OF_ITEMS; ++i) {
						if (itemsFound[i] == 1) {
							// draw big yellow circle when treasure found
							if (items[i] == ITEM_TREASURE) {
								screenGraphics.setColor(COLOR_TREASURE_FOUND);
								screenGraphics.fillOval(50, 50 + i * 200, 100,
										100);
							}
							// draw found items at the bottom of the view area
							x = 80 + 50 * i;
							y = 570;
							if (items[i] == ITEM_SEA_MONSTER_REPEL) {
								screenGraphics
										.setColor(COLOR_SEA_MONSTER_REPEL_ITEM);
								screenGraphics.fillRect(x - 20, y - 20, 40, 40);
								screenGraphics.fillRect(x - 10, y - 10, 20, 20);
								screenGraphics.fillRect(x - 5, y - 5, 10, 10);
							}
							if (items[i] == ITEM_MAP_NW) {
								screenGraphics
										.setColor(COLOR_MAP_BACKGROUND_ITEM);
								screenGraphics.fillRect(x - 20, y - 20, 40, 40);
								screenGraphics
										.setColor(COLOR_MAP_AND_LOOKOUT_ITEM);
								screenGraphics.fillRect(x - 20, y - 20, 20, 20);
							}
							if (items[i] == ITEM_MAP_NE) {
								screenGraphics
										.setColor(COLOR_MAP_BACKGROUND_ITEM);
								screenGraphics.fillRect(x - 20, y - 20, 40, 40);
								screenGraphics
										.setColor(COLOR_MAP_AND_LOOKOUT_ITEM);
								screenGraphics.fillRect(x, y - 20, 20, 20);
							}
							if (items[i] == ITEM_MAP_SW) {
								screenGraphics
										.setColor(COLOR_MAP_BACKGROUND_ITEM);
								screenGraphics.fillRect(x - 20, y - 20, 40, 40);
								screenGraphics
										.setColor(COLOR_MAP_AND_LOOKOUT_ITEM);
								screenGraphics.fillRect(x - 20, y, 20, 20);
							}
							if (items[i] == ITEM_MAP_SE) {
								screenGraphics
										.setColor(COLOR_MAP_BACKGROUND_ITEM);
								screenGraphics.fillRect(x - 20, y - 20, 40, 40);
								screenGraphics
										.setColor(COLOR_MAP_AND_LOOKOUT_ITEM);
								screenGraphics.fillRect(x, y, 20, 20);
							}
							if (items[i] == ITEM_LOOKOUT) {
								screenGraphics
										.setColor(COLOR_MAP_AND_LOOKOUT_ITEM);
								screenGraphics.fillOval(x - 20, y - 20, 40, 40);
								screenGraphics
										.setColor(COLOR_MAP_AND_LOOKOUT_ITEM);
								screenGraphics.fillOval(x - 15, y - 15, 30, 30);
							}
						}
					}

					// restore sea monster gain
					seaMonsterGain = Math.min(seaMonsterGain
							+ SEA_MONSTER_GAIN_INCREMENT, SEA_MONSTER_GAIN_MAX);

					// show big black dot when dead
					if (playerState == PLAYER_DEAD) {
						screenGraphics.setColor(COLOR_BLACK);
						screenGraphics.fillOval(350, 150, 300, 300);
					}

					// spin around when all treasures are found
					if (playerState == PLAYER_RICH) {
						objectAngle[PLAYER] += 0.5;
					}

					// draw map
					if (keys[KEY_MAP]) {
						rectangle.setBounds(200, 0, 600, 600);
						screenGraphics
								.setPaint(new TexturePaint(map, rectangle));
						screenGraphics.fillRect(200, 0, 600, 600);
					}

					if (DEBUG) {
						if (keys['1']) {
							screenGraphics.drawImage(heightMapBase, 200, 0,
									600, 600, COLOR_BLACK, null);
						}
						if (keys['2']) {
							screenGraphics.drawImage(heightMap, 200, 0, 600,
									600, COLOR_BLACK, null);
						}
						if (keys['3']) {
							screenGraphics.drawImage(terrainMap, 200, 0, 600,
									600, COLOR_BLACK, null);
						}
						if (keys['4']) {
							screenGraphics.drawImage(terrainImage, 200, 0, 600,
									600, COLOR_BLACK, null);
						}
						final int[] KEY_ITEMS = { 'q', 'w', 'e', 'r', 't', 'y',
								'u', 'i', 'o', 'p', '[' };
						for (i = 0; i < NUMBER_OF_ITEMS; ++i) {
							if (keys[KEY_ITEMS[i]]) {
								keys[KEY_ITEMS[i]] = false;
								itemsFound[i] = itemsFound[i] == 0 ? 1 : 0;
							}
						}
						if (keys['a']) {
							keys['a'] = false;
							worldScaleGain *= LOOKOUT_SCALE_MULTIPLYER;
							worldScaleTarget *= LOOKOUT_SCALE_MULTIPLYER;
							System.out.println("worldScaleGain: "
									+ worldScaleGain);
						}
						if (keys['s']) {
							keys['s'] = false;
							worldScaleGain /= LOOKOUT_SCALE_MULTIPLYER;
							worldScaleTarget /= LOOKOUT_SCALE_MULTIPLYER;
							System.out.println("worldScaleGain: "
									+ worldScaleGain);
						}
						if (keys['d']) {
							keys['d'] = false;
							seaMonsterGain -= SEA_MONSTER_GAIN_REDUCTION;
							System.out.println("seaMonsterGain: "
									+ seaMonsterGain);
						}
						if (keys['f']) {
							keys['f'] = false;
							seaMonsterGain += SEA_MONSTER_GAIN_REDUCTION;
							System.out.println("seaMonsterGain: "
									+ seaMonsterGain);
						}
						if (keys['h']) {
							keys['h'] = false;
							mapGraphics.drawImage(terrainMap, 0, 0,
									TERRAIN_SIZE / 2, TERRAIN_SIZE / 2, 0, 0,
									TERRAIN_SIZE / 2, TERRAIN_SIZE / 2, null);
						}
						if (keys['j']) {
							keys['j'] = false;
							mapGraphics.drawImage(terrainMap, TERRAIN_SIZE / 2,
									0, TERRAIN_SIZE, TERRAIN_SIZE / 2,
									TERRAIN_SIZE / 2, 0, TERRAIN_SIZE,
									TERRAIN_SIZE / 2, null);
						}
						if (keys['k']) {
							keys['k'] = false;
							mapGraphics.drawImage(terrainMap, 0,
									TERRAIN_SIZE / 2, TERRAIN_SIZE / 2,
									TERRAIN_SIZE, 0, TERRAIN_SIZE / 2,
									TERRAIN_SIZE / 2, TERRAIN_SIZE, null);
						}
						if (keys['l']) {
							keys['l'] = false;
							mapGraphics.drawImage(terrainMap, TERRAIN_SIZE / 2,
									TERRAIN_SIZE / 2, TERRAIN_SIZE,
									TERRAIN_SIZE, TERRAIN_SIZE / 2,
									TERRAIN_SIZE / 2, TERRAIN_SIZE,
									TERRAIN_SIZE, null);
						}
					}

					// draw seed
					if (DEBUG) {
						screenGraphics.setColor(new Color(200, 0, 0));
						screenGraphics.drawString("url: " + getDocumentBase(),
								210, 20);
						screenGraphics.drawString("seed: " + seed, 210, 40);
					}

					// show fps
					if (DEBUG) {
						screenGraphics.setColor(new Color(200, 0, 0));
						screenGraphics.drawString("fps: " + fps, 10, 20);
					}

					// draw the screen on the applet
					appletGraphics.drawImage(screen, 0, 0, null);

					// lock the frame rate ~25Hz
					if (LOCK_FRAME_RATE) {
						final long frameTime = System.nanoTime()
								- frameStartTime;
						if (frameTime < 40000000L) {
							try {
								Thread.sleep((40000000L - frameTime) / 1000000L);
							} catch (final Exception e) {
							}
						}
					}

					// update fps
					if (DEBUG) {
						final long frameTime = System.nanoTime()
								- frameStartTime;
						fpsAccumulatedTime += frameTime;
						++fpsFrameCount;
						if (fpsAccumulatedTime > 1000000000L) {
							fps = 1000000000L * fpsFrameCount
									/ fpsAccumulatedTime;
							fpsAccumulatedTime = 0;
							fpsFrameCount = 0;
						}
					}
				} while (isActive() && gameState == PLAYING);
			} while (gameState == INITIALIZE_GAME);
		} while (gameState == GENERATE_TERRAIN);
	}

	@Override
	public void start() {
		new Thread(this).start();
	}
}
