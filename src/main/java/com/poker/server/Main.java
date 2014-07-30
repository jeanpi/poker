package com.poker.server;

import main.com.poker.Consts;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 *  Objects of this type handle accepting new client connections.
 *  They spin up a new main.com.poker.server.PokerServer Thread for each new connection.
 */
public class Main {

    // These are the objects that are actually shared between all clients.
    // Make them volatile to keep them very up to date.
    private static volatile HashMap<String, Game> _games = new HashMap<String, Game>();
    private static volatile HashMap<String, Player> _players = new HashMap<String, Player>();

    public static void main(String[] args) throws IOException
    {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(Consts.PORT);
            System.out.println ("Connection Socket Created");
            try {

                // Just keep accepting new client sockets 'forever'
                while (true)
                {
                    System.out.println ("Waiting for Connection");
                    Socket newClientSocket = serverSocket.accept();
                    System.out.println(newClientSocket.getInetAddress());
                    System.out.println(newClientSocket.getLocalPort());
                    new PokerServer(newClientSocket, _games, _players);
                }
            }
            catch (IOException e)
            {
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            System.err.println("Couldn't bind to port: " + Consts.PORT);
            System.exit(1);
        }
        finally
        {
            try {
                serverSocket.close();
            }
            catch (IOException e)
            {
                System.err.println("Couldn't close port: " + Consts.PORT);
                System.exit(1);
            }
        }
    }
}
