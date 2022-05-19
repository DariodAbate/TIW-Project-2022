package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * This servlet is used to parse the data taken from the html form and
 * save them in the session.
 */
@WebServlet("/SaveMeeting")
public class SaveMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
 
    public SaveMeeting() {
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
		String title = request.getParameter("title");
		String dateString = request.getParameter("date");
		String timeString = request.getParameter("time");
		String durationString = request.getParameter("duration");
		String maxParticipantString = request.getParameter("maxPart");
		
		if(title == null || title.isEmpty() || dateString == null || dateString.isEmpty() 
				|| timeString == null || timeString.isEmpty()
				|| durationString == null || durationString.isEmpty()
				|| maxParticipantString == null || maxParticipantString.isEmpty()) {
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;//early return
		}
			
		//parsing date
		Date date = null;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
			if(date.before(cal.getTime())) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Date cannot be in the past");
				return;//early return
			}
		}catch (ParseException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad date format creation");
			return;//early return
		}
		
		// parsing time
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		long ms = -1;
		try {
			ms = sdf.parse(timeString).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad time format creation");
			return;//early return
		}
		
		Time time = new Time(ms);
		
		//cannot insert a time in the past
		String currentTimeString = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
		
		sdf = new SimpleDateFormat("HH:mm");
		ms = -1;
		
		try {
			ms = sdf.parse(currentTimeString).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "cannot get current time");
			return;//early return
		}
		Time currentTime = new Time(ms);

		if(date.compareTo(cal.getTime()) == 0 && time.compareTo(currentTime) < 0) {//same days, but time in the past
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Time cannot be in the past");
			return;//early return
		}
		
		//parsing duration and maxParticipant
		int duration = -1;
		int maxParticipant = -1;
		try{
			duration = Integer.parseInt(durationString);
			maxParticipant = Integer.parseInt(maxParticipantString);
			if(duration <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The number of hours must be positive");
				return;//early return	
			}
			if(maxParticipant <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The maximum number of participants must be positive");
				return;//early return	
			}
		} catch(NumberFormatException e1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Number format");
			return;//early return	
		}
		
		Meeting m = new Meeting();
		m.setTitle(title);
		m.setDate(date);
		m.setTime(time);
		m.setDuration(duration);
		m.setMaxParticipant(maxParticipant);
		
		// constructing new URI redirecting to anagraphic page
		request.getSession().setAttribute("meetingForm", m);// save data from html form in session
		String path = getServletContext().getContextPath() + "/GetRegisteredUser";// refill dynamically the page
		response.sendRedirect(path);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e ){
			e.printStackTrace();
		}
	}

}
