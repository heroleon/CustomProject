package com.dcl.customproject;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * @author dcl
 * 2017/9/1
 **/
public class CustomCamera extends Activity implements SurfaceHolder.Callback {

	private Camera mCamera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	int REQUEST_EXTERNAL_STORAGE=1;
	String[] PERMISSIONS_STORAGE={
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			String path ="/sdcard/waterpic/";
			File tempFile = new File(path);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			Bitmap bit = compressImageFromFile(path, data);
			take_photo.setVisibility(View.GONE);
			switch_camera.setVisibility(View.GONE);
			surfaceView.setVisibility(View.GONE);

			cancel_choose.setVisibility(View.VISIBLE);
			confirm_choose.setVisibility(View.VISIBLE);
			showPicture.setVisibility(View.VISIBLE);
			startPropertyAnim(cancel_choose, -300f);
			startPropertyAnim(confirm_choose, 300f);
			showPicture.setImageBitmap(bit);

		}
	};
	private String wholeUrl;

	private Bitmap compressImageFromFile(String file, byte[] data) {
		if (PackageManager.PERMISSION_GRANTED!=
				ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS))
		{
			ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
		}
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap;
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 800f;//
		float ww = 480f;//
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;

		newOpts.inPreferredConfig = Config.ARGB_8888;
		newOpts.inPurgeable = true;
		newOpts.inInputShareable = true;
		bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, newOpts);
		Matrix matrix = new Matrix();
		if (cameraPosition == 1) {
			matrix.setRotate(90);
		} else {
			matrix.setRotate(-90);
		}
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		String timeMark = convertToTime(System.currentTimeMillis());
		bitmap = drawTextToRightBottom(getApplicationContext(), bitmap, timeMark, 8, Color.YELLOW, 10, 5);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		try {
			String time = String.valueOf(System.currentTimeMillis());
			wholeUrl = file + time + ".png";
			FileOutputStream fos = new FileOutputStream(wholeUrl);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return bitmap;
	}

	public static String convertToTime(long time) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(time);
		return df.format(date);
	}

	private int cameraPosition = 1;
	private ImageButton cancel_choose;
	private ImageButton confirm_choose;
	private ImageButton take_photo;
	private ImageButton switch_camera;
	private ImageView showPicture;
	private String addr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_custom_camera);

		surfaceView = (SurfaceView) findViewById(R.id.sf_preview);

		cancel_choose = (ImageButton) findViewById(R.id.ib_cancel_choose);
		confirm_choose = (ImageButton) findViewById(R.id.ib_confirm_choose);
		take_photo = (ImageButton) findViewById(R.id.ib_take_photo);
		switch_camera = (ImageButton) findViewById(R.id.ib_switch_camera);
		showPicture = (ImageView) findViewById(R.id.iv_show_pic);

		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCamera.autoFocus(null);
			}
		});
	}

	@SuppressWarnings("deprecation")
	public void capture(View view) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
		Display display = getWindowManager().getDefaultDisplay();
		int maxSize = Math.max(display.getWidth(), display.getHeight());
		int length = sizes.size();
		if (maxSize > 0) {
			for (int i = 0; i < length; i++) {
				if (maxSize <= Math.max(sizes.get(i).width, sizes.get(i).height)) {
					parameters.setPictureSize(sizes.get(i).width, sizes.get(i).height);
					break;
				}
			}
		}
		List<Camera.Size> ShowSizes = parameters.getSupportedPreviewSizes();
		int showLength = ShowSizes.size();
		if (maxSize > 0) {
			for (int i = 0; i < showLength; i++) {
				if (maxSize <= Math.max(ShowSizes.get(i).width, ShowSizes.get(i).height)) {
					parameters.setPreviewSize(ShowSizes.get(i).width, ShowSizes.get(i).height);
					break;
				}
			}
		}

		mCamera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				camera.takePicture(null, null, mPictureCallback);
			}
		});
	}

	public void switchCamera(View view) {
		chooseCamera();
	}
	
	private void chooseCamera() {

				int cameraCount = 0;
				CameraInfo cameraInfo = new CameraInfo();
				cameraCount = Camera.getNumberOfCameras();
				for (int i = 0; i < cameraCount; i++) {
					Camera.getCameraInfo(i, cameraInfo);
					if (cameraPosition == 1) {

						if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
							releaseCamera();
							mCamera = Camera.open(i);
							setStartPreview(mCamera, surfaceHolder);
							cameraPosition = 0;
							break;
						}
					} else {
						if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
							releaseCamera();
							mCamera = Camera.open(i);
							setStartPreview(mCamera, surfaceHolder);
							cameraPosition = 1;
							break;
						}
					}

				}
		
	}

	public void choosePic(View view) {
		Intent returnIntent = getIntent();
		returnIntent.putExtra(MainActivity.RETURN_IMAGES, wholeUrl);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	public void cancel(View view) {
		File imgUrl = new File(wholeUrl);
		if (imgUrl.exists()) {
			imgUrl.delete();
		}
		/*if (mCamera == null) {
			chooseCamera();
		}*/
		take_photo.setVisibility(View.VISIBLE);
		switch_camera.setVisibility(View.VISIBLE);
		surfaceView.setVisibility(View.VISIBLE);

		cancel_choose.setVisibility(View.GONE);
		confirm_choose.setVisibility(View.GONE);
		showPicture.setVisibility(View.GONE);
		startPropertyAnim(cancel_choose, 300f);
		startPropertyAnim(confirm_choose, -300f);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCamera == null) {
			mCamera = getCamera();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}

	private Camera getCamera() {
		Camera camera;
		try {
			camera = Camera.open();
		} catch (Exception e) {
			camera = null;
		}
		return camera;
	}

	private void setStartPreview(Camera camera, SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.setDisplayOrientation(90);
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	}

	private void startPropertyAnim(ImageButton ib, float translate) {

		float translationX = take_photo.getTranslationX();

		ObjectAnimator anim = ObjectAnimator.ofFloat(ib, "translationX", translationX, translate);
		anim.setDuration(500);
		anim.start();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setStartPreview(mCamera, surfaceHolder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mCamera != null) {
			mCamera.stopPreview();
			setStartPreview(mCamera, surfaceHolder);
		} else {
			Toast.makeText(getApplicationContext(),"请开启相机权限",Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public Bitmap drawTextToRightBottom(Context context, Bitmap bitmap, String text, int size, int color,
			int paddingRight, int paddingBottom) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		paint.setTextSize(dp2px(context, size));
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		return drawTextToBitmap(context, bitmap, text, paint, bounds,
				bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
				bitmap.getHeight() - dp2px(context, paddingBottom));
	}


	private Bitmap drawTextToBitmap(Context context, Bitmap bitmap, String text, Paint paint, Rect bounds,
			int paddingLeft, int paddingTop) {
		Config bitmapConfig = bitmap.getConfig();

		paint.setDither(true);
		paint.setFilterBitmap(true);
		if (bitmapConfig == null) {
			bitmapConfig = Config.ARGB_8888;
		}
		bitmap = bitmap.copy(bitmapConfig, true);
		Canvas canvas = new Canvas(bitmap);

		canvas.drawText(text, paddingLeft, paddingTop, paint);
		if(!TextUtils.isEmpty(addr)){
			canvas.drawText(addr, paddingLeft, paddingTop-30, paint);
		}
		return bitmap;
	}

	public int dp2px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
}
