/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery.systemproperties;

import gallery.enums.Environment;

/**
 *
 * @author Obsidiam
 */
public class EnvVars {
    public String getEnvironmentVariable(Environment e){
        String var = null;
        switch(e){
            case OS_NAME:
                var = System.getProperty("os.name");
                break;
            case XML_PATH:
                var = System.getProperty("xml.path");
                break;
            case USER_NAME:
                var = System.getProperty("user.name");
                break;
            case USER_HOME:
                var = System.getProperty("user.home");
                break;
            case USER_DIR:
                var = System.getProperty("user.dir");
                break;
            case PHOTO_STORE:
                break;
        }
        return var;
    }
}
