class Chunk{
    private final int chunk_size = 512*1024;
    private final int sequence_number;
    private final int max_sequence_number;
    private byte[] chunk = new byte[chunk_size];
    private long actual_length;

    public Chunk(int sequence_number,long actual_length, int max_sequence_number, byte[] chunk) {
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

    public long getActual_length() {
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
