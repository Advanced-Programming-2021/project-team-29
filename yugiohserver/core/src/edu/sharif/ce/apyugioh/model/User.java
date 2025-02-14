package edu.sharif.ce.apyugioh.model;

import org.jetbrains.annotations.NotNull;

import edu.sharif.ce.apyugioh.controller.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class User implements Comparable<User> {

    @Getter
    @EqualsAndHashCode.Include
    private int id;
    @Getter
    @EqualsAndHashCode.Include
    private String username;
    private String password;
    @Getter
    @EqualsAndHashCode.Include
    private String nickname;
    @Getter
    @Setter
    private int score;
    @Getter
    @Setter
    private int mainDeckID;
    @Getter
    @Setter
    private String avatarName;

    public User(String username, String password, String nickname, String avatarName) {
        id = DatabaseManager.getUserList().stream().mapToInt(e -> e.id).max().getAsInt() + 1;
        this.username = username;
        this.password = Utils.hash(password);
        this.nickname = nickname;
        this.avatarName = avatarName;
        mainDeckID = -1;
        DatabaseManager.addUser(this);
        new Inventory(id);
    }

    public static User getUserByID(int id) {
        return DatabaseManager.getUserList().stream().filter(e -> e.id == id).findFirst().orElse(null);
    }

    public static User getUserByUsername(String username) {
        return DatabaseManager.getUserList().stream().filter(e -> e.username.equals(username)).findFirst()
                .orElse(null);
    }

    public static User getUserByNickname(String nickname) {
        return DatabaseManager.getUserList().stream().filter(e -> e.nickname.equals(nickname)).findFirst()
                .orElse(null);
    }

    public boolean isPasswordCorrect(String password) {
        return Utils.hash(password).equals(this.password);
    }

    public void setPassword(String password) {
        this.password = Utils.hash(password);
        DatabaseManager.updateUsersToDB();
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
        DatabaseManager.updateUsersToDB();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        DatabaseManager.updateUsersToDB();
    }

    public void setAvatar(String avatarName) {
        this.avatarName = avatarName;
        DatabaseManager.updateUsersToDB();
    }

    @Override
    public int compareTo(@NotNull User o) {
        if (o.score != score) return -Integer.compare(score, o.score);
        return nickname.compareTo(o.nickname);
    }
}
