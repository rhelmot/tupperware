import java.util.Date;
import java.util.ArrayList;

public class PostsEntity extends Entity {
    Integer pid;
    int author;
    String text;
    Date timestamp;
    boolean isPublic;

    public PostsEntity(
            Integer pid,
            int author,
            String text,
            Date timestamp,
            boolean isPublic) {
        this.pid = pid;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.isPublic = isPublic;
    }

    public static PostsEntity createPublic(UsersEntity author, String text, String[] tags) throws DomainError {
        if (text.length() > 1400) {
            throw new DomainError("Post text must be at most 1400 chars");
        }

        for (String tag : tags) {
            if (tag.length() > 200) {
                throw new DomainError("Post tags must be at most 200 chars");
            } else if (tag.length() == 0) {
                if (tags.length == 1) {
                    throw new DomainError("Must provide at least one tag");
                } else {
                    throw new DomainError("Post tags must be longer than zero characters");
                }
            }
        }

        PostsEntity out = new PostsEntity(null, author.hid, text, Database.i().getTime(), true);
        if (out.save()) {
            if (!Database.i().makePostVisible(out.pid, null)) {
                // ???
                return null;
            }
            for (String tag : tags) {
                if (!out.addTag(tag)) {
                    // ???
                    return null;
                }
            }
            return out;
        } else {
            return null;
        }
    }

    public static PostsEntity createPrivate(UsersEntity author, String text, String[] tags, UsersEntity[] audience) throws DomainError {
        if (text.length() > 1400) {
            throw new DomainError("Post text must be at most 1400 chars");
        }

        for (String tag : tags) {
            if (tag.length() > 200) {
                throw new DomainError("Post tags must be at most 200 chars");
            } else if (tag.length() == 0) {
                if (tags.length == 1) {
                    throw new DomainError("Must provide at least one tag");
                } else {
                    throw new DomainError("Post tags must be longer than zero characters");
                }
            }
        }

        PostsEntity out = new PostsEntity(null, author.hid, text, Database.i().getTime(), false);
        if (out.save()) {
            if (audience != null && audience.length > 0) {
                for (UsersEntity viewer : audience) {
                    if (!Database.i().makePostVisible(out.pid, viewer.hid)) {
                        // ???
                        return null;
                    }
                }
                if (!Database.i().makePostVisible(out.pid, author.hid)) {
                    // ???
                    return null;
                }
            }
            for (String tag : tags) {
                if (!out.addTag(tag)) {
                    // ???
                    return null;
                }
            }
            return out;
        } else {
            return null;
        }
    }

    public boolean save() {
        if (this.pid == null) {
            this.pid = Database.i().insertPost(this.author, this.text, this.isPublic);
            return this.pid != null;
        } else {
            return Database.i().updatePost(this.pid, this.author, this.text, this.isPublic);
        }
    }

    public static ArrayList<PostsEntity> loadUserPosts(UsersEntity me, UsersEntity target, boolean includePublic, PostsEntity last, int count) {
        return Database.i().getPostsByUser(me.hid, target.hid, includePublic, last == null ? 999999999 : last.pid, count);
    }

    public static ArrayList<PostsEntity> loadMyCircle(UsersEntity me, boolean includePublic, PostsEntity last, int count) {
        return Database.i().getPostsByCircle(me.hid, includePublic, last == null ? 999999999 : last.pid, count);
    }

    public static ArrayList<PostsEntity> searchTag(String tag, PostsEntity last, int count) {
        return Database.i().getPostsByTag(tag, last == null ? 999999999 : last.pid, count);
    }

    public static ArrayList<PostsEntity> tagSearch(String[] tagsAnd, String[] tagsOr, PostsEntity last, int count) {
        return Database.i().searchPosts(tagsAnd, tagsOr, last == null ? 999999999 : last.pid, count);
    }

    public boolean addTag(String tagText) {
        return Database.i().addPostTag(this.pid, tagText);
    }

    public boolean deleteTag(String tagText) {
        return Database.i().deletePostTag(this.pid, tagText);
    }

    public boolean delete() {
        return Database.i().deletePost(this.pid) && Database.i().deletePostVisibilities(this.pid);
    }

    public ArrayList<String> getTags() {
        return Database.i().getPostTags(this.pid);
    }

    public UsersEntity getAuthor() {
        return Database.i().getUser(this.author);
    }
}
