package net.minecraft.util;

/**
 * 26.3 移除了原版 {@code net.minecraft.util.Tuple}。本地桩实现，仅供编译使用。
 */
public class Tuple<A, B> {
    private A first;
    private B second;

    public Tuple(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return this.first;
    }

    public B getSecond() {
        return this.second;
    }

    public void setFirst(final A first) {
        this.first = first;
    }

    public void setSecond(final B second) {
        this.second = second;
    }
}
