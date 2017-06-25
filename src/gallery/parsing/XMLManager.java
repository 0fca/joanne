/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.parsing;

import gallery.enums.Environment;
import gallery.systemproperties.EnvVars;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import java.util.function.BiConsumer;
import javafx.collections.ObservableList;

/**
 *
 * @author Obsidiam
 */
public class XMLManager {
    private static volatile XMLManager XML;
    private Properties p = new Properties();
    private static String PATH = new EnvVars().getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"folders.xml";
    private static String PATHF = new EnvVars().getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"favorites.xml";
     
    public static synchronized XMLManager getInstance(){
        if(XML == null){
            XML = new XMLManager();
        }
        return XML;
    }
    
    private Properties getFoldersList() throws FileNotFoundException, IOException{
        Properties p = new Properties();
                
            if(Files.exists(new File(PATH).toPath())){
                FileInputStream in = new FileInputStream(new File(PATH));
                p.loadFromXML(in);
                BiConsumer<Object,Object> bi = (x,y) ->{
                    p.getProperty(x.toString(), new File(y.toString()).getName());
                };
                p.forEach(bi);

            }
            return p;
    }
    
    public ArrayList<String> getFolderList() throws FileNotFoundException, IOException{
        ArrayList<String> a = new ArrayList<>();
                
            if(Files.exists(new File(PATH).toPath())){
                FileInputStream in = new FileInputStream(new File(PATH));
                p.loadFromXML(in);
                
                p.forEach((x,y) ->{
                    a.add(x.toString());
                    System.out.println(y);
                });

            }
            return a;
    }
    
    public boolean saveFoldersList(ArrayList<String> list) throws FileNotFoundException, IOException{
            Properties p = getFoldersList(); 
            String pa = "";
            if(new File(pa).isFile()){
                pa = new File(pa).getParent();
            }
            FileOutputStream f = new FileOutputStream(new File(PATH));
            if(!p.containsKey(pa)){
                p.setProperty(new File(pa).getAbsolutePath(),new File(pa).getName());
            }else{
                return false;
            }
            p.storeToXML(f,null);
            return true;
    }
    
    public void createFavoritesList(ObservableList o, Object selectedItem, String actual,ArrayList<String> images) throws FileNotFoundException, IOException{        
       if(!Files.exists(new File(PATHF).toPath())){
           FileOutputStream f = new FileOutputStream(new File(PATHF));
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
           FileInputStream fin = new FileInputStream(new File(PATHF));
           
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
           FileOutputStream f = new FileOutputStream(new File(PATHF));
           p.storeToXML(f, null);
           f.flush();
           f.close();
       }
    }
    
    public boolean removeFromFavorites(ObservableList o, Object selectedItem, String actual,ArrayList<String> images) throws FileNotFoundException, IOException{
       if(Files.exists(new File(PATHF).toPath())){
           FileInputStream fin = new FileInputStream(new File(PATHF));
           
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

                   FileOutputStream f = new FileOutputStream(new File(PATHF));
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

    
    public void setEnvConfiguration() throws MalformedURLException, FileNotFoundException, IOException{
        InputStream in = XMLManager.class.getResourceAsStream("/gallery/configs/properties.xml");
        
        p.loadFromXML(in);
        p.forEach((x,y)->{
            if(x.toString().equals("default.photo.store")){
                System.setProperty(x.toString(), y.toString());
            }
            if(x.toString().equals("xml.linux")&System.getProperty("os.name").contains("Linux")){
                System.setProperty("xml.path", y.toString());
            }else if(x.toString().equals("xml.windows")&System.getProperty("os.name").contains("Windows")){
                String[] arr = y.toString().split("\\\\");
                System.setProperty("xml.path", arr[0]+"\\"+arr[1]+"\\"+System.getProperty("user.name")+"\\"+arr[3]+"\\"+arr[4]+"\\"+arr[5]);
            }
            
            if(x.toString().equals("default.temp.store.linux")&System.getProperty("os.name").equals("Linux")){
                System.setProperty("temp.store", y.toString());
            }else if(x.toString().equals("default.temp.store.win")&System.getProperty("os.name").contains("Windows")){
                String[] arr = y.toString().split("\\\\");
                System.setProperty("temp.store", arr[0]+"\\"+arr[1]+"\\"+System.getProperty("user.name")+"\\"+arr[3]+"\\"+arr[4]+"\\"+arr[5]);
            }}
        );
        
    }
    
    public String getPhotoStore(){
        return p.getProperty("default.photo.store");
    }
}
