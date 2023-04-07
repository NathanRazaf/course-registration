import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import server.models.Course;

public class CourseRegistrationApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Set up left side of the window
        Label coursesTitleLabel = new Label("Liste des cours");
        coursesTitleLabel.setStyle("-fx-font-size: 20;");
        TableView<Course> coursesTable = new TableView<>();
        TableColumn<Course, String> code = new TableColumn<>("Code");
        TableColumn<Course, String> cours = new TableColumn<>("Cours");
        coursesTable.getColumns().addAll(code, cours);
        coursesTable.setPrefWidth(300);
        coursesTable.setPrefHeight(200);

        ComboBox<String> semesterComboBox = new ComboBox<>();
        semesterComboBox.getItems().addAll("Automne", "Ete", "Hiver");
        semesterComboBox.getSelectionModel().selectFirst();

        Button loadButton = new Button("Charger");
        VBox leftVBox = new VBox(10, coursesTitleLabel, coursesTable, semesterComboBox, loadButton);
        leftVBox.setAlignment(Pos.TOP_CENTER);
        leftVBox.setPadding(new Insets(20));

        // Set up right side of the window
        Label registrationTitleLabel = new Label("Formulaire d'inscription");
        registrationTitleLabel.setStyle("-fx-font-size: 20;");
        Label firstNameLabel = new Label("Prenom:");
        TextField firstNameTextField = new TextField();
        Button sendButton = new Button("Envoyer");
        VBox rightVBox = new VBox(10, registrationTitleLabel, firstNameLabel, firstNameTextField, sendButton);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setPadding(new Insets(20));

        // Set up main layout
        HBox mainLayout = new HBox(leftVBox, rightVBox);
        mainLayout.setSpacing(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Set up scene and stage
        Scene scene = new Scene(mainLayout, 800, 400);
        primaryStage.setTitle("Course Registration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }






    public static void main(String[] args) {
        launch(args);
    }
}
