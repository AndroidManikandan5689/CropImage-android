package com.manikandan.androidcropimage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {



    private Context mContext;
    private static final String TAG = "Dashboard";

    //Capture Image and Pick Gallery Image
    private static final int REQUEST_CAMERA = 112;
    private static final int REQUEST_PHOTO_LIBRARY = 111;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int ACTION_TAKE_PHOTO_CAMERA = 1;
    private static final int ACTION_TAKE_PHOTO_DEVICE = 4;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private String mCurrentPhotoPath;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    Bitmap myPhoto;
    private static final int REQ_WIDTH = 100;
    private static final int REQ_HEIGHT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
    }

    public void openCamera(View v)
    {
        if (permissionCamera())
            dispatchTakePictureIntent(ACTION_TAKE_PHOTO_CAMERA);
    }

    public void openGallery(View v)
    {
        if (permissionPhoto())
            dispatchTakePictureIntent(ACTION_TAKE_PHOTO_DEVICE);
    }


    private boolean permissionCamera() {
        if (Build.VERSION.SDK_INT >= 23) {
            if ( ActivityCompat.checkSelfPermission(mContext,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }

    }

    private boolean permissionPhoto() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PHOTO_LIBRARY);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&  grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_CAMERA);
                }
                break;
            case REQUEST_PHOTO_LIBRARY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_DEVICE);
                }
                break;
        }
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {

        } else {

            return;
        }

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_CAMERA:
                File f;
                Uri mediaFileUri;
                try {

                    f = setUpPhotoFile();

                    if(f != null){
                        mediaFileUri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", f);
                        mCurrentPhotoPath = f.getAbsolutePath();
                    }else{
                        f = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        mediaFileUri = getOutputMediaFileProviderUri(MEDIA_TYPE_IMAGE);
                    }

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaFileUri);

                } catch (Exception e) {
                    e.printStackTrace();
                    mCurrentPhotoPath = null;
                    //mCurrentPhotoUri = null;
                }
                break;
            case ACTION_TAKE_PHOTO_DEVICE:
                try{
                    takePictureIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    takePictureIntent.setType("image/*");
                }catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        if(f == null){
            return null;
        }
        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir(mAlbumStorageDirFactory);
        if(albumF == null){
            return null;
        }
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }

    public File getAlbumDir(AlbumStorageDirFactory mAlbumStorageDirFactory) {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getString(R.string.app_name));
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }


    private Uri getOutputMediaFileProviderUri(int type){
        return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Bauwow");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Zaffee", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_CANCELED){
            finish();
            return;
        }
        switch (requestCode) {

            case ACTION_TAKE_PHOTO_CAMERA: {
                if (resultCode == RESULT_OK) {
                    if(mCurrentPhotoPath == null){
                        mCurrentPhotoPath = ImageFilePath.getPath(this, data.getData());
                    }

                    openCropIntent();
                }
                break;
            }
            case ACTION_TAKE_PHOTO_DEVICE:{
                if (resultCode == RESULT_OK) {
                    Uri chosenImageUri;
                    try
                    {
                        if(data != null){
                            chosenImageUri = data.getData();

                            mCurrentPhotoPath = ImageFilePath.getPath(this, chosenImageUri);

                            openCropIntent();

                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                break;
            }
            case CROP_FROM_CAMERA:
                handleBigCameraPhoto();

                break;
            default:{
                finish();
            }
        }
    }


    private void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {

            try {
                ImageView iv_image = findViewById(R.id.iv_image);
                try {
                    myPhoto = Constants.bmp;

                    iv_image.setImageBitmap(myPhoto);
                    Constants.bmp = null;

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        myPhoto = Constants.decodeBitmapFromFile(mCurrentPhotoPath, REQ_WIDTH, REQ_HEIGHT);
                        Bitmap bitmap = Bitmap.createScaledBitmap(myPhoto, iv_image.getWidth(), iv_image.getHeight(), false);
                        iv_image.setImageBitmap(bitmap);
                        bitmap = null;
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
//                        global.alertStatus(mContext, "errore photo: " + e.toString(), "");
                        //finish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
//                global.alertStatus(mContext, "errore photo: " + e.toString(), "");
            }

            Log.v("PHOTO", mCurrentPhotoPath);
            //galleryAddPic();
            //mCurrentPhotoPath = null;
        }else{
            Log.e("Error", "");
//            global.alertStatus(mContext, "Error to load photo", "");
        }

    }

    private void openCropIntent(){
        if (mCurrentPhotoPath != null) {
            Intent intent = new Intent(MainActivity.this, CropImageIntent.class);
            intent.putExtra("image", mCurrentPhotoPath);
            startActivityForResult(intent, CROP_FROM_CAMERA);
        }else{
            Log.e("Error", "");
//            global.alertStatus(mContext, "errore photo: take camera", "");
        }
    }
}
