package com.terraformersmc.biolith.impl.biome;

import java.util.Iterator;

public class SimpleArrayIterator<T> implements Iterator<T> {
    private final T[] array;
    private int index;

    public SimpleArrayIterator(T[] array) {
        super();

        this.array = array;
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < array.length;
    }

    @Override
    public T next() {
        return array[index++];
    }
}
