package com.poker.server;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * This class represents a typical playing card.
 */
public class PokerCard {
    public PokerDeck.Suit _suit;
    public PokerDeck.Rank _rank;

    public PokerCard(PokerDeck.Suit suit, PokerDeck.Rank rank) {
        _suit = suit;
        _rank = rank;
    }

    @Override
    public String toString() {
        return _rank.toString() + " of " + _suit.toString();
    }
}
