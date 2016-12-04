import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

import java.util.*;

public abstract class PostsWindow extends ParametrizedWindow {
    protected abstract ArrayList<PostsEntity> loadPosts(PostsEntity after, int count);

    private Button loadMoreDummy;
    private ScrollingPanel mainPanel;
    private PostsEntity lastLoaded = null;

    public PostsWindow(Tupperware parent, String title) {
        super(parent, title, new TerminalSize(53, 20));
    } 

    public void construct() {
        loadMoreDummy = new Button("Load More Posts", new Runnable() {
            @Override
            public void run() {
                mainPanel.previousFocus();
                loadMore(7);
                mainPanel.nextFocus();
            }
        });
        mainPanel = new ScrollingPanel();
        mainPanel.addComponent(loadMoreDummy);
        loadMore(7);
        setComponent(mainPanel);
        mainPanel.scrollToTop();
    }

    private void loadMore(int count) {
        mainPanel.removeComponent(loadMoreDummy);
        List<PostsEntity> result = loadPosts(lastLoaded, count);
        for (PostsEntity p : result) {
            renderPost(p);
            lastLoaded = p;
        }
        if (result.size() != count) {
            loadMoreDummy.setLabel("No more posts to load!");
        }
        mainPanel.addComponent(loadMoreDummy);
        // TODO: set focus correctly
    }

    public void renderPost(final PostsEntity post) {
        final Panel a = new Panel(new LinearLayout(Direction.HORIZONTAL))
            .addComponent(getUserButton(post.getAuthor()))
            .addComponent(new Label(" - Posted on " + post.timestamp.toString()));

        final WordWrapTextBox b = new WordWrapTextBox(post.text);

        mainPanel
            .addComponent(a)
            .addComponent(b);
            //.addComponent(new EmptySpace(TerminalSize.ONE));

        String tagString = getTagString(post);
        final TextBox c = tagString.equals("<no tags>") ? null : new TextBox(new TerminalSize(50000, 1), getTagString(post)).setEnabled(false);

        if (c != null) {
            mainPanel.addComponent(c);
        }

        final EmptySpace e = new EmptySpace(new TerminalSize(1, 2));
        final Button d = post.author != root.currentUser.hid ? null : new Button("Delete Post");
        if (d != null) {
            d.addListener(new Button.Listener() {
                 public void onTriggered(Button button) {
                     if (MessageDialog.showMessageDialog(root.gui, "Confirm", "Are you sure you want to delete this post?", MessageDialogButton.Yes, MessageDialogButton.Cancel) == MessageDialogButton.Yes) {
                         post.delete();
                         mainPanel.nextFocus();
                         mainPanel.removeComponent(a);
                         mainPanel.removeComponent(b);
                         if (c != null)
                             mainPanel.removeComponent(c);
                         if (d != null)
                             mainPanel.removeComponent(d);
                         mainPanel.removeComponent(e);
                     }
                 }
            });
            mainPanel.addComponent(d);
        }

        mainPanel.addComponent(e);
    }

    private String getTagString(PostsEntity post) {
        ArrayList<String> tags = post.getTags();
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


    private Button getUserButton(final UsersEntity u) {
        return new Button(" " + u.name, new Runnable() {
            @Override
            public void run() {
                root.addWindow(new UserWindow(root, u));
            }
        }).setRenderer(new Button.FlatButtonRenderer());
    }
}
