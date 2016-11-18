public class TestTupperware {
    public static void main(String[] args) {
        try {
            UsersEntity newUser = UsersEntity.create("andrew@cs", "Andrew Dutcher", "8056983068", "doodle", "rhelmot", true);
            System.out.println(newUser.hid);
            newUser.save();
            System.out.println(newUser.hid);

            UsersEntity newUser2 = UsersEntity.create("brandon@cs", "Brandon Dutcher", "8056983048", "hellfire", "iolight", false);
            System.out.println(newUser2.hid);
            newUser2.save();
            System.out.println(newUser2.hid);

            UsersEntity newUser3 = UsersEntity.get(1);
            System.out.println(newUser3.name);
        } catch (DomainError e) {
            System.out.println(e);
        }
    }
}
