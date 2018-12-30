package com.techprd.cordova.httpd;

import android.content.Context;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer extends NanoHTTPD {
    public WebServer(InetSocketAddress localAddr, AndroidFile wwwroot, Context context) throws IOException {
        super(localAddr, wwwroot, context);
    }

    public WebServer(int port, AndroidFile wwwroot, Context context) throws IOException {
        super(port, wwwroot, context);
    }
}
