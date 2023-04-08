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

    public final static String QUIT_COMMAND = "QUITTER";
    public static Scanner sc = new Scanner(System.in);
    public static final String TEXT_RESET = "\u001B[0m";
    public static final String TEXT_RED = "\u001B[31m";
    public static final String TEXT_BLUE   = "\u001B[34m";

    public static void main(String[] args) {
        try {
            System.out.println(TEXT_BLUE + "*** Bienvenue au portail d'inscription de cours de l'UDEM ***" + TEXT_RESET);

            ArrayList<Course> courseList = loadCourses();
            printCommandOptions();
            String command = getCommand();

            while (true) {
                handleCommands(command, courseList);
                printCommandOptions();
                command = getCommand();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void printSemesterOptions() {
        System.out.println(TEXT_BLUE + "\nVeuillez choisir la session pour laquelle vous voulez consulter la liste de cours :" + TEXT_RESET);
        System.out.println("1. Automne");
        System.out.println("2. Hiver");
        System.out.println("3. Été");
        System.out.print("> Choix : ");
    }

    public static String getSemester() {
        String choice;

        while (true) {
            choice = sc.nextLine();

            if (choice.equals("1")) {
                return FALL_SEMESTER;
            }

            if (choice.equals("2")) {
                return WINTER_SEMESTER;
            }

            if (choice.equals("3")) {
                return SUMMER_SEMESTER;
            }

            System.out.println("Entrez un choix valide SVP!\n");
            System.out.print("> Choix : ");
        }
    }

    public static void printCommandOptions() {
        System.out.println(TEXT_BLUE + "\nVeuillez choisir un choix parmi les suivants :" + TEXT_RESET);
        System.out.println("1. Consulter les cours offerts pour une autre session");
        System.out.println("2. Inscription");
        System.out.println("3. Quitter");
        System.out.print("> Choix : ");
        /* TODO : print command options after success of registration to let user register to another course */
        /* TODO : add quit option */
    }

    public static String getCommand() {
        String choice;

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

            if (choice.equals("3")) {
                sc.close();
                return QUIT_COMMAND;
            }

            System.out.println("Entrez un choix valide SVP!\n");
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
            System.out.println("\nLes cours offerts durant la session d'été sont :");
        }

        if (semester.equals(FALL_SEMESTER) || semester.equals(WINTER_SEMESTER)) {
            System.out.println("\nLes cours offerts durant la session d'" + semester.toLowerCase() + " sont :");
        }

        for (int i = 1; i < courseList.size() + 1; i++) {
            System.out.println(i + ". " + courseList.get(i - 1).getCode() + "\t" + courseList.get(i - 1).getName());
        }
    }

    public static RegistrationForm fillForm(ArrayList<Course> courseList) {
        String firstName, lastName, email, studentID, courseID;
        Course course = null;

        while (true) {
            System.out.print("\nVeuillez saisir votre prénom : ");
            firstName = sc.nextLine();

            if (firstName.isBlank()) {
                System.out.println(TEXT_RED + "Un prénom est requis!" + TEXT_RESET);
            } else if (firstName.matches(".*[0-9!@#$%&*()_+=|<>?{}/\\\\~].*")) {
                System.out.println("Le prénom n'est pas conforme!");
            } else if (firstName.startsWith(" ") || firstName.endsWith(" ")) {
                System.out.println("Un prénom ne doit pas contenir d'espaces au début et à la fin!");
            } else if (!Character.isUpperCase(firstName.charAt(0))) {
                System.out.println("Un prénom doit commencer par une majuscule!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("\nVeuillez saisir votre nom : ");
            lastName = sc.nextLine();

            if (lastName.isBlank()) {
                System.out.println(TEXT_RED + "Un nom est requis!" + TEXT_RESET);
            } else if (lastName.matches(".*[0-9!@#$%&*()_+=|<>?{}/\\\\~].*")) {
                System.out.println("Le nom n'est pas conforme!");
            }else if (lastName.startsWith(" ") || lastName.endsWith(" ")) {
                System.out.println("Un nom ne doit pas contenir d'espaces au début et à la fin!");
            } else if (!Character.isUpperCase(lastName.charAt(0))) {
                System.out.println("Un nom doit commencer par une majuscule!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("\nVeuillez saisir votre e-mail : ");
            email = sc.nextLine();

            if (email.isBlank()) {
                System.out.println(TEXT_RED + "Un e-mail est requis!" + TEXT_RESET);
            } else if (email.contains(" ") || !email.contains("@") || email.indexOf("@") == 0 ||
                    email.lastIndexOf(".") < email.indexOf("@") || email.lastIndexOf(".") == email.length() - 1 
                    || email.matches(".*[!#$%&*()+=|<>?{}/\\\\~].*")) {
                System.out.println("L'e-mail entré est non conforme!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("\nVeuillez saisir votre matricule : ");
            studentID = sc.nextLine();

            if (studentID.isBlank()) {
                System.out.println(TEXT_RED + "Un matricule est requis!" + TEXT_RESET);
            } else if (studentID.matches(".*[a-zA-z!@#$%&*()_+=|<>?{}/\\\\~-].*")) {
                System.out.println("Un matricule doit contenir des chiffres seulement!");
            } else if (studentID.contains(" ")) {
                System.out.println("Un matricule ne doit pas contenir des espaces!");
            } else if (!studentID.matches(".*[a-zA-z!@#$%&*()_+=|<>?{}/\\\\~-].*") && studentID.length() != 8) {
                System.out.println("Un matricule doit contenir exactement 8 chiffres!");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("\nVeuillez saisir le code du code : ");
            courseID = sc.nextLine();

            String courseListString = courseList.toString();

            if (courseID.isBlank()) {
                System.out.println(TEXT_RED + "Un code de cours est requis!" + TEXT_RESET);
            } else if (!courseListString.contains(courseID)) {
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

        return new RegistrationForm(firstName, lastName, email, studentID, course);
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

    public static ArrayList<Course> loadCourses() {
        printSemesterOptions();
        String semester = getSemester();
        ArrayList<Course> courseList = fetchCourses(semester);
        printCourses(semester, courseList);
        return courseList;
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

        if (command.equals(QUIT_COMMAND)) {
            System.exit(0);
        }
    }
}
