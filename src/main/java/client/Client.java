package client;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
public class Client {
        public static void main(String[] args) {

            try {
                Socket cS = new Socket("127.0.0.1",1337);


            } catch (ConnectException x) {
                System.out.println("Connexion impossible sur port 1337: pas de serveur.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 }


