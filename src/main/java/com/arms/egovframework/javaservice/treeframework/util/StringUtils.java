package com.arms.egovframework.javaservice.treeframework.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class StringUtils extends org.apache.commons.lang.StringUtils {

    public static  String removeHtmlTags(String value) {
        String rtnVal = StringEscapeUtils.unescapeHtml4(value);
        // unescapeHtml : <p><h3 class="kkk">테스트</p> 케이스

        rtnVal = rtnVal.replaceAll("<(/)?([a-zA-Z]*)([0-9]?)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
        //  태그제거 : 테스트 케이스

        rtnVal = StringEscapeUtils.escapeHtml4(rtnVal);
        // escapeHtml : 테스트&nbsp;케이스

        return rtnVal;
    }
    public static String getString(String text) {
        if (null == text) {
            return "";
        }
        
        return text;
    }
    
    public static String getString(Date text) {
        if (null == text) {
            return "";
        }
        
        return text.toString();
    }
    
    public static boolean getBoolean(String text) {
        if (equalsIgnoreCase(text, "true") || equalsIgnoreCase(text, "1")) {
            return true;
        }
        
        return false;
    }
    
    public static String getString(int value) {
        return ((Integer) value).toString();
    }
    
    public static String getString(long value) {
        return ((Long) value).toString();
    }
    
    public static String getString(boolean flag) {
        if (flag) {
            return "1";
        } else {
            return "0";
        }
    }
    
    public static String makeIpv4Cidr(String ip) {
        if (!StringUtils.contains(ip, "/")) {
            return ip + "/32";
        }
        
        return ip;
    }
    
    public static String[] makeIpv4(String ipMask) {
        if (!StringUtils.contains(ipMask, "/")) {
            String[] ips = { ipMask, "32" };
            return ips;
        } else {
            return StringUtils.split(ipMask, "/");
        }
    }
    
    public static String[] getTypeForIp(String value) {
        String type = "0";
        String mask = "32";
        String ip = value;
        if (contains(value, "-")) {
            type = "2";
            String[] temp = split(value, "/");
            ip = temp[0];
        } else if (contains(value, "/")) {
            type = "1";
            String[] temp = split(value, "/");
            ip = temp[0];
            mask = temp[1];
            
            if (StringUtils.equals(mask, "32")) {
                type = "0";
            }
        }
        
        String[] result = { ip, type, mask };
        return result;
    }
    
    public static List<List<String>> diffMembers(List<String> oldMember, List<String> newMember) {
        return diffMembers(oldMember.toArray(new String[oldMember.size()]), newMember.toArray(new String[newMember.size()]));
    }
    
    public static List<List<String>> diffMembersLong(List<Long> oldMember, List<Long> newMember) {
        List<String> newMembers = new ArrayList<>();
        List<String> oldMembers = new ArrayList<>();
        for (Long tempLong : oldMember) {
            oldMembers.add(StringUtils.getString(tempLong));
        }
        for (Long tempLong : newMember) {
            newMembers.add(StringUtils.getString(tempLong));
        }
        
        return diffMembers(oldMembers, newMembers);
    }
    
    public static List<List<String>> diffMembers(String[] oldMember, String[] newMember) {
        Map<String, Integer> oldMap = new LinkedHashMap<>();
        List<String> newMembers = new ArrayList<>();
        if (null != oldMember) {
            for (String string : oldMember) {
                oldMap.put(string, 0);
            }
        }
        
        if (null != newMember && newMember.length > 0) {
            for (String string : newMember) {
                if (oldMap.containsKey(string)) {
                    oldMap.put(string, 1);
                } else {
                    newMembers.add(string);
                }
            }
        }
        
        List<String> updateMembers = new ArrayList<>();
        List<String> deleteMembers = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : oldMap.entrySet()) {
            if (entry.getValue() > 0) {
                updateMembers.add(entry.getKey());
            } else {
                deleteMembers.add(entry.getKey());
            }
        }
        
        List<List<String>> result = new ArrayList<>();
        result.add(deleteMembers);
        result.add(newMembers);
        result.add(updateMembers);
        
        return result;
    }
    
    
    public static String removeMember(String members, String removeMember) {
        String members2 = ";" + members + ";";
        String result = StringUtils.replace(members2, removeMember + ";", "");
        
        return StringUtils.substring(result, 1, result.length() - 1);
    }
    
    public static String getFullURL(HttpServletRequest request) {
        String requestURL = request.getRequestURI();
        String queryString = request.getQueryString();
        
        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL + "?" + queryString;
        }
    }
    
    public static String changeApplyIcon(String applyFlag) {
        if (StringUtils.equals(applyFlag, "1")) {
            return "<img src=\"/files/image/icon/apply.png\" class=\"imgTop\" />";
        }
        return "";
    }
    
    public static String substringBeforeLastByte(String str, String encoding, int cutByte) throws UnsupportedEncodingException {
        int subStrByte = str.getBytes(encoding).length - cutByte;
        
        if(subStrByte <= 0) {
            return "";
        }
        
        String temp = str;
        while (subStrByte < temp.getBytes(encoding).length) {
            temp = substring(temp, 0, temp.length() - 1);
        }
        
        return temp;
    }
    
    public static String[] splitStringByNewLineOrTab(String str) {
        
        return str.split("(\r\n)|\r|\n|\t");
    }

    public static String nvl(String str, String defaultStr) {
        return str == null ? defaultStr : str ;
    }

    public static String[] nvl(String[] input) {
        if(input == null) {
            return new String[0];
        }
        return input;
    }

    public static String enterToBr(String str) {
        str = replaceString(str);
        return str == null ? "" : str.replaceAll("\n", " <br/>");
    }

    public static String enterToNull(String str) {
        return str == null ? "" : str.replaceAll("\n", "");
    }

    public static String decode (String msg, String type) throws UnsupportedEncodingException {
        return URLDecoder.decode (msg, type);
    }

    /**
     * toString();
     * @param value
     * @return
     */
    public static String toString(int value) {
        try {
            return value+"";
        } catch (Exception e) {
            return "";
        }
    }

    public static int toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String lpad(int value, int length, String prefix) {
        try {
            StringBuilder sb = new StringBuilder();
            String castValue = value + "";

            for (int i = castValue.length(); i< length; i++) {
                sb.append(prefix);
            }
            sb.append(castValue);
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String rpad(int value, int length, String prefix) {
        try {
            StringBuilder sb = new StringBuilder();
            String castValue = value + "";
            sb.append(castValue);
            for (int i = castValue.length(); i< length; i++) {
                sb.append(prefix);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String cutText(String text, int length, String suffix) {
        StringBuffer sb = new StringBuffer();

        if (!text.isEmpty()) {
            if (text.length() > length) {
                sb.append(text.substring(0, length)).append(suffix);
            } else {
                sb.append(text);
            }
        } else {
            sb.append(text);
        }
        return sb.toString();
    }

    //String이 비었거나 null인지 검사
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String replaceText(String text, String originTxt, String replaceTxt) {
        return text.replaceAll(originTxt, replaceTxt);
    }


    public static String replaceBrTag(String text) {
        return text.replaceAll("\\n", "<br/>");
    }

    /**
     *  get String of java.util.Map keys and values to log
     */
    public static String getDatasOfMap(Map map) {
        StringBuffer buf = new StringBuffer();
        buf.append("getDatasOfMap() =>");
        Set set = map.keySet();
        Iterator iter = set.iterator();
        while(iter.hasNext()) {
            String name = (String)iter.next();
            String value = (String)map.get(name);
            buf.append("[").append(name).append("|").append(value).append("]");
        }
        return buf.toString();
    }


    public static boolean isNullCheck(String str) {
        boolean bool = false;

        if(str != null && !"".equals(str)) {
            bool = true;
        }

        return bool;
    }


    /**
     * 콤마 추가
     * @param data
     * @return
     */
    public static String addComma(long data) {

        return new DecimalFormat("#,###").format(data);
    }

    /**
     * 해당 URL 의 HTML 코드를 String 으로 가져옴
     * @param uri
     * @return
     */
    public static String getSource(String uri){
        String str=null;
        try {
            URL url = new URL( uri );
            URLConnection uc = url.openConnection();
            InputStream in = uc.getInputStream();
            int len = uc.getContentLength();
            byte buf[] = new byte[len];
            in.read(buf, 0, buf.length);
            str = new String(buf);
        } catch (Exception e){
            log.info("StringUtils :: getSource :: Exception -> " + e.getMessage());
        }
        return str;
    }
    /**
     * 길이체크
     * @param min
     * @param max
     * @param str
     * @return
     */
    public static boolean chkLength (int min, int max, String str) {
        int len = str.length ();
        return len < min || len > max ;
    }
    /**
     * 반복값 체크
     * @param str
     * @param cmpCnt
     * @return
     */
    public static boolean chkRpt (String str, int cmpCnt) {
        ByteBuffer bf = ByteBuffer.wrap(str.getBytes());
        //반복된 개수
        int rptCnt = 1;
        //첫번째 문자코드
        int curr = bf.get ();
        //에러 여부
        boolean flag = false;

        for (int i = bf.position(), last = bf.capacity(); i < last; i += 1) {
            //임시 변수
            int tmp = bf.get();
            //첫번째 문자와 현재 문자의 차이가 1이면
            if ( curr == tmp ) {
                //연속 카운트 1증가
                rptCnt += 1;
            } else {
                //카운트 리셋
                rptCnt = 1;
            }
            //숫자 변경
            curr = tmp;
            //연속횟수가 설정한 값과 같다면 종료
            //$ANALYSIS-IGNORE
            if ( (flag = rptCnt == cmpCnt) ) {
                break;
            }
        }
        return flag;
    }
    /**
     * 지정한 Byte로 문자열을 자르고 지정한 말줄임 문자를 붙인다.
     * @param raw
     * @param len
     * @param encoding
     * @param prefix
     * @return
     */
    public static String stringByteCut(String raw, int len, String encoding, String prefix) {
        if (raw == null) return null;
        String[] ary = null;
        String result = null;
        try {
            // raw 의 byte
            byte[] rawBytes = raw.getBytes(encoding);
            int rawLength = rawBytes.length;

            int index = 0;
            int minus_byte_num = 0;
            int offset = 0;

            int hangul_byte_num = encoding.equals("UTF-8") ? 3 : 2;

            if(rawLength > len){
                int aryLength = (rawLength / len) + (rawLength % len != 0 ? 1 : 0);
                ary = new String[aryLength];

                for(int i=0; i<aryLength; i++){
                    minus_byte_num = 0;
                    offset = len;
                    if(index + offset > rawBytes.length){
                        offset = rawBytes.length - index;
                    }
                    for(int j=0; j<offset; j++){
                        if(((int)rawBytes[index + j] & 0x80) != 0){
                            minus_byte_num ++;
                        }
                    }
                    if(minus_byte_num % hangul_byte_num != 0){
                        offset -= minus_byte_num % hangul_byte_num;
                    }
                    ary[i] = new String(rawBytes, index, offset, encoding);
                    index += offset ;

                }
                result = ary[0] + prefix;
            } else {
                //ary = new String[]{raw};
                result = raw;
            }
        } catch(Exception e) {
            log.info("StringUtils :: stringByteCut :: Exception -> " + e.getMessage());
        }
        return result;
    }
    public static Boolean regex (String regex,  String str) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }
    public static boolean NumberChk(String str){
        str = str.trim();
        char c = ' ';

        if(str.length() == 0) return false;

        int loopCnt = str.length();
        for(int i = 0 ; i < loopCnt ; i++){
            c = str.charAt(i);
            if(c < 48 || c > 57){
                return false;
            }
        }
        return true;
    }

    /**
     * 특수문자를 웹 브라우저에서 정상적으로 보이기 위해 특수문자를 처리 하는 기능이다
     */
    public static String replaceString(String str)	{
        String returnValue = "";
        if(  str== null ) 			{
            returnValue="" ;
        } else {
            str = str.replaceAll("&"  , "&amp;");
            str = str.replaceAll("<"  , "&lt;");
            str = str.replaceAll(">"  , "&gt;");
            str = str.replaceAll("\'" , "&apos;");
            str = str.replaceAll("\"" , "&quot;");
//		  	str = str.replaceAll("%"  , "&#37;");
//		  	str = str.replaceAll(" "  , "&#10;");
//		  	str = str.replaceAll("\r"  , "&#10;");
//		  	str = str.replaceAll("\n"  , "&#10;");
//		  	str = str.replaceAll("\\("  , "&#40;");
//		  	str = str.replaceAll("\\)"  , "&#41;");
//		  	str = str.replaceAll("#"  , "&#35;");
            returnValue = str;

        }
        return returnValue ;
    }

    public static boolean adminYn(String str){
        return Pattern.matches("^[가-힣]*$", str);
    }

    public static String getClientIp(HttpServletRequest request){
        String clientIp = StringUtils.nvl(request.getHeader("X-Forwarded-For"),"");
        if(clientIp == null || clientIp.length() == 0){
            clientIp = StringUtils.nvl(request.getHeader("WL-Proxy-Client-IP"),"");
        }
        if(clientIp == null || clientIp.length() == 0){
            clientIp = StringUtils.nvl(request.getHeader("Proxy-Client-IP"),"");
        }
        if(clientIp == null || clientIp.length() == 0){
            clientIp = request.getRemoteAddr();
        }
        return clientIp.trim();
    }

    public static String commaStr(Long num) {
        String commaNum = NumberFormat.getInstance(Locale.US).format(num);
        return commaNum;
    }

    public static String commaStr(int num) {
        String commaNum = NumberFormat.getInstance(Locale.US).format(num);
        return commaNum;
    }

    public static String nullTrim( String str ) {
        if (str == null) {
            str = "";
        } else {
            str = str.trim();
        }
        return str;
    }

    public static String[] jsonStringifyConvert(String versionInfo) {
        versionInfo = StringUtils.remove(versionInfo, "\"");
        versionInfo = StringUtils.remove(versionInfo, "]");
        versionInfo = StringUtils.remove(versionInfo, "[");
        return StringUtils.split(versionInfo, ",");
    }
}
