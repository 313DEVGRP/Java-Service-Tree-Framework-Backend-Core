package com.egovframework.javaservice.treeframework.util;


import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Wrapper;

public class EgovResourceCloseHelper {
    public EgovResourceCloseHelper() {
    }

    public static void close(Closeable... resources) {
        Closeable[] var1 = resources;
        int var2 = resources.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Closeable resource = var1[var3];
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception var6) {
                    EgovBasicLogger.ignore("Occurred Exception to close resource is ingored!!");
                }
            }
        }

    }

    public static void closeDBObjects(Wrapper... objects) {
        Wrapper[] var1 = objects;
        int var2 = objects.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Object object = var1[var3];
            if (object != null) {
                if (object instanceof ResultSet) {
                    try {
                        ((ResultSet)object).close();
                    } catch (Exception var7) {
                        EgovBasicLogger.ignore("Occurred Exception to close resource is ingored!!");
                    }
                } else if (object instanceof Statement) {
                    try {
                        ((Statement)object).close();
                    } catch (Exception var8) {
                        EgovBasicLogger.ignore("Occurred Exception to close resource is ingored!!");
                    }
                } else {
                    if (!(object instanceof Connection)) {
                        throw new IllegalArgumentException("Wrapper type is not found : " + object.toString());
                    }

                    try {
                        ((Connection)object).close();
                    } catch (Exception var6) {
                        EgovBasicLogger.ignore("Occurred Exception to close resource is ingored!!");
                    }
                }
            }
        }

    }

    public static void closeSocketObjects(Socket socket, ServerSocket server) {
        if (socket != null) {
            try {
                socket.shutdownOutput();
            } catch (Exception var5) {
                EgovBasicLogger.ignore("Occurred Exception to shutdown ouput is ignored!!");
            }

            try {
                socket.close();
            } catch (Exception var4) {
                EgovBasicLogger.ignore("Occurred Exception to close resource is ignored!!");
            }
        }

        if (server != null) {
            try {
                server.close();
            } catch (Exception var3) {
                EgovBasicLogger.ignore("Occurred Exception to close resource is ignored!!");
            }
        }

    }

    public static void closeSockets(Socket... sockets) {
        Socket[] var1 = sockets;
        int var2 = sockets.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Socket socket = var1[var3];
            if (socket != null) {
                try {
                    socket.shutdownOutput();
                } catch (Exception var7) {
                    EgovBasicLogger.ignore("Occurred Exception to shutdown ouput is ignored!!");
                }

                try {
                    socket.close();
                } catch (Exception var6) {
                    EgovBasicLogger.ignore("Occurred Exception to close resource is ignored!!");
                }
            }
        }

    }
}
