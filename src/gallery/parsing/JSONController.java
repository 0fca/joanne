/*
 * Copyright (C) 2017 lukas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gallery.parsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gallery.enums.Environment;
import gallery.systemproperties.EnvVars;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Obsidiam
 */
public class JSONController {
    private static volatile JSONController JSON;
    private static EnvVars ENV = new EnvVars();
    static BufferedReader br = null;
    
    public static synchronized JSONController getInstance(){
        if(JSON == null){
            JSON = new JSONController();
        }
        return JSON;
    } 
    
    public void writeJson(ArrayList<SyncDataWrapper> sync,int fileCount) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(ENV.getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"sync_data.json"));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(fileCount,writer);
        gson.toJson(sync, writer);
        writer.close();
    }
    
    public SyncDataWrapper[] readArray() throws FileNotFoundException{
        br = new BufferedReader(new FileReader(ENV.getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"sync_data.json"));
        Gson gson = new Gson(); 
        SyncDataWrapper[] wrapper = gson.fromJson(br, SyncDataWrapper[].class);
        return wrapper;
    }
    
    public String readString(String field) throws FileNotFoundException{
        br = new BufferedReader(new FileReader(ENV.getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"sync_data.json"));
        JsonArray entries = (JsonArray) new JsonParser().parse(br);
        return ((JsonObject)entries.get(0)).get(field).toString();
    }
    
    public int readInt32(String field) throws FileNotFoundException{
        br = new BufferedReader(new FileReader(ENV.getEnvironmentVariable(Environment.USER_HOME)+File.separator+"joanne"+File.separator+"sync_data.json"));
        JsonArray entries = (JsonArray) new JsonParser().parse(br);
        return ((JsonObject)entries.get(0)).get(field).getAsInt();
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        //writeJson();
    }
}
