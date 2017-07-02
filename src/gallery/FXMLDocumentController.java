/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import gallery.enums.Environment;
import gallery.googlesync.Authorization;
import gallery.googlesync.DownloadFiles;
import gallery.image.ImageManager;
import gallery.systemproperties.EnvVars;
import gallery.parsing.XMLManager;
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
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import gallery.image.ImageManager.ImageProperties;
import gallery.parsing.JSONController;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

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
    private Label prev_lbl,next_lbl,title,menu_label,items_count,size,elements,rotate_right,rotate_left;
    @FXML
    private ComboBox folders;
    @FXML
    private MenuItem reset,delete,about,fullscreen,save,close,rename,remove,open,open_file,codes;
    @FXML
    private CheckMenuItem ext,owner,date,find_date;
    @FXML
    private ContextMenu context;
    @FXML
    private Pane pane;
    @FXML
    private ScrollPane scroll;
    @FXML
    private HBox front_panel;
    @FXML
    private Button gd_sync;
    
    private double rot_arc;
    private int first,last;
    private DoubleProperty zoom;
    private ArrayList<String> sorted = new ArrayList();
    private static ArrayList<String> paths = new ArrayList<>();
    private static ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> google_files = new ArrayList<>();
    private int selection = -1;
    private int LAST_SELECTED = -1;
    private double X = 500;
    private String ACTUAL_SELECTED = "";
    private Alert a = new Alert(AlertType.INFORMATION);
    private Image next_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/next.png"));
    private Image prev_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/prev.png"));
    private Image menu_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/menu.png"),32,32,true,true);
    private Image gd_sync_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/gd_icon48x48.png"),32,32,true,true);
    private Image iv_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/iv.png"),48,48,true,true);
    private Image rotate_right_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/rotate_right.png"),32,32,true,true);
    private Image rotate_left_img = new Image(FXMLDocumentController.class.getResourceAsStream("images/rotate_left.png"),32,32,true,true);
    private ModelManager model_man = new ModelManager();
    private ImageManager image_man = new ImageManager();
    private ErrorLogger e = new ErrorLogger();
    AppVersion ver = new AppVersion();
    private String OWNER = System.getProperty("user.name");
    private static XMLManager XML = XMLManager.getInstance();
    EnvVars ENV = new EnvVars();
    private boolean isAuthorized = false;
    private String nick = "";
    private JSONController PARSER = JSONController.getInstance();
    
    {
        Runtime r = Runtime.getRuntime();
        r.traceInstructions(true);
        r.traceMethodCalls(true);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FileWriter fw;
        try {
            fw = new FileWriter(new File("debug"));
       
        BufferedWriter bw = new BufferedWriter(fw);

        
        bw.append("Initializing...");
        bw.newLine();

        e.prepareErrorList();
        model_man.trayInit();
        model_man.prepareContextItems();
        gd_sync.setGraphic(new ImageView(gd_sync_img));
        bw.append("Initialized.");
        bw.newLine();
        bw.append("Initializing listeners...");
        bw.newLine();
        GCRunner run = new GCRunner();
        run.start();



        try {
            paths = XML.getFolderList();
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
        
        
        zoom = new SimpleDoubleProperty(image_view.getFitWidth());
         
        zoom.addListener(listener ->{ 
            model_man.resize(); 
        });
        
        rotate_right.setGraphic(new ImageView(rotate_right_img));
        rotate_left.setGraphic(new ImageView(rotate_left_img));
        
        rotate_right.setOnMouseClicked(event ->{
            rot_arc += 90.0;
            model_man.rotate();
        });
        
        rotate_left.setOnMouseClicked(event ->{
            rot_arc -= 90.0;
            model_man.rotate();
        });


        image_list.setOnMouseClicked((event) ->{
            if(event.getButton() == MouseButton.SECONDARY){
                selection = image_list.getSelectionModel().getSelectedIndex();
                MenuItem m = context.getItems().get(2);
               
                if(PATH != null){
                    String name = new File(PATH).getName();
                    m.setText("Back to "+name);
                }else{
                    m.setText("Back to ");
                }
                context.show(image_list, event.getScreenX(),event.getScreenY());
            }else{
               System.gc();
               selection = image_list.getSelectionModel().getSelectedIndex();
               LAST_SELECTED = selection;
               ACTUAL_SELECTED = images.get(selection);
               
               if(selection != -1&selection == images.indexOf(ACTUAL_SELECTED)){
                   
                 Image loaded = image_man.getImage(images.get(selection));
                 model_man.setToImgView(loaded);
                 
                 ImageProperties im = ImageProperties.getOuter();
                 ArrayList inf = new ArrayList();
                   try {
                       //System.out.println(new File(images.get(selection)).getParent());
                       im.handleFileData(new File(images.get(selection)));
                       if(Files.probeContentType(new File(images.get(selection)).toPath()).contains("gif")){
                          ArrayList tmp = im.getInformation();
                          for(int i = 0; i < 15; i++){
                              inf.add(tmp.get(i));
                          }
                          tmp.clear();
                       }else{
                          inf = im.getInformation();
                       }
                       model_man.setInformationToModel(inf);
                       
                   } catch (IOException ex) {
                       Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                       a.setAlertType(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while vieweing information.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                   } catch (JpegProcessingException ex) {
                       Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                   }
               }
               
            }
        });

        image_list.setOnScrollFinished(event ->{
            System.out.println("Finished.");
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
        
        
        
        image_view.setOnMouseClicked(event ->{
            if(event.getClickCount() == 2){
                fullscreen.fire();
            }
        });
        
        close.setOnAction(event ->{
            System.exit(0);
        });
        
        save.setOnAction((event) ->{
            ArrayList<String> list = new ArrayList<>();
            XMLManager xml = new XMLManager();
            list.add(PATH);
            folders.getItems().forEach(x ->{
                System.out.println(x);
                list.add(x.toString());
            });
            
            try {
                xml.saveFoldersList(list);
            } catch (IOException ex) {
               a.setAlertType(Alert.AlertType.ERROR);
               a.setTitle("Rename");
               a.setHeaderText("Error while renaming the file.");
               a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
               a.showAndWait();
               Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        folders.setOnAction((event) ->{
            Object o = folders.getSelectionModel().getSelectedItem();
            model_man.listImages(paths.get(folders.getItems().indexOf(o))); 
        });
        
        pane.setOnMouseClicked(event ->{
            animatePanelMove();
        });
        
        fullscreen.setOnAction((event) ->{
            Stage stage2 = (Stage) root.getScene().getWindow();
     
            if(stage2.isFullScreen()){
                size.setText(String.valueOf(100.0));
                model_man.resize();
                stage2.setFullScreen(false);
            }else{
                model_man.resize();
                stage2.setFullScreen(true); 
            }
        });
        
        about.setOnAction(event ->{
            a.setAlertType(AlertType.INFORMATION);
            a.setHeaderText("Joanne");
            a.setContentText("Joanne\nAuthor: Obsidiam\nver:"+ver.selectAndGetVersion(0)+"\nLicense:GNU GPL v.3.0 or newer\n");
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
                in = new FileInputStream(new EnvVars().getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"folders.xml");
                p.loadFromXML(in);
                
                in.close();
                for(int i = 0; i<p.size(); i++){
                    String get = paths.get(i);
                    if(new File(get).getName().equals(folders.getSelectionModel().getSelectedItem())){
                       p.remove(get);
                    }
                }
                FileOutputStream f = new FileOutputStream(new EnvVars().getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"folders.xml");
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

        root.setOnKeyTyped(event ->{
            if(selection != -1){
                if(event.getCharacter().equals("+")){
                    model_man.resize();
                    size.setText(String.valueOf(image_view.getFitWidth()/X*100));
                }

                if(event.getCharacter().equals("-")){
                    model_man.resize();
                    size.setText(String.valueOf(image_view.getFitWidth()/X*100));
                }
            }
            event.consume();
        });
        
        root.setOnMouseMoved(event ->{
           double y = event.getSceneY();
           if(y < 100){
               animateFrontPanelMove(false);
           }else{
               animateFrontPanelMove(true);
           }
        });
        
        image_view.setOnMouseEntered(event -> {
             image_view.setCursor(Cursor.HAND);
        });
        
        image_view.setOnKeyPressed(event ->{
               fullscreen.fire();
        });
        
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
           
                TextInputDialog in = new TextInputDialog(".png");
                Optional<String> change = in.showAndWait();
                change.ifPresent(present ->{
                    try {
                        if(!present.isEmpty()){
                        System.out.print(present);

                        model_man.sortImageList("ext", present);
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
            event.consume();
        });
        
        owner.setOnAction(event ->{
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
           
            event.consume();
        });
        
        find_date.setOnAction(event ->{
            Date date2 = new Date();
            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            TextInputDialog in = new TextInputDialog(df.format(date2));
            Optional<String> change = in.showAndWait();
            items_count.setText(String.valueOf(images.size()));
            
            change.ifPresent(event2 ->{
                try {
                   
                    model_man.sortImageList("find_date", event2);  

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
        });

        date.setOnAction(event ->{
                try { 
                    model_man.sortImageList("date", new File(PATH).getParent());
                } catch (IOException | ParseException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
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
            PATH = new File(ACTUAL_SELECTED).getParent();
            System.out.println(PATH);
            Image load = image_man.getImage(ACTUAL_SELECTED);
            model_man.setToImgView(load);
            model_man.listImages(PATH);
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
        
        gd_sync.setOnAction(event ->{
            TextInputDialog d = new TextInputDialog(ENV.getEnvironmentVariable(Environment.USER_NAME));
            Optional<String> opt = d.showAndWait();
            
            opt.ifPresent(event2 ->{
                if(!event2.isEmpty()){
                    nick = event2;
                    GoogleService g  = new GoogleService();
                    
                    g.start();
                    model_man.generateDialog(g);
                }
            });
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
        
        image_list.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL_FINISHED, event -> {
            System.out.println("Scroll finished.");
            System.gc();
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
        scroll.setOnMouseClicked(event ->{
            int count = event.getClickCount();
            
            if(count == 2){
                fullscreen.fire();
            }
        });
    }  
      
    private void accessGoogleDrive(String nick,String operation) throws IOException{
        
        if(!isAuthorized){
            Authorization.authorize();
            Authorization.setNick(nick);
        }
        
        switch(operation){
            case "download":
                PARSER.readArray();
                
                DownloadFiles.getInstance().downloadFiles();
                google_files = Authorization.getFilesList();
                model_man.listFromGoogleTable();
                break;
            case "upload":
                
                break;
            case "listFiles":
                
                break;
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
    
    private void animateFrontPanelMove(boolean isFarAway) {
        TranslateTransition openNav = new TranslateTransition(new Duration(350), front_panel);
        openNav.setToY(-front_panel.getHeight());
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), front_panel);
        closeNav.setToY(0.0);
        
        if(!isFarAway){
            closeNav.play();
        }else{
            openNav.play();
        }
    }

    
    private class ModelManager{
        private void addFavoritesToList() throws FileNotFoundException, IOException {
            FileInputStream in; 
            Properties p = new Properties();
            System.out.println(ACTUAL_SELECTED);
            
            
            if(Files.exists(new File(new EnvVars().getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"favorites.xml").toPath())){
                in = new FileInputStream(new File(new EnvVars().getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"favorites.xml"));
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
        image_list.getSelectionModel().clearAndSelect(index);
        image_list.scrollTo(index);
    }
   
        
    private void sortImageList(String sorting_option,String param) throws IOException, ParseException{
        System.out.println(param);
        Sorter s = new Sorter(images,sorted);
        s.chooseSortAlgorithm(sorting_option, param);
        s.getSortedList();
        listFromTable();
    }
    
    private void setInformationToModel(ArrayList<String> a){
        ObservableList<String> l = FXCollections.observableArrayList(a);
        information.setItems(l);
        information.refresh();
    }
    
    private void listFromTable(){
        ObservableList p = FXCollections.observableArrayList();
        images.clear();
        images.addAll(sorted);
        p.addAll(images);
        sorted.clear();
        image_list.setCellFactory(new CallbackImpl());
        image_list.getItems().clear();
        image_list.refresh();
        image_list.setItems(p);
        items_count.setText(String.valueOf(images.size()));
    }
    
    public void listFromGoogleTable() throws IOException{
       images.clear();
       String gd_dir = ENV.getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"google_drive";
       Files.list(new File(gd_dir).toPath()).forEach(image ->{
           images.add(gd_dir+File.separator+image);
       });
    }
    
    private void listImages(String dir) {
         try {
            if(!dir.isEmpty()){
                    Stream<Path> list = Files.list(new File(dir).toPath());
                    
                    ObservableList<String> o = FXCollections.observableArrayList();
                    image_list.getItems().clear();
                    images.clear();
                    list.forEach(x -> {
                        try {
                            if(Files.probeContentType(new File(x.toString()).toPath()).contains("image/")){
                                o.add(x.toString());
                                images.add(x.toString());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    image_list.setItems(o);
                    image_list.setCellFactory(new CallbackImpl());
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
       image_list.refresh();
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
                 ObservableList o = image_list.getSelectionModel().getSelectedIndices();
                 Object selected = folders.getSelectionModel().getSelectedItem();
                 XML.createFavoritesList(o,selected,ACTUAL_SELECTED,images);
                 
             }catch(IOException e){
                 Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
             }
        });
        
        MenuItem i4 = new MenuItem("Remove from favorites...");
        i4.setOnAction(event ->{
            try {
                ObservableList o = image_list.getSelectionModel().getSelectedIndices();
                Object selected = folders.getSelectionModel().getSelectedItem();
                try{
                boolean isDone = XML.removeFromFavorites(o,selected,ACTUAL_SELECTED,images);
                if(isDone){
                   a.setAlertType(AlertType.INFORMATION);
                   a.setTitle("Removing");
                   a.setContentText("All items has been removed from favorties.");
                   a.setHeaderText("Removing successful");
                }else{
                   a.setAlertType(AlertType.ERROR);
                   a.setTitle("Removing");
                   a.setContentText("An error occured while removing items.");
                   a.setHeaderText("Removing unsuccessful."); 
                }
                a.showAndWait();
                }finally{
                    model_man.addFavoritesToList();
                }
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        MenuItem i3 = new MenuItem();
        i3.setOnAction(event ->{
            model_man.listImages(PATH);
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
        
     private void generateDialog(GoogleService t) {
            Dialog<ArrayList<String>> dialog = new Dialog<>();
            dialog.setWidth(300);
            dialog.setTitle("Sync with Google Drive");
            dialog.setHeaderText("Google Drive Sync");
            
            ButtonType loginButtonType = new ButtonType("View", ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);

            VBox vbox = new VBox();
            vbox.setSpacing(10);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPrefWidth(dialog.getWidth());
            vbox.getChildren().add(new Label("Wait, sync is in progress."));
            ProgressBar p = new ProgressBar();
            p.setPrefWidth(300);

            vbox.getChildren().add(p);

            // Enable/Disable login button depending on whether a username was entered.
            Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
            loginButton.setDisable(true);

            Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
            cancelButton.setOnMouseClicked(event ->{
                t.cancel();
                DownloadFiles.getInstance().stop();
            });
            
            t.setOnCancelled(event ->{
                System.out.println("Downloading cancelled.");
                t.cancel();
            });
            
            t.setOnSucceeded(success_evt ->{
                System.out.println("Succeded.");
                loginButton.setDisable(false);
                cancelButton.setDisable(true);
            });
            dialog.getDialogPane().setContent(vbox);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    try {
                        model_man.listFromGoogleTable();
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return images;
                }
                return null;
            });

            Optional<ArrayList<String>> result = dialog.showAndWait();

            result.ifPresent(usernamePassword -> {
               ObservableList list = FXCollections.observableArrayList(images);
               image_list.setCellFactory(new CallbackImpl());
               image_list.getItems().clear();
               image_list.refresh();
               image_list.setItems(list);
               items_count.setText(String.valueOf(images.size()));
            });
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
        
       private void trayInit() throws MalformedURLException {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(new ImageIcon(new URL(getClass().getResource("/gallery/images/iv.png").toExternalForm()).toString()).getImage());
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == 1){
                    trayIcon.displayMessage("Joanne", "Joanne Photo Viewer", TrayIcon.MessageType.INFO);
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
           JOptionPane.showMessageDialog(null, "                  Joanne\n"+"ver: "+ver.toString()+"\nAuthor:Obsidiam\nLicense:Freeware", "About", JOptionPane.PLAIN_MESSAGE,new ImageIcon("icons/disc_small.png"));
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

        private void resize() {
              size.setText(String.valueOf((zoom.get()/X)*100));
              image_view.setFitHeight(zoom.get());
              image_view.setFitWidth(zoom.get());
        }

        private void rotate() {
            image_view.setRotate(rot_arc);
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
               try{
                if(model_man.getFirst() <= selected_index | model_man.getLast() >= selected_index){
                    if(!Files.probeContentType(Paths.get(item)).equals("image/gif")){
                        Image i = new Image("file:///"+item,64,64,true,true);
                        im = new ImageView(i);
                        setGraphic(im);
                        setAlignment(Pos.CENTER);
                    }else{
                        BufferedImage i = image_man.readGif(item);
                        ImageIO.write(i, "GIF", new File(ENV.getEnvironmentVariable(Environment.TEMP_DIR)+File.separator+"joanne"+File.separator+new File(item).getName()));
                        Image im2 = new Image("file:///"+ENV.getEnvironmentVariable(Environment.TEMP_DIR)+File.separator+"joanne"+File.separator+new File(item).getName(),64,64,true,true);
                        im = new ImageView(im2);
                        setGraphic(im);
                        setAlignment(Pos.CENTER);
                    }
                }
               }catch(IOException e){
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
    
  public class GCRunner extends Thread implements Runnable{
      private Thread TH;
      @Override
      public void start(){
          if(TH == null){
              TH = new Thread(this,"GCRunner");
              TH.setDaemon(true);
              TH.start();
          }
      }
      
      @Override
      public void run(){
          Runtime r = Runtime.getRuntime();
          while(Thread.currentThread() == TH){
              if(!image_list.isHover()){
                  long free = r.freeMemory()/(1024*1024);
                  long tot = r.totalMemory()/(1024*1024);
                  if(tot > 40L){
                      System.gc();
                      System.out.println("GC.");
                      System.out.println(free+" MB");
                      System.out.println(tot+" MB");
                      try {
                          Thread.sleep(5000);
                      } catch (InterruptedException ex) {
                          Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                      }
                  } 
              }
          }
      }
  }
  
  private class GoogleService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    try {
                        accessGoogleDrive(nick,"download");
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return null;
                }
                
                @Override
                protected void cancelled(){
                    super.cancelled();
                    DownloadFiles.getInstance().stop();
                    this.cancel(true);
                }
                
            };
        }
    }
  
}

