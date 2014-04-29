package net.fhtagn.libsegdemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.fhtagn.libseg.SimpleMatter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

// Porter Duff modes visual :
// http://ssp.impulsetrain.com/porterduff.html
public class MatterView extends View {
    private final static String TAG = MatterView.class.getName();
    private static Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private static Paint mBitmapPaintAtop = new Paint(Paint.DITHER_FLAG);
    static {
        mBitmapPaintAtop.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
    }
    
    public static enum DrawMode {
        FOREGROUND,
        BACKGROUND
    }
    
    private DrawMode drawMode = DrawMode.FOREGROUND;
    
    private SimpleMatter mMatter;
    private Canvas mBgCanvas;
    private Canvas mFgCanvas;
    
    private Paint mCirclePaint;
    private Paint mFgLinePaint;
    private Paint mBgLinePaint;
    
    private ResultView mResultView;
    
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    
    public MatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeJoin(Paint.Join.MITER);
        mCirclePaint.setStrokeWidth(4f); 
        
        mFgLinePaint = createLinePaint(0xFF00FF00);
        mBgLinePaint = createLinePaint(0xFFFF0000);
    }
    
    private Paint createLinePaint(int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(20);
        return p;
    }
    
    public void setDrawMode(DrawMode dm) {
        drawMode = dm;
    }
    
    public void setImage(Bitmap bitmap, ResultView rv) { 
        final float widthRatio = bitmap.getWidth() / (float)getWidth();
        final float heightRatio = bitmap.getHeight() / (float)getHeight();
        final float ratio = Math.max(widthRatio, heightRatio);
        
        Log.i(TAG, "ratio :" + ratio);
        
        final int dstWidth = (int)(bitmap.getWidth() / ratio);
        final int dstHeight = (int)(bitmap.getHeight() / ratio);
        
        final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, dstWidth,
                dstHeight, true);
        
        mResultView = rv;
        mMatter = new SimpleMatter(scaled);
        rv.setMatter(mMatter);
        mBgCanvas = new Canvas(mMatter.bgScribbles);
        mBgCanvas.drawColor(Color.TRANSPARENT);
        mFgCanvas = new Canvas(mMatter.fgScribbles);
        mFgCanvas.drawColor(Color.TRANSPARENT);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mMatter != null) {
            canvas.drawBitmap(mMatter.image, 0, 0, mBitmapPaint);
            canvas.drawBitmap(mMatter.bgScribbles, 0, 0, mBitmapPaintAtop);
            canvas.drawBitmap(mMatter.fgScribbles, 0, 0, mBitmapPaintAtop);
        }
        
        switch (drawMode) {
            case FOREGROUND:
                canvas.drawPath(mPath, mFgLinePaint);
                break;
            case BACKGROUND:
                canvas.drawPath(mPath, mBgLinePaint);
                break;
        }
        canvas.drawPath(mCirclePath, mCirclePaint);
    }
    
    private void updateMatter() {
        // TODO: We might want to rate-limit, especially if we have lots of
        // call to updateMatter in a short period.
        // Basically, we should really call updateMatter 300ms after the last
        // call to updateMatter if nothing happened in-between
        // TODO: Look at ScheduledThreadExecutor
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Update matter");
                mMatter.updateMatter();
                if (mResultView != null) {
                    Log.i(TAG, "Updating resultview");
                    mResultView.postInvalidate();
                }
            }
        });
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
        
        switch(drawMode) {
            case BACKGROUND:
                mBgCanvas.drawPath(mPath, mBgLinePaint);
                break;
            case FOREGROUND:
                mFgCanvas.drawPath(mPath, mFgLinePaint);
                break;
        }
        mPath.reset();
        
        updateMatter();
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
