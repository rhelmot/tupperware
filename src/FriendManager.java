import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;

import java.util.*;

public class FriendManager extends ParametrizedWindow {
    private List<UsersEntity> friends;
    private List<UsersEntity> activeRequests;
    private List<UsersEntity> pendingRequests;
    public void construct() {
        friends = root.currentUser.getFriends();
        activeRequests = root.currentUser.getActiveRequests();
        pendingRequests = root.currentUser.getPendingRequests();

        final ScrollingPanel panel = new ScrollingPanel();

        panel.addComponent(new Label("New requests:"));

        if (activeRequests.size() == 0) {
            panel.addComponent(new Label("<empty>"));
        }

        for (UsersEntity u : activeRequests) {
            panel.addComponent(getUserButton(u));
        }

        panel.addComponent(new Button("+ Add Friends", new Runnable() {
            @Override
            public void run() {
                root.addWindow(new FriendSearchWindow(root));
            }
        }).setRenderer(new Button.FlatButtonRenderer()));

        panel.addComponent(new EmptySpace(TerminalSize.ONE));

        panel.addComponent(new Label("Pending requests:"));

        if (pendingRequests.size() == 0) {
            panel.addComponent(new Label("<empty>"));
        }

        for (UsersEntity u : pendingRequests) {
            panel.addComponent(getUserButton(u));
        }

        panel.addComponent(new EmptySpace(TerminalSize.ONE));

        panel.addComponent(new Label("Friends:"));

        if (friends.size() == 0) {
            panel.addComponent(new Label("<empty>"));
        }

        for (UsersEntity u : friends) {
            panel.addComponent(getUserButton(u));
        }

        setComponent(panel);
        panel.scrollToTop();
    }

    public Button getUserButton(final UsersEntity u) {
        return new Button(" " + u.name, new Runnable() {
            @Override
            public void run() {
                root.addWindow(new UserWindow(root, u));
            }
        }).setRenderer(new Button.FlatButtonRenderer());
    }

    public FriendManager(Tupperware root) {
        super(root, "Friend Manager", new TerminalSize(20, 15));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FriendManager;  // There can ONLY BE ONE
    }

    @Override
    public int hashCode() {
        return 876546;
    }
}
