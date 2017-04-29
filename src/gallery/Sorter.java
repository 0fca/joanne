/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gallery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.UserPrincipal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

/**
 *
 * @author Obsidiam
 */
public class Sorter {
    private  ArrayList<String> toSort;
    private  ArrayList sorted; 
    
    public Sorter(ArrayList<String> toSort, ArrayList sorted){
        this.toSort = toSort;
        this.sorted = sorted;
    }
    
    public void chooseSortAlgorithm(String sorting_option,String param){
        switch(sorting_option){
            case "ext":
                sortByKeyword(param);
                break;
            case "date":
                sortByDate(param);
                break;
            case "find_date":
                findByDate(param);
                break;
            case "owner":
                sortByOwner(param);
                break;
        }
    }
    
    
    private void sortByKeyword(String param){
        toSort.forEach(event ->{
            if(new File(event).getName().contains(param)){
               sorted.add(event);
            }
        });      
    }
    
    private void sortByOwner(String param){
        toSort.forEach(event ->{
            try {
                UserPrincipal user = Files.getOwner(new File(event).toPath(), LinkOption.NOFOLLOW_LINKS);
                
                if(user.toString().equals(param)){
                   sorted.add(event);
                }
            } catch (IOException ex) {

            }
        });
    }
    
    private void findByDate(String param){
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy");
        LastModifiedFileComparator c = new LastModifiedFileComparator();
        
        Date file = new Date();
        String d = df.format(file);
        sorted.clear();

        List<File> l1 = new ArrayList<>();
        toSort.stream().filter((image) -> ( df2.format(new File(image).lastModified()).equals(d))).forEach((image) -> {
            l1.add(new File(image));
        });

        List<File> f = c.sort(l1);
        f.forEach(x ->{
            sorted.add(x.getAbsolutePath());
        });
    }

    private static void insertionsort(ArrayList<Long> l) {
        int i,j;
        long v;

        for (i=1;i<l.size();i++) {
            j=i;
            v=l.get(i);
            while ((j>0) && (l.get(j-1)>v)) {
                l.set(j, l.get(j-1));
                j--;
            }

            l.set(j, v);
        }
    }

    private void sortByDate(String path) {
        ArrayList<Long> l = new ArrayList<>();
        
        File f = new File(path);
        File[] array = f.listFiles();
        
        for(File str : array){
            l.add(str.lastModified());
        }
        
        insertionsort(l);
        
        for(File file : array){
            long date = file.lastModified();
            l.forEach(x ->{
                if(date == x){
                    sorted.add(file.getAbsolutePath());
                }
            });
        }
    }
    
    public ArrayList getSortedList(){
        return toSort;
    }
}
