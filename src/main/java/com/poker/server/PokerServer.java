package com.poker.server;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import main.com.poker.Messenger;

/**
 * Created by: John Paul Wallway
 * UWB: CSS 432, Spring 2014
 * Professor: Brent Lagesse
 *
 * Objects of this class are created when main.com.poker.server.Main's main method accepts a new socket.
 * So, there are one of these per client. The main.com.poker.server.PokerServer object handles main
 * main.com.poker.server.MenuCommand execution, adding players to the shared player list, creating games,
 * and adding them to the shared game list, etc.
 */
public class PokerServer extends Thread
{
    public static enum MenuOption { REGISTER, LIST_GAMES, CREATE_GAME, JOIN_GAME, EXIT_GAME, UNREGISTER, QUIT };

    // Member Variables
    private Socket _clientSocket;
    private Messenger _messenger = new Messenger();
    private HashMap<String, Game> _games = new HashMap<String, Game>();
    private HashMap<String, Player> _players = new HashMap<String, Player>();
    private HashMap<MenuOption, String> _menu = new HashMap<MenuOption, String>();
    private Player _client;
    private boolean _running;

    // Menu Item Strings
    private static final String REGISTER = "Type \"register\" to sign up as an available player.";
    private static final String LIST_GAMES = "Type \"list\" to list available games to join.";
    private static final String CREATE_GAME = "Type \"create\" to create your own game";
    private static final String JOIN_GAME = "Type \"join\" to join an available game.";
    private static final String UNREGISTER = "Type \"unregister\" to unregister as an available player and leave the system.";
    private static final String QUIT = "Type \"quit\" to disconnect from the server.";

    // Make the command execution clean
    private HashMap<String, MenuOption> _selectionMap = new HashMap<String, MenuOption>() {
        {
        put("register", MenuOption.REGISTER);
        put("list", MenuOption.LIST_GAMES);
        put("create", MenuOption.CREATE_GAME);
        put("join", MenuOption.JOIN_GAME);
        put("exit", MenuOption.EXIT_GAME);
        put("unregister", MenuOption.UNREGISTER);
        put("quit", MenuOption.QUIT);
        }
    };

    // Make the command execution clean
    private HashMap<MenuOption, MenuCommand> _commandMap = new HashMap<MenuOption, MenuCommand>() {
        {
            put(MenuOption.REGISTER, new Register());
            put(MenuOption.LIST_GAMES, new ListGames());
            put(MenuOption.CREATE_GAME, new CreateGame());
            put(MenuOption.JOIN_GAME, new JoinGame());
            put(MenuOption.UNREGISTER, new Unregister());
            put(MenuOption.QUIT, new Quit());
        }
    };

    private void addMenuToMsg() {
        for(String menuOption : _menu.values()) {
            _messenger.addLineToMsg(menuOption);
        }
    }

    private synchronized void addPlayerListToMsg() {
        if(_players.size() == 0) { return; } // don't append if there are no users
        _messenger.addStringToLine("Current players: ");

        for(Player player : _players.values()) {
            _messenger.addStringToLine("  \"" + player._name + "\"  ");
        }
        _messenger.addMsgSplit();
    }

    private void addUserExists(String user) {
        _messenger.addLineToMsg("User: " + user + " already exists! Please register under another name.");
    }

    // Delegates user's commands to the respective class
    private void handleClientCommand(String clientCommand) {
        if(_selectionMap.keySet().contains(clientCommand)) {
            System.out.println("Executing command: " + clientCommand);
            _commandMap.get(_selectionMap.get(clientCommand)).execute();
        }
        else {
            System.out.println("In else with clientCommand: " + clientCommand);
            _messenger.addClientErrorToMsg(clientCommand);
        }
        addMenuToMsg();
        _messenger.sendClientMsg(_client._out);
    }

    public PokerServer(Socket clientSocket, HashMap<String, Game> games, HashMap<String, Player> players) {
        _games = games;
        _players = players;
        _clientSocket = clientSocket;
        start();
    }

    /**
     * main.com.poker.server.Main menu loop to interact with PokerClients.
     */
    public void run() {
        _running = true;
        System.out.println ("New PokerClient Thread Started");

        try {
            _client = new Player();
            _client._out = new PrintWriter(_clientSocket.getOutputStream(), true);
            _client._in = new BufferedReader(new InputStreamReader( _clientSocket.getInputStream()));

            String inputLine;
            _messenger.addGreetingToMsg();
            _menu.put(MenuOption.REGISTER, REGISTER);
            _menu.put(MenuOption.LIST_GAMES, LIST_GAMES);
            _menu.put(MenuOption.QUIT, QUIT);
            addMenuToMsg();
            _messenger.sendClientMsg(_client._out);

            // Continue to display options while client is not playing a game
            while (_running && (inputLine = _client._in.readLine()) != null) {
                if(_client._in == null || _client._out == null) { _players.remove(_client); }
                if(inputLine.equals("")) { continue; }
                System.out.println("SERVER's inputLine from clientIn: " + inputLine);

                handleClientCommand(inputLine);
            }

            _client._out.close();
            _client._in.close();
            _clientSocket.close();

        }
        catch (IOException e)
        {
            System.err.println("Problem with Communication main.com.poker.server.PokerServer");
            System.exit(1);
        }
    }

    private class Register implements MenuCommand {
        public void execute() {
            String playerName = "";
            try {
                _messenger.addRequestUserToMsg();
                _messenger.sendClientMsg(_client._out);
                playerName = _client._in.readLine();

                // Only allow unique player names
                if(_players.keySet().contains(playerName)) {
                    addUserExists(playerName);
                }
                else {
                    _client._name = playerName;
                    _players.put(playerName, _client);
                    addPlayerListToMsg();
                    _messenger.addMsgSplit();

                    _menu.remove(MenuOption.REGISTER);
                    _menu.put(MenuOption.CREATE_GAME, CREATE_GAME);
                    _menu.put(MenuOption.JOIN_GAME, JOIN_GAME);
                    _menu.put(MenuOption.UNREGISTER, UNREGISTER);
                }
            } catch (IOException e) {
                _messenger.addFailedToMsg();
                _messenger.sendClientMsg(_client._out);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Creates a game for the player executing the command, if the game doesn't
     * already exist and the player is registered. The creating player
     * automatically joins the created game.
     */
    private class CreateGame implements MenuCommand {
        public void execute() {
            String newGameName;
            _messenger.addRequestGameNameToMsg();
            _messenger.sendClientMsg(_client._out);
            try {
                newGameName = _client._in.readLine();
                if(!_players.keySet().contains(_client._name)) {
                    _messenger.addLineToMsg("You are not registered!");
                }
                else if(_games.keySet().contains(newGameName)) {
                    _messenger.addLineToMsg(newGameName + " already exists!");
                    _messenger.addMsgSplit();
                }
                else {
                    Game newGame = new Game(newGameName, _messenger);
                    _games.put(newGameName, newGame);
                    _messenger.addLineToMsg("You have created a game: " + newGame._name);

                    newGame.addPlayerToNewPlayerQueue(_client);
                    newGame.play();
                    if((_games.get(newGameName) != null ) && (_games.get(newGameName)._players != null) &&
                            (_games.get(newGameName)._players.size() == 0)) {
                        _games.remove(newGameName);
                    }
                }
            } catch (IOException e) {
                _messenger.addFailedToMsg();
                _messenger.sendClientMsg(_client._out);

                _client._out.flush();
                _client._out.close();

                try {
                    _client._in.close();
                    _clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Executing client joins the named game, if it exists and they are
     * registered. Server thread sleeps, waiting for the client to exit
     * the game; then, return to the main menu.
     */
    private class JoinGame implements MenuCommand {
        public void execute() {
            _messenger.addRequestGameNameToMsg();
            _messenger.sendClientMsg(_client._out);
            try {
                String gameName = _client._in.readLine();
                if(_client == null || _client._name.equals("")) {
                    _messenger.addLineToMsg("You are not registered! Please hit the enter key.");
                }
                else if(_games.keySet().contains(gameName)) {
                    if(_games.get(gameName).canAddPlayer()) {
                        _games.get(gameName).addPlayerToNewPlayerQueue(_client);
                        _client._exitingGame = false;
                        while (!_client._exitingGame) {
                            Thread.sleep(1000);
                        }
                        _client._exitingGame = false;
                        if((_games.get(gameName) != null) && (_games.get(gameName)._players != null) &&
                                _games.get(gameName)._players.size() == 0) {
                            _games.remove(gameName);
                        }
                    }
                    else {
                        _messenger.addLineToMsg("This game is full. " + Game.MAX_PLAYERS + " player max.");
                        _messenger.addMsgSplit();
                    }
                }
                else {
                    _messenger.addLineToMsg("There is no game: " + gameName);
                    _messenger.addMsgSplit();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lists available games to play, and the players in the respective game.
     */
    private class ListGames implements MenuCommand {
        public void execute() {

            for(Game game : _games.values()) {
                if(game._players.size() == 0) {
                    _games.remove(game);
                }
            }
            _messenger.addLineToMsg("GAMES");
            for(Game game : _games.values()) {
                _messenger.addLineToMsg("\"" + game._name + "\"");
                _messenger.addLineToMsg("\tPlayers:");
                for(Player player : game._players) {
                    _messenger.addLineToMsg("\t\t\t \"" + player._name + "\" ");
                }
            }
            _messenger.addMsgSplit();
        }
    }

    /**
     * Removes the player from the list of registered players. Essentially,
     * disallows them from joining or creating games.
     */
    private class Unregister implements MenuCommand {
        public void execute() {
            _players.remove(_client._name);
            _messenger.addRemovalToMsg();
            addPlayerListToMsg();
            _messenger.addMsgSplit();
            _menu.put(MenuOption.REGISTER, REGISTER);
            _menu.remove(MenuOption.CREATE_GAME);
            _menu.remove(MenuOption.JOIN_GAME);
            _menu.remove(MenuOption.UNREGISTER);
        }
    }

    /**
     * Disconnects the socket. When _running equals false, the run method will
     * exit and the main.com.poker.server.Main object will disconnect the client.
     */
    private class Quit implements MenuCommand {
        public void execute() {
            // main.com.poker.server.Player is quiting. Remove them from list, if necessary.
            if(_client != null && _client._name != null
                    && _players.keySet().contains(_client._name)) {
                _players.remove(_client._name);
            }
            _running = false;
        }
    }
}