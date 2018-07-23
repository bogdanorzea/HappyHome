package com.bogdanorzea.happyhome.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Bill implements Parcelable {
    public static final Creator<Bill> CREATOR = new Creator<Bill>() {
        @Override
        public Bill createFromParcel(Parcel in) {
            return new Bill(in);
        }

        @Override
        public Bill[] newArray(int size) {
            return new Bill[size];
        }
    };
    public String id;
    public String utility_id;
    public String issue_date;
    public String due_date;
    public Double value;

    public Bill() {

    }

    protected Bill(Parcel in) {
        id = in.readString();
        utility_id = in.readString();
        issue_date = in.readString();
        due_date = in.readString();
        if (in.readByte() == 0) {
            value = null;
        } else {
            value = in.readDouble();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(utility_id);
        dest.writeString(issue_date);
        dest.writeString(due_date);
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(value);
        }
    }
}
