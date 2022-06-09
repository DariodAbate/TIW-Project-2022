package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		String query = "SELECT title, date, time, duration, maxParticipant, isCreator FROM participation NATURAL JOIN meeting WHERE idUser = ?";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, idUser);
			try(ResultSet result = pstatement.executeQuery();){
				ArrayList<Meeting> temp = new ArrayList<>();
				while(result.next()) {
					Meeting m = new Meeting();
					

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
	 * @return the auto-generated key associated to the meeting created
	 * @throws SQLException
	 */
	private int createMeeting(Meeting meeting) throws SQLException {
		String query = "INSERT into `meeting` (`title`, `date`, `time`, `duration`, `maxParticipant`) VALUES (?, ?, ?, ?, ?)";
		try(PreparedStatement pstatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){
			pstatement.setString(1, meeting.getTitle());
			pstatement.setObject(2, meeting.getDate().toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDate());
			pstatement.setTime(3, meeting.getTime());
			pstatement.setInt(4, meeting.getDuration());
			pstatement.setInt(5, meeting.getMaxParticipant());
			
			pstatement.executeUpdate();
			
			//get the auto generated key
			ResultSet generatedKeys = pstatement.getGeneratedKeys();
			if(generatedKeys.next()) {
				return generatedKeys.getInt(1);
			}else {
				throw new SQLException("Creating meeting failes, no ID obtained");
			}
		}
	}

	/**
	 * This method will be invoked as many times as the number of user that participate to a mission; it will
	 * insert them into the database, distinguishing between a creator and a guest
	 * @param idUser id of the user that participate to a meeeting
	 * @param idMeeting id of the meeting in which the user partecipates
	 * @param creator true if the user inserted is the creator of this meeting, false if it is a guest
	 * @throws SQLException
	 */
	private void createParticipant(int idUser, int idMeeting, boolean creator) throws SQLException {
		String query = "INSERT into `participation` (`idUser`, `idMeeting`, `isCreator`) VALUES (?, ?, ?)";
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setInt(1, idUser);
			pstatement.setInt(2, idMeeting);
			pstatement.setBoolean(3, creator);

			pstatement.executeUpdate();
		}
	}
	
	/**
	 * This method is used to create a new meeting and to invite the the selected users to it.
	 * If one of the interaction with the database fails, roll back all the works
	 * @param meeting meeting that will be created
	 * @param idCreator id of the user that created the meeting
	 * @param idUsersInvited list of id of the users that has been invited
	 * @throws SQLException
	 */
	public void createMeetingWithParticipants(Meeting meeting, int idCreator, ArrayList<Integer> idUsersInvited) throws SQLException{
		con.setAutoCommit(false);//delimit of transaction
		try {
			int generatedIdMeeting = createMeeting(meeting);
			//saving creator of the meeting
			createParticipant(idCreator, generatedIdMeeting, true);
			//saving participant of the meeting
			for(int idUser: idUsersInvited) 	
				createParticipant(idUser, generatedIdMeeting, false);
			con.commit();
			
		}catch(SQLException e) {//if one of the updates fails, roll back all the work
			con.rollback();
			throw e;
		}
	}
		
}
