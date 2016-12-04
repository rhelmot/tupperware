import java.util.Date;
import java.util.ArrayList;

public class ChatsEntity extends Entity {
    public Integer cid;
    public int author;
    private UsersEntity authorUser;
    public String text;
    public Date timestamp;

    public Integer hid;
    private UsersEntity receiverUser;
    public boolean deletedBySender;
    public boolean deletedByReceiver;

    public Integer gid;
    private ChatGroupsEntity receiverGroup;

    public ChatsEntity(
            Integer cid,
            int author,
            String text,
            Date timestamp,
            Integer hid,
            Integer gid,
            boolean deletedBySender,
            boolean deletedByReceiver) {
        this.cid = cid;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.hid = hid;
        this.gid = gid;
        this.deletedBySender = deletedBySender;
        this.deletedByReceiver = deletedByReceiver;

        this.authorUser = null;
        this.receiverUser = null;
        this.receiverGroup = null;
    }

    public static ChatsEntity create(UsersEntity author, String text, UsersEntity receiver) throws DomainError {
        if (text.length() > 1400) {
            throw new DomainError("Chat text must be no more than 1400 chars");
        }
        ChatsEntity out = new ChatsEntity(null, author.hid, text, Database.i().getTime(), receiver.hid, null, false, false);
        if (out.save()) {
            return out;
        } else {
            return null;
        }
    }

    public static ChatsEntity create(UsersEntity author, String text, ChatGroupsEntity receiver) throws DomainError {
        if (text.length() > 1400) {
            throw new DomainError("Chat text must be no more than 1400 chars");
        }

        // AAAAAAAGH
        // this call creates many entries so there is no one entity we can return.
        // make a dummy one for returning to the user
        if (Database.i().insertChatForGroup(author.hid, text, receiver.gid)) {
            return new ChatsEntity(null, author.hid, text, Database.i().getTime(), author.hid, receiver.gid, false, false);
        } else {
            return null;
        }
    }

    public boolean save() {
        if (this.cid == null) {
            if (this.gid != null) return false; // :/
            this.cid = Database.i().insertChat(this.author, this.text, this.hid);
            return this.cid != null;
        } else {
            System.err.println("You can't update a chat like that!!!");
            return false;
        }
    }

    public boolean delete(UsersEntity deleter) {
        if (deleter.hid.equals(this.hid)) {
            if (Database.i().deleteChatReceiver(this.cid)) {
                this.deletedByReceiver = true;
                return true;
            } else {
                return false;
            }
        } else if (deleter.hid.equals(this.author)) {
            if (Database.i().deleteChatSender(this.cid)) {
                this.deletedBySender = true;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static ArrayList<ChatsEntity> getChats(UsersEntity me, UsersEntity other, ChatsEntity after, int count) {
        // this is... a hack
        // correct solution: have two queries, one from cold start, one for load more
        return Database.i().getChats(me.hid, other.hid, after == null ? 999999999 : after.cid, count);
    }

    public static ArrayList<ChatsEntity> getGroupChats(UsersEntity me, ChatGroupsEntity group, ChatsEntity after, int count) {
        return Database.i().getChatsForGroup(me.hid, group.gid, after == null ? 999999999 : after.cid, count);
    }

    public String toString() {
        return text;
    }

    public boolean equals(Object other) {
        if (other instanceof ChatsEntity) {
            return cid.equals(((ChatsEntity)other).cid);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 89043 + cid.hashCode() * 4457;
    }
}
