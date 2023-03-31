package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * La classe Server représente le serveur de l'application.
 */
public class Server {

    /**
     * La constante REGISTER_COMMAND représente la commande "INSCRIRE".
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";
    /**
     * La constante LOAD_COMMAND représente la commande "CHARGER".
     */
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    /**
     * La méthode Server est le constructeur de la classe Server et lance une exception lorsqu'il y a une erreur
     * d'entrée ou de sortie du socket.
     * Le paramètre port indique à quel port le socket doit se lier.
     *
     * @param port          port auquel le socket doit se lier
     * @throws IOException  si le socket n'arrive pas à se lier au port
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
     * La méthode addEventHandler ajoute un évènement h dans une liste d'objets EventHandler nommée handlers.
     *
     * @param h évènement à ajouter dans la liste handlers
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
     * La méthode run attend et accepte une connexion au client et gère les commandes données par le client.
     * La méthode imprime l'état de la connexion du serveur au client dans la console.
     */
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

    /**
     * La méthode listen gère les commandes envoyées par le client.
     *
     * @throws IOException            si le stream ne peut pas être lu
     * @throws ClassNotFoundException si la classe dans le stream n'existe pas
     */
    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    /**
     * La fonction processCommandLine retourne une paire contenant une commande et les arguments associés à la commande.
     * Le paramètre line est une ligne qui contient une commande et des arguments.
     *
     * @param line  ligne contenant une commande et ses arguments
     * @return      paire Pair<>(cmd, args)
     */
    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    /**
     * La méthode disconnect ferme la lecture et l'écriture du stream, et ferme la connexion entre le serveur et le
     * client.
     * La méthode lance une exception lorsqu'il y a une erreur lors de la fermeture du stream ou lors de la
     * déconnexion du socket.
     *
     * @throws IOException si le stream ou la connexion ne peut pas fermer
     */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    /**
     * La méthode handleEvent gère les inscriptions aux cours et le chargement de la liste des cours disponibles selon
     * la commande entrée en paramètre.
     * Le paramètre cmd est la commande à gérer et le paramètre arg contient les arguments associés à la commande.
     *
     * @param cmd commande à gérer
     * @param arg arguments de la commande à gérer
     */
    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     * La méthode handleLoadCourses envoie au client la liste de cours disponibles pour la session choisie (Automne,
     * Hiver, Été).
     * Le paramètre arg est la session choisie.
     * La méthode gère les exceptions lorsqu'il y a une erreur lors de la lecture du fichier cours.txt ou lors de
     * l'écriture de l'objet, et lorsqu'il y a un argument en trop dans le fichier cours.txt.
     *
     * @param arg session choisie
     */
    public void handleLoadCourses(String arg) {
        try {
            if (!(arg.equals("Hiver") || arg.equals("Automne") || arg.equals("Ete"))) {
                objectOutputStream.writeObject("Veuillez entrer un argument valide!");
                throw new IllegalArgumentException("Veuillez entrer un argument valide!");
            }

            /* Lecture du fichier cours.txt dans le dossier data pour y voir les cours disponibles. */
            FileReader fileReader = new FileReader("src/main/java/server/data/cours.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            ArrayList<Course> courseList = new ArrayList<Course>();
            String line;

            /* Tant que le fichier comporte encore des lignes, traduire chaque ligne en arguments pour créer et ajouter
            dans l'ArrayList courseList une instance de Course lorsque la session du cours dans le fichier correspond à
            l'argument arg. */
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

            /* Sérialiser la liste courseList pour ensuite l'envoyer au client via le socket. */
            objectOutputStream.writeObject(courseList);
            objectOutputStream.close();

        /* Lancer une exception lorsqu'une erreur survient lors de la lecture du fichier cours.txt ou de l'écriture de
        l'objet. */
        } catch (IOException e) {
            System.out.println("Échec d'écriture de l'objet CourseList ou échec de lecture du cours.txt");

        /* Puis s'il y a un ArrayIndexOutOfBoundsException alors c'est forcément durant la lecture du
        fichier contenant tous les cours disponibles. */
        } catch (ArrayIndexOutOfBoundsException f) {
            System.out.println("Erreur de saisie de certains cours dans le fichier cours.txt");
        }
    }

    /**
     * La méthode handleRegistration ajoute le formulaire d'inscription du client dans le fichier inscription.txt et
     * envoie une confirmation au client.
     * La méthode gère les exceptions s'il y a une erreur lors de la lecture de l'objet, l'écriture dans le fichier
     * inscription.txt ou dans le flux de sortie, et s'il la classe RegistrationForm n'existe pas.
     */
    public void handleRegistration() {
        try {
            /* Préparer à écrire dans le fichier inscription.txt. */
            FileWriter fw = new FileWriter("src/main/java/server/data/inscription.txt");
            BufferedWriter bw = new BufferedWriter(fw);

            RegistrationForm registrationForm = (RegistrationForm) objectInputStream.readObject();

            /* Vérification de l'adresse e-mail et du matricule : */
            /* Lorsque l'adresse e-mail inscrite ne se finit pas par "@umontreal.ca" : */
            if (registrationForm.getEmail().indexOf("@umontreal.ca") !=
                    registrationForm.getEmail().length() - 13) {
                objectOutputStream.writeObject("L'adresse e-mail entrée est incorrecte!");
                throw new IllegalArgumentException("L'adresse e-mail entrée est incorrecte!");
            }

            /* Lorsque le matricule inscrit n'est pas un entier à 8 chiffres : */
            try {
                /* Convertir la string matricule en eniter pour vérifier qu'il s'agit d'un entier. */
                int intMatricule = Integer.parseInt(registrationForm.getMatricule());

                /* Si le matricule est un entier, vérifier qu'il est composé de 8 chiffres. */
                if (registrationForm.getMatricule().length() != 8) {
                    objectOutputStream.writeObject("Le matricule doit être composé de 8 chiffres!");
                    throw new IllegalArgumentException("Le matricule doit être composé de 8 chiffres!");
                }
            } catch (NumberFormatException e) {
                /* Lancer une exception lorsque la string n'a pas un format approprié pour convertir en entier. */
                objectOutputStream.writeObject("Le matricule entré n'est pas conforme!");
                System.out.println("Le matricule entré n'est pas conforme!");
            }

            bw.append(registrationForm.getCourse().getSession()).append("\t");
            bw.append(registrationForm.getCourse().getCode()).append("\t");
            bw.append(registrationForm.getMatricule()).append("\t");
            bw.append(registrationForm.getPrenom()).append("\t");
            bw.append(registrationForm.getNom()).append("\t");
            bw.append(registrationForm.getEmail());
            bw.close();

            String message = "Félicitations ! Inscription réussie de " + registrationForm.getPrenom() + "au cours "
                    + registrationForm.getCourse().getCode() + ".";

            objectOutputStream.writeObject(message);

        /* Lancer une exception lorsqu'un erreur survient lors de la lecture du registrationForm, l'écriture dans le
        fichier inscription.txt ou dans le flux de sortie. */
        } catch (IOException e) {
            System.out.println("Alerte : Erreur lors de la lecture du registrationForm ou de l'écriture dans le " +
                    "fichier inscription.txt.");

        /* Lancer une exception si la classe RegistrationForm n'existe pas. */
        } catch (ClassNotFoundException e) {
            System.out.println("La classe RegistrationForm est introuvable.");
        }
    }
}
