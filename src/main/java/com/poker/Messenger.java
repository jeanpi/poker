package com.poker;

import java.io.PrintWriter;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * Objects of this type manage composing a message,
 * and sending it to a Client/main.com.poker.server.Player.
 *
 * The protocol for sending a message requires sending the number of lines that
 * the Client will need to read.
 */
public class Messenger {
    StringBuilder _msg = new StringBuilder();
    int _msgLines = 0;

    public void addStringToLine(String words) {
        _msg.append(words);
    }

    public void addLineToMsg(String line) {
        _msg.append(line + "\n");
        _msgLines++;
    }

    public void addGreetingToMsg() {
        addLineToMsg("You have connected to the server....");
    }

    public void addFoldedToMsg() {
        addLineToMsg("You have folded. Please wait for the end of the hand.");
    }

    public void addExitingGame() {
        addLineToMsg("You are (folding and) exiting the game. Please type \"Enter\"");
    }

    public void addRequestUserToMsg() {
        addLineToMsg("Please enter your username: ");
    }

    public void addRequestGameNameToMsg() {
        addLineToMsg("Please enter the name of the game: ");
    }

    public void addClientErrorToMsg(String input) {
        addLineToMsg("Invalid client input! " + input);
    }

    public void addRemovalToMsg() {
        addLineToMsg("You have been removed from the list of available players.");
    }

    public void addFailedToMsg() {
        addLineToMsg("Failed to process request! EXITING!!!");
    }
    // Handle concatenating messages and newline anomaly, regarding line count
    public void addMsgSplit() {
        _msg.append("\n");
        _msgLines++;
    }

    public void sendClientMsg(PrintWriter clientOut) {
        // Add line for last new line of messages
        _msgLines++;
        clientOut.println("" + _msgLines);
        clientOut.println(_msg.toString());
        _msg.setLength(0);
        _msgLines = 0;
    }

    public void addToMsgLineCount(int cnt) {
        _msgLines += cnt;
    }
}
