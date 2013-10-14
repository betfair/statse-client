/*
Copyright 2013, The Sporting Exchange Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

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
