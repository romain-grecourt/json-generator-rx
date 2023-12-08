package com.acme.json.rx;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import io.helidon.common.http.DataChunk;
import io.helidon.common.reactive.EmittingPublisher;
import io.helidon.common.reactive.Multi;

public class JsonMultiOutputStream extends OutputStream implements Multi<DataChunk> {

    private enum State {
        INIT {
            @Override
            boolean canEmit() {
                return false;
            }

            @Override
            boolean isTerminated() {
                return false;
            }
        },
        READY_TO_EMIT {
            @Override
            boolean canEmit() {
                return true;
            }

            @Override
            boolean isTerminated() {
                return false;
            }
        },
        CLOSED {
            @Override
            boolean canEmit() {
                return true;
            }

            @Override
            boolean isTerminated() {
                return false;
            }
        },
        CANCELED {
            @Override
            boolean canEmit() {
                return false;
            }

            @Override
            boolean isTerminated() {
                return true;
            }
        },
        FAILED {
            @Override
            boolean canEmit() {
                return false;
            }

            @Override
            boolean isTerminated() {
                return true;
            }
        },
        COMPLETED {
            @Override
            boolean canEmit() {
                return false;
            }

            @Override
            boolean isTerminated() {
                return true;
            }
        };

        abstract boolean canEmit();

        abstract boolean isTerminated();
    }

    private static final int BUFFER_SIZE = 4 * 1024;
    private static final DataChunk FLUSH_CHUNK = DataChunk.create(true);

    private final EmittingPublisher<DataChunk> emitter = EmittingPublisher.create();
    private final Deque<DataChunk> buffer = new ConcurrentLinkedDeque<>();
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final AtomicReference<State> state = new AtomicReference<>(State.INIT);

    private JsonMultiOutputStream() {
        emitter.onCancel(() -> state.updateAndGet(s -> s.isTerminated() ? s : State.CANCELED));
        emitter.onRequest((n, demand) -> state.compareAndSet(State.INIT, State.READY_TO_EMIT));
    }

    public static JsonMultiOutputStream create() {
        return new JsonMultiOutputStream();
    }

    JsonMultiOutputStream onRequest(BiConsumer<Long, Long> requestCallback) {
        this.emitter.onRequest(requestCallback);
        return this;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super DataChunk> subscriber) {
        emitter.subscribe(subscriber);
    }

    @Override
    public void write(byte[] b) {
        publishBufferedMaybe();
        publish(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        publishBufferedMaybe();
        publish(b, off, len);
    }

    @Override
    public void write(int b) {
        if (!byteBuffer.hasRemaining()) {
            publish();
        }
        byteBuffer.put((byte) b);
    }

    @Override
    public void close() {
        flush();
        state.updateAndGet(s -> s.isTerminated() ? s : State.CLOSED);
    }

    @Override
    public void flush() {
        drain();
        publishBufferedMaybe();
        doPublish(FLUSH_CHUNK);
    }

    boolean ready() {
        return state.get() == State.READY_TO_EMIT && emitter.hasRequests();
    }

    boolean closed() {
        return state.get() == State.CLOSED;
    }

    void drain() {
        while (state.get().canEmit() && emitter.hasRequests() && !buffer.isEmpty()) {
            DataChunk chunk = buffer.poll();
            if (chunk != null && !emitter.emit(chunk)) {
                fail(new IllegalStateException("Unable to emit chunk"));
                break;
            }
        }
    }

    void complete() {
        emitter.complete();
        state.updateAndGet(s -> s.isTerminated() ? s : State.COMPLETED);
    }

    void fail(Throwable t) {
        emitter.fail(t);
        state.updateAndGet(s -> s.isTerminated() ? s : State.FAILED);
    }

    private void publishBufferedMaybe() {
        if (byteBuffer.position() > 0) {
            publish();
        }
    }

    private void publish(byte[] b, int off, int len) {
        ByteBuffer emitBuffer = ByteBuffer.allocate(len - off);
        emitBuffer.put(b, off, len);
        emitBuffer.flip();
        doPublish(DataChunk.create(emitBuffer));
    }

    private void publish() {
        byteBuffer.flip();
        ByteBuffer emitBuffer = ByteBuffer.allocate(byteBuffer.remaining());
        emitBuffer.put(byteBuffer);
        emitBuffer.flip();
        doPublish(DataChunk.create(emitBuffer));
        byteBuffer.clear();
    }

    private void doPublish(DataChunk emitChunk) {
        if (!ready() || !emitter.emit(emitChunk)) {
            buffer.add(emitChunk);
        }
    }
}
