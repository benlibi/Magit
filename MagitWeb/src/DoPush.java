import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/push"})
public class DoPush extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
            String user = (String) request.getSession().getAttribute("UserName");
            String repo = (String) request.getSession().getAttribute("repoName");
            MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
            magitManager.setRepo(repo, user);
            magitManager.push();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
        }

}
