package com.betfair.sre.statse.testagent;

import org.zeromq.ZMQ;

/**
 * User: mcintyret2
 * Date: 14/10/2013
 */
public class RunTestAgent {

    public static void main(String[] args) {
        int port = 14444;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("Connecting to ZMQ...");
        ZMQ.Context ctx = ZMQ.context(1);
        ZMQ.Socket subscriber = ctx.socket(ZMQ.SUB);

        subscriber.bind("tcp://127.0.0.1:" + port);
        subscriber.subscribe("".getBytes());
        System.out.println("Connected");

        String line;
        while ((line = subscriber.recvStr()) != null) {
            System.out.println(line);
        }
        System.out.println("Finished");
    }
}
