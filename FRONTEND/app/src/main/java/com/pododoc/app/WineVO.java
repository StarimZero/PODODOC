package com.pododoc.app;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class WineVO implements Parcelable {
    private int index;
    private String wineName;
    private float wineRating;
    private int wineReviews;
    private String winePrice;
    private String wineLink;
    private String wineCountry;
    private String wineRegion;
    private String wineWinery;
    private String wineType;
    private String wineGrape;
    private String wineImage;
    private float body;
    private float texture;
    private float sweetness;
    private float acidity;
    private String flavor1;
    private String flavor2;
    private String flavor3;




    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getWineName() {
        return wineName;
    }

    public void setWineName(String wineName) {
        this.wineName = wineName;
    }

    public float getWineRating() {
        return wineRating;
    }

    public void setWineRating(float wineRating) {
        this.wineRating = wineRating;
    }

    public int getWineReviews() {
        return wineReviews;
    }

    public void setWineReviews(int wineReviews) {
        this.wineReviews = wineReviews;
    }

    public String getWinePrice() {
        return winePrice;
    }

    public void setWinePrice(String winePrice) {
        this.winePrice = winePrice;
    }

    public String getWineLink() {
        return wineLink;
    }

    public void setWineLink(String wineLink) {
        this.wineLink = wineLink;
    }

    public String getWineCountry() {
        return wineCountry;
    }

    public void setWineCountry(String wineCountry) {
        this.wineCountry = wineCountry;
    }

    public String getWineRegion() {
        return wineRegion;
    }

    public void setWineRegion(String wineRegion) {
        this.wineRegion = wineRegion;
    }

    public String getWineWinery() {
        return wineWinery;
    }

    public void setWineWinery(String wineWinery) {
        this.wineWinery = wineWinery;
    }

    public String getWineType() {
        return wineType;
    }

    public void setWineType(String wineType) {
        this.wineType = wineType;
    }

    public String getWineGrape() {
        return wineGrape;
    }

    public void setWineGrape(String wineGrape) {
        this.wineGrape = wineGrape;
    }

    public String getWineImage() {
        return wineImage;
    }

    public void setWineImage(String wineImage) {
        this.wineImage = wineImage;
    }

    public float getBody() {
        return body;
    }

    public void setBody(float body) {
        this.body = body;
    }

    public float getTexture() {
        return texture;
    }

    public void setTexture(float texture) {
        this.texture = texture;
    }

    public float getSweetness() {
        return sweetness;
    }

    public void setSweetness(float sweetness) {
        this.sweetness = sweetness;
    }

    public float getAcidity() {
        return acidity;
    }

    public void setAcidity(float acidity) {
        this.acidity = acidity;
    }

    public String getFlavor1() {
        return flavor1;
    }

    public void setFlavor1(String flavor1) {
        this.flavor1 = flavor1;
    }

    public String getFlavor2() {
        return flavor2;
    }

    public void setFlavor2(String flavor2) {
        this.flavor2 = flavor2;
    }

    public String getFlavor3() {
        return flavor3;
    }

    public void setFlavor3(String flavor3) {
        this.flavor3 = flavor3;
    }

    public WineVO() {
        // 기본 생성자
    }

    protected WineVO(Parcel in) {
        index = in.readInt();
        wineName = in.readString();
        wineRating = in.readFloat();
        wineReviews = in.readInt();
        winePrice = in.readString();
        wineLink = in.readString();
        wineCountry = in.readString();
        wineRegion = in.readString();
        wineWinery = in.readString();
        wineType = in.readString();
        wineGrape = in.readString();
        wineImage = in.readString();
        body = in.readFloat();
        texture = in.readFloat();
        sweetness = in.readFloat();
        acidity = in.readFloat();
        flavor1 = in.readString();
        flavor2 = in.readString();
        flavor3 = in.readString();
    }

    public static final Creator<WineVO> CREATOR = new Creator<WineVO>() {
        @Override
        public WineVO createFromParcel(Parcel in) {
            return new WineVO(in);
        }

        @Override
        public WineVO[] newArray(int size) {
            return new WineVO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(index);
        parcel.writeString(wineName);
        parcel.writeFloat(wineRating);
        parcel.writeInt(wineReviews);
        parcel.writeString(winePrice);
        parcel.writeString(wineLink);
        parcel.writeString(wineCountry);
        parcel.writeString(wineRegion);
        parcel.writeString(wineWinery);
        parcel.writeString(wineType);
        parcel.writeString(wineGrape);
        parcel.writeString(wineImage);
        parcel.writeFloat(body);
        parcel.writeFloat(texture);
        parcel.writeFloat(sweetness);
        parcel.writeFloat(acidity);
        parcel.writeString(flavor1);
        parcel.writeString(flavor2);
        parcel.writeString(flavor3);
    }
}
