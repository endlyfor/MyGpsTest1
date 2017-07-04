package ylybbs.study.mygpstest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fritt on 2017/6/29.
 */

public class satelliteStatus implements Parcelable {
    private float mElevation;
    private float mAzimuth;
    private float mSnr;
    private int mPrn;
  //  private boolean musedInFix;

    public satelliteStatus(float elevation, float azimuth, float snr, int prn) {
        mElevation = elevation;
        mAzimuth = azimuth;
        mSnr = snr;
        mPrn = prn;

    }

    public float getElevation() {

        return mElevation;
    }

    public void setElevation(float elevation) {
        mElevation = elevation;
    }

    public float getAzimuth() {
        return mAzimuth;
    }

    public void setAzimuth(float azimuth) {
        mAzimuth = azimuth;
    }

    public float getSnr() {
        return mSnr;
    }

    public void setSnr(float snr) {
        mSnr = snr;
    }

    public int getPrn() {
        return mPrn;
    }

    public void setPrn(int prn) {
        mPrn = prn;
    }



    protected satelliteStatus(Parcel in) {
       mElevation=in.readFloat();
        mAzimuth=in.readFloat();
        mSnr=in.readFloat();
        mPrn=in.readInt();

    }

    public static final Creator<satelliteStatus> CREATOR = new Creator<satelliteStatus>() {
        @Override
        public satelliteStatus createFromParcel(Parcel in) {
            return new satelliteStatus(in.readFloat(),in.readFloat(),in.readFloat(),in.readInt());
        }

        @Override
        public satelliteStatus[] newArray(int size) {
            return new satelliteStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(getElevation());
        dest.writeFloat(getAzimuth());
        dest.writeFloat(getSnr());
        dest.writeInt(getPrn());
    }
}
