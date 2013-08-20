package com.betfair.sre.statse.client;

/**
 * User: mcintyret2
 * Date: 14/08/2013
 */
public enum ValidValues {
    TIME("time"),
    TTFB("ttfb"),
    SIZE("size"),
    ERROR("err");

    private final String statseName;

    private ValidValues(String statseName) {
        this.statseName = statseName;
    }

    public String getStatseName() {
        return statseName;
    }
}
