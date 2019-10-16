import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/login"})
public class signupServlet extends HttpServlet {

    private static final String USER_NAME_PARAMETER = "UserName";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userName = req.getParameter(USER_NAME_PARAMETER);
        if(userName==null)
            resp.sendRedirect(req.getContextPath() +"/index.html");
        //guestName != null && !guestName.isEmpty();
        MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
        magitManager.addCurrentUser(userName);
        HttpSession aSession = req.getSession();
        aSession.setAttribute(USER_NAME_PARAMETER, userName);
        resp.sendRedirect(req.getContextPath() +"/user.html");
    }
}
