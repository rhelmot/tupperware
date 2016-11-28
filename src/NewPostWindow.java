import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.regex.Pattern;
import java.util.ArrayList;

public class NewPostWindow extends ParametrizedWindow {
    private TextBox postBodyKnob = null;
    private TextBox postTagsKnob = null;
    private CheckBox isPublicKnob = null;
    private TextBox postAudienceView = null;
    private ArrayList<UsersEntity> postAudienceBacker = null;

    public void construct() {
        Button addFriendButton = new Button("Add friend", new Runnable() {
            @Override
            public void run() {
                UsersEntity selected = root.friendsPicker("Add to the audience");
                if (selected != null && !postAudienceBacker.contains(selected)) {
                    postAudienceBacker.add(selected);
                    updateAudienceView();
                }
            }
        });

        Button removeFriendButton = new Button("Remove friend", new Runnable() {
            @Override
            public void run() {
                UsersEntity selected = root.genericPicker(postAudienceBacker, "Remove from the audience", "There's nobody to remove!");
                if (selected != null) {
                    postAudienceBacker.remove(selected);
                    updateAudienceView();
                }
            }
        });

        postBodyKnob = new TextBox(new TerminalSize(10, 6));

        postTagsKnob = new TextBox(new TerminalSize(50000, 1))  // hack to fill a horizontal panel
            .setValidationPattern(Pattern.compile("[^,]{1,200}(,[ ]*[^,]{0,200})*"));

        isPublicKnob = new CheckBox()
            .addListener(new CheckBox.Listener() {
                @Override
                public void onStatusChanged(boolean checked) {
                    if (checked) {
                        postAudienceView.setText("<entire world>");
                        addFriendButton.setEnabled(false);
                        removeFriendButton.setEnabled(false);
                        postAudienceView.setEnabled(false);
                    } else {
                        updateAudienceView();
                        addFriendButton.setEnabled(true);
                        removeFriendButton.setEnabled(true);
                        postAudienceView.setEnabled(true);
                    }
                }
            });

        postAudienceView = new TextBox("<all friends>")
            .setReadOnly(true);

        postAudienceBacker = new ArrayList<UsersEntity>();

        Button submitPostButton = new Button("Submit Post", new Runnable() {
            @Override
            public void run() {

            }
        });

        Panel panel = new Panel(new BorderLayout())
            .addComponent(new Label("Post Body:")
                    .setLayoutData(BorderLayout.Location.TOP))
            .addComponent(postBodyKnob
                .setLayoutData(BorderLayout.Location.CENTER))
            .addComponent(new Panel()
                .addComponent(new EmptySpace(TerminalSize.ONE))
                .addComponent(new Panel()
                        .setLayoutManager(new LinearLayout(Direction.HORIZONTAL))
                        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill))
                        .addComponent(new Label("Post Tags: "))
                        .addComponent(postTagsKnob))
                .addComponent(new Panel()
                        .setLayoutManager(new LinearLayout(Direction.HORIZONTAL))
                        .addComponent(new Label("Post Publicly: "))
                        .addComponent(isPublicKnob))
                .addComponent(new Panel()
                        .setLayoutManager(new LinearLayout(Direction.HORIZONTAL))
                        .addComponent(new Label("Post Audience: "))
                        .addComponent(addFriendButton)
                        .addComponent(removeFriendButton))
                .addComponent(postAudienceView
                        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)))
                .addComponent(new EmptySpace(TerminalSize.ONE))
                .addComponent(submitPostButton
                    .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))));

        setComponent(panel);
    }

    private void updateAudienceView() {
        if (postAudienceBacker.size() == 0) {
            postAudienceView.setText("<all friends>");
        } else {
            StringBuilder s = new StringBuilder();
            s.append(postAudienceBacker.get(0).name);
            for (UsersEntity u : postAudienceBacker.subList(1, postAudienceBacker.size())) {
                s.append(", ").append(u.name);
            }
            postAudienceView.setText(s.toString());
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this;       // as many new post windows as you like!
    }

    public int hashCode() {
        return 4562;
    }

    public NewPostWindow(Tupperware root) {
        super(root, "New Post", new TerminalSize(50, 15));
    }
}
