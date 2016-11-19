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

    public ArrayList<ChatGroupsEntity> getChatGroups() {
        return Database.i().getChatGroupsForUser(this.hid);
    }

    public ArrayList<ChatGroupsEntity> getPendingChatGroups() {
        return Database.i().getChatGroupsPendingForUser(this.hid);
    }

    public boolean addTag(String tagText) {
        return Database.i().addUserTag(this.hid, tagText);
    }

    public boolean deleteTag(String tagText) {
        return Database.i().deleteUserTag(this.hid, tagText);
    }
}
