package com.kasperovich.serverinfo;

import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ServerProcessingThread extends Thread {

    private static final Logger logger = LoggerUtil.getLogger(ServerProcessingThread.class);
    private ServerConfig server;

    public ServerProcessingThread(int port) throws Exception {
        logger.info("Initializing server processing thread on port: {}", port);
        server = new ServerConfig(port);
    }

    @Override
    public void run() {
        try {
            logger.info("Starting server on port: {}", server.getPort());
            server.runServer();
            super.run();
        } catch (IOException e) {
            logger.error("Error running server", e);
            // We still throw RuntimeException here to propagate the error up
            // since this is a critical failure that should terminate the server
            throw new RuntimeException(e);
        }
    }

    @Override
    public void interrupt() {
        try {
            logger.info("Stopping server");
            server.stopServer();
            super.interrupt();
            logger.info("Server stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping server", e);
            // We still throw RuntimeException here to propagate the error up
            // since this is a critical failure during shutdown
            throw new RuntimeException(e);
        }
    }

    public int getAmountOfConnectedClients() {
        int clientCount = server.getAmountOfConnectedClients();
        logger.debug("Current connected clients: {}", clientCount);
        return clientCount;
    }
}
