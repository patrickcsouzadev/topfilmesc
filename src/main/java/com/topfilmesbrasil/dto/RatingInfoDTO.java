package com.topfilmesbrasil.dto;

public class RatingInfoDTO {
    private double media;
    private int total;

    public RatingInfoDTO(double media, int total) {
        this.media = media;
        this.total = total;
    }

    public double getMedia() {
        return media;
    }

    public void setMedia(double media) {
        this.media = media;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}