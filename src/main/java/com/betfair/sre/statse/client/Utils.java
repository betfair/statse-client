package com.betfair.sre.statse.client;

/**
 * User: mcintyret2
 * Date: 19/08/2013
 */
public final class Utils {

    private Utils() {
        // Cannot be instantiated
    }

    public static void checkArgument(boolean assertion, String message) {
        if (!assertion) {
            throw new IllegalArgumentException(message);
        }
    }

}
