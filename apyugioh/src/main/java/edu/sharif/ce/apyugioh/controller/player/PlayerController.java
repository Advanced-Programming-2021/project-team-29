package edu.sharif.ce.apyugioh.controller.player;

import edu.sharif.ce.apyugioh.controller.game.GameController;
import edu.sharif.ce.apyugioh.controller.game.SelectionController;
import edu.sharif.ce.apyugioh.model.Phase;
import edu.sharif.ce.apyugioh.model.Player;
import edu.sharif.ce.apyugioh.model.card.*;
import edu.sharif.ce.apyugioh.view.GameView;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class PlayerController {

    @Setter
    protected int gameControllerID;
    protected Player player;

    public PlayerController(Player player) {
        this.player = player;
    }

    public void select(CardLocation location) {
        if (isZoneSelected(location, location.isFromMonsterZone(), player.getField().getMonsterZone())) return;
        if (isZoneSelected(location, location.isFromSpellZone(), player.getField().getSpellZone())) return;
        if (isFieldZoneSelected(location)) return;
        if (isHandSelected(location)) return;
    }

    private boolean isHandSelected(CardLocation location) {
        if (location.isInHand()) {
            if (location.getPosition() >= player.getField().getHand().size() || location.getPosition() < 0) {
                GameController.getView().showError(GameView.ERROR_SELECTION_CARD_POSITION_INVALID);
            } else if (player.getField().getHand().get(location.getPosition()) == null) {
                GameController.getView().showError(GameView.ERROR_SELECTION_CARD_NOT_FOUND);
            } else {
                getGameController().select(location);
                GameController.getView().showSuccess(GameView.SUCCESS_SELECTION_SUCCESSFUL,
                        getSelectionController().getCard().getCard().getName());
            }
            return true;
        }
        return false;
    }

    private boolean isFieldZoneSelected(CardLocation location) {
        if (location.isFromFieldZone()) {
            if (player.getField().getFieldZone() == null) {
                GameController.getView().showError(GameView.ERROR_SELECTION_CARD_NOT_FOUND);
            } else {
                getGameController().select(location);
                GameController.getView().showSuccess(GameView.SUCCESS_SELECTION_SUCCESSFUL,
                        getSelectionController().getCard().getCard().getName());
            }
            return true;
        }
        return false;
    }

    private boolean isZoneSelected(CardLocation location, boolean isZoneSelected, GameCard[] zone) {
        if (isZoneSelected) {
            if (location.getPosition() > 4 || location.getPosition() < 0) {
                GameController.getView().showError(GameView.ERROR_SELECTION_CARD_POSITION_INVALID);
            } else if (zone[location.getPosition()] == null) {
                GameController.getView().showError(GameView.ERROR_SELECTION_CARD_NOT_FOUND);
            } else {
                getGameController().select(location);
                GameController.getView().showSuccess(GameView.SUCCESS_SELECTION_SUCCESSFUL,
                        getSelectionController().getCard().getCard().getName());
            }
            return true;
        }
        return false;
    }

    public void deselect() {
        if (getSelectionController() == null) {
            GameController.getView().showError(GameView.ERROR_CARD_NOT_SELECTED);
        } else {
            GameController.getView().showSuccess(GameView.SUCCESS_SELECTION_SUCCESSFUL,
                    getSelectionController().getCard().getCard().getName());
            getGameController().deselect();
        }
    }

    public void set() {
        if (getSelectionController() == null) {
            GameController.getView().showError(GameView.ERROR_CARD_NOT_SELECTED);
            return;
        }
        if (!getSelectionController().getLocation().isInHand()) {
            GameController.getView().showError(GameView.ERROR_SELECTION_NOT_IN_HAND, "set");
            return;
        }
        getGameController().set();
        getGameController().deselect();
    }

    public void summon() {
        //checkForSpellOrTrapBanningToSummon
        if (getSelectionController() == null) {
            GameController.getView().showError(GameView.ERROR_CARD_NOT_SELECTED);
            return;
        }
        if (!getSelectionController().getLocation().isInHand() ||
                !getSelectionController().getCard().getCard().getCardType().equals(CardType.MONSTER) ||
                ((Monster) getSelectionController().getCard().getCard()).getSummon().equals(MonsterSummon.RITUAL)) {
            GameController.getView().showError(GameView.ERROR_SELECTION_NOT_IN_HAND, "summon");
            return;
        }
        if (!(getPhase().equals(Phase.MAIN1) || getPhase().equals(Phase.MAIN2))) {
            GameController.getView().showError(GameView.ERROR_ACTION_NOT_POSSIBLE_IN_THIS_PHASE);
            return;
        }
        getGameController().summon();
        getGameController().deselect();
    }

    public void changePosition(boolean isChangeToAttack) {
        if (getSelectionController() == null) {
            GameController.getView().showError(GameView.ERROR_CARD_NOT_SELECTED);
            return;
        }
        if (!getPlayer().getField().isInMonsterZone(getSelectionController().getCard())) {
            GameController.getView().showError(GameView.ERROR_CANT_CHANGE_CARD_POSITION);
            return;
        }
        if (!getGameController().getGameTurnController().getPhase().equals(Phase.MAIN1) &&
                !getGameController().getGameTurnController().getPhase().equals(Phase.MAIN2)){
            GameController.getView().showError(GameView.ERROR_ACTION_NOT_POSSIBLE_IN_THIS_PHASE);
            return;
        }
        getGameController().changePosition(isChangeToAttack);
        getGameController().deselect();
    }

    public void flipSummon() {
        if (getSelectionController() == null) {
            GameController.getView().showError(GameView.ERROR_CARD_NOT_SELECTED);
            return;
        }
        if (!getPlayer().getField().isInMonsterZone(getSelectionController().getCard())) {
            GameController.getView().showError(GameView.ERROR_CANT_CHANGE_CARD_POSITION);
            return;
        }
        if (!(getPhase().equals(Phase.MAIN1) || getPhase().equals(Phase.MAIN2))) {
            GameController.getView().showError(GameView.ERROR_ACTION_NOT_POSSIBLE_IN_THIS_PHASE);
            return;
        }
        if (!getSelectionController().getCard().isFaceDown() ||
                (getSelectionController().getCard().equals(getGameController().getGameTurnController().getSetOrSummonedMonster()))) {
            GameController.getView().showError(GameView.ERROR_SELECTION_NOT_IN_HAND, "flip summon");
            return;
        }
        getGameController().flipSummon();
        getGameController().deselect();
    }

    public void attack(int position) {
        if (checkBeforeAttack()) return;
        getGameController().attack(position);
        getGameController().deselect();
    }

    public void directAttack() {
        if (checkBeforeAttack()) return;
        getGameController().directAttack();
        getGameController().deselect();
    }

    private boolean checkBeforeAttack() {
        if (getSelectionController() == null){
            GameController.getView().showError(GameView.ERROR_CARD_NOT_SELECTED);
            return true;
        }
        if (!getPlayer().getField().isInMonsterZone(getSelectionController().getCard())){
            GameController.getView().showError(GameView.ERROR_CANT_ATTACK_WITH_CARD);
            return true;
        }
        if (!getGameController().getGameTurnController().getPhase().equals(Phase.BATTLE)){
            GameController.getView().showError(GameView.ERROR_ACTION_NOT_POSSIBLE_IN_THIS_PHASE);
            return true;
        }
        return false;
    }

    public void nextPhase() {
        getGameController().nextPhase();
    }

    public void endRound() {

    }

    public void startRound() {
        getGameController().startRound();
    }

    public void activeEffect() {

    }

    public void surrender() {

    }

    public void cancel() {

    }

    public void exchangeSideDeckCards() {

    }

    //SpecialCases
    //TributeMonsterForSummon
    public abstract GameCard[] tributeMonster(int amount);

    //Scanner
    public abstract GameCard scanMonsterForScanner();

    //Man-Eater Bug
    public abstract GameCard directRemove();

    //TexChanger
    public abstract GameCard specialCyberseSummon();

    //HeraldOfCreation
    public abstract GameCard summonFromGraveyard();

    //Beast King Barbaros & Tricky
    public abstract int chooseHowToSummon();

    //terratiger
    public abstract GameCard selectMonsterToSummon();

    //EquipMonster
    public abstract GameCard equipMonster();

    //Select card from graveyard
    public abstract GameCard selectCardFromGraveyard();

    //Select card from monster zone
    public abstract GameCard selectCardFromMonsterZone();

    //Select card from both graveyards
    public abstract GameCard selectCardFromAllGraveyards();

    //Select card from hand
    public abstract GameCard selectCardFromHand();

    //Select card from deck
    public abstract GameCard selectCardFromDeck();

    //Select field spell from deck
    public abstract GameCard selectFieldSpellFromDeck();

    //Select one of rival monsters
    public abstract GameCard selectRivalMonster();

    //Select at most two spell or trap form field
    public abstract GameCard[] selectSpellTrapsFromField(int amount);

    //Select card from graveyard with level less than mostLevel
    public abstract GameCard selectCardFromGraveyard(int mostLevel);

    //Select normal card(without effect) from hand with level less than mostLevel
    public abstract GameCard selectNormalCardFromHand(int mostLevel);

    public abstract boolean confirm(String message);


    protected SelectionController getSelectionController() {
        return getGameController().getSelectionController();
    }

    protected Phase getPhase() {
        return getGameController().getGameTurnController().getPhase();
    }

    protected GameController getGameController() {
        return GameController.getGameControllerById(gameControllerID);
    }
}
