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

import org.jeromq.ZMQ;
import org.jeromq.ZMsg;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * User: mcintyret2
 * Date: 14/08/2013
 */

@Test
public class StatsESenderTest {

    @Mock
    private ZMsg mockZMsg;
    @Mock
    private ZMQ.Socket mockPublisher;
    @Mock
    private StatsEMsgBuilder mockMsg;

    @InjectMocks
    private TestStatsESender sender;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        sender = new TestStatsESender();
        sender.setQueueSize(1);
        sender.setEnabled(true);

        initMocks(this);
    }

    public void shouldSendMessage() throws Exception {
        when(mockMsg.getBody()).thenReturn("Body");
        when(mockMsg.getHeader()).thenReturn("Header");

        sender.start();
        assertTrue(sender.isRunning());
        try {
            sender.sendMessage(mockMsg);

            Thread.sleep(10);
            verify(mockMsg).getBody();
            verify(mockMsg).getHeader();
            verify(mockZMsg).addString("Header");
            verify(mockZMsg).addString("Body");
            verify(mockZMsg).send(mockPublisher);
            assertEquals(1L, sender.getSentCount());
        } finally {
            sender.stop();
        }
    }

    public void shouldNotSendMessageIfQueueFull() {
        sender.start();
        sender.stop();

        sender.sendMessage(mockMsg);
        sender.sendMessage(mockMsg);

        assertEquals(1L, sender.getSentCount());
        assertEquals(1L, sender.getDroppedCount());
    }

    public void shouldNotStartIfNotEnabled() {
        sender.setEnabled(false);

        sender.start();

        assertFalse(sender.isRunning());

        sender.stop();

        assertFalse(sender.isRunning());
    }

    public class TestStatsESender extends StatsESender {

        @Override
        protected ZMsg makeZMsg() {
            return mockZMsg;
        }

        @Override
        protected void startZeroMQ() {

        }

        @Override
        protected void stopZeroMQ() {

        }
    }

}
