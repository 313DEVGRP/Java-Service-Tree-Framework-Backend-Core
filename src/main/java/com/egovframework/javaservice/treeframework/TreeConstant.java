package com.egovframework.javaservice.treeframework;

public class TreeConstant {

    public static final Long ROOT_CID = 1L;
    public static final Long First_Node_CID = 2L;

    public static final String Node_ID_Column = "c_id";

    public static final String Node_Position_Column = "c_position";

    public static final String Node_Left_Column = "c_left";

    public static final String Node_Right_Column = "c_right";

    public static final String Node_ParentID_Column = "c_parentid";
    public static final String Node_Type_Column = "c_type";

    public static final String ROOT_TYPE = "root";
    public static final String First_Node_TYPE = "drive";
    public static final String Leaf_Node_TYPE = "default";
    public static final String Branch_TYPE = "folder";

    public static final String REQADD_PREFIX_TABLENAME = "T_ARMS_REQADD_";
    public static final String REQSTATUS_PREFIX_TABLENAME = "T_ARMS_REQSTATUS_";

    public static final Long MAX_UPLOAD_FILESIZE = 313L;

    public static boolean isCType(String value){
        return ROOT_TYPE.equals(value)
                ||First_Node_TYPE.equals(value)
                ||Leaf_Node_TYPE.equals(value)
                ||Branch_TYPE.equals(value);

    }


}
