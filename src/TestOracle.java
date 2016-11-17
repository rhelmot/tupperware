import java.sql.*;

public class TestOracle {

	public static void main(String[] args){
		try{
			String url = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
			String username = "dutcher";
			String password = "096";
			Connection con = DriverManager.getConnection(url,username, password);
			Statement st = con.createStatement();
			String sql = "select aid, aname from rmr.Agents";
			ResultSet rs = st.executeQuery(sql);
			while(rs.next())
				System.out.println(rs.getInt(1)+" "+rs.getString(2));
			con.close();
		}
		catch(Exception e) {
            System.out.println(e);
        }
	}
}



