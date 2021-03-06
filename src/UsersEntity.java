import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class UsersEntity extends Entity {
    public Integer hid;
    public String email;
    public String name;
    public String phone;
    public String passwordHash;
    public String screenname;
    public boolean isManager;

    public static UsersEntity create(
            String name,
            String email,
            String password,
            String phone,
            String screenname,
            boolean isManager) throws DomainError {
        if (email.length() > 20) throw new DomainError("Email too long");
        for (int i = 0; i < email.length(); i++) {
            char c = email.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                throw new DomainError("Email must not contain whitespace");
            }
        }

        if (name.length() > 20) throw new DomainError("Name too long");

        if (phone.length() != 10) throw new DomainError("Phone must be 10 chars");
        for (int i = 0; i < phone.length(); i++) {
            if (phone.charAt(i) < '0' || phone.charAt(i) > '9') {
                throw new DomainError("Phone must be digits");
            }
        }

        if (password.length() < 2 || password.length() > 10) {
            throw new DomainError("Password must be 2-10 chars");
        }

        UsersEntity out = new UsersEntity(null, email, name, phone, UsersEntity.hashPassword(password), screenname, isManager);
        if (out.save()) {
            return out;
        } else {
            return null;
        }
    }

    public UsersEntity(
            Integer hid,
            String email,
            String name,
            String phone,
            String passwordHash,
            String screenname,
            boolean isManager) {
        this.hid = hid;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.screenname = screenname;
        this.isManager = isManager;
        this.existsInTable = existsInTable;
    }

    private final static char[] hexChars = "0123456789ABCDEF".toCharArray();

    public static String hashPassword (String password) {
        MessageDigest md = null;
        try {
           md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {}
        md.update(password.getBytes(StandardCharsets.UTF_8));
        md.update("hell yell".getBytes(StandardCharsets.UTF_8)); // salt

        byte[] hash = md.digest();
        char[] hexhash = new char[hash.length * 2];

        for (int i = 0; i < hash.length; i++) {
            hexhash[i*2] = UsersEntity.hexChars[(hash[i] & 0xf0) >>> 4];
            hexhash[i*2 + 1] = UsersEntity.hexChars[hash[i] & 15];
        }

        return new String(hexhash);
    }

    public boolean checkPassword(String password) {
        return this.passwordHash.equals(UsersEntity.hashPassword(password));
    }

    public static UsersEntity get(int id) {
        return Database.i().getUser(id);
    }

    public static UsersEntity getByEmail(String email) {
        return Database.i().getUserByEmail(email);
    }

    public boolean save() {
        if (this.hid == null) {
            this.hid = Database.i().insertUser(this.email, this.name, this.phone, this.passwordHash, this.screenname, this.isManager);
            return this.hid != null;
        } else {
            return Database.i().updateUser(this.hid, this.email, this.name, this.phone, this.passwordHash, this.screenname, this.isManager);
        }
    }

    public ArrayList<UsersEntity> getFriends() {
        return Database.i().getFriends(this.hid);
    }

    public ArrayList<UsersEntity> getActiveRequests() {
        return Database.i().getActiveRequests(this.hid);
    }

    public ArrayList<UsersEntity> getPendingRequests() {
        return Database.i().getPendingRequests(this.hid);
    }

    public boolean isActiveRequest(UsersEntity other) {
        return getActiveRequests().contains(other);
    }

    public boolean isPendingRequest(UsersEntity other) {
        return getPendingRequests().contains(other);
    }

    public boolean makeFriends(UsersEntity other) {
        return Database.i().makeFriends(this.hid, other.hid);
    }

    public boolean areFriends(UsersEntity other) {
        return Database.i().areFriends(this.hid, other.hid);
    }

    public boolean requestFriends(UsersEntity other) {
        return Database.i().requestFriends(this.hid, other.hid);
    }

    public boolean unrequestFriends(UsersEntity other) {
        return Database.i().unrequestFriends(this.hid, other.hid);
    }

    public boolean unfriend(UsersEntity other) {
        return Database.i().unfriend(this.hid, other.hid);
    }

    public ArrayList<ChatGroupsEntity> getChatGroups() {
        return Database.i().getChatGroupsForUser(this.hid);
    }

    public ArrayList<ChatGroupsEntity> getPendingChatGroups() {
        return Database.i().getChatGroupsPendingForUser(this.hid);
    }

    public ArrayList<UsersEntity> getPrivateMessages() {
        return Database.i().getPrivateMessageThreads(this.hid);
    }

    public boolean addTag(String tagText) {
        return Database.i().addUserTag(this.hid, tagText);
    }

    public boolean deleteTag(String tagText) {
        return Database.i().deleteUserTag(this.hid, tagText);
    }

    public ArrayList<String> getTags() {
        return Database.i().getUserTags(hid);
    }

    public static ArrayList<UsersEntity> search(String email, String phone, String name, String screenname, String[] tagsAnd, String[] tagsOr, Integer lastPost, Integer postCount) throws DomainError {
        if (email == null && phone == null && name == null && screenname == null && tagsAnd == null && tagsOr == null && lastPost == null && postCount == null) {
            throw new DomainError("You must specify at last one search parameter!");
        }

        if (email != null) {
            if (email.length() > 20) throw new DomainError("Email too long");
            for (int i = 0; i < email.length(); i++) {
                char c = email.charAt(i);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    throw new DomainError("Email must not contain whitespace");
                }
            }
        }

        if (name != null && name.length() > 20) throw new DomainError("Name too long");

        if (phone != null) {
            if (phone.length() != 10) throw new DomainError("Phone must be 10 chars");
            for (int i = 0; i < phone.length(); i++) {
                if (phone.charAt(i) < '0' || phone.charAt(i) > '9') {
                    throw new DomainError("Phone must be digits");
                }
            }
        }

        if (tagsAnd != null) {
            for (int i = 0; i < tagsAnd.length; i++) {
                String tag = tagsAnd[i].trim();;
                tagsAnd[i] = tag;
                if (tag.length() > 200) {
                    throw new DomainError("Tags must be at most 200 chars");
                } else if (tag.length() == 0) {
                    throw new DomainError("Tags must be longer than zero characters");
                }
            }
        }

        if (tagsOr != null) {
            for (int i = 0; i < tagsOr.length; i++) {
                String tag = tagsOr[i].trim();;
                tagsOr[i] = tag;
                if (tag.length() > 200) {
                    throw new DomainError("Tags must be at most 200 chars");
                } else if (tag.length() == 0) {
                    throw new DomainError("Tags must be longer than zero characters");
                }
            }
        }

        return Database.i().searchUsers(email, phone, name, screenname, tagsAnd, tagsOr, lastPost, postCount);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UsersEntity) {
            return ((UsersEntity)other).hid == hid;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 97653 + 371 * hid;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
