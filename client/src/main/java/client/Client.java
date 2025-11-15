package client;

import ui.*;
import chess.*;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client {

    private final ServerFacade server;

    public Client(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
    }

    public static void repl(){
        displayWelcome();
        displayHelp();

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")||!result.equals("exit")){
            displayPrompt();
            String line = scanner.nextLine();

            try {
                result = evaluate(line);
                System.out.print(SET_TEXT_COLOR_ORANGE + result);

            } catch (Exception ex) {

            }
        }
    }

    public static void displayWelcome(){

    }

    public static void displayHelp(){

    }

    public static void displayPrompt(){

    }

    public static String evaluate(String input) {
        return "it works!";
    }

    public static void displayPostLogin(){

    }

}
