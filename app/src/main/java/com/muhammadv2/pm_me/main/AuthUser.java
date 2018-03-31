package com.muhammadv2.pm_me.main;


import android.os.Parcel;
import android.os.Parcelable;

public class AuthUser implements Parcelable {

    private String uid, name, imageUrl;

    public AuthUser() {
        //Firebase require an empty constructor to be able to use the model
    }

    public AuthUser(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    protected AuthUser(Parcel in) {
        uid = in.readString();
        name = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<AuthUser> CREATOR = new Creator<AuthUser>() {
        @Override
        public AuthUser createFromParcel(Parcel in) {
            return new AuthUser(in);
        }

        @Override
        public AuthUser[] newArray(int size) {
            return new AuthUser[size];
        }
    };

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(name);
        parcel.writeString(imageUrl);
    }
}
