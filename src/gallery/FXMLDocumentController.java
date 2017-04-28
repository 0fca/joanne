/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import gallery.image.ImageManager;
import java.awt.AWTException;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

/**
 * @author Obsidiam
 */

public class FXMLDocumentController extends Gallery implements Initializable {
    
    @FXML
    private ListView image_list,information;
    @FXML
    private ImageView image_view;
    @FXML
    private Parent root;
    @FXML
    private Label prev_lbl,next_lbl,title,menu_label,items_count,size,elements;
    @FXML
    private BorderPane border;
    @FXML
    private ComboBox rotate,folders;
    @FXML
    private ChoiceBox defaults;
    @FXML
    private MenuItem reset,delete,about,fullscreen,save,close,rename,remove,open,open_file,codes;
    @FXML
    private CheckMenuItem ext,owner,date;
    @FXML
    private ContextMenu context;
    @FXML
    private Pane pane;
    @FXML
    private ScrollPane scroll;
    
    private int first,last;
    private DoubleProperty zoom;
    
    private boolean GIVEN_DATE = false;
    private ArrayList<String> sorted = new ArrayList();
    private static ArrayList<String> paths = new ArrayList<>();
    private static ArrayList<String> images = new ArrayList<>();
    private int selection = -1;
    private int LAST_SELECTED = -1;
    private double PERCENT = 100;
    private String ACTUAL_SELECTED = "";
    private Alert a = new Alert(AlertType.INFORMATION);
    private Image next_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/next.png"));
    private Image prev_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/prev.png"));
    private Image menu_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/menu.png"),48,48,true,true);
    private Image iv_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/iv.png"),48,48,true,true);
    private ModelManager model_man = new ModelManager();
    private ImageManager image_man = new ImageManager();
    private ErrorLogger e = new ErrorLogger();
    AppVersion ver = new AppVersion();
    private String OWNER = System.getProperty("user.name");
    private String DATE = "";
    private String KEYWORD = ".png";
    //private ArrayList<String> SUMMARY = new ArrayList<>();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FileWriter fw;
        try {
            fw = new FileWriter(new File("/home/lukas/Desktop/debug"));
       
        BufferedWriter bw = new BufferedWriter(fw);
     
                
        bw.append("Initializing...");
        bw.newLine();

        e.prepareErrorList();
        model_man.trayInit();
        model_man.prepareContextItems();
        bw.append("Initialized.");
        bw.newLine();
        bw.append("Initializing listeners...");
        bw.newLine();
        
        try {
            paths = image_man.loadFolders();
            paths.forEach(item ->{
                folders.getItems().add(new File(item).getName());
            });
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            a.setAlertType(AlertType.ERROR);
           a.setTitle("Rename");
           a.setHeaderText("Error while renaming the file.");
           a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
           a.showAndWait();
        }
        
        
        zoom = new SimpleDoubleProperty(200);
         
        zoom.addListener(listener ->{ 
            model_man.resize(zoom.get(),zoom.get());
            
        });
        
        
        image_list.setOnMouseClicked((event) ->{
            if(event.getButton() == MouseButton.SECONDARY){
                selection = image_list.getSelectionModel().getSelectedIndex();
                MenuItem m = context.getItems().get(2);
                m.setText("Back to "+new File(ACTUAL_SELECTED).getName());
                context.show(image_list, event.getScreenX(),event.getScreenY());
            }else{
               System.gc();
               selection = image_list.getSelectionModel().getSelectedIndex();
               LAST_SELECTED = selection;
               if(selection != -1){
                 Image loaded = image_man.getImage(images.get(selection));
                 model_man.setToImgView(loaded);
                 ImageProperties im = new ImageProperties();
                   try {
                       model_man.setInofrmationToModel(im.getInformation(images.get(selection)));
                   } catch (IOException ex) {
                       Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                       a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                   }
               }
               
            }
        });
        
        
        border.setOnMouseClicked(event ->{ 
           
        });

        close.setOnAction(event ->{
            System.exit(0);
        });
        
        save.setOnAction((event) ->{
           
            try {
                Properties p = new Properties();
                String pa = "";
                if(new File(pa).isFile()){
                    pa = new File(pa).getParent();
                }
                if(Files.exists(new File("folders.xml").toPath())){
                FileInputStream in = new FileInputStream(new File("folders.xml"));
                p.loadFromXML(in);
                BiConsumer<Object,Object> bi = (x,y) ->{
                    p.getProperty(x.toString(), new File(y.toString()).getName());
                };
                p.forEach(bi);

                }
                FileOutputStream f = new FileOutputStream(new File("folders.xml"));
                if(!p.containsKey(pa)){
                p.setProperty(new File(pa).getAbsolutePath(),new File(pa).getName());
                }else{
                    a.setAlertType(AlertType.ERROR);
                    a.setHeaderText("Adding folder.");
                    a.setContentText("You have already added this folder!");
                    a.showAndWait();
                }
                p.storeToXML(f,null);
                ObservableList<String> s = folders.getItems();
                s.add(0, new File(pa).getName());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                 a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
            }
           
        });
        
        folders.setOnAction((event) ->{
            Object o = folders.getSelectionModel().getSelectedItem();
            String name = o.toString();
            paths.forEach(x ->{
               if(name.equals(new File(x).getName())){
                  model_man.listImages(x);
                  ACTUAL_SELECTED = x;
               } 
            });  
            
        });
        
        pane.setOnMouseClicked(event ->{
            animatePanelMove();
        });
        
        fullscreen.setOnAction((event) ->{
            if(selection != -1){
            Stage stage2 = (Stage) root.getScene().getWindow();
     
            if(stage2.isFullScreen()){
                PERCENT = 100;
                size.setText(String.valueOf(PERCENT));
                model_man.resize();
                stage2.setFullScreen(false);
                
            }else{
                
                model_man.resize();
                stage2.setFullScreen(true); 
            }
            }
        });
        
        rotate.setOnAction(event ->{
            if(image_view.getImage() != null){
            Object o = rotate.getSelectionModel().getSelectedItem();
            double d = Double.parseDouble(o.toString());
            image_view.setRotate(d);
            }
        });
       
        
        defaults.setOnAction(event ->{
            String name = defaults.getSelectionModel().getSelectedItem().toString().toLowerCase()+".jpg";
            Image i = new Image(FXMLDocumentController.class.getResourceAsStream("images/"+name));
            Stage s = (Stage)root.getScene().getWindow();
            
            if(!s.isFullScreen()){
                image_view.setFitHeight(border.getPrefWidth());
                image_view.setFitWidth(border.getPrefHeight());
            }else{
                image_view.setFitHeight(0);
                image_view.setFitWidth(0);
                
            }
            image_view.setImage(i);
            selection = -1;
            image_list.setItems(FXCollections.observableArrayList());
            title.setText(defaults.getSelectionModel().getSelectedItem().toString());
        });
        
      
        ObservableList<Double> d = FXCollections.observableArrayList();
        double iter = 0.0;
        
        while(iter<=360.0){
            d.add(iter);
            iter+=45.0;
        }
        rotate.setItems(d);
       
        about.setOnAction(event ->{
            a.setAlertType(AlertType.INFORMATION);
            a.setHeaderText("Susanne");
            a.setContentText("Susanne\nAuthor: Obsidiam\nver:"+ver.selectAndGetVersion(0)+"\nLicense:GNU GPL v.3.0\n");
            a.showAndWait();
        });
        
        
        
        delete.setOnAction(event ->{
            a.setAlertType(AlertType.CONFIRMATION);
            a.setHeaderText("Are you sure?");
            a.setContentText("This action will permanently delete this item from the list.");
            Optional<ButtonType> result = a.showAndWait();
            if(result.get() == ButtonType.OK){
            Properties p = new Properties();
            FileInputStream in;
            try {
                in = new FileInputStream("folders.xml");
                p.loadFromXML(in);
                
                in.close();
                for(int i = 0; i<p.size(); i++){
                    String get = paths.get(i);
                    if(new File(get).getName().equals(folders.getSelectionModel().getSelectedItem())){
                       p.remove(get);
                    }
                }
                FileOutputStream f = new FileOutputStream("folders.xml");
                p.storeToXML(f, null);
                f.close();
                ObservableList<String> folder = folders.getItems();
                folder.remove(folders.getSelectionModel().getSelectedItem().toString());
                folders.setItems(folder);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
               a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
            }
            
            }else{
                a.setAlertType(AlertType.INFORMATION);
                a.setHeaderText("Action abroted.");
                a.setContentText("The action has been canceled, no action has been taken.");
                a.showAndWait();
            }
        });
        next_lbl.setCursor(Cursor.HAND);
        prev_lbl.setCursor(Cursor.HAND);
        
        next_lbl.setOnMouseClicked(event ->{
             
            if(selection >= 0&selection <= images.size()-1){
              selection = selection + 1; 
              image_list.getSelectionModel().clearAndSelect(selection);
              model_man.setToImgView(image_man.getImage(images.get(selection)));
              image_list.scrollTo(selection);
            }
        });
        
       
        prev_lbl.setOnMouseClicked(event ->{
           
            if(selection >= 0&selection <= images.size()-1){
              selection = selection - 1; 
              image_list.getSelectionModel().clearAndSelect(selection);
              model_man.setToImgView(image_man.getImage(images.get(selection)));
              image_list.scrollTo(selection);
            }
        });
        
        prev_lbl.setOnMouseEntered(event ->{
            prev_lbl.setGraphic(new ImageView(prev_img));
        });
        
        prev_lbl.setOnMouseExited(event ->{
             prev_lbl.setGraphic(null);
        });
        
        next_lbl.setOnMouseEntered(event ->{
           next_lbl.setGraphic(new ImageView(next_img));
        });
        
        next_lbl.setOnMouseExited(event ->{
           next_lbl.setGraphic(null);
        });
        
        image_list.setOnKeyPressed(event ->{
            if(event.getCode() == KeyCode.RIGHT){
                if(selection != -1&selection < images.size()){
                  selection = selection+1;
                  if(selection != -1){
                  Image i = image_man.getImage(images.get(selection));
                  model_man.setToImgView(i);
                  }
                }
            }
            if(event.getCode() == KeyCode.LEFT){
                if(selection != -1){
                  selection = selection-1;
                  if(selection != -1){
                  Image i = image_man.getImage(images.get(selection));
                  model_man.setToImgView(i);
                  }
                }
            }
        });
        
        root.setOnKeyTyped(event ->{
            if(selection != -1){
                if(event.getCharacter().equals("+")){
                    PERCENT = PERCENT + 50;
                    model_man.resize();
                    size.setText(String.valueOf(PERCENT));
                }

                if(event.getCharacter().equals("-")){
                    PERCENT = PERCENT - 50;
                    model_man.resize();
                    size.setText(String.valueOf(PERCENT));
                }
            }
            event.consume();
        });
        
        image_view.setOnMouseEntered(event -> {
             image_view.setCursor(Cursor.HAND);
        });
        
        image_view.setOnKeyPressed(event ->{
               fullscreen.fire();
        });
       
        ObservableList<String> s = FXCollections.observableArrayList();
        for(int i = 50; i<1000;i+=50){
            String percent = String.valueOf(i);
            s.add(percent);
        }
        
        reset.setOnAction(event ->{
            if(selection != -1){ 
                model_man.resize(); 
            }
        });
        
        open_file.setOnAction(event ->{
            File get = image_man.chooseFile();
            if(get != null){
                    PATH = get.getParent();
                    model_man.listImages(PATH);
                    ACTUAL_SELECTED = get.getAbsolutePath();
                    model_man.selectImg(images.indexOf(ACTUAL_SELECTED));
            }else{
                a.setAlertType(AlertType.ERROR);
                a.setTitle("Choose...");
                a.setHeaderText("Error while opening the file/dir.");
                a.setContentText("Nothing has been chosen.");
            }
        });
        
        open.setOnAction((ActionEvent event) -> {
        if(selection != -1){
                a.setAlertType(AlertType.CONFIRMATION);
                a.setTitle("Choose...");
                a.setHeaderText("Bash or your own choice?");
                a.setContentText("Your program - OK. Bash - No. Windows - Windows");

                List<String> choices = new ArrayList<>();
                choices.add("My program");
                choices.add("Bash");
                choices.add("Windows");
               
                
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Bash", choices);
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(letter -> {
                    if(letter.equals("My program")){
                     FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Find program to open the file with.");
                        File f = fileChooser.showOpenDialog(root.getScene().getWindow());
                    if(f != null){
                        if(System.getProperty("os.name").equals("Linux")){
                            String name = f.getName();
                            Runtime r = Runtime.getRuntime();
                            String[] array = {"bash","-c",name+" "+images.get(selection)};
                            try {
                                r.exec(array);
                            } catch (IOException ex) {
                                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }else{
                        a.setAlertType(AlertType.ERROR);
                        a.setTitle("Error");
                        a.setHeaderText("Error while reading the file path.");
                        a.setContentText("The file doesn't exist.");
                        a.showAndWait();
                    }
                    }
                    if(letter.equals("Bash")){
                        TextInputDialog input = new TextInputDialog("bash name");
                        Optional<String> result1 = input.showAndWait();
                        result1.ifPresent(name -> {
                           Runtime r = Runtime.getRuntime();
                                String[] array = {"bash","-c",name+" "+images.get(selection)};
                                try {
                                    r.exec(array);
                                } catch (IOException ex) {
                                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                    a.setAlertType(AlertType.ERROR);
                                   a.setTitle("Rename");
                                   a.setHeaderText("Error while renaming the file.");
                                   a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                                   a.showAndWait();
                                } 
                        });
                    }
                    if(letter.equals("Windows")){
                        
                    }
                }
                );
               }else{
            a.setAlertType(AlertType.ERROR);
            a.setContentText("The selection hasn't been made.");
            a.showAndWait();
        }
        });
           
        remove.setOnAction(event ->{
           if(selection != -1){
               String file_to_delete = images.get(selection);
               try {
                   Files.deleteIfExists(new File(file_to_delete).toPath());
                   model_man.listImages(ACTUAL_SELECTED);
                   System.gc();
               } catch (IOException ex) {
                   a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
               }
           }else{
            a.setAlertType(AlertType.ERROR);
            a.setContentText("The selection hasn't been made.");
            a.showAndWait();  
           } 
        });
        
        rename.setOnAction(event ->{
            if(selection != -1){
                String file_to_rename = new File(images.get(selection)).getName();
                image_man.renameImage(ACTUAL_SELECTED,file_to_rename);
            }
            event.consume();
        });
        
        ext.setOnAction(event ->{
            if(ext.isSelected()){
                TextInputDialog in = new TextInputDialog(".png");
                Optional<String> change = in.showAndWait();
                change.ifPresent(present ->{
                    try {
                        if(!present.isEmpty()){
                        System.out.print(present);

                        model_man.sortImageList("ext", present);
                        KEYWORD = present;
                        }
                    } catch (IOException ex) {
                       a.setAlertType(AlertType.ERROR);
                           a.setTitle("Rename");
                           a.setHeaderText("Error while renaming the file.");
                           a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                           a.showAndWait();
                    } catch (ParseException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }else{
                if(!ACTUAL_SELECTED.isEmpty()){
                      model_man.listImages(ACTUAL_SELECTED);
                }
                try {
                if(owner.isSelected()){
                    
                        model_man.sortImageList("owner", OWNER);
                    
                }
                if(date.isSelected()){
                   
                        model_man.sortImageList("date", DATE);
                   
                }
                } catch (IOException | ParseException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            event.consume();
        });
        
        owner.setOnAction(event ->{
            if(owner.isSelected()){
            TextInputDialog in = new TextInputDialog(OWNER);
            Optional<String> change = in.showAndWait();
            change.ifPresent(event2 ->{
                try {
                    if(!event2.isEmpty()){
                    model_man.sortImageList("owner", event2);
                    OWNER = event2;
                    }
                    
                } catch (IOException ex) {
                  a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                } catch (ParseException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            }else{
                if(!ACTUAL_SELECTED.isEmpty()){
                      model_man.listImages(ACTUAL_SELECTED);
                }
                try {
                    if(date.isSelected()){

                            model_man.sortImageList("date", DATE);

                    }
                    if(ext.isSelected()){

                            model_man.sortImageList("ext", KEYWORD);

                }
                } catch (IOException | ParseException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            event.consume();
        });
        
        date.setOnAction(event ->{
            if(date.isSelected()){
            Date date2 = new Date();
            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            TextInputDialog in = new TextInputDialog(df.format(date2));
            Optional<String> change = in.showAndWait();
            items_count.setText(String.valueOf(images.size()));
            
            change.ifPresent(event2 ->{
                try {
                    if(event2.isEmpty()|!event2.contains("-")){
                      GIVEN_DATE = true;
                      DATE = event2;
                      model_man.sortImageList("date", event2);
                    }else{
                      GIVEN_DATE = false;  
                      model_man.sortImageList("date", event2);  
                      DATE = event2;
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                } catch (ParseException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            }else{
                if(!ACTUAL_SELECTED.isEmpty()){
                        model_man.listImages(ACTUAL_SELECTED);
                }
                try {
                    if(owner.isSelected()){
                            model_man.sortImageList("owner", OWNER);
                    }
                    if(ext.isSelected()){
                            model_man.sortImageList("ext", KEYWORD);
                    }
                } catch (IOException | ParseException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            event.consume();
            }
        });

        image_view.setPreserveRatio(true);
        prev_lbl.setGraphic(new ImageView(prev_img));
        next_lbl.setGraphic(new ImageView(next_img));
        menu_label.setVisible(true);
        menu_label.setGraphic(new ImageView(menu_img));
        menu_label.setOnMouseClicked(event ->{
            animatePanelMove();
        });
            
        if(PATH != null){
            ACTUAL_SELECTED = PATH;
            Image load = image_man.getImage(PATH);
            model_man.setToImgView(load);
            model_man.listImages(new File(PATH).getParent());
        }else{
            System.out.print("NULL");
        }
        
        root.setOnKeyPressed((KeyEvent event) ->{
            if(event.getCode() == KeyCode.F&event.isControlDown()){
                fullscreen.fire();
            }

            if(event.getCode() == KeyCode.F1){
                about.fire();
            }
            
            if(event.getCode() == KeyCode.F5&event.isControlDown()){
                save.fire();
            }
            
            if(event.getCode() == KeyCode.F6&event.isControlDown()){
                delete.fire();
            }
            
            if(event.getCode() == KeyCode.O&event.isControlDown()){
                owner.fire();
            }
            
            if(event.getCode() == KeyCode.K&event.isControlDown()){
                ext.fire();
            }
            
            if(event.getCode() == KeyCode.D&event.isControlDown()){
                date.fire();
            }

            event.consume();
            
        });
        codes.setOnAction(event ->{
                     TextInputDialog tx = new TextInputDialog("");
            tx.setContentText("Type the error code here:");
            Optional<String> in = tx.showAndWait();
            in.ifPresent(consumer ->{
                //Error: NullPointerException - kod: 10
                //Error: NoSuchFileException - kod: 20
                //Error: IllegalStateException - kod: 30
                //Error: FileNotFoundException - kod: 200
                //Error: AccessDeniedException - kod: 40
                //Error: ArrayIndexOutOfBoundsException - kod: 50
                //Error: UnsupportedOperationException - kod: 60
                //Error: IOException - kod: 70
                //Error: MalformedURLException - kod: 80
              HashMap<Exception,Integer> errors = e.returnMap();
              System.out.print(consumer);
              
              if(errors.containsValue(new Integer(consumer))){
                  e.setErrorName(Integer.parseInt(consumer));
                  a.setHeaderText(e.NAME);
                  a.setContentText(e.getExceptionDescriptor(new Integer(consumer)));
                  a.showAndWait();
              }else{
                  a.setAlertType(AlertType.ERROR);
                  a.setTitle("Errors' codes list");
                  a.setHeaderText("Error while looking for the code.");
                  a.setContentText("There is no error with that code.");
                  a.showAndWait();
                  
              }
            });
        });

        bw.append("Listeneres initialized...");
        bw.newLine();
        ObservableList items = FXCollections.observableArrayList();
        items.add("Jeżyk");
        items.add("Para");
        defaults.setItems(items);
        image_list.setCellFactory(new CallbackImpl());
        System.gc();
        bw.append("Defaults and list prepared.");
        bw.newLine();
        bw.append("Done.");
        bw.newLine();
         bw.flush();
         bw.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        image_list.addEventFilter(javafx.scene.input.ScrollEvent.ANY, event -> {
            //System.gc();
            image_list.setCellFactory(new CallbackImpl());
        });
        image_list.addEventFilter(javafx.scene.control.ScrollToEvent.ANY, event -> { 
            //just for a future...
        });
        image_list.setOnScroll(event ->{
            System.out.println("Scroll");
        });
        border.leftProperty().addListener(listener ->{
            System.out.println("Listener");
        });
        
        
        
          scroll.setPannable(true);
          scroll.addEventFilter(ScrollEvent.ANY, (ScrollEvent event) -> {
              if (event.getDeltaY() > 0) {
                  zoom.set(zoom.get() * 1.1);
              } else if (event.getDeltaY() < 0) {
                  zoom.set(zoom.get() / 1.1);
              }
              event.consume();
        }); 
    }  
        
    private void createFavoritesList() throws FileNotFoundException, IOException{
        ObservableList o = image_list.getSelectionModel().getSelectedIndices();
       
        
       if(!Files.exists(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml").toPath())){
           FileOutputStream f = new FileOutputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
           Properties p = new Properties();
           
          o.forEach(x ->{
              System.out.print(x);
              try{
               if(folders.getSelectionModel().getSelectedItem() != null){
                
                   System.out.print(o);
                   p.setProperty(folders.getSelectionModel().getSelectedItem().toString()+"-"+new File(images.get(Integer.parseInt(x.toString()))).getName(), images.get(Integer.parseInt(x.toString())));

               }else{
                   File file = new File(images.get(Integer.parseInt(x.toString())));
                   p.setProperty("NoName-"+new File(ACTUAL_SELECTED).getName(), file.getAbsolutePath());
               }
               System.out.print(new File(images.get(Integer.parseInt(x.toString()))).getName());
               p.storeToXML(f, null);
               f.flush();
               f.close();
              }catch(NumberFormatException | IOException ex){

              }
          });
       }else{
           FileInputStream fin = new FileInputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
           
           Properties p = new Properties();
           
           p.loadFromXML(fin);
           o.forEach(x1 ->{
               try{
                   if(!p.containsValue(images.get(Integer.parseInt(x1.toString())))){
                   if(folders.getSelectionModel().getSelectedItem() != null){
                        p.setProperty(new File(ACTUAL_SELECTED).getName()+"-"+new File(images.get(Integer.parseInt(x1.toString()))).getName(), images.get(Integer.parseInt(x1.toString())));
                   }else{
                       File file = new File(ACTUAL_SELECTED);
                       if(file.isFile()){
                        p.setProperty("NoName-"+new File(ACTUAL_SELECTED).getName(), ACTUAL_SELECTED);
                       }
                   }
                   }else{
                       a.setAlertType(AlertType.ERROR);
                       a.setTitle(String.valueOf(AlertType.ERROR));
                       a.setHeaderText("Error while adding the item.");
                       a.setContentText("Item has been already added.");
                       a.showAndWait();
                   }
                   fin.close();

               }catch(NumberFormatException | IOException ex){
                   
               }
               System.out.print(new File(images.get(Integer.parseInt(x1.toString()))).getName());
           });
           FileOutputStream f = new FileOutputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
           p.storeToXML(f, null);
           f.flush();
           f.close();
       }
    }

    private void removeFromFavorites() throws FileNotFoundException, IOException{
       ObservableList o = image_list.getSelectionModel().getSelectedIndices();
       try{
       if(Files.exists(new File("favorites.xml").toPath())){
           FileInputStream fin = new FileInputStream(new File("favorites.xml"));
           
           Properties p = new Properties();
           
           p.loadFromXML(fin);
           o.forEach(x1 ->{
               try{
                   if(p.containsValue(images.get(Integer.parseInt(x1.toString())))){
                   if(folders.getSelectionModel().getSelectedItem() != null){            
                        p.remove(new File(ACTUAL_SELECTED).getName()+"-"+new File(images.get(Integer.parseInt(x1.toString()))).getName(), images.get(Integer.parseInt(x1.toString())));

                   }else{
                       File file = new File(ACTUAL_SELECTED);
                       if(file.isFile()){
                        p.remove("NoName-"+new File(ACTUAL_SELECTED).getName(), ACTUAL_SELECTED);
                       }
                   }
                   }else{
                       a.setAlertType(AlertType.ERROR);
                       a.setTitle(String.valueOf(AlertType.ERROR));
                       a.setHeaderText("Error while removing the item.");
                       a.setContentText("Item has been never added.");
                       a.showAndWait();
                   }
                   fin.close();

                   FileOutputStream f = new FileOutputStream(new File("favorites.xml"));
                   p.storeToXML(f, null);
                   f.flush();
                   f.close();
               }catch(NumberFormatException | IOException ex){
                   
               }
               System.out.print(new File(images.get(Integer.parseInt(x1.toString()))).getName());
           });
       }else{
          a.setAlertType(AlertType.ERROR);
          a.setTitle("Error");
          a.setHeaderText("Error while searching for a file.");
          a.setContentText("The file: favorites.xml cannot be found.");
          a.showAndWait();
       }
       }finally{
           model_man.addFavoritesToList();
       }
    }

    private void animatePanelMove() {
        TranslateTransition openNav = new TranslateTransition(new Duration(350), pane);
        openNav.setToX(pane.getWidth());
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), pane);
        
        if(pane.getTranslateX() < pane.getWidth()){
            openNav.play();
        }else{
            closeNav.setToX(-(pane.getWidth()));
            closeNav.play();
        }
    }
    
    private class ModelManager{

        private void addFavoritesToList() throws FileNotFoundException, IOException {
            FileInputStream in; 
            Properties p = new Properties();
            
            
            if(Files.exists(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml").toPath())){
                in = new FileInputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
                p.loadFromXML(in);
                try{
                images.clear();
                p.forEach((action,action2) ->{
                   String[] split = action.toString().split("-");
                   if(new File(action2.toString()).exists()){
                   if(new File(ACTUAL_SELECTED).getName().equals(split[0])){
                       String pathToFile = p.getProperty(action.toString());
                       images.add(pathToFile);
                   }else{
                       if(!ACTUAL_SELECTED.isEmpty()){
                       if(new File(ACTUAL_SELECTED).isDirectory()){
                           if(new File(ACTUAL_SELECTED).getName().equals(split[0])){
                               String pathToFile = p.getProperty(action.toString());
                               images.add(pathToFile);
                           }
                       }else{
                           if(new File(new File(ACTUAL_SELECTED).getParent()).getName().equals(split[0])){
                               String pathToFile = p.getProperty(action.toString());
                                images.add(pathToFile);
                           }
                       }
                       }
                   }
                   }
                });
                }finally{
                    setFavoritesToList();
                }
            }else{
                a.setAlertType(AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Error while loading the favorites.");
                a.setContentText("The favorites table doesn't exist!");
                a.showAndWait();
            }
          System.gc();
    }
     
    private void setToImgView(Image img){
        image_view.setImage(img);
    }    
        
    private void selectImg(int index){
         image_list.getSelectionModel().select(index);
         image_list.scrollTo(index);
    }
   
        
    private void sortImageList(String sorting_option,String param) throws IOException, ParseException{
        if(!ACTUAL_SELECTED.isEmpty()){       
        switch(sorting_option){
            
            case "ext":   
                try{
               
                images.forEach(event ->{
                   System.out.print(event);
                    if(new File(event).getName().contains(param)){
                       sorted.add(event);
                    }
                });
                break;
             }finally{
                listFromTable();
             }
                
            case "owner":
            try{
                images.forEach(event ->{
                   System.out.print(event);
                    try {
                        UserPrincipal user = Files.getOwner(new File(event).toPath(), LinkOption.NOFOLLOW_LINKS);
                        System.out.print(user);
                        if(user.toString().equals(param)){
                            
                           sorted.add(event);
                        }
                    } catch (IOException ex) {
                      a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                    }
                    
                });
            
            }finally{
                listFromTable();
            }
             break;
             
            case "date":
                try{
                    LastModifiedFileComparator c = new LastModifiedFileComparator();
                    List<File> l = new ArrayList<>();
                        for(int i = 0; i<images.size(); i++){
                            l.add(new File(images.get(i)));
                        }
                    if(GIVEN_DATE){
                        
                        List<File> f = c.sort(l);
                        sorted.clear();
                        
                        f.forEach(x ->{
                           sorted.add(x.getAbsolutePath());
                        });
                    }else{
                        
                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                        SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy");
                        
                        Date file = new Date();
                        String d = df.format(file);
                        System.out.print(df2.format(new File("/home/lukas/Podręczne/DENIED/susann.png").lastModified())+"\n");
                        System.out.print(d);
                        sorted.clear();
                        
                        List<File> l1 = new ArrayList<>();
                        images.stream().filter((image) -> ( df2.format(new File(image).lastModified()).equals(d))).forEach((image) -> {
                            l1.add(new File(image));
                        });
                        
                        List<File> f = c.sort(l1);
                        f.forEach(x ->{
                            sorted.add(x.getAbsolutePath());
                        });
                    }
                }finally{
                    listFromTable();
                }
            break;
            
        }
        }
    }
    
    private void setInofrmationToModel(ArrayList<String> a){
        ObservableList<String> l = FXCollections.observableArrayList(a);
        information.setItems(l);
        information.refresh();
    }
    private void listFromTable(){
        ObservableList p = FXCollections.observableArrayList();
        images.clear();
        sorted.forEach(event ->{
            images.add(event);
        });
        
        images.forEach(event ->{
            p.add(event);
        });
        sorted.clear();
        image_list.setCellFactory(new CallbackImpl());
        image_list.getItems().clear();
        image_list.refresh();
        image_list.setItems(p);
        items_count.setText(String.valueOf(images.size()));
    }
    
    
    private void listImages(String dir) {
         try {
       if(!dir.isEmpty()){
                if(!dir.startsWith("https")||!dir.startsWith("http")){
               
                    Stream<Path> list = Files.list(new File(dir).toPath());
                    
                    ObservableList<String> o = image_list.getItems();
                    o.clear();
                    
                    image_list.setItems(o);
                    image_list.refresh();
                    images.clear();
                    image_list.setCellFactory(new CallbackImpl());
                    Consumer<Path> c = x -> {
                        try {
                            if(Files.probeContentType(new File(x.toString()).toPath()).contains("image/")){
                                o.add(x.toString());
                                images.add(x.toString());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    };
                    image_list.setItems(o);
                    list.forEach(c);
                    
                }else{
                   Image image;
                    try {
                        image = image_man.getImage(new URL(dir).toString());
                        image_view.setImage(image);
                        title.setText(new File(dir).getName());
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                    }
                   
                }
            }else{
                Image i = new Image(FXMLDocumentController.class.getResourceAsStream("jeżyk.jpg"));
                Image i2 = new Image(FXMLDocumentController.class.getResourceAsStream("para.jpg"));
                
                Image[] list = {i,i2};
                Random r = new Random();
                Image rand = list[(r.nextInt(0)+2)-1];
                model_man.setToImgView(rand);
            }
            }catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
            }
       items_count.setText(String.valueOf(images.size()));
       System.gc();
    }
        
    private void setFavoritesToList(){
        image_list.refresh();
        ObservableList<String> l = FXCollections.observableArrayList(images);
        if(l.size() > 0){
          image_list.setItems(l);
          image_list.setCellFactory(new CallbackImpl());
        }else{
            System.out.print("List length is 0");
        }
        System.gc();
    }
        
        private void prepareContextItems(){
            
        image_list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        MenuItem i = new MenuItem("Load favorites...");
        i.setOnAction(event ->{
            try {
                addFavoritesToList();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                
            } 
        });
        
        
        MenuItem i2 = new MenuItem("Add to favorites...");
        i2.setOnAction(event ->{
             try {
                 createFavoritesList();
                 
             }catch(IOException e){
                 Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
             }
        });
        
        MenuItem i4 = new MenuItem("Remove from favorites...");
        i4.setOnAction(event ->{
            try {
                removeFromFavorites();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        MenuItem i3 = new MenuItem("");
        i3.setOnAction(event ->{
            model_man.listImages(ACTUAL_SELECTED);
            if(LAST_SELECTED != -1){
                int index = model_man.getIndex()/2;
                image_list.scrollTo(Math.abs(LAST_SELECTED-index));
                image_list.getSelectionModel().clearAndSelect(LAST_SELECTED);
            }
        });
        
        context.getItems().add(i2);
        context.getItems().add(i);
        context.getItems().add(i3);
        context.getItems().add(i4);
        
        System.gc();
       }
        
        private int getIndex(){
            ListViewSkin<?> ts = (ListViewSkin<?>) image_list.getSkin();
            VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
            int first = vf.getFirstVisibleCell().getIndex();
            int last = vf.getLastVisibleCell().getIndex();
            int out = Math.abs(last-first);
            System.out.print(out);
            return out;
        }
        
       private void trayInit() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(new ImageIcon("iv.png").getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == 1){
                    trayIcon.displayMessage("Susanne", "Susanne Photo Viewer", TrayIcon.MessageType.INFO);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
              
            }
        });
        // Create a pop-up menu components
        java.awt.MenuItem aboutItem = new java.awt.MenuItem("About");
        java.awt.MenuItem reportItem = new java.awt.MenuItem("Report a bug");
        java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
        
        popup.add(aboutItem);
        popup.add(reportItem);
        popup.addSeparator();
        popup.add(exitItem);
        exitItem.addActionListener((action) ->{
           Platform.exit();
           System.exit(0);
        });
        aboutItem.addActionListener((action) ->{
           JOptionPane.showMessageDialog(null, "                  Susanne\n"+"ver: "+ver.toString()+"\nAuthor:Obsidiam\nLicense:Freeware", "About", JOptionPane.PLAIN_MESSAGE,new ImageIcon("icons/disc_small.png"));
        });
        reportItem.addActionListener(action ->{
           JOptionPane.showMessageDialog(null, new JTextField("Code: "+e.getErrorInfo(new UnsupportedOperationException())+" Email us at: http://neologysoftware.wix.com/main"),new UnsupportedOperationException().toString(),JOptionPane.INFORMATION_MESSAGE);
            
        });
        trayIcon.setPopupMenu(popup);
       
        try {
            tray.add(trayIcon);
            
        } catch (AWTException ex) {
            a.setAlertType(AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText(e.getErrorInfo(ex));
            a.setContentText(e.getErrorMessage(ex));
            a.showAndWait();
        }
    }
       
           @SuppressWarnings("restriction")
        public void getFirstAndLast(ListView<?> t) {
            try {
                ListViewSkin<?> ts = (ListViewSkin<?>) t.getSkin();
                VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
                first = vf.getFirstVisibleCell().getIndex();
                last = vf.getLastVisibleCell().getIndex();
            }catch (Exception ex) {}
        }

        public int getFirst() {
            return first;
        }

        public int getLast() {
            return last;
        }

        private void resize(double x, double y) {
              size.setText(String.valueOf(PERCENT/100f*x));
              image_view.setFitHeight(PERCENT/100f*x);
              image_view.setFitWidth(PERCENT/100f*y);
              System.gc();
        }

        private void resize() {
              size.setText(String.valueOf(PERCENT/100f*border.getWidth()));
              image_view.setFitHeight(PERCENT/100f*border.getWidth());
              image_view.setFitWidth(PERCENT/100f*border.getHeight());
              System.gc();
        }
    }
    
   private class CellImageAdd extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (item != null) {
                model_man.getFirstAndLast(image_list);
                int selected_index = this.getIndex();
                ImageView im;
                
                if(model_man.getFirst() <= selected_index | model_man.getLast() >= selected_index){
                    Image i = new Image("file:///"+item,64,64,true,true);
                    im = new ImageView(i);
                    setGraphic(im);
                    setAlignment(Pos.CENTER);
                    System.out.println(item);
                }else{
                    im = new ImageView(iv_img);
                    im.setCache(true);
                    setGraphic(im);
                    setAlignment(Pos.CENTER);
                }
            }
        }
    }

    private class CallbackImpl implements Callback<ListView<String>, ListCell<String>> {
        @Override
        public ListCell<String> call(ListView<String> list) {
            return new CellImageAdd();
        }
    }
   private class ImageProperties {
      private ArrayList<String> getInformation(String path) throws IOException{
          ArrayList<String> a = new ArrayList<>();
          File f = new File(path);
          String name = f.getName();
          long size = f.length()/1024;
          long tl = f.lastModified();
          SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
          String ft = sdf.format(tl);
          String user = Files.getOwner(new File(path).toPath(), LinkOption.NOFOLLOW_LINKS).getName();
          Image i = image_man.getImage(path);
          a.add("Name: "+name);
          a.add("Size: "+String.valueOf(size)+"kB");
          a.add("Original dimension: "+i.getWidth()+","+i.getHeight());
          a.add("Viewed dimension: "+image_view.getFitWidth()+","+image_view.getFitHeight());
          a.add("Date modified: "+ft);
          a.add("Owner: "+user);
          return a;
      }
   }
  
   
}

