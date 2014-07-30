package com.poker.client; /**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * Objects of this type connect to a main.com.poker.server.Main class, but really interact with
 * main.com.poker.server.PokerServer threads. PokerClients simply read and respond to PokerServers.
 * The only real embedded logic in this class is used to handle the quit and
 * exit commands.
 */
import main.com.poker.Consts;

import java.io.*;
import java.net.*;

public class PokerClient {
    private static final String USAGE = "USAGE:\n\n To connect to the server use: java PokerClient <hostname>";

    private static Socket sendSocket = null;
    private static PrintWriter serverOut = null;
    private static BufferedReader serverIn = null;

    public PokerClient(InetAddress serverIP) {
        openServerSocket(serverIP);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader((System.in)));

        String userInput;
        try {

            // Read Initial main.com.poker.server.PokerServer Message
            readServerMessage(stdIn);

            /**
             * Handles main menu interactions.
             */
            while ((userInput = stdIn.readLine().trim()) != null)
            {
                // Handle quit
                if(userInput.equals("quit")) {
                    serverOut.println(userInput);
                    disconnect();
                    break;
                }
                serverOut.println(userInput);

                // Read Server's response to command
                readServerMessage(stdIn);
            }

            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            serverIn.close();
            sendSocket.close();
            serverOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readServerMessage(BufferedReader stdIn) {
        String serverLine = null;
        try {
            while((serverLine = serverIn.readLine()) != null && !serverLine.equals("")) {
                int numLines = Integer.parseInt(serverLine.trim());
                for(int i = 0; i < numLines; i++) {
                    serverLine = serverIn.readLine();
                    System.out.println(serverLine);
                    if(serverLine.contains("Welcome to game: ")) {
                        serverLine = serverIn.readLine();
                        System.out.println(serverLine);
                        handleGamePlay(stdIn);
                        break;
                    }
                }
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles game play interactions.
     */
    private static void handleGamePlay(BufferedReader stdIn) {
        String userInput = "";
        try {
            readServerMessage(stdIn);
            while ((userInput = stdIn.readLine().trim()) != null)
            {
                if(userInput.equals("exit")) {
                    serverOut.println(userInput);
                    readServerMessage(stdIn);
                    break;
                }

                serverOut.println(userInput);

                // Read Server's response to command
                readServerMessage(stdIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validateArgs(String[] args) {
        if(args.length < 1) {
            System.err.println(USAGE);
            System.exit(1);
        }
    }

    /**
     * Open a new connection with a main.com.poker.server.PokerServer on the supplied host,
     * the connection port is, of course, hardcoded for client and server.
     */
    private void openServerSocket(InetAddress serverIP) {
        System.out.println ("Attempting to connect to server...");

        try {
            sendSocket = new Socket(serverIP, Consts.PORT);
            serverOut = new PrintWriter(sendSocket.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(sendSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unable to locate host: " + serverIP); System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to connect to host: " + serverIP); System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        validateArgs(args);
        InetAddress serverIP = InetAddress.getByName(args[0]);
        new PokerClient(serverIP);
    }
}
