package client;

import ui.*;
import chess.*;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client {

    private final ServerFacade server;
    private State state;

    public Client(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
        state = server.state;
    }

    public void repl(){
        displayWelcome();

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quitting...")&&!result.equals("exiting...")){
            state = server.state;
            displayPrompt();
            String line = scanner.nextLine();

            try {
                result = evaluate(line);
                if (result.contains("Error")) {
                    int start = result.indexOf("Error: ") + 7;
                    int end = result.indexOf("\" }", start);
                    String error = result.substring(start, end);
                    System.out.print(SET_TEXT_COLOR_RED + error + "\n");
                } else if (result.contains("GAMES LIST")) {

                } else {
                    System.out.print(SET_TEXT_COLOR_ORANGE + result + "\n");
                    if (result.contains("logged")){
                        state = server.state;
                        displayHelp();
                        System.out.println();
                    }
                }

            } catch (Exception ex) {

            }
        }
    }

    public void displayWelcome(){
        System.out.print(SET_TEXT_COLOR_ORANGE + "WELCOME TO CALLUM'S CS 240 CHESS");
        doubleLineBreak();
        displayHelp();
        System.out.println();
        System.out.print(SET_TEXT_COLOR_ORANGE + "Log in to continue\n");
    }

    public String displayHelp(){
        if (state == State.SIGNED_OUT) {
            System.out.print(SET_TEXT_COLOR_TURQUOISE + SET_TEXT_BOLD + "Commands available:\n" + RESET_TEXT_BOLD_FAINT);
            System.out.print(SET_TEXT_COLOR_WHITE + "help -- show this info screen\n");
            System.out.print(SET_TEXT_COLOR_SILVER + "quit -- exits the program\n");
            System.out.print(SET_TEXT_COLOR_WHITE + "login <USERNAME> <PASSWORD> -- log in to play chess\n");
            System.out.print(SET_TEXT_COLOR_SILVER + "register <USERNAME> <EMAIL> <PASSWORD> -- create an account\n");
            System.out.print(SET_TEXT_COLOR_TURQUOISE + "----------------------------");
        } else if (state == State.SIGNED_IN) {
            System.out.print(SET_TEXT_COLOR_TURQUOISE + SET_TEXT_BOLD + "Commands available:\n" + RESET_TEXT_BOLD_FAINT);
            System.out.print(SET_TEXT_COLOR_WHITE + "help -- show this info screen\n");
            System.out.print(SET_TEXT_COLOR_SILVER + "quit -- exits the program\n");
            System.out.print(SET_TEXT_COLOR_WHITE + "logout -- log out of session\n");
            System.out.print(SET_TEXT_COLOR_SILVER + "list -- list available games\n");
            System.out.print(SET_TEXT_COLOR_WHITE + "create <NAME> -- create a game with the given name\n");
            System.out.print(SET_TEXT_COLOR_SILVER + "join <GAME> -- join the game with the given id\n");
            System.out.print(SET_TEXT_COLOR_WHITE + "observe <GAME> -- observe the game with the given id\n");
            System.out.print(SET_TEXT_COLOR_TURQUOISE + "----------------------------");
        }
        return "";
    }

    public void displayPrompt(){
        statePrefix();
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_BLINKING + SET_TEXT_BOLD + ">>> " + RESET_TEXT_BLINKING + RESET_TEXT_BOLD_FAINT);
    }

    public String evaluate(String input) {
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
                case "list" -> state == State.SIGNED_IN ? server.listGames() : "You must be logged in to do that!";
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
            System.out.print(SET_TEXT_COLOR_RED + "[[Logged Out]] ");
        }
        if (state == State.SIGNED_IN) {
            System.out.print(SET_TEXT_COLOR_LIGHT_GREY + "[[Logged In]] ");
        }
    }

    public static void doubleLineBreak(){
        System.out.println();
        System.out.println();
    }

}
