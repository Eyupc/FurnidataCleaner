package com.master;

import org.ini4j.Wini;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class Main {

    public static void main(String[] args) throws IOException {

        Wini ini = new Wini(new File("config.ini"));
        String FurniDataPath = ini.get("Configuration", "FurniData-Path");
        String FurniPath = ini.get("Configuration", "Furni-Path");

        File FurniFiles = new File(FurniPath);
        File FurniDataFile = new File(FurniDataPath);
        if (FurniDataFile.exists() && FurniFiles.exists()) {

            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(new FileReader(FurniDataPath));
                JSONObject jsonObject = (JSONObject) obj;
                JSONObject roomitemtypes = (JSONObject) jsonObject.get("roomitemtypes");
                JSONArray furnitypes = (JSONArray) roomitemtypes.get("furnitype");

                JSONObject wallitemtypes = (JSONObject) jsonObject.get("wallitemtypes");
                JSONArray furnitypes2 = (JSONArray) wallitemtypes.get("furnitype");



                String[] FurniArray = FurniFiles.list();
                // Get all nitro files
                for (int i = 0; i <= FurniArray.length - 1; i++) {
                    if (!FurniArray[i].contains(".nitro")) {
                        for (int x = i; x < FurniArray.length - 1; x++) {
                            FurniArray[x] = FurniArray[x + 1];
                        }
                        FurniArray = Arrays.copyOf(FurniArray, FurniArray.length - 1);
                    }
                }


                FurniArray = nameWithoutExtension(FurniArray); // Nitro files without extension -> test.nitro -> test
                List<String> FurniList = Arrays.asList(FurniArray.clone());
                JSONArray flooritems = new JSONArray();
                int tel = -1;
                for (Object x : furnitypes) { //Floor items
                    tel++;
                    JSONObject p = (JSONObject) x;
                    String classname = p.get("classname").toString();
                    if (classname.contains("*")) {
                        short index = (short) classname.indexOf("*");
                        classname = classname.substring(0, index);
                    }


                    if (FurniList.contains(classname)) {
                        flooritems.add(furnitypes.get(tel));
                    }else{

                        System.out.print("[FLOOIRTEMS]     |"+classname);
                        System.out.println("| doesn't exist in your furniture folder!");
                    }
                }

                int tel2 = -1;
                JSONArray wallitems = new JSONArray();
                for (Object x : furnitypes2) { //Wallitems
                    tel2++;
                    JSONObject p = (JSONObject) x;
                    String classname = p.get("classname").toString();
                    if (classname.contains("*")) {
                        short index = (short) classname.indexOf("*");
                        classname = classname.substring(0, index);
                    }


                    if (FurniList.contains(classname)) {
                        wallitems.add(furnitypes2.get(tel2));
                    }else{
                        System.out.print("[WALLITEMS]     |"+classname);
                        System.out.println("| doesn't exist in your furniture folder!");
                    }
                }




                JSONArray flooritemsChronologic= new JSONArray();
                for(Object y: flooritems){      // Sort in chronological order like in file(Flooritems)
                    String adurl= "adurl";
                    String offerid ="offerid";

                    JSONObject f = (JSONObject) y;

                    if(f.get("adurl") == null)
                        adurl = "adUrl";
                    if(f.get("offerid") == null)
                        offerid = "offerId";

                    try {
                        flooritemsChronologic.add(ChronologicalFloorItems(
                                (long) f.get("id"), (String) f.get("classname"), (long) f.get("revision"), (String) f.get("category"), (long) f.get("defaultdir"),
                                (long) f.get("xdim"), (long) f.get("ydim"), (JSONObject) f.get("partcolors"), (String) f.get("name"), (String) f.get("description"), (String) f.get(adurl), (long) f.get(offerid),
                                (boolean) f.get("buyout"), (long) f.get("rentofferid"), (boolean) f.get("rentbuyout"), (boolean) f.get("bc"),
                                (boolean) f.get("excludeddynamic"), (String) f.get("customparams"), (long) f.get("specialtype"), (boolean) f.get("canstandon"),
                                (boolean) f.get("cansiton"), (boolean) f.get("canlayon"), (String) f.get("furniline"), (String) f.get("environment"), (boolean) f.get("rare")));
                    }catch(NullPointerException e){
                        System.out.println("Error with your furnidata!");
                    }
                }

                JSONArray wallitemsChronologic= new JSONArray();
                for(Object y: wallitems){ //Sort in chronological order like in file(Wallitems)
                    String adurl= "adurl";
                    String offerid ="offerid";

                    JSONObject f = (JSONObject) y;

                    if(f.get("adurl") == null)
                        adurl = "adUrl";
                    if(f.get("offerid") == null)
                        offerid = "offerId";

                    try {
                        wallitemsChronologic.add(ChronologicalWallitems(
                                (long) f.get("id"), (String) f.get("classname"), (long) f.get("revision"), (String) f.get("category"), (String) f.get("name"), (String) f.get("description"), (String) f.get(adurl), (long) f.get(offerid),
                                (boolean) f.get("buyout"), (long) f.get("rentofferid"), (boolean) f.get("rentbuyout"), (boolean) f.get("bc"),
                                (boolean) f.get("excludeddynamic"), (String) f.get("customparams"), (long) f.get("specialtype"),(String) f.get("furniline"), (String) f.get("environment"), (boolean) f.get("rare")));
                    }catch(NullPointerException e){
                        System.out.println("Error with your furnidata!");
                    }
                }


                JSONObject JSONroomitemtypes = new JSONObject();
                JSONObject JSONfurnitype = new JSONObject();
                JSONObject JSONfurnitypeW = new JSONObject();

                JSONfurnitype.put("furnitype",flooritemsChronologic);
                JSONroomitemtypes.put("roomitemtypes",JSONfurnitype);
                JSONfurnitypeW.put("furnitype",wallitemsChronologic);
                JSONroomitemtypes.put("wallitemtypes",JSONfurnitypeW);


                Path path = Paths.get("FurnitureData-Cleaned.json");
                Files.deleteIfExists(path);
                File myObj = new File("FurnitureData-Cleaned.json");
                FileWriter file = new FileWriter(myObj);
                file.write(JSONroomitemtypes.toJSONString());
                file.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("[SUCCESS]  Cleaned furnidata successfully!");
        } else {
            if(!FurniDataFile.exists() && !FurniFiles.exists()){
                System.out.println("[ERROR]  Furnidata & (Nitro)Furni Files path is wrong!");
            }else if(!FurniDataFile.exists()){
                System.out.println("[ERROR]  Furnidata path is wrong!");
            }else {
                System.out.println("[ERROR]  (Nitro)Furni Files path is wrong!");
            }
        }
    }


    public static String[] nameWithoutExtension(String[] files) {
        String[] FileName = new String[files.length];

        for (int i = 0; i <= files.length - 1; i++) {
            if (files[i].contains(".nitro")) {
                int index = files[i].lastIndexOf('.');
                FileName[i] = files[i].substring(0, index);
            }
        }
        return FileName;
    }

    public static LinkedHashMap<String, Object> ChronologicalFloorItems(
    long id, String classname, long revision, String category, long defaultdir,
    long xdim, long ydim, JSONObject partcolors, String name, String description, String adurl, long offerId, boolean buyout,
    long rentofferid, boolean rentbuyout, boolean bc, boolean excludeddynamic, String customparams, long specialtype, boolean canstandon,
    boolean cansiton, boolean canlayon, String furniline, String environment, boolean rare) {

        LinkedHashMap<String,Object> jsonObject= new LinkedHashMap();

     jsonObject.put("id", id);
     jsonObject.put("classname",classname);
     jsonObject.put("revision",revision);
     jsonObject.put("category",category);
     jsonObject.put("defaultdir",defaultdir);
     jsonObject.put("xdim",xdim);
     jsonObject.put("ydim",ydim);
     jsonObject.put("partcolors",partcolors);
     jsonObject.put("name",name);
     jsonObject.put("description",description);
     jsonObject.put("adurl",adurl);
     jsonObject.put("offerid",offerId);
     jsonObject.put("buyout",buyout);
     jsonObject.put("rentofferid",rentofferid);
     jsonObject.put("rentbuyout",rentbuyout);
     jsonObject.put("bc",bc);
     jsonObject.put("excludeddynamic",excludeddynamic);
     jsonObject.put("customparams",customparams);
     jsonObject.put("specialtype",specialtype);
     jsonObject.put("canstandon",canstandon);
     jsonObject.put("cansiton",cansiton);
     jsonObject.put("canlayon",canlayon);
     jsonObject.put("furniline",furniline);
     jsonObject.put("environment",environment);
     jsonObject.put("rare",rare);
     return jsonObject;

    }
    public static LinkedHashMap<String, Object> ChronologicalWallitems(
     long id, String classname,long revision,String category,String name,String description,
     String adurl, long offerId, boolean buyout, long rentofferid, boolean rentbuyout, boolean bc,
     boolean excludeddynamic, String customparams, long specialtype, String furniline,String environment,
     boolean rare){

        LinkedHashMap<String,Object> jsonObject= new LinkedHashMap();

        jsonObject.put("id", id);
        jsonObject.put("classname",classname);
        jsonObject.put("revision",revision);
        jsonObject.put("category",category);
        jsonObject.put("name",name);
        jsonObject.put("description",description);
        jsonObject.put("adurl",adurl);
        jsonObject.put("offerid",offerId);
        jsonObject.put("buyout",buyout);
        jsonObject.put("rentofferid",rentofferid);
        jsonObject.put("rentbuyout",rentbuyout);
        jsonObject.put("bc",bc);
        jsonObject.put("excludeddynamic",excludeddynamic);
        jsonObject.put("customparams",customparams);
        jsonObject.put("specialtype",specialtype);
        jsonObject.put("furniline",furniline);
        jsonObject.put("environment",environment);
        jsonObject.put("rare",rare);
        return jsonObject;
    }
}
