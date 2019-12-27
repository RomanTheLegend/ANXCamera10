package io.reactivex.internal.observers;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public final class FutureSingleObserver<T> extends CountDownLatch implements SingleObserver<T>, Future<T>, Disposable {
    Throwable error;
    final AtomicReference<Disposable> s = new AtomicReference<>();
    T value;

    public FutureSingleObserver() {
        super(1);
    }

    public boolean cancel(boolean z) {
        Disposable disposable;
        DisposableHelper disposableHelper;
        do {
            disposable = this.s.get();
            if (disposable == this) {
                return false;
            }
            disposableHelper = DisposableHelper.DISPOSED;
            if (disposable == disposableHelper) {
                return false;
            }
        } while (!this.s.compareAndSet(disposable, disposableHelper));
        if (disposable != null) {
            disposable.dispose();
        }
        countDown();
        return true;
    }

    public void dispose() {
    }

    public T get() throws InterruptedException, ExecutionException {
        if (getCount() != 0) {
            BlockingHelper.verifyNonBlocking();
            await();
        }
        if (!isCancelled()) {
            Throwable th = this.error;
            if (th == null) {
                return this.value;
            }
            throw new ExecutionException(th);
        }
        throw new CancellationException();
    }

    public T get(long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        if (getCount() != 0) {
            BlockingHelper.verifyNonBlocking();
            if (!await(j, timeUnit)) {
                throw new TimeoutException();
            }
        }
        if (!isCancelled()) {
            Throwable th = this.error;
            if (th == null) {
                return this.value;
            }
            throw new ExecutionException(th);
        }
        throw new CancellationException();
    }

    public boolean isCancelled() {
        return DisposableHelper.isDisposed(this.s.get());
    }

    public boolean isDisposed() {
        return isDone();
    }

    public boolean isDone() {
        return getCount() == 0;
    }

    public void onError(Throwable th) {
        Disposable disposable;
        do {
            disposable = this.s.get();
            if (disposable == DisposableHelper.DISPOSED) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.error = th;
        } while (!this.s.compareAndSet(disposable, this));
        countDown();
    }

    public void onSubscribe(Disposable disposable) {
        DisposableHelper.setOnce(this.s, disposable);
    }

    public void onSuccess(T t) {
        Disposable disposable = this.s.get();
        if (disposable != DisposableHelper.DISPOSED) {
            this.value = t;
            this.s.compareAndSet(disposable, this);
            countDown();
        }
    }
}
