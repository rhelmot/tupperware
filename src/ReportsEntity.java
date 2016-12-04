import java.util.*;

public class ReportsEntity extends Entity {
    public Integer tid;
    public Date timestamp;
    public int newMessages;
    public int newMessageReads;
    public float avgMessageReads;
    public float avgNewMessageReads;
    public PostsEntity topPost1;
    public PostsEntity topPost2;
    public PostsEntity topPost3;
    public UsersEntity topUser1;
    public UsersEntity topUser2;
    public UsersEntity topUser3;
    public int inactiveUserCount;
    public Map<String, PostsEntity> tagData;

    public ReportsEntity(
            Integer tid,
            Date timestamp,
            int newMessages,
            int newMessageReads,
            float avgMessageReads,
            float avgNewMessageReads,
            PostsEntity topPost1,
            PostsEntity topPost2,
            PostsEntity topPost3,
            UsersEntity topUser1,
            UsersEntity topUser2,
            UsersEntity topUser3,
            int inactiveUserCount,
            Map<String, PostsEntity> tagData) {
        this.newMessages = newMessages;
        this.timestamp = timestamp;
        this.newMessageReads = newMessageReads;
        this.avgMessageReads = avgMessageReads;
        this.avgNewMessageReads = avgNewMessageReads;
        this.topPost1 = topPost1;
        this.topPost2 = topPost2;
        this.topPost3 = topPost3;
        this.topUser1 = topUser1;
        this.topUser2 = topUser2;
        this.topUser3 = topUser3;
        this.inactiveUserCount = inactiveUserCount;
        this.tagData = tagData;
    }

    public static ReportsEntity generate() {
        int newMessages = Database.i().getMessageCount(7);
        int newMessageReads = Database.i().getReadCount(7);
        float avgMessageReads = newMessageReads == 0 ? 0 : newMessageReads / (float)newMessages;
        int newestMessageReads = Database.i().getReadCountForNewMessages(7);
        float avgNewMessageReads = newestMessageReads == 0 ? 0 : newestMessageReads / (float)newMessages;

        ArrayList<PostsEntity> topPosts = Database.i().getTopPosts(7, 3);
        PostsEntity topPost1 = topPosts.size() >= 1 ? topPosts.get(0) : null;
        PostsEntity topPost2 = topPosts.size() >= 2 ? topPosts.get(1) : null;
        PostsEntity topPost3 = topPosts.size() >= 3 ? topPosts.get(2) : null;

        ArrayList<UsersEntity> topUsers = Database.i().getActiveUsers(7, 3);
        UsersEntity topUser1 = topUsers.size() >= 1 ? topUsers.get(0) : null;
        UsersEntity topUser2 = topUsers.size() >= 2 ? topUsers.get(1) : null;
        UsersEntity topUser3 = topUsers.size() >= 3 ? topUsers.get(2) : null;

        int inactiveUserCount = Database.i().countInactiveUsers(7, 3);

        Map<String, PostsEntity> tagData = Database.i().getTopTagData(7);
        return new ReportsEntity(null, Database.i().getTime(), newMessages, newMessageReads, avgMessageReads, avgNewMessageReads, topPost1, topPost2, topPost3, topUser1, topUser2, topUser3, inactiveUserCount, tagData);
    }

    public boolean save() {
        if (tid != null) {
            // why would you want to do this
            return false;
        }

        tid = Database.i().insertReport(newMessages, newMessageReads, avgMessageReads, avgNewMessageReads, topPost1 == null ? null : topPost1.pid, topPost2 == null ? null : topPost2.pid, topPost3 == null ? null : topPost3.pid, topUser1 == null ? null : topUser1.hid, topUser2 == null ? null : topUser2.hid, topUser3 == null ? null : topUser3.hid, inactiveUserCount);
        if (tid != null) {
            for (Map.Entry<String, PostsEntity> entry : tagData.entrySet()) {
                if (!Database.i().insertReportTagData(tid, entry.getKey(), entry.getValue().pid)) {
                    // ???
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
