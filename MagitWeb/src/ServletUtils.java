import javax.servlet.ServletContext;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String CHAT_MANAGER_ATTRIBUTE_NAME = "chatManager";

    /*
    Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
    the actual fetch of them is remained un-synchronized for performance POV
     */
    private static final Object magitManagerLock = new Object();


    public static MagitManager getMagitManager(ServletContext servletContext) {

        synchronized (magitManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new MagitManager());
            }
        }
        return (MagitManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

}
