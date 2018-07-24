package com.bogdanorzea.happyhome.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Reading implements Parcelable {
    public static final Creator<Reading> CREATOR = new Creator<Reading>() {
        @Override
        public Reading createFromParcel(Parcel in) {
            return new Reading(in);
        }

        @Override
        public Reading[] newArray(int size) {
            return new Reading[size];
        }
    };
    public String id;
    public String meter_id;
    public String date;
    public Double value;

    public Reading() {

    }

    protected Reading(Parcel in) {
        id = in.readString();
        meter_id = in.readString();
        date = in.readString();
        value = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(meter_id);
        dest.writeString(date);
        dest.writeDouble(value);
    }
}
