package com.poker.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * Objects of this type represent a typical poker hand.
 * The class implements comparable, allowing comparisons with other PokerHands.
 * It also provide a static sort method.
 */
public class PokerHand implements Comparable<PokerHand> {
    public static enum HandRanking {
        HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND,
        STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGH_FLUSH
    }

    public static final int NUM_CARDS = 5;
    public PokerCard[] _cards = new PokerCard[NUM_CARDS];
    public PokerHand(PokerCard[] cards) {
        for(int i = 0; i < NUM_CARDS; i++) {
            _cards[i] = cards[i];
        }
    }

    /**
     * Compares the current 'this' main.com.poker.server.PokerHand with the supplied main.com.poker.server.PokerHand.
     * It returns positive, if this main.com.poker.server.PokerHand is a better hand; negative,
     * if this hand is worse than the supplied hand; and 0, if this hand
     * ties in rank with the supplied hand.
     */
    public int compareTo(PokerHand thatHand) {
        HandRanking thisHandRank = getHandRanking(_cards);
        HandRanking thatHandRank = getHandRanking(thatHand._cards);
        if(thisHandRank.ordinal() > thatHandRank.ordinal()) { return 1; }
        else if( thisHandRank.ordinal() < thatHandRank.ordinal()) { return -1; }
        else {
            return getTieBreakValue(_cards, thatHand._cards, thatHandRank);
        }
    }

    /**
     * Returns the canonical ranking for a poker hand.
     */
    private HandRanking getHandRanking(PokerCard[] hand) {
        if(isStraightFlush(hand)) { return HandRanking.STRAIGH_FLUSH; }
        if(isFourOfAKind(hand)) { return HandRanking.FOUR_OF_A_KIND; }
        if(isFullHouse(hand)) { return HandRanking.FULL_HOUSE; }
        if(isFlush(hand)) { return HandRanking.FLUSH; }
        if(isStraight(hand)) { return HandRanking.STRAIGHT; }
        if(isThreeOfAKind(hand)) { return HandRanking.THREE_OF_A_KIND; }
        if(isTwoPair(hand)) { return HandRanking.TWO_PAIR; }
        if(isPair(hand)) { return HandRanking.PAIR; }
        return HandRanking.HIGH_CARD;
    }

    /**
     * Breaks ties among equally ranked PokerHands.
     */
    private int getTieBreakValue(PokerCard[] thisHand, PokerCard[] thatHand, HandRanking rank) {
        switch(rank) {
            case HIGH_CARD: case STRAIGHT: case FLUSH: case STRAIGH_FLUSH:
                return tieBreakWithHighCard(thisHand, thatHand, 5);
            case PAIR: return tieBreakOfAKind(thisHand, thatHand, 5, 2);
            case TWO_PAIR: return tieBreakTwoPair(thisHand, thatHand);
            case THREE_OF_A_KIND: return tieBreakOfAKind(thisHand, thatHand, 5, 3);
            case FULL_HOUSE: return tieBreakFullHouse(thisHand, thatHand);
            case FOUR_OF_A_KIND: return tieBreakOfAKind(thisHand, thatHand, 5, 4);
        }
        return 0;
    }

    /**
     * Ignores hand ranking, and determines winning hand according to highest cards.
     */
    private int tieBreakWithHighCard(PokerCard[] thisHand, PokerCard[] thatHand, int numToCompare) {
        sortCardsInHand(thisHand);
        sortCardsInHand(thatHand);
        for(int i = 0; i < numToCompare; i++) {
            if(thisHand[i]._rank.ordinal() > thatHand[i]._rank.ordinal()) { return 1; }
            else if(thisHand[i]._rank.ordinal() < thatHand[i]._rank.ordinal()) { return -1; }
        }
        return 0;
    }

    /**
     *  Breaks ties among two hands having the same canonical ranking,
     *  e.g. two hands that are both Three of a Kinds.
     */
    private int tieBreakOfAKind(PokerCard[] thisHand, PokerCard[] thatHand, int handSize, int ofAKind) {
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> thisSeq = splitIntoSequences(thisHand, handSize);
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> thatSeq = splitIntoSequences(thatHand, handSize);
        int thisOfAKindRank = 0;
        int thatOfAKindRank = 0;
        for(ArrayList<PokerCard> cards : thisSeq.values()) {
            if(cards.size() == ofAKind) {
                thisOfAKindRank = cards.get(0)._rank.ordinal();
            }
        }
        for(ArrayList<PokerCard> cards : thatSeq.values()) {
            if(cards.size() == ofAKind) {
                thatOfAKindRank = cards.get(0)._rank.ordinal();
            }
        }

        if(thisOfAKindRank > thatOfAKindRank) { return 1; }
        else if(thisOfAKindRank < thatOfAKindRank) { return -1; }
        else {
            return tieBreakWithHighCard(thisHand, thatHand, handSize);
        }
    }

    /**
     * Breaks ties among two hands that are both Two of a Kind.
     */
    private int tieBreakTwoPair(PokerCard[] thisHand, PokerCard[] thatHand) {
    HashMap<PokerDeck.Rank, ArrayList<PokerCard>> thisSeq = splitIntoSequences(thisHand, NUM_CARDS);
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> thatSeq = splitIntoSequences(thatHand, NUM_CARDS);
        int thisHighPairRank = 0;
        int thatHighPairRank = 0;
        for(ArrayList<PokerCard> cards : thisSeq.values()) {
            if(cards.size() == 2 && cards.get(0)._rank.ordinal() > thisHighPairRank) {
                thisHighPairRank = cards.get(0)._rank.ordinal();
            }
        }
        for(ArrayList<PokerCard> cards : thatSeq.values()) {
            if(cards.size() == 2 && cards.get(0)._rank.ordinal() > thisHighPairRank) {
                thisHighPairRank = cards.get(0)._rank.ordinal();
            }
        }

        if(thisHighPairRank > thatHighPairRank) { return 1; }
        else if(thisHighPairRank < thatHighPairRank) { return -1; }
        else {
            ArrayList<PokerCard> thisForRemoving = new ArrayList<PokerCard>(Arrays.asList(thisHand));
            ArrayList<PokerCard> thatForRemoving = new ArrayList<PokerCard>(Arrays.asList(thisHand));

            for(int i = 0; i < NUM_CARDS; i++) {
                if(thisForRemoving.get(i)._rank.ordinal() == thisHighPairRank) {
                    thisForRemoving.remove(i);
                }
                if(thatForRemoving.get(i)._rank.ordinal() == thisHighPairRank) {
                    thatForRemoving.remove(i);
                }
            }
            return tieBreakOfAKind((PokerCard[]) thisForRemoving.toArray(), (PokerCard[]) thatForRemoving.toArray(), 3, 2);
        }
    }

    /**
     * Breaks ties among two hands that are both Full Houses.
     */
    private int tieBreakFullHouse(PokerCard[] thisHand, PokerCard[] thatHand) {
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> thisSeq = splitIntoSequences(thisHand, NUM_CARDS);
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> thatSeq = splitIntoSequences(thatHand, NUM_CARDS);

        int thisThreeRank = 0;
        int thatThreeRank = 0;
        int thisPairRank = 0;
        int thatPairRank = 0;
        for(ArrayList<PokerCard> cards : thisSeq.values()) {
            if(cards.size() == 3) { thisThreeRank = cards.get(0)._rank.ordinal(); }
            if(cards.size() == 2) { thisPairRank = cards.get(0)._rank.ordinal(); }
        }

        for(ArrayList<PokerCard> cards : thatSeq.values()) {
            if(cards.size() == 3) { thatThreeRank = cards.get(0)._rank.ordinal(); }
            if(cards.size() == 2) { thatPairRank = cards.get(0)._rank.ordinal(); }
        }

        if(thisThreeRank > thatThreeRank) { return 1; }
        else if(thisThreeRank < thatThreeRank) { return -1; }
        else {
            if(thisPairRank > thatPairRank) { return 1; }
            else if(thisPairRank < thatPairRank) { return -1; }
            else { return 0; }
        }
    }

    /**
     * Sorts the cards in the supplied main.com.poker.server.PokerCard[] from highest to lowest.
     */
    static void sortCardsInHand(PokerCard[] hand) {
        int highCardIndex;
        int highCardRank;
        PokerCard temp;
        for(int i = 0; i < NUM_CARDS; i++) {
            highCardIndex = i;
            highCardRank = hand[i]._rank.ordinal();
            for(int j = i + 1; j < NUM_CARDS; j++) {
                if(hand[j]._rank.ordinal() > highCardRank) {
                    highCardIndex = j;
                    highCardRank = hand[j]._rank.ordinal();
                }
            }
            temp = hand[i];
            hand[i] = hand[highCardIndex];
            hand[highCardIndex] = temp;
        }
    }

    private boolean isStraightFlush(PokerCard[] hand) {
        return isFlush(hand) && isStraight(hand);
    }

    private boolean isFourOfAKind(PokerCard[] hand) {
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> splitHand = splitIntoSequences(hand, 5);
        for(ArrayList<PokerCard> cards : splitHand.values()) {
            if(cards.size() == 4) { return true; }
        }
        return false;
    }

    private boolean isFullHouse(PokerCard[] hand) {
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> splitHand = splitIntoSequences(hand, 5);
        boolean hasThreeOfAKind = false;
        boolean hasPair = false;
        for(ArrayList<PokerCard> cards : splitHand.values()) {
            if(cards.size() == 3) { hasThreeOfAKind = true; }
            if(cards.size() == 2) { hasPair = true; }
        }
        return hasPair && hasThreeOfAKind;
    }

    private boolean isFlush(PokerCard[] hand) {
        PokerDeck.Suit firstSuit = hand[0]._suit;
        for(int i = 1; i < NUM_CARDS; i++) {
            if(firstSuit != hand[i]._suit) {
                return false;
            }
        }
        return true;
    }

    private boolean isStraight(PokerCard[] hand) {
        PokerDeck.Rank lastRank = hand[0]._rank;
        for(int i = 0; i < NUM_CARDS - 1; i++) {
            if( (lastRank.ordinal() - 1) != hand[i + 1]._rank.ordinal() ||
                    (i == 3 &&
                            lastRank == PokerDeck.Rank.TWO &&
                            hand[i + 1]._rank == PokerDeck.Rank.ACE
                    )
              ) {
                return false;
            }
            lastRank = hand[i + 1]._rank;
        }
        return true;
    }

    private boolean isThreeOfAKind(PokerCard[] hand) {
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> splitHand = splitIntoSequences(hand, 5);
        for(ArrayList<PokerCard> cards : splitHand.values()) {
            if(cards.size() == 3) { return true; }
        }
        return false;
    }

    private boolean isTwoPair(PokerCard[] hand) {
        return getNumPairs(hand) == 2;
    }

    private boolean isPair(PokerCard[] hand) {
        return getNumPairs(hand) == 1;
    }

    private int getNumPairs(PokerCard[] hand) {
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> splitHand = splitIntoSequences(hand, 5);
        int numPairs = 0;
        for(ArrayList<PokerCard> cards : splitHand.values()) {
            if(cards.size() == 2) {
                numPairs++;
            }
        }
        return numPairs;
    }

    /**
     * This method is handy for determining rank and grouping cards of the same rank.
     */
    private HashMap<PokerDeck.Rank, ArrayList<PokerCard>> splitIntoSequences(PokerCard[] hand, int numCards) {
        sortCardsInHand(hand);
        HashMap<PokerDeck.Rank, ArrayList<PokerCard>> result = new HashMap<PokerDeck.Rank, ArrayList<PokerCard>>();
        result.put(hand[0]._rank, new ArrayList<PokerCard>());
        result.get(hand[0]._rank).add(hand[0]);
        for(int i = 1; i < numCards; i++) {
            if(result.keySet().contains(hand[i]._rank)) {
                result.get(hand[i]._rank).add(hand[i]);
            }
            else {
                result.put(hand[i]._rank, new ArrayList<PokerCard>());
                result.get(hand[i]._rank).add(hand[i]);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "|| " + _cards[0].toString() + " | " + _cards[1].toString() + " | " +
        _cards[2].toString() + " | " + _cards[3].toString() + " | " + _cards[4].toString() + " ||";
    }
}
