package com.facecheck.tools;
/**
 *
 * @author user
 */
public class AppInfo {
    public static final String APP_NAME = "FaceCheck Client";
    public static final int APP_VERSION_CODE = 1;
    public static final String APP_VERSION = "1.0";
    public static final String APP_PRINT_NAME = String.format("%s  V.%s", APP_NAME, APP_VERSION);
    public static final String DATABASE_TYPE = "sqlite";
    public static final String BASE_URL = "http://182.65.121.30:3000/api/v0/";
    public static final String GET_CAMERAS = "camera/all";
    public static final String ADD_CAMERA = "camera/add";
    public static final String EDIT_CAMERA = "camera/edit";
    public static final String DELETE_CAMERA = "camera/delete";
}
