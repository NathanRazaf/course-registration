package client;

import server.models.Course;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static String currentSession;

    public static void main(String[] args) {

        try {
            System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");
            askCourses();



        } catch (ConnectException x) {
            System.out.println("Connexion impossible sur le port 1337 : pas de serveur.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void askCourses() throws IOException {
        System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste de cours : ");
        System.out.println("1. Automne");
        System.out.println("2. Hiver");
        System.out.println("3. Ete");
        System.out.print("> Choix : ");

        Scanner sc = new Scanner(System.in);
        String choice = sc.next();

        switch (choice) {
            case "1" -> {
                fetchCourses("Automne");
                currentSession = "Automne";
            } case "2" -> {
                fetchCourses("Hiver");
                currentSession = "Hiver";
            } case "3" -> {
                fetchCourses("Ete");
                currentSession = "Ete";
            } default -> {
                System.out.println("Entrez un choix valide svp!");
                askCourses();
            }
        }
    }

    public static void fetchCourses(String session) {
        try {
            Socket cS = new Socket("127.0.0.1", 1337);

            ObjectOutputStream oos = new ObjectOutputStream(cS.getOutputStream());
            oos.writeObject("CHARGER " + session);

            ObjectInputStream ois = new ObjectInputStream(cS.getInputStream());
            ArrayList<Course> courseList = (ArrayList<Course>) ois.readObject();

            if (session.equals("Ete")) {
                System.out.println("Les cours offerts durant la session d'été sont : ");
            } else {
                System.out.println("Les cours offerts durant la session d'" + session.toLowerCase() + " sont : ");
            }

            for (int i = 1; i < courseList.size() + 1; i++) {
                System.out.println(i + ". " + courseList.get(i - 1).getCode() + "\t" + courseList.get(i - 1).getName());
            }

            cS.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }
}
