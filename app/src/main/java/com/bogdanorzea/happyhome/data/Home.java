package com.bogdanorzea.happyhome.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Home implements Parcelable {
    public static final Creator<Home> CREATOR = new Creator<Home>() {
        @Override
        public Home createFromParcel(Parcel in) {
            return new Home(in);
        }

        @Override
        public Home[] newArray(int size) {
            return new Home[size];
        }
    };
    public String id;
    public String user_id;
    public String location;
    public String name;

    protected Home(Parcel in) {
        id = in.readString();
        user_id = in.readString();
        location = in.readString();
        name = in.readString();
    }

    public Home() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(user_id);
        dest.writeString(location);
        dest.writeString(name);
    }
}
