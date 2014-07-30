package com.poker.server;

import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * Objects of this type represent the Client/main.com.poker.server.Player complex.
 */
public class Player {
    public PokerHand _hand;
    public long _chips = 10000;
    public String _name = "";
    public PrintWriter _out;
    public BufferedReader _in;
    public long _bet = 0;
    public boolean _alreadyBet = false;
    public boolean _hasPlayed;
    public long _lastBet = 0L;
    public boolean _exitingGame;
}
