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
			lifeView.pause();
			lifeView.init();
			setPreferences();
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
		setPreferences();
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
			final View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);
			final float viewTop = contentView.getTop();
			final float viewLeft = contentView.getLeft();
			
			if ((x < viewLeft) || (y < viewTop)){
				return false;
			}
			
			lifeView.createLife(lifeView.getCellX(x - viewLeft), lifeView.getCellY(y - viewTop));

			break;
		}

		case MotionEvent.ACTION_MOVE: {
			float x = event.getX();
			float y = event.getY();

			final Window window = getWindow();
			final View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);
			final float viewTop = contentView.getTop();
			final float viewLeft = contentView.getLeft();
			
			if ((x < viewLeft) || (y < viewTop)){
				return false;
			}			
			
			lifeView.createLife(lifeView.getCellX(x - viewLeft), lifeView.getCellY(y - viewTop));

			break;
		}
		}
		lifeView.invalidate();
		return true;
	}

	private void setPreferences() {
		if (this.lifeView == null) {
			return;
		}

		// get data from settings activity in this case the language
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		int cell_size = preferences.getInt("cellSize", 20);
		
		lifeView.setCellWidth(cell_size);
		lifeView.setCellHeight(cell_size);
		lifeView.setCellCornerRadius(preferences.getInt("cellCornerRadius", 4));
		lifeView.setSleepTime(preferences.getInt("sleepTime", 0));
	}

	public class CellPattern {
		int cells[][];
		int cells_x_count = 5;
		int cells_y_count = 5;
	};

	public class CellPrototype {
		int width;
		int height;
		int cornerRadius;
		int color;
	}

	public class Cell {
		int color;

		public Cell(int color) {
			this.color = color;
		}

		public boolean isAlive() {
			return this.color != Color.BLACK;
		}

		public void kill() {
			this.color = Color.BLACK;
		}
	}

	public class CellArray {
		Cell cells[][];
		int xCount;
		int yCount;

		public CellArray(int x_count, int y_count) {
			if ((x_count <= 0) || (y_count <= 0))
				return;

			this.xCount = x_count;
			this.yCount = y_count;
			cells = new Cell[x_count][y_count];

			for (int x = 0; x < x_count; x++) {
				for (int y = 0; y < y_count; y++) {
					cells[x][y] = new Cell(Color.BLACK);
				}
			}
		}
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

		CellArray cells[];
		int cells_now = 0;
		int cells_next = 1;
		int cells_x_count = 2;
		int cells_y_count = 2;

		int sleepTime = 0;

		CellPrototype cellPrototype;

		FrameCounter frameCounter;

		public ClassicLifeView(Context context) {
			super(context);

			frameCounter = new FrameCounter();

			cellPrototype = new CellPrototype();
			cellPrototype.color = Color.GREEN;
			cellPrototype.cornerRadius = 4;
			cellPrototype.height = 40;
			cellPrototype.width = 40;

		}

		public int getCellX(float screenX) {
			return (int) (screenX / cellPrototype.width);
		}

		public int getCellY(float screenY) {
			return (int) (screenY / cellPrototype.height);
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

			cells[cells_now].cells[x][y].color = Color.RED;// cellPrototype.color;

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

			int now = this.cells_now;
			int next = this.cells_next;

			for (int x = 0; x < cells_x_count; x++) {
				for (int y = 0; y < cells_y_count; y++) {

					simulateCell(now, next, x, y);
				}
			}

			// swap next and now indexes
			this.cells_now = next;
			this.cells_next = now;
		}

		void drawCell(Canvas canvas, int x, int y, RectF rect, Paint paint) {
			int light_color = cellPrototype.color;
			// int dark_color = (cellPrototype.color & 0xFF0000)
			// | (cellPrototype.color & 0xFF00)
			// | (cellPrototype.color & 0xFF);

			int dark_color = Color.MAGENTA;

			if (cells[cells_now].cells[x][y].isAlive()) {
				paint.setShader(new LinearGradient(0, 0, 0,
						cellPrototype.height, light_color, dark_color,
						Shader.TileMode.REPEAT));
				paint.setARGB(0xFF, 0, 0xFF, 0);
				canvas.drawRoundRect(rect, cellPrototype.cornerRadius,
						cellPrototype.cornerRadius, paint);
			} else {
				paint.setShader(null);
				paint.setColor(Color.BLACK);
				canvas.drawRoundRect(rect, cellPrototype.cornerRadius,
						cellPrototype.cornerRadius, paint);
			}
		}

		void drawCells(RectF rect, Paint paint) {

			Canvas canvas = holder.lockCanvas();

			for (int x = 0; x < cells_x_count; x++) {
				for (int y = 0; y < cells_y_count; y++) {
					rect.offsetTo(x * cellPrototype.width, y
							* cellPrototype.height);
					drawCell(canvas, x, y, rect, paint);
				}
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
			
			cells_x_count = canvas.getWidth() / cellPrototype.width;
			cells_y_count = canvas.getHeight() / cellPrototype.height;
			holder.unlockCanvasAndPost(canvas);

			if (cells == null) {
				init();
			}

			Paint paint = new Paint();
			RectF rect = new RectF(0, 0, cellPrototype.width,
					cellPrototype.height);

			while (isRunning) {
				if (!holder.getSurface().isValid()) {
					continue;
				}

				if (isSimulating) {
					simulateTick = true;
				}

				if (simulateTick) {
					simulateTick();
					simulateTick = false;
				}

				drawCells(rect, paint);

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
