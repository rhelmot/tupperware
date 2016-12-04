import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;
import java.text.*;
import java.util.*;
import java.time.temporal.ChronoUnit;

public class ManagerWindow extends ParametrizedWindow {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMM d yyyy h:mm:ss a");
    private Date currentTime;
    private ReportsEntity newestReport;
    public void construct() {
        newestReport = null;
        currentTime = Database.i().getTime();

        final TextBox currentTimeKnob = new TextBox(new TerminalSize(41, 1), dateFormat.format(currentTime));
        final CheckBox dynamicTimeKnob = new CheckBox("Time is an Illusion").setChecked(!Database.i().isTimeMoving());

        Button updateTimeButton = new Button("Change System Time", new Runnable() {
            public void run() {
                try {
                    Date newTime = dateFormat.parse(currentTimeKnob.getText());
                    if (newTime.before(currentTime)) {
                        root.error("No going backwards in time!!!!!!");
                    } else {
                        for (Date rDate = plusWeek(newestReport.timestamp); !rDate.after(newTime); rDate = plusWeek(rDate)) {
                            Database.i().setTimeStatic(rDate);
                            ReportsEntity.generate().save();
                        }
                        if (dynamicTimeKnob.isChecked()) {
                            Database.i().setTimeStatic(newTime);
                        } else {
                            Database.i().setTimeDynamic(newTime);
                        }
                        Database.i().collectGarbage();
                        construct();
                    }
                } catch (ParseException e) {
                    MessageDialog.showMessageDialog(root.gui, "Error", "Please format your time correctly (MMMMM d yyyy h:mm:ss a)");
                }
            }
        });
        Button currentReportButton = new Button("View current content status", new Runnable() {
            public void run() {
                root.addWindow(new ReportWindow(root, ReportsEntity.generate()));
            }
        });

        Panel panel = new Panel()
            .addComponent(currentTimeKnob)
            .addComponent(dynamicTimeKnob)
            .addComponent(updateTimeButton)
            .addComponent(new EmptySpace(TerminalSize.ONE))
            .addComponent(currentReportButton);

        for (ReportsEntity r : Database.i().getReports()) {
            if (newestReport == null) {
                newestReport = r;
            }
            panel.addComponent(getReportButton(r));
        }

        setComponent(panel);
    }

    private static Date plusWeek(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_YEAR, 7);
        return c.getTime();
    }

    private Button getReportButton(ReportsEntity r) {
        return new Button("View content status report for " + dateFormat.format(r.timestamp), new Runnable() {
            public void run() {
                root.addWindow(new ReportWindow(root, r));
            }
        });
    }

    public ManagerWindow(Tupperware root) {
        super(root, "Manage Tupperware");
    }

    public boolean equals(Object other) {
        return other instanceof ManagerWindow;
    }

    public int hashCode() {
        return 583739;
    }
}
