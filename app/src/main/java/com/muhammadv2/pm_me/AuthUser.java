package com.muhammadv2.pm_me;

class AuthUser {

    private String name, imageUrl;

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
