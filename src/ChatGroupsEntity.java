import java.util.ArrayList;

public class ChatGroupsEntity extends Entity {
    public Integer gid;
    public String groupName;
    public int duration;

    private UsersEntity owner;

    public ChatGroupsEntity(Integer gid, String groupName, int duration) {
        this.gid = gid;
        this.groupName = groupName;
        this.duration = duration;

        this.owner = null;
    }

    public static ChatGroupsEntity create(String groupName, int duration, UsersEntity owner) throws DomainError {
        if (groupName.length() > 20) {
            throw new DomainError("Group must be no more than 20 chars");
        }
        ChatGroupsEntity out = new ChatGroupsEntity(null, groupName, duration);
        out.owner = owner;
        if (out.save()) {
            if (Database.i().insertChatGroupMembership(out.gid, owner.hid, true, true)) {
                return out;
            } else {
                // ???????????????
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean save() {
        if (this.gid == null) {
            this.gid = Database.i().insertChatGroup(this.groupName, this.duration);
            return this.gid != null;
        } else {
            return Database.i().updateChatGroup(this.gid, this.groupName, this.duration);
        }
    }

    public ArrayList<UsersEntity> getMembers() {
        return Database.i().getChatGroupMembers(this.gid);
    }

    public ArrayList<UsersEntity> getPendingMembers() {
        return Database.i().getChatGroupPendingMembers(this.gid);
    }
}
