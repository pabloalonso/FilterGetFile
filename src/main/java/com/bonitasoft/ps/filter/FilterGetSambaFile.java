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
    private static final String PARAM_OPERATION = "operation";
    private static final String PARAM_FILENAME = "fileName";
    private static final String PARAM_DOWNLOAD = "download";

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

        String o = request.getParameter(PARAM_OPERATION);
        LOGGER.info("Operation is "+ o);
        if("read".equals(o)){
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

    /**
     * Method that will see if we have enough information to get the file,
     * it will look for the fileName and if we have to download it.
     * @param request HTTP Request where the parameters come
     * @param response HTTP Response where we will put the response (file or error)
     * @throws ServletException
     * @throws IOException
     */
    private void getSambaFile(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        LOGGER.info("Start");
        Serializable result;
        String fileName = request.getParameter(PARAM_FILENAME);
        String download = request.getParameter(PARAM_DOWNLOAD);

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

    /**
     * Method that will generate a file response based on the download parameter
     * @param file File to return
     * @param fileName Filename that will be use to name the file on download
     * @param mimeType Mimetype of the file
     * @param download If we need to prompup the downlaod dialog "true"
     * @param response HTTP Response where we will put the file
     * @throws IOException
     */
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

    /**
     * Method that will generate a JSON response, usually a error one
     * @param responseCode HTTP Error code
     * @param result Message to return
     * @param response HTTP Response where we will put the JSON message
     * @throws IOException
     */
    private void buildResponse(int responseCode, String result, HttpServletResponse response) throws IOException{
        response.setStatus(responseCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer=response.getWriter();
        writer.append(result);
        writer.flush();
    }

    /**
     * Method that will return a MimeType (https://www.sitepoint.com/web-foundations/mime-types-complete-list/)
     * @param fileName Filename to extract the extension
     * @return The given MIME type
     */
    private String getMimeType(String fileName) {
        //TO DO
        return "application/pdf";
    }

    /**
     * Method to get the file from Filesystem / Samba
     * @param fileName FileName of the file
     * @return the file
     */
    private File getFile(String fileName) {
        // TO DO
        return new File ("C:\\Proyectos\\Binter\\example.pdf");
    }
}
