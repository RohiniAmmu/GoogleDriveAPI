package com.drive.server;

import com.drive.error.ERROR_CODE;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.simple.parser.JSONParser;


public class DriveStaticHelper {

    public final static String GET_FOLDER_API ="https://staging.cloud-elements.com/elements/api-v2/folders/contents";
    public final static String FILE_API ="https://staging.cloud-elements.com/elements/api-v2/files";
    public final static String GET_FILE_API = "https://staging.cloud-elements.com/elements/api-v2/files/";
    public final static String SEARCH_API = "https://staging.cloud-elements.com/elements/api-v2/search";
    public final static String DOWNLOAD_API ="/links";

    public final static String UPLOAD_DIRECTORY = "/Volumes/Official/Rohini/Cloud/tomcat/webapps/ROOT/";
    public final static String AUTHORIZATION_VALUE = "User U9hRkslEsPPJRZ5EdwK7KjTwwewlamOq8P51EW21L+w=, Organization 56fa28f9ded3940c8879a8567a2aced0, Element I99f4eqp6SijiVZ6VqGtlICEbCZje4PX78lRMO/Q+sU=";
    public final static String PROVIDER_LINK ="providerLink";
    public final static String ID ="id";
    public final static String FILE_ID ="fileId";
    public final static String DIRECTORY ="directory";
    public final static String LOCATION ="location";
    public final static String NAME ="name";
    public final static String MESSAGE ="Message";
    public final static String UPLOAD_SUCCESS_MESSAGE ="Uploaded successfully";
    public final static String FOLDER_NAME ="folder_name";
    public final static String PARENT_FOLDER_ID ="parentFolderId";
    public final static String DOWNLOAD_LINK ="download_link";
    public final static String EXCEPTION ="Exception";
    public final static String STATUS_CODE ="Status_Code";
    public final static String ERROR_MESSAGE ="Error_Message";
    public final static int BUFFER_LENGTH =8192;

    public final static String PATH_PARAMTER ="?path=/";
    public final static String TEXT_PARAMTER ="?text=";

    public final static String FILE_PARAMTER ="file";
    public final static String FOLDER_NAME_PARAMTER ="folderName";
    public final static String FILE_NAME_PARAMTER ="fileName";

    public final static String ACCEPT_HEADER ="Accept";
    public final static String AUTHORIZATION_HEADER ="Authorization";
    public final static String CONTENT_TYPE_HEADER ="Content-Type";
    public final static String MULTI_FORM_DATA="multipart/form-data;";
    public final static String APPLICATION_JSON="application/json";
    
    public final static String RESPONSE_CONTENT_TYPE ="text/html";
    public final static String RESPONSE_CODE ="responseCode";
    public final static String RESPONSE_CONTENT ="responseContent";
    public final static String ERROR_CONTENT ="errorContent";

    public static JSONObject addFileToDrive(InputStream inputStream,String fileName){
        JSONObject jsonObject = new JSONObject();
        try{
            fileName = fileName.replaceAll(" ","_");
            HttpClient hc = new HttpClient(FILE_API+PATH_PARAMTER+fileName);
            System.out.println(FILE_API+PATH_PARAMTER+fileName);
            hc.setHeader(AUTHORIZATION_HEADER,AUTHORIZATION_VALUE );
            hc.setHeader(ACCEPT_HEADER, APPLICATION_JSON);
            hc.setHeader(CONTENT_TYPE_HEADER, MULTI_FORM_DATA);
                        
            hc.doMultiPart();
            hc.setParameter(FILE_PARAMTER,fileName,null,inputStream);
            try{
                InputStream ins = hc.post();
                int responseCode = hc.getResponseCode();

                jsonObject.put(RESPONSE_CODE,responseCode);
                
                if (responseCode == 200) {
                    jsonObject.put(MESSAGE,UPLOAD_SUCCESS_MESSAGE);
                }
            }catch (Exception e) {
                jsonObject.put(RESPONSE_CODE, hc.getResponseCode());
                jsonObject.put(ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_ALREADY_PRESENT.getErrorMessage());
            }
        } catch (Exception e) {
            System.out.println("Exception occurs in addFileToDrive method >>>>>>>>>>>>>" + e);
            return null;
        }
        return jsonObject;
    }

    public static JSONArray getFolderDetails(String folderName){
        try{
            JSONArray fileArray = new JSONArray();
            JSONObject outputObject = new JSONObject();
            if(folderName == null || folderName.equals("")){
                outputObject.put(ERROR_MESSAGE,ERROR_CODE.ERRORS.FOLDER_NAME_EMPTY.getErrorMessage());
                outputObject.put(STATUS_CODE,ERROR_CODE.ERRORS.FOLDER_NAME_EMPTY.getStatusCode());
                fileArray.put(outputObject);
                return fileArray;
            }
            try{
                HttpClient hc = new HttpClient(GET_FOLDER_API+PATH_PARAMTER+folderName);

                hc.setHeader(AUTHORIZATION_HEADER,AUTHORIZATION_VALUE );
                hc.setHeader(ACCEPT_HEADER, APPLICATION_JSON);
                hc.doGet();
                if (hc.getResponseCode() == 200) {
                    StringBuilder httpResponse = hc.getSuccessResponse();
                    JSONArray responseArray = new JSONArray(httpResponse.toString());
                    for(int i=0; i < responseArray.length(); i++){
                       JSONObject response = (JSONObject)responseArray.get(i);
                       JSONObject folderDetails = new JSONObject();
                       folderDetails.put(FOLDER_NAME,folderName);
                       folderDetails.put(FILE_ID,response.getString(ID));
                       folderDetails.put(NAME,response.getString(NAME));
                       folderDetails.put(DIRECTORY,response.getBoolean(DIRECTORY));
                       folderDetails.put(PARENT_FOLDER_ID,response.getString(PARENT_FOLDER_ID));
                       fileArray.put(folderDetails);
                    }
                } else{
                    String errorContent = hc.getErrorMessage();
                    JSONParser parser = new JSONParser();
                    outputObject = (JSONObject) parser.parse(errorContent);
                    fileArray.put(outputObject);
                }
            } catch (FileNotFoundException ex){
                outputObject.put(ERROR_MESSAGE,ERROR_CODE.ERRORS.FOLDER_NOT_FOUND.getErrorMessage());
                outputObject.put(STATUS_CODE,ERROR_CODE.ERRORS.FOLDER_NOT_FOUND.getStatusCode());
                fileArray.put(outputObject);
            } catch (Exception ex){
                outputObject.put(ERROR_MESSAGE,ERROR_CODE.ERRORS.GET_FOLDER_DETAILS.getErrorMessage());
                outputObject.put(STATUS_CODE,ERROR_CODE.ERRORS.GET_FOLDER_DETAILS.getStatusCode());
                fileArray.put(outputObject);
            }
            return fileArray;
        } catch (Exception ex) {
            System.out.println("Exception occurs in getFolderDetails method >>>>>>>>>>>>>" + ex);
            return null;
        }
    }

    public static InputStream getFileContent(String fileId){
        try{
            HttpClient hc = new HttpClient(GET_FILE_API+fileId);

            hc.setHeader(AUTHORIZATION_HEADER,AUTHORIZATION_VALUE );
            hc.setHeader(ACCEPT_HEADER, APPLICATION_JSON);
            hc.doGet();
            if (hc.getResponseCode() == 200) {
                return hc.getInputStream();
            }
            return null;
        } catch (Exception ex){
            System.out.println("Exception occurs in getFileContent method >>>>>>>>>>>>>" + ex);
            return null;
        }
    }

    public static List<JSONObject> searchTheFromName(String fileOrFolderName, boolean onlyFile){
        try{
            HttpClient hc = new HttpClient(SEARCH_API+TEXT_PARAMTER+fileOrFolderName);

            hc.setHeader(AUTHORIZATION_HEADER,AUTHORIZATION_VALUE );
            hc.setHeader(ACCEPT_HEADER, APPLICATION_JSON);
            hc.doGet();

            if (hc.getResponseCode() == 200) {
                StringBuilder httpResponse = hc.getSuccessResponse();
                JSONArray responseArray = new JSONArray(httpResponse.toString());
                List<JSONObject> fileList = new ArrayList<JSONObject>();
                for(int i=0; i < responseArray.length(); i++){
                    JSONObject response = (JSONObject)responseArray.get(i);
                    String resourceName = response.getString(NAME);
                    boolean compareName = (!onlyFile || resourceName.equals(fileOrFolderName));
                    if(compareName){
                        boolean isDirectory = response.getBoolean(DIRECTORY);
                        boolean canAdd = !(onlyFile && isDirectory);
                        if(canAdd){
                            JSONObject responseObject = new JSONObject();
                            String fileId = response.getString(ID);
                            responseObject.put(FILE_ID,fileId);
                            responseObject.put(NAME,resourceName);
                            responseObject.put(DIRECTORY,isDirectory);
                            fileList.add(responseObject);
                        }
                    }
                }
                return fileList;
            }
            return null;
        } catch (Exception ex){
            System.out.println("Exception occurs in searchTheFromName method >>>>>>>>>>>>>" + ex);
            return null;
        }
    }

    public static JSONArray getLinksFromFileIds(List<JSONObject> fileDetails){
        try{
            JSONArray links = new JSONArray();
            if(fileDetails.isEmpty()){
                JSONObject responseObject = new JSONObject();
                responseObject.put(ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_OR_FOLDER_NOT_FOUND.getErrorMessage());
                responseObject.put(STATUS_CODE,ERROR_CODE.ERRORS.FILE_OR_FOLDER_NOT_FOUND.getStatusCode());
                links.put(responseObject);
            } else{
                for(JSONObject fileDetail : fileDetails){
                    String fileId = fileDetail.getString(FILE_ID);
                    String link = getLinkFromFileId(fileId);
                    JSONObject responseObject = new JSONObject();
                    responseObject.put(FILE_ID,fileId);
                    responseObject.put(DOWNLOAD_LINK,link);
                    responseObject.put(NAME,fileDetail.getString(NAME));
                    links.put(responseObject);
                }
            }
            return links;
        } catch (Exception ex){
            System.out.println("Exception occurs in getLinksFromFileIds method >>>>>>>>>>>>>" + ex);
            return null;
        }
    }

    public static String getLinkFromFileId(String fileId){
        try{
            HttpClient hc = new HttpClient(FILE_API+"/"+fileId+DOWNLOAD_API);

            hc.setHeader(AUTHORIZATION_HEADER,AUTHORIZATION_VALUE );
            hc.setHeader(ACCEPT_HEADER, APPLICATION_JSON);
            hc.doGet();
            String link ="";
            if (hc.getResponseCode() == 200) {
                StringBuilder httpResponse = hc.getSuccessResponse();
                JSONObject response = new JSONObject(httpResponse.toString());
                link = response.getString(PROVIDER_LINK);
            }

            return link;
        } catch (Exception ex){
            System.out.println("Exception occurs in getLinkFromFileId method >>>>>>>>>>>>>" + ex);
            return null;
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte buffer[] = new byte[BUFFER_LENGTH];
        int len = BUFFER_LENGTH;
        while (true) {
            len = in.read(buffer, 0, BUFFER_LENGTH);
            if (len < 0) {
                break;
            }
            out.write(buffer, 0, len);
        }
    }
}
