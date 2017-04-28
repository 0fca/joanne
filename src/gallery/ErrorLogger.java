/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author lukas
 * @ver 1.0
 */
public class ErrorLogger {
    private HashMap<Exception,Integer> errors = new HashMap<>();
    private HashMap<Integer,String> desc = new HashMap<>();
    public String NAME = "Error 00: This code doesn't exist.";
    
    public void prepareErrorList(){
        
        errors.put(new Exception(), Integer.MIN_VALUE);
        errors.put(new NullPointerException(), 10);
        errors.put(new NoSuchFileException("The file could not be found. Sorry for the inconvience"), 20);
        errors.put(new IllegalStateException(), 30);
        errors.put(new FileNotFoundException(), 200);
        errors.put(new AccessDeniedException("The account "+System.getProperty("user.name")+"\nhas not the privileges to do this action."), 40);
        errors.put(new ArrayIndexOutOfBoundsException(),  50);
        errors.put(new UnsupportedOperationException(), 60);
        errors.put(new IOException(), 70);
        errors.put(new MalformedURLException(), 80);
        errors.put(new IllegalArgumentException(), 90);
        
        desc.put(10,"NullPointerException - w którymś momencie w kodzie została napotkana wartość null.");
        desc.put(30,"The value or component has tried to gain illegal state.");
        desc.put(200, "The given file hasn't been found, asure that you gave\nan absolute path and the file exists!");
        desc.put(50, "The index is out of range; it means that the method tried to access the index which is\n"
                + "not in that array.");
        desc.put(60, "Requested operation is not supported at the moment.");
        desc.put(70, "The problem was occured while operating on Input/Output streams.");
        desc.put(90, "The argument given was illegal.");
        desc.put(80, "Given URL is malformed, check\nthat you have write a proper URL address");
    }
    
    public String getErrorInfo(Exception e){
        
        String out_code = "00";
        Set<Exception> s = errors.keySet();
        for(Exception ex : s){
            System.out.print(e);
            if(ex.toString().equals(e.toString())){
               out_code = String.valueOf(errors.get(ex));
            }
        }
        return out_code;
    }
    
    public void setErrorName(final int code){
        errors.forEach((x,y) ->{
            if(code == y){
               NAME = x.toString(); 
            }
        });
    }
    
    public String getExceptionDescriptor(int code){
        String out = desc.get(code);
        return out;
    }
    
    public String getErrorMessage(Exception e){
        String message = e.getMessage();
        return message;
    }
    
    public HashMap<Exception,Integer> returnMap(){
        return errors;
    }
}
