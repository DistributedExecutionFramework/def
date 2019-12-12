package at.enfilo.def.node.queue;

import org.apache.thrift.TBase;

public interface IQueueObserver<T extends TBase> {
    void notifyQueueReleased(Queue<T> queue);
    void notifyQueuePaused(Queue<T> queue);
    void notifyNewElement(Queue<T> queue);
}
