import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/myRepo"})
public class RepoServlet extends HttpServlet {

    private static final String CURRENT_REPO = "repoName";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String repoName = req.getParameter(CURRENT_REPO);
        if(repoName!=null) {
            //guestName != null && !guestName.isEmpty();
            HttpSession aSession = req.getSession();
            aSession.setAttribute(CURRENT_REPO, repoName);
            resp.sendRedirect(req.getContextPath() + "/repo.html");
        }
    }
}
