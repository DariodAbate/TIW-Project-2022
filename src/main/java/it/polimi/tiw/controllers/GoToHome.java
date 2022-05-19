package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * This servlet redirect a logged user in the Homepage. 
 */
@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    public GoToHome() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		ArrayList<Meeting> allMeetings;
		ArrayList<Meeting> meetingsCreated  = new ArrayList<>(); 
		ArrayList<Meeting> meetingsInvited = new ArrayList<>();
		
		try {
			allMeetings = meetingDAO.findMeetingsByUser(user.getIdUser());
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot get meetings list");
			return;
		}
		
		//separating meetings
		for(Meeting meeting: allMeetings) {
			if(meeting.isCreator())
				meetingsCreated.add(meeting);
			else
				meetingsInvited.add(meeting);
		}
		
		String path = "/WEB-INF/homePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("meetingsCreated", meetingsCreated);
		ctx.setVariable("meetingsInvitedTo", meetingsInvited);
		templateEngine.process(path, ctx, response.getWriter());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e ){
			e.printStackTrace();
		}
	}
}
