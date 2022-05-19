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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * This servlet is used to load the dynamic content of the anagraphic page.
 */
@WebServlet("/GetRegisteredUser")
public class GetRegisteredUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
		

    public GetRegisteredUser() {
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
		// If the user is not logged in (not present in session), redirect to the login
		HttpSession session = request.getSession();
		String loginPath = getServletContext().getContextPath() + "/loginPage.html";
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginPath);
			return;
		}	
		User user = (User) request.getSession().getAttribute("user");
		
		
		UserDAO userDAO = new UserDAO(connection);
		ArrayList<User> usersInvitable = new ArrayList<>();
		
		try {
			String usernameCreator = user.getUsername();
			usersInvitable = userDAO.findUsersExceptCreator(usernameCreator);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot get users list");
			return;
		}
		
		String path = "/WEB-INF/anagraphicPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("userListInvitable", usersInvitable);
		templateEngine.process(path, ctx, response.getWriter());
	
	}



	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException e ){
			e.printStackTrace();
		}
	}
}
