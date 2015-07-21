package com.example.helloworld;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	TextView textView;

	static {
		System.loadLibrary("HelloWorld");
	}

	public native String msgFromNDK();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new GLSurfaceView(getApplicationContext()));
//		setContentView(R.layout.activity_main);
//		initView();
//		textView.setText(msgFromNDK());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {
		textView = (TextView)findViewById(R.id.text);
	}

	public class GLSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

		private Bitmap mImage;
		private SurfaceHolder mHolder;
		private Thread mLooper;

		private int mHeight;			// 画面の高さ
		private int mPositionTop = 0;	// 表示位置【TOP:Y座標】
		private int mPositionLeft = 0;	// 表示位置【LEFT:X座標】
		private long mTime = 0;		// ひとつ前の描画時刻
		private long mLapTime = 0;		// 画面上部から下部に到達するまでの時間

		public GLSurfaceView(Context context) {
			super(context);
			getHolder().addCallback(this);
			mImage = BitmapFactory.decodeResource(getResources(), R.drawable.img);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// スレッドの生成
			mHolder = holder;
			mLooper = new Thread(this, "GLThread");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// スレッドを削除
			mLooper = null;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (mLooper != null) {
				mHeight = height;
				mTime = System.currentTimeMillis();
				mLooper.start();
			}
		}

		@Override
		public void run() {

			while (mLooper != null) {
				// 描画処理
				doDraw();

				// 位置更新処理

				// 処理落ちによるスローモーションを避けるため現在時刻を取得
				long delta = System.currentTimeMillis() - mTime;
				mTime = System.currentTimeMillis();

				// 次の描画位置
				int nextPosition = (int)((delta / 100.0) * 200);	// 1秒間に200px動くとして

				if (mPositionTop + nextPosition < mHeight ) {
//					mPositionTop += nextPosition;
				} else {
					Log.d("VIEW", "mLapTime:" + (mTime - mLapTime));

					// 位置の初期化
					mPositionTop = 0;
				}
			}
		}

		private void doDraw() {
			// Canvasの取得（マルチスレッド環境対応のためのロック）
			Canvas canvas = mHolder.lockCanvas();

			Paint paint = new Paint();
			// 描画処理（Lock中なのでなるべく早く）
			canvas.drawColor(Color.GRAY);
			canvas.drawBitmap(mImage, mPositionLeft, mPositionTop, paint);

			// LockしたCanvasを解放、ほかの描画スレッドがあればそちらに
			mHolder.unlockCanvasAndPost(canvas);
		}
	}
}
