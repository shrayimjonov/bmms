package uz.paykap.gateway.dto;

public class Pair<N, V> {

    private final N name;
    private final V value;

    public Pair(N name, V value) {
        this.name = name;
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public N getName() {
        return name;
    }
}
