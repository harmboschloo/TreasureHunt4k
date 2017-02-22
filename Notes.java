// Treasure Hunt 4k, Harm Boschloo 2013/2014
// Big map with lost of sea and islands.
// Ship, captain.
// Treasures are hidden on islands.
// Treasure maps are 'screenshots' of the location with an big X
// Two modes of travel: 
// - ship on sea, camera is zoom out
// - player on land, camera is zoom in
// Treasure map zoom >> on sea, << on land, so that the location is not easily found.
// Treasure maps are given at the start of the game
// World is generated randomly at the start of the game
// Actions: move ship/player, get in/out ship, dig for treasure, view treasure maps.
// Find all in x time?
// Move by mouse or keys?
// Sea monsters, start with a few, when they cross each other, they multiply

// TODO compass, (partial) maps, find random things on icebergs, trees, rocks, buildings...
// TODO check http://sourceforge.net/projects/jarg/

// http://www.ahristov.com/tutorial/java4k-tips/java4k-tips.html

// TODO feedback Gef
//Hi, I also find it's a good game idea.
//
//After having retried many times, here is what I can say, based on my really poor gamer's experience :
//- the boat responds well to controls and has good speed
//- the human is really too slow for me
//- deep water is dangerous, so agree with you to not have a break there Cheesy
//- perhaps make your icebergs a bit larger so that can explain they block the boat
//- I spent too much time in searching a first island (and others too)
//- I think a bigger view area around the player, only on the map, would help a bit for finding island
//- when I found a place, digging was funny
//
//Voila !

// TODO check http://www.java-gaming.org/topics/in-the-dark-4k/31671/msg/295713/view.html#msg295713

// Applet templates: http://www.java-gaming.org/topics/applet-templates/21626/view.html
// ProGuard : http://proguard.sourceforge.net/
// More on compression: http://blog.slackers.se/2011/01/java-4k-competition-2011.html
// and more: http://neophob.com/2008/08/optimize-java-code-for-a-4k-compo/

// sample sources
// http://www.java4k.com/index.php?action=games&method=view&gid=394#source (with sound)
// http://www.java4k.com/index.php?action=games&method=view&gid=451#source 

// terrainGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
// RenderingHints.VALUE_ANTIALIAS_OFF);

// terrainGraphics.setColor(new Color(0, 0, 21));
// terrainGraphics.fillRect(0, 0, TERRAIN_SIZE, TERRAIN_SIZE);

// base height map using grouped ovals
// for (i = 0; i < 30; ++i) {
// x = (int) (TERRAIN_SIZE * Math.random()) - TERRAIN_SIZE / 4;
// y = (int) (TERRAIN_SIZE * Math.random()) - TERRAIN_SIZE / 4;
// scale = (0.5 + Math.random() / 2) * TERRAIN_SIZE / 600.0;
// for (i2 = 0; i2 < 10; ++i2) {
// x2 = x + (int) (-25 + 50 * Math.random());
// y2 = y + (int) (-25 + 50 * Math.random());
// w = 40 + (int) (2 * Math.random());
// h = 40 + (int) (100 * Math.random());
// angle = 6.2832 * Math.random();
// v = (int) (255 * Math.random());
// terrainGraphics.setColor(new Color(0, 0, v, v));
// terrainGraphics.setTransform(new AffineTransform());
// terrainGraphics.translate(x2, y2);
// terrainGraphics.rotate(angle);
// terrainGraphics.scale(scale, scale);
// terrainGraphics.fillOval(-w / 2 , -h / 2 , w
// , h );
// terrainGraphics.scale(0.5, 0.5);
// terrainGraphics.rotate(Math.random() / 10);
// terrainGraphics.fillOval(-w / 2, -h / 2, w, h);
// terrainGraphics.scale(0.5, 0.5);
// terrainGraphics.rotate(Math.random() / 10);
// terrainGraphics.fillOval(-w / 2, -h / 2, w, h);
// }
// }

// scale up and blur
// this doesn't take wrapping into account
// BufferedImage terrain2 = new BufferedImage(TERRAIN_SIZE,
// TERRAIN_SIZE,
// BufferedImage.TYPE_INT_ARGB);
// Graphics2D terrain2Graphics = terrain.createGraphics();
// AffineTransform at = new AffineTransform();
// at.scale(2.0, 2.0);
// BufferedImageOp scaleOp = new AffineTransformOp(at,
// AffineTransformOp.TYPE_BICUBIC);
// float[] matrix = { 0.111f, 0.111f, 0.111f, 0.111f, 0.111f, 0.111f,
// 0.111f, 0.111f, 0.111f, };
// BufferedImageOp smoothOp = new ConvolveOp(new Kernel(3, 3, matrix));
// for (i = 0; i < TERRAIN_SCALE_FACTOR; ++i) {
// for (i2 = 0; i2 < 9; ++i2) {
// matrix[i2] = (float) (0.05 + Math.random() * 0.5);
// }
// terrain2 = scaleOp.filter(terrain, terrain2);
// terrain = smoothOp.filter(terrain2, terrain);
// terrainSize *= 2;
// System.out.println("terrain size: " + terrainSize + "; "
// + terrain.getWidth());
// }

// FIXM debug
// System.out.println("terrain base size: " + TERRAIN_BASE_SIZE
// + "; terrain size: " + TERRAIN_SIZE);
// System.out.println("TERRAIN_SCALE_FACTOR: " + TERRAIN_SCALE_FACTOR
// + "; TERRAIN_TO_WORLD: " + TERRAIN_TO_WORLD);

// final Color[] ARCTIC_COLOR_MAP = { new Color(50, 50, 100),
// new Color(100, 100, 200), new Color(150, 150, 255),
// new Color(200, 200, 255), new Color(255, 255, 150),
// new Color(150, 255, 150), new Color(150, 230, 150),
// new Color(150, 200, 150), new Color(200, 100, 50),
// new Color(150, 150, 150), new Color(200, 200, 200),
// new Color(230, 230, 230), new Color(250, 250, 250),
// new Color(255, 255, 255) };

// FIXM debug draw object terrain position
// screenGraphics.setColor(Color.MAGENTA);
// screenGraphics.drawRect((int) (x * TERRAIN_TO_WORLD),
// (int) (y * TERRAIN_TO_WORLD), (int) TERRAIN_TO_WORLD,
// (int) TERRAIN_TO_WORLD);

// draw map
//if (keys[KEY_MAP]) {
// screenGraphics.setPaint(new TexturePaint(map, new
// Rectangle(0,
// 0, 600, 600)));
// screenGraphics.fillRect(200, 0, 600, 600);

//				screenGraphics.translate(500, 300);
//				screenGraphics
//						.setPaint(new TexturePaint(
//								map,
//								new Rectangle(
//										(int) (-worldX * 600 / TERRAIN_SIZE / TERRAIN_TO_WORLD),
//										(int) (-worldY * 600 / TERRAIN_SIZE / TERRAIN_TO_WORLD),
//										600, 600)));
//				screenGraphics.fillRect(-300, -300, 600, 600);
//				screenGraphics.setTransform(new AffineTransform());

// screenGraphics
// .setPaint(new TexturePaint(
// map,
// new Rectangle(
// (int) (-worldX * 600 / TERRAIN_SIZE / TERRAIN_TO_WORLD) +
// 500,
// (int) (-worldY * 600 / TERRAIN_SIZE / TERRAIN_TO_WORLD) +
// 300,
// 600, 600)));
// screenGraphics.fillRect(200, 0, 600, 600);

// screenGraphics.drawImage(map, 200, 0, 600, 600, null);
// }

// FIXM debug
// screenGraphics.setColor(Color.WHITE);
// screenGraphics.drawString("URL: " + getDocumentBase(), 210, 40);
// if (getDocumentBase() != null) {
// screenGraphics.drawString("URL ref: "
// + getDocumentBase().getRef(), 210, 60);
// }

// FIXM debug
// System.out.println("frame rate: "
// + (1000000000L/(System.nanoTime() - lastTime)));

// determine region types
// if (DEBUG) {
// System.out.println((System.nanoTime() / 1000000000.0)
// + " determine region types");
// }
// for (i = 0; i < REGION_MAP_SIZE; ++i) {
// for (j = 0; j < REGION_MAP_SIZE; ++j) {
// for (x = i * REGION_SIZE; x < (i + 1) * REGION_SIZE; ++x) {
// for (y = j * REGION_SIZE; y < (j + 1) * REGION_SIZE; ++y) {
// v = terrainData[x][y];
// if (v == TERRAIN_WATER_1) {
// terrainTypeCount[REGION_SEA] += 1;
// }
// if (v >= TERRAIN_WATER_2 && v <= TERRAIN_SHALLOWS) {
// terrainTypeCount[REGION_WATER] += 1;
// }
// if (v == TERRAIN_BEACH) {
// terrainTypeCount[REGION_BEACH] += 1;
// }
// if (v >= TERRAIN_GRASS_1 && v <= TERRAIN_DIRT) {
// terrainTypeCount[REGION_LAND] += 1;
// }
// if (v >= TERRAIN_ROCK_1) {
// terrainTypeCount[REGION_MOUNTAIN] += 1;
// }
// }
// }
//
// // evaluate terrain type counts
// s = 0;
// for (v = 1; v < REGION_TYPES; ++v) {
// if (terrainTypeCount[v] > terrainTypeCount[s]) {
// s = v;
// }
// }
// regionData[i][j] = s;
// if ((s == REGION_SEA && terrainTypeCount[REGION_LAND] >
// terrainTypeCount[REGION_SEA] / 2)
// || (s == REGION_LAND && terrainTypeCount[REGION_SEA] >
// terrainTypeCount[REGION_LAND] / 2)) {
// regionData[i][j] = REGION_CITY;
//
// for (dx = -CITY_INFLUENCE; dx <= CITY_INFLUENCE; ++dx) {
// for (dy = -CITY_INFLUENCE; dy <= CITY_INFLUENCE; ++dy) {
// x = ((i + dx) % REGION_MAP_SIZE + REGION_MAP_SIZE)
// % REGION_MAP_SIZE;
// y = ((j + dy) % REGION_MAP_SIZE + REGION_MAP_SIZE)
// % REGION_MAP_SIZE;
// v = -Math.min(-dx, dx);
// w = -Math.min(-dy, dy);
// h = -Math.min(-v, -w);
// h = CITY_INFLUENCE - h + 1;
// cityInfluence[x][y] += h * h;
// }
// }
// }
//
// // reset tile count
// for (v = 0; v < REGION_TYPES; ++v) {
// terrainTypeCount[v] = 0;
// }
// }
// }

// region map and city influence map (debug)
// if (DEBUG) {
// System.out.println((System.nanoTime() / 1000000000.0)
// + " region and influence map");
// for (i = 0; i < REGION_MAP_SIZE; ++i) {
// for (j = 0; j < REGION_MAP_SIZE; ++j) {
// switch (regionData[i][j]) {
// case REGION_SEA:
// regionMap.setRGB(i, j, 0xFF0000AA);
// break;
// case REGION_WATER:
// regionMap.setRGB(i, j, 0xFF0000FF);
// break;
// case REGION_BEACH:
// regionMap.setRGB(i, j, 0xFFFFFF00);
// break;
// case REGION_LAND:
// regionMap.setRGB(i, j, 0xFF00FF00);
// break;
// case REGION_MOUNTAIN:
// regionMap.setRGB(i, j, 0xFFAAAAAA);
// break;
// case REGION_CITY:
// regionMap.setRGB(i, j, 0xFFFFFFFF);
// break;
// }
//
// v = Math.min(32 * cityInfluence[i][j], 255);
// cityInfluenceMap.setRGB(i, j,
// ((v << 16) | (v << 8) | v));
// }
// }
// }

// add cities
// if (DEBUG) {
// System.out.println((System.nanoTime() / 1000000000.0)
// + " add cities");
// }
// for (i = 0; i < REGION_MAP_SIZE; ++i) {
// for (j = 0; j < REGION_MAP_SIZE; ++j) {
// s = cityInfluence[i][j];
// if (s > 0) {
// for (x = i * REGION_SIZE; x < (i + 1) * REGION_SIZE; x +=
// (HOUSE_SIZE + HOUSE_SPACING)) {
// for (y = j * REGION_SIZE; y < (j + 1) * REGION_SIZE; y +=
// (HOUSE_SIZE + HOUSE_SPACING)) {
// if (r.nextDouble() / s < 0.05
// && terrainData[x][y] >= TERRAIN_GRASS_1
// && terrainData[x][y] <= TERRAIN_DIRT) {
// for (dx = -HOUSE_SIZE / 2; dx < HOUSE_SIZE / 2; ++dx) {
// for (dy = -HOUSE_SIZE / 2; dy < HOUSE_SIZE / 2; ++dy) {
// v = ((x + dx) % TERRAIN_SIZE + TERRAIN_SIZE)
// % TERRAIN_SIZE;
// w = ((y + dy) % TERRAIN_SIZE + TERRAIN_SIZE)
// % TERRAIN_SIZE;
// h = (int) (r.nextDouble() * 20);
// terrainMap.setRGB(v, w,
// ((210 + h) << 16)
// | ((100 + h) << 8)
// | (80 + h));
// terrainData[v][w] = TERRAIN_BUILDING;
// }
// }
// }
// }
// }
// }
// }
// }

//	private static void tic(String message) {
//		final long time = System.nanoTime();
//		if (message != null) {
//			System.out.println((time - ticTime) / 1000000000.0 + " " + message);
//		}
//		ticTime = time;
//	}