package com.github.andyapp.andylife;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.Window;
import android.widget.Toast;
import java.util.Random;

public class AndyLife extends Activity implements OnLayoutChangeListener {

	ClassicLifeView lifeView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lifeView = new ClassicLifeView(this);
		setPreferences();

		lifeView.addOnLayoutChangeListener(this);

		setContentView(lifeView);

		Toast.makeText(this, "Touch the screen to begin.", Toast.LENGTH_LONG)
				.show();
	}

	public void onLayoutChange(View v, int left, int top, int right,
			int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		Log.d("LayoutTest", "left=" + left + ", top=" + top + ", right="
				+ right + ", bottom=" + bottom);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int screen_height = displaymetrics.heightPixels;
		int screen_width = displaymetrics.widthPixels;

		Log.d("ScreenMetrics", screen_height + " " + screen_width);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_andy_life, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemStart: {
			lifeView.setSimulating(true);
			break;
		}
		case R.id.menuItemStop: {
			lifeView.setSimulating(false);
			break;
		}
		case R.id.menuItemNext: {
			lifeView.setSimulateTick(true);
			break;
		}
		case R.id.menuItemReset: {
			this.resetLifeView();
			lifeView.resume();
			break;
		}
		case R.id.menu_preferences: {
			Intent intent = new Intent(this, AndyLifePreferences.class);
			startActivity(intent);
			break;
		}
		default:
			break;
		}

		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		lifeView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.resetLifeView();
		lifeView.resume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int eventaction = event.getAction();

		switch (eventaction) {

		case MotionEvent.ACTION_DOWN: {
			float x = event.getX();
			float y = event.getY();

			Log.i("ACTION_DOWN", x + " " + y);

			final Window window = getWindow();
			final View contentView = window
					.findViewById(Window.ID_ANDROID_CONTENT);
			final float viewTop = contentView.getTop();
			final float viewLeft = contentView.getLeft();

			if ((x < viewLeft) || (y < viewTop)) {
				return false;
			}

			lifeView.createLife(lifeView.getCellX(x - viewLeft),
					lifeView.getCellY(y - viewTop));

			break;
		}

		case MotionEvent.ACTION_MOVE: {
			float x = event.getX();
			float y = event.getY();

			final Window window = getWindow();
			final View contentView = window
					.findViewById(Window.ID_ANDROID_CONTENT);
			final float viewTop = contentView.getTop();
			final float viewLeft = contentView.getLeft();

			if ((x < viewLeft) || (y < viewTop)) {
				return false;
			}

			lifeView.createLife(lifeView.getCellX(x - viewLeft),
					lifeView.getCellY(y - viewTop));

			break;
		}
		}
		lifeView.invalidate();
		return true;
	}

	private void resetLifeView() {
		lifeView.pause();
		lifeView.cells = null;
		setPreferences();
	}

	private void setPreferences() {
		if (this.lifeView == null) {
			return;
		}

		// get data from settings activity in this case the language
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean show_stats = preferences.getBoolean("show_stats", true);
		lifeView.setShowStats(show_stats);

		String cell_size_str = preferences.getString("cell_size", "");
		Log.d("cell_size_str", cell_size_str);

		int cell_size = 20;
		try {
			cell_size = Integer.parseInt(cell_size_str);
		} catch (NumberFormatException e) {
			// todo reset to default
		}

		lifeView.setCellWidth(cell_size);
		lifeView.setCellHeight(cell_size);
		lifeView.setCellCornerRadius(preferences.getInt("cellCornerRadius", 5));
		lifeView.setSleepTime(preferences.getInt("sleepTime", 0));

		int color = Color.GREEN;

		String color_str = preferences.getString("cell_color", "");
		color_str = color_str.toUpperCase();

		if (color_str.equals("GREEN")) {
			color = Color.GREEN;
		} else if (color_str.equals("RED")) {
			color = Color.RED;
		} else if (color_str.equals("BLUE")) {
			color = Color.BLUE;
		} else if (color_str.equals("YELLOW")) {
			color = Color.YELLOW;
		}

		lifeView.cellPrototype.color = color;
	}

	public class CellPrototype {
		int width;
		int height;
		int cornerRadius;
		int color;
	}

	public class Cell {
		int color;

		public final int DEAD_CELL_COLOR = Color.BLACK;

		public Cell() {
			this.color = DEAD_CELL_COLOR;
		}

		public Cell(int color) {
			this.color = color;
		}

		public boolean isAlive() {
			return this.color != Color.BLACK;
		}

		public void kill() {
			this.color = DEAD_CELL_COLOR;
		}
	}

	public class CellArray {
		Cell cells[][];
		int countX;
		int countY;

		public CellArray(int x_count, int y_count) {
			if ((x_count <= 0) || (y_count <= 0))
				return;

			this.countX = x_count;
			this.countY = y_count;
			cells = new Cell[x_count][y_count];

			for (int x = 0; x < x_count; x++) {
				for (int y = 0; y < y_count; y++) {
					cells[x][y] = new Cell();
				}
			}
		}
	}

	public class CellSeeder {
		/* Current number of seeds */
		int seedCount = 0;

		/* Maximum number of seeds */
		int seedMaxCount = 9;

		/* Number of ticks required to spawn a single seed */
		int seedSpawnRate = 1;

		/* Number of ticks until next seed is spawned */
		int seedSpawnTicks = 0;
	}

	public class FrameCounter {
		private int samplesCollected = 0;
		private int sampleTime = 0;
		private int fps = 0;
		long previousTime = 0;

		void update() {
			long now = System.currentTimeMillis();

			if (previousTime != 0) {
				// Time difference between now and last time we were here
				int time = (int) (now - previousTime);
				sampleTime += time;
				samplesCollected++;
				// After 10 frames
				if (samplesCollected == 10) {

					// Update the fps variable
					fps = (int) (10000 / sampleTime);
					// Log.i("fps:", " " + fps);

					// Reset the sampletime + frames collected
					sampleTime = 0;
					samplesCollected = 0;
				}
			}
			previousTime = now;
		}

		int getFps() {
			return fps;
		}
	}

	public class ClassicLifeView extends SurfaceView implements Runnable {

		Thread thread = null;
		SurfaceHolder holder;
		boolean isRunning = false;
		boolean isSimulating = false;
		boolean simulateTick = false;
		boolean showStats = false;

		CellSeeder cellSeeder;
		CellPrototype cellPrototype;
		CellArray cells[];
		int cells_now = 0;
		int cells_next = 1;
		int cells_x_count = 2;
		int cells_y_count = 2;
		int cells_alive_count = 0;
		int cell_gap = 1;

		int sleepTime = 0;

		FrameCounter frameCounter;

		public ClassicLifeView(Context context) {
			super(context);

			frameCounter = new FrameCounter();

			cellPrototype = new CellPrototype();
			cellPrototype.color = Color.GREEN;
			cellPrototype.cornerRadius = 4;
			cellPrototype.height = 40;
			cellPrototype.width = 40;

			cellSeeder = new CellSeeder();
		}

		public int getCellX(float screenX) {
			return (int) (screenX / (cellPrototype.width + this.cell_gap));
		}

		public int getCellY(float screenY) {
			return (int) (screenY / (cellPrototype.height + this.cell_gap));
		}

		public int getCellWidth() {
			return cellPrototype.width;
		}

		public void setCellWidth(int cellWidth) {
			this.cellPrototype.width = cellWidth;
		}

		public int getCellHeight() {
			return cellPrototype.height;
		}

		public void setCellHeight(int cellHeight) {
			this.cellPrototype.height = cellHeight;
		}

		public int getCellCornerRadius() {
			return cellPrototype.cornerRadius;
		}

		public void setCellCornerRadius(int cellCornerRadius) {
			this.cellPrototype.cornerRadius = cellCornerRadius;
		}

		public int getSleepTime() {
			return sleepTime;
		}

		public void setSleepTime(int sleepTime) {
			this.sleepTime = sleepTime;
		}

		public boolean isShowStats() {
			return showStats;
		}

		public void setShowStats(boolean showStats) {
			this.showStats = showStats;
		}

		public boolean isSimulating() {
			return isSimulating;
		}

		public void setSimulating(boolean isSimulating) {
			this.isSimulating = isSimulating;
		}

		public void setSimulateTick(boolean simulateTick) {
			this.simulateTick = simulateTick;
		}

		public void createLife(int x, int y) {

			if (cellSeeder.seedCount == 0) {
				return;
			}

			// Random random = new Random();

			if (x >= cells_x_count) {
				return;
			}

			if (y >= cells_y_count) {
				return;
			}

			// // create left vertical column
			// if (x > 0) {
			//
			// if (y > 0) {
			// cells[cells_now][x - 1][y - 1] = random.nextInt(2);
			// }
			//
			// cells[cells_now][x - 1][y] = random.nextInt(2);
			//
			// if (y < cells_y_count - 1) {
			// cells[cells_now][x - 1][y + 1] = random.nextInt(2);
			// }
			// }
			//
			// // create center column
			// if (y > 0) {
			// cells[cells_now][x][y - 1] = random.nextInt(2);
			// }

			if (!cells[cells_now].cells[x][y].isAlive()) {
				cellSeeder.seedCount -= 1;
				cells[cells_now].cells[x][y].color = cellPrototype.color;
			}

			// if (y < cells_y_count - 1) {
			// cells[cells_now][x][y + 1] = random.nextInt(2);
			// }
			//
			// // count right vertical column
			// if (x < cells_x_count - 1) {
			// if (y > 0) {
			// cells[cells_now][x + 1][y - 1] = random.nextInt(2);
			// }
			//
			// cells[cells_now][x + 1][y] = random.nextInt(2);
			//
			// if (y < cells_y_count - 1) {
			// cells[cells_now][x + 1][y + 1] = random.nextInt(2);
			// }
			// }
		}

		private int getCellNeighborCount(int now, int next, int x, int y) {
			int neighbors = 0;

			// count left vertical column
			if (x > 0) {

				if (y > 0) {
					if (cells[now].cells[x - 1][y - 1].isAlive()) {
						neighbors = neighbors + 1;
					}
				}

				if (cells[now].cells[x - 1][y].isAlive()) {
					neighbors = neighbors + 1;
				}

				if (y < cells_y_count - 1) {
					if (cells[now].cells[x - 1][y + 1].isAlive()) {
						neighbors = neighbors + 1;
					}
				}
			}

			// count center column
			if (y > 0) {
				if (cells[now].cells[x][y - 1].isAlive()) {
					neighbors = neighbors + 1;
				}
			}

			if (y < cells_y_count - 1) {
				if (cells[now].cells[x][y + 1].isAlive()) {
					neighbors = neighbors + 1;
				}
			}

			// count right vertical column
			if (x < cells_x_count - 1) {
				if (y > 0) {
					if (cells[now].cells[x + 1][y - 1].isAlive()) {
						neighbors = neighbors + 1;
					}
				}

				if (cells[now].cells[x + 1][y].isAlive()) {
					neighbors = neighbors + 1;
				}

				if (y < cells_y_count - 1) {
					if (cells[now].cells[x + 1][y + 1].isAlive()) {
						neighbors = neighbors + 1;
					}
				}
			}

			return neighbors;
		}

		// 1) Any live cell with fewer than two live neighbors dies, as if
		// caused
		// by under-population.
		// 2) Any live cell with two or three live neighbors lives on to the
		// next
		// generation.
		// 3) Any live cell with more than three live neighbors dies, as if by
		// overcrowding.
		// 4) Any dead cell with exactly three live neighbors becomes a live
		// cell,
		// as if by reproduction.

		private void simulateCell(int now, int next, int x, int y) {

			Cell cell = cells[now].cells[x][y];

			int neighbors = getCellNeighborCount(now, next, x, y);
			if (cell.isAlive()) {
				if ((neighbors < 2) || (neighbors > 3)) {
					cells[next].cells[x][y].kill();
				} else {
					cells[next].cells[x][y].color = cell.color;
				}
			} else {
				if (neighbors == 3) {
					cells[next].cells[x][y].color = cellPrototype.color;
				} else {
					cells[next].cells[x][y].kill();
				}
			}
		}

		private void simulateTick() {

			this.cells_alive_count = 0;
			int now = this.cells_now;
			int next = this.cells_next;

			for (int x = 0; x < cells_x_count; x++) {
				for (int y = 0; y < cells_y_count; y++) {
					simulateCell(now, next, x, y);

					if (cells[next].cells[x][y].isAlive()) {
						this.cells_alive_count += 1;
					}
				}
			}

			// swap next and now indexes
			this.cells_now = next;
			this.cells_next = now;
		}

		void drawDeadCell(Canvas canvas, int x, int y, RectF rect, Paint paint) {
			paint.setShader(null);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rect, cellPrototype.cornerRadius,
					cellPrototype.cornerRadius, paint);
		}

		void drawLiveCell(Canvas canvas, int x, int y, RectF rect, Paint paint) {
			int light_color = cellPrototype.color;

			float[] hsv = new float[3];
			Color.colorToHSV(cellPrototype.color, hsv);
			hsv[2] *= 0.8f; // value component
			int dark_color = Color.HSVToColor(hsv);

			paint.setShader(new LinearGradient(0, 0, 0, cellPrototype.height
					+ cell_gap, light_color, dark_color, Shader.TileMode.REPEAT));
			paint.setARGB(0xFF, 0, 0xFF, 0);
			canvas.drawRoundRect(rect, cellPrototype.cornerRadius,
					cellPrototype.cornerRadius, paint);
		}

		void drawCell(Canvas canvas, int x, int y, RectF rect, Paint paint) {
			if (cells[cells_now].cells[x][y].isAlive()) {
				drawLiveCell(canvas, x, y, rect, paint);
			} else {
				drawDeadCell(canvas, x, y, rect, paint);
			}
		}

		void drawCells(Canvas canvas) {
			Paint paint = new Paint();
			RectF rect = new RectF(0, 0, cellPrototype.width,
					cellPrototype.height);

			for (int x = 0; x < cells_x_count; x++) {
				for (int y = 0; y < cells_y_count; y++) {
					rect.offsetTo(x * (cellPrototype.width + cell_gap), y
							* (cellPrototype.height + cell_gap));
					drawCell(canvas, x, y, rect, paint);
				}
			}

		}

		void drawStatusText(Canvas canvas) {
			Paint paint = new Paint();

			paint.setColor(Color.WHITE);
			paint.setTextSize(18);

			String text;
			int text_top = 25;

			if (isSimulating) {
				text = "Simulation Running";
			} else {
				text = "Simulation Stopped";
			}
			canvas.drawText(text, 10, text_top, paint);
			text_top += 25;

			canvas.drawText("Grid: " + this.cells_x_count + "x"
					+ this.cells_y_count, 10, text_top, paint);
			text_top += 25;

			canvas.drawText("Total cells: " + this.cells_x_count
					* this.cells_y_count, 10, text_top, paint);
			text_top += 25;

			canvas.drawText("Live cells: " + cells_alive_count, 10, text_top,
					paint);
			text_top += 25;

			canvas.drawText("Seeds: " + cellSeeder.seedCount, 10, text_top,
					paint);
			text_top += 25;

			canvas.drawText("FPS: " + frameCounter.getFps(), 10, text_top,
					paint);
			text_top += 25;

		}

		void draw() {
			Canvas canvas = holder.lockCanvas();

			canvas.drawColor(Color.DKGRAY);
			drawCells(canvas);

			if (this.showStats) {
				drawStatusText(canvas);
			}

			holder.unlockCanvasAndPost(canvas);
			frameCounter.update();
		}

		public void init() {
			this.cells = null;
			this.cells = new CellArray[2];
			for (int i = 0; i < 2; i++) {
				cells[i] = new CellArray(cells_x_count, cells_y_count);
			}
		}

		public void run() {

			if (holder == null) {
				holder = getHolder();
			}

			while (!holder.getSurface().isValid()) {
			}

			// calculate number of cells
			Canvas canvas = holder.lockCanvas();

			Log.i("CanvasSize", canvas.getWidth() + " " + canvas.getHeight());

			cells_x_count = canvas.getWidth()
					/ (cellPrototype.width + cell_gap);
			cells_y_count = canvas.getHeight()
					/ (cellPrototype.height + cell_gap);
			holder.unlockCanvasAndPost(canvas);

			if (cells == null) {
				init();
			}

			while (isRunning) {
				if (!holder.getSurface().isValid()) {
					continue;
				}

				if (cellSeeder.seedSpawnTicks == 0) {
					cellSeeder.seedSpawnTicks += cellSeeder.seedSpawnRate;
					if (cellSeeder.seedCount < cellSeeder.seedMaxCount) {
						cellSeeder.seedCount += 1;
					}
				} else {
					if (cellSeeder.seedSpawnTicks > 0) {
						cellSeeder.seedSpawnTicks -= 1;
					}
				}

				if (isSimulating) {
					simulateTick = true;
				}

				if (simulateTick) {
					simulateTick();
					simulateTick = false;
				}

				this.draw();

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void pause() {
			if (thread == null) {
				return;
			}

			isRunning = false;
			while (true) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			thread = null;
		}

		public void resume() {
			isRunning = true;
			thread = new Thread(this);
			thread.start();
		}
	}

}
