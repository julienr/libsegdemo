package net.fhtagn.libsegdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MatterView extends View {
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    
    // The original image
    private Bitmap mImageBitmap;
    private Bitmap mScribbles;
    
    public MatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setImage(Bitmap bitmap) {
        mImageBitmap = bitmap;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mImageBitmap != null) {
            canvas.drawBitmap(mImageBitmap, 0, 0, mBitmapPaint);
        }
    }


}
