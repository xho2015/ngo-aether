import java.io.File;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;

public class Program {

	//private static String PROJECT_PATH = "D:/NGO/client";// System.getProperty("user.dir");
	//private static String CATALINA_HOME = PROJECT_PATH + "/embed";
	//private static String WEB_APP_PATH = CATALINA_HOME + "/webroot";
	
	//A context path must either be an empty string or start with a '/' and do not end with a '/'. The path [/] does not meet these criteria and has been changed to []
	//private static String CONTEXT_PATH = "/s1web";
	
	private static Tomcat tomcat = new Tomcat();

	public static final String SHUT_CMD = "NGO_CATA_BYE";
	private boolean started = false;
	private int port;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int port = Integer.valueOf(System.getProperty("port","60080"));
		int shutPort = Integer.valueOf(System.getProperty("shutPort","60010"));
		String shutCmd = System.getProperty("shutCmd", SHUT_CMD);
		String catalinaHome = System.getProperty("catalinaHome", "C:/NGO/embed");
		String webAppPath = catalinaHome + "/" +System.getProperty("webAppPath");
		String contextPath = System.getProperty("contextPath");
		
		
		startServer(port, shutPort, shutCmd, catalinaHome, webAppPath, contextPath);

	}
	//some change for testing C# kill eclipse process

	// ONE EXTERNAL CHANGE 03
	private static void startServer(int port, int shutPort, String shutCmd,String catalinaHome, String webAppPath, String contextPath) {
		try {
		
			StandardServer server = (StandardServer) tomcat.getServer();
			server.setPort(shutPort);
			server.setShutdown(shutCmd);
			
			AprLifecycleListener listener = new AprLifecycleListener();
			server.addLifecycleListener(listener);
			
			//below is simply added a web application with given name and path, the other folder 
			// in "CATALINA_HOME" will not be treaded as web app, jvisualvm->mbean show more details. 
			org.apache.catalina.Context ctx = tomcat.addWebapp(contextPath, webAppPath);
			ctx.setReloadable(true);
			
			tomcat.setPort(port);
			tomcat.setBaseDir(catalinaHome);
			tomcat.getHost().setAppBase(webAppPath);
			tomcat.getHost().setAutoDeploy(true);
			tomcat.enableNaming();

			//Tomcat.addServlet(ctx, "test", "Test2");
			//ctx.addServletMapping("/test", "test");
			tomcat.start();

		} catch (LifecycleException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		tomcat.getServer().await();
	}

	private static void stopServer() {
		try {
			tomcat.stop();
		} catch (LifecycleException ex) {
			ex.printStackTrace();
		}
	}
}
