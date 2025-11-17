package client;

import datamodel.*;
import ui.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client {

    private final ServerFacade server;
    private State state;
    private PrintStream out;

    public Client(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
        state = server.state;
        out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void repl(){
        displayWelcome();

        Scanner scanner = new Scanner(System.in);
        Object result = "";
        while (!result.equals("quitting...")&&!result.equals("exiting...")){
            state = server.state;
            displayPrompt();
            String line = scanner.nextLine();

            try {
                result = evaluate(line);
                if (result != null) {
                    if (result instanceof String) {
                        if (result.toString().contains("Error")) {
                            displayError(result.toString());
                        } else {
                            out.print(SET_TEXT_COLOR_ORANGE + result + "\n");
                            if (result.toString().contains("logged")) {
                                state = server.state;
                                displayHelp();
                                out.println();
                            }
                        }
                    } else if (result instanceof GameList) {
                        displayGameList((GameList) result);
                    } else if (result instanceof JoinData) {
                        displayGame(out,(JoinData) result);
                    }
                }
            } catch (Exception ex) {
                out.print("An error has occurred. Message: " + ex.getMessage());
            }
        }
    }

    public void displayWelcome(){
        out.print(SET_TEXT_COLOR_ORANGE + "WELCOME TO CALLUM'S CS 240 CHESS");
        doubleLineBreak(out);
        displayHelp();
        out.println();
        out.print(SET_TEXT_COLOR_ORANGE + "Log in to continue\n");
    }

    public String displayHelp(){
        if (state == State.SIGNED_OUT) {
            out.print(SET_TEXT_COLOR_TURQUOISE + SET_TEXT_BOLD + "Commands available:\n" + RESET_TEXT_BOLD_FAINT);
            out.print(SET_TEXT_COLOR_WHITE + "help -- show this info screen\n");
            out.print(SET_TEXT_COLOR_SILVER + "quit -- exits the program\n");
            out.print(SET_TEXT_COLOR_WHITE + "login <USERNAME> <PASSWORD> -- log in to play chess\n");
            out.print(SET_TEXT_COLOR_SILVER + "register <USERNAME> <EMAIL> <PASSWORD> -- create an account\n");
            out.print(SET_TEXT_COLOR_TURQUOISE + "----------------------------");
        } else if (state == State.SIGNED_IN) {
            out.print(SET_TEXT_COLOR_TURQUOISE + SET_TEXT_BOLD + "Commands available:\n" + RESET_TEXT_BOLD_FAINT);
            out.print(SET_TEXT_COLOR_WHITE + "help -- show this info screen\n");
            out.print(SET_TEXT_COLOR_SILVER + "quit -- exits the program\n");
            out.print(SET_TEXT_COLOR_WHITE + "logout -- log out of session\n");
            out.print(SET_TEXT_COLOR_SILVER + "games -- games available games\n");
            out.print(SET_TEXT_COLOR_WHITE + "create <NAME> -- create a game with the given name\n");
            out.print(SET_TEXT_COLOR_SILVER + "join <GAME> <WHITE/BLACK> -- join the game with the given id and color\n");
            out.print(SET_TEXT_COLOR_WHITE + "observe <GAME> -- observe the game with the given id\n");
            out.print(SET_TEXT_COLOR_TURQUOISE + "----------------------------");
        }
        return "";
    }

    public void displayPrompt(){
        statePrefix();
        out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_BLINKING + SET_TEXT_BOLD + ">>> " + RESET_TEXT_BLINKING + RESET_TEXT_BOLD_FAINT);
    }

    public Object evaluate(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                //pre login
                case "help" -> displayHelp();
                case "login" -> state == State.SIGNED_OUT ? server.login(params) : "You must be logged out to do that!";
                case "register" -> state == State.SIGNED_OUT ? server.register(params) : "You must be logged out to do that!";
                case "quit" -> "quitting...";
                case "exit" -> "exiting...";
                //post-login
                case "logout" -> state == State.SIGNED_IN ? server.logout() : "You must be logged in to do that!";
                case "create" -> state == State.SIGNED_IN ? server.createGame(params) : "You must be logged in to do that!";
                case "games" -> state == State.SIGNED_IN ? server.listGames() : "You must be logged in to do that!";
                case "join" -> state == State.SIGNED_IN ? server.joinGame(params) : "You must be logged in to do that!";
                case "observe" -> state == State.SIGNED_IN ? server.observeGame(params) : "You must be logged in to do that!";

                default -> displayHelp();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public void statePrefix(){
        if (state == State.SIGNED_OUT) {
            out.print(SET_TEXT_COLOR_RED + "[[Logged Out]] ");
        }
        if (state == State.SIGNED_IN) {
            out.print(SET_TEXT_COLOR_LIGHT_GREY + "[[Logged In]] ");
        }
    }

    public static void doubleLineBreak(PrintStream out){
        out.println();
        out.println();
    }

    private void displayError(String result){
        int start = result.indexOf("Error: ") + 7;
        int end = result.indexOf("\" }", start);
        String error = result.substring(start, end);
        out.print(SET_TEXT_COLOR_RED + error + "\n");
    }

    private void displayGameList(GameList gameList){
        var games = gameList.games();
        if (games.size() != 0) {
            for (int game = 0; game < games.size(); game++) {
                String print = "Game #" + (game + 1) + " " + games.get(game).gameName() + "\n";
                out.print(SET_TEXT_COLOR_TURQUOISE + print);
            }
        } else {
            out.print(SET_TEXT_COLOR_TURQUOISE + "There are no available games. Try creating one!\n");
        }
    }

    private void displayGame(PrintStream out, JoinData joinData) throws Exception {
        GameData gameData = server.findGame(joinData.gameID(), server.listGames());
        BoardDisplay.printCurrentBoard(out, joinData.playerColor(), gameData.game());
    }
}
