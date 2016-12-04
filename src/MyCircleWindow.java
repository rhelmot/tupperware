import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public class MyCircleWindow extends PostsWindow {
    public MyCircleWindow(Tupperware root) {
        super(root, "MyCircle");
    }

    @Override
    protected ArrayList<PostsEntity> loadPosts(PostsEntity after, int count) {
        return PostsEntity.loadMyCircle(root.currentUser, true, after, count);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MyCircleWindow;    // only one at a time
    }

    @Override
    public int hashCode() {
        return 83351;
    }
}
