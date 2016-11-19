import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ExtendedTerminal;
import com.googlecode.lanterna.terminal.MouseCaptureMode;

import java.util.regex.Pattern;
import java.util.Arrays;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.PrintWriter;

public class Tupperware {
    public static void main(String[] args) throws IOException {
        Tupperware t = new Tupperware();
        try {
            String token = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".tupperwaretoken")));
            SessionsEntity s = SessionsEntity.lookup(token);
            if (s != null) {
                t.currentSession = s;
                t.currentUser = s.getUser();
            }
        } catch (IOException e) {}
        if (t.currentSession == null) {
            t.welcome();
            try {
                PrintWriter out = new PrintWriter(Paths.get(System.getProperty("user.home"), ".tupperwaretoken").toString());
                out.print(t.currentSession.token);
                out.close();
            } catch (IOException e) {}
        }
        t.close();
        System.out.println("Hey there " + t.currentUser.name);
    }

    private ExtendedTerminal terminal;
    private Screen screen;
    private MultiWindowTextGUI gui;

    public SessionsEntity currentSession;
    public UsersEntity currentUser;

    public Tupperware() {
        try {
            terminal = (ExtendedTerminal)new DefaultTerminalFactory().createTerminal();
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
}
