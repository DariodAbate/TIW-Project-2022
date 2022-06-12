package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CheckNumInvitation
 */
@WebServlet("/CheckNumInvitation")
public class CheckNumInvitation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;     
	
	private final static int MAX_ATTEMPT = 3;
	
    public CheckNumInvitation() {
        super();

    }
    
    public void init() throws ServletException{
		ServletContext context = getServletContext();
    	
    	ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
    	templateResolver.setTemplateMode(TemplateMode.HTML);
    	this.templateEngine = new TemplateEngine();
    	this.templateEngine.setTemplateResolver(templateResolver);
    	templateResolver.setSuffix(".html");
    	
    	connection = ConnectionHandler.getConnection(context);
    }

	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//parsing user from checkbox
		ArrayList<Integer> userList = new ArrayList<>();
		Enumeration<String> selectedUser = request.getParameterNames();
		while(selectedUser.hasMoreElements()) {
			String idUserString = request.getParameter(selectedUser.nextElement());
			int idUser = -1;
			try {
				idUser = Integer.parseInt(idUserString);
			}catch(NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot check the selected user");
				return;//early return
			}
			
			userList.add(idUser);
		}
		
		//not enough selection
		String path;
		if(userList.isEmpty()) {//not enough selection
			request.setAttribute("emptySelection", "Please select at least one user!");
			ServletContext servletContext = getServletContext();
			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/GetRegisteredUser");
			requestDispatcher.forward(request,response);
			return;
		}
		
		
		//retrieve meeting.maxParticipant from session
		HttpSession session = request.getSession();
		String anagraphicPath = getServletContext().getContextPath() + "/anagraphicPage.html";
		if(session.isNew() || session.getAttribute("meetingForm") == null) {
			response.sendRedirect(anagraphicPath);
			return;
		}
		Meeting meeting = (Meeting) request.getSession().getAttribute("meetingForm");
		int maxParticipant = meeting.getMaxParticipant();
		
		//too much participant
		if(userList.size() > maxParticipant) {
			//management of maximum attempt
			if(session.isNew() || session.getAttribute("failCounter") == null) {
				session.setAttribute("failCounter", 1);
			}else {
				int failCounter = (int) session.getAttribute("failCounter");
				++failCounter;
				session.setAttribute("failCounter", failCounter);
				
				if(failCounter == MAX_ATTEMPT) {
					session.removeAttribute("failCounter");//remove the counter from the session
					session.removeAttribute("meetingForm");//remove the data from the session
					/*
					WEB-INF directory is a private area of the web application, any files under WEB-INF directory
					cannot be accessed directly from browser by specifying the URL. Web container will not serve 
					the content of this directory. However the content of the WEB-INF directory is accessible by the classes/servlets within the application.
					*/
					path = "/WEB-INF/cancellationPage.html";
					ServletContext servletContext = getServletContext();
					final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
					templateEngine.process(path, ctx, response.getWriter());
					return;
				}
			}
				
			request.setAttribute("oversizeSelection", "Too many selected users, delete at least "+ (userList.size() - maxParticipant) +" !");
			request.setAttribute("previousChoice", userList);
			ServletContext servletContext = getServletContext();
			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/GetRegisteredUser");
			requestDispatcher.forward(request,response);
			return;
		}
		
		//creating a meeting by forawding to another servlet
		
		request.setAttribute("invitedPeople", userList);
		ServletContext servletContext = getServletContext();
		RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/CreateMeeting");
		requestDispatcher.forward(request,response);
		
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e ){
			e.printStackTrace();
		}
	}

}
