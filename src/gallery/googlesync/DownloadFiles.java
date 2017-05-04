/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.googlesync;

import com.google.api.services.drive.Drive;
import gallery.enums.Environment;
import static gallery.googlesync.Authorization.getDriveService;
import gallery.systemproperties.EnvVars;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    
    public static void downloadFiles() throws IOException {
        driveService = getDriveService();
        Authorization.listFiles();
        ArrayList<String> ids = Authorization.getFilesIds();
        
        ids.forEach(fileId ->{
        File out_dir = new File("/tmp/joanne/"+fileId);
            try {
                FileOutputStream fs = new FileOutputStream(out_dir);
                driveService.files().get(fileId)
                .executeMediaAndDownloadTo(fs);
            } catch (IOException ex) {
                Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
