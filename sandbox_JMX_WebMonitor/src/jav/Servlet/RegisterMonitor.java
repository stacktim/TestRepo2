package jav.Servlet;

import jav.jmx.ServerMonitor.JavServerMonitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class RegisterMonitor
 */
public class RegisterMonitor extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public RegisterMonitor() {
    	//register();
    }
    
    public void register(){
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		ObjectName mbeanName;
		try {
			mbeanName = new ObjectName("jav.jmx.ServerMonitor:type=JavServerMonitor");
	

			// Create the Hello World MBean
			JavServerMonitor mbean = new JavServerMonitor();
			if ( mbs.isRegistered(mbeanName) == false ){
				mbs.registerMBean(mbean, mbeanName);
			}
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Entered");
		register();
		RequestDispatcher rd= getServletContext().getRequestDispatcher("/HelloWorld");
		rd.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Entered");
		register();
		RequestDispatcher rd= getServletContext().getRequestDispatcher("/HelloWorld");
		rd.forward(request, response);
	}

}
