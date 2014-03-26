package com.betfair.sre.statse.client;

import org.zeromq.ZMQ;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class StatsESubscriber {

    private final ZMQ.Context ctx = ZMQ.context(1);
    private final ZMQ.Socket subscriber = ctx.socket(ZMQ.SUB);

    private final BlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();

    private final Thread t;

    public StatsESubscriber() throws InterruptedException {
        subscriber.bind(StatsESenderTest.ADDRESS);
        subscriber.subscribe("".getBytes());
        final ZMQ.Poller poller = new ZMQ.Poller(1);
        poller.register(subscriber, ZMQ.Poller.POLLIN);
        t = new Thread() {
            @Override
            public void run() {
                String line;
                String header = null;
                while (!Thread.interrupted()) {
                    if (poller.poll(100) > 0) {
                        line = subscriber.recvStr(ZMQ.DONTWAIT);
                        if (header == null) {
                            header = line;
                        } else {
                            messages.add(new Message(header, line));
                            header = null;
                        }
                    }
                }
            }
        };
        t.start();
    }

    public void stop() throws InterruptedException {
        t.interrupt();
        t.join();
        subscriber.close();
        ctx.term();
    }

    public Message nextMessage() throws InterruptedException {
        return messages.take();
    }
}
