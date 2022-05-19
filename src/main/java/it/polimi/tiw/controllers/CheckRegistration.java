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

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * This servlet is used to register a user to the application.
 * It checks the equality between the password fields and
 * the uniqueness of the username. 
 * Then, the user is redirected to the home page
 */
@WebServlet("/CheckRegistration")
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    public CheckRegistration() {
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
		String usr = request.getParameter("newUsername");
		String mail = request.getParameter("newMail");
		String password = request.getParameter("newPassword");;
		String repeatedPassword = request.getParameter("newRepeatedPassword");
		
		if(usr == null || usr.isEmpty() || mail == null || mail.isEmpty()
				|| password == null || password.isEmpty() || repeatedPassword == null || repeatedPassword.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		
		String path;
		if(!password.equals(repeatedPassword)) {//password are not equal
			path = "/loginPage.html";
			
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("passwordsDifferent", "Password do not match!");
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		
		ArrayList<User> registeredUser = new ArrayList<>();
		try {
			registeredUser = userDAO.findAllUsers();
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot check the username");
			return;
		}
		
		for(User regUser: registeredUser) {//checking unicity of nickname
			if(regUser.getUsername().equals(usr)) {
				path = "/loginPage.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("nicknameAlreadyTaken", "Your nickname is already taken, please insert another one!");
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}
		}
		
		User u = new User();
		
		try {
			u.setUsername(usr);
			u.setMail(mail);
			u.setPassword(password);
			
			userDAO.registerUser(u);
		}catch(SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot create a new user");
			return;
		}
		
		// constructing new URI redirecting to home page
		path = getServletContext().getContextPath() + "/loginPage.html"; 
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
