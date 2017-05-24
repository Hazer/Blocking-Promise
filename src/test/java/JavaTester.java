
import io.vithor.blockingpromise.BlockingPromises;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JavaTester {

    @Test
    public void evaluateSuccess() throws ExecutionException, InterruptedException {

        Future<String> promise = BlockingPromises.makeFrom(complete -> {
                    String message = "lasmlaskf";
//                    throw new RuntimeException(message);
                    complete.success(message);

//                    complete.exceptionally(new IllegalAccessError("What"));
                }
        );

        String message = promise.get();

        System.out.println(message);
    }
}
