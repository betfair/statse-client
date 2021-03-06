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

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static com.betfair.sre.statse.client.Utils.checkArgument;

/**
 * User: mcintyret2
 * Date: 16/08/2013
 */
public class StatsEMsgBuilder {

    private static final String STATSE_VERSION = "2 ";

    private final String metric;

    private final Map<ValidValues, Object> values = new EnumMap<ValidValues, Object>(ValidValues.class);

    private long timestamp = System.currentTimeMillis();

    private float sampleRate = 1.0F;

    private final StatsESender statsESender;

    private final TsdbCleaner cleaner;

    private final Map<String, String> tags = new TreeMap<String, String>();

    public StatsEMsgBuilder(String metric, StatsESender statsESender, TsdbCleaner cleaner) {
        this.statsESender = statsESender;
        this.cleaner = cleaner;
        this.metric = cleaner.clean(metric);
        values.put(ValidValues.ERROR, false);
    }

    /**
     * <p>Sets the sample rate, to go in the StatsE header.</p>
     * <p>If unset the sample rate defaults to 1.0</p>
     *
     * @param sampleRate the sample rate. Must be > 0.0 and <= 1.0
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder sampleRate(float sampleRate) {
        checkArgument(sampleRate > 0.0F && sampleRate <= 1.0F, "Sample rate must be (0, 1]");
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * <p>Sets the timestamp, to go in the StatsE header.</p>
     * <p>If unset the timestamp defaults to the moment this StatsEMsgBuilder was created</p>
     *
     * @param timestamp the timestamp, in millis since the epoch
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * <p>Sets the operation, an optional tag for this event</p>
     *
     * @param operation the operation
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder operation(String operation) {
        return withTag("op", operation);
    }

    /**
     * <p>Sets the time, or duration, of this event.</p>
     *
     * @param time the time, or duration, of this event
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder time(long time) {
        values.put(ValidValues.TIME, time);
        return this;
    }

    /**
     * <p>Sets the time, or duration, of this event.</p>
     *
     * @param time the time, or duration, of this event
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder time(double time) {
        values.put(ValidValues.TIME, time);
        return this;
    }

    /**
     * <p>Sets the size of this event.</p>
     *
     * @param size the size of this event
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder size(long size) {
        values.put(ValidValues.SIZE, size);
        return this;
    }

    /**
     * <p>Sets the size of this event.</p>
     *
     * @param size the size of this event
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder size(double size) {
        values.put(ValidValues.SIZE, size);
        return this;
    }

    /**
     * <p>Sets the ttfb of this event.</p>
     *
     * @param ttfb the ttfb of this event
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder ttfb(long ttfb) {
        values.put(ValidValues.TTFB, ttfb);
        return this;
    }

    /**
     * <p>Sets the ttfb of this event.</p>
     *
     * @param ttfb the ttfb of this event
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder ttfb(double ttfb) {
        values.put(ValidValues.TTFB, ttfb);
        return this;
    }

    /**
     * <p>Sets whether this event was an error.</p>
     * <p>If unset the error defaults to false.</p>
     *
     * @param error whether this event was an error
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder error(boolean error) {
        values.put(ValidValues.ERROR, error);
        return this;
    }

    /**
     * <p>Adds a single tag key-value pair.</p>
     *
     * @param tagKey the tag key
     * @param tagValue the tag value
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder withTag(String tagKey, String tagValue) {
        tags.put(cleaner.clean(tagKey), cleaner.clean(tagValue));
        return this;
    }

    /**
     * <p>Adds all tag key-value pairs present in the input.</p>
     *
     * @param tags the tags to add to this message
     * @return this StatsEMsgBuilder, for chaining
     */
    public StatsEMsgBuilder withTags(Map<String, String> tags) {
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            withTag(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Send this message to StatsE.
     */
    public void send() {
        statsESender.sendMessage(this);
    }

    String getHeader() {
        if (sampleRate != 1.0F) {
            return STATSE_VERSION + timestamp + " " + sampleRate;
        } else {
            return STATSE_VERSION + timestamp;
        }
    }

    String getBody() {
        StringBuilder body = new StringBuilder("EVENT|" + metric + "|");

        Iterator<Map.Entry<String, String>> tagIt = tags.entrySet().iterator();
        while (tagIt.hasNext()) {
            Map.Entry<String, String> entry = tagIt.next();
            body.append(entry.getKey()).append("=").append(entry.getValue());
            if (tagIt.hasNext()) {
                body.append(" ");
            }
        }

        body.append("|");

        Iterator<Map.Entry<ValidValues, Object>> valueIt = values.entrySet().iterator();
        while (valueIt.hasNext()) {
            Map.Entry<ValidValues, Object> entry = valueIt.next();
            body.append(entry.getKey().getStatseName()).append("=").append(entry.getValue());
            if (valueIt.hasNext()) {
                body.append(" ");
            }
        }
        return body.toString();
    }

}
