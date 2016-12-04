import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;

import java.util.*;

public class SearchWindow extends ParametrizedWindow {
    private TextBox tagsAndKnob;
    private TextBox tagsOrKnob;

    public void construct() {
        tagsAndKnob = new TextBox(new TerminalSize(21, 1));

        tagsOrKnob = new TextBox(new TerminalSize(21, 1), String.join(", ", root.currentUser.getTags()));

        Button submitButton = new Button("Search", new Runnable() {
            @Override
            public void run() {
                try {
                    search();
                } catch (DomainError e) {
                    MessageDialog.showMessageDialog(root.gui, "Error", e.getMessage());
                }
            }
        });

        Panel panel = new Panel(new GridLayout(2))
                .addComponent(new Label("Having any of these tags"))
                .addComponent(tagsOrKnob)
                .addComponent(new Label("Having all of these tags"))
                .addComponent(tagsAndKnob)
                .addComponent(new EmptySpace(TerminalSize.ONE))
                .addComponent(submitButton);

        setComponent(panel);
    }

    private void search() throws DomainError {
        String tagsAndString = tagsAndKnob.getTextOrDefault(null);
        String tagsOrString = tagsOrKnob.getTextOrDefault(null);

        if (tagsAndString == null && tagsOrString == null) {
            throw new DomainError("Please enter a search parameter");
        }

        String[] tagsAnd = tagsAndString == null ? null : tagsAndString.split(",");
        String[] tagsOr = tagsOrString == null ? null : tagsOrString.split(",");

        if (tagsAnd != null) {
            for (int i = 0; i < tagsAnd.length; i++) {
                String tag = tagsAnd[i].trim();;
                tagsAnd[i] = tag;
                if (tag.length() > 200) {
                    throw new DomainError("Tags must be at most 200 chars");
                } else if (tag.length() == 0) {
                    throw new DomainError("Tags must be longer than zero characters");
                }
            }
        }

        if (tagsOr != null) {
            for (int i = 0; i < tagsOr.length; i++) {
                String tag = tagsOr[i].trim();;
                tagsOr[i] = tag;
                if (tag.length() > 200) {
                    throw new DomainError("Tags must be at most 200 chars");
                } else if (tag.length() == 0) {
                    throw new DomainError("Tags must be longer than zero characters");
                }
            }
        }

        root.addWindow(new SearchPostsWindow(root, tagsAnd, tagsOr));
    }

    public SearchWindow(Tupperware root) {
        super(root, "Search Public Posts");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SearchWindow;
    }

    @Override
    public int hashCode() {
        return 875499;
    }
}
