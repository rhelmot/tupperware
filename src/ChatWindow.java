import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;
import java.text.SimpleDateFormat;

public abstract class ChatWindow extends ParametrizedWindow {
    protected abstract void constructControls(Panel controlPanel);
    protected abstract ChatsEntity submitMessage(String message) throws DomainError;
    protected abstract List<ChatsEntity> loadChats(ChatsEntity after, int count);
    protected List<ChatsEntity> currentChats;
    protected Map<Integer, UsersEntity> userCache;
    protected boolean loadedAll;

    private WordWrapTextBox logBox;
    private ChatsEntity lastLoaded;

    public void construct() {
        userCache = new HashMap<Integer, UsersEntity>();
        userCache.put(root.currentUser.hid, root.currentUser);

        currentChats = new LinkedList<ChatsEntity>();
        lastLoaded = null;
        loadedAll = false;

        logBox = new WordWrapTextBox("<loading more data...>", WordWrapTextBox.Style.MULTI_LINE)
            .setEnabled(false)
            .addTopListener(new Runnable() {
                public void run() {
                    int loaded = loadMore(7);
                    if (loaded > 0) {
                        logBox.moveToLine(loaded); // loadmore is 0, first new is 1, first old is 1+n, last new is n
                    }
                }
            });

        loadMore(7);

        final ChatLineTextBox lineBox = new ChatLineTextBox(this);

        Button deleteButton = new Button("Delete Message", new Runnable() {
            public void run() {
                ChatsEntity toDelete = root.genericPicker(currentChats, "Select a chat to delete", "No messages to delete!");
                if (toDelete != null) {
                    if (!toDelete.delete(root.currentUser)) {
                        MessageDialog.showMessageDialog(root.gui, "Error", "Deletion failed...?");
                    } else {
                        int index = currentChats.indexOf(toDelete);
                        currentChats.remove(toDelete);
                        logBox.removeLine(index + 1);
                    }
                }
            }
        });

        Panel controlPanel = new Panel()
            .addComponent(lineBox, LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
            .addComponent(deleteButton);

        constructControls(controlPanel);

        Panel panel = new Panel(new BorderLayout())
            .addComponent(logBox, BorderLayout.Location.CENTER)
            .addComponent(controlPanel, BorderLayout.Location.BOTTOM);

        setComponent(panel);
        logBox.setSize(new TerminalSize(30, 7));
        logBox.pendingMoveToBottom();
    }

    private int loadMore(int count) {
        if (loadedAll) return 0;

        List<ChatsEntity> result = loadChats(lastLoaded, count);
        for (ChatsEntity c : result) {
            currentChats.add(0, c);
            logBox.addLine(renderLine(c), 1);
            lastLoaded = c;
        }
        if (result.size() < count) {
            loadedAll = true;
            logBox.setLine("<all messages loaded>", 0);
        }
        return result.size();
    }

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a");
    private String renderLine(ChatsEntity c) {
        return new StringBuilder()
            .append("[")
            .append(dateFormatter.format(c.timestamp))
            .append("] ")
            .append(getUser(c.author).screenname)
            .append(": ")
            .append(c.text).toString();
    }

    protected UsersEntity getUser(int hid) {
        if (userCache.containsKey(hid)) {
            return userCache.get(hid);
        } else {
            UsersEntity out = UsersEntity.get(hid);
            userCache.put(hid, out);
            return out;
        }
    }

    protected void insertMessage(ChatsEntity c) {
        int i = 0;
        if (currentChats.size() > 0) {
            for (i = currentChats.size() - 1; i >= 0; i--) {
                if (!c.timestamp.before(currentChats.get(i).timestamp)) {
                    i++;
                    break;
                }
            }
        }
        currentChats.add(i, c);
        logBox.addLine(renderLine(c), i+1);
    }

    public ChatWindow(Tupperware root, String title) {
        super(root, title, new TerminalSize(50, 9));
    }

    private static class ChatLineTextBox extends TextBox {
        private ChatWindow parent;
        public ChatLineTextBox(ChatWindow parent) {
            super();
            this.parent = parent;
        }

        @Override
        public Interactable.Result handleKeyStroke(KeyStroke stroke) {
            switch (stroke.getKeyType()) {
                case Enter:
                    if (!getText().equals("")) {
                        try {
                            ChatsEntity c = parent.submitMessage(getText());
                            if (c != null) {
                                setText("");
                                parent.insertMessage(c);
                                parent.logBox.moveToBottom();
                            } else {
                                parent.root.error("Unknown chat error!!!!!!!!!!!");
                            }
                        } catch (DomainError e) {
                            parent.root.error(e.getMessage());
                        }
                    }
                    return Interactable.Result.HANDLED;

                case ArrowUp:
                case ArrowDown:
                case PageUp:
                case PageDown:
                case Home:
                case End:
                    if (stroke.isCtrlDown() || stroke.isAltDown()) {
                        return Interactable.Result.UNHANDLED;
                    } else if (parent.logBox.handleKeyStroke(stroke) == Result.HANDLED) {
                        return Result.HANDLED;
                    } else {
                        return super.handleKeyStroke(stroke);
                    }

                default:
                    return super.handleKeyStroke(stroke);
            }
        }
    }
}
