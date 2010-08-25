/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities;

import com.sun.xml.internal.fastinfoset.DecoderStateTables;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class XmlUtils {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(XmlUtils.class);

    /**
     * Sends XML back to the client
     *
     * @param xml
     * @param startTime
     * @param response
     * @throws IOException
     */
    public static void sendXml(String xml, Long startTime, HttpServletResponse response)
               throws IOException {

        response.setContentType("text/xml");
        response.setCharacterEncoding("utf-8");
        Writer writer = response.getWriter();
        try {
            char[] characters = xml.toCharArray();
            for (int index = 0; index < characters.length; ++index) {
                char current = characters[index];
                if (DecoderStateTables.UTF8(current) == DecoderStateTables.STATE_ILLEGAL) current = '\u00BF';
                writer.write(current);
            }
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        
        log.debug(xml);
        log.info("Process completed in " + (new Date().getTime() - startTime.longValue()) + " milliseconds.");
    }

    public static void sendXml(XmlReplyBean xmlReply, Long startTime, HttpServletResponse response)
            throws IOException {
        
        sendXml(xmlReply.toXml(), startTime, response);
    }
}
