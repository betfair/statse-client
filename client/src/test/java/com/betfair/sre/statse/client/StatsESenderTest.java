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

import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.*;

/**
 * User: mcintyret2
 * Date: 14/08/2013
 * <p/>
 * <p>This test is a little hairy because, as far as I can tell, both ZMQ and TCP itself buffer messages,
 * so asserting single messages have been sent and received is fraught with difficulty as there is no
 * guarantee that a message will get sent straight away.</p>
 * <p/>
 * <p>This has been addressed by having the sender continuously send messages and asserting that at some
 * point the subscriber starts seeing those messages. For the purpose of StatsE that is all we care about
 * anyway.</p>
 */

@Test /*(invocationCount = 100)*/
public class StatsESenderTest {

    static final String ADDRESS = "tcp://127.0.0.1:14444";

    @Mock
    private StatsEMsgBuilder mockMsg;

    private TestStatsESender sender;

    private Thread sendingThread;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        sender = new TestStatsESender();
        sender.setQueueSize(1);
        sender.setAgentAddress(ADDRESS);
        sender.setEnabled(true);
        sender.start();

        initSendingThread();

        initMocks(this);

        when(mockMsg.getHeader()).thenReturn("Header");
        when(mockMsg.getBody()).thenReturn("Body");
    }

    private void initSendingThread() {
        sendingThread = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    sender.sendMessage(mockMsg);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
    }

    @AfterMethod
    public void afterMethod() throws InterruptedException, IOException {
        if (sendingThread.isAlive()) {
            sendingThread.interrupt();
            sendingThread.join();
        }
        if (StastESubscriberRunner.isRunning()) {
            StastESubscriberRunner.stop();
        }
        sender.stop();
    }

    //
    public void shouldSendMessage() throws Exception {
        StastESubscriberRunner.start();

        assertTrue(sender.isRunning());

        sendingThread.start();

        StastESubscriberRunner.verify();

        assertTrue(sender.getSentCount() > 0);
    }

    public void shouldGetMessagesAfterRestartingSubscriber() throws Exception {
        StastESubscriberRunner.start();

        assertTrue(sender.isRunning());
        sendingThread.start();

        StastESubscriberRunner.verify();

        long firstSentCount = sender.getSentCount();
        assertTrue(firstSentCount > 0);

        StastESubscriberRunner.stop();

        StastESubscriberRunner.start();

        assertTrue(sender.isRunning());

        StastESubscriberRunner.verify();

        assertTrue(sender.getSentCount() > firstSentCount);

    }

    public void shouldGetMessagesAfterRestartingSender() throws Exception {
        // Initial tests
        StastESubscriberRunner.start();

        assertTrue(sender.isRunning());
        sendingThread.start();

        StastESubscriberRunner.verify();

        long firstSentCount = sender.getSentCount();
        assertTrue(firstSentCount > 0);

        // Restart the sender
        sendingThread.interrupt();
        sendingThread.join();
        sender.stop();

        initSendingThread();
        sender.start();
        sendingThread.start();

        // Check it is still sending
        StastESubscriberRunner.verify();

        assertTrue(sender.getSentCount() > firstSentCount);

    }

    public void shouldHandleSenderError() throws InterruptedException, IOException {
        StastESubscriberRunner.start();

        sender.zMsgProvider = new ZMsgProvider() {
            boolean first = true;

            @Override
            public ZMsg newZMsg() {
                if (first) {
                    first = false;
                    ZMsg mockZmsg = mock(ZMsg.class);
                    when(mockZmsg.send(any(ZMQ.Socket.class))).thenThrow(new RuntimeException("Broken pipe"));
                    return mockZmsg;
                } else {
                    return new ZMsg();
                }
            }
        };

        assertTrue(sender.isRunning());
        sendingThread.start();

        StastESubscriberRunner.verify();

        long firstSentCount = sender.getSentCount();
        assertTrue(firstSentCount > 0);

        assertEquals(143, StastESubscriberRunner.kill());

        StastESubscriberRunner.start();

        assertTrue(sender.isRunning());

        StastESubscriberRunner.verify();

        assertTrue(sender.getSentCount() > firstSentCount);
    }

    public void shouldNotSendMessageIfQueueFull() {
        sender.stop();

        sender.sendMessage(mockMsg);
        sender.sendMessage(mockMsg);

        assertEquals(1L, sender.getSentCount());
        assertEquals(1L, sender.getDroppedCount());
    }

    public void shouldNotStartIfNotEnabled() {
        sender.stop();
        sender.setEnabled(false);

        sender.start();

        assertFalse(sender.isRunning());

        sender.stop();

        assertFalse(sender.isRunning());
    }

    private interface ZMsgProvider {
        ZMsg newZMsg();
    }

    private class TestStatsESender extends StatsESender {

        private ZMsgProvider zMsgProvider = new ZMsgProvider() {
            @Override
            public ZMsg newZMsg() {
                return new ZMsg();
            }
        };

        @Override
        protected ZMsg makeZMsg() {
            return zMsgProvider.newZMsg();
        }
    }

}
