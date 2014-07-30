package com.poker.server;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * An interface for the Command pattern, in order to handle player interactions.
 * Implemented by the main.com.poker.server.Game class.
 */
public interface GameCommand {
    public void execute(Player player);
}
