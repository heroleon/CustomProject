package com.dcl.customproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {
		private static final float TEXT_SIZE   = 10.0f;
	    private static final float TEXT_MARGIN = 10.0f;
	    private static final int   TEXT_COLOR  = Color.YELLOW;
	    private static final int BUFFER_SIZE = 1024 * 8;

	    public static Bitmap drawTextInPhoto(Bitmap bitmap,Context ctx, String... texts)
	    {
	        float  textSize = TEXT_SIZE;
	        int    width    = bitmap.getWidth();
	        int    height   = bitmap.getHeight();
	        Bitmap newb     = Bitmap.createBitmap(width, height, Config.ARGB_8888);
	        Canvas canvas   = new Canvas(newb);

	        // ������ͼ
	        canvas.drawBitmap(bitmap, 0, 0, null);
	        Paint paint = new Paint();
	        paint.setTextSize(dp2px(ctx,textSize));
	        paint.setTypeface(Typeface.DEFAULT_BOLD);
	        paint.setAntiAlias(true);
	        paint.setColor(TEXT_COLOR);
	        float margin = TEXT_MARGIN;

	        int length = texts.length;
	        for (int i = 0; i < length; i++)
	        {
	            String text = texts[i];
	            if (TextUtils.isEmpty(text))
	            {
	                continue;
	            }

	            float  textWidth  = paint.measureText(text);
	            Paint.FontMetrics fm         = paint.getFontMetrics();
	            float             textHeight = fm.descent - fm.ascent;

	            float x = width - textWidth - margin;
	            float y = height - (i + 1) * textHeight - (i + 1) * margin;
	            paint.setColor(Color.TRANSPARENT);
	            canvas.drawRect(x, y - textHeight + 5, x + textWidth, y + 5, paint);
	            paint.setColor(TEXT_COLOR);
	            canvas.drawText(text, x, y, paint);
	            canvas.save(Canvas.ALL_SAVE_FLAG);
	            canvas.restore();
	        }
	        if (!bitmap.isRecycled())
	        {
	            bitmap.recycle();
	        }
	        return newb;
	    }
	    
	    public static Bitmap getBitmapFormUri(Bitmap decoedBitmap,String uri, int reqWidth, int reqHeight)
	    {
	        // ����ĳЩ�ֻ����պ���ת����
	        int rotate = 0;
	        try
	        {
	            ExifInterface exif   = new ExifInterface(uri);
	            int  orientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
	            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
	            {
	                rotate = 90;
	            }
	            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
	            {
	                rotate = 180;
	            }
	            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
	            {
	                rotate = 270;
	            }
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }

	        if (rotate != 0)
	        {
	            Matrix m = new Matrix();
	            m.setRotate(rotate);

	            Bitmap rotatedBitmap = Bitmap.createBitmap(decoedBitmap, 0, 0, decoedBitmap.getWidth(), decoedBitmap.getHeight(), m, true);
	            if (!decoedBitmap.isRecycled())
	            {
	                decoedBitmap.recycle();
	            }
	            return rotatedBitmap;
	        }

	        return decoedBitmap;
	    }
	    
	    /**
	     * ����ͼƬ������ֵ
	     * ���ͼƬ��ԭʼ�߶Ȼ��߿�ȴ������������Ŀ�Ⱥ͸߶ȣ�������Ҫ��������ű�������ֵ������Ͳ����š�
	     * heightRatio��ͼƬԭʼ�߶���ѹ����߶ȵı�����
	     * widthRatio��ͼƬԭʼ�����ѹ�����ȵı�����
	     * inSampleSize��������ֵ ��ȡheightRatio��widthRatio����С��ֵ��
	     * inSampleSizeΪ1��ʾ��Ⱥ͸߶Ȳ����ţ�Ϊ2��ʾѹ����Ŀ����߶�Ϊԭ����1/2(ͼƬΪԭ1/4)��
	     *
	     * @param options
	     * @param reqWidth
	     * @param reqHeight
	     * @return
	     */
	    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	    {
	        final int height = options.outHeight;
	        final int width  = options.outWidth;
	        if (width > height)
	        {
	            int temp;
	            temp = reqWidth;
	            reqWidth = reqHeight;
	            reqHeight = temp;
	        }
	        int inSampleSize = 1;

	        if (height > reqHeight || width > reqWidth)
	        {
	            final int heightRatio = Math.round((float) height / (float) reqHeight);
	            final int widthRatio  = Math.round((float) width / (float) reqWidth);
	            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	        }
	        if (inSampleSize <= 0)
	        {
	            inSampleSize = 1;
	        }
	        return inSampleSize;
	    }
	    
	    
	    /**
	     * ѹ��ͼƬ
	     *
	     * @param originalBitmap
	     * @return
	     */
	    public static Bitmap compressBitmap(Bitmap originalBitmap)
	    {
	        if (originalBitmap == null)
	        {
	            return null;
	        }
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        //����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
	        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
	        int options = 100;
	        while (baos.toByteArray().length / 1024 > 100)
	        {
	            baos.reset();//����baos������һ�ε�д�븲��֮ǰ������
	            options -= 10;//ͼƬ����ÿ�μ���10
	            if (options < 0) options = 0;//���ͼƬ����С��10����ͼƬ������ѹ������Сֵ
	            originalBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//��ѹ�����ͼƬ���浽baos��
	            if (options == 0) break;//���ͼƬ�������ѽ�������򣬲��ٽ���ѹ��
	        }
	        if (!originalBitmap.isRecycled())
	        {
	            originalBitmap.recycle();
	            System.gc();
	        }
	        //��ѹ���������baos��ŵ�ByteArrayInputStream��
	        ByteArrayInputStream transformedBitmap = new ByteArrayInputStream(baos.toByteArray());
	        try
	        {
	            baos.close();
	        }
	        catch (IOException e)
	        {
	            e.printStackTrace();
	        }
	        //��ByteArrayInputStream��������ͼƬ
	        return BitmapFactory.decodeStream(transformedBitmap, null, null);
	    }
	    
	    public static void writeBitmap2File(Bitmap bitmap, File file)
	    {
	        if (bitmap == null || file == null)
	        {
	            return;
	        }
	        if (file.exists()) file.delete();
	        try
	        {
	            file.createNewFile();
	            FileOutputStream           fos = new FileOutputStream(file);
	            final BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
	            bitmap.compress(Bitmap.CompressFormat.JPEG,80, bos);
	            bos.flush();
	            bos.close();
	            fos.close();
	        }
	        catch (FileNotFoundException e)
	        {
	            e.printStackTrace();
	        }
	        catch (IOException e)
	        {

	        }
	    }
	    
	    public static Bitmap compressImageFromFile(byte[] data) {  
	        BitmapFactory.Options newOpts = new BitmapFactory.Options();  
	        newOpts.inJustDecodeBounds = true;//ֻ����,��������  
	        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,newOpts); 
	        newOpts.inJustDecodeBounds = false;  
	        
	        newOpts.inSampleSize = calculateInSampleSize(newOpts, 500, 500);//���ò�����  
	          
	        newOpts.inPreferredConfig = Config.ARGB_8888;//��ģʽ��Ĭ�ϵ�,�ɲ���  
	        newOpts.inPurgeable = true;// ͬʱ���òŻ���Ч  
	        newOpts.inInputShareable = true;//����ϵͳ�ڴ治��ʱ��ͼƬ�Զ�������  
	          
	        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,newOpts);  

	        return bitmap;  
	    }  
	    
	    public static Bitmap compressImageFromFile(String srcPath) {  
	        BitmapFactory.Options newOpts = new BitmapFactory.Options();  
	        newOpts.inJustDecodeBounds = true;//ֻ����,��������  
	        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);  
	  
	        newOpts.inJustDecodeBounds = false;  
	        newOpts.inSampleSize = calculateInSampleSize(newOpts, 500, 500);//���ò�����  
	          
	        newOpts.inPreferredConfig = Config.ARGB_8888;//��ģʽ��Ĭ�ϵ�,�ɲ���  
	        newOpts.inPurgeable = true;// ͬʱ���òŻ���Ч  
	        newOpts.inInputShareable = true;//����ϵͳ�ڴ治��ʱ��ͼƬ�Զ�������  
	          
	        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);  
//	      return compressBmpFromBmp(bitmap);//ԭ���ķ������������������ͼ���ж���ѹ��  
	         
	        //��ʵ����Ч��,��Ҿ��ܳ���  
	        return bitmap;  
	    }  
	    private static int dp2px(Context context, float dp) {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (dp * scale + 0.5f);
		}
}
