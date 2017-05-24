
import io.vithor.blockingpromise.makePromise
import org.junit.Assert.*
import org.junit.Test
import kotlin.concurrent.thread

class KotlinTests {

    @Test
    fun evaluateBlocking() {
        val promise = makePromise<String?> { complete ->
            val string: String? = "First"

            thread(start = true) {
                Thread.sleep(3000)
                complete.success(string)
            }
//        complete.exceptionally(IllegalAccessError("What"))
        }

        thread(start = true) {
            Thread.sleep(1000)
            println("Second")
        }

        println(promise.get())

        println("Finished")
    }

    @Test
    fun evaluateOrder() {
        makePromise<String?> { complete ->
            val string: String? = "askdf"
            Thread.sleep(3000)
            complete.success(string)
//        complete.exceptionally(IllegalAccessError("What"))
        }.get().let {
            println("First $it")
            assertEquals(it, "askdf")
        }

        val promise = makePromise<String?> { complete ->
            val string: String? = "second"
            Thread.sleep(1000)
            complete.success(string)
//        complete.exceptionally(IllegalAccessError("What"))
        }

        thread(start = true) {
            val test = promise.get()
            println(test)
            assertEquals(test, "second")
        }

        println("Finished")
        Thread.sleep(1500)
    }
}
