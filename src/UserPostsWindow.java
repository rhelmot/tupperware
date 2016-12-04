import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public class UserPostsWindow extends PostsWindow {
    private UsersEntity user;

    public UserPostsWindow(Tupperware root, UsersEntity user) {
        super(root, "Posts by " + user.name);
        this.user = user;
    }

    @Override
    protected ArrayList<PostsEntity> loadPosts(PostsEntity after, int count) {
        return PostsEntity.loadUserPosts(user, root.currentUser, true, after, count);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UserPostsWindow) {
            return ((UserPostsWindow)other).user.equals(user);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 422 + user.hashCode() * 3201;
    }
}
