/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.googlesync;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import gallery.enums.Environment;
import static gallery.googlesync.Authorization.getDriveService;
import gallery.systemproperties.EnvVars;
import gallery.parsing.JSONController;
import gallery.parsing.SyncDataWrapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        ArrayList<SyncDataWrapper> records = new ArrayList<>();
        Date d = new Date();
        long l_date = d.getTime();
        SyncDataWrapper wrapper = new SyncDataWrapper();
        wrapper.setFileModifDate(l_date);
        ids.forEach(fileId ->{
            wrapper.setFileId(Integer.parseInt(fileId));
            File out_dir = new File(ENV.getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"google_drive"+File.separator+names.get(ids.indexOf(fileId)));
            try {
                fs = new FileOutputStream(out_dir);
                driveService.files().get(fileId)
                .executeMediaAndDownloadTo(fs);
                wrapper.setFileName(out_dir.getName());
            } catch (IOException ex) {
                Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        records.add(wrapper);
        JSONController.getInstance().writeJson(records,ids.size());
        
    }
    
    public void stop(){
        try {
            if(fs != null){
                fs.flush();
                fs.close();
                System.out.println("FS closed.");
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
