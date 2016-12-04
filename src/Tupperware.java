import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ExtendedTerminal;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.PrintWriter;

public class Tupperware {
    public static void main(String[] args) throws IOException {
        try {
            Tupperware t = new Tupperware();

            String token = Tupperware.readToken();
            if (token != null) {
                SessionsEntity s = SessionsEntity.lookup(token);
                if (s != null) {
                    t.currentSession = s;
                    t.currentUser = s.getUser();
                }
            }

            if (t.currentSession == null) {
                t.welcome();
            }

            if (t.currentSession != null) {
                Tupperware.writeToken(t.currentSession.token);
                t.mainMenu();
            }
        } finally {
            Database.cleanup();
        }
    }

    private Terminal terminal;
    private Screen screen;
    public MultiWindowTextGUI gui;

    private ArrayList<ParametrizedWindow> activeWindows = new ArrayList<ParametrizedWindow>();

    public SessionsEntity currentSession;
    public UsersEntity currentUser;

    public Tupperware() {
        try {
            terminal = new DefaultTerminalFactory().createTerminal();
            //terminal.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE);
            screen = new TerminalScreen(terminal);
            screen.startScreen();
            gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void close() {
        try {
            screen.stopScreen();
        } catch (IOException e) { }
    }

    private void welcome() {
        final BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        new Button("Login", new Runnable() {
            @Override
            public void run() {
                login();
                if (currentSession != null) {
                    window.close();
                }
            }
        }).addTo(panel);

        new Button("Register", new Runnable() {
            @Override
            public void run() {
                register();
                if (currentSession != null) {
                    window.close();
                }
            }
        }).addTo(panel);

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    private void register() {
        final BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        final Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        new Label("Name").addTo(panel);
        final TextBox nameBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Email").addTo(panel);
        final TextBox emailBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Password").addTo(panel);
        final TextBox passwordBox = new TextBox(new TerminalSize(21, 1))
            .setMask('*')
            .setValidationPattern(Pattern.compile(".{0,10}"))
            .addTo(panel);
        new Label("Confirm Password").addTo(panel);
        final TextBox password2Box = new TextBox(new TerminalSize(21, 1))
            .setMask('*')
            .setValidationPattern(Pattern.compile(".{0,10}"))
            .addTo(panel);
        new Label("Phone #").addTo(panel);
        final TextBox phoneBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile("[0-9]{0,10}"))
            .addTo(panel);
        new Label("Screen Name").addTo(panel);
        final TextBox screenNameBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Topic Words (comma separated)").addTo(panel);
        final TextBox topicWordsBox = new TextBox(new TerminalSize(21, 1))
            .addTo(panel);

        new EmptySpace(new TerminalSize(0,0)).addTo(panel);
        new Button("Register", new Runnable() {
            private Label message = null;
            @Override
            public void run() {
                String result = process();
                if (result == null) {
                    window.close();
                } else {
                    if (message == null) {
                        new EmptySpace(new TerminalSize(0,0)).addTo(panel);
                        message = new Label(result).addTo(panel);
                    } else {
                        message.setText(result);
                    }
                }
            }

            private String process() {
                if (nameBox.getText().length() == 0 ||
                        emailBox.getText().length() == 0 ||
                        passwordBox.getText().length() == 0 ||
                        password2Box.getText().length() == 0 ||
                        phoneBox.getText().length() == 0 ||
                        screenNameBox.getText().length() == 0) {
                    return "Please fill all fields!";
                }
                if (!passwordBox.getText().equals(password2Box.getText())) {
                    return "Password don't match!";
                }

                HashSet<String> unique = new HashSet<String>();
                String[] tagstring = topicWordsBox.getText().split(",");
                for (int i = 0; i < tagstring.length; i++) {
                    String tag = tagstring[i].trim();;
                    tagstring[i] = tag;
                    if (tag.length() > 200) {
                        return "Topic words must be at most 200 chars";
                    } else if (tag.length() == 0) {
                        if (tagstring.length == 0) {
                            return "Must provide at least one topic word";
                        } else {
                            return "Tags must be longer than zero characters";
                        }
                    }
                    if (!unique.add(tag)) {
                        return "No duplicate topic words allowed!";
                    }
                }

                try {
                    UsersEntity u = UsersEntity.create(
                            nameBox.getText(),
                            emailBox.getText(),
                            passwordBox.getText(),
                            phoneBox.getText(),
                            screenNameBox.getText(),
                            false);
                    if (u == null) {
                        return "Email already in use!";
                    }

                    for (String tag : tagstring) {
                        u.addTag(tag);
                    }

                    currentUser = u;
                    currentSession = SessionsEntity.create(u, false);
                    return null;
                } catch (DomainError e) {
                    return e.getMessage();
                }
            }
        }).addTo(panel);

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    private void login() {
        final BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        final Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        new Label("Email").addTo(panel);
        final TextBox emailBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Password").addTo(panel);
        final TextBox passwordBox = new TextBox(new TerminalSize(21, 1))
            .setMask('*')
            .setValidationPattern(Pattern.compile(".{0,10}"))
            .addTo(panel);

        new EmptySpace(new TerminalSize(0,0)).addTo(panel);
        new Button("Login", new Runnable() {
            private Label message = null;
            @Override
            public void run() {
                String result = process();
                if (result == null) {
                    window.close();
                } else {
                    if (message == null) {
                        new EmptySpace(new TerminalSize(0,0)).addTo(panel);
                        message = new Label(result).addTo(panel);
                    } else {
                        message.setText(result);
                    }
                }
            }

            private String process() {
                if (emailBox.getText().length() == 0 || passwordBox.getText().length() == 00) {
                    return "Please fill all fields!";
                }

                UsersEntity u = UsersEntity.getByEmail(emailBox.getText());
                if (u == null) {
                    return "That email is not in use";
                }

                if (!u.checkPassword(passwordBox.getText())) {
                    return "Incorrect password";
                }

                currentUser = u;
                currentSession = SessionsEntity.create(u, false);
                return null;
            }
        }).addTo(panel);

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    public static String readToken() {
        try {
            String token = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".tupperwaretoken")));
            return token;
        } catch (IOException e) {
            return null;
        }
    }

    public static void writeToken(String token) {
        try {
            PrintWriter out = new PrintWriter(Paths.get(System.getProperty("user.home"), ".tupperwaretoken").toString());
            out.print(token);
            out.close();
        } catch (IOException e) {}
    }

    public void logout() {
        currentSession.delete();
        try {
            Files.delete(Paths.get(System.getProperty("user.home"), ".tupperwaretoken"));
        } catch (Exception e) {}
    }

    public void mainMenu() {
        final Tupperware root = this;
        final BasicWindow window = new DynamicWindow("", true, null, true);
        //window.setHints(Arrays.asList(Window.Hint.CENTERED));

        final Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(6));

        new Label("Tupperware™        ").addTo(panel);

        new Button("Friends", new Runnable() {
            @Override
            public void run() {
                addWindow(new FriendManager(root));
            }
        }).addTo(panel);

        new Button("New Post", new Runnable() {
            @Override
            public void run() {
                addWindow(new NewPostWindow(root));
            }
        }).addTo(panel);

        new Button("MyCircle", new Runnable() {
            @Override
            public void run() {
                addWindow(new MyCircleWindow(root));
            }
        }).addTo(panel);

        new Button("My Profile", new Runnable() {
            @Override
            public void run() {
                addWindow(new UserWindow(root, currentUser));
            }
        }).addTo(panel);

        new Button("Chat", new Runnable() {
            @Override
            public void run() {
                addWindow(new ChatOverviewWindow(root));
            }
        }).addTo(panel);

        // -------------------------------
        new EmptySpace(TerminalSize.ONE).addTo(panel);

        new Button("Browse Posts", new Runnable() {
            @Override
            public void run() {
                addWindow(new SearchWindow(root));
            }
        }).addTo(panel);

        new Button("Logout", new Runnable() {
            @Override
            public void run() {
                logout();
                window.close();
            }
        }).addTo(panel);

        new Button("Quit", new Runnable() {
            @Override
            public void run() {
                window.close();
            }
        }).addTo(panel);

        new Button("Help", new Runnable() {
            @Override
            public void run() {
                addWindow(new HelpWindow(root));
            }
        }).addTo(panel);

        if (currentUser.isManager) {
            new Button("Manage", new Runnable() {
                @Override
                public void run() {
                    addWindow(new ManagerWindow(root));
                }
            }).addTo(panel);
        }

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    public void addWindow(ParametrizedWindow window) {
        if (!activeWindows.contains(window)) {
            window.construct();
            activeWindows.add(window);
            gui.addWindow(window);
        } else {
            for (Window w : activeWindows) {
                if (w.equals(window)) {
                    gui.setActiveWindow(w);
                    break;
                }
            }
        }
    }

    public void removeWindow(ParametrizedWindow window) {
        activeWindows.remove(window);
    }

    public <T> T genericPicker(List<T> items, String text, String failText) {
        return Tupperware.staticGenericPicker(gui, items, text, failText);
    }

    public static <T> T staticGenericPicker(WindowBasedTextGUI gui, List<T> items, String text, String failText) {
        if (items.size() == 0) {
            if (failText != null) {
                MessageDialog.showMessageDialog(gui, "Nothing to Pick!", failText);
            }
            return null;
        }

        final ArrayList<T> selected = new ArrayList<T>();
        ActionListDialogBuilder b = new ActionListDialogBuilder();
        b.setTitle("Picker");
        b.setDescription(text);

        for (T item : items) {
            b.addAction(item.toString(), new HellRun<T>(item, selected));
        }

        b.build().showDialog(gui);
        if (selected.size() == 0) {
            return null;
        } else {
            return selected.get(0);
        }
    }

    private static class HellRun<T> implements Runnable {
        private final T innerItem;
        private final ArrayList<T> selected;

        public HellRun(T item, ArrayList<T> selectedList) {
            innerItem = item;
            selected = selectedList;
        }

        @Override public void run() {
            if (selected.size() != 0) {
                selected.clear();
            }
            selected.add(innerItem);
        }
    }

    public UsersEntity friendsPicker(String text) {
        return genericPicker(currentUser.getFriends(), text, "Please make some friends first!");
    }

    public void error(String message) {
        message(message, "Error"); // thanks java
    }

    public void message(String message, String title) {
        MessageDialog.showMessageDialog(gui, title, message);
    }

    public static String lipsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse a metus vel dui dapibus feugiat. Etiam nisi mauris, blandit quis odio ut, pharetra aliquet orci.\nNam facilisis dui vehicula urna vulputate, a vestibulum ex luctus. Phasellus consectetur nisl quis tincidunt gravida. Duis fermentum, sapien ac dapibus viverra, felis eros volutpat sem, ut vehicula justo neque et nisi.\nMaecenas luctus nibh vel orci congue placerat. Praesent a felis id risus consequat scelerisque. Aenean non ultrices quam, quis blandit sapien. Curabitur venenatis commodo purus, id aliquam odio pellentesque in.\nEtiam eu tristique enim. Pellentesque euismod at elit sit amet placerat. Nunc nulla diam, bibendum nec nunc ut, varius semper erat. Cras vel velit a purus porttitor placerat. Nulla mattis velit eros, nec ultricies mauris ultrices sit amet. Fusce mi quam, auctor et vulputate quis, tempus ac diam. Integer nec nibh orci. Praesent ac sapien tortor.\nPellentesque vel ante eu mauris pretium accumsan.";
}
