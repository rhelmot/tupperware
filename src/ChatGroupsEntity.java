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
            throw new DomainError("Group name must be no more than 20 chars");
        }
        if (duration <= 0) {
            throw new DomainError("Group duration must be a postitve number");
        }
        ChatGroupsEntity out = new ChatGroupsEntity(null, groupName, duration);
        out.owner = owner;
        if (out.save()) {
            if (Database.i().insertChatGroupMembership(out.gid, owner.hid, 0, true, true)) {
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

    public boolean inviteUser(UsersEntity user, UsersEntity inviter) {
        return Database.i().insertChatGroupMembership(this.gid, user.hid, inviter.hid, false, false);
    }

    public boolean acceptInvitation(UsersEntity user) {
        return Database.i().updateChatGroupMembership(this.gid, user.hid, false, true);
    }

    public UsersEntity getOwner() {
        if (owner == null) {
            owner = Database.i().getChatGroupOwner(this.gid);
        }
        return owner;
    }

    public boolean equals(Object other) {
        if (gid == null) return this == other;
        if (other instanceof ChatGroupsEntity) {
            return gid.equals(((ChatGroupsEntity)other).gid);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 99321 + groupName.hashCode() * 56711;
    }
}
