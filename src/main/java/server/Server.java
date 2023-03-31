package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transformer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg) {
        try {
            if (!(arg.equals("Hiver") || arg.equals("Automne") || arg.equals("Ete"))) {
                throw new IllegalArgumentException("Veuillez entrer un argument valide!");
            }
            /*On lit le fichier cours.txt dans le dossier data pour y voir les cours disponibles*/
            FileReader fileReader = new FileReader("src/main/java/server/data/cours.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            ArrayList<Course> courseList = new ArrayList<Course>();
            String line;

            /*Tant que le fichier comporte encore des lignes, on traduit chaque ligne en
             * arguments pour créer et ajouter dans l'ArrayList courseList une instance de Course
             * lorsque la session du cours dans le fichier correspond à l'argument de l'utilisateur
             * pour la fonction handleLoadCourses*/
            while ((line = bufferedReader.readLine()) != null) {
                String[] courseLine = line.split("\\t");
                String code = courseLine[0];
                String name = courseLine[1];
                String session = courseLine[2];
                if (session.equals(arg)) {
                    courseList.add(new Course(code, name, session));
                }
            }
            bufferedReader.close();

            /*On sérialise cette liste pour ensuite l'envoyer au client via le socket*/
            objectOutputStream.writeObject(courseList);
            objectOutputStream.close();

        } catch (IOException e) {
            System.out.println("Échec d'écriture de l'objet CourseList");
            /*Puis s'il y a un ArrayIndexOutOfBoundsException alors c'est forcément durant la lecture du
            * fichier contenant tous les cours disponibles*/
        } catch (ArrayIndexOutOfBoundsException f) {
            System.out.println("Erreur de saisie de certains cours dans le fichier cours.txt");
        }
    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gère les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            /*On se prépare à écrire dans le fichier inscription.txt*/
            FileWriter fw = new FileWriter("src/main/java/server/data/inscription.txt");
            BufferedWriter bw = new BufferedWriter(fw);

            RegistrationForm registrationForm = (RegistrationForm) objectInputStream.readObject();
            /*Exceptions :*/
            //Lorsque l'adresse e-mail inscrite ne se finit pas par "@umontreal.ca" :
            if (registrationForm.getEmail().indexOf("@umontreal.ca") !=
                registrationForm.getEmail().length()-13) {
                throw new IllegalArgumentException("L'adresse e-mail entrée est incorrecte!");
            }
            //Lorsque le matricule inscrit n'est pas un entier à 8 chiffres :
            if (registrationForm.getMatricule().length() != 8 || registrationForm.getMatricule().contains(".")) {
                throw new IllegalArgumentException("Le matricule entré n'est pas conforme!");
            }
            bw.append(registrationForm.getCourse().getSession()).append("\t");
            bw.append(registrationForm.getCourse().getCode()).append("\t");
            bw.append(registrationForm.getMatricule()).append("\t");
            bw.append(registrationForm.getPrenom()).append("\t");
            bw.append(registrationForm.getNom()).append("\t");
            bw.append(registrationForm.getEmail());
            bw.close();

            String message = "Félicitations ! Inscription réussie de "+registrationForm.getPrenom()+"au cours "
                            +registrationForm.getCourse().getCode()+".";

            objectOutputStream.writeObject(message);

        } catch (IOException e) {
            System.out.println("Alerte : Erreur lors de la lecture du registrationForm ou de l'écriture dans le" +
                    "fichier inscription.txt");
        } catch (ClassNotFoundException e) {
            System.out.println("La classe RegistrationForm est introuvable");
        }
    }
}
