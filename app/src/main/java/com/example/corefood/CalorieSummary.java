package com.example.corefood;

public class CalorieSummary {

    private final int consumed;
    private final int burned;

    public CalorieSummary(int consumed, int burned) {
        this.consumed = consumed;
        this.burned = burned;
    }

    public int getConsumed() {
        return consumed;
    }

    public int getBurned() {
        return burned;
    }

    public int getNet() {
        return consumed - burned;
    }
}