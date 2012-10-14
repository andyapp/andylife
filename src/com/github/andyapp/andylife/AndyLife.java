package com.github.andyapp.andylife;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import java.util.Random;

public class AndyLife extends Activity {

	ClassicLifeView lifeView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lifeView = new ClassicLifeView(this);

		setContentView(lifeView);

		Toast.makeText(this, "Touch the screen to begin.", Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		lifeView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		lifeView.resume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int eventaction = event.getAction();

		switch (eventaction) {

		case MotionEvent.ACTION_DOWN: {
			float x = event.getX();
			float y = event.getY();

			lifeView.createLife(lifeView.getCellX(x), lifeView.getCellY(y));

			break;
		}

		case MotionEvent.ACTION_MOVE: {
			float x = event.getX();
			float y = event.getY();

			lifeView.createLife(lifeView.getCellX(x), lifeView.getCellY(y));

			break;
		}
		}
		lifeView.invalidate();
		return true;
	}

	public class ClassicLifeView extends SurfaceView implements Runnable {

		Thread thread = null;
		SurfaceHolder holder;
		boolean isRunning = false;

		int cells[][][];
		int cells_now = 0;
		int cells_next = 1;

		int cell_width = 20;
		int cell_height = 20;
		int cell_corner_radius = 4;

		int cells_x_size = 2;
		int cells_y_size = 2;

		int sleep_time = 0;

		public ClassicLifeView(Context context) {
			super(context);
		}

		public int getCellX(float screenX) {
			return (int) (screenX / cell_width);
		}

		public int getCellY(float screenY) {
			return (int) (screenY / cell_height);
		}

		public void createLife(int x, int y) {

			Random random = new Random();

			if (x >= cells_x_size) {
				return;
			}

			if (y >= cells_y_size) {
				return;
			}

			// create left vertical column
			if (x > 0) {

				if (y > 0) {
					cells[cells_now][x - 1][y - 1] = random.nextInt(2);
				}

				cells[cells_now][x - 1][y] = random.nextInt(2);

				if (y < cells_y_size - 1) {
					cells[cells_now][x - 1][y + 1] = random.nextInt(2);
				}
			}

			// create center column
			if (y > 0) {
				cells[cells_now][x][y - 1] = random.nextInt(2);
			}

			cells[cells_now][x][y] = 1;

			if (y < cells_y_size - 1) {
				cells[cells_now][x][y + 1] = random.nextInt(2);
			}

			// count right vertical column
			if (x < cells_x_size - 1) {
				if (y > 0) {
					cells[cells_now][x + 1][y - 1] = random.nextInt(2);
				}

				cells[cells_now][x + 1][y] = random.nextInt(2);

				if (y < cells_y_size - 1) {
					cells[cells_now][x + 1][y + 1] = random.nextInt(2);
				}
			}
		}

		private int getCellNeighborCount(int now, int next, int x, int y) {
			int neighbors = 0;

			// count left vertical column
			if (x > 0) {

				if (y > 0) {
					neighbors = neighbors + cells[now][x - 1][y - 1];
				}

				neighbors = neighbors + cells[now][x - 1][y];

				if (y < cells_y_size - 1) {
					neighbors = neighbors + cells[now][x - 1][y + 1];
				}
			}

			// count center column
			if (y > 0) {
				neighbors = neighbors + cells[now][x][y - 1];
			}

			if (y < cells_y_size - 1) {
				neighbors = neighbors + cells[now][x][y + 1];
			}

			// count right vertical column
			if (x < cells_x_size - 1) {
				if (y > 0) {
					neighbors = neighbors + cells[now][x + 1][y - 1];
				}

				neighbors = neighbors + cells[now][x + 1][y];

				if (y < cells_y_size - 1) {
					neighbors = neighbors + cells[now][x + 1][y + 1];
				}
			}

			return neighbors;
		}

		// Any live cell with fewer than two live neighbors dies, as if caused
		// by under-population.
		// Any live cell with two or three live neighbors lives on to the next
		// generation.
		// Any live cell with more than three live neighbors dies, as if by
		// overcrowding.
		// Any dead cell with exactly three live neighbors becomes a live cell,
		// as if by reproduction.

		private void simulateCell(int now, int next, int x, int y) {

			int cell = cells[now][x][y];

			int neighbors = getCellNeighborCount(now, next, x, y);

			if (cell > 0) {
				if ((neighbors < 2) || (neighbors > 3)) {
					cell = 0;
				}
			} else {
				if (neighbors == 3) {
					cell = 1;
				}
			}

			cells[next][x][y] = cell;
		}

		private void simulateTick() {
			int now = this.cells_now;
			int next = this.cells_next;

			for (int x = 0; x < cells_x_size; x++) {
				for (int y = 0; y < cells_y_size; y++) {

					simulateCell(now, next, x, y);
				}
			}

			this.cells_now = next;
			this.cells_next = now;
		}

		void drawCells() {

			Paint paint = new Paint();

			RectF rect = new RectF(0, 0, cell_width, cell_height);

			Canvas canvas = holder.lockCanvas();

			for (int x = 0; x < cells_x_size; x++) {
				for (int y = 0; y < cells_y_size; y++) {

					rect.offsetTo(x * cell_width, y * cell_height);

					if (cells[cells_now][x][y] > 0) {
						paint.setShader(new LinearGradient(0, 0, 0,
								cell_height, Color.GREEN, Color.rgb(0x0, 0xAA,
										0x0), Shader.TileMode.REPEAT));
						paint.setARGB(0xFF, 0, 0xFF, 0);
						canvas.drawRoundRect(rect, cell_corner_radius,
								cell_corner_radius, paint);
					} else {
						paint.setShader(null);
						paint.setARGB(0xFF, 0, 0, 0);
						canvas.drawRoundRect(rect, cell_corner_radius,
								cell_corner_radius, paint);
					}

				}
			}

			holder.unlockCanvasAndPost(canvas);
		}

		public void run() {

			if (holder == null) {
				holder = getHolder();
			}

			while (!holder.getSurface().isValid()) {
			}

			// calculate number of cells
			Canvas canvas = holder.lockCanvas();
			cells_x_size = canvas.getWidth() / cell_width;
			cells_y_size = canvas.getHeight() / cell_height;
			holder.unlockCanvasAndPost(canvas);

			if (cells == null) {
				cells = new int[2][cells_x_size][cells_y_size];
			}

			while (isRunning) {
				if (!holder.getSurface().isValid()) {
					continue;
				}

				simulateTick();
				drawCells();

				try {
					Thread.sleep(sleep_time);
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
