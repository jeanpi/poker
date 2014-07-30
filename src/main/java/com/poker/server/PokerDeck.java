package com.poker.server;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * This object holds 52 PokerCards, randomly arranged in an array.
 * No safety net here, yet. If you pull out too many cards,
 * you're going to get an exception. The contract could be improved.
 */
public class PokerDeck {
    public static enum Suit { HEARTS, SPADES, DIAMONDS, CLUBS; }
    public static enum Rank { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE; }
    private int _numSuits = 4;
    private int _numRanks = 13;
    private ArrayList<PokerCard> _deck = new ArrayList<PokerCard>(_numSuits * _numRanks);
    private Random _rand;

    public PokerDeck() {
        _rand = new Random();
        for(int i = 0; i < _numSuits; i++) {
            for(int j = 0; j < _numRanks; j++) {
                _deck.add(new PokerCard( Suit.values()[i], Rank.values()[j] ));
            }
        }
    }

    PokerCard dealCard() {
        int card = _rand.nextInt(_deck.size());
        return _deck.remove(card);
    }
}
