/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.image;

import com.drew.imaging.bmp.BmpMetadataReader;
import com.drew.imaging.gif.GifMetadataReader;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngMetadataReader;
import com.drew.imaging.png.PngProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpReader;
import gallery.ErrorLogger;
import gallery.systemproperties.EnvVars;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
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
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import com.drew.metadata.jfif.JfifReader;
import gallery.FXMLDocumentController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Obsidiam
 */
 public class ImageManager{
    private ErrorLogger e = new ErrorLogger();   
    private EnvVars env = new EnvVars();
    
    

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

        public static class ImageProperties {
            private Object[] FORMATS = new Object[]{new ExifReader(), new IptcReader(),new JfifReader()};
            private int selector = 0;
            
              private ArrayList<String> out = new ArrayList<>();
              private Iterable readers = null;

              public ArrayList<String> getInformation() throws IOException{
                  return out;
              }

              private void createMetadataFormatList(Object[] formats){
                   readers = Arrays.asList(formats);
              }

              public void handleFileData(File file) throws JpegProcessingException, IOException{
                    createMetadataFormatList(FORMATS);
                    if(file.isFile()){
                        Metadata metadata = readMetdataFor(file);
                        setToArray(metadata);
                    }
              }

              public void setToArray(Metadata metadata){   
                for (Directory directory : metadata.getDirectories()) {
                    directory.getTags().forEach((tag) -> {
                        if (!directory.hasErrors()) {
                            out.add(tag.getTagName()+" "+tag.getDescription());
                            System.out.println(tag.toString());
                        }
                    });
                }
              }

                public Metadata readMetdataFor(File f) throws JpegProcessingException {
                  Metadata m = null;
                  try {
                      String mime = Files.probeContentType(f.toPath());
                      System.out.println(mime);
                      switch(mime){
                          case "image/jpeg":
                              m = JpegMetadataReader.readMetadata(f,readers);
                              break;
                          case "image/png":
                              m = PngMetadataReader.readMetadata(f);
                              break;
                          case "image/gif":
                              m = GifMetadataReader.readMetadata(f);
                              break;
                          case "image/bmp":
                              m = BmpMetadataReader.readMetadata(f);
                              break;
                      }
                  } catch (IOException | PngProcessingException ex) {
                      Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                  }
                  return m;
                }
                
                public static ImageProperties getOuter() {
                        return new ImageProperties();
                }
                public Object[] getAllAccessibleFormats(){
                    return FORMATS;
                }
       }
    
 }
