import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;
import java.util.regex.Pattern;

public class EditChatGroupWindow extends ParametrizedWindow {
    private ChatGroupsEntity group;

    public void construct() {
        final TextBox nameKnob = new TextBox(new TerminalSize(21, 1))
            .setText(group == null ? "" : group.groupName)
            .setValidationPattern(Pattern.compile(".{0,20}"));

        final TextBox durationKnob = new TextBox(new TerminalSize(21, 1))
            .setText(group == null ? "7" : new Integer(group.duration).toString())
            .setValidationPattern(Pattern.compile("[0-9]{0,10}"));

        Button submitButton = new Button("Done", new Runnable() {
            public void run() {
                Integer duration = 0;
                try {
                    duration = Integer.parseInt(durationKnob.getText());
                } catch (NumberFormatException e) {}

                try {
                    if (group == null) {
                        ChatGroupsEntity newGroup = ChatGroupsEntity.create(nameKnob.getText(), duration, root.currentUser);
                        if (newGroup != null) {
                            root.addWindow(new ChatGroupWindow(root, newGroup));
                            close();
                        } else {
                            root.error("Unknown group creation error!!!!!!!!!!!!!");
                        }
                    } else {
                        String groupName = nameKnob.getText();

                        if (groupName.length() > 20) {
                            throw new DomainError("Group name must be no more than 20 chars");
                        }
                        if (duration <= 0) {
                            throw new DomainError("Group duration must be a postitve number");
                        }

                        new ChatGroupsEntity(group.gid, groupName, duration).save();
                        close();
                    }
                } catch (DomainError e) {
                    root.error(e.getMessage());
                }
            }
        });
        Panel panel = new Panel(new GridLayout(2))
            .addComponent(new Label("Group Name:"))
            .addComponent(nameKnob)
            .addComponent(new Label("Message lifetime (days):"))
            .addComponent(durationKnob)
            .addComponent(new EmptySpace())
            .addComponent(submitButton);

        setComponent(panel);
    }

    public EditChatGroupWindow(Tupperware root, ChatGroupsEntity group) {
        super(root, group == null ? "New ChatGroup" : "Edit ChatGroup");
        this.group = group;
    }

    public boolean equals(Object other) {
        if (group == null) return this == other;
        if (other instanceof EditChatGroupWindow) {
            return group.equals(((EditChatGroupWindow)other).group);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 8888881 + (group == null ? 0 : group.hashCode() * 404);
    }
}
