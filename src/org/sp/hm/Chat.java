package org.sp.hm;

/**
 * Created by IntelliJ IDEA.
 * User: sp
 * Date: 19.06.12
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class Chat { 
    public static String chatColor;
    
    public static String $(String msg) {
        return "§" + chatColor + msg;
    }
}
