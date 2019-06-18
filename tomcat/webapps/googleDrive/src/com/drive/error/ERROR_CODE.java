package com.drive.error;

public class ERROR_CODE {

    public enum ERRORS{

        GET_FOLDER_DETAILS(1,"Exception occurs in get folder details"),
        FILE_NAME_EMPTY(2,"File name is empty"),
        FOLDER_NAME_EMPTY(3,"Folder name is empty"),
        FILE_ID_EMPTY(4,"File ID is empty"),
        FILE_OR_FOLDER_NAME_EMPTY(5,"File or folder name is empty"),
        FOLDER_NOT_FOUND(6,"Folder not found"),
        FILE_OR_FOLDER_NOT_FOUND(7,"Folder or file not found"),
        FILE_ID_NOT_FOUND(8,"The given file ID is not a file or given file ID does not exist"),
        FILE_ALREADY_PRESENT(9,"File name already exist");


        private final int statusCode;
        private final String errorMessage;


        public int getStatusCode() {
            return statusCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        ERRORS (int statusCode, String errorMessage) {
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }
    }

}
