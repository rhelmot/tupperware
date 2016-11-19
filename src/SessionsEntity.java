import java.security.SecureRandom;
import java.math.BigInteger;

public class SessionsEntity extends Entity {
    public Integer sid;
    public String token;
    public int hid;
    private UsersEntity user;
    public boolean isManaging;

    public SessionsEntity(Integer sid, String token, int hid, boolean isManaging) {
        this.sid = sid;
        this.token = token;
        this.hid = hid;
        this.isManaging = isManaging;

        this.user = null;
    }

    // http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
    public static SessionsEntity create(UsersEntity user, boolean isManaging) {
        String token = new BigInteger(160, new SecureRandom()).toString(32);
        SessionsEntity out = new SessionsEntity(null, token, user.hid, isManaging);
        out.user = user;
        if (out.save()) {
            return out;
        } else {
            return null;
        }
    }

    public boolean save() {
        if (this.sid == null) {
            this.sid = Database.i().insertSession(this.token, this.hid, this.isManaging);
            return this.sid != null;
        } else {
            return Database.i().updateSession(this.sid, this.token, this.hid, this.isManaging);
        }
    }

    public UsersEntity getUser() {
        if (this.user == null) {
            this.user = Database.i().getUser(this.hid);
        }
        return this.user;
    }

    public static SessionsEntity lookup(String token) {
        return Database.i().getSession(token);
    }
}
