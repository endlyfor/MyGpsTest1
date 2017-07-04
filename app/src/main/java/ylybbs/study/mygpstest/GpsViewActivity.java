package ylybbs.study.mygpstest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

public class GpsViewActivity extends Activity {
	private int minTime = 1000;
	private int minDistance = 0;
	private static final String TAG = "GpsView";

	private LocationManager locationManager;
	private SatellitesView satellitesView;
	private TextView lonlatText;
	private TextView gpsStatusText;
	private SensorManager mSensorManager;
	public  float direction=0.0f;
	private int  count=0;
	ArrayList<GpsSatellite> satelliteList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps_view_activity);

		gpsStatusText = (TextView) findViewById(R.id.gps_status_text);
		lonlatText = (TextView) findViewById(R.id.lonlat_text);
		satellitesView = (SatellitesView) findViewById(R.id.satellitesView);

		registerListener();

	}
    /**
     * 注册监听
     */
	private void registerListener() {
		if (locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		//侦听位置信息(经纬度变化)
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				minTime, minDistance, locationListener);
		// 侦听GPS状态，主要是捕获到的各个卫星的状态
		locationManager.addGpsStatusListener(gpsStatusListener);
		//TODO:考虑增加监听传感器中的方位数据，以使罗盘的北能自动指向真实的北向

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// Use the legacy orientation sensors
		Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (sensor != null) {
			mSensorManager.registerListener(mOrientationSensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);

		}

	}
    /**
     * 移除监听
     */
	private void unregisterListener() {
		if (locationManager != null) {
			locationManager.removeGpsStatusListener(gpsStatusListener);
			locationManager.removeUpdates(locationListener);
		}
		mSensorManager.unregisterListener(mOrientationSensorEventListener);
	}
    /**
     * 坐标位置监听
     */
	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			StringBuffer sb = new StringBuffer();
			int fmt = Location.FORMAT_DEGREES;
			sb.append(Location.convert(location.getLongitude(), fmt));
			sb.append(" ");
			sb.append(Location.convert(location.getLatitude(), fmt));
			lonlatText.setText(sb.toString());

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			gpsStatusText.setText("onStatusChanged");

		}

		@Override
		public void onProviderEnabled(String provider) {
			gpsStatusText.setText("onProviderEnabled");

		}

		@Override
		public void onProviderDisabled(String provider) {
			gpsStatusText.setText("onProviderDisabled");

		}

	};

	/**
	 * Sensor状态监听
	 */
	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(Math.abs(event.values[0]-direction)>10.0f){
				Log.i(TAG,""+Math.abs(event.values[0]-direction));
				direction = event.values[0];
				satellitesView.repaintDirection( satelliteList ,direction);
				Log.i(TAG,"onsensorchanged"+count);
				count++;
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
    /**
     * Gps状态监听
     */
	private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			GpsStatus gpsStatus = locationManager.getGpsStatus(null);
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX: {
				gpsStatusText.setText("GPS_EVENT_FIRST_FIX");
				// 第一次定位时间UTC gps可用
				// Log.v(TAG,"GPS is usable");
				int i = gpsStatus.getTimeToFirstFix();
				break;
			}

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {// 周期的报告卫星状态
				// 得到所有收到的卫星的信息，包括 卫星的高度角、方位角、信噪比、和伪随机号（及卫星编号）
				Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();

				 satelliteList = new ArrayList<GpsSatellite>();
				ArrayList<Parcelable> st=new ArrayList<Parcelable>();

				for (GpsSatellite satellite : satellites) {
					// 包括 卫星的高度角、方位角、信噪比、和伪随机号（及卫星编号）
					/*
					 * satellite.getElevation(); //卫星仰角
					 * satellite.getAzimuth();   //卫星方位角 
					 * satellite.getSnr();       //信噪比
					 * satellite.getPrn();       //伪随机数，可以认为他就是卫星的编号
					 * satellite.hasAlmanac();   //卫星历书 
					 * satellite.hasEphemeris();
					 * satellite.usedInFix();
					 */
					satelliteList.add(satellite);
					st.add(new satelliteStatus(satellite.getElevation(),satellite.getAzimuth(),satellite.getSnr(),satellite.getPrn()));
				}

				satellitesView.repaintSatellites(satelliteList,direction);
				gpsStatusText.setText("GPS_EVENT_SATELLITE_STATUS:"
						+ satelliteList.size());
				Intent intent=new Intent();
				intent.setAction("GpsStatus");
				Bundle bundle=new Bundle();


				bundle.putParcelableArrayList("satellites",st);

				sendBroadcast(intent);
				break;
			}

			case GpsStatus.GPS_EVENT_STARTED: {
				gpsStatusText.setText("GPS_EVENT_STARTED");
				break;
			}

			case GpsStatus.GPS_EVENT_STOPPED: {
				gpsStatusText.setText("GPS_EVENT_STOPPED");
				break;
			}

			default:
				gpsStatusText.setText("GPS_EVENT:" + event);
				break;
			}
		}
	};

	/**
	 * Returns the Global Navigation Satellite System (GNSS) for a satellite given the PRN.  For
	 * Android 6.0.1 (API Level 23) and lower.  Android 7.0 and higher should use
	 *
	 * @param prn PRN value provided by the GpsSatellite.getPrn() method
	 * @return GnssType for the given PRN
	 */
	public static GnssType getGnssType(int prn) {
		if (prn >= 65 && prn <= 96) {
			// See Issue #26 for details
			return GnssType.GLONASS;
		} else if (prn >= 193 && prn <= 200) {
			// See Issue #54 for details
			return GnssType.QZSS;
		} else if (prn >= 201 && prn <= 235) {
			// See Issue #54 for details
			return GnssType.BEIDOU;
		} else if (prn >= 301 && prn <= 330) {
			// See https://github.com/barbeau/gpstest/issues/58#issuecomment-252235124 for details
			return GnssType.GALILEO;
		} else {
			// Assume US NAVSTAR for now, since we don't have any other info on sat-to-PRN mappings
			return GnssType.NAVSTAR;
		}
	}


	/**
	 * Converts screen dimension units from dp to pixels, based on algorithm defined in
	 * http://developer.android.com/guide/practices/screens_support.html#dips-pels
	 *
	 * @param dp value in dp
	 * @return value in pixels
	 */
	public static int dpToPixels(Context context, float dp) {
		// Get the screen's density scale
		final float scale = context.getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (dp * scale + 0.5f);
	}




//

	@Override
	protected void onResume() {
		super.onResume();
		registerListener();
	}

	@Override
	protected void onDestroy() {
		unregisterListener();
		super.onDestroy();
	}

}
