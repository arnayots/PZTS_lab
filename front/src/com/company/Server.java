package com.company;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;


    public void start(int port) throws IOException {
        try (var server = new ServerSocket(port)){
            while (true) {
                var socket = server.accept();
                var thread = new Handler(socket);
                thread.start();
            }
        }
    }
}
