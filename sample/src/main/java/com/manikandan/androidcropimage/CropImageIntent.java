package com.manikandan.androidcropimage;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.imagecropper.crop.CropImageView;

import java.io.IOException;

public class CropImageIntent extends FragmentActivity {
	
	private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
    private static final int ON_TOUCH = 1;

    String mCurrentPhotoPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.crop_image_intent);
		 
		 final CropImageView cropImageView = (CropImageView) findViewById(R.id.CropImageView);
		 
		 try {
			mCurrentPhotoPath = getIntent().getStringExtra("image");
			Log.v("PATH:", "PATH:" + mCurrentPhotoPath );
			 Bitmap bmp = Constants.decodeBitmapFromFile(mCurrentPhotoPath, 560, 560);
			 cropImageView.setImageBitmap(bmp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 cropImageView.setFixedAspectRatio(true);
		 cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);
		 cropImageView.setGuidelines(ON_TOUCH);
		 
		 
		 
		 final Button btnCloseIntent = (Button) findViewById(R.id.btnCloseIntent);
		 btnCloseIntent.setOnClickListener(new View.OnClickListener() {

	            @Override
	            public void onClick(View v) {
	                finish();
	            }
	        });
		 
		 final Button cropButton = (Button) findViewById(R.id.btnCropImage);
	        cropButton.setOnClickListener(new View.OnClickListener() {

	            @Override
	            public void onClick(View v) {
	            	try {
						Bitmap returnBmp = cropImageView.getCroppedImage();
						if(returnBmp.getWidth() > 560){
							Constants.bmp = Bitmap.createScaledBitmap(returnBmp, 560, 560, true);
						}
						Constants.bmp = returnBmp;
						
						returnBmp = null;
						
						RectF rect = cropImageView.getActualCropRect();
		                
		                Intent intent = new Intent();
		                intent.putExtra("l", rect.left);
		                intent.putExtra("r", rect.right);
		                intent.putExtra("t", rect.top);
		                intent.putExtra("b", rect.bottom);
		                
		        		setResult(Activity.RESULT_OK, intent);
		        		finish();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	                
	            }
	        });
	}
	
	public static Bitmap decodeBitmapFromFile(String filePath){
	    // First decode with inJustDecodeBounds=true to check dimensions
		
	    
	    Bitmap bmp = BitmapFactory.decodeFile(filePath);
	    
	    int orientation = 0;
	    try {
			ExifInterface exif = new ExifInterface(filePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	   
	    Matrix matrix = new Matrix();
	    switch (orientation) {
		    case ExifInterface.ORIENTATION_ROTATE_90:
		        matrix.postRotate(90);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_180:
		        matrix.postRotate(180);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_270:
		        matrix.postRotate(270);
		        break;
		    default:
		        break;
	    }
	    Bitmap bmRotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

	   
	    if (!bmRotated.equals(bmp)) {
	        if (!bmp.isRecycled()) {
	            bmp.recycle();
	        }
	        bmp = null;
	    }
	    
	    return bmRotated;     
	}
	
	
}
