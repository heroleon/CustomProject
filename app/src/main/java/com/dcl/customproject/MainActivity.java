package com.dcl.customproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ImageView iv;
	public final static  String RETURN_IMAGES = "return_images";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		iv = (ImageView) findViewById(R.id.iv_show);
	}
	
	public void takePhoto(View view){
		Intent takePhoto = new Intent(this,CustomCamera.class);
		startActivityForResult(takePhoto,1000);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK){
			if (data == null)
				return;
			String arrImg = data.getStringExtra(RETURN_IMAGES);
		/*	File file = new File(arrImg);
			if (file.exists()) {
				Bitmap bm = BitmapFactory.decodeFile(filepath);
				//将图片显示到ImageView中
				img.setImageBitmap(bm);
			}*/
			Toast.makeText(getApplicationContext(),arrImg,Toast.LENGTH_SHORT).show();
			if(!TextUtils.isEmpty(arrImg)){
				iv.setImageURI(Uri.parse(arrImg));
			}
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
