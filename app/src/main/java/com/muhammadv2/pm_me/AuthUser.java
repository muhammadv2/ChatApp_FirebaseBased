package com.muhammadv2.pm_me;

class AuthUser {

    private String uid, name, imageUrl;

    public AuthUser() {
        //Firebase require an empty constructor to be able to use the model
    }

    public AuthUser(String uid, String name, String imageUrl) {
        this.uid = uid;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
