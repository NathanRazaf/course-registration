package client;

import server.models.Course;
import server.models.RegistrationForm;

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
            }
            case "2" -> {
                fetchCourses("Hiver");
                currentSession = "Hiver";
            }
            case "3" -> {
                fetchCourses("Ete");
                currentSession = "Ete";
            }
            default -> {
                System.out.println("Entrez un choix valide svp!");
                askCourses();
            }
        }

        System.out.println("> Choix:");
        System.out.println("1. Consulter les cours offerts pour une autre session");
        System.out.println("2. Inscription à un cours");
        System.out.println("> Choix:");
        Scanner sc1 = new Scanner(System.in);
        String line1 = sc1.nextLine();
        switch (line1) {
            case "1" -> askCourses();
            case "2" -> sendRegistration();
            default -> {
                System.out.println("Entrez un choix valide svp!");
                askCourses();
            }
        }
    }

        public static void fetchCourses (String session){
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

        public static void sendRegistration () {
            try {

                Socket socket1 = new Socket("127.0.0.1", 1337);
                System.out.println("Veuillez saisir votre prénom:");
                String name = new Scanner(System.in).next();
                System.out.println("Veuillez saisir votre nom:");
                String lastName = new Scanner(System.in).next();
                System.out.println("Veuillez saisir votre email:");
                String email = new Scanner(System.in).next();
                System.out.println("Veuillez saisir votre matricule:");
                String matricule = new Scanner(System.in).next();
                System.out.println("Veuillez saisir le code du cours:");
                String codeCours = new Scanner(System.in).next();
                Course course = null;
                ObjectOutputStream output1 = new ObjectOutputStream(socket1.getOutputStream());
                output1.writeObject("CHARGER " + currentSession);
                ObjectInputStream input1 = new ObjectInputStream(socket1.getInputStream());
                ArrayList<Course> courseList = (ArrayList<Course>) input1.readObject();
                socket1.close();
                for (Course coursePointed : courseList) {
                    if (coursePointed.getCode().equals(codeCours)) {
                        course = coursePointed;
                        break;
                    }
                }
                if (course == null) {
                    throw new IllegalArgumentException("Ce cours n'existe pas dans la session sélectionnée");
                }
                RegistrationForm registrationForm = new RegistrationForm(name, lastName, email, matricule, course);
                Socket socket2 = new Socket("127.0.0.1", 1337);

                ObjectOutputStream output2 = new ObjectOutputStream(socket2.getOutputStream());
                output2.writeObject("INSCRIRE");
                output2.writeObject(registrationForm);


                ObjectInputStream input2 = new ObjectInputStream(socket2.getInputStream());
                System.out.println((String) input2.readObject());
                socket2.close();

            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e);
            }
        }
}
