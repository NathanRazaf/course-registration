package client;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Scanner;

public class Client {
    public static String currentSession;

    public static void main(String[] args) {

        try {
            System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");
            askCourses();



        } catch (ConnectException x) {
            System.out.println("Connexion impossible sur port 1337: pas de serveur.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void askCourses() throws IOException {
        System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste de cours :");
        System.out.println("1. Automne");
        System.out.println("2. Hiver");
        System.out.println("3. Ete");
        System.out.println("> Choix:");
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        switch (line) {
            case "1" -> {
                currentSession = "Automne";
            }
            case "2" -> {
                currentSession = "Hiver";
            }
            case "3" -> {
                currentSession = "Ete";
            }
            default -> {
                System.out.println("Entrez un choix valide svp!");
                askCourses();
            }
        }
    }


}
