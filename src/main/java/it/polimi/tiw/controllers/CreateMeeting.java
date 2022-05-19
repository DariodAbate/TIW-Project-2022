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
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * This servlet is used to save a meeting and the participants to it in the database.
 */
@WebServlet("/CreateMeeting")
public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    public CreateMeeting() {
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
		// retrieve data of meeting from session
		HttpSession session = request.getSession();
		String anagraphicPath = getServletContext().getContextPath() + "/anagraphicPage.html";
		if (session.isNew() || session.getAttribute("meetingForm") == null) {
			response.sendRedirect(anagraphicPath);
			return;
		}	
			
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		//saving meeting
		Meeting savedMeeting = (Meeting) session.getAttribute("meetingForm");
		session.removeAttribute("meetingForm");
		try {
			meetingDAO.createMeeting(savedMeeting);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot save the meeting");
			return;
		}
		
		//saving participating of that meeting
		ArrayList<Integer> userList = (ArrayList<Integer>) request.getAttribute("invitedPeople");
		if(userList == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot save the participant for a meeting");
			return;
		}
		
		for(int idUser: userList) {
			try {
				meetingDAO.createParticipant(idUser, false);
			}catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot save the participant for a meeting");
				return;
			}
		}
		
		//saving creator of that meeting
		User creator = (User) session.getAttribute("user");
		int idUserCreator = creator.getIdUser();
		try {
			meetingDAO.createParticipant(idUserCreator, true);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot save the participant for a meeting");
			return;
		}
		
		//redirect to home page
		String homePath = getServletContext().getContextPath() + "/GoToHome";
		response.sendRedirect(homePath);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e ){
			e.printStackTrace();
		}
	}

}
