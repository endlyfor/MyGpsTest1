package ylybbs.study.mygpstest;

import java.util.List;

import android.content.Context;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SatellitesView extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String LOG_TAG = "SatellitesView";

	 /** The thread that actually draws the animation */
    private DrawCompassThread thread;
	private int count=0;
	private DrawSatellitesThread thread1;
 

	public SatellitesView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setKeepScreenOn(true);
		SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        // create thread only; it's started in surfaceCreated()
        thread = new DrawCompassThread(holder, context);
		thread1=new DrawSatellitesThread(holder,context);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		thread.setSurfaceSize(width, height);
		thread1.setSurfaceSize(width,height);

	}
	
    public DrawCompassThread getThread() {
        return thread;
    }



	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
		thread1.setRunning(true);
		thread1.start();
		thread.start();
		Log.i(LOG_TAG,""+count);
       count++;
	}
	
	
	
	public void repaintSatellites(List<GpsSatellite> satellites,float direction){
		thread.repaintSatellites(satellites);
		thread.repaintDirection(direction);
	}

	public void repaintDirection(List<GpsSatellite> satellites,float direction){
		Log.i(LOG_TAG,"repaintDirection");
		thread1.repaintDirection(direction);
		thread1.repaintSatellites(satellites);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		 // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
		thread1.setRunning(false);
        while (retry) {

                thread.interrupt();
			    thread1.interrupt();

                retry = false;

        }
	}
}
