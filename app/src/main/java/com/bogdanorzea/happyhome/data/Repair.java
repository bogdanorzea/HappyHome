package com.bogdanorzea.happyhome.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Repair implements Parcelable {
    public String id;
    public String home_id;
    public String name;
    public String description;
    public String location;
    public String image_uri;
    public Double cost;
    public boolean fixed;

    public Repair() {

    }

    protected Repair(Parcel in) {
        id = in.readString();
        home_id = in.readString();
        name = in.readString();
        description = in.readString();
        location = in.readString();
        image_uri = in.readString();
        if (in.readByte() == 0) {
            cost = null;
        } else {
            cost = in.readDouble();
        }
        fixed = in.readByte() != 0;
    }

    public static final Creator<Repair> CREATOR = new Creator<Repair>() {
        @Override
        public Repair createFromParcel(Parcel in) {
            return new Repair(in);
        }

        @Override
        public Repair[] newArray(int size) {
            return new Repair[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(home_id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(location);
        dest.writeString(image_uri);
        if (cost == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(cost);
        }
        dest.writeByte((byte) (fixed ? 1 : 0));
    }
}
