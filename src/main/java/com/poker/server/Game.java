package com.poker.server;

import main.com.poker.Messenger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * 
 * This class represents a poker game. It manages the round robin-ing of player
 * turns; delegates messages, dealing, and hand ranking tasks; determines 
 * winners and losers, and sequentially handles each players main.com.poker.server.GameCommand.
 */
public class Game {
    volatile ArrayList<Player> _players = new ArrayList<Player>();
    private volatile ArrayList<Player> _playersInHand = new ArrayList<Player>();
    private volatile ArrayList<Player> _newPlayerQueue = new ArrayList<Player>();
    private ArrayList<Player> _winners = new ArrayList<Player>();
    private StringBuilder _lastHandStats = new StringBuilder();
    private volatile Messenger _messenger;
    private int _actionIndex = 0;
    private boolean _takingBets;
    private int _lastHandMsgLines = 0;
    private Long _minBet = 0L;
    private Long _pot = 0L;
    private Long _lastPot = 0L;
    public static final int MAX_PLAYERS = 10;
    String _name;

    public Game(String name, Messenger messenger) {
        _name = name;
        _messenger = messenger;
    }

    /**
     * Add the joining player to a queue. Only really add them to the game at
     * the end of a round.
     */
    void addPlayerToNewPlayerQueue(Player newPlayer) {
        _newPlayerQueue.add(newPlayer);
        if(_players.size() == 0) { addNewPlayers(); }
    }

    boolean canAddPlayer() {
        return _players.size() + _newPlayerQueue.size() != MAX_PLAYERS;
    }

    /**
     * Add all the players waiting to join the game. Clear the waiting queue.
     */
    private void addNewPlayers() {
        for(Player newPlayer : _newPlayerQueue) {
            _players.add(newPlayer);
            _messenger.addLineToMsg("Welcome to game: " + this._name);
            _messenger.sendClientMsg(newPlayer._out);
        }
        _newPlayerQueue.clear();
    }

    /**
     * Create new hands for each player, every round.
     */
    private void deal() {
        PokerDeck _deck = new PokerDeck();
        for(Player player : _players) {
            PokerCard[] hand = new PokerCard[PokerHand.NUM_CARDS];
            for(int i = 0; i < PokerHand.NUM_CARDS; i++) {
                hand[i] = _deck.dealCard();
            }
            player._hand = new PokerHand(hand);
        }
    }

    private void addPlayersHandToMsg(Player player) {
        _messenger.addLineToMsg("Your Hand");
        _messenger.addLineToMsg(player._hand.toString());
        _messenger.addMsgSplit();
    }

    private void addPlayerStatus() {
        for(Player player : _players) {
            _messenger.addLineToMsg("main.com.poker.server.Player Chip Stacks");
            _messenger.addLineToMsg("\t" + player._name);
            _messenger.addLineToMsg("\t\tChips: " + player._chips);
            _messenger.addLineToMsg("\t\tBet: " + player._bet);
        }
        _messenger.addMsgSplit();
    }

    private void addCommandRequest() {
        _messenger.addLineToMsg("Type \"exit\" to EXIT GAME");
        _messenger.addLineToMsg("Type \"fold\" to FOLD");
        _messenger.addLineToMsg("Type \"check\" to CHECK");
        _messenger.addLineToMsg("Type \"bet\" to BET");
        _messenger.addMsgSplit();
    }

    private void addBetRequest() {
        _messenger.addLineToMsg("Minimum Bet: " + _minBet);
        _messenger.addLineToMsg("Enter your bet amount: ");
    }

    private void addTotalPotAndMinBetToMsg() {
        updateMinBetAndPot();
        _messenger.addLineToMsg("Total Pot: " + _pot);
        _messenger.addLineToMsg("Min Bet: " + _minBet);
        _messenger.addMsgSplit();
    }

    private void addHandResultsToMsg() {
        _messenger.addLineToMsg(_lastHandStats.toString());
        _messenger.addToMsgLineCount(_lastHandMsgLines);
        _lastHandStats.setLength(0);
        _lastHandMsgLines = 0;
    }

    private void buildLastHandStatsMsg() {
        _lastHandStats.append("Winning hands\n");
        _lastHandMsgLines++;
        for(Player player : _winners) {
            _lastHandStats.append("\t\"" + player._name + "\": " + player._hand.toString() + "\n");
            _lastHandMsgLines++;
        }
        _lastHandStats.append("Losing hands\n");
        _lastHandMsgLines++;
        for(Player player : _playersInHand) {
            _lastHandStats.append("\t\"" + player._name + "\": " + player._hand.toString() + "\n");
            _lastHandMsgLines++;
        }
        _messenger.addLineToMsg("Winners each won: " + _lastPot);
    }

    /**
     * Ensure players are shown the current min bet and pot size.
     */
    private void updateMinBetAndPot() {
        for(Player player : _playersInHand) {
            if(player._bet > _minBet) {
                _minBet = player._bet;
            }
        }
        updatePot();
    }

    private void updatePot() {
        long newPot = 0L;
        for(Player player : _players) {
            newPot += player._bet;
        }
        _pot = newPot;
    }

    /**
     * Determine whether the round is over
     */
    private boolean stillTakingBets() {
        for(Player player : _playersInHand) {
            if(!player._hasPlayed || player._bet < _minBet) { return true; }
        }
        return false;
    }

    /**
     * Cycle through all the hands, determine winner(s), loser(s), construct
     * hand stats message, and reset all appropriate member variables.
     */
    private void handleFinishHand() {
        if(_playersInHand.size() == 0) { return; }
        Player bestHanded = _playersInHand.get(0);

        for(int i = 1; i < _playersInHand.size(); i++) {
            if(bestHanded._hand.compareTo(_playersInHand.get(i)._hand) < 0) {
                bestHanded = _playersInHand.get(i);
                _winners.clear();
                _winners.add(bestHanded);
            }
            else if(bestHanded._hand.compareTo(_playersInHand.get(i)._hand) == 0) {
                _winners.add(_playersInHand.get(i));
            }
        }

        if(_winners.size() == 0) { _winners.add(bestHanded); }
        _playersInHand.removeAll(_winners);

        _pot = _pot / _winners.size();
        _lastPot = _pot;

        buildLastHandStatsMsg();

        // Pay the winners
        for(Player player : _winners) {
            player._chips += _pot;
        }

        resetForDeal();
    }

    private void resetForDeal() {
        _pot = 0L;
        _minBet = 0L;
        _takingBets = false;

        addNewPlayers();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(Player player : _players) {
            player._bet = 0;
            player._lastBet = 0;
            player._alreadyBet = false;
            player._hasPlayed = false;
        }
        _actionIndex++;
        if(_actionIndex >= _players.size()) { _actionIndex = 0; }
        play();
    }

    /**
     * Command pattern to handle player commands. Delegates the command to one
     * of 4 inner classes.
     */
    private void handleCommand(Player player) {
        String playerInput;
        addCommandRequest();
        _messenger.sendClientMsg(player._out);

        try {
            while((playerInput = player._in.readLine().trim().toLowerCase()) != null && !playerInput.equals("")) {
                if(playerInput.equals("bet")) { new Bet().execute(player); return; }
                if(playerInput.equals("fold")) { new Fold().execute(player); return; }
                if(playerInput.equals("check")) { new Check().execute(player); return; }
                if(playerInput.equals("exit")) { new ExitGame().execute(player); return; }
                handleCommand(player);
            }
        } catch (IOException e) {
            _messenger.addFailedToMsg();
            new ExitGame().execute(player);
        }
    }

    /**
     * Handle a player's bet command, validate bet amount.
     */
    private void handleBet(Player player) {
        try {
            String input;
            while(( input = player._in.readLine().trim().toLowerCase()) != null) {
                try {
                    Long playerBet = Long.parseLong(input);
                    if(playerBet > player._chips) {
                        _messenger.addLineToMsg("You don't have enough chips.");
                        handleCommand(player);
                    }
                    else if(playerBet < _minBet) {
                        _messenger.addLineToMsg("You must bet at least: " + _minBet);
                        handleCommand(player);
                    }
                    else {
                        if(player._alreadyBet) {
                            player._chips += player._lastBet;
                            player._bet = playerBet;
                            player._lastBet = playerBet;
                            player._chips -= playerBet;
                        }
                        else {
                            player._bet = playerBet;
                            player._lastBet = playerBet;
                            player._chips -= playerBet;
                            player._alreadyBet = true;
                        }
                        updateMinBetAndPot();
//                        if(!stillTakingBets()) { handleFinishHand(); }
                        break;
                    }
                } catch (NumberFormatException e) {
                    _messenger.addClientErrorToMsg(input);
                    handleCommand(player);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * main.com.poker.server.Main turn method. Handles round robin-ing through all of the players in
     * the game.
     */
    void play() {
        while(_players.size() != 0) {
            _playersInHand.clear();
            _playersInHand.addAll(_players);
            deal();
            for(Player player : _players) {
                PokerHand.sortCardsInHand(player._hand._cards);
            }

            _takingBets = true;

            int playerIndex;
            int i = 0;
            while(_takingBets) {
                if(_actionIndex + i >= _playersInHand.size()) {
                    playerIndex = _actionIndex + i - _playersInHand.size();
                }
                else { playerIndex = _actionIndex + i; }
                Player player = _playersInHand.get(playerIndex);
                addHandResultsToMsg();
                addPlayerStatus();
                addPlayersHandToMsg(player);
                addTotalPotAndMinBetToMsg();
                handleCommand(player);
                player._hasPlayed = true;
                _takingBets = stillTakingBets();
                i++;
                if(i >= _playersInHand.size()) { i = 0; }
            }
            _winners.clear();
            handleFinishHand();
        }
    }

    /**
     * Folds the players hand, exits the game, and returns to the main menu.
     */
    private class ExitGame implements GameCommand {
        public void execute(Player player) {
            _messenger.addExitingGame();
            _messenger.sendClientMsg(player._out);
            player._exitingGame = true;
            _playersInHand.remove(player);
            _players.remove(player);
        }
    }

    /**
     * Folds the players hand, and waits for the other players to finish.
     */
    private class Fold implements GameCommand {
        public void execute(Player player) {
            if(_playersInHand.size() > 1) {
                _playersInHand.remove(player);
            }
            if(!stillTakingBets()) { handleFinishHand(); }
        }
    }

    /**
     * If the player doesn't need to bet to stay in the hand, the method
     * effectively goes to the next active player in the round robin.
     */
    private class Check implements GameCommand {
        public void execute(Player player) {
            if(player._bet < _minBet) {
                _messenger.addLineToMsg("You must either match or beat the minimum bet: " + _minBet);
                handleCommand(player);
            }
            if(!stillTakingBets()) { handleFinishHand(); }
        }
    }

    /**
     * Subtracts the bet from this player's chip stack,
     * and augments the minimum bet, if necessary.
     */
    private class Bet implements GameCommand {
        public void execute(Player player) {
            addBetRequest();
            _messenger.sendClientMsg(player._out);
            handleBet(player);
            if(!stillTakingBets()) { handleFinishHand(); }
        }
    }
 }
