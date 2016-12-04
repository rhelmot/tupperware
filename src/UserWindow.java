import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public class UserWindow extends ParametrizedWindow {
    private UsersEntity user;

    public void construct() {
        Panel panel = new Panel()
            .addComponent(new Panel(new GridLayout(2))
                .addComponent(new Label("Screen Name:"))
                .addComponent(new Label(user.screenname))
                .addComponent(new Label("Email Address:"))
                .addComponent(new Label(user.email))
                .addComponent(new Label("Phone number:"))
                .addComponent(new Label(user.phone)))
            .addComponent(new EmptySpace(TerminalSize.ONE))
            .addComponent(new Panel(new LinearLayout(Direction.HORIZONTAL))
                .addComponent(new Label("Topics:"))
                .addComponent(new TextBox(new TerminalSize(20, 1), getTagsString())
                    .setReadOnly(true)))
            .addComponent(new EmptySpace(TerminalSize.ONE));

        Panel controlPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        controlPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        controlPanel.addComponent(showPostsButton());

        if (user.equals(root.currentUser)) {
            controlPanel.addComponent(editProfileButton());
        } else if (root.currentUser.isActiveRequest(user)) {
            controlPanel.addComponent(acceptRequestButton());
            controlPanel.addComponent(declineRequestButton());
        } else if (root.currentUser.isPendingRequest(user)) {
            controlPanel.addComponent(cancelRequestButton());
        } else if (user.areFriends(root.currentUser)) {
            controlPanel.addComponent(unfriendButton());
            panel.addComponent(new Label("Friends since " + "XXX")
                .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        } else {
            controlPanel.addComponent(requestFriendsButton());
        }

        panel.addComponent(controlPanel);
        setComponent(panel);
    }

    public Button showPostsButton() {
        return new Button("Show Posts", new Runnable() {
            @Override
            public void run() {
                root.addWindow(new UserPostsWindow(root, user));
            }
        });
    }

    public Button editProfileButton() {
        return new Button("Edit Profile", new Runnable() {
            @Override
            public void run() {
                // TODO
                //root.addWindow(new EditProfileWindow(root));
            }
        });
    }

    public Button acceptRequestButton() {
        return new Button("Accept Friend Request", new Runnable() {
            @Override
            public void run() {
                user.unrequestFriends(root.currentUser);
                user.makeFriends(root.currentUser);
                construct();
            }
        });
    }

    public Button declineRequestButton() {
        return new Button("Decline Friend Request", new Runnable() {
            @Override
            public void run() {
                user.unrequestFriends(root.currentUser);
                construct();
            }
        });
    }

    public Button cancelRequestButton() {
        return new Button("Cancel Friend Request", new Runnable() {
            @Override
            public void run() {
                root.currentUser.unrequestFriends(user);
                construct();
            }
        });
    }

    public Button unfriendButton() {
        return new Button("Unfriend", new Runnable() {
            @Override
            public void run() {
                if (MessageDialog.showMessageDialog(root.gui, "Confirm", "Are you sure you want to unfriend " + user.name + "?", MessageDialogButton.Yes, MessageDialogButton.Cancel) == MessageDialogButton.Yes) {
                    root.currentUser.unfriend(user);
                    construct();
                }
            }
        });
    }

    public Button requestFriendsButton() {
        return new Button("Request Friendship", new Runnable() {
            @Override
            public void run() {
                root.currentUser.requestFriends(user);
                construct();
            }
        });
    }

    public UserWindow(Tupperware root, UsersEntity user) {
        super(root, "User: " + user.name);
        this.user = user;
    }

    private String getTagsString() {
        ArrayList<String> tags = user.getTags();
        if (tags.size() == 0) {
            return "<no tags>";
        } else {
            StringBuilder b = new StringBuilder();
            b.append('#').append(tags.get(0));
            for (String s : tags.subList(1, tags.size())) {
                b.append(", #").append(s);
            }
            return b.toString();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UserWindow) {
            return ((UserWindow)other).user.equals(user);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 6878 + user.hashCode() * 9165;
    }
}
