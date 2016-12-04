import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;

import java.util.*;
import java.util.regex.Pattern;

public class FriendSearchWindow extends ParametrizedWindow {
    private TextBox emailKnob;
    private TextBox phoneKnob;
    private TextBox nameKnob;
    private TextBox screenNameKnob;
    private TextBox tagsAndKnob;
    private TextBox tagsOrKnob;
    private TextBox lastPostKnob;
    private TextBox postCountKnob;
    private ScrollingPanel resultsPanel;

    public void construct() {
        emailKnob = new TextBox(new TerminalSize(21 ,1))
            .setValidationPattern(Pattern.compile("[^ ]{0,20}"));

        phoneKnob = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile("[0123456789]{0,10}"));

        nameKnob = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"));

        screenNameKnob = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"));

        tagsAndKnob = new TextBox(new TerminalSize(21, 1));

        tagsOrKnob = new TextBox(new TerminalSize(21, 1));

        lastPostKnob = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile("[0123456789]{0,10}"));

        postCountKnob = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile("[0123456789]{0,10}"));

        resultsPanel = new ScrollingPanel();

        Button submitButton = new Button("Search", new Runnable() {
            @Override
            public void run() {
                resultsPanel.removeAllComponents();
                try {
                    ArrayList<UsersEntity> results = search();
                    if (results.size() == 0) {
                        resultsPanel.addComponent(new Label("No users found"));
                    } else {
                        resultsPanel.addComponent(new Label("Results:"));
                        for (UsersEntity u : results) {
                            resultsPanel.addComponent(getUserButton(u));
                        }
                    }
                } catch (DomainError e) {
                    MessageDialog.showMessageDialog(root.gui, "Error", e.getMessage());
                }
            }
        });

        Panel panel = new Panel(new BorderLayout())
                .addComponent(new Panel(new GridLayout(2))
                        .setLayoutData(BorderLayout.Location.TOP)
                        .addComponent(new Label("Email address"))
                        .addComponent(emailKnob)
                        .addComponent(new Label("Phone number"))
                        .addComponent(phoneKnob)
                        .addComponent(new Label("Name"))
                        .addComponent(nameKnob)
                        .addComponent(new Label("Screen Name"))
                        .addComponent(screenNameKnob)
                        .addComponent(new Label("Having all of these tags"))
                        .addComponent(tagsAndKnob)
                        .addComponent(new Label("Having any of these tags"))
                        .addComponent(tagsOrKnob)
                        .addComponent(new Label("Last post at most how many days ago"))
                        .addComponent(lastPostKnob)
                        .addComponent(new Label("At least this many posts in last 7 days"))
                        .addComponent(postCountKnob)
                        .addComponent(new EmptySpace(TerminalSize.ONE))
                        .addComponent(submitButton))
                .addComponent(resultsPanel
                        .setLayoutData(BorderLayout.Location.CENTER));

        setComponent(panel);
    }

    private ArrayList<UsersEntity> search() throws DomainError {
        String email = emailKnob.getTextOrDefault(null);
        String phone = phoneKnob.getTextOrDefault(null);
        String name = nameKnob.getTextOrDefault(null);
        String screenname = screenNameKnob.getTextOrDefault(null);
        String tagsAndString = tagsAndKnob.getTextOrDefault(null);
        String tagsOrString = tagsOrKnob.getTextOrDefault(null);
        String lastPostString = lastPostKnob.getTextOrDefault(null);
        String postCountString = postCountKnob.getTextOrDefault(null);

        String[] tagsAnd = tagsAndString == null ? null : tagsAndString.split(",");
        String[] tagsOr = tagsOrString == null ? null : tagsOrString.split(",");
        Integer lastPost = null;
        Integer postCount = null;
        try {
            if (lastPostString != null) lastPost = Integer.parseInt(lastPostString);
            if (postCountString != null) postCount = Integer.parseInt(postCountString);
        } catch (NumberFormatException e) {} // should be impossible

        return UsersEntity.search(email, phone, name, screenname, tagsAnd, tagsOr, lastPost, postCount);
    }

    public Button getUserButton(final UsersEntity u) {
        return new Button(" " + u.name, new Runnable() {
            @Override
            public void run() {
                root.addWindow(new UserWindow(root, u));
            }
        }).setRenderer(new Button.FlatButtonRenderer());
    }


    public FriendSearchWindow(Tupperware root) {
        super(root, "Search for Users", new TerminalSize(60, 22));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FriendSearchWindow;
    }

    @Override
    public int hashCode() {
        return 42;
    }
}
