/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author lukas
 */
public class XMLManager {
    private static volatile XMLManager XML;
    
    public static synchronized XMLManager getInstance(){
        if(XML == null){
            XML = new XMLManager();
        }
        return XML;
    }
    public Properties getFoldersList() throws FileNotFoundException, IOException{
        Properties p = new Properties();
                
                if(Files.exists(new File("folders.xml").toPath())){
                     FileInputStream in = new FileInputStream(new File("folders.xml"));
                    p.loadFromXML(in);
                    BiConsumer<Object,Object> bi = (x,y) ->{
                        p.getProperty(x.toString(), new File(y.toString()).getName());
                    };
                    p.forEach(bi);

                }
                return p;
    }
    
    public boolean saveFoldersList(ArrayList<String> list) throws FileNotFoundException, IOException{
            Properties p = getFoldersList(); 
                String pa = "";
                if(new File(pa).isFile()){
                    pa = new File(pa).getParent();
                }
                FileOutputStream f = new FileOutputStream(new File("folders.xml"));
                if(!p.containsKey(pa)){
                    p.setProperty(new File(pa).getAbsolutePath(),new File(pa).getName());
                }else{
                    return false;
                }
                p.storeToXML(f,null);
                ObservableList<String> s = FXCollections.observableArrayList(list);
                s.add(0, new File(pa).getName());
                return true;
    }
    
    
    public void createFavoritesList(ObservableList o, Object selectedItem, String actual,ArrayList<String> images) throws FileNotFoundException, IOException{        
       if(!Files.exists(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml").toPath())){
           FileOutputStream f = new FileOutputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
           Properties p = new Properties();
           
          o.forEach(x ->{
              System.out.print(x);
              try{
               if(selectedItem != null){
                
                   System.out.print(o);
                   p.setProperty(selectedItem.toString()+"-"+new File(images.get(Integer.parseInt(x.toString()))).getName(), images.get(Integer.parseInt(x.toString())));

               }else{
                   File file = new File(images.get(Integer.parseInt(x.toString())));
                   p.setProperty("NoName-"+new File(actual).getName(), file.getAbsolutePath());
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
                   if(selectedItem != null){
                        p.setProperty(new File(actual).getName()+"-"+new File(images.get(Integer.parseInt(x1.toString()))).getName(), images.get(Integer.parseInt(x1.toString())));
                   }else{
                       File file = new File(actual);
                       if(file.isFile()){
                        p.setProperty("NoName-"+new File(actual).getName(), actual);
                       }
                   }
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
    
    public boolean removeFromFavorites(ObservableList o, Object selectedItem, String actual,ArrayList<String> images) throws FileNotFoundException, IOException{
       if(Files.exists(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml").toPath())){
           FileInputStream fin = new FileInputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
           
           Properties p = new Properties();
           
           p.loadFromXML(fin);
           o.forEach(x1 ->{
               try{
                   if(p.containsValue(images.get(Integer.parseInt(x1.toString())))){
                   if(selectedItem != null){            
                        p.remove(new File(actual).getName()+"-"+new File(images.get(Integer.parseInt(x1.toString()))).getName(), images.get(Integer.parseInt(x1.toString())));

                   }else{
                       File file = new File(actual);
                       if(file.isFile()){
                        p.remove("NoName-"+new File(actual).getName(), actual);
                       }
                   }
                   }
                   fin.close();

                   FileOutputStream f = new FileOutputStream(new File(System.getProperty("user.home")+File.separatorChar+"favorites.xml"));
                   p.storeToXML(f, null);
                   f.flush();
                   f.close();
               }catch(NumberFormatException | IOException ex){
                   
               }
               System.out.print(new File(images.get(Integer.parseInt(x1.toString()))).getName());
           });
           return true;
       }else{
           return false;
       }
    }
}
