package edu.sharif.ce.apyugioh.controller.player;

import edu.sharif.ce.apyugioh.model.card.GameCard;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

public abstract class ArraySelectionAction implements Callable<GameCard[]> {

    @Getter
    @Setter
    public ArrayBlockingQueue<GameCard[]> choices;
}
