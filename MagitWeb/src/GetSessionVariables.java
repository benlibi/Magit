import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/getSessionVars"})
public class GetSessionVariables extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //guestName != null && !guestName.isEmpty();
        try (PrintWriter out = resp.getWriter()) {
            HttpSession aSession = req.getSession();
            Enumeration<String> attributes = aSession.getAttributeNames();
            Map<String, String> attributeMap = new HashMap<String, String>();
            while (attributes.hasMoreElements()) {
                String attribute = attributes.nextElement();
                String attributeValue = (String) aSession.getAttribute(attribute);
                attributeMap.put(attribute, attributeValue);
            }
            Gson gson = new Gson();
            String json = gson.toJson(attributeMap);
            out.println(json);

        }
    }
}
