/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.googlesync;

import com.google.api.services.drive.Drive;
import static gallery.googlesync.Authorization.getDriveService;
import gallery.systemproperties.EnvVars;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Obsidiam
 */
public class DownloadFiles {
    private static Drive driveService = null;
    private static EnvVars ENV = new EnvVars();
    private static  FileOutputStream fs;
    private static DownloadFiles DF;
    
    public static synchronized DownloadFiles getInstance(){
        if(DF == null){
            DF = new DownloadFiles();
        }
        return DF;
    }
    
    public void downloadFiles() throws IOException {
        driveService = getDriveService();
        Authorization.listFiles();
        ArrayList<String> ids = Authorization.getFilesIds();
        ArrayList<String> names = Authorization.getFilesList();
        ids.forEach(fileId ->{
            File out_dir = new File("/tmp/joanne/"+names.get(ids.indexOf(fileId)));
            try {
                fs = new FileOutputStream(out_dir);
                driveService.files().get(fileId)
                .executeMediaAndDownloadTo(fs);
            } catch (IOException ex) {
                Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public void stop(){
        try {
            if(fs != null){
                fs.flush();
                fs.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
