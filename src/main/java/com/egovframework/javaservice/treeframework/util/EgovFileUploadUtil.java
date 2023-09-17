package com.egovframework.javaservice.treeframework.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class EgovFileUploadUtil extends EgovFormBasedFileUtil {

    public static List<EgovFormBasedFileVo> uploadFiles(HttpServletRequest request, String where, long maxFileSize) throws Exception {
        List<EgovFormBasedFileVo> list = new ArrayList();
        MultipartHttpServletRequest mptRequest = (MultipartHttpServletRequest)request;
        Iterator fileIter = mptRequest.getFileNames();

        while(fileIter.hasNext()) {
            MultipartFile mFile = mptRequest.getFile((String)fileIter.next());
            if( mFile != null){
                EgovFormBasedFileVo vo = new EgovFormBasedFileVo();
                String tmp = mFile.getOriginalFilename();
                if( tmp != null){
                    if (tmp.lastIndexOf("\\") >= 0) {
                        tmp = tmp.substring(tmp.lastIndexOf("\\") + 1);
                    }

                    vo.setFileName(tmp);
                    vo.setContentType(mFile.getContentType());
                    vo.setServerSubPath(getTodayString());
                    vo.setPhysicalName(getPhysicalFileName());
                    vo.setSize(mFile.getSize());
                    if (tmp.lastIndexOf(".") >= 0) {
                        vo.setPhysicalName(vo.getPhysicalName());
                    }

                    if (mFile.getSize() > 0L) {
                        InputStream is = null;

                        try {
                            is = mFile.getInputStream();
                            saveFile(is, new File(EgovWebUtil.filePathBlackList(where + SEPERATOR + vo.getServerSubPath() + SEPERATOR + vo.getPhysicalName())));
                        } finally {
                            if (is != null) {
                                is.close();
                            }

                        }

                        list.add(vo);
                    }
                }else{
                    throw new RuntimeException("EgovFileUploadUtil :: uploadFiles : mFile.getOriginalFilename() is null");
                }

            }else{
                throw new RuntimeException("EgovFileUploadUtil :: uploadFiles : MultipartFile is null");
            }
        }

        return list;
    }
}
