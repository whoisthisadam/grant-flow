package com.kasperovich;

/**
 * A simple class to run the server and catch any exceptions that occur.
 */
public class ServerRunner {
    public static void main(String[] args) {
        try {
            System.out.println("Starting server using ServerRunner...");
            RunServer.main(args);
            System.out.println("Server started successfully.");
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
