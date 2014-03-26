package com.betfair.sre.statse.client;

import java.io.*;
import java.util.Scanner;

import static org.testng.AssertJUnit.assertTrue;

public class StastESubscriberRunner {

    private static Thread serrThread;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (process != null) {
                    try {
                        System.out.println("Running shutdown hook:");
                        kill();
                    } catch (InterruptedException e) {
                        // Ignore - at least we tried!
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        StatsESubscriber subscriber = new StatsESubscriber();

        String line;
        Scanner sc = new Scanner(System.in);
        while ((line = sc.nextLine()) != null) {
            switch (line) {
                case "stop":
                    subscriber.stop();
                    System.exit(0);
                case "verify":
                    System.out.println(new Message("Header", "Body").equals(subscriber.nextMessage()));
                    System.out.flush();
                    break;

                default:
                    System.exit(123);
            }
        }
    }

    private static Process process;

    public static void start() throws IOException {
        process = new ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            StastESubscriberRunner.class.getName()
        ).start();
        (serrThread = new Thread() {
            @Override
            public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while (!Thread.interrupted()) {
                    try {
                        System.err.println(reader.readLine());
                    } catch (IOException e) {
                        return;
                    }
                }
            }

        }).start();
    }

    public static int stop() throws IOException, InterruptedException {
        checkProcess();
        sendMessage("stop");

        return cleanup();
    }

    private static void checkProcess() {
        if (process == null) {
            throw new IllegalStateException("Process hasn't started!");
        }
    }

    private static int cleanup() throws InterruptedException {
        int ret = process.waitFor();
        process = null;
        serrThread.interrupt();
        serrThread.join();
        return ret;
    }

    public static int kill() throws InterruptedException {
        checkProcess();
        process.destroy();
        return cleanup();
    }

    public static boolean isRunning() {
        return process != null;
    }

    public static void verify() throws IOException {
        sendMessage("verify");
        assertTrue(Boolean.valueOf(recvMessage()));
    }

    public static void sendMessage(String message) throws IOException {
        PrintStream ps = new PrintStream(process.getOutputStream());
        ps.println(message);
        ps.flush();
    }

    public static String recvMessage() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return r.readLine();
    }
}
