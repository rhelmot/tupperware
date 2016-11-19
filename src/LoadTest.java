import java.text.ParseException;

public class LoadTest {
    public static void main(String[] args) {
        try {
            UsersEntity Chris = UsersEntity.create("Chris Brown", "ChrisBrown@gmail.com", "ChrisBro1", "4008801345", "CB", false);
            UsersEntity Russell = UsersEntity.create("Russell Westbrook", "RWestbrook@gmail.com", "RWesbrook1", "2318760989", "Thunder Leader", false);
            UsersEntity Kevin = UsersEntity.create("Kevin Durant", "DurantKev@gmail.com", "KDurant1", "5345298112", "KDTrey5", false);
            UsersEntity Iggy = UsersEntity.create("Iggy Azalea", "Iggy@gmail.com", "Iggy1", "6876498432", "IGGY", false);
            UsersEntity Mariah = UsersEntity.create("Mariah Carey", "MariaCarey@gmail.com", "MCarey1", "5540390182", "MariahCarey", false);
            UsersEntity Nicki = UsersEntity.create("Nicki Minaj", "NickiMinaj@gmail.com", "NMinaj1", "4764033082", "NickiMalaji", false);
            UsersEntity Daniel = UsersEntity.create("Daniel Radcliffe", "DRadcliffe@gmail.com", "DRad1", "2734063052", "HP_Daniel", false);
            UsersEntity Rihanna= UsersEntity.create("Rihanna", "Rihanna@gmail.com", "Rihanna1", "2723060542", "Rihanna", false);
            UsersEntity Justin = UsersEntity.create("Justin Bieber", "JBieber@gmail.com", "JBieber1", "8340740172", "JBtheKing", false);
            UsersEntity Larry = UsersEntity.create("Larry Page", "LarryPage@gmail.com", "LarryPage1", "6540320172", "LarryPage", false);
            UsersEntity Stephen = UsersEntity.create("Stephen Curry", "SCurry@gmail.com", "SCurry1", "3412377584", "WarriorChampion", false);
            UsersEntity Emma = UsersEntity.create("Emma Watson", "EmmaWatson@yahoo.com", "EWatson1", "8443298162", "EmWatson", false);
            UsersEntity Lena = UsersEntity.create("Lena Headey", "Lheadey@yahoo.com", "LHeadey1", "1031973432", "The Queen", false);
            UsersEntity Ariana = UsersEntity.create("Ariana Grande", "AGrande@yahoo.com", "AGrande1", "9376498172", "Ariana Grande", true);
            UsersEntity Kit = UsersEntity.create("Kit Harington", "KHarin@gmail.com", "KHarin1", "6340740172", "Lord Commander", false);
            UsersEntity Emilia = UsersEntity.create("Emilia Clarke", "EClarke@gmail.com", "EClarke1", "3431193740", "Khaleesi", false);
            UsersEntity Peter = UsersEntity.create("Peter Dinklage", "PDinklage@gmail.com", "PDinklage1", "6753568567", "The Imp", false);
            UsersEntity James = UsersEntity.create("James Harden", "JHarden@gmail.com", "JHarden1", "3416274384", "James Harden", false);
            UsersEntity Tim = UsersEntity.create("Tim Cook", "Timcook@gmail.com", "TCook1", "4512756891", "CookApple", true);
            UsersEntity Elon = UsersEntity.create("Elon Musk", "Elonmusk@yahoo.com", "EMusk1", "2847201343", "Elon Musk", false);
            UsersEntity Jeff = UsersEntity.create("Jeff Bezos", "JeffBezos@yahoo.com", "JBezos1", "3463712649", "AmazonPrime", false);

            UsersEntity[][] circles = new UsersEntity[][]{{Russell, Kevin, James, Stephen}, {Kit, Emilia, Peter, Lena}, {Chris, Justin, Iggy, Rihanna, Ariana, Nicki, Mariah}, {Emma, Daniel}, {Tim, Jeff, Elon, Larry}};
            for (int i = 0; i < circles.length; i++) {
                for (int j = 0; j < circles[i].length - 1; j++) {
                    for (int k = j + 1; k < circles[i].length; k++) {
                        if (!circles[i][j].makeFriends(circles[i][k])) {
                            System.err.println("FUCK " + circles[i][j].name + "/" + circles[i][k].name);
                        }
                    }
                }
            }

            Mariah.makeFriends(Emma);
            Tim.makeFriends(Stephen);
            Chris.makeFriends(Russell);
            Kit.makeFriends(Russell);
            Kit.makeFriends(Kevin);
            James.makeFriends(Iggy);
            James.makeFriends(Rihanna);
            Daniel.makeFriends(Justin);

            Database.setTime("4.1.2016, 9:00 AM", false);
            ChatsEntity.create(Ariana, "I'm gonna stop the rumor between us.", Justin);
            Database.setTime("4.1.2016, 9:01 AM", false);
            ChatsEntity.create(Justin, "go ahead.", Ariana);
            Database.setTime("7.5.2016, 9:00 AM", false);
            ChatsEntity.create(Kevin, "I am going to warriors.", Russell);
            Database.setTime("7.8.2016, 10:00 PM", false);
            ChatsEntity.create(Peter, "We are gonna building ships and sail to Westeros.", Emilia);

            ChatGroupsEntity Thunder = ChatGroupsEntity.create("Thunder Big3", 7, Kevin);
            Thunder.inviteUser(Russell);
            Thunder.acceptInvitation(Russell);
            Thunder.inviteUser(James);
            Thunder.acceptInvitation(James);
            Database.setTime("11.1.2015, 5:00 PM", false);
            ChatsEntity.create(Kevin, "We are moving forward.", Thunder);
            Database.setTime("11.1.2015, 5:01 PM", false);
            ChatsEntity.create(James, "I deserve the MVP this season!", Thunder);
            Database.setTime("11.1.2015, 5:02 PM", false);
            ChatsEntity.create(Russell, "I will lead the team to Champion!", Thunder);
            Database.setTime("11.1.2015, 5:03 PM", false);
            ChatsEntity.create(Kevin, "LOL. Hope to see u in western finals.", Thunder);

            ChatGroupsEntity Game = ChatGroupsEntity.create("Game of Thrones", 7, Emilia);
            Game.inviteUser(Peter);
            Game.acceptInvitation(Peter);
            Game.inviteUser(Kit);
            Game.acceptInvitation(Kit);
            Game.inviteUser(Lena);
            Game.acceptInvitation(Lena);
            Database.setTime("11.3.2016, 9:00 AM", false);
            ChatsEntity.create(Emilia, "Do you guys see my dragons?", Game);
            Database.setTime("11.3.2016, 9:01 AM", false);
            ChatsEntity.create(Peter, "They almost ate me last night...", Game);
            Database.setTime("11.3.2016, 9:02 AM", false);
            ChatsEntity.create(Kit, "Didn't see them... fought Ramsay last night...", Game);
            Database.setTime("11.3.2016, 9:03 AM", false);
            ChatsEntity.create(Lena, "I got them. So disappointed they didn't eat Tyrion... But it doesn't matter, I am the real queen now...", Game);
            Database.setTime("11.3.2016, 9:04 AM", false);
            ChatsEntity.create(Peter, "All right...", Game);

            ChatGroupsEntity CEOs = ChatGroupsEntity.create("CEOs", 7, Tim);
            CEOs.inviteUser(Jeff);
            CEOs.acceptInvitation(Jeff);
            CEOs.inviteUser(Larry);
            CEOs.acceptInvitation(Larry);
            CEOs.inviteUser(Elon);
            CEOs.acceptInvitation(Elon);
            Database.setTime("11.10.2016, 10:01 AM", false);
            ChatsEntity.create(Tim, "There will be no discount on Macbook Pro!", CEOs);
            Database.setTime("11.10.2016, 10:02 AM", false);
            ChatsEntity.create(Jeff, "C'mon man, it's soon Black Friday...", CEOs);
            Database.setTime("11.10.2016, 10:15 AM", false);
            ChatsEntity.create(Elon, "I think Surface book is better!", CEOs);
            Database.setTime("11.10.2016, 10:16 AM", false);
            ChatsEntity.create(Jeff, "😂", CEOs);

            Database.setTime("11.11.2016, 6:27 AM", false);
            PostsEntity.createPublic(Tim, "Proud to work alongside these & many more veterans at Apple. We honor all the brave men & women who sacrificed for our freedom.", new String[]{"veterans", "apple", "freedom"});

            Database.setTime("11.11.2016, 6:27 AM", false);
            PostsEntity.createPublic(Emma, "So happy to show all of you the new teaser poster for Beauty and the Beast! I hope you like it. Love Emma x", new String[]{"poster", "Disney", "movie"});

            Database.setTime("11.11.2016, 6:27 AM", false);
            PostsEntity.createPrivate(Kevin, "Everything about this city keeps getting better. Hitting the streets today!", new String[]{"street", "city"}, new UsersEntity[]{Russell});


            Database.setTime("11.13.2016, 10:50 AM", false);
            PostsEntity.createPublic(Kevin, "Lets go support our own and check out Almost Christmas!", new String[]{"city", "Christmas"});

            Database.setTime("11.13.2016, 10:50 AM", false);
            PostsEntity.createPrivate(Kit, "Lannisters Everywhere! Smiling Smiling Smiling", new String[]{"Game of Thrones", "TV Series"}, new UsersEntity[]{Lena, Peter});

            Chris.addTag("singer");
            Chris.addTag("rapper");
            Chris.addTag("dancer");
            Russell.addTag("Basketball");
            Russell.addTag("thunder");
            Russell.addTag("NBA");
            Kevin.addTag("Basketball");
            Kevin.addTag("warrior");
            Kevin.addTag("NBA");
            Iggy.addTag("rapper");
            Iggy.addTag("model");
            Mariah.addTag("Singer");
            Nicki.addTag("Singer");
            Nicki.addTag("rapper");
            Daniel.addTag("Actor");
            Daniel.addTag("Harry Potter");
            Rihanna.addTag("Singer");
            Rihanna.addTag("Barbadian");
            Justin.addTag("Singer");
            Justin.addTag("Canadian");
            Larry.addTag("Google");
            Larry.addTag("CEO");
            Larry.addTag("computer scientist");
            Stephen.addTag("Basketball");
            Stephen.addTag("warrior");
            Stephen.addTag("NBA");
            Emma.addTag("Actress");
            Lena.addTag("Actress");
            Lena.addTag("Game of Thrones");
            Ariana.addTag("Singer");
            Ariana.addTag("actress");
            Kit.addTag("Actor");
            Kit.addTag("Game of Thrones");
            Kit.addTag("You_Know_Nothing");
            Emilia.addTag("Actress");
            Emilia.addTag("Game of Thrones");
            Peter.addTag("Actor");
            Peter.addTag("Game of Thrones");
            James.addTag("Basketball");
            James.addTag("rockets");
            James.addTag("NBA");
            Tim.addTag("Apple");
            Tim.addTag("CEO");
            Elon.addTag("Space X");
            Elon.addTag("Tesla");
            Elon.addTag("SolarCity");
            Elon.addTag("OpenAI");
            Jeff.addTag("Amzon");
            Jeff.addTag("CEO");

        } catch (DomainError e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println(e);
        }
    }
}
