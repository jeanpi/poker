package com.poker.server;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * An interface for the Command pattern, in order to handle client menu actions.
 * Implemented by the main.com.poker.server.PokerServer class.
 */
public interface MenuCommand {
    public void execute();
}
