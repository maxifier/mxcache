package com.maxifier.mxcache.legacy.layered;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 03.02.2009
 * Time: 17:28:31
 */
public final class MxList<T extends MxList.Element<T>> {
    private T first;
    private T last;
    private int size;

    public static class Element<T extends Element> {
        private boolean inList;
        private T prev;
        private T next;
    }

    public T remove(T e) {
        assert e.inList : "Cannot remove from list as element is not in list";
        e.inList = false;
        size--;
        if (e.prev == null) {
            first = e.next;
        } else {
            e.prev.next = e.next;
        }
        if (e.next == null) {
            last = e.prev;
        } else {
            e.next.prev = e.prev;
        }
        T t = e.next;
        e.prev = null;
        e.next = null;
        return t;
    }

    public void addToTail(T e) {
        assert !e.inList : "Element already in list";
        assert e.next == null : "Element is not in list but is linked to some element";
        assert e.prev == null : "Element is not in list but is linked to some element";
        size++;
        e.inList = true;
        if (first == null) {
            first = e;
            last = e;
            e.inList = true;
        } else {
            assert last.next == null;
            last.next = e;
            e.prev = last;
            last = e;
        }
    }

    public T getFirst() {
        return first;
    }

    public int getSize() {
        return size;
    }
}
