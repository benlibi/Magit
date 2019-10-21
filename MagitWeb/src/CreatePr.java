import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/createPr"})
public class CreatePr extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
            String user = (String) request.getSession().getAttribute("UserName");
            String repo = (String) request.getSession().getAttribute("repoName");
            MagitManager magitManager = ServletUtils.getMagitManager(getServletContext());
            magitManager.setRepo(repo, user);
            String src_branch = request.getParameter("src_branch");
            String trg_branch = request.getParameter("trg_branch");
            String pr_msg = request.getParameter("pr_msg");
            magitManager.createPr(src_branch, trg_branch, pr_msg, user);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
        }

}
