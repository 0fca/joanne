/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

/**
 *
 * @author lukas
 * @ver 1.0
 */
public class AppVersion {
    private int SELECTOR = 0;
    private int MAJOR = 0;
    private int MINOR = 7;
    private int BUILD = 1;
    
    public AppVersion(int selector){
        this.SELECTOR = selector;
    }
    public AppVersion(){
        
    }
    
    private String getFullVersion(){
        String ret = String.valueOf(MAJOR+"."+MINOR+"."+BUILD);
        return ret;
    }
    
    public String selectAndGetVersion(int selector){
        String ret = "";
        this.SELECTOR = selector;
        switch(SELECTOR){
            case 0:
                ret = getFullVersion();
                break;
            case 1:
                ret = getMajorVersion();
                break;
            case 2:
                ret = getMinorVersion();
                break;
            case 3:
                ret = getBuild();
                break;
        }
        return ret;
    }
    

    private String getMajorVersion() {
        String ret = String.valueOf(MAJOR);
        return ret;
    }

    private String getMinorVersion() {
        String ret = String.valueOf(MINOR);
        return ret;
    }

    private String getBuild() {
        String ret = String.valueOf(BUILD);
        return ret;
    }
    @Override
    public String toString(){
        String ret = "";
        switch(SELECTOR){
            case 0:
                ret = getFullVersion();
                break;
            case 1:
                ret = getMajorVersion();
                break;
            case 2:
                ret = getMinorVersion();
                break;
            case 3:
                ret = getBuild();
                break;
        }
        
        return ret;
    }
}
