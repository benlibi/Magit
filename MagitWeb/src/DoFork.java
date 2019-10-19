import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/fork"})
public class DoFork extends HttpServlet {



    private MagitManager magitManager = new MagitManager();
    private static final String USER_NAME_PARAMETER = "UserName";


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
        String currentUser = (String) request.getSession().getAttribute("UserName");
        String repoName = request.getParameter("repoName");
        String remoteUser = request.getParameter("userName");
        magitManager.clone(repoName, remoteUser, currentUser);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
        }

}
