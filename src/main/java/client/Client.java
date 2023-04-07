package client;

import server.models.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public final static String FALL_SEMESTER = "Automne";
    public final static String WINTER_SEMESTER = "Hiver";
    public final static String SUMMER_SEMESTER = "Ete";
    public final static String LOAD_COMMAND = "CHARGER";
    public final static String REGISTER_COMMAND = "INSCRIRE";

    public static void main(String[] args) {
        System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");
        String command;

        /* TODO : run the program */

    }

    public static void printSemesterOptions() {
        System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste de cours :");
        System.out.println("1. Automne");
        System.out.println("2. Hiver");
        System.out.println("3. Été");
        System.out.print("> Choix : ");
    }

    public static String getSemester() {
        String choice;

        Scanner sc = new Scanner(System.in);

        while (true) {
            choice = sc.nextLine();

            if (choice.equals("1")) {
                sc.close();
                return FALL_SEMESTER;
            }

            if (choice.equals("2")) {
                sc.close();
                return WINTER_SEMESTER;
            }

            if (choice.equals("3")) {
                sc.close();
                return SUMMER_SEMESTER;
            }

            System.out.println("Entrez un choix valide SVP!");
            System.out.print("> Choix : ");
        }
    }

    public static void printCommandOptions() {
        System.out.println("Veuillez choisir un choix parmi les suivants :");
        System.out.println("1. Consulter les cours offerts pour une autre session");
        System.out.println("2. Inscription");
        System.out.print("> Choix :");
        /* TODO : print command options after success of registration to let user register to another course */
        /* TODO : add quit option */
    }

    public static String getCommand() {
        String choice;

        Scanner sc = new Scanner(System.in);

        while (true) {
            choice = sc.nextLine();

            if (choice.equals("1")) {
                sc.close();
                return LOAD_COMMAND;
            }

            if (choice.equals("2")) {
                sc.close();
                return REGISTER_COMMAND;
            }

            System.out.println("Entrez un choix valide SVP!");
            System.out.print("> Choix : ");
        }
    }

    public static ArrayList<Course> fetchCourses(String semester) {
        ArrayList<Course> courseList = null;

        try (Socket socket = new Socket("127.0.0.1", 1337)) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(LOAD_COMMAND + " " + semester);

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            courseList = (ArrayList<Course>) ois.readObject();
        } catch (ConnectException e) {
            System.out.println("Connexion impossible sur le port 1337 : pas de serveur.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("La classe Course est introuvable.");
        }

        return courseList;
    }

    public static void printCourses(String semester, ArrayList<Course> courseList) {
        if (semester.equals(SUMMER_SEMESTER)) {
            System.out.println("Les cours offerts durant la session d'été sont :");
        }

        if (semester.equals(FALL_SEMESTER) || semester.equals(WINTER_SEMESTER)) {
            System.out.println("Les cours offerts durant la session d'" + semester.toLowerCase() + " sont :");
        }

        for (int i = 1; i < courseList.size() + 1; i++) {
            System.out.println(i + ". " + courseList.get(i - 1).getCode() + "\t" + courseList.get(i - 1).getName());
        }
    }

    public static RegistrationForm fillForm(ArrayList<Course> courseList) {
        Scanner sc = new Scanner(System.in);
        String name, lastName, email, studentID, courseID;
        Course course = null;

        while (true) {
            System.out.print("Veuillez saisir votre prénom : ");
            name = sc.nextLine();

            if (!(Character.isUpperCase(name.charAt(0)))) {
                System.out.println("Un prénom doit commencer par une majuscule!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.println("Veuillez saisir votre nom : ");
            lastName = sc.nextLine();

            if (!(Character.isUpperCase(lastName.charAt(0)))) {
                System.out.println("Un nom doit commencer par une majuscule!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.println("Veuillez saisir votre e-mail : ");
            email = sc.nextLine();

            if (!email.contains("@") || email.indexOf("@") == 0 || email.lastIndexOf(".") < email.indexOf("@") ||
                    email.lastIndexOf(".") == email.length() - 1) {
                System.out.println("Le-mail entré est non conforme!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.println("Veuillez saisir votre matricule : ");
            studentID = sc.nextLine();

            for  (int i = 0; i < studentID.length() - 1; i++) {
                if (!Character.isDigit(studentID.charAt(i))) {
                    System.out.println("Le matricule doit contenir seulement des chiffres!");
                    break;
                }
            }

            if (studentID.length() != 8) {
                System.out.println("Le matricule doit contenir exactement 8 chiffres!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.println("Veuillez saisir le code du code : ");
            courseID = sc.nextLine();

            String courseListString = courseList.toString();

            if (!courseListString.contains(courseID)) {
                System.out.println("Ce cours n'existe pas dans la session sélectionnée!");
            } else {
                for (Course coursePointed : courseList) {
                    if (coursePointed.getCode().equals(courseID)) {
                        course = coursePointed;
                        break;
                    }
                }
                break;
            }
        }

        sc.close();

        return new RegistrationForm(name, lastName, email, studentID, course);
    }

    public static void sendRegistration(RegistrationForm form) {
        try (Socket socket = new Socket("127.0.0.1", 1337)) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(REGISTER_COMMAND);
            oos.writeObject(form);

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println((String) ois.readObject());
        } catch (ConnectException e) {
            System.out.println("Connexion impossible sur le port 1337 : pas de serveur.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("La classe RegistrationForm est introuvable!");
        }
    }

    public static void loadCourses() {
        printSemesterOptions();
        String semester = getSemester();
        ArrayList<Course> courseList = fetchCourses(semester);
        printCourses(semester, courseList);
    }

    public static void register(ArrayList<Course> courseList) {
        RegistrationForm form = fillForm(courseList);
        sendRegistration(form);
        /* TODO : make sure that the user does not sign up for the same course again */
    }

    public static void handleCommands(String command, ArrayList<Course> courseList) {
        if (command.equals(LOAD_COMMAND)) {
            loadCourses();
        }

        if (command.equals(REGISTER_COMMAND)) {
            register(courseList);
        }
    }
}
