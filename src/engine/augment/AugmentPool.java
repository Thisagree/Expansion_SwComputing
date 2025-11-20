package engine.augment;

import java.util.List;

// All available augments
public class AugmentPool {
    public static final List<Augment> pool = List.of(
            new Augment("Test 1", "lorem ipsum", new TestEffect()),
            new Augment("Test 2", "lorem ipsum", new TestEffect()),
            new Augment("Test 3", "lorem ipsum", new TestEffect()),
            new Augment("Test 4", "lorem ipsum", new TestEffect()),
            new Augment("Test 5", "lorem ipsum", new TestEffect()),
            new Augment("Test 6", "lorem ipsum", new TestEffect()),
            new Augment("Test 7", "lorem ipsum", new TestEffect()),
            new Augment("Test 8", "lorem ipsum", new TestEffect()),
            new Augment("Test 9", "lorem ipsum", new TestEffect())
    );
}