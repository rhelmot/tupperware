import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public class ChatOverviewWindow extends ParametrizedWindow {
    public void construct() {
        ScrollingPanel panel = new ScrollingPanel();

        ArrayList<ChatGroupsEntity> pending = root.currentUser.getPendingChatGroups();
        if (pending.size() > 0) {
            panel.addComponent(new Label("ChatGroup invitations"));
            for (ChatGroupsEntity c : pending) {
                panel.addComponent(getPendingButton(c));
            }
            panel.addComponent(new EmptySpace(TerminalSize.ONE));
        }

        panel.addComponent(new Button("Create ChatGroup", new Runnable() {
            @Override
            public void run() {
                root.addWindow(new EditChatGroupWindow(root, null));
            }
        }));

        panel.addComponent(new Button("New Private Message", new Runnable() {
            @Override
            public void run() {
                UsersEntity selected = root.friendsPicker("Send new message to...");
                if (selected != null) {
                    root.addWindow(new PrivateMessageWindow(root, selected));
                }
            }
        }));

        panel.addComponent(new EmptySpace(TerminalSize.ONE));

        ArrayList<ChatGroupsEntity> groups = root.currentUser.getChatGroups();
        if (groups.size() > 0) {
            panel.addComponent(new Label("ChatGroups"));
            for (ChatGroupsEntity c : groups) {
                panel.addComponent(getChatGroupButton(c));
            }
            panel.addComponent(new EmptySpace(TerminalSize.ONE));
        }

        ArrayList<UsersEntity> privateMessages = root.currentUser.getPrivateMessages();
        if (privateMessages.size() > 0) {
            panel.addComponent(new Label("Private Messages"));
            for (UsersEntity u : privateMessages) {
                panel.addComponent(getPrivateMessageButton(u));
            }
            panel.addComponent(new EmptySpace(TerminalSize.ONE));
        }

        setComponent(panel);
        panel.scrollToTop();
    }

    private Button getPendingButton(final ChatGroupsEntity c) {
        return new Button(" " + c.groupName, new Runnable() {
            @Override
            public void run() {
                if (MessageDialog.showMessageDialog(root.gui, "ChatGroup invitation", "Do you want to join the group \"" + c.groupName + "\"?", MessageDialogButton.Yes, MessageDialogButton.No) == MessageDialogButton.Yes) {
                    c.acceptInvitation(root.currentUser);
                    construct();
                    root.addWindow(new ChatGroupWindow(root, c));
                }
            }
        }).setRenderer(new Button.FlatButtonRenderer());
    }

    private Button getChatGroupButton(final ChatGroupsEntity c) {
        return new Button(" " + c.groupName, new Runnable() {
            @Override
            public void run() {
                root.addWindow(new ChatGroupWindow(root, c));
            }
        }).setRenderer(new Button.FlatButtonRenderer());
    }

    private Button getPrivateMessageButton(final UsersEntity u) {
        return new Button(" " + u.name, new Runnable() {
            @Override
            public void run() {
                root.addWindow(new PrivateMessageWindow(root, u));
            }
        }).setRenderer(new Button.FlatButtonRenderer());
    }

    public ChatOverviewWindow(Tupperware root) {
        super(root, "Chat", new TerminalSize(22, 20));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ChatOverviewWindow;
    }

    @Override
    public int hashCode() {
        return 11837;
    }
}
