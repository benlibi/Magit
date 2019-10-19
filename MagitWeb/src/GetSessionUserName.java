import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/getSessionUserName"})
public class GetSessionUserName extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //guestName != null && !guestName.isEmpty();
        try (PrintWriter out = resp.getWriter()) {
            HttpSession aSession = req.getSession();
            //Gson gson = new Gson();
            //String json = gson.toJson(aSession.getAttribute("UserName"));
            out.println(aSession.getAttribute("UserName"));
            out.flush();
        }
    }
}
