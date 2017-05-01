/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.googlesync;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import static gallery.googlesync.Authorization.getDriveService;
import java.io.IOException;
/**
 *
 * @author Obsidiam
 */
public class UploadFiles {
    private static Drive driveService = null;
    
    
    public void simpleUpload(java.io.File filePath,String name,String mime) throws IOException{
        driveService = getDriveService();
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType(mime);

        FileContent mediaContent = new FileContent(mime, filePath);
        File file = driveService.files().create(fileMetadata, mediaContent)
        .setFields("id")
        .execute();
        System.out.println("File ID: " + file.getId());
    }
}
