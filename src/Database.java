import java.sql.*;
import java.lang.ThreadLocal;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class Database {
    private static ThreadLocal<Database> inst = new ThreadLocal<Database>() {
        protected synchronized Database initialValue() {
            return new Database();
        }
    };
    private Connection con;

    public Database() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String url = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
            String username = "dutcher";
            String password = "096";

            this.con = DriverManager.getConnection(url, username, password);

            this.getTimeStmt = con.prepareStatement(Database.getTimeSql);
            this.setTimeStaticStmt = con.prepareStatement(Database.setTimeStaticSql);
            this.setTimeDynamicStmt = con.prepareStatement(Database.setTimeDynamicSql);

            this.getUserStmt = con.prepareStatement(Database.getUserSql);
            this.getUserByEmailStmt = con.prepareStatement(Database.getUserByEmailSql);
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

            this.insertChatStmt = con.prepareStatement(Database.insertChatSql, new String[]{"cid"});
            this.insertChatForGroupStmt = con.prepareStatement(Database.insertChatForGroupSql);
            this.deleteChatSenderStmt = con.prepareStatement(Database.deleteChatSenderSql);
            this.deleteChatReceiverStmt = con.prepareStatement(Database.deleteChatReceiverSql);
            this.getChatsStmt = con.prepareStatement(Database.getChatsSql);
            this.getChatsForGroupStmt = con.prepareStatement(Database.getChatsForGroupSql);

            this.insertPostStmt = con.prepareStatement(Database.insertPostSql, new String[]{"pid"});
            this.updatePostStmt = con.prepareStatement(Database.updatePostSql);
            this.makePostVisibleStmt = con.prepareStatement(Database.makePostVisibleSql);

            this.getPostsByUserStmt = con.prepareStatement(Database.getPostsByUserSql);
            this.getPostsByTagStmt = con.prepareStatement(Database.getPostsByTagSql);
            this.getUsersByTagStmt = con.prepareStatement(Database.getUsersByTagSql);
            this.addPostTagStmt = con.prepareStatement(Database.addPostTagSql);
            this.addUserTagStmt = con.prepareStatement(Database.addUserTagSql);
            this.deletePostTagStmt = con.prepareStatement(Database.deletePostTagSql);
            this.deleteUserTagStmt = con.prepareStatement(Database.deleteUserTagSql);
        } catch (Exception e) {
            System.err.println("FATAL: Failed to set up database connection!");
            e.printStackTrace();
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

    private static String getTimeSql = "SELECT getTime() from dual";
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

    private static String getFriendsSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users join Friendships on Friendships.left = Users.hid WHERE Friendships.right=?";
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

    private static String makeFriendsSql = "INSERT ALL INTO Friendships (left, right, since) VALUES (?, ?, getTime()) INTO Friendships (left, right, since) VALUES (?, ?, getTime()) SELECT * from dual";
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

    private static String areFriendsSql = "SELECT 1 FROM Friendships WHERE left=? AND right=?";
    private PreparedStatement areFriendsStmt;
    public boolean areFriends(int hid1, int hid2) {
        try {
            this.makeFriendsStmt.setInt(1, hid1);
            this.makeFriendsStmt.setInt(2, hid2);
            ResultSet rs = this.makeFriendsStmt.executeQuery();
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
            this.makeFriendsStmt.setInt(1, requester);
            this.makeFriendsStmt.setInt(2, requestee);
            this.makeFriendsStmt.executeUpdate();
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
            this.makeFriendsStmt.setInt(1, requester);
            this.makeFriendsStmt.setInt(2, requestee);
            this.makeFriendsStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
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

    private static String getChatGroupMembersSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid = ChatGroupMemberships.hid WHERE ChatGroupMemberships.gid=? AND invitationAccepted != 0";
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

    private static String getChatGroupPendingMembersSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN ChatGroupMemberships ON Users.hid = ChatGroupMemberships.hid WHERE ChatGroupMemberships.gid=? AND invitationAccepted == 0";
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

    private static String getChatGroupsPendingForUserSql = "SELECT gid, groupName, duration FROM ChatGroups JOIN ChatGroupMemberships ON ChatGroups.gid = ChatGroupMemberships.gid WHERE ChatGroupMemberships.hid=? AND invitationAccepted == 0";
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

    private static String insertChatSql = "INSERT INTO Chats (cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver) VALUES (SeqCid.NEXTVAL, ?, ?, getTime(), ?, NULL, 0, 0)";
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
    private static String insertChatForGroupSql = "INSERT INTO Chats (cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver) SELECT SeqCid.NEXTVAL, ?, ?, getTime(), hid, ?, 1, 0 FROM ChatGroupMemberships WHERE gid=? AND invitationAccepted!=0";
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

    private static String deleteChatSenderSql = "UPDATE Chats SET deletedBySender=1 WHERE cid=?)";
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

    private static String deleteChatReceiverSql = "UPDATE Chats SET deletedByReceiver=1 WHERE cid=?)";
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

    private static String getChatsSql = "SELECT cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver FROM Chats WHERE ((author=? AND hid=? AND deletedBySender=0) OR (author=? AND hid=? AND deletedByReceiver=0)) AND cid<? ORDER BY timetstamp DESC";
    private PreparedStatement getChatsStmt;
    public ArrayList<ChatsEntity> getChats(int me, int other, int index, int count) {
        try {
            this.getChatsStmt.setInt(1, me);
            this.getChatsStmt.setInt(2, other);
            this.getChatsStmt.setInt(3, me);
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

    private static String getChatsForGroupSql = "SELECT cid, author, text, timestamp, hid, gid, deletedBySender, deletedByReceiver FROM Chats WHERE hid=? AND gid=? AND deletedByReceiver=0 AND cid<? ORDER BY timestamp DESC";
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

    private static String insertPostSql = "INSERT INTO Posts (pid, author, text, timestamp, isPublic) VALUES (SeqPid.NEXTVAL, ?, ?, getTime(), ?)";
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

    private static String getPostsByUserSql = "SELECT pid, author, text, timestamp, isPublic FROM Posts JOIN PostVisibilities ON PostVisibilities.pid=Posts.pid WHERE hid=? (OR hid is NULL OR (isPublic!=0 AND ?)) AND pid<?";
    private PreparedStatement getPostsByUserStmt;
    public ArrayList<PostsEntity> getPostsByUser(int hid, boolean includePublic, int index, int count) {
        try {
            this.getPostsByUserStmt.setInt(1, hid);
            this.getPostsByUserStmt.setBoolean(2, includePublic);
            this.getPostsByUserStmt.setInt(3, index);
            ResultSet rs = this.getPostsByUserStmt.executeQuery();

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

    private static String getPostsByTagSql = "SELECT pid, author, text, timestamp, isPublic FROM Posts JOIN PostTags ON PostTags.pid=Posts.pid WHERE tagText=? AND pid<?";
    private PreparedStatement getPostsByTagStmt;
    public ArrayList<PostsEntity> getPostsByTag(String tagText, int index, int count) {
        try {
            this.getPostsByTagStmt.setString(1, tagText);
            this.getPostsByTagStmt.setInt(2, index);
            ResultSet rs = this.getPostsByTagStmt.executeQuery();

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

    private static String getUsersByTagSql = "SELECT hid, email, name, phone, passwordHash, screenname, isManager FROM Users JOIN UserTags ON UserTags.hid=Users.hid WHERE tagText=? AND hid<?";
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
}
