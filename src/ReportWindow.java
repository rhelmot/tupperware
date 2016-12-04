import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;

import java.util.Map;

public class ReportWindow extends ParametrizedWindow {
    private ReportsEntity report;

    public void construct() {
        Panel panel = new Panel(new GridLayout(2))
            .addComponent(new Label("New Messages:"))
            .addComponent(new Label(new Integer(report.newMessages).toString()))
            .addComponent(new Label("New Messages Reads:"))
            .addComponent(new Label(new Integer(report.newMessageReads).toString()))
            .addComponent(new Label("Average Message Reads:"))
            .addComponent(new Label(new Float(report.avgMessageReads).toString()))
            .addComponent(new Label("Average Message Reads (new posts):"))
            .addComponent(new Label(new Float(report.avgNewMessageReads).toString()))
            .addComponent(new Label("Top posts:"))
            .addComponent(makePostButton(report.topPost1))
            .addComponent(new EmptySpace())
            .addComponent(makePostButton(report.topPost2))
            .addComponent(new EmptySpace())
            .addComponent(makePostButton(report.topPost3))
            .addComponent(new Label("Top active users:"))
            .addComponent(makeUserButton(report.topUser1))
            .addComponent(new EmptySpace())
            .addComponent(makeUserButton(report.topUser2))
            .addComponent(new EmptySpace())
            .addComponent(makeUserButton(report.topUser3))
            .addComponent(new Label("Inactive users:"))
            .addComponent(new Label(new Integer(report.inactiveUserCount).toString()))
            .addComponent(new EmptySpace())
            .addComponent(new EmptySpace())
            .addComponent(new Label("Top posts for each tag"), GridLayout.createHorizontallyFilledLayoutData(2));

        for (Map.Entry<String, PostsEntity> entry : report.tagData.entrySet()) {
            panel.addComponent(new Label("#" + entry.getKey()));
            panel.addComponent(makePostButton(entry.getValue()));
        }

        setComponent(panel);
    }

    public Component makePostButton(PostsEntity post) {
        if (post == null) {
            return new Label("<none>");
        } else {
            return new Label(post.getAuthor().name + ": " + (post.text.length() > 30 ? post.text.substring(0, 37) + "..." : post.text));
        }
    }

    public Component makeUserButton(final UsersEntity user) {
        if (user == null) {
            return new Label("<none>");
        } else {
            /*
            return new Button(user.name, new Runnable() {
                public void run() {

                }
            }).setRenderer(new Button.FlatButtonRenderer());
            */
            return new Label(user.name);
        }
    }

    public ReportWindow(Tupperware root, ReportsEntity report) {
        super(root, "Content Report for " + (report.tid == null ? "Right Now" : report.timestamp.toString()));
        this.report = report;
    }

    public boolean equals(Object other) {
        if (other instanceof ReportWindow) {
            ReportWindow that = (ReportWindow)other;
            if (that.report.tid == null || this.report.tid == null) {
                return that.report.tid == null && this.report.tid == null;
            } else {
                return that.report.tid.equals(this.report.tid);
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 9245 + report.tid.hashCode() * 222217;
    }
}
