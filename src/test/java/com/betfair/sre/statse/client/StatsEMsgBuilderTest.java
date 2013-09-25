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

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * User: mcintyret2
 * Date: 14/08/2013
 */

@Test
public class StatsEMsgBuilderTest {

    @Mock
    private StatsESender mockSender;
    @Mock
    private TsdbCleaner mockCleaner;

    @Before
    public void before() {
        initMocks(this);

        when(mockCleaner.clean(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "cleaned." + invocation.getArguments()[0].toString();
            }
        });
    }

    public void shouldFormatMsg() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .time(12.34D)
            .error(false);

        assertEquals("EVENT|cleaned.my.metric|op=cleaned.myop|time=12.34 err=false", msg.getBody());
        assertTrue(msg.getHeader().startsWith("2 13"));
        assertEquals(15, msg.getHeader().length());
    }

    public void shouldFormatMsgNoOp() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .time(12.34D)
            .error(false);

        assertEquals("EVENT|cleaned.my.metric||time=12.34 err=false", msg.getBody());
        assertTrue(msg.getHeader().startsWith("2 13"));
        assertEquals(15, msg.getHeader().length());
    }

    public void shouldFormatMsgNoValue() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .error(false);

        assertEquals("EVENT|cleaned.my.metric|op=cleaned.myop|err=false", msg.getBody());
        assertTrue(msg.getHeader().startsWith("2 13"));
        assertEquals(15, msg.getHeader().length());
    }

    public void shouldFormatMsgCustomValues() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .time(12.34D)
            .size(18.56)
            .error(false);

        assertEquals("EVENT|cleaned.my.metric|op=cleaned.myop|time=12.34 size=18.56 err=false", msg.getBody());
        assertTrue(msg.getHeader().startsWith("2 13"));
        assertEquals(15, msg.getHeader().length());
    }

    public void shouldFormatMsgTimestamp() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .time(12.34D)
            .size(18.56)
            .timestamp(12345678L)
            .error(false);

        assertEquals("EVENT|cleaned.my.metric|op=cleaned.myop|time=12.34 size=18.56 err=false", msg.getBody());
        assertEquals("2 12345678", msg.getHeader());

    }

    public void shouldFormatMsgTimestampSampleRate() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .time(12.34D)
            .size(18.56)
            .timestamp(12345678L)
            .sampleRate(0.75F)
            .error(false);

        assertEquals("EVENT|cleaned.my.metric|op=cleaned.myop|time=12.34 size=18.56 err=false", msg.getBody());
        assertEquals("2 12345678 0.75", msg.getHeader());

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailIllegalSampleRate() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .time(12.34D)
            .size(18.56)
            .timestamp(12345678L)
            .sampleRate(75F)
            .error(false);
    }

    public void shouldDefaultErrorToFalse() {
        StatsEMsgBuilder msg = new StatsEMsgBuilder("my.metric", mockSender, mockCleaner)
            .operation("myop")
            .time(12.34D)
            .size(18.56);

        assertEquals("EVENT|cleaned.my.metric|op=cleaned.myop|time=12.34 size=18.56 err=false", msg.getBody());
        assertTrue(msg.getHeader().startsWith("2 13"));
        assertEquals(15, msg.getHeader().length());
    }

}
