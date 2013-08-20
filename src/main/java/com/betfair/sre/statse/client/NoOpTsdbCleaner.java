package com.betfair.sre.statse.client;

/**
 * User: mcintyret2
 * Date: 19/08/2013
 */
public class NoOpTsdbCleaner implements TsdbCleaner {

    @Override
    public String clean(String value) {
        return value;
    }
}
