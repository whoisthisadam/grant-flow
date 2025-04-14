package com.kasperovich.config;


import com.kasperovich.clientconnection.ClientConnection;

public interface Connectionable {
    void setAccess(ClientConnection access);
}
