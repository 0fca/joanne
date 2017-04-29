/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Obsidiam
 */
public class Gallery extends Application {
    public static String PATH;
    public Stage stage = new Stage();
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        stage.setMinWidth(600);
        stage.setMinHeight(650);
        Scene scene = new Scene(root);
        
        Alert a = new Alert(AlertType.ERROR);
       
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        stage.getIcons().add(new Image(new URL(getClass().getResource("images/iv.png").toExternalForm()).toString(),16,16,true,true));
        stage.setOnCloseRequest(event ->{
            System.exit(0);
        });
        stage.setScene(scene);
        stage.setTitle("Joanne");
        stage.show();
        this.stage = stage;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length > 0){
           PATH = args[0];
           System.out.print(PATH);
        }
        launch(args);
        
    }
    
}
