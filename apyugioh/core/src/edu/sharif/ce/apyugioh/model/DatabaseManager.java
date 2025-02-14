package edu.sharif.ce.apyugioh.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.sharif.ce.apyugioh.controller.CSVParser;
import edu.sharif.ce.apyugioh.controller.Utils;
import edu.sharif.ce.apyugioh.model.card.CardType;
import edu.sharif.ce.apyugioh.model.card.Monster;
import edu.sharif.ce.apyugioh.model.card.MonsterAttribute;
import edu.sharif.ce.apyugioh.model.card.MonsterEffect;
import edu.sharif.ce.apyugioh.model.card.MonsterType;
import edu.sharif.ce.apyugioh.model.card.Spell;
import edu.sharif.ce.apyugioh.model.card.SpellLimit;
import edu.sharif.ce.apyugioh.model.card.SpellProperty;
import edu.sharif.ce.apyugioh.model.card.Trap;
import lombok.Getter;

public class DatabaseManager {

    private static HashMap<String, FileHandle> dbs;
    private static Moshi moshi;
    @Getter
    private static List<User> userList;
    @Getter
    private static List<Inventory> inventoryList;
    @Getter
    private static ShopCards cards;
    @Getter
    private static List<Deck> deckList;
    private static Logger logger;

    static {
        logger = LogManager.getLogger(DatabaseManager.class);
    }

    public static void init() {
        moshi = new Moshi.Builder().build();
        dbs = new HashMap<>();
        ArrayList<String> models = new ArrayList<>();
        models.add("user");
        models.add("inventory");
        models.add("deck");
        try {
            for (String model : models) {
                if (model.equals("inventory"))
                    dbs.put(model, Gdx.files.local("assets/db/" + model.substring(0, model.length() - 1) + "ies.json"));
                else dbs.put(model, Gdx.files.local("assets/db/" + model + "s.json"));
            }
            updateUsersFromDB();
            updateInventoriesFromDB();
            updateDecksFromDB();
            cards = CSVToShopCards();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            Utils.printError("couldn't initialize database");
            System.exit(1);
        }
    }

    private static String readFromFile(FileHandle jsonDB) {
        try {
            initDB(jsonDB);
            return jsonDB.readString();
        } catch (Exception e) {
            logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            Utils.printError("failed to read database");
            System.exit(1);
        }
        return null;
    }

    private static void writeToFile(FileHandle jsonDB, String jsonText) {
        try {
            initDB(jsonDB);
            jsonDB.writeString(jsonText, false);
        } catch (Exception e) {
            logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            Utils.printError("failed to write database");
            System.exit(1);
        }
    }

    private static void initDB(FileHandle jsonDB) {
        if (!jsonDB.parent().isDirectory()) {
            jsonDB.parent().mkdirs();
        }
        if (!jsonDB.exists()) {
            jsonDB.writeString("[]", false);
        }
    }

    private static void updateUsersFromDB() {
        updateFromDB("user");
    }

    private static void updateInventoriesFromDB() {
        updateFromDB("inventory");
    }

    private static void updateDecksFromDB() {
        updateFromDB("deck");
    }

    private static void updateFromDB(String dbName) {
        try {
            if (!dbs.containsKey(dbName))
                throw new IllegalArgumentException("Unexpected value: " + dbName);
            String input = readFromFile(dbs.get(dbName));
            Type type;
            switch (dbName) {
                case "user":
                    type = Types.newParameterizedType(List.class, User.class);
                    JsonAdapter<List<User>> usersAdapter = moshi.adapter(type);
                    userList = usersAdapter.fromJson(input);
                    break;
                case "inventory":
                    type = Types.newParameterizedType(List.class, Inventory.class);
                    JsonAdapter<List<Inventory>> inventoriesAdapter = moshi.adapter(type);
                    inventoryList = inventoriesAdapter.fromJson(input);
                    break;
                case "deck":
                    type = Types.newParameterizedType(List.class, Deck.class);
                    JsonAdapter<List<Deck>> decksAdapter = moshi.adapter(type);
                    deckList = decksAdapter.fromJson(input);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + dbName);
            }
        } catch (Exception e) {
            logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            Utils.printError("corrupted database");
            System.exit(1);
        }
    }

    public static void updateUsersToDB() {
        updateToDB("user");
    }

    public static void updateInventoriesToDB() {
        updateToDB("inventory");
    }

    public static void updateDecksToDB() {
        updateToDB("deck");
    }

    private static void updateToDB(String dbName) {
        try {
            String output;
            Type type;
            switch (dbName) {
                case "user":
                    type = Types.newParameterizedType(List.class, User.class);
                    JsonAdapter<List<User>> usersAdapter = moshi.adapter(type);
                    output = usersAdapter.toJson(userList);
                    break;
                case "inventory":
                    type = Types.newParameterizedType(List.class, Inventory.class);
                    JsonAdapter<List<Inventory>> inventoryAdapter = moshi.adapter(type);
                    output = inventoryAdapter.toJson(inventoryList);
                    break;
                case "deck":
                    type = Types.newParameterizedType(List.class, Deck.class);
                    JsonAdapter<List<Deck>> deckAdapter = moshi.adapter(type);
                    output = deckAdapter.toJson(deckList);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + dbName);
            }
            writeToFile(dbs.get(dbName), output);
        } catch (Exception e) {
            logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            Utils.printError("corrupted database");
            System.exit(1);
        }
    }

    public static void addUser(User user) {
        userList.add(user);
        updateUsersToDB();
    }

    private static ShopCards CSVToShopCards() {
        ShopCards cards = new ShopCards();
        addMonstersToCards(cards);
        addSpellsToCards(cards);
        return cards;
    }

    private static void addSpellsToCards(ShopCards cards) {
        for (HashMap<String, String> spellMap : new CSVParser("shop/SpellTrap.csv").getContentsAsMap()) {
            if (spellMap.get("type").equalsIgnoreCase("spell")) {
                Spell spell = new Spell(Utils.firstUpperOnly(spellMap.get("name")), spellMap.get("description"), SpellProperty.
                        valueOf(spellMap.get("icon (property)").toUpperCase().replaceAll("-", "_")),
                        SpellLimit.valueOf(spellMap.get("status").toUpperCase()));
                spell.setCardEffects(getCardEffects(spell.getName(), CardType.SPELL));
                cards.addSpell(spell, Integer.parseInt(spellMap.get("price")));
            } else {
                Trap trap = new Trap(Utils.firstUpperOnly(spellMap.get("name")), spellMap.get("description"), SpellProperty.
                        valueOf(spellMap.get("icon (property)").toUpperCase().replaceAll("-", "_")),
                        SpellLimit.valueOf(spellMap.get("status").toUpperCase()));
                trap.setCardEffects(getCardEffects(trap.getName(), CardType.TRAP));
                cards.addTrap(trap, Integer.parseInt(spellMap.get("price")));
            }
        }
    }

    private static void addMonstersToCards(ShopCards cards) {
        for (HashMap<String, String> monsterMap : new CSVParser("shop/Monster.csv").getContentsAsMap()) {
            Monster monster = new Monster(Utils.firstUpperOnly(monsterMap.get("name")), monsterMap.get("description"),
                    Integer.parseInt(monsterMap.get("level")), Integer.parseInt(monsterMap.get("atk")),
                    Integer.parseInt(monsterMap.get("def")), MonsterAttribute.valueOf(monsterMap.get("attribute").toUpperCase()),
                    MonsterType.valueOf(monsterMap.get("monster type").replaceAll("[- ]", "_")
                            .toUpperCase()), MonsterEffect.valueOf(monsterMap.get("card type").toUpperCase()));
            monster.setCardEffects(getCardEffects(monster.getName(), CardType.MONSTER));
            cards.addMonster(monster, Integer.parseInt(monsterMap.get("price")));
        }
    }

    public static List<Effects> getCardEffects(String cardName, CardType type) {
        FileHandle effectsPath = Gdx.files.internal("shop/" + type.name().toLowerCase() + "/" +
                Utils.firstUpperOnly(cardName).replaceAll(" ", "") + ".json");
        if (effectsPath.exists()) {
            try {
                String effects = effectsPath.readString();
                JsonAdapter<CardEffects> effectsAdapter = moshi.adapter(CardEffects.class);
                return effectsAdapter.fromJson(effects).effects;
            } catch (IOException ignored) {
                //System.out.println(effectsPath);
            } catch (Exception e) {
                logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    public static FileHandle exportShopCards(ShopCards cards) {
        JsonAdapter<ShopCards> cardsAdapter = moshi.adapter(ShopCards.class);
        FileHandle exportPath = Gdx.files.external("assets/backup" + LocalDateTime.now().format(DateTimeFormatter
                .ofPattern("yyyy_MM_dd_HH:mm:ss")) + "_export.json");
        writeToFile(exportPath, cardsAdapter.toJson(cards));
        return exportPath;
    }

    public static boolean importShopCards(FileHandle shopCardsPath) {
        JsonAdapter<ShopCards> cardsAdapter = moshi.adapter(ShopCards.class);
        try {
            ShopCards shopCards = cardsAdapter.fromJson(readFromFile(shopCardsPath));
            if (shopCards != null && !cards.addShopCards(shopCards)) {
                return false;
            }
        } catch (IOException e) {
            logger.error("Exception caused by: {}\nDetails: {}", e.getCause(), e.getMessage());
            Utils.printError("corrupted database");
            System.exit(1);
        }
        return true;
    }

    public static List<Map.Entry<String, Integer>> getScoreboard() {
        HashMap<String, Integer> results = new HashMap<>();
        int size = 0;
        for (User user : getUserList()) {
            if (!user.getUsername().equals("AIHard")
                    && !user.getUsername().equals("AIMediocre")
                    && !user.getUsername().equals("AIEasy")) {
                if (size < 8) results.put(user.getUsername(), user.getScore());
                size++;
            }
        }
        return results.entrySet().stream().sorted(Comparator.comparingInt(e -> -e.getValue())).collect(Collectors.toList());
    }

    private static class CardEffects {
        List<Effects> effects;
    }
}
