package weatheranim;

import android.content.Context;
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

public class TaiYangTypeImpl extends BaseType {

    Context context;

    // 背景
    private Drawable mBackground;
    // 雨滴集合
    private RainHolder mRain;
    // 画笔
    private Paint mPaint;

    public TaiYangTypeImpl(Context context, DynamicWeatherView dynamicWeatherView) {
        super(context, dynamicWeatherView);
        this.context = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setColor(Color.WHITE);
        // 这里雨滴的宽度统一为3
        mPaint.setStrokeWidth(3);
    }

    @Override
    public void generate() {
        mBackground = context.getResources().getDrawable(R.drawable.rain_sky_day);
        mBackground.setBounds(0, 0, getWidth(), getHeight());
        mRain = new RainHolder(
                getRandom( getWidth()-180, getWidth()-150),
                dp2px(110),
                dp2px(50),
                getRandom(dp2px(1), dp2px(2)),
                getRandom(20, 100));

    }

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
        mPaint.setAlpha(mRain.a);
        mPaint.setTextSize(50);
        canvas.drawCircle(mRain.x, mRain.y, mRain.size, mPaint);

//        mRain.y += mRain.s;
//        if (mRain.y > getHeight()) {
//            mRain.y = -mRain.size;
//
//        }
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

        public RainHolder(int x, int y, int size, int s, int a) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.s = s;
            this.a = a;
        }

    }

}
