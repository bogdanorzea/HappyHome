package com.bogdanorzea.happyhome.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Meter implements Parcelable {
    public static final Creator<Meter> CREATOR = new Creator<Meter>() {
        @Override
        public Meter createFromParcel(Parcel in) {
            return new Meter(in);
        }

        @Override
        public Meter[] newArray(int size) {
            return new Meter[size];
        }
    };
    public String home_id;
    public String id;
    public String name;
    public String location;

    public Meter() {

    }

    protected Meter(Parcel in) {
        home_id = in.readString();
        id = in.readString();
        name = in.readString();
        location = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(home_id);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(location);
    }
}
