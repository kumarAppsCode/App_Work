package Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import javax.faces.context.FacesContext;

import javax.naming.Context;

import javax.naming.InitialContext;

import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import oracle.apps.xdo.template.FOProcessor;
import oracle.apps.xdo.template.RTFProcessor;

import oracle.jbo.domain.Number;

public class RTFUtils {
    public RTFUtils() {
        super();
    }


    public static ServletContext getContext() {
        return (ServletContext)getFacesContext().getExternalContext().getContext();
    }

    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }


    //    public HttpServletResponse getResponse() {
    //           return (HttpServletResponse)getFacesContext().getExternalContext().getResponse();
    //       }

    public static byte[] runReport(String templateFile,
                                   oracle.jbo.domain.Number ID,
                                   String packageName, String dataSource) throws IOException,
                                                             Exception {
        //  String  templateFile = ;
        byte[] dataBytes = null;

        try {
            ServletContext context = getContext();
            InputStream fs = context.getResourceAsStream(templateFile);
            //Process RTF template to convert to XSL-FO format
            
            RTFProcessor rtfp = new RTFProcessor(fs);
            ByteArrayOutputStream xslOutStream = new ByteArrayOutputStream();
            rtfp.setOutput(xslOutStream);
            rtfp.process();
            System.err.println("====2====");
            //Use XSL Template and Data from the VO to generate report and return the OutputStream of report
            ByteArrayInputStream xslInStream =
                new ByteArrayInputStream(xslOutStream.toByteArray());

            FOProcessor processor = new FOProcessor();
            ByteArrayInputStream dataStream =
                //new ByteArrayInputStream((byte[])ADFUtils.findOperation("getXMLData").execute());
                new ByteArrayInputStream((getXMLData(ID, packageName,
                                                     dataSource).getBytes()));
            processor.setData(dataStream);
            processor.setTemplate(xslInStream);

            ByteArrayOutputStream pdfOutStream = new ByteArrayOutputStream();
            processor.setOutput(pdfOutStream);
            byte outFileTypeByte = FOProcessor.FORMAT_PDF;
            processor.setOutputFormat(outFileTypeByte);
            processor.generate();

            dataBytes = pdfOutStream.toByteArray();

        } catch (IOException e) {
            System.out.println("IOException when generating pdf===IO" +
                               e.toString());
            throw new IOException(e.getMessage());
        } catch (Exception e) {           
            System.out.println("IOException when generating pdf===EXE" +
                               e.toString());
            throw new Exception(e.getMessage());
        }
        return dataBytes;
    }


    public static String getXMLData(oracle.jbo.domain.Number ID,
                                    String packageName,
                                    String dataSource) throws Exception {
        String retStr = "";
        try {

            Context ctx = new InitialContext();
            Connection con = null;
            DataSource ds = (DataSource)ctx.lookup(dataSource);
            con = ds.getConnection();
            String selectSQL =
                "SELECT " + packageName + "('" + ID + "') xml FROM dual";

            PreparedStatement preparedStatement =
                con.prepareStatement(selectSQL);
            System.err.println("=====OUT XML==" + selectSQL);
            ResultSet rs = preparedStatement.executeQuery(selectSQL);
            while (rs.next()) {
                retStr = rs.getString("xml");
            }
            System.err.println("=====OUT XML==" + retStr);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return retStr;
    }

}
