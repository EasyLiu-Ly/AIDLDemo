package com.easyliu.demo.aidlremotedemo;

/**
 * Created by LiuYi on 2016/6/19.
 */

import android.os.Parcel;
import android.os.Parcelable;

public final class Rect implements Parcelable {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public static final Parcelable.Creator<Rect> CREATOR = new
            Parcelable.Creator<Rect>() {
                public Rect createFromParcel(Parcel in) {
                    return new Rect(in);
                }

                public Rect[] newArray(int size) {
                    return new Rect[size];
                }
            };

    private Rect(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        left = in.readInt();
        top = in.readInt();
        right = in.readInt();
        bottom = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(left);
        dest.writeInt(top);
        dest.writeInt(right);
        dest.writeInt(bottom);
    }
}
