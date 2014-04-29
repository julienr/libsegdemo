package net.fhtagn.libsegdemo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

// ImageView with zoom and pan
// https://github.com/sephiroth74/ImageViewZoom
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static final int SELECT_PHOTO = 1;
    
    private MatterView mMatterView;
    private ResultView mResultView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mMatterView = (MatterView)findViewById(R.id.matterview);
        mResultView = (ResultView)findViewById(R.id.resultview);
        
        final Uri defaultUri = Uri.parse("android.resource://"
                + this.getPackageName() + "/" + R.drawable.default_img);
        
        final RadioButton plusBtn = (RadioButton)findViewById(R.id.btn_plus);
        plusBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i(TAG, "plus click");
                mMatterView.setDrawMode(MatterView.DrawMode.FOREGROUND);
            }
        });
        final RadioButton minusBtn = (RadioButton)findViewById(R.id.btn_minus);
        minusBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i(TAG, "minus click");
                mMatterView.setDrawMode(MatterView.DrawMode.BACKGROUND);
            }
        });
        
        plusBtn.setChecked(true);
        mMatterView.setDrawMode(MatterView.DrawMode.FOREGROUND);
        
        ViewTreeObserver vto = mMatterView.getViewTreeObserver(); 
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
            @Override 
            public void onGlobalLayout() {
                // Note that we cannot reuse the same vto, have to get it again
                mMatterView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.i(TAG, "imageView : " + mMatterView.getWidth() + ", " + mMatterView.getHeight());
                Log.i(TAG, "Setting default image");
                setImage(defaultUri);
            } 
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    private void startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.load_img:
                startImagePicker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    // http://stackoverflow.com/questions/3647993/android-bitmaps-loaded-from-gallery-are-rotated-in-imageview
    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                null, null, null);
        
        if (cursor == null || cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    // http://stackoverflow.com/questions/3647993/android-bitmaps-loaded-from-gallery-are-rotated-in-imageview
    public static Bitmap getSampledOrientedBitmap(Context context, Uri uri,
                                                  int maxWidth, int maxHeight)
            throws IOException {
        InputStream is = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, o);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, uri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = o.outHeight;
            rotatedHeight = o.outWidth;
        } else {
            rotatedWidth = o.outWidth;
            rotatedHeight = o.outHeight;
        }
        
        is = context.getContentResolver().openInputStream(uri);
        
        o = new BitmapFactory.Options();
        o.inSampleSize = calculateInSampleSize(rotatedWidth,
                                               rotatedHeight,
                                               maxWidth,
                                               maxHeight);
        Bitmap bmp = BitmapFactory.decodeStream(is, null, o);
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), matrix, true);
        }
        Log.i(TAG, "Loaded bitmap : " + bmp.getWidth() + ", " + bmp.getHeight());
        return bmp;
    }
    
    // Calculate the largest inSampleSize value that is a power of 2 and
    // keeps both height and width larger than the requested height and width.
    public static int calculateInSampleSize(int width, int height,
                                            int maxWidth, int maxHeight) {
        int inSampleSize = 1;
        if (width > maxWidth || height > maxHeight) {
            final int halfWidth = width / 2;
            final int halfHeight = height / 2;
            
            while ((halfHeight / inSampleSize) > maxHeight &&
                   (halfWidth / inSampleSize) > maxWidth) {
                inSampleSize *= 2;
            }
        }
        Log.i(TAG, "Scaling bitmap " + width + " * " + height + " => sampleSize=" + inSampleSize);
        return inSampleSize;
    }
    
    // Same as calculateInSampleSize, but ensure width and height are LOWER
    // than requested (so the image fully fits)
    public static int calculateInSampleSizeFitFully(int width, int height,
            int maxWidth, int maxHeight) {
        int inSampleSize = 1;
        if (width > maxWidth || height > maxHeight) {
            while ((height / inSampleSize) > maxHeight ||
                   (width / inSampleSize) > maxWidth) {
                inSampleSize *= 2;
            }
        }
        Log.i(TAG, "Scaling bitmap " + width + " * " + height
                + " => sampleSize=" + inSampleSize);
        return inSampleSize;
    }
    
    private void setImage(Uri uri) {
        Log.i(TAG, "imageUri : " + uri.toString());
        try {
            final Bitmap bitmap = getSampledOrientedBitmap(this,
                                                           uri,
                                                           mMatterView.getWidth(),
                                                           mMatterView.getHeight()); 
            mMatterView.setImage(bitmap, mResultView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = data.getData();
                    setImage(imageUri);
                }
                break;
        }
    }
}
