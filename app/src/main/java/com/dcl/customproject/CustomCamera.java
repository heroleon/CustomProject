package com.dcl.customproject;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * @author dcl
 * 2017/9/1
 * 自定义相机
 **/
public class CustomCamera extends Activity implements SurfaceHolder.Callback {

	public static final String RETURN_IMAGES = "return_img";
	private Camera mCamera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private String wholeUrl;
	private String[] markTexts = null;

	private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			String time = convertToTime(System.currentTimeMillis());
			if(!TextUtils.isEmpty(addr)){
				setMarkTexts(addr,time);
			}else{
				setMarkTexts(time);
			}
			String path = Environment.getExternalStorageDirectory().getPath()+"/waterpic/";
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

	private void setMarkTexts(String... markTexts)
	{
		this.markTexts = markTexts;
	}
	private Bitmap compressImageFromFile(String file, byte[] data) {
		String time = String.valueOf(System.currentTimeMillis());
		wholeUrl = file + time + ".jpg";
		Bitmap compressBitmap = BitmapUtil.compressImageFromFile(data);
		Matrix matrix = new Matrix();
		if (cameraPosition == 1) {
			matrix.setRotate(90);
		} else {
			matrix.setRotate(-90);
		}
		compressBitmap = Bitmap.createBitmap(compressBitmap, 0, 0, compressBitmap.getWidth(), compressBitmap.getHeight(), matrix, true);
		Bitmap markedBitmap = null;
		if(markTexts!=null){
			markedBitmap = BitmapUtil.drawTextInPhoto(compressBitmap,this,markTexts);
			BitmapUtil.writeBitmap2File(markedBitmap, new File(wholeUrl));
		}
		return markedBitmap;
	}

	public String convertToTime(long time) {
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
		Intent intent = getIntent();
		//addr = getIntent().getStringExtra(MainActivity.WATERADDR);

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
		parameters.getSupportedPictureSizes();
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
		if(cameraPosition==1){
			mCamera.setParameters(parameters);
		}
		try{
			if(mCamera!=null){
				mCamera.autoFocus(new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						camera.takePicture(null, null, mPictureCallback);
					}
				});
			}
		}catch (Exception e) {
		}

	}

	public void switchCamera(View view) {
		chooseCamera();
	}

	private void chooseCamera() {
		// 切换前后摄像头
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
			if (cameraPosition == 1) {
				// 现在是后置，变更为前置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
					releaseCamera();
					mCamera = Camera.open(i);// 打开当前选中的摄像头
					setStartPreview(mCamera, surfaceHolder);
					cameraPosition = 0;
					break;
				}
			} else {
				// 现在是前置， 变更为后置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
					releaseCamera();
					mCamera = Camera.open(i);// 打开当前选中的摄像头
					setStartPreview(mCamera, surfaceHolder);
					cameraPosition = 1;
					break;
				}
			}

		}

	}

	public void choosePic(View view) {
		Intent returnIntent = getIntent();
		returnIntent.putExtra(RETURN_IMAGES, wholeUrl);
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
		// X轴方向上的坐标
		float translationX = take_photo.getTranslationX();
		// 向右移动500pix，然后再移动到原来的位置复原。
		// 参数“translationX”指明在x坐标轴位移，即水平位移。
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
			Toast.makeText(getApplicationContext(), "请开启摄像权限",Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}
