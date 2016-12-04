import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public class PrivateMessageWindow extends ChatWindow {
    private UsersEntity user;
    public PrivateMessageWindow(Tupperware root, UsersEntity user) {
        super(root, "Chat with " + user.name);
        this.user = user;
    }

    protected void constructControls(Panel panel) {
        panel.addComponent(new Button("Go to user profile", new Runnable() {
            public void run() {
                root.addWindow(new UserWindow(root, user));
            }
        }));
    }

    protected ChatsEntity submitMessage(String message) throws DomainError {
        return ChatsEntity.create(root.currentUser, message, user);
    }

    protected List<ChatsEntity> loadChats(ChatsEntity after, int count) {
        return ChatsEntity.getChats(root.currentUser, user, after, count);
    }

    public boolean equals(Object other) {
        if (other instanceof PrivateMessageWindow) {
            return user.equals(((PrivateMessageWindow)other).user);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 889991 + user.hashCode() * 443;
    }
}
