import java.util.ArrayList;

public class TestTupperware {
    public static void main(String[] args) {
        try {
            UsersEntity u = UsersEntity.getByEmail("JeffBezos@yahoo.com");
            ArrayList<ChatGroupsEntity> gl = u.getChatGroups();
            ChatGroupsEntity g = gl.get(0);
            ChatsEntity c = ChatsEntity.getGroupChats(u, g, null, 10).get(0);
            System.out.println(c.text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
