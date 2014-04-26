package net.fhtagn.libsegdemo;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

// ImageView with zoom and pan
// https://github.com/sephiroth74/ImageViewZoom
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    private static final int SELECT_PHOTO = 1;
    
    private ImageView mImageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mImageView = (ImageView)findViewById(R.id.imageview);
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
    
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth,
                                            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    
    
    public Bitmap decodeSampledBitmap(Uri imageUri, int maxWidth, int maxHeight) throws FileNotFoundException {
        Log.i(TAG, "Scaling bitmap to max " + maxWidth + " * " + maxHeight);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream stream = getContentResolver().openInputStream(imageUri);
        BitmapFactory.decodeStream(stream, null, options);
        
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        
        stream = getContentResolver().openInputStream(imageUri);
        return BitmapFactory.decodeStream(stream, null, options);
    }
    
    private void setImage(Bitmap image) {
        // Recycle current bitmap to avoid out of memory errors
        BitmapDrawable curr = (BitmapDrawable)mImageView.getDrawable();
        if (curr != null && curr.getBitmap() != null) {
            curr.getBitmap().recycle();
        }
        
        mImageView.setImageBitmap(image);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        final Bitmap bitmap = decodeSampledBitmap(imageUri,
                                                                  mImageView.getWidth(),
                                                                  mImageView.getHeight()); 
                        setImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
