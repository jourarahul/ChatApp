package com.example.rahul.chatapp;

/**
 * Created by Rahul on 9/2/2018.
 */

public class Users {

    private String Image;
    private String name;
    private String status;
    private String thumb_image;
    Users()
    {

    }

    public Users(String image, String name, String status,String thumb_image) {
        this.Image = image;
        this.name = name;
        this.status = status;
        this.thumb_image=thumb_image;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
