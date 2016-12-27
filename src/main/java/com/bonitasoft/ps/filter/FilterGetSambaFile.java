package com.bonitasoft.ps.filter;

import groovy.json.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;

/**
 * Created by pablo on 27/12/2016.
 */
public class FilterGetSambaFile implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterGetSambaFile.class);

    private FilterConfig filterConfig;
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String url ="", queryString="";
        if (request instanceof HttpServletRequest) {
            url = ((HttpServletRequest)request).getRequestURL().toString();
            queryString = ((HttpServletRequest)request).getQueryString();
        }
        LOGGER.info("Filtering request to "+ url + "?" + queryString);


        //Parametro que indicaba si era lectura o escritutra
        String o = request.getParameter("operation");
        LOGGER.info("Operation is "+ o);
        if(o!=null && o.equals("read")){
            getSambaFile((HttpServletRequest)request,(HttpServletResponse)response);

        }else{
            LOGGER.debug("We do not handle this query, we let it go ");
            chain.doFilter(request, response);
            return;
        }

    }

    public void destroy() {
        filterConfig = null;
    }
    private void getSambaFile(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        LOGGER.info("Start");
        Serializable result;
        String fileName = request.getParameter("fileName");
        String download = request.getParameter("download");

        if (fileName == null) {
            result = new HashMap<String, String>();
            ((HashMap)result).put("error","the parameter fileName is missing");
            buildResponse( HttpServletResponse.SC_BAD_REQUEST,new JsonBuilder(result).toPrettyString(), response);
            return;
        }
        String mimeType = getMimeType(fileName);
        File file = getFile(fileName);
        LOGGER.info("End");
        buildFileResponse(file, fileName, mimeType, download, response);
        return;
    }

    private File getFile(String fileName) {
        // TO DO
        return new File ("C:\\Proyectos\\Binter\\example.pdf");
    }

    private void buildFileResponse(File file,String fileName, String mimeType, String download, HttpServletResponse response) throws IOException {
        response.setContentType(mimeType);

        //IF WE WANT TO FORCE DOWNLOAD
        if("true".equals(download)) {
            LOGGER.info("Download "+download);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        }

        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0){
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
    }

    private String getMimeType(String fileName) {
        //TO DO
        return "application/pdf";
    }

    private void buildResponse(int responseCode, String result, HttpServletResponse response) throws IOException{
        response.setStatus(responseCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer=response.getWriter();
        writer.append(result);
        writer.flush();


    }





}
