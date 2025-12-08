package main.engine.augment;

import main.engine.augment.effectDB.StatEffect;

import java.util.List;

// All available augments
public class AugmentLibrary {
    public static final List<Augment> pool = List.of(
            new Augment("Attack Overflow", "It's time to fire! <UNIMPLEMENTED>",
                    new TestEffect()),

            new Augment("April Fools", "Your playing skill improves! <UNIMPLEMENTED>",
                    new TestEffect()),

            new Augment("Attack UP", "Engineers said it shouldn’t fire this hard.",
                    new StatEffect(StatEffect.StatType.ATTACK, 1)),

            new Augment("Move Speed UP", "The ship feels lighter",
                    new StatEffect(StatEffect.StatType.SPEED, 1)),

            new Augment("Repair", "Some glue and tape. Should be fine <UNIMPLEMENTED>",
                    new TestEffect()),

            new Augment("Overdrive", "The ship’s burning. Looks like targets are too  <UNIMPLEMENTED>",
                    new TestEffect()),

            new Augment("Strange Button", "It looks very pressable <UNIMPLEMENTED>",
                    new TestEffect()),

            new Augment("Bullet Speed UP", "Feels like being a sniper",
                    new StatEffect(StatEffect.StatType.BULLET_SPEED, 2)),

            new Augment("Additional shot", "The trigger seems broken <UNIMPLEMENTED>",
                    new TestEffect()),

            new Augment("Fire Speed UP", "The trigger feels lighter",
                    new StatEffect(StatEffect.StatType.INTERVAL, 100))
    );
}