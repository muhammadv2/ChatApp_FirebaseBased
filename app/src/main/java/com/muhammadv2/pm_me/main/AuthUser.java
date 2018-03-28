package com.muhammadv2.pm_me.main;

class AuthUser {

    private String name, imageUrl;

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
}
