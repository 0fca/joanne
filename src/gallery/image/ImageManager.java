/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.image;

import gallery.ErrorLogger;
import gallery.systemproperties.EnvVars;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
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
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Obsidiam
 */
 public class ImageManager{
     private ErrorLogger e = new ErrorLogger();   
     private EnvVars env = new EnvVars();
     
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

    public BufferedImage readGif(String path) throws IOException{
    ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
    ImageInputStream ciis = ImageIO.createImageInputStream(new File(path));
    reader.setInput(ciis);

    int lastx = 0;
    int lasty = 0;

    int width = -1;
    int height = -1;

    IIOMetadata metadata = reader.getStreamMetadata();

    Color backgroundColor = null;

    if(metadata != null) {
        IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

        NodeList globalColorTable = globalRoot.getElementsByTagName("GlobalColorTable");
        NodeList globalScreeDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

        if (globalScreeDescriptor != null && globalScreeDescriptor.getLength() > 0){
            IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreeDescriptor.item(0);

            if (screenDescriptor != null){
                width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
            }
        }

        if (globalColorTable != null && globalColorTable.getLength() > 0){
            IIOMetadataNode colorTable = (IIOMetadataNode) globalColorTable.item(0);

            if (colorTable != null) {
                String bgIndex = colorTable.getAttribute("backgroundColorIndex");

                IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
                while (colorEntry != null) {
                    if (colorEntry.getAttribute("index").equals(bgIndex)) {
                        int red = Integer.parseInt(colorEntry.getAttribute("red"));
                        int green = Integer.parseInt(colorEntry.getAttribute("green"));
                        int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

                        backgroundColor = new Color(red, green, blue);
                        break;
                    }

                    colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
                }
            }
        }
    }

    BufferedImage master = null;
    boolean hasBackround = false;

    for (int frameIndex = 0;; frameIndex++) {
        BufferedImage image;
        try{
            image = reader.read(frameIndex);
        }catch (IndexOutOfBoundsException io){
            break;
        }

        if (width == -1 || height == -1){
            width = image.getWidth();
            height = image.getHeight();
        }

        IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
        IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
        NodeList children = root.getChildNodes();
        
        if (master == null){
            master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            master.createGraphics().setColor(backgroundColor);
            master.createGraphics().fillRect(0, 0, master.getWidth(), master.getHeight());

        hasBackround = image.getWidth() == width && image.getHeight() == height;

            master.createGraphics().drawImage(image, 0, 0, null);
        }else{
            int x = 0;
            int y = 0;

          
                Node nodeItem = children.item(0);

                if (nodeItem.getNodeName().equals("ImageDescriptor")){
                    NamedNodeMap map = nodeItem.getAttributes();

                    x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                    y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                }
 
            master.createGraphics().drawImage(image, x, y, null);

            lastx = x;
            lasty = y;
        }

        {
            BufferedImage copy;

            {
                ColorModel model = master.getColorModel();
                boolean alpha = master.isAlphaPremultiplied();
                WritableRaster raster = master.copyData(null);
                copy = new BufferedImage(model, raster, alpha, null);
            }
   
        }

        master.flush();
    }
    reader.dispose();

    return master;
}
      
      
    public Image getImage(String get_path) {
       Image image = new Image("file:///"+get_path);
       
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
