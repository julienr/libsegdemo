package net.fhtagn.libsegdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MatterView extends View {
    private final static String TAG = MatterView.class.getName();
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    
    // The original image
    private Bitmap mImageBitmap;
    private Bitmap mScribblesBitmap;
    private Canvas mScribblesCanvas;
    
    private Paint mCirclePaint;
    private Paint mLinePaint;
    
    public MatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeJoin(Paint.Join.MITER);
        mCirclePaint.setStrokeWidth(4f); 
        
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mLinePaint.setColor(0xFFFF0000);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(20);
    }
    
    public void setImage(Bitmap bitmap) {
        final float widthRatio = bitmap.getWidth() / (float)getWidth();
        final float heightRatio = bitmap.getHeight() / (float)getHeight();
        final float ratio = Math.max(widthRatio, heightRatio);
        
        Log.i(TAG, "ratio :" + ratio);
        
        final int dstWidth = (int)(bitmap.getWidth() / ratio);
        final int dstHeight = (int)(bitmap.getHeight() / ratio);
        
        mImageBitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight,
                true);
        
        mScribblesBitmap = Bitmap.createBitmap(mImageBitmap.getWidth(),
                mImageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mScribblesCanvas = new Canvas(mScribblesBitmap);
        mScribblesCanvas.drawColor(Color.TRANSPARENT);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mImageBitmap != null) {
            canvas.drawBitmap(mImageBitmap, 0, 0, mBitmapPaint);
        }
        if (mScribblesBitmap != null) {
            canvas.drawBitmap(mScribblesBitmap, 0, 0, mBitmapPaint);
        }
        
        canvas.drawPath(mPath, mLinePaint);
        canvas.drawPath(mCirclePath, mCirclePaint);
    }
    
    // http://stackoverflow.com/questions/16650419/draw-in-canvas-by-finger-android
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private Path mPath = new Path();
    // Just a drawing indicator
    private Path mCirclePath = new Path();
    
    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            mCirclePath.reset();
            mCirclePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }
    
    private void touchUp() {
        mPath.lineTo(mX, mY);
        mCirclePath.reset();
        mScribblesCanvas.drawPath(mPath, mLinePaint);
        mPath.reset();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }


}
