package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection con; //session between a Java application and a database

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	/**
	 * This method gets a user if it is logged
	 * @param user username of the user
	 * @param pwd password of the user
	 * @return a user object associated to the login credentials, null if the user is not registered
	 * @throws SQLException
	 */
	public User checkCredentials(String user, String pwd) throws SQLException{
		//Don't need of password
		String query = "SELECT  idUser, username, mail FROM user WHERE username = ? AND  password = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setString(1, user);
			pstatement.setString(2, pwd);
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.isBeforeFirst()) //user is not logged
					return null;
				else {
					result.next();//only one user with that username and password
					User u = new User();
					u.setIdUser(result.getInt("idUser"));
					u.setUsername(result.getString("username"));
					u.setMail(result.getString("mail"));
					return u;
				}
			}
		}
	}
	
	/**
	 * This method register a user into the database
	 * @param user user bean that contains informations about that player
	 * @throws SQLException
	 */
	public void registerUser(User user) throws SQLException{
		String query = "INSERT into `user` (`username`, `password`, `mail`) VALUES (?, ?, ?)";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setString(1, user.getUsername());
			pstatement.setString(2, user.getPassword());
			pstatement.setString(3, user.getMail());
			
			pstatement.executeUpdate();
		}
	}
	
	/**
	 * This method is used to get all the user except the one that is logged
	 * @param idUserCreator id of the user that is logged
	 * @return list of user except that do not contain the one that is logged
	 * @throws SQLException
	 */
	public ArrayList<User> findUsersExceptCreator(int idUserCreator) throws SQLException{
		String query = "select idUser, username, mail FROM user WHERE idUser <> ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, idUserCreator);
			try(ResultSet result = pstatement.executeQuery();){
				ArrayList<User> temp = new ArrayList<>();
				while(result.next()) {
					User u = new User();
					
					u.setIdUser(result.getInt("idUser"));
					u.setUsername(result.getString("username"));
					u.setMail(result.getString("mail"));
					
					temp.add(u);
				}
				return temp;
			}
		}
	}
}
