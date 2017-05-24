package io.vithor.blockingpromise;

public interface AsyncAction<T> {
    void execute(FirePromise<T> complete);
}
