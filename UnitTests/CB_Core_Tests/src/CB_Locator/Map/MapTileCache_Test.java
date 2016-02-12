package CB_Locator.Map;

import junit.framework.TestCase;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.Batch;

public class MapTileCache_Test extends TestCase {

	private final MapTileCache cache = new MapTileCache((short) 5);
	private final long Hash1 = 1;
	private final long Hash2 = 2;
	private final long Hash3 = 3;
	private final long Hash4 = 4;
	private final long Hash5 = 5;
	private final long Hash6 = 6;
	private final long Hash7 = 7;

	public void test() {

		InstanceCount = 0;

		add1SortAndClear();

		createAndFill();
		checkInerhitedTiles(new long[] { Hash1, Hash2, Hash3, Hash4, Hash5 });
		chekMark325();
		changeAge();
		sort();
		chekMark325();
		add2newTiles();
		checkInerhitedTiles(new long[] { Hash6, Hash7, Hash3, Hash4, Hash5 });
		chekMark3657();

		assertFalse("Cache Size must be 5", cache.size() != 5);
	}

	private void add1SortAndClear() {
		cache.clear();
		assertFalse("InstanceCount must be 0", InstanceCount != 0);

		TileGL t1 = new DummyTile(Hash6);
		cache.add(Hash6, t1);
		assertFalse("InstanceCount must be 1", InstanceCount != 1);
		checkInerhitedTiles(new long[] { Hash6 });

		cache.sort();
		cache.clear();
	}

	private void add2newTiles() {
		TileGL t1 = new DummyTile(Hash6);
		cache.add(Hash6, t1);
		TileGL t2 = new DummyTile(Hash7);
		cache.add(Hash7, t2);

		// chek holded Tiles
		int[] targetSortList = new int[] { 7, 6, 4, 5, 3 };

		for (int i = 0, n = cache.size(); i < n; i++) {
			assertEquals(cache.get(i).toString(), "Tile:" + String.valueOf(targetSortList[i]));
		}

		// check TileInstance == 5 ,Tiles 1,2 must be disposed
		assertFalse("InstanceCount must be 5", InstanceCount != 5);

	}

	private void checkInerhitedTiles(long[] list) {
		for (int i = 0, n = list.length; i < n; i++) {
			assertTrue("Tile must be included", cache.containsKey(list[i]));
		}
	}

	private void chekMark3657() {
		// mark Tile 3,2,5 to draw
		cache.markToDraw(Hash3);
		cache.markToDraw(Hash6);
		cache.markToDraw(Hash5);
		cache.markToDraw(Hash7);

		int[] targetDrawList = new int[] { 3, 6, 5, 7 };
		for (int i = 0, n = cache.DrawingSize(); i < n; i++) {
			assertEquals(cache.getDrawingTile(i).toString(), "Tile:" + String.valueOf(targetDrawList[i]));
		}

		cache.clearDrawingList();

		for (int i = 0, n = cache.DrawingSize(); i < n; i++) {
			assertFalse("Cache drawingTiles must be clear", true);
		}
	}

	private void chekMark325() {
		// mark Tile 3,2,5 to draw
		cache.markToDraw(Hash3);
		cache.markToDraw(Hash2);
		cache.markToDraw(Hash5);

		int[] targetDrawList = new int[] { 3, 2, 5 };
		for (int i = 0, n = cache.DrawingSize(); i < n; i++) {
			assertEquals(cache.getDrawingTile(i).toString(), "Tile:" + String.valueOf(targetDrawList[i]));
		}

		cache.clearDrawingList();

		for (int i = 0, n = cache.DrawingSize(); i < n; i++) {
			assertFalse("Cache drawingTiles must be clear", true);
		}
	}

	private void createAndFill() {
		cache.clear();

		// fill with 5 Tiles
		{
			TileGL t1 = new DummyTile(Hash1);
			cache.add(Hash1, t1);
			TileGL t2 = new DummyTile(Hash2);
			cache.add(Hash2, t2);
			TileGL t3 = new DummyTile(Hash3);
			cache.add(Hash3, t3);
			TileGL t4 = new DummyTile(Hash4);
			cache.add(Hash4, t4);
			TileGL t5 = new DummyTile(Hash5);
			cache.add(Hash5, t5);
		}

		assertFalse("Cache Size must be 5", cache.size() != 5);
		assertFalse("InstanceCount must be 5", InstanceCount != 5);

	}

	private void changeAge() {
		cache.increaseLoadedTilesAge();
		for (int i = 0, n = cache.size(); i < n; i++) {
			assertFalse("All Tile.Age must be 1", cache.get(i).Age != 1);
		}

	}

	private void sort() {

		// all Ages are 1
		cache.get(Hash3).Age = 0;
		cache.increaseLoadedTilesAge();
		// all Ages are 2 Tile3 are 1
		cache.get(Hash5).Age = 0;
		cache.increaseLoadedTilesAge();
		// all Ages are 3 Tile3 are 2, Tile5 are 1
		cache.get(Hash4).Age = 0;

		// Check Age
		assertFalse("Tile.Age must be 1", cache.get(Hash1).Age != 3);
		assertFalse("Tile.Age must be 1", cache.get(Hash2).Age != 3);
		assertFalse("Tile.Age must be 1", cache.get(Hash3).Age != 2);
		assertFalse("Tile.Age must be 1", cache.get(Hash4).Age != 0);
		assertFalse("Tile.Age must be 1", cache.get(Hash5).Age != 1);

		// check sort
		for (int i = 0, n = cache.size(); i < n; i++) {
			assertEquals(cache.get(i).toString(), "Tile:" + String.valueOf(5 - i));
		}

		for (int i = 1, n = cache.size() + 1; i < n; i++) {
			assertEquals(cache.get((long) i).toString(), "Tile:" + String.valueOf(i));
		}

		cache.sort();

		int[] targetSortList = new int[] { 4, 5, 3, 2, 1 };

		for (int i = 0, n = cache.size(); i < n; i++) {
			assertEquals(cache.get(i).toString(), "Tile:" + String.valueOf(targetSortList[i]));
		}

		for (int i = 1, n = cache.size() + 1; i < n; i++) {
			assertEquals(cache.get((long) i).toString(), "Tile:" + String.valueOf(i));
		}

	}

	private static int InstanceCount = 0;

	private class DummyTile extends TileGL {

		private boolean isDisposed = false;
		private final long Hash;

		public DummyTile(Long Hash) {
			this.Hash = Hash;
			InstanceCount++;
		}

		@Override
		public void dispose() {
			isDisposed = true;
			InstanceCount--;
		}

		@Override
		public boolean isDisposed() {
			return isDisposed;
		}

		@Override
		public boolean canDraw() {
			return true;
		}

		@Override
		public String toString() {
			return ("Tile:" + Hash);
		}

		@Override
		public long getWidth() {
			return 256;
		}

		@Override
		public long getHeight() {
			return 256;
		}

		@Override
		public void draw(Batch batch, float f, float y, float tILESIZE, float tILESIZE2, CB_List<TileGL_RotateDrawables> rotateList) {

		}

	}

}
