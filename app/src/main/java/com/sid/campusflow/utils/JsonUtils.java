package com.sid.campusflow.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.sid.campusflow.models.Room;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final String TAG = "JsonUtils";
    
    /**
     * Parse room data from raw JSON resource
     */
    public static List<Room> parseRoomsFromRawResource(Context context, int resourceId) {
        List<Room> rooms = new ArrayList<>();
        
        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            
            Gson gson = new Gson();
            Type roomListType = new TypeToken<ArrayList<Room>>(){}.getType();
            rooms = gson.fromJson(jsonString.toString(), roomListType);
            reader.close();
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON resource: " + e.getMessage());
        }
        
        return rooms;
    }
    
    /**
     * Parse room data from a JSON file URI
     */
    public static List<Room> parseRoomsFromUri(Context context, Uri uri) {
        List<Room> rooms = new ArrayList<>();
        
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder jsonString = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                
                String jsonContent = jsonString.toString();
                Log.d(TAG, "JSON Content Length: " + jsonContent.length());
                
                // If JSON content is empty, log and return
                if (jsonContent.isEmpty()) {
                    Log.e(TAG, "Empty JSON file");
                    return rooms;
                }
                
                try {
                    Gson gson = new Gson();
                    Type roomListType = new TypeToken<ArrayList<Room>>(){}.getType();
                    rooms = gson.fromJson(jsonContent, roomListType);
                    Log.d(TAG, "Parsed " + rooms.size() + " rooms from JSON");
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    // If direct parsing fails, try parsing as a single room
                    try {
                        Gson gson = new Gson();
                        Room singleRoom = gson.fromJson(jsonContent, Room.class);
                        if (singleRoom != null) {
                            rooms.add(singleRoom);
                            Log.d(TAG, "Parsed single room from JSON");
                        }
                    } catch (Exception singleEx) {
                        Log.e(TAG, "Error parsing as single room: " + singleEx.getMessage());
                    }
                }
                reader.close();
            } else {
                Log.e(TAG, "Could not open input stream from URI");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON file: " + e.getMessage());
        }
        
        return rooms;
    }

    /**
     * Parse room data from a CSV asset file
     */
    public static List<Room> parseRoomsFromAsset(Context context, String assetFileName) {
        List<Room> rooms = new ArrayList<>();
        
        try {
            InputStream is = context.getAssets().open(assetFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            com.opencsv.CSVReader csvReader = new com.opencsv.CSVReader(reader);
            
            // Read header (first line)
            String[] header = null;
            try {
                header = csvReader.readNext();
            } catch (CsvValidationException e) {
                Log.e(TAG, "CSV validation error: " + e.getMessage());
                return rooms;
            }
            
            if (header == null) {
                Log.e(TAG, "CSV file is empty or invalid");
                return rooms;
            }
            
            // Map column indices
            int nameIndex = -1, typeIndex = -1, locationIndex = -1, buildingIndex = -1, 
                floorIndex = -1, descIndex = -1, latIndex = -1, longIndex = -1;
            
            for (int i = 0; i < header.length; i++) {
                String col = header[i].toLowerCase().trim();
                if (col.contains("name") && !col.contains("building")) nameIndex = i;
                else if (col.contains("type")) typeIndex = i;
                else if (col.contains("location")) locationIndex = i;
                else if (col.contains("building")) buildingIndex = i;
                else if (col.contains("floor")) floorIndex = i;
                else if (col.contains("desc")) descIndex = i;
                else if (col.contains("lat")) latIndex = i;
                else if (col.contains("long")) longIndex = i;
            }
            
            // Read rows
            String[] line;
            try {
                while ((line = csvReader.readNext()) != null) {
                    try {
                        Room room = new Room();
                        
                        // Set room properties based on CSV columns
                        if (nameIndex >= 0 && nameIndex < line.length) 
                            room.setName(line[nameIndex]);
                        
                        if (typeIndex >= 0 && typeIndex < line.length) 
                            room.setType(line[typeIndex]);
                        
                        if (locationIndex >= 0 && locationIndex < line.length) 
                            room.setLocation(line[locationIndex]);
                        
                        if (buildingIndex >= 0 && buildingIndex < line.length) 
                            room.setBuildingName(line[buildingIndex]);
                        
                        if (floorIndex >= 0 && floorIndex < line.length) 
                            room.setFloor(line[floorIndex]);
                        
                        if (descIndex >= 0 && descIndex < line.length) 
                            room.setDescription(line[descIndex]);
                        
                        // Parse coordinates if present
                        try {
                            if (latIndex >= 0 && latIndex < line.length && !line[latIndex].isEmpty()) 
                                room.setLatitude(Double.parseDouble(line[latIndex]));
                            
                            if (longIndex >= 0 && longIndex < line.length && !line[longIndex].isEmpty()) 
                                room.setLongitude(Double.parseDouble(line[longIndex]));
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
                        }
                        
                        // Only add room if it has at least a name and type
                        if (room.getName() != null && !room.getName().isEmpty() &&
                            room.getType() != null && !room.getType().isEmpty()) {
                            rooms.add(room);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing CSV row: " + e.getMessage());
                    }
                }
            } catch (CsvValidationException e) {
                Log.e(TAG, "CSV validation error while reading rows: " + e.getMessage());
            }
            
            csvReader.close();
            reader.close();
            is.close();
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV asset: " + e.getMessage());
        }
        
        return rooms;
    }
} 