import java.sql.*;
import java.lang.ThreadLocal;
import java.util.ArrayList;

public class Database {
    private static ThreadLocal<Database> inst = new ThreadLocal<Database>() {
        protected synchronized Database initialValue() {
            return new Database();
        }
    };
    private Connection con;

    public Database() {
        String url = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
        String username = "dutcher";
        String password = "096";
        try {
            this.con = DriverManager.getConnection(url, username, password);

            this.getUserStmt = con.prepareStatement(Database.getUserSql);
            this.insertUserStmt = con.prepareStatement(Database.insertUserSql, new String[]{"hid"});
            this.updateUserStmt = con.prepareStatement(Database.updateUserSql);
            this.getFriendsStmt = con.prepareStatement(Database.getFriendsSql);
            this.makeFriendsStmt = con.prepareStatement(Database.makeFriendsSql);
            this.areFriendsStmt = con.prepareStatement(Database.areFriendsSql);
            this.requestFriendsStmt = con.prepareStatement(Database.requestFriendsSql);
            this.unrequestFriendsStmt = con.prepareStatement(Database.unrequestFriendsSql);
            this.insertSessionStmt = con.prepareStatement(Database.insertSessionSql, new String[]{"sid"});
            this.updateSessionStmt = con.prepareStatement(Database.updateSessionSql);
            this.getSessionStmt = con.prepareStatement(Database.getSessionSql);
            this.insertChatGroupStmt = con.prepareStatement(Database.insertChatGroupSql, new String[]{"gid"});
            this.updateChatGroupStmt = con.prepareStatement(Database.updateChatGroupSql);
            this.getChatGroupStmt = con.prepareStatement(Database.getChatGroupSql);
            this.insertChatGroupMembershipStmt = con.prepareStatement(Database.insertChatGroupMembershipSql);
            this.updateChatGroupMembershipStmt = con.prepareStatement(Database.updateChatGroupMembershipSql);
            this.getChatGroupMembersStmt = con.prepareStatement(Database.getChatGroupMembersSql);
            this.getChatGroupPendingMembersStmt = con.prepareStatement(Database.getChatGroupPendingMembersSql);
            this.getChatGroupsForUserStmt = con.prepareStatement(Database.getChatGroupsForUserSql);
            this.getChatGroupsPendingForUserStmt = con.prepareStatement(Database.getChatGroupsPendingForUserSql);
        } catch (SQLException e) {
            System.err.println("FATAL: Failed to set up database connection!");
            System.err.println(e);
            System.exit(1);
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.con.close();
        } finally {
            super.finalize();
        }
    }

    public static Database i() {
        return inst.get();
    }

    private static String getUserSql = "SELECT email, name, phone, passwordHash, screenname, isManager FROM Users WHERE hid=?";
    private PreparedStatement getUserStmt;
    public UsersEntity getUser(int hid) {
        try {
            this.getUserStmt.setInt(1, hid);
            this.getUserStmt.execute();
            ResultSet rs = this.getUserStmt.getResultSet();
            if (rs.next()) {
                return new UsersEntity(hid, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6) != 0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private static String getUserByEmailSql = "SELECT hid, name, phone, passwordHash, screenname, isManager FROM Users WHERE email=?";
    private PreparedStatement getUserByEmailStmt;
    public UsersEntity getUserByEmail(String email) {
        try {
            this.getUserStmt.setString(1, email);
            this.getUserStmt.execute();
            ResultSet rs = this.getUserStmt.getResultSet();
            if (rs.next()) {
                return new UsersEntity(rs.getInt(1), email, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6) != 0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private static String insertUserSql = "INSERT INTO Users (hid, email, name, phone, passwordHash, screenname, isManager) VALUES (SeqHid.NEXTVAL, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement insertUserStmt;
    public Integer insertUser(String email, String name, String phone, String passwordHash, String screenname, boolean isManager) {
        try {
            int isManagerInt = 0;
            if (isManager) isManagerInt = 1;

            this.insertUserStmt.setString(1, email);
            this.insertUserStmt.setString(2, name);
            this.insertUserStmt.setString(3, phone);
            this.insertUserStmt.setString(4, passwordHash);
            this.insertUserStmt.setString(5, screenname);
            this.insertUserStmt.setInt(6, isManagerInt);
            this.insertUserStmt.executeUpdate();
            ResultSet rs = this.insertUserStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    private static String updateUserSql = "UPDATE Users SET email=?, name=?, phone=?, passwordHash=?, screenname=?, isManager=? WHERE hid=?";
    private PreparedStatement updateUserStmt;
    public boolean updateUser(int hid, String email, String name, String phone, String passwordHash, String screenname, boolean isManager) {
        try {
            int isManagerInt = 0;
            if (isManager) isManagerInt = 1;

            this.updateUserStmt.setString(1, email);
            this.updateUserStmt.setString(2, name);
            this.updateUserStmt.setString(3, phone);
            this.updateUserStmt.setString(4, passwordHash);
            this.updateUserStmt.setString(5, screenname);
            this.updateUserStmt.setInt(6, isManagerInt);
            this.updateUserStmt.setInt(7, hid);
            this.updateUserStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getFriendsSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users join Friendships on Friendships.left = Users.hid WHERE Friendships.right=?";
    private PreparedStatement getFriendsStmt;
    public ArrayList<UsersEntity> getFriends(int hid) {
        try {
            this.getFriendsStmt.setInt(1, hid);
            this.getFriendsStmt.execute();
            ResultSet rs = this.getFriendsStmt.getResultSet();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            return null;
        }
    }

    private static String makeFriendsSql = "INSERT INTO Friendships (left, right, since) VALUES (?, ?, (SELECT currentTime FROM Settings)), (?, ?, (SELECT currentTime FROM Settings))";
    private PreparedStatement makeFriendsStmt;
    public boolean makeFriends(int hid1, int hid2) {
        try {
            this.makeFriendsStmt.setInt(1, hid1);
            this.makeFriendsStmt.setInt(2, hid2);
            this.makeFriendsStmt.setInt(3, hid2);
            this.makeFriendsStmt.setInt(4, hid1);
            this.makeFriendsStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String areFriendsSql = "SELECT 1 FROM Friendships WHERE left=? AND right=?";
    private PreparedStatement areFriendsStmt;
    public boolean areFriends(int hid1, int hid2) {
        try {
            this.makeFriendsStmt.setInt(1, hid1);
            this.makeFriendsStmt.setInt(2, hid2);
            this.makeFriendsStmt.execute();
            ResultSet rs = this.makeFriendsStmt.getResultSet();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static String requestFriendsSql = "INSERT INTO FriendRequests (requester, requestee) VALUES (?, ?)";
    private PreparedStatement requestFriendsStmt;
    public boolean requestFriends(int requester, int requestee) {
        try {
            this.makeFriendsStmt.setInt(1, requester);
            this.makeFriendsStmt.setInt(2, requestee);
            this.makeFriendsStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String unrequestFriendsSql = "DELETE FROM FriendRequests WHERE requester=? AND requestee=?";
    private PreparedStatement unrequestFriendsStmt;
    public boolean unrequestFriends(int requester, int requestee) {
        try {
            this.makeFriendsStmt.setInt(1, requester);
            this.makeFriendsStmt.setInt(2, requestee);
            this.makeFriendsStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String insertSessionSql = "INSERT INTO Sessions (sid, token, hid, isManaging) VALUES (SeqSid.NEXTVAL, ?, ?, ?)";
    private PreparedStatement insertSessionStmt;
    public Integer insertSession(String token, int hid, boolean isManaging) {
        try {
            int isManagingInt = 0;
            if (isManaging) isManagingInt = 1;

            this.insertSessionStmt.setString(1, token);
            this.insertSessionStmt.setInt(2, hid);
            this.insertSessionStmt.setInt(3, isManagingInt);
            this.insertSessionStmt.executeUpdate();
            ResultSet rs = this.insertSessionStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    private static String updateSessionSql = "UPDATE Sessions SET token=?, hid=?, isManaging=? WHERE sid=?";
    private PreparedStatement updateSessionStmt;
    public boolean updateSession(int sid, String token, int hid, boolean isManaging) {
        try {
            int isManagingInt = 0;
            if (isManaging) isManagingInt = 1;

            this.updateSessionStmt.setString(1, token);
            this.updateSessionStmt.setInt(2, hid);
            this.updateSessionStmt.setInt(3, isManagingInt);
            this.updateSessionStmt.setInt(4, sid);
            this.updateSessionStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getSessionSql = "SELECT sid, hid, isManaging FROM Sessions WHERE token=?";
    private PreparedStatement getSessionStmt;
    public SessionsEntity getSession(String token) {
        try {
            this.getSessionStmt.setString(1, token);
            this.getSessionStmt.execute();
            ResultSet rs = this.getSessionStmt.getResultSet();
            if (rs.next()) {
                return new SessionsEntity(rs.getInt(1), token, rs.getInt(2), rs.getInt(3) != 0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private static String insertChatGroupSql = "INSERT INTO ChatGroups (gid, groupName, duration) VALUES (SeqGid.NEXTVAL, ?, ?)";
    private PreparedStatement insertChatGroupStmt;
    public Integer insertChatGroup(String groupName, int duration) {
        try {
            this.insertChatGroupStmt.setString(1, groupName);
            this.insertChatGroupStmt.setInt(2, duration);
            this.insertChatGroupStmt.executeUpdate();
            ResultSet rs = this.insertChatGroupStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    private static String updateChatGroupSql = "UPDATE ChatGroups SET groupName=?, duration=? WHERE gid=?";
    private PreparedStatement updateChatGroupStmt;
    public boolean updateChatGroup(int gid, String groupName, int duration) {
        try {
            this.updateChatGroupStmt.setString(1, groupName);
            this.updateChatGroupStmt.setInt(2, duration);
            this.updateChatGroupStmt.setInt(3, gid);
            this.updateChatGroupStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getChatGroupSql = "SELECT groupName, duration FROM ChatGroups WHERE gid=?";
    private PreparedStatement getChatGroupStmt;
    public ChatGroupsEntity getChatGroup(int gid) {
        try {
            this.getChatGroupStmt.setInt(1, gid);
            this.getChatGroupStmt.execute();
            ResultSet rs = this.getChatGroupStmt.getResultSet();
            if (rs.next()) {
                return new ChatGroupsEntity(gid, rs.getString(1), rs.getInt(2));
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private static String insertChatGroupMembershipSql = "INSERT INTO ChatGroupMemberships (gid, hid, isOwner, invitationAccepted) VALUES (?, ?, ?, ?)";
    private PreparedStatement insertChatGroupMembershipStmt;
    public boolean insertChatGroupMembership(int gid, int hid, boolean isOwner, boolean invitationAccepted) {
        try {
            int isOwnerInt = 0;
            if (isOwner) isOwnerInt = 1;

            int invitationAcceptedInt = 0;
            if (invitationAccepted) invitationAcceptedInt = 1;

            this.insertChatGroupMembershipStmt.setInt(1, gid);
            this.insertChatGroupMembershipStmt.setInt(2, hid);
            this.insertChatGroupMembershipStmt.setInt(3, isOwnerInt);
            this.insertChatGroupMembershipStmt.setInt(4, invitationAcceptedInt);
            this.insertChatGroupMembershipStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    private static String updateChatGroupMembershipSql = "UPDATE ChatGroupMemberships SET isOwner=?, invitationAccepted=? WHERE gid=? and hid=?";
    private PreparedStatement updateChatGroupMembershipStmt;
    public boolean updateChatGroupMembership(int gid, int hid, boolean isOwner, boolean invitationAccepted) {
        try {
            int isOwnerInt = 0;
            if (isOwner) isOwnerInt = 1;

            int invitationAcceptedInt = 0;
            if (invitationAccepted) invitationAcceptedInt = 1;

            this.updateChatGroupMembershipStmt.setInt(1, isOwnerInt);
            this.updateChatGroupMembershipStmt.setInt(2, invitationAcceptedInt);
            this.updateChatGroupMembershipStmt.setInt(3, gid);
            this.updateChatGroupMembershipStmt.setInt(4, hid);
            this.updateChatGroupMembershipStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getChatGroupMembersSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid = ChatGroupMemberships.hid WHERE ChatGroupMemberships.gid=? AND invitationAccepted != 0";
    private PreparedStatement getChatGroupMembersStmt;
    public ArrayList<UsersEntity> getChatGroupMembers(int gid) {
        try {
            this.getChatGroupMembersStmt.setInt(1, gid);
            this.getChatGroupMembersStmt.execute();
            ResultSet rs = this.getChatGroupMembersStmt.getResultSet();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            return null;
        }
    }

    private static String getChatGroupPendingMembersSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid = ChatGroupMemberships.hid WHERE ChatGroupMemberships.gid=? AND invitationAccepted == 0";
    private PreparedStatement getChatGroupPendingMembersStmt;
    public ArrayList<UsersEntity> getChatGroupPendingMembers(int gid) {
        try {
            this.getChatGroupMembersStmt.setInt(1, gid);
            this.getChatGroupMembersStmt.execute();
            ResultSet rs = this.getChatGroupMembersStmt.getResultSet();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            return null;
        }
    }

    private static String getChatGroupsForUserSql = "SELECT gid, groupName, duration FROM ChatGroups JOIN ChatGroupMemberships ON ChatGroups.gid = ChatGroupMemberships.gid WHERE ChatGroupMemberships.hid=? AND invitationAccepted != 0";
    private PreparedStatement getChatGroupsForUserStmt;
    public ArrayList<ChatGroupsEntity> getChatGroupsForUser(int hid) {
        try {
            this.getChatGroupsForUserStmt.setInt(1, hid);
            this.getChatGroupsForUserStmt.execute();
            ResultSet rs = this.getChatGroupsForUserStmt.getResultSet();
            ArrayList<ChatGroupsEntity> out = new ArrayList<ChatGroupsEntity>();
            while (rs.next()) {
                out.add(new ChatGroupsEntity(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
            return out;
        } catch (SQLException e) {
            return null;
        }
    }

    private static String getChatGroupsPendingForUserSql = "SELECT gid, groupName, duration FROM ChatGroups JOIN ChatGroupMemberships ON ChatGroups.gid = ChatGroupMemberships.gid WHERE ChatGroupMemberships.hid=? AND invitationAccepted == 0";
    private PreparedStatement getChatGroupsPendingForUserStmt;
    public ArrayList<ChatGroupsEntity> getChatGroupsPendingForUser(int hid) {
        try {
            this.getChatGroupsPendingForUserStmt.setInt(1, hid);
            this.getChatGroupsPendingForUserStmt.execute();
            ResultSet rs = this.getChatGroupsPendingForUserStmt.getResultSet();
            ArrayList<ChatGroupsEntity> out = new ArrayList<ChatGroupsEntity>();
            while (rs.next()) {
                out.add(new ChatGroupsEntity(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
            return out;
        } catch (SQLException e) {
            return null;
        }
    }
}
