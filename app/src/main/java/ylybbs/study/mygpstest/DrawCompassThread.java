package ylybbs.study.mygpstest;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.location.GpsSatellite;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateInterpolator;

public class DrawCompassThread extends Thread {
    // 卫星图
//    private Bitmap satelliteBitmap;
    private Bitmap compassBitmap;

    private Paint paint;

    /**
     * Handle to the surface manager object we interact with
     */
    private SurfaceHolder surfaceHolder;

    /**
     * Indicate whether the surface has been created & is ready to draw
     */
    private boolean isRunning = false;
    private int cx = 0;
    private int cy = 0;
    private int compassRadius = 434 / 2;
    private final float MAX_ROATE_DEGREE = 1.0f;
    private AccelerateInterpolator mInterpolator;
    float mTargetDirection ;
    float direction = 0.0f;
    int countT = 0;
    int countP = 0;
    private static int SAT_RADIUS;
    private Paint mHorizonActiveFillPaint, mHorizonInactiveFillPaint, mHorizonStrokePaint,
            mGridStrokePaint,
            mSatelliteFillPaint, mSatelliteStrokePaint, mSatelliteUsedStrokePaint,
            mNorthPaint, mNorthFillPaint, mPrnIdPaint, mNotInViewPaint,mSatellitesTextPaint;
    private int[] mBarColors = new int[]{Color.RED,Color.rgb(255, 97, 0), Color.rgb(56, 94, 15), Color.YELLOW, Color.GREEN, Color.BLUE,  Color.MAGENTA};
    private final float mSnrThresholds[]={10,20,30,40,45,48};


    PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0,
            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public static final LinkedBlockingQueue<List<GpsSatellite>> queue =
            new LinkedBlockingQueue<List<GpsSatellite>>(60);

    public static final LinkedBlockingQueue<String> queue1 =
            new LinkedBlockingQueue<String>(600);


    public DrawCompassThread(SurfaceHolder surfaceHolder, Context context) {
        this.surfaceHolder = surfaceHolder;
        Resources res = context.getResources();
        // cache handles to our key sprites & other drawables
        compassBitmap = BitmapFactory.decodeResource(res, R.drawable.compass);
        compassRadius = compassBitmap.getWidth() / 2;
        SAT_RADIUS=GpsViewActivity.dpToPixels(context,5);

    //    satelliteBitmap = BitmapFactory.decodeResource(res, R.drawable.satellite_mark);


        mSatellitesTextPaint = new Paint();
        // paint.setSubpixelText(true);
        mSatellitesTextPaint.setAntiAlias(true);
        mSatellitesTextPaint.setFilterBitmap(true);
        mSatellitesTextPaint.setColor(Color.WHITE);
        mSatellitesTextPaint.setTextSize(12);
        mSatellitesTextPaint.setTextAlign(Align.CENTER);

        mSatelliteStrokePaint = new Paint();
        mSatelliteStrokePaint.setColor(Color.rgb(192,255,62));
        mSatelliteStrokePaint.setStyle(Paint.Style.STROKE);
        mSatelliteStrokePaint.setStrokeWidth(1f);
        mSatelliteStrokePaint.setAntiAlias(true);

        mSatelliteUsedStrokePaint = new Paint();
        mSatelliteUsedStrokePaint.setColor(Color.rgb(192,255,62));
        mSatelliteUsedStrokePaint.setStyle(Paint.Style.STROKE);
        mSatelliteUsedStrokePaint.setStrokeWidth(3f);
        mSatelliteUsedStrokePaint.setAntiAlias(true);

        mSatelliteFillPaint = new Paint();
        mSatelliteFillPaint.setColor(Color.RED);
        mSatelliteFillPaint.setStyle(Paint.Style.FILL);
        mSatelliteFillPaint.setAntiAlias(true);
    }

    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        synchronized (surfaceHolder) {
            cx = width / 2;
            cy = height / 2;
        }
    }

    @Override
    public void run() {
        List<GpsSatellite> list = null;
        Canvas c = null;


        try {


            c = surfaceHolder.lockCanvas(null);
            //初始化画板的中心坐标
            cx = c.getWidth() / 2;
            cy = c.getWidth() / 2;
            synchronized (surfaceHolder) {
                doDrawC(c,direction);

            }
        } finally {
            if (c != null) {
                surfaceHolder.unlockCanvasAndPost(c);
            }
        }






        while (isRunning) {
//            try {// 睡眠一段时间
//                Thread.sleep(500);
//            } catch (Exception e) {
//                e.printStackTrace();
//           }
//            if(queue.poll()==null&&queue1.poll()==null  ){
//                Log.i("DrawCompassThread", "1");
//                continue;
//            }
//            if (queue.poll() != null) {
//                list = queue.poll();
//                Log.i("DrawCompassThread", "2");
//            }
//            if (queue1.poll() != null) {
//                mTargetDirection = Float.valueOf(queue1.poll());
//                //Log.i("DrawCompassThread","T"+mTargetDirection+" "+countT);
//                countT++;
//                Log.i("DrawCompassThread", "3");
//            }
            try{
                list = queue.take();
                mTargetDirection = Float.valueOf(queue1.take());
            }catch (InterruptedException e){
                e.printStackTrace();
            }


            Log.i("DrawCompassThread", "dire" + direction + countP);

            if (direction != mTargetDirection) {

                // calculate the short routine
                float to = mTargetDirection;
                if (to - direction > 180) {
                    to -= 360;
                } else if (to - direction < -180) {
                    to += 360;
                }

                // limit the max speed to MAX_ROTATE_DEGREE
                float distance = to - direction;
                if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                    distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
                }

                // need to slow down if the distance is short
//				direction = normalizeDegree(direction
//						+ ((to - direction) * mInterpolator.getInterpolation(Math
//						.abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
                direction = mTargetDirection;
            }
                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        doDrawC(c, mTargetDirection);
                        Log.i("DrawCompassThread", "mtd" + mTargetDirection + "" + countP);
                        countP++;
                        doDrawS(c, list, mTargetDirection);
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                        //currentThread().interrupt();
                    }
                }

        }


    }


    public void setRunning(boolean b) {
        isRunning = b;
    }

    public void repaintSatellites(List<GpsSatellite> list) {
        synchronized (surfaceHolder) {
            try {
                queue.put(list);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void repaintDirection(float direction) {
        synchronized (surfaceHolder) {
            try {
                queue1.put(String.valueOf(direction));
                Log.i("DrawCompassThread", "P" + mTargetDirection + "" + countT);
                countT++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 绘制背景罗盘
     *
     * @param canvas
     * @param cx     罗盘中心点位于画布上的X坐标
     * @param cy     罗盘中心点位于画布上的Y坐标
     * @param r      罗盘的半径
     */
    private void drawBackground(Canvas canvas, int cx, int cy, int r) {
        int x = cx - r;
        int y = cy - r;
        canvas.drawBitmap(compassBitmap, x, y, paint);
    }

    private void drawBackground(Canvas canvas, Matrix m) {


        canvas.drawBitmap(compassBitmap, m, paint);
    }


    /**
     * 将角度转换为弧度，以用于三角函数的运算
     *
     * @param degree
     * @return
     */
    private double degreeToRadian(double degree) {
        return (degree * Math.PI) / 180.0d;
    }

    /*
     * 将SNR的值，转化为通用的信号强度级别，主要用于在绘制卫星时，通过颜色来表明它的信号强度，暂时没用到
     * SNR is mapped to signal strength [0,1,4-9] COMMENT SNR: >500 >100 >50 >10
     * >5 >0 bad n/a COMMENT sig: 9 8 7 6 5 4 1 0 COMMENT
     */
    private int snrToSignalLevel(float snr) {
        int level = 0;
        if (snr >= 0 && snr < 5) {
            level = 4;
        } else if (snr >= 5 && snr < 10) {
            level = 5;
        } else if (snr >= 10 && snr < 50) {
            level = 6;
        } else if (snr >= 50 && snr < 100) {
            level = 7;
        } else if (snr >= 100 && snr < 500) {
            level = 8;
        } else if (snr >= 500) {
            level = 9;
        }
        return level;
    }

    /**
     * 在背景罗盘上绘制卫星
     *
     * @param canvas
     * @param satellite
     * @param cx        中心圆点的X座标
     * @param cy        中心圆点的Y座标
     * @param r         罗盘背景的半径
     */
    private void drawSatellite(Canvas canvas, GpsSatellite satellite, int cx, int cy, int r, float direction) {

        /**
         * GPS卫星导航仪通常选用仰角大于5º，小于85º。 因为当卫星仰角大于85º时，L1波段的电离层折射误差较大，故规定仰角大于85º时，
         * 定位无效，不进行数据更新。而卫星仰角越小，则对流层折射误差越大，故一般选用仰角大于5º的卫星来定位。
         */
        //得到卫星图标的半径
        int sr = SAT_RADIUS;
        //得到仰角
        float elevation = satellite.getElevation();
        //通过仰角，计算出这个卫星应该绘制到离圆心多远的位置，这里用的是角度的比值
        double r2 =(compassRadius-sr*2) * ((90.0f - elevation) / 90.0f);
        
		/*得到方位角（与正北向也就是Y轴顺时针方向的夹角，注意我们通常几何上的角度
         * 是与X轴正向的逆时针方向的夹角）,在计算X，Y座标的三角函数时，要做转换
         */
        double azimuth = satellite.getAzimuth();
        
		/*
         * 转换成XY座标系中的夹角,方位角是与正北向也就是Y轴顺时针方向的夹角，
         * 注意我们通常几何上的角度是与X轴正向的逆时针方向的夹角）,
         * 在计算X，Y座标的三角函数时，要做转换
         */
        double radian;
        if (direction == 0.0f) {
            radian = degreeToRadian(360 - azimuth + 90);
        } else {
            radian = degreeToRadian(360 + (azimuth - 90) - direction);
        }


        double x = cx + Math.cos(radian) * r2;
        double y = cy + Math.sin(radian) * r2;


//        //以x,y为中心绘制卫星图标
//        canvas.drawBitmap(satelliteBitmap, (float) (x - sr), (float) (y - sr), paint);
//        //在卫星图标的位置上绘出文字（卫星编号及信号强度）
//        int snr = (int) satellite.getSnr();
//        int signLevel = snrToSignalLevel(snr);  //暂时不用
//        String info = String.format("#%s_%s", satellite.getPrn(), snr);
//        canvas.drawText(info, (float) (x), (float) (y), paint);


        Paint fillPaint;
        if (satellite.getSnr() == 0.0f) {
            // Satellite can't be seen
            fillPaint = mNotInViewPaint;
        } else {
            // Calculate fill color based on signal strength
            fillPaint = getSatellitePaint(mSatelliteFillPaint, satellite.getSnr());
        }

        Paint strokePaint;
        if (satellite.usedInFix()) {
            strokePaint = mSatelliteUsedStrokePaint;
        } else {
            strokePaint = mSatelliteStrokePaint;
        }
        float textHeight=mSatellitesTextPaint.getFontMetricsInt().top-mSatellitesTextPaint.getFontMetricsInt().bottom;
       GnssType operator;
        operator=GpsViewActivity.getGnssType(satellite.getPrn());
        switch (operator) {
            case NAVSTAR:
                canvas.drawCircle((float)x,(float) y, SAT_RADIUS, fillPaint);
                canvas.drawCircle((float)x, (float)y, SAT_RADIUS, strokePaint);
                canvas.drawText(String.valueOf(satellite.getPrn()),(float)(x),(float)(y-SAT_RADIUS),mSatellitesTextPaint);
                break;
            case GLONASS:
                canvas.drawRect((float)(x - SAT_RADIUS), (float)(y - SAT_RADIUS), (float)(x + SAT_RADIUS),(float)( y + SAT_RADIUS),
                        fillPaint);
                canvas.drawRect((float)(x - SAT_RADIUS), (float)(y - SAT_RADIUS), (float)(x + SAT_RADIUS), (float)(y + SAT_RADIUS),
                        strokePaint);
                canvas.drawText(String.valueOf(satellite.getPrn()),(float)(x),(float)(y-SAT_RADIUS),mSatellitesTextPaint);
                break;
            case QZSS:
                drawTriangle(canvas, (float)x, (float)y, fillPaint, strokePaint);
                canvas.drawText(String.valueOf(satellite.getPrn()),(float)(x),(float)(y-SAT_RADIUS),mSatellitesTextPaint);
                break;
            case BEIDOU:
                drawPentagon(canvas, (float)x,(float) y, fillPaint, strokePaint);
                canvas.drawText(String.valueOf(satellite.getPrn()),(float)(x),(float)(y-SAT_RADIUS),mSatellitesTextPaint);
                break;
            case GALILEO:
                // We're running out of shapes - QZSS should be regional to Japan, so re-use triangle
                drawTriangle(canvas, (float)x,(float)y, fillPaint, strokePaint);
                canvas.drawText(String.valueOf(satellite.getPrn()),(float)(x),(float)(y-SAT_RADIUS),mSatellitesTextPaint);
                break;
        }




    }


    private void doDrawC(Canvas canvas, float direction) {
        if (canvas != null) {
            // 绘制背景罗盘
            Matrix m = new Matrix();
            // 设置旋转角度
            m.postRotate(
                    -direction,
                    compassBitmap.getWidth() / 2,
                    compassBitmap.getHeight() / 2);
            // 设置左边距和上边距
            m.postTranslate((canvas.getWidth() - compassRadius * 2) / 2, (canvas.getHeight() - compassRadius * 2) / 2);
            //drawBackground(canvas, cx, cy, compassRadius);
            drawBackground(canvas, m);

        }

    }


    private void doDrawS(Canvas canvas, List<GpsSatellite> satellites, float direction) {


        //绘制卫星分布
        if (satellites != null) {
            for (GpsSatellite satellite : satellites) {
                drawSatellite(canvas, satellite, canvas.getWidth() / 2, canvas.getHeight() / 2, compassRadius, mTargetDirection);
            }
        }
    }



    private Paint getSatellitePaint(Paint base, float snrCn0) {
        Paint newPaint;
        newPaint = new Paint(base);

        int numSteps;
        final float thresholds[];
        final int colors[];



            numSteps = mSnrThresholds.length;
            thresholds = mSnrThresholds;
            colors = mBarColors;


        if (snrCn0 <= thresholds[0]) {
            newPaint.setColor(colors[0]);
            return newPaint;
        }

        if (snrCn0 >= thresholds[numSteps - 1]) {
            newPaint.setColor(colors[numSteps - 1]);
            return newPaint;
        }

        for (int i = 0; i < numSteps - 1; i++) {
            float threshold = thresholds[i];
            float nextThreshold = thresholds[i + 1];
            if (snrCn0 >= threshold && snrCn0 <= nextThreshold) {
                int c1, r1, g1, b1, c2, r2, g2, b2, c3, r3, g3, b3;
                float f;

                c1 = colors[i];
                r1 = Color.red(c1);
                g1 = Color.green(c1);
                b1 = Color.blue(c1);

                c2 = colors[i + 1];
                r2 = Color.red(c2);
                g2 = Color.green(c2);
                b2 = Color.blue(c2);

                f = (snrCn0 - threshold) / (nextThreshold - threshold);

                r3 = (int) (r2 * f + r1 * (1.0f - f));
                g3 = (int) (g2 * f + g1 * (1.0f - f));
                b3 = (int) (b2 * f + b1 * (1.0f - f));
                c3 = Color.rgb(r3, g3, b3);

                newPaint.setColor(c3);

                return newPaint;
            }
        }

        newPaint.setColor(Color.MAGENTA);

        return newPaint;
    }



    private void drawTriangle(Canvas c, float x, float y, Paint fillPaint, Paint strokePaint) {
        float x1, y1;  // Top
        x1 = x;
        y1 = y - SAT_RADIUS;

        float x2, y2; // Lower left
        x2 = x - SAT_RADIUS;
        y2 = y + SAT_RADIUS;

        float x3, y3; // Lower right
        x3 = x + SAT_RADIUS;
        y3 = y + SAT_RADIUS;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x1, y1);
        path.close();

        c.drawPath(path, fillPaint);
        c.drawPath(path, strokePaint);
    }

    private void drawPentagon(Canvas c, float x, float y, Paint fillPaint, Paint strokePaint) {
        Path path = new Path();
        path.moveTo(x, y - SAT_RADIUS);
        path.lineTo(x - SAT_RADIUS, y - (SAT_RADIUS / 3));
        path.lineTo(x - 2 * (SAT_RADIUS / 3), y + SAT_RADIUS);
        path.lineTo(x + 2 * (SAT_RADIUS / 3), y + SAT_RADIUS);
        path.lineTo(x + SAT_RADIUS, y - (SAT_RADIUS / 3));
        path.close();

        c.drawPath(path, fillPaint);
        c.drawPath(path, strokePaint);
    }

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

}






