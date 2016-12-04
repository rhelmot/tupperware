import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public class ChatGroupWindow extends ChatWindow {
    private ChatGroupsEntity group;
    private boolean isOwner;

    public ChatGroupWindow(Tupperware root, ChatGroupsEntity group) {
        super(root, "ChatGroup " + group.groupName);
        this.group = group;
    }

    protected void constructControls(Panel panel) {
        if (group.getOwner().equals(root.currentUser)) {
            panel.addComponent(new Button("Edit group settings", new Runnable() {
                public void run() {
                    root.addWindow(new EditChatGroupWindow(root, group));
                }
            }));
        } else {
            /*  not yet implemented...
            panel.addComponent(new Button("Leave Group", new Runnable() {
                public void run() {
                    if (MessageDialog.showMessageDialog(root.gui, "Confirm", "Are you sure you want to leave this group?", MessageDialogButton.Yes, MessageDialogButton.Cancel) == MessageDialogButton.Yes) {

                    }
                }
            }));
            */
        }
        panel.addComponent(new Button("Invite User", new Runnable() {
            public void run() {
                UsersEntity picked = root.friendsPicker("Invite friend to join " + group.groupName);
                if (picked != null) {
                    if (!group.inviteUser(picked, root.currentUser)) {
                        root.error("This user is already in this group, or at least has already been invited");
                    }
                }
            }
        }));
    }

    protected ChatsEntity submitMessage(String message) throws DomainError {
        return ChatsEntity.create(root.currentUser, message, group);
    }

    protected List<ChatsEntity> loadChats(ChatsEntity after, int count) {
        return ChatsEntity.getGroupChats(root.currentUser, group, after, count);
    }

    public boolean equals(Object other) {
        if (other instanceof ChatGroupWindow) {
            return group.equals(((ChatGroupWindow)other).group);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 889995 + group.hashCode() * 443;
    }
}
