package weatheranim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.example.a10734.weatherdome.R;

import java.util.ArrayList;

/**
 * Author:      Melodyxxx
 * Email:       95hanjie@gmail.com
 * Created at:  17/03/06.
 * Description:
 */

public class XueTypeImpl extends BaseType {

    // 背景
    private Drawable mBackground;
    // 雨滴集合
    private ArrayList<RainHolder> mRains;
    // 画笔
    private Paint mPaint;

    public XueTypeImpl(Context context, DynamicWeatherView dynamicWeatherView) {
        super(context, dynamicWeatherView);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setColor(Color.WHITE);
        // 这里雨滴的宽度统一为3
        mPaint.setStrokeWidth(3);
        mRains = new ArrayList<>();
    }

    @Override
    public void generate() {
        mBackground = getContext().getResources().getDrawable(R.drawable.rain_sky_day);
        mBackground.setBounds(0, 0, getWidth(), getHeight());
        for (int i = 0; i < 60; i++) {
            RainHolder rain = new RainHolder(
                    getRandom(1, getWidth()),
                    getRandom(1, getHeight()),
                    getRandom(5,8),
                    getRandom(dp2px(1), dp2px(2)),
                    getRandom(20, 100)
            );
            mRains.add(rain);
        }
    }

    private RainHolder r;

    @Override
    public void onDraw(Canvas canvas) {
        clearCanvas(canvas);
        // 画背景
        if(mBackground == null){
            init();
            generate();
            mBackground.draw(canvas);

        }else {
            mBackground.draw(canvas);
        }
        // 画出集合中的雨点
        for (int i = 0; i < mRains.size(); i++) {
            r = mRains.get(i);
            mPaint.setAlpha(r.a);
            mPaint.setTextSize(50);
//            canvas.drawLine(r.x, r.y, r.x, r.y + r.l, mPaint);
//            canvas.drawCircle(r.x, r.y,5, mPaint);
            canvas.drawCircle(r.x, r.y,r.size, mPaint);


        }
//        for (int i = 0; i < 5; i++) {
//            r= mRains.get(i);
//            Paint p = new Paint();`
//            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.w00);
//            canvas.drawBitmap(bitmap, r.x, r.y, p);
//        }
        // 将集合中的点按自己的速度偏移
        for (int i = 0; i < mRains.size(); i++) {
            r = mRains.get(i);
            r.y += r.s;
            if (r.y > getHeight()) {
                r.y = -r.size;
            }
        }
    }

    private class RainHolder {
        /**
         * 雪花 x 轴坐标
         */
        int x;
        /**
         * 雪花 y 轴坐标
         */
        int y;
        /**
         * 雪花大小
         */
        int size;
        /**
         * 雪花移动速度
         */
        int s;
        /**
         * 雪花透明度
         */
        int a;

        public RainHolder(int x, int y, int size , int s, int a) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.s = s;
            this.a = a;
        }

    }

}
