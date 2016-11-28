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

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.PrintWriter;

public class Tupperware {
    public static void main(String[] args) throws IOException {
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
    }

    private Terminal terminal;
    private Screen screen;
    private MultiWindowTextGUI gui;

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
        BasicWindow window = new BasicWindow();
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
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        new Label("Name").addTo(panel);
        TextBox nameBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Email").addTo(panel);
        TextBox emailBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Password").addTo(panel);
        TextBox passwordBox = new TextBox(new TerminalSize(21, 1))
            .setMask('*')
            .setValidationPattern(Pattern.compile(".{0,10}"))
            .addTo(panel);
        new Label("Confirm Password").addTo(panel);
        TextBox password2Box = new TextBox(new TerminalSize(21, 1))
            .setMask('*')
            .setValidationPattern(Pattern.compile(".{0,10}"))
            .addTo(panel);
        new Label("Phone #").addTo(panel);
        TextBox phoneBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile("[0-9]{0,10}"))
            .addTo(panel);
        new Label("Screen Name").addTo(panel);
        TextBox screenNameBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
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
        BasicWindow window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        new Label("Email").addTo(panel);
        TextBox emailBox = new TextBox(new TerminalSize(21, 1))
            .setValidationPattern(Pattern.compile(".{0,20}"))
            .addTo(panel);
        new Label("Password").addTo(panel);
        TextBox passwordBox = new TextBox(new TerminalSize(21, 1))
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
        try {
            throw new Exception("fuck");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mainMenu() {
        Tupperware root = this;
        BasicWindow window = new BasicWindow();
        //window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        new Label("Tupperware™").addTo(panel);

        new EmptySpace(new TerminalSize(5,1)).addTo(panel);

        new Button("New Post", new Runnable() {
            @Override
            public void run() {
                addWindow(new NewPostWindow(root));
            }
        }).addTo(panel);

        new Button("lmao", new Runnable() {
            @Override
            public void run() {
                //DynamicWindow x = new DynamicWindow("Chat hell", false, new TerminalSize(30, 10));
                //x.setComponent(new Label("welcome to the bone zone"));
                BasicWindow x = dumbPostsWindow();
                gui.addWindow(x);
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

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }

    public void addWindow(ParametrizedWindow window) {
        if (!activeWindows.contains(window)) {
            window.construct();
            activeWindows.add(window);
            gui.addWindow(window);
        }
    }

    public <T> T genericPicker(List<T> items, String text, String failText) {
        if (items.size() == 0) {
            if (failText != null) {
                MessageDialog.showMessageDialog(gui, "Nothing to Pick!", failText);
            }
            return null;
        }

        final ArrayList<T> selected = new ArrayList<T>();
        ActionListDialogBuilder b = new ActionListDialogBuilder();
        b.setTitle("Picker");
        b.setDescription("Text");

        for (T item : items) {
            b.addAction(item.toString(), new Runnable() {
                private final T innerItem = item;
                @Override public void run() {
                    if (selected.size() != 0) {
                        selected.clear();
                    }
                    selected.add(innerItem);
                }
            });
        }

        b.build().showDialog(gui);
        if (selected.size() == 0) {
            return null;
        } else {
            return selected.get(0);
        }
    }

    public UsersEntity friendsPicker(String text) {
        return genericPicker(currentUser.getFriends(), text, "Please make some friends first!");
    }

    public static BasicWindow dumbPostsWindow() {
        BasicWindow window = new DynamicWindow("Dummy posts!", false, new TerminalSize(60, 15));
        ScrollingPanel contentPanel = new ScrollingPanel();

        contentPanel.addComponent(new Button("Line 0"));
        contentPanel.addComponent(new Label ("Line 1"));
        contentPanel.addComponent(new Label ("Line 2"));
        contentPanel.addComponent(new Button("Line 3"));
        contentPanel.addComponent(new Label ("Line 4"));
        contentPanel.addComponent(new Label ("Line 5"));
        contentPanel.addComponent(new Button("Line 6"));
        contentPanel.addComponent(new Label ("Line 7"));
        contentPanel.addComponent(new Label ("Line 8"));
        contentPanel.addComponent(new Button("Line 9"));
        contentPanel.addComponent(new Label ("Line 10"));
        contentPanel.addComponent(new Label ("Line 11"));
        contentPanel.addComponent(new Button("Line 12"));
        contentPanel.addComponent(new Label ("Line 13"));
        contentPanel.addComponent(new Label ("Line 14"));
        contentPanel.addComponent(new Button("Line 15"));
        contentPanel.addComponent(new Label ("Line 16"));
        contentPanel.addComponent(new Label ("Line 17"));
        contentPanel.addComponent(new Button("Line 18"));
        contentPanel.addComponent(new Label ("Line 19"));
        contentPanel.addComponent(new Label ("Line 20"));
        contentPanel.addComponent(new Button("Line 21"));
        contentPanel.addComponent(new WordWrapTextBox(lipsum));
        contentPanel.addComponent(new Button("Line 23"));
        contentPanel.addComponent(new Button("Line 24"));
        contentPanel.addComponent(new Button("Line 25"));
        contentPanel.addComponent(new Button("Line 26"));
        contentPanel.addComponent(new Button("Line 27"));
        contentPanel.addComponent(new Button("Line 28"));
        contentPanel.addComponent(new Button("Line 29"));
        contentPanel.addComponent(new Button("Line 30"));
        contentPanel.addComponent(new Button("Line 31"));
        contentPanel.addComponent(new Button("Line 32"));
        contentPanel.addComponent(new Button("Line 33"));
        contentPanel.addComponent(new Button("Line 34"));

        window.setComponent(contentPanel);
        return window;
    }

    public static BasicWindow dumbChatWindow() {
        BasicWindow window = new DynamicWindow("Dummy chat!", false, new TerminalSize(50, 15));
        Panel controlPanel = new Panel();

        WordWrapTextBox contentBox = new WordWrapTextBox("here we go :)", WordWrapTextBox.Style.MULTI_LINE);
        ChatLineTextBox box = new ChatLineTextBox() {
            @Override
            public boolean handleLine(String line) {
                contentBox.addLine(getText());
                contentBox.moveToBottom();
                return true;
            }

            @Override
            public boolean handleUp(KeyStroke stroke) {
                contentBox.handleKeyStroke(stroke);
                return true;
            }

            @Override
            public boolean handleDown(KeyStroke stroke) {
                contentBox.handleKeyStroke(stroke);
                return true;
            }

            @Override
            public boolean handlePageUp(KeyStroke stroke) {
                contentBox.handleKeyStroke(stroke);
                return true;
            }

            @Override
            public boolean handlePageDown(KeyStroke stroke) {
                contentBox.handleKeyStroke(stroke);
                return true;
            }
        };

        controlPanel.setLayoutManager(new BorderLayout());
        box.setLayoutData(BorderLayout.Location.BOTTOM);
        contentBox.setLayoutData(BorderLayout.Location.CENTER);
        //contentBox.setReadOnly(true);
        contentBox.setEnabled(false);

        controlPanel.addComponent(contentBox);
        controlPanel.addComponent(box);

        window.setComponent(controlPanel);
        window.setFocusedInteractable(box);
        return window;
    }

    public static String lipsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse a metus vel dui dapibus feugiat. Etiam nisi mauris, blandit quis odio ut, pharetra aliquet orci.\nNam facilisis dui vehicula urna vulputate, a vestibulum ex luctus. Phasellus consectetur nisl quis tincidunt gravida. Duis fermentum, sapien ac dapibus viverra, felis eros volutpat sem, ut vehicula justo neque et nisi.\nMaecenas luctus nibh vel orci congue placerat. Praesent a felis id risus consequat scelerisque. Aenean non ultrices quam, quis blandit sapien. Curabitur venenatis commodo purus, id aliquam odio pellentesque in.\nEtiam eu tristique enim. Pellentesque euismod at elit sit amet placerat. Nunc nulla diam, bibendum nec nunc ut, varius semper erat. Cras vel velit a purus porttitor placerat. Nulla mattis velit eros, nec ultricies mauris ultrices sit amet. Fusce mi quam, auctor et vulputate quis, tempus ac diam. Integer nec nibh orci. Praesent ac sapien tortor.\nPellentesque vel ante eu mauris pretium accumsan.";
}
