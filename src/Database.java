import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.lang.ThreadLocal;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Database {
    private static ThreadLocal<Database> inst = new ThreadLocal<Database>() {
        protected synchronized Database initialValue() {
            return new Database();
        }
    };
    private Connection con;

    private static ArrayList<Connection> conList = new ArrayList<Connection>();

    public Database() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql:tupperware";
            String username = "tupperware";
            String password = "";

            this.con = DriverManager.getConnection(url, username, password);
            synchronized (conList) {
                conList.add(this.con);
            }

            this.getTimeStmt = con.prepareStatement(Database.getTimeSql);
            this.setTimeStaticStmt = con.prepareStatement(Database.setTimeStaticSql);
            this.setTimeDynamicStmt = con.prepareStatement(Database.setTimeDynamicSql);
            this.isTimeMovingStmt = con.prepareStatement(Database.isTimeMovingSql);

            this.getUserStmt = con.prepareStatement(Database.getUserSql);
            this.getUserByEmailStmt = con.prepareStatement(Database.getUserByEmailSql);
            this.insertUserStmt = con.prepareStatement(Database.insertUserSql, new String[]{"hid"});
            this.updateUserStmt = con.prepareStatement(Database.updateUserSql);
            this.getFriendsStmt = con.prepareStatement(Database.getFriendsSql);
            this.getPendingRequestsStmt = con.prepareStatement(Database.getPendingRequestsSql);
            this.getActiveRequestsStmt = con.prepareStatement(Database.getActiveRequestsSql);
            this.makeFriendsStmt = con.prepareStatement(Database.makeFriendsSql);
            this.areFriendsStmt = con.prepareStatement(Database.areFriendsSql);
            this.requestFriendsStmt = con.prepareStatement(Database.requestFriendsSql);
            this.unrequestFriendsStmt = con.prepareStatement(Database.unrequestFriendsSql);
            this.unfriendStmt = con.prepareStatement(Database.unfriendSql);

            this.insertSessionStmt = con.prepareStatement(Database.insertSessionSql, new String[]{"sid"});
            this.updateSessionStmt = con.prepareStatement(Database.updateSessionSql);
            this.getSessionStmt = con.prepareStatement(Database.getSessionSql);
            this.deleteSessionStmt = con.prepareStatement(Database.deleteSessionSql);

            this.insertChatGroupStmt = con.prepareStatement(Database.insertChatGroupSql, new String[]{"gid"});
            this.updateChatGroupStmt = con.prepareStatement(Database.updateChatGroupSql);
            this.getChatGroupStmt = con.prepareStatement(Database.getChatGroupSql);
            this.insertChatGroupMembershipStmt = con.prepareStatement(Database.insertChatGroupMembershipSql);
            this.bootstrapNewGroupMemberStmt = con.prepareStatement(Database.bootstrapNewGroupMemberSql);
            this.updateChatGroupMembershipStmt = con.prepareStatement(Database.updateChatGroupMembershipSql);
            this.getChatGroupMembersStmt = con.prepareStatement(Database.getChatGroupMembersSql);
            this.getChatGroupPendingMembersStmt = con.prepareStatement(Database.getChatGroupPendingMembersSql);
            this.getChatGroupsForUserStmt = con.prepareStatement(Database.getChatGroupsForUserSql);
            this.getChatGroupsPendingForUserStmt = con.prepareStatement(Database.getChatGroupsPendingForUserSql);
            this.getChatGroupOwnerStmt = con.prepareStatement(Database.getChatGroupOwnerSql);

            this.insertChatStmt = con.prepareStatement(Database.insertChatSql, new String[]{"cid"});
            this.insertChatForGroupStmt = con.prepareStatement(Database.insertChatForGroupSql);
            this.deleteChatSenderStmt = con.prepareStatement(Database.deleteChatSenderSql);
            this.deleteChatReceiverStmt = con.prepareStatement(Database.deleteChatReceiverSql);
            this.getChatsStmt = con.prepareStatement(Database.getChatsSql);
            this.getChatsForGroupStmt = con.prepareStatement(Database.getChatsForGroupSql);

            this.insertPostStmt = con.prepareStatement(Database.insertPostSql, new String[]{"pid"});
            this.updatePostStmt = con.prepareStatement(Database.updatePostSql);
            this.makePostVisibleStmt = con.prepareStatement(Database.makePostVisibleSql);
            this.getPostStmt = con.prepareStatement(Database.getPostSql);
            this.deletePostStmt = con.prepareStatement(Database.deletePostSql);
            this.deletePostVisibilitiesStmt = con.prepareStatement(Database.deletePostVisibilitiesSql);

            this.getPostsByCircleStmt = con.prepareStatement(Database.getPostsByCircleSql);
            this.getPostsByUserStmt = con.prepareStatement(Database.getPostsByUserSql);
            this.getPostsByTagStmt = con.prepareStatement(Database.getPostsByTagSql);
            this.getUsersByTagStmt = con.prepareStatement(Database.getUsersByTagSql);
            this.addPostTagStmt = con.prepareStatement(Database.addPostTagSql);
            this.addUserTagStmt = con.prepareStatement(Database.addUserTagSql);
            this.deletePostTagStmt = con.prepareStatement(Database.deletePostTagSql);
            this.deleteUserTagStmt = con.prepareStatement(Database.deleteUserTagSql);
            this.getPostTagsStmt = con.prepareStatement(Database.getPostTagsSql);
            this.getUserTagsStmt = con.prepareStatement(Database.getUserTagsSql);

            this.getPrivateMessageThreadsStmt = con.prepareStatement(Database.getPrivateMessageThreadsSql);

            this.getActiveUsersStmt = con.prepareStatement(Database.getActiveUsersSql);
            this.countInactiveUsersStmt = con.prepareStatement(Database.countInactiveUsersSql);
            this.getTopPostsStmt = con.prepareStatement(Database.getTopPostsSql);
            this.getMessageCountStmt = con.prepareStatement(Database.getMessageCountSql);
            this.getReadCountStmt = con.prepareStatement(Database.getReadCountSql);
            this.getReadCountForNewMessagesStmt = con.prepareStatement(Database.getReadCountForNewMessagesSql);

            this.getTopTagDataStmt = con.prepareStatement(Database.getTopTagDataSql);
            this.insertReportStmt = con.prepareStatement(Database.insertReportSql, new String[]{"tid"});
            this.insertReportTagDataStmt = con.prepareStatement(Database.insertReportTagDataSql);
            this.getReportsStmt = con.prepareStatement(Database.getReportsSql);
            this.getReportsTagDataStmt = con.prepareStatement(Database.getReportsTagDataSql);

            this.collectGarbage1Stmt = con.prepareStatement(Database.collectGarbage1Sql);

            this.logPostReadStmt = con.prepareStatement(Database.logPostReadSql);
            //this.XXXStmt = con.prepareStatement(Database.XXXSql);
        } catch (Exception e) {
            System.err.println("FATAL: Failed to set up database connection!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Database i() {
        return inst.get();
    }

    private static String getTimeSql = "SELECT getTime()";
    private PreparedStatement getTimeStmt;
    public Date getTime() {
        try {
            ResultSet rs = this.getTimeStmt.executeQuery();
            rs.next();
            return new Date(rs.getTimestamp(1).getTime());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String isTimeMovingSql = "SELECT clockActive from Settings";
    private PreparedStatement isTimeMovingStmt;
    public boolean isTimeMoving() {
        try {
            ResultSet rs = isTimeMovingStmt.executeQuery();
            rs.next();
            return rs.getInt(1) != 0;
        } catch (SQLException e) {
            // ???
            e.printStackTrace();
            return false;
        }
    }

    private static String setTimeStaticSql = "UPDATE Settings SET currentTime=?, clockActive=0";
    private PreparedStatement setTimeStaticStmt;
    public boolean setTimeStatic(Date when) {
        try {
            this.setTimeStaticStmt.setTimestamp(1, new Timestamp(when.getTime()));
            this.setTimeStaticStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String setTimeDynamicSql = "UPDATE Settings SET currentInterval=(? - SYSTIMESTAMP), clockActive=1";
    private PreparedStatement setTimeDynamicStmt;
    public boolean setTimeDynamic(Date when) {
        try {
            this.setTimeDynamicStmt.setTimestamp(1, new Timestamp(when.getTime()));
            this.setTimeDynamicStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setTime(String when, boolean dynamic) throws ParseException {
        //SimpleDateFormat parser = new SimpleDateFormat("MMMMM d yyyy h:mm:ss a");
        SimpleDateFormat parser = new SimpleDateFormat("M.d.yyyy, h:mm a");
        if (dynamic) {
            return Database.i().setTimeDynamic(parser.parse(when));
        } else {
            return Database.i().setTimeStatic(parser.parse(when));
        }
    }

    private static String getUserSql = "SELECT email, name, phone, passwordHash, screenname, isManager FROM Users WHERE hid=?";
    private PreparedStatement getUserStmt;
    public UsersEntity getUser(int hid) {
        try {
            this.getUserStmt.setInt(1, hid);
            ResultSet rs = this.getUserStmt.executeQuery();
            if (rs.next()) {
                return new UsersEntity(hid, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6) != 0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getUserByEmailSql = "SELECT hid, name, phone, passwordHash, screenname, isManager FROM Users WHERE email=?";
    private PreparedStatement getUserByEmailStmt;
    public UsersEntity getUserByEmail(String email) {
        try {
            this.getUserByEmailStmt.setString(1, email);
            ResultSet rs = this.getUserByEmailStmt.executeQuery();
            if (rs.next()) {
                return new UsersEntity(rs.getInt(1), email, rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6) != 0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String insertUserSql = "INSERT INTO Users (hid, email, name, phone, passwordHash, screenname, isManager) VALUES (NEXTVAL('SeqHid'), ?, ?, ?, ?, ?, ?)";
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    private static String getFriendsSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users join Friendships on Friendships.up = Users.hid WHERE Friendships.down=?";
    private PreparedStatement getFriendsStmt;
    public ArrayList<UsersEntity> getFriends(int hid) {
        try {
            this.getFriendsStmt.setInt(1, hid);
            ResultSet rs = this.getFriendsStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPendingRequestsSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users join FriendRequests on FriendRequests.requestee = Users.hid WHERE FriendRequests.requester=?";
    private PreparedStatement getPendingRequestsStmt;
    public ArrayList<UsersEntity> getPendingRequests(int hid) {
        try {
            this.getPendingRequestsStmt.setInt(1, hid);
            ResultSet rs = this.getPendingRequestsStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getActiveRequestsSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users join FriendRequests on FriendRequests.requester = Users.hid WHERE FriendRequests.requestee=?";
    private PreparedStatement getActiveRequestsStmt;
    public ArrayList<UsersEntity> getActiveRequests(int hid) {
        try {
            this.getActiveRequestsStmt.setInt(1, hid);
            ResultSet rs = this.getActiveRequestsStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String makeFriendsSql = "INSERT INTO Friendships (up, down, since) VALUES (?, ?, getTime()), (?, ?, getTime())";
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
            e.printStackTrace();
            return false;
        }
    }

    private static String areFriendsSql = "SELECT 1 FROM Friendships WHERE up=? AND down=?";
    private PreparedStatement areFriendsStmt;
    public boolean areFriends(int hid1, int hid2) {
        try {
            this.areFriendsStmt.setInt(1, hid1);
            this.areFriendsStmt.setInt(2, hid2);
            ResultSet rs = this.areFriendsStmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String requestFriendsSql = "INSERT INTO FriendRequests (requester, requestee) VALUES (?, ?)";
    private PreparedStatement requestFriendsStmt;
    public boolean requestFriends(int requester, int requestee) {
        try {
            this.requestFriendsStmt.setInt(1, requester);
            this.requestFriendsStmt.setInt(2, requestee);
            this.requestFriendsStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String unrequestFriendsSql = "DELETE FROM FriendRequests WHERE requester=? AND requestee=?";
    private PreparedStatement unrequestFriendsStmt;
    public boolean unrequestFriends(int requester, int requestee) {
        try {
            this.unrequestFriendsStmt.setInt(1, requester);
            this.unrequestFriendsStmt.setInt(2, requestee);
            this.unrequestFriendsStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String unfriendSql = "DELETE FROM Friendships WHERE (up=? AND down=?) OR (up=? AND down=?)";
    private PreparedStatement unfriendStmt;
    public boolean unfriend(int a, int b) {
        try {
            this.unfriendStmt.setInt(1, a);
            this.unfriendStmt.setInt(2, b);
            this.unfriendStmt.setInt(3, b);
            this.unfriendStmt.setInt(4, a);
            this.unfriendStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String insertSessionSql = "INSERT INTO Sessions (sid, token, hid, isManaging) VALUES (NEXTVAL('SeqSid'), ?, ?, ?)";
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    private static String getSessionSql = "SELECT sid, hid, isManaging FROM Sessions WHERE token=?";
    private PreparedStatement getSessionStmt;
    public SessionsEntity getSession(String token) {
        try {
            this.getSessionStmt.setString(1, token);
            ResultSet rs = this.getSessionStmt.executeQuery();
            if (rs.next()) {
                return new SessionsEntity(rs.getInt(1), token, rs.getInt(2), rs.getInt(3) != 0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String deleteSessionSql = "DELETE FROM Sessions where sid=?";
    private PreparedStatement deleteSessionStmt;
    public boolean deleteSession(int sid) {
        try {
            this.deleteSessionStmt.setInt(1, sid);
            this.deleteSessionStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String insertChatGroupSql = "INSERT INTO ChatGroups (gid, groupName, duration) VALUES (NEXTVAL('SeqGid'), ?, ?)";
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    private static String getChatGroupSql = "SELECT groupName, duration FROM ChatGroups WHERE gid=?";
    private PreparedStatement getChatGroupStmt;
    public ChatGroupsEntity getChatGroup(int gid) {
        try {
            this.getChatGroupStmt.setInt(1, gid);
            ResultSet rs = this.getChatGroupStmt.executeQuery();
            if (rs.next()) {
                return new ChatGroupsEntity(gid, rs.getString(1), rs.getInt(2));
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String insertChatGroupMembershipSql = "INSERT INTO ChatGroupMemberships (gid, hid, isOwner, invitationAccepted) VALUES (?, ?, ?, ?)";
    private PreparedStatement insertChatGroupMembershipStmt;
    private static String bootstrapNewGroupMemberSql = "INSERT INTO Chats (cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver) SELECT NEXTVAL('SeqCid'), author, text, timestamp, ?, gid, 1, 0 FROM Chats WHERE gid=? AND hid=?";
    private PreparedStatement bootstrapNewGroupMemberStmt;
    public boolean insertChatGroupMembership(int gid, int hid, int inviter, boolean isOwner, boolean invitationAccepted) {
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

            this.bootstrapNewGroupMemberStmt.setInt(1, hid);
            this.bootstrapNewGroupMemberStmt.setInt(2, gid);
            this.bootstrapNewGroupMemberStmt.setInt(3, inviter);
            this.bootstrapNewGroupMemberStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    private static String getChatGroupMembersSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid = ChatGroupMemberships.hid WHERE ChatGroupMemberships.gid=? AND invitationAccepted != 0";
    private PreparedStatement getChatGroupMembersStmt;
    public ArrayList<UsersEntity> getChatGroupMembers(int gid) {
        try {
            this.getChatGroupMembersStmt.setInt(1, gid);
            ResultSet rs = this.getChatGroupMembersStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getChatGroupPendingMembersSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid = ChatGroupMemberships.hid WHERE ChatGroupMemberships.gid=? AND invitationAccepted=0";
    private PreparedStatement getChatGroupPendingMembersStmt;
    public ArrayList<UsersEntity> getChatGroupPendingMembers(int gid) {
        try {
            this.getChatGroupMembersStmt.setInt(1, gid);
            ResultSet rs = this.getChatGroupMembersStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getChatGroupsForUserSql = "SELECT ChatGroups.gid, groupName, duration FROM ChatGroups JOIN ChatGroupMemberships ON ChatGroups.gid = ChatGroupMemberships.gid WHERE ChatGroupMemberships.hid=? AND invitationAccepted != 0";
    private PreparedStatement getChatGroupsForUserStmt;
    public ArrayList<ChatGroupsEntity> getChatGroupsForUser(int hid) {
        try {
            this.getChatGroupsForUserStmt.setInt(1, hid);
            ResultSet rs = this.getChatGroupsForUserStmt.executeQuery();
            ArrayList<ChatGroupsEntity> out = new ArrayList<ChatGroupsEntity>();
            while (rs.next()) {
                out.add(new ChatGroupsEntity(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getChatGroupsPendingForUserSql = "SELECT ChatGroups.gid, groupName, duration FROM ChatGroups JOIN ChatGroupMemberships ON ChatGroups.gid = ChatGroupMemberships.gid WHERE ChatGroupMemberships.hid=? AND invitationAccepted=0";
    private PreparedStatement getChatGroupsPendingForUserStmt;
    public ArrayList<ChatGroupsEntity> getChatGroupsPendingForUser(int hid) {
        try {
            this.getChatGroupsPendingForUserStmt.setInt(1, hid);
            ResultSet rs = this.getChatGroupsPendingForUserStmt.executeQuery();
            ArrayList<ChatGroupsEntity> out = new ArrayList<ChatGroupsEntity>();
            while (rs.next()) {
                out.add(new ChatGroupsEntity(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getChatGroupOwnerSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid=ChatGroupMemberships.hid WHERE isOwner!=0 AND gid=?";
    private PreparedStatement getChatGroupOwnerStmt;
    public UsersEntity getChatGroupOwner(int gid) {
        try {
            getChatGroupOwnerStmt.setInt(1, gid);
            ResultSet rs = getChatGroupOwnerStmt.executeQuery();
            rs.next();
            return new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String insertChatSql = "INSERT INTO Chats (cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver) VALUES (NEXTVAL('SeqCid'), ?, ?, getTime(), ?, NULL, 0, 0)";
    private PreparedStatement insertChatStmt;
    public Integer insertChat(int author, String text, int hid) {
        try {
            this.insertChatStmt.setInt(1, author);
            this.insertChatStmt.setString(2, text);
            this.insertChatStmt.setInt(3, hid);
            this.insertChatStmt.executeUpdate();
            ResultSet rs = this.insertChatStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // FUCK these horrible project requirements
    // FUCK me for not thinking this through
    // but mostly FUCK sql and java for both being so verbose and bureaucratic and forcing me to jump through a billion hoops to change my mind about any sort of design decision halfway through
    private static String insertChatForGroupSql = "INSERT INTO Chats (cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver) SELECT NEXTVAL('SeqCid'), ?, ?, getTime(), hid, ?, 1, 0 FROM ChatGroupMemberships WHERE gid=? AND invitationAccepted!=0";
    private PreparedStatement insertChatForGroupStmt;
    public boolean insertChatForGroup(int author, String text, int gid) {
        try {
            this.insertChatForGroupStmt.setInt(1, author);
            this.insertChatForGroupStmt.setString(2, text);
            this.insertChatForGroupStmt.setInt(3, gid);
            this.insertChatForGroupStmt.setInt(4, gid);
            this.insertChatForGroupStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String deleteChatSenderSql = "UPDATE Chats SET deletedBySender=1 WHERE cid=?";
    private PreparedStatement deleteChatSenderStmt;
    public boolean deleteChatSender(int cid) {
        try {
            this.deleteChatSenderStmt.setInt(1, cid);
            this.deleteChatSenderStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String deleteChatReceiverSql = "UPDATE Chats SET deletedByReceiver=1 WHERE cid=?";
    private PreparedStatement deleteChatReceiverStmt;
    public boolean deleteChatReceiver(int cid) {
        try {
            this.deleteChatReceiverStmt.setInt(1, cid);
            this.deleteChatReceiverStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getChatsSql = "SELECT Chats.cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver FROM Chats WHERE ((author=? AND hid=? AND deletedBySender=0) OR (author=? AND hid=? AND deletedByReceiver=0)) AND gid IS NULL AND cid<? ORDER BY timestamp DESC, Chats.cid DESC";
    private PreparedStatement getChatsStmt;
    public ArrayList<ChatsEntity> getChats(int me, int other, int index, int count) {
        try {
            this.getChatsStmt.setInt(1, me);
            this.getChatsStmt.setInt(2, other);
            this.getChatsStmt.setInt(3, other);
            this.getChatsStmt.setInt(4, me);
            this.getChatsStmt.setInt(5, index);
            ResultSet rs = this.getChatsStmt.executeQuery();
            ArrayList<ChatsEntity> out = new ArrayList<ChatsEntity>();

            while (rs.next() && count > 0) {
                out.add(new ChatsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) == 0 ? null : rs.getInt(5), rs.getInt(6) == 0 ? null : rs.getInt(6), rs.getInt(7) != 0, rs.getInt(8) != 0));
                count--;
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getChatsForGroupSql = "SELECT Chats.cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver FROM Chats WHERE hid=? AND gid=? AND deletedByReceiver=0 AND cid<? ORDER BY timestamp DESC, Chats.cid DESC";
    private PreparedStatement getChatsForGroupStmt;
    public ArrayList<ChatsEntity> getChatsForGroup(int me, int group, int index, int count) {
        try {
            this.getChatsForGroupStmt.setInt(1, me);
            this.getChatsForGroupStmt.setInt(2, group);
            this.getChatsForGroupStmt.setInt(3, index);
            ResultSet rs = this.getChatsForGroupStmt.executeQuery();
            ArrayList<ChatsEntity> out = new ArrayList<ChatsEntity>();

            while (rs.next() && count > 0) {
                out.add(new ChatsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) == 0 ? null : rs.getInt(5), rs.getInt(6) == 0 ? null : rs.getInt(6), rs.getInt(7) != 0, rs.getInt(8) != 0));
                count--;
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String insertPostSql = "INSERT INTO Posts (pid, author, text, timestamp, isPublic) VALUES (NEXTVAL('SeqPid'), ?, ?, getTime(), ?)";
    private PreparedStatement insertPostStmt;
    public Integer insertPost(int author, String text, boolean isPublic) {
        try {
            int isPublicInt = 0;
            if (isPublic) isPublicInt = 1;

            this.insertPostStmt.setInt(1, author);
            this.insertPostStmt.setString(2, text);
            this.insertPostStmt.setInt(3, isPublicInt);
            this.insertPostStmt.executeUpdate();
            ResultSet rs = this.insertPostStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String updatePostSql = "UPDATE Posts author=?, text=?, isPublic=? where pid=?";
    private PreparedStatement updatePostStmt;
    public boolean updatePost(int pid, int author, String text, boolean isPublic) {
        try {
            int isPublicInt = 0;
            if (isPublic) isPublicInt = 1;

            this.updatePostStmt.setInt(1, author);
            this.updatePostStmt.setString(2, text);
            this.updatePostStmt.setInt(3, isPublicInt);
            this.updatePostStmt.setInt(4, pid);
            this.updatePostStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String makePostVisibleSql = "INSERT INTO PostVisibilities (pid, hid) VALUES (?, ?)";
    private PreparedStatement makePostVisibleStmt;
    public boolean makePostVisible(int pid, Integer hid) {
        try {
            this.makePostVisibleStmt.setInt(1, pid);
            if (hid == null) {
                this.makePostVisibleStmt.setNull(2, Types.INTEGER);
            } else {
                this.makePostVisibleStmt.setInt(2, hid);
            }
            this.makePostVisibleStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getPostSql = "SELECT Posts.pid, author, text, timestamp, isPublic FROM Posts WHERE pid=?";
    private PreparedStatement getPostStmt;
    public PostsEntity getPost(int pid) {
        if (pid == 0) return null;
        try {
            getPostStmt.setInt(1, pid);
            ResultSet rs = getPostStmt.executeQuery();
            rs.next();
            return new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String deletePostSql = "DELETE FROM Posts where pid=?";
    private PreparedStatement deletePostStmt;
    public boolean deletePost(int pid) {
        try {
            deletePostStmt.setInt(1, pid);
            deletePostStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String deletePostVisibilitiesSql = "DELETE FROM PostVisibilities where pid=?";
    private PreparedStatement deletePostVisibilitiesStmt;
    public boolean deletePostVisibilities(int pid) {
        try {
            deletePostVisibilitiesStmt.setInt(1, pid);
            deletePostVisibilitiesStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getPostsByCircleSql =
"SELECT Posts.pid, author, text, timestamp, isPublic                            " +
"    FROM Posts LEFT JOIN PostVisibilities ON PostVisibilities.pid=Posts.pid    " +
"    WHERE (                                                                    " +
"            (hid=? OR hid IS NULL) AND                                         " +
"            (isPublic=0 OR 0!=?) AND                                           " +
"            (author=? OR                                                       " +
"                EXISTS(SELECT up, down                                      " +
"                    FROM Friendships                                           " +
"                    WHERE up=author                                          " +
"                        AND down=?)                                           " +
"             )                                                                 " +
"        ) AND Posts.pid<?                                                      " +
"    ORDER BY timestamp DESC, Posts.pid DESC                                    ";
    private PreparedStatement getPostsByCircleStmt;
    public ArrayList<PostsEntity> getPostsByCircle(int hid, boolean includePublic, int index, int count) {
        try {
            this.getPostsByCircleStmt.setInt(1, hid);
            this.getPostsByCircleStmt.setInt(2, includePublic ? 1 : 0);
            this.getPostsByCircleStmt.setInt(3, hid);
            this.getPostsByCircleStmt.setInt(4, hid);
            this.getPostsByCircleStmt.setInt(5, index);
            ResultSet rs = this.getPostsByCircleStmt.executeQuery();

            ArrayList<PostsEntity> out = new ArrayList<PostsEntity>();
            while (rs.next() && count > 0) {
                markPostRead(rs.getInt(1));
                out.add(new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0));
                count--;
            }

            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPostsByUserSql =
"SELECT Posts.pid, author, text, timestamp, isPublic                            " +
"    FROM Posts LEFT JOIN PostVisibilities ON PostVisibilities.pid=Posts.pid    " +
"    WHERE (                                                                    " +
"            author=? AND (hid=? OR hid IS NULL) AND                            " +
"            (                                                                  " +
"                (isPublic=1 AND 0!=?) OR                                       " +
"                author=? OR                                                    " +
"                EXISTS(SELECT up, down                                      " +
"                    FROM Friendships                                           " +
"                    WHERE up=author                                          " +
"                        AND down=?)                                           " +
"             )                                                                 " +
"        ) AND Posts.pid<?                                                      " +
"    ORDER BY timestamp DESC, Posts.pid DESC                                    ";
    // Start with exactly one row per post
    // Conditions for viewing a post, given that it's either open-view or you have explicit permission:
    // 1) it's public and we want to see public posts, or
    // 2) you wrote it, or
    // 3) you're friends with the author
    private PreparedStatement getPostsByUserStmt;
    public ArrayList<PostsEntity> getPostsByUser(int author, int viewer, boolean includePublic, int index, int count) {
        try {
            this.getPostsByUserStmt.setInt(1, author);
            this.getPostsByUserStmt.setInt(2, viewer);
            this.getPostsByUserStmt.setInt(3, includePublic ? 1 : 0);
            this.getPostsByUserStmt.setInt(4, viewer);
            this.getPostsByUserStmt.setInt(5, viewer);
            this.getPostsByUserStmt.setInt(6, index);
            ResultSet rs = this.getPostsByUserStmt.executeQuery();

            ArrayList<PostsEntity> out = new ArrayList<PostsEntity>();
            while (rs.next() && count > 0) {
                markPostRead(rs.getInt(1));
                out.add(new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0));
                count--;
            }

            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPostsByTagSql = "SELECT Posts.pid, author, text, timestamp, isPublic FROM Posts JOIN PostTags ON PostTags.pid=Posts.pid WHERE tagText=? AND pid<? ORDER BY timestamp DESC, Posts.pid DESC";
    private PreparedStatement getPostsByTagStmt;
    public ArrayList<PostsEntity> getPostsByTag(String tagText, int index, int count) {
        try {
            this.getPostsByTagStmt.setString(1, tagText);
            this.getPostsByTagStmt.setInt(2, index);
            ResultSet rs = this.getPostsByTagStmt.executeQuery();

            ArrayList<PostsEntity> out = new ArrayList<PostsEntity>();
            while (rs.next() && count > 0) {
                markPostRead(rs.getInt(1));
                out.add(new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0));
                count--;
            }

            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getUsersByTagSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN UserTags ON UserTags.hid=Users.hid WHERE tagText=? AND hid<?";
    private PreparedStatement getUsersByTagStmt;
    public ArrayList<UsersEntity> getUsersByTag(String tagText, int index, int count) {
        try {
            this.getUsersByTagStmt.setString(1, tagText);
            this.getUsersByTagStmt.setInt(2, index);
            ResultSet rs = this.getUsersByTagStmt.executeQuery();

            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next() && count > 0) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
                count--;
            }

            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String addPostTagSql = "INSERT INTO PostTags (pid, tagText) VALUES (?, ?)";
    private PreparedStatement addPostTagStmt;
    public boolean addPostTag(int pid, String tagText) {
        try {
            this.addPostTagStmt.setInt(1, pid);
            this.addPostTagStmt.setString(2, tagText);
            this.addPostTagStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String addUserTagSql = "INSERT INTO UserTags (hid, tagText) VALUES (?, ?)";
    private PreparedStatement addUserTagStmt;
    public boolean addUserTag(int hid, String tagText) {
        try {
            this.addUserTagStmt.setInt(1, hid);
            this.addUserTagStmt.setString(2, tagText);
            this.addUserTagStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String deletePostTagSql = "DELETE FROM PostTags WHERE pid=? AND tagText=?";
    private PreparedStatement deletePostTagStmt;
    public boolean deletePostTag(int pid, String tagText) {
        try {
            this.deletePostTagStmt.setInt(1, pid);
            this.deletePostTagStmt.setString(2, tagText);
            this.deletePostTagStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String deleteUserTagSql = "DEELTE FROM UserTags WHERE hid=? AND tagText=?";
    private PreparedStatement deleteUserTagStmt;
    public boolean deleteUserTag(int hid, String tagText) {
        try {
            this.deleteUserTagStmt.setInt(1, hid);
            this.deleteUserTagStmt.setString(2, tagText);
            this.deleteUserTagStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getUserTagsSql = "SELECT tagText FROM UserTags where hid=?";
    private PreparedStatement getUserTagsStmt;
    public ArrayList<String> getUserTags(int hid) {
        try {
            this.getUserTagsStmt.setInt(1, hid);
            ResultSet rs = this.getUserTagsStmt.executeQuery();

            ArrayList<String> out = new ArrayList<String>();
            while (rs.next()) {
                out.add(rs.getString(1));
            }
            return out;
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    private static String getPostTagsSql = "SELECT tagText FROM PostTags where pid=?";
    private PreparedStatement getPostTagsStmt;
    public ArrayList<String> getPostTags(int pid) {
        try {
            this.getPostTagsStmt.setInt(1, pid);
            ResultSet rs = this.getPostTagsStmt.executeQuery();

            ArrayList<String> out = new ArrayList<String>();
            while (rs.next()) {
                out.add(rs.getString(1));
            }
            return out;
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    // disgusting
    public ArrayList<UsersEntity> searchUsers(String email, String phone, String name, String screenname, String[] tagsAnd, String[] tagsOr, Integer lastPost, Integer postCount) {
        try {
            StringBuilder sql = new StringBuilder("SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users WHERE 1=1");
            if (email != null) {
                sql.append(" AND (email=?)");
            }
            if (phone != null) {
                sql.append(" AND (phone=?)");
            }
            if (name != null) {
                sql.append(" AND (name=?)");
            }
            if (screenname != null) {
                sql.append(" AND (screenname=?)");
            }
            if (tagsAnd != null) {
                for (String tag : tagsAnd) {
                    sql.append(" AND EXISTS(SELECT hid FROM UserTags WHERE hid=Users.hid AND tagText=?)");
                }
            }
            if (tagsOr != null) {
                sql.append(" AND (1=0");
                for (String tag : tagsOr) {
                    sql.append(" OR EXISTS(SELECT hid FROM UserTags WHERE hid=Users.hid AND tagText=?)");
                }
                sql.append(")");
            }
            if (lastPost != null) {
                sql.append(" AND EXISTS(SELECT pid FROM Posts WHERE author=Users.hid AND timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY')))");
            }
            if (postCount != null) {
                sql.append(" AND ?<=(SELECT COUNT(*) FROM Posts where author=Users.hid AND timestamp >= DATE_SUB(getTime(), INTERVAL '7' DAY))");
            }

            System.err.println(sql);
            PreparedStatement stmt = con.prepareStatement(sql.toString());
            int p = 1;

            if (email != null) {
                stmt.setString(p, email);
                p++;
            }
            if (phone != null) {
                stmt.setString(p, phone);
                p++;
            }
            if (name != null) {
                stmt.setString(p, name);
                p++;
            }
            if (screenname != null) {
                stmt.setString(p, screenname);
                p++;
            }
            if (tagsAnd != null) {
                for (String tag : tagsAnd) {
                    stmt.setString(p, tag);
                    p++;
                }
            }
            if (tagsOr != null) {
                for (String tag : tagsOr) {
                    stmt.setString(p, tag);
                    p++;
                }
            }
            if (lastPost != null) {
                stmt.setString(p, new Integer(lastPost).toString());
                p++;
            }
            if (postCount != null) {
                stmt.setInt(p, postCount);
                p++;
            }

            ResultSet rs = stmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // this is not a pretty query
    private static String getPrivateMessageThreadsSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users join (" +
        "SELECT other, MAX(timestamp) AS last FROM (" +
            "SELECT author as other, timestamp FROM Chats WHERE Chats.hid=? AND deletedByReceiver=0 AND Chats.gid is NULL " +
            "UNION SELECT hid as other, timestamp FROM Chats where Chats.author=? AND deletedBySender=0 AND Chats.gid is NULL" +
        ") Sub1 GROUP BY other" +
    ") Sub2 on other=Users.hid ORDER BY last DESC";
    private PreparedStatement getPrivateMessageThreadsStmt;
    public ArrayList<UsersEntity> getPrivateMessageThreads(int hid) {
        try {
            getPrivateMessageThreadsStmt.setInt(1, hid);
            getPrivateMessageThreadsStmt.setInt(2, hid);

            ResultSet rs = getPrivateMessageThreadsStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next()) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // slightly less disgusting
    public ArrayList<PostsEntity> searchPosts(String[] tagsAnd, String[] tagsOr, int index, int count) {
        try {
            StringBuilder sql = new StringBuilder("SELECT pid, author, text, timestamp, isPublic FROM Posts WHERE pid<? AND isPublic!=0");
            if (tagsAnd != null) {
                for (String tag : tagsAnd) {
                    sql.append(" AND EXISTS(SELECT pid FROM PostTags WHERE pid=Posts.pid AND tagText=?)");
                }
            }
            if (tagsOr != null) {
                sql.append(" AND (1=0");
                for (String tag : tagsOr) {
                    sql.append(" OR EXISTS(SELECT pid FROM PostTags WHERE pid=Posts.pid AND tagText=?)");
                }
                sql.append(")");
            }

            sql.append(" ORDER BY timestamp DESC, pid DESC");
            PreparedStatement stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, index);
            int p = 2;

            if (tagsAnd != null) {
                for (String tag : tagsAnd) {
                    stmt.setString(p, tag);
                    p++;
                }
            }
            if (tagsOr != null) {
                for (String tag : tagsOr) {
                    stmt.setString(p, tag);
                    p++;
                }
            }

            ResultSet rs = stmt.executeQuery();
            ArrayList<PostsEntity> out = new ArrayList<PostsEntity>();
            while (rs.next() && count > 0) {
                markPostRead(rs.getInt(1));
                out.add(new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0));
                count--;
            }
            return out;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getActiveUsersSql = "SELECT Users.hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN (SELECT author, COUNT(*) as postcount FROM Posts WHERE timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY')) GROUP BY author) PostStats on PostStats.author=Users.hid ORDER BY PostStats.postcount DESC";
    private PreparedStatement getActiveUsersStmt;
    public ArrayList<UsersEntity> getActiveUsers(int since, int count) {
        try {
            getActiveUsersStmt.setInt(1, since);
            ResultSet rs = getActiveUsersStmt.executeQuery();
            ArrayList<UsersEntity> out = new ArrayList<UsersEntity>();
            while (rs.next() && count > 0) {
                out.add(new UsersEntity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7) != 0));
                count--;
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String countInactiveUsersSql = "SELECT COUNT(*) FROM (SELECT author, COUNT(*) AS postcount FROM Posts where timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY')) GROUP BY author) PostStats WHERE postcount <= ?";
    private PreparedStatement countInactiveUsersStmt;
    public Integer countInactiveUsers(int since, int threshold) {
        try {
            countInactiveUsersStmt.setInt(1, since);
            countInactiveUsersStmt.setInt(2, threshold);
            ResultSet rs = countInactiveUsersStmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getTopPostsSql = "SELECT Posts.pid, author, text, timestamp, isPublic FROM Posts JOIN (SELECT pid, COUNT(*) as viewcount FROM Reads WHERE timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY')) GROUP BY pid) PopPosts ON PopPosts.pid=Posts.pid ORDER BY viewcount DESC";
    private PreparedStatement getTopPostsStmt;
    public ArrayList<PostsEntity> getTopPosts(int since, int count) {
        try {
            getTopPostsStmt.setInt(1, since);
            ResultSet rs = getTopPostsStmt.executeQuery();

            ArrayList<PostsEntity> out = new ArrayList<PostsEntity>();
            while (rs.next() && count > 0) {
                out.add(new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0));
                count--;
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getMessageCountSql = "SELECT COUNT(*) FROM Posts WHERE timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY'))";
    private PreparedStatement getMessageCountStmt;
    public Integer getMessageCount(int since) {
        try {
            getMessageCountStmt.setInt(1, since);
            ResultSet rs = getMessageCountStmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getReadCountSql = "SELECT COUNT(*) FROM Reads WHERE timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY'))";
    private PreparedStatement getReadCountStmt;
    public Integer getReadCount(int since) {
        try {
            getReadCountStmt.setInt(1, since);
            ResultSet rs = getReadCountStmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getReadCountForNewMessagesSql = "SELECT COUNT(*) FROM Reads JOIN Posts ON Reads.pid=Posts.pid WHERE Reads.timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY')) AND Posts.timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY'))";
    private PreparedStatement getReadCountForNewMessagesStmt;
    public Integer getReadCountForNewMessages(int since) {
        try {
            getReadCountForNewMessagesStmt.setInt(1, since);
            getReadCountForNewMessagesStmt.setInt(2, since);
            ResultSet rs = getReadCountForNewMessagesStmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getTopTagDataSql = "SELECT Posts.pid, author, text, timestamp, isPublic, tagText, viewCount FROM Posts JOIN (SELECT PostTags.pid, tagText, COUNT(*) as viewCount FROM PostTags JOIN Reads on PostTags.pid=Reads.pid WHERE timestamp >= DATE_SUB(getTime(), NUMTODSINTERVAL(?, 'DAY')) GROUP BY tagText, PostTags.pid) viewData on viewData.pid=Posts.pid ORDER BY viewCount DESC";
    private PreparedStatement getTopTagDataStmt;
    public Map<String, PostsEntity> getTopTagData(int since) {
        try {
            getTopTagDataStmt.setInt(1, since);
            ResultSet rs = getTopTagDataStmt.executeQuery();
            Map<String, PostsEntity> out = new HashMap<String, PostsEntity>();
            Map<String, Integer> sofar = new HashMap<String, Integer>();
            while (rs.next()) {
                String tag = rs.getString(6);
                Integer count = rs.getInt(7);
                if (!sofar.containsKey(tag) || sofar.get(tag) < count) {
                    sofar.put(tag, count);
                    out.put(tag, new PostsEntity(rs.getInt(1), rs.getInt(2), rs.getString(3), new Date(rs.getTimestamp(4).getTime()), rs.getInt(5) != 0));
                }
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String insertReportSql = "INSERT INTO Reports (tid, timestamp, newMessages, messageReads, avgMessageReads, avgNewMessageReads, topPost1, topPost2, topPost3, topUser1, topUser2, topUser3, inactiveUserCount) VALUES (NEXTVAL('SeqTid'), getTime(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement insertReportStmt;
    public Integer insertReport(int newMessages, int newMessageReads, float avgMessageReads, float avgNewMessageReads, Integer topPost1, Integer topPost2, Integer topPost3, Integer topUser1, Integer topUser2, Integer topUser3, int inactiveUserCount) {
        try {
            insertReportStmt.setInt(1, newMessages);
            insertReportStmt.setInt(2, newMessageReads);
            insertReportStmt.setFloat(3, avgMessageReads);
            insertReportStmt.setFloat(4, avgNewMessageReads);
            if (topPost1 == null)
                insertReportStmt.setNull(5, Types.INTEGER);
            else
                insertReportStmt.setInt(5, topPost1);
            if (topPost2 == null)
                insertReportStmt.setNull(6, Types.INTEGER);
            else
                insertReportStmt.setInt(6, topPost2);
            if (topPost3 == null)
                insertReportStmt.setNull(7, Types.INTEGER);
            else
                insertReportStmt.setInt(7, topPost3);
            if (topUser1 == null)
                insertReportStmt.setNull(8, Types.INTEGER);
            else
                insertReportStmt.setInt(8, topUser1);
            if (topUser2 == null)
                insertReportStmt.setNull(9, Types.INTEGER);
            else
                insertReportStmt.setInt(9, topUser2);
            if (topUser3 == null)
                insertReportStmt.setNull(10, Types.INTEGER);
            else
                insertReportStmt.setInt(10, topUser3);
            insertReportStmt.setInt(11, inactiveUserCount);

            insertReportStmt.executeUpdate();
            ResultSet rs = insertReportStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String insertReportTagDataSql = "INSERT INTO ReportTagData (tid, tagText, pid) VALUES (?, ?, ?)";
    private PreparedStatement insertReportTagDataStmt;
    public boolean insertReportTagData(int tid, String tagText, int pid) {
        try {
            insertReportTagDataStmt.setInt(1, tid);
            insertReportTagDataStmt.setString(2, tagText);
            insertReportTagDataStmt.setInt(3, pid);
            insertReportTagDataStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // what the shit have I gotten myself into
    private static String getReportsSql = "SELECT tid, timestamp, newMessages, messageReads, avgMessageReads, avgNewMessageReads, topPost1, topPost2, topPost3, topUser1, topUser2, topUser3, inactiveUserCount FROM Reports ORDER BY timestamp DESC";
    private PreparedStatement getReportsStmt;
    private static String getReportsTagDataSql = "SELECT ReportTagData.pid, author, text, timestamp, isPublic, tagText FROM Posts RIGHT JOIN ReportTagData ON ReportTagData.pid=Posts.pid WHERE tid=?";
    private PreparedStatement getReportsTagDataStmt;
    public ArrayList<ReportsEntity> getReports() {
        try {
            ResultSet rs = getReportsStmt.executeQuery();
            ArrayList<ReportsEntity> out = new ArrayList<ReportsEntity>();
            while (rs.next()) {
                int tid = rs.getInt(1);
                PostsEntity topPost1 = getPost(rs.getInt(7));
                PostsEntity topPost2 = getPost(rs.getInt(8));
                PostsEntity topPost3 = getPost(rs.getInt(9));
                UsersEntity topUser1 = getUser(rs.getInt(10));
                UsersEntity topUser2 = getUser(rs.getInt(11));
                UsersEntity topUser3 = getUser(rs.getInt(12));

                getReportsTagDataStmt.setInt(1, tid);
                ResultSet rs2 = getReportsTagDataStmt.executeQuery();
                Map<String, PostsEntity> tagData = new HashMap<String, PostsEntity>();
                while (rs2.next()) {
                    int pid = rs2.getInt(1);
                    tagData.put(rs2.getString(6), pid == 0 ? null : new PostsEntity(rs2.getInt(1), rs2.getInt(2), rs2.getString(3), new Date(rs2.getTimestamp(4).getTime()), rs2.getInt(5) != 0));
                }

                out.add(new ReportsEntity(tid, new Date(rs.getTimestamp(2).getTime()), rs.getInt(3), rs.getInt(4), rs.getFloat(5), rs.getFloat(6), topPost1, topPost2, topPost3, topUser1, topUser2, topUser3, rs.getInt(13), tagData));
            }

            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String collectGarbage1Sql = "DELETE FROM Chats WHERE gid IS NOT NULL AND timestamp < DATE_SUB(getTime(), NUMTODSINTERVAL((SELECT duration FROM ChatGroups where ChatGroups.gid=Chats.gid), 'DAY'))";
    private PreparedStatement collectGarbage1Stmt;
    public boolean collectGarbage() {
        try {
            collectGarbage1Stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String logPostReadSql = "INSERT INTO Reads (pid, timestamp) VALUES (?, getTime())";
    private PreparedStatement logPostReadStmt;
    public boolean logPostRead(int pid) {
        try {
            logPostReadStmt.setInt(1, pid);
            logPostReadStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class PostReadWorker implements Runnable {
        private int pid;
        public PostReadWorker(int pid) {
            this.pid = pid;
        }

        public void run() {
            Database.i().logPostRead(pid);
        }
    }

    public static void markPostRead(int pid) {
        bgJobs.submit(new PostReadWorker(pid));
    }

    private static ThreadPoolExecutor bgJobs = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    public static void cleanup() {
        bgJobs.shutdown();
        for (Connection con : conList) {
            try {
                con.close();
            } catch (Exception e) {}
        }
    }
}
