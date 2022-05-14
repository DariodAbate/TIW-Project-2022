package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;

import it.polimi.tiw.beans.Meeting;

public class MeetingDAO {
	private Connection con; //session between a Java application and a database

	public MeetingDAO(Connection connection) {
		this.con = connection;
	}
	
	/**
	 * This method is used to get a list of the Meeting associated with the logged user. 
	 * Thus, I need the information about the creator.
	 * @param idUser id of the logged user
	 * @return an array list of Meeting associated with the user
	 * @throws SQLException
	 */
	public ArrayList<Meeting> findMeetingsByUser(int idUser) throws SQLException{
		String query = "SELECT title, date, time, duration, maxParticipant, isCreator FROM participation NATURAL JOIN meeting WHERE idMeeting = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, idUser);
			try(ResultSet result = pstatement.executeQuery();){
				ArrayList<Meeting> temp = new ArrayList<>();
				while(result.next()) {
					Meeting m = new Meeting();
					
					m.setIdMeeting(idUser);
					m.setTitle(result.getString("title"));
					m.setDate(result.getDate("date"));
					m.setTime(result.getTime("time"));
					m.setDuration(result.getInt("duration"));
					m.setMaxParticipant(result.getInt("maxParticipant"));
					m.setCreator(result.getBoolean("isCreator"));
					
					temp.add(m);
				}
				return temp;
			}
		}
	}
	
	/**
	 * This method is used to insert the information about a meeting in the db
	 * @param meeting meeting beans that contains all the information
	 * @throws SQLException
	 */
	public void createMeeting(Meeting meeting) throws SQLException {
		String query = "INSERT into `meeting` (`title`, `date`, `time`, `duration`, `maxParticipant`) VALUES (?, ?, ?, ?, ?)";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setString(1, meeting.getTitle());
			pstatement.setObject(2, meeting.getDate().toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDate());
			pstatement.setTime(3, meeting.getTime());//non sono sicuro che funzioni
			pstatement.setInt(4, meeting.getDuration());
			pstatement.setInt(5, meeting.getMaxParticipant());
			
			pstatement.executeUpdate();
		}
	}

	

	/**
	 * This method will be invoked as many times as the number of user that participate to a mission; it will
	 * insert them into the database, distinguishing between a creator and a guest
	 * @param idUser id of the user that participate to a meeeting
	 * @param creator true if the user inserted is the creator of this meeting, false if it is a guest
	 * @throws SQLException
	 */
	public void createParticipant(int idUser, boolean creator) throws SQLException {
		String query = "INSERT into `participation` (`idUser`, `idMeeting`, `isCreator`) VALUES (?, ?, ?)";
		int idMeeting;
		// since "idMeeting" in "meeting" is auto incremented and the meeting is inserted  before invoking this method, just find the
		// maximum value of the key "idMeeting" from the table "meeting"
		try {
			idMeeting = findLatestIdMeeting();
			if(idMeeting == -1)
				throw new SQLException("Latest idMeeting does not exists");
		}catch(SQLException e) {
			throw new SQLException(e.getMessage());
		}
		
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, idUser);
			pstatement.setInt(2, idMeeting);
			pstatement.setBoolean(3, creator);

			pstatement.executeUpdate();
		}
	}
	
	/**
	 * This method is invoked when we have to find the latest idMeeting inserted in table "meeting", since it is auto incremented 
	 * @return latest id inserted in the table "meeting"
	 * @throws SQLException
	 */
	private int findLatestIdMeeting() throws SQLException{ 	//this method will be invoked every time after create meeting
		String query = "SELECT idMeeting FROM  meeting WHERE idMeeting = (SELECT MAX(idMeeting) FROM meeting)";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			try(ResultSet result = pstatement.executeQuery();){
				if(!result.isBeforeFirst()) //error when created meeting
					return -1;
				else {
					result.next();//only one id
					int maxId = result.getInt("idMeeting");
					return maxId;
				}
			}
		}
	}
	
}
