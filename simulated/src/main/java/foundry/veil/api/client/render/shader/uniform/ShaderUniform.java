package foundry.veil.api.client.render.shader.uniform;

/**
 * A located uniform: the std140 block it belongs to, its byte offset inside that
 * block and how many components it occupies.
 *
 * <p>Values are staged as a {@code float[4]} scratch so a caller may keep issuing
 * {@link #setVector(float, float)} style updates across frames; only the first
 * {@link #arity()} components ever reach the GPU buffer.
 */
public final class ShaderUniform implements ShaderUniformAccess {
    private final String block;
    private final int offset;
    private final int arity;

    private final float[] values = new float[4];

    public ShaderUniform(final String block, final int offset, final int arity) {
        if (arity < 1 || arity > 4) {
            throw new IllegalArgumentException("Unsupported uniform arity: " + arity);
        }
        this.block = block;
        this.offset = offset;
        this.arity = arity;
    }

    public String block() {
        return this.block;
    }

    public int offset() {
        return this.offset;
    }

    public int arity() {
        return this.arity;
    }

    @Override
    public void setFloat(final float value) {
        this.values[0] = value;
    }

    @Override
    public void setVector(final float x, final float y) {
        this.values[0] = x;
        this.values[1] = y;
    }

    @Override
    public void setVector(final float x, final float y, final float z, final float w) {
        this.values[0] = x;
        this.values[1] = y;
        this.values[2] = z;
        this.values[3] = w;
    }

    /** Copies the staged components into {@code dst} at this uniform's offset. */
    public void write(final java.nio.ByteBuffer dst) {
        for (int i = 0; i < this.arity; i++) {
            dst.putFloat(this.offset + i * 4, this.values[i]);
        }
    }

    @Override
    public String toString() {
        return "ShaderUniform[" + this.block + "@" + this.offset + ", " + this.arity + " components]";
    }
}
