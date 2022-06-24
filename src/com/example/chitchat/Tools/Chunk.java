
package com.example.chitchat.Tools;
import java.io.Serializable;

import java.io.Serializable;

public class Chunk implements Serializable {
    private final int chunk_size = 512*1024;
    private final int sequence_number;
    private final int max_sequence_number;
    private final byte[] chunk;
    private final int actual_length;

    private static final long serialVersionUID = -2L;

    public Chunk(int sequence_number,int actual_length, int max_sequence_number, byte[] chunk) {
        this.sequence_number = sequence_number;
        this.actual_length = actual_length;
        this.max_sequence_number = max_sequence_number;
        this.chunk = chunk;
    }

    public int getChunk_size() {
        return chunk_size;
    }

    public int getSequence_number() {
        return sequence_number;
    }

    public int getMax_sequence_number() {
        return max_sequence_number;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public int getActual_length() {
        return actual_length;
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "sequence_number=" + sequence_number +
                ", max_sequence_number=" + max_sequence_number +
                ", actual_length=" + actual_length +
                '}';
    }
}
