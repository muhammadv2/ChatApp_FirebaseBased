package com.muhammadv2.pm_me.main;

import android.os.Parcelable;

class AuthUser implements Parcelable {

    private String uid, name, imageUrl;

    public AuthUser() {
        //Firebase require an empty constructor to be able to use the model
    }

    public AuthUser(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.imageUrl);
        dest.writeString(this.uid);
    }

    protected AuthUser(android.os.Parcel in) {
        this.name = in.readString();
        this.imageUrl = in.readString();
        this.uid = in.readString();
    }

    public static final Parcelable.Creator<AuthUser> CREATOR = new Parcelable.Creator<AuthUser>() {
        @Override
        public AuthUser createFromParcel(android.os.Parcel source) {
            return new AuthUser(source);
        }

        @Override
        public AuthUser[] newArray(int size) {
            return new AuthUser[size];
        }
    };
}
