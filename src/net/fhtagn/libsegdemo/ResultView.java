package net.fhtagn.libsegdemo;

import net.fhtagn.libseg.SimpleMatter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.View;

public class ResultView extends View {
    public final static String TAG = ResultView.class.getName();
    
    private static Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private static Paint mBitmapPaintMask = new Paint(Paint.DITHER_FLAG);
    static {
        mBitmapPaintMask.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }
    
    private SimpleMatter mMatter;
    
    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    void setMatter(SimpleMatter matter) {
        mMatter = matter;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mMatter != null) {
            canvas.drawBitmap(mMatter.image, 0, 0, mBitmapPaint);
            canvas.drawBitmap(mMatter.finalMask, 0, 0, mBitmapPaintMask);
        }
    }

}
