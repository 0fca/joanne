/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.threading;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import static gallery.googlesync.Authorization.getDriveService;
import gallery.googlesync.DownloadFiles;
import gallery.googlesync.UploadFiles;
import gallery.xml.XMLManager;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Obsidiam
 */
public class FileSync extends Thread implements Runnable {
    private Thread SYNC = null;
    private UploadFiles UPLOAD = new UploadFiles();
    private DownloadFiles DOWNLOAD = new DownloadFiles();
    
    
    public static List<File> getFilesList() throws IOException{
        // Build a new authorized API client service.
         // Build a new authorized API client service.
        Drive service = getDriveService();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
             .setPageSize(10)
             .setFields("nextPageToken, files(id, name)")
             .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
            return null;
        } else {
            return files;
        }
    }
    
    public void syncFiles(){
        this.start();
    }
    
    @Override
    public void start(){
        if(SYNC == null){
            SYNC = new Thread(this);
            SYNC.start();
        }
    }
    
    @Override
    public void run(){
        XMLManager xml = new XMLManager();
        
    }
}
