package com.kasperovich.serverinfo;

import com.kasperovich.config.ConnectedClientConfig;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerConfig {
    private static final Logger logger = LoggerUtil.getLogger(ServerConfig.class);
    
    // Port that our server will listen on
    private final int serverPort;

    // Socket that listens to the port and accepts incoming connections
    private final ServerSocket acceptingSocket;

    private final List<ClientProcessingThread> processingThreads;

    Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable exception) {
            int threadIndex = Integer.parseInt(thread.getName());
            logger.error("Uncaught exception in client thread {}: {}", threadIndex, exception.getMessage(), exception);
            
            // Remove the thread from our list
            if (threadIndex >= 0 && threadIndex < processingThreads.size()) {
                processingThreads.remove(threadIndex);
                logger.info("Removed client thread {} from active threads list", threadIndex);
            } else {
                logger.warn("Could not remove client thread {} - index out of bounds", threadIndex);
            }
        }
    };

    public ServerConfig(int serverPort) throws IOException {
        this.serverPort = serverPort;
        logger.info("Creating server socket on port: {}", serverPort);
        acceptingSocket = new ServerSocket(serverPort);
        processingThreads = new ArrayList<>();
        logger.info("Server socket created successfully");
    }

    public void runServer() throws IOException {
        logger.info("Server started and listening on port: {}", serverPort);
        
        while (true) {
            logger.debug("Waiting for client connections...");
            var newClientSocket = acceptingSocket.accept();
            
            logger.info("New client connected from: {}", newClientSocket.getInetAddress());
            
            try {
                var newClient = new ConnectedClientConfig(newClientSocket);
                var newThread = new ClientProcessingThread(newClient);
                
                String threadName = String.valueOf(processingThreads.size());
                newThread.setName(threadName);
                newThread.setUncaughtExceptionHandler(exceptionHandler);
                
                logger.debug("Starting client processing thread: {}", threadName);
                newThread.start();
                processingThreads.add(newThread);
                logger.info("Client thread {} started successfully", threadName);
            } catch (IOException e) {
                logger.error("Error creating client processing thread", e);
                try {
                    newClientSocket.close();
                } catch (IOException closeError) {
                    logger.error("Error closing client socket after failed thread creation", closeError);
                }
            }
        }
    }

    public void stopServer() throws IOException {
        logger.info("Stopping server...");
        
        acceptingSocket.close();
        logger.debug("Server socket closed");
        
        int clientCount = processingThreads.size();
        logger.info("Interrupting {} client threads", clientCount);
        
        for (var thread : processingThreads) {
            try {
                thread.interrupt();
            } catch (Exception e) {
                logger.error("Error interrupting client thread: {}", thread.getName(), e);
            }
        }
        
        logger.info("Server stopped successfully");
    }

    public int getAmountOfConnectedClients() {
        return processingThreads.size();
    }
    
    public int getPort() {
        return serverPort;
    }
}