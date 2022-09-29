package com.bcld;  

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.WebAppContext;

public class Start {

    private static final int SERVER_PORT = 8080;
    private static final String CONTEXT_PATH = "/Bcld";
    private static final String RESOURCE_BASE = "src/main/webapp";
    private static final String DESCRIPTOR = "src/main/webapp/WEB-INF/web.xml";

    public static void main(String[] args) throws Exception {
        
        Server server = new Server(SERVER_PORT);
        server.setStopAtShutdown(true);   
        WebAppContext context = new WebAppContext();
        context.setContextPath(CONTEXT_PATH);
        context.setResourceBase(RESOURCE_BASE);//123
        context.setDescriptor(DESCRIPTOR);
        context.setParentLoaderPriority(true);
        context.addServlet(DefaultServlet.class, "/");
        context.setInitParameter("useFileMappedBuffer", "false");
        server.setHandler(context);
        server.start();
        server.join();
    }
    
}
