package com.drive.client;

import com.drive.error.ERROR_CODE;
import com.drive.server.DriveStaticHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.List;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

public class DriveAPI extends HttpServlet {

	public final static String GET_FOLDER ="/drive/folder";
	public final static String GET_FILE ="/drive/file";
	public final static String DOWNLOAD_LINK ="/drive/download/link";
	public final static String DOWNLOAD_FILE ="/drive/download/file";
	public final static String UPLOAD_FILE ="/drive/upload";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		try {
			String uri = request.getRequestURI();
			if(uri.equals(GET_FOLDER)){
				try{
					String folderName = request.getParameter(DriveStaticHelper.FOLDER_NAME_PARAMTER);
					PrintWriter writer = response.getWriter();   
					if(folderName==null || folderName.equals("")){
						JSONObject responseObject = new JSONObject();
						responseObject.put(DriveStaticHelper.ERROR_MESSAGE,ERROR_CODE.ERRORS.FOLDER_NAME_EMPTY.getErrorMessage());
                		responseObject.put(DriveStaticHelper.STATUS_CODE,ERROR_CODE.ERRORS.FOLDER_NAME_EMPTY.getStatusCode());
                		writer.print(responseObject);
                		return;
					} 
					JSONArray responseJsonArray = DriveStaticHelper.getFolderDetails(folderName);
					writer.print(responseJsonArray);
				}catch(Exception ex){
		            System.out.println("Exception occurs in getting folder >>>>>>>>>>>>>" + ex);
				}
			} else if(uri.equals(GET_FILE)){
				try{
					String fileId = request.getParameter(DriveStaticHelper.FILE_ID);
					if(fileId==null || fileId.equals("")){
						JSONObject responseObject = new JSONObject();
						responseObject.put(DriveStaticHelper.ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_ID_EMPTY.getErrorMessage());
                		responseObject.put(DriveStaticHelper.STATUS_CODE,ERROR_CODE.ERRORS.FILE_ID_EMPTY.getStatusCode());
						PrintWriter writer = response.getWriter();   
                		writer.print(responseObject);
                		return;
					} 
					InputStream outputFile = DriveStaticHelper.getFileContent(fileId);
					if(outputFile==null){
						JSONObject responseObject = new JSONObject();
						responseObject.put(DriveStaticHelper.ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_ID_NOT_FOUND.getErrorMessage());
                		responseObject.put(DriveStaticHelper.STATUS_CODE,ERROR_CODE.ERRORS.FILE_ID_NOT_FOUND.getStatusCode());
						PrintWriter writer = response.getWriter();   
                		writer.print(responseObject);
                		return;
					}
					ServletOutputStream outputStream = null;
					try{
						outputStream = response.getOutputStream();
						DriveStaticHelper.copy(outputFile,outputStream);
					}
					catch(Exception ex){
			            System.out.println("Exception occurs in getting the content of a file by ID >>>>>>>>>>>>>" + ex);
					}finally{
						outputFile.close();
						outputStream.close();
					}
				}catch(Exception ex){
		            System.out.println("Exception occurs in getting file >>>>>>>>>>>>>" + ex);
				}
			} else if(uri.equals(DOWNLOAD_LINK)){
				try{
					PrintWriter writer = response.getWriter();   
					String fileOrFolderName = request.getParameter(DriveStaticHelper.NAME);
					if(fileOrFolderName==null || fileOrFolderName.equals("")){
						JSONObject responseObject = new JSONObject();
						responseObject.put(DriveStaticHelper.ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_OR_FOLDER_NAME_EMPTY.getErrorMessage());
                		responseObject.put(DriveStaticHelper.STATUS_CODE,ERROR_CODE.ERRORS.FILE_OR_FOLDER_NAME_EMPTY.getStatusCode());
                		writer.print(responseObject);
                		return;
					}
					List<JSONObject> resourceDetails = DriveStaticHelper.searchTheFromName(fileOrFolderName,false);
					JSONArray linkArray = DriveStaticHelper.getLinksFromFileIds(resourceDetails);
					
					writer.print(linkArray);
				} catch (Exception ex) {
		            System.out.println("Exception occurs in getting the link for a file >>>>>>>>>>>>>" + ex);
				}
			} else if(uri.equals(DOWNLOAD_FILE)){
				try{
					String fileName = request.getParameter(DriveStaticHelper.FILE_NAME_PARAMTER);
					if(fileName==null || fileName.equals("")){
						JSONObject responseObject = new JSONObject();
						responseObject.put(DriveStaticHelper.ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_NAME_EMPTY.getErrorMessage());
                		responseObject.put(DriveStaticHelper.STATUS_CODE,ERROR_CODE.ERRORS.FILE_NAME_EMPTY.getStatusCode());
						PrintWriter writer = response.getWriter();   
                		writer.print(responseObject);
                		return;
					}
					List<JSONObject> resourceDetails = DriveStaticHelper.searchTheFromName(fileName,true);
					if(resourceDetails.isEmpty()){
						JSONObject responseObject = new JSONObject();
						responseObject.put(DriveStaticHelper.ERROR_MESSAGE,ERROR_CODE.ERRORS.FILE_OR_FOLDER_NOT_FOUND.getErrorMessage());
                		responseObject.put(DriveStaticHelper.STATUS_CODE,ERROR_CODE.ERRORS.FILE_OR_FOLDER_NOT_FOUND.getStatusCode());
						PrintWriter writer = response.getWriter();   
                		writer.print(responseObject);
                		return;
					}
					InputStream outputFile = null;
					ServletOutputStream outputStream = null;
					try{
						outputStream = response.getOutputStream();
						JSONObject fileDetails = resourceDetails.get(0);
						outputFile = DriveStaticHelper.getFileContent(fileDetails.getString(DriveStaticHelper.FILE_ID));
						if(outputFile!=null){
							DriveStaticHelper.copy(outputFile,outputStream);
						}
					}
					catch(Exception ex){
			            System.out.println("Exception occurs in getting the file content for download >>>>>>>>>>>>>" + ex);
					}finally{
						outputFile.close();
						outputStream.close();
					}
				} catch (Exception ex) {
			        System.out.println("Exception occurs in downloading the file >>>>>>>>>>>>>" + ex);
				}
			}
		} catch(Exception ex){
            System.out.println("Exception occurs in doGet method >>>>>>>>>>>>>" + ex);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		try {
			String uri = request.getRequestURI();
			if(uri.equals(UPLOAD_FILE)){
				response.setContentType(DriveStaticHelper.RESPONSE_CONTENT_TYPE);   
				PrintWriter writer = response.getWriter();   
		        JSONObject responseObject = null;
		        if(ServletFileUpload.isMultipartContent(request)){
		            try {
		                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
						String filePath = request.getParameter(DriveStaticHelper.LOCATION);
						String location = (filePath==null) ? "":filePath+"/";
		                for(FileItem item : multiparts){
		                    if(!item.isFormField()){
		                        String name = new File(item.getName()).getName();
		                        
		                        File inputFile = new File(DriveStaticHelper.UPLOAD_DIRECTORY + name);
		                        item.write(inputFile );
						        
						        ServletContext context = getServletContext();
						        InputStream is = context.getResourceAsStream(name);
						        if (is != null) {
						            InputStreamReader isr = new InputStreamReader(is);
			       					responseObject = DriveStaticHelper.addFileToDrive(is,location+name);
						        }

		                    }
		                }
		                writer.print(responseObject);
		            } catch (Exception ex) {
		               System.out.println("Exception occurs while Uploading the file in drive >>>>>>>>>>>>>" + ex);
		            }          
	       	 	}
	       	} 
	    } catch (Exception ex) {
            System.out.println("Exception occurs in doPost method >>>>>>>>>>>>>" + ex);
        } 
    }
}
