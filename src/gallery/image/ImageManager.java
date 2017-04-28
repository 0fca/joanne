/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.image;

import gallery.ErrorLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 *
 * @author Obsidiam
 */
 public class ImageManager{
     private ErrorLogger e = new ErrorLogger();   

      public ArrayList<String> loadFolders() throws FileNotFoundException, IOException {
        Properties p = new Properties();
        File f = new File("folders.xml");
        ArrayList<String> paths = new ArrayList<>();
        System.out.print(f.getAbsoluteFile());
        if(!new File(f.getAbsolutePath()).exists()){
            
            String os = System.getProperty("os.name");
            if(os.equals("Linux")){
                f = new File("/home/"+System.getProperty("user.name")+"/folders.xml").getAbsoluteFile();
            }else{
                if(os.contains("Windows")){
                  f = new File("C:\\Users\\"+System.getProperty("user.name")+"\\folders.xml").getAbsoluteFile(); 
                }   
            }
        }
        FileInputStream in = new FileInputStream(f);
        p.loadFromXML(in);
        p.forEach((x,y)->{
            paths.add(x.toString());
        });
        return paths;
    }

      
      
    public Image getImage(String get_path) {
       Image im = new Image("file:///"+get_path);
           
       double x = im.getWidth();
       double y = im.getHeight();
       Image image = new Image("file:///"+get_path,x,y,true,true);
       return image;
    }

    
        public void renameImage(String path,String file_to_rename) {
             TextInputDialog input = new TextInputDialog(file_to_rename.substring(0, file_to_rename.length()-4)+"1"+file_to_rename.substring(file_to_rename.length()-4));
                Optional<String> change = input.showAndWait();
                
                change.ifPresent((String change_event) -> {
                    try {
                        Files.move(new File(path).toPath(),new File(new File(path).getParent()+File.separator+change_event).toPath());
                    } catch (IOException ex) {
                       Alert a = new Alert(AlertType.ERROR);
                       a.setTitle("Rename");
                       a.setHeaderText("Error while renaming the file.");
                       a.setContentText("Error code: "+e.getErrorInfo(ex)+"\n"+e.getErrorMessage(ex));
                       a.showAndWait();
                    }
             });
                System.gc();
        }

    public File chooseFile() {
        FileChooser f = new FileChooser();
        FileChooser.ExtensionFilter[] extFilter = new FileChooser.ExtensionFilter[]{new FileChooser.ExtensionFilter("JFIF","*.jpg"),new FileChooser.ExtensionFilter("PNG","*.png"),new FileChooser.ExtensionFilter("BMP","*.bmp"),new FileChooser.ExtensionFilter("GIF", "*.gif")};
        f.getExtensionFilters().addAll(extFilter);
        File file = f.showOpenDialog(null);
        if(file != null){
            return file;
        }else{
            return null;
        }
    }
        
    }
