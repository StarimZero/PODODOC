package com.pododoc.app;

public class ReviewVO {
    private String id;
    private int index;
    private String date;
    private float rating;
    private String contents;
    private String email;
    private String photo;

    @Override
    public String toString() {
        return "ReviewVO{" +
                "id='" + id + '\'' +
                ", index=" + index +
                ", date='" + date + '\'' +
                ", rating=" + rating +
                ", contents='" + contents + '\'' +
                ", email='" + email + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
