package com.bogdanorzea.happyhome.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Utility implements Parcelable{
    public String id;
    public String name;
    public String location;
    public String company_name;
    public String company_website;
    public String home_id;

    public Utility() {
    }

    protected Utility(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        company_name = in.readString();
        company_website = in.readString();
        home_id = in.readString();
    }

    public static final Creator<Utility> CREATOR = new Creator<Utility>() {
        @Override
        public Utility createFromParcel(Parcel in) {
            return new Utility(in);
        }

        @Override
        public Utility[] newArray(int size) {
            return new Utility[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(company_name);
        dest.writeString(company_website);
        dest.writeString(home_id);
    }
}
