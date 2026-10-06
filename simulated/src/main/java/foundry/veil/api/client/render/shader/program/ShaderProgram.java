package foundry.veil.api.client.render.shader.program;

import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parsed view over a vertex+fragment shader pair.
 *
 * <p>26.3 no longer exposes {@code ShaderInstance} with its {@code getUniform(String)}
 * lookup against a live GL program, and renderpearl does not hand back reflected
 * uniform metadata either. Instead of stubbing that API out, this class reads the
 * GLSL sources and derives the std140 layout itself, which is exactly the contract
 * the render pass needs: block name, byte offset and component count per uniform.
 */
public final class ShaderProgram {
    private static final Pattern BLOCK = Pattern.compile(
            "layout\\s*\\(\\s*std140\\s*\\)\\s*uniform\\s+(\\w+)\\s*\\{([^}]*)}", Pattern.DOTALL);
    private static final Pattern MEMBER = Pattern.compile(
            "\\b(float|int|bool|vec[234]|ivec[234]|bvec[234]|mat[234](?:x[234])?)\\s+(\\w+)\\s*(?:\\[[^]]*\\])?\\s*;");
    private static final Pattern SAMPLER = Pattern.compile(
            "\\b(uniform)\\s+(sampler\\w+|itexture\\w+|utexture\\w+)\\s+(\\w+)\\s*;");

    private static final String SHADER_PATH = "shaders/";
    private static final String VERTEX_EXTENSION = ".vsh";
    private static final String FRAGMENT_EXTENSION = ".fsh";

    private final Identifier location;
    private final Map<String, ShaderUniform> uniforms = new LinkedHashMap<>();
    private final Map<String, Integer> blockSizes = new LinkedHashMap<>();
    private final Set<String> samplers = new LinkedHashSet<>();

    private ShaderProgram(final Identifier location) {
        this.location = location;
    }

    /**
     * Parses both stages of {@code location} (for example {@code simulated:post/diagram})
     * and merges their uniform declarations.
     */
    public static ShaderProgram compile(final ResourceManager resources, final Identifier location) {
        final ShaderProgram program = new ShaderProgram(location);
        program.parse(resources, source(resources, location, VERTEX_EXTENSION));
        program.parse(resources, source(resources, location, FRAGMENT_EXTENSION));
        return program;
    }

    private static String source(final ResourceManager resources, final Identifier location, final String extension) {
        final Identifier file = Identifier.fromNamespaceAndPath(
                location.getNamespace(), SHADER_PATH + location.getPath() + extension);
        final Optional<Resource> resource = resources.getResource(file);
        if (resource.isEmpty()) {
            throw new IllegalStateException("Missing shader source " + file);
        }

        try (InputStream stream = resource.get().open()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to read shader source " + file, e);
        }
    }

    private void parse(final ResourceManager resources, final String source) {
        final Matcher samplerMatcher = SAMPLER.matcher(source);
        while (samplerMatcher.find()) {
            this.samplers.add(samplerMatcher.group(3));
        }

        final Matcher blockMatcher = BLOCK.matcher(source);
        while (blockMatcher.find()) {
            final String blockName = blockMatcher.group(1);
            final String body = blockMatcher.group(2);

            // Block sizes only depend on their members, so re-parsing the other stage
            // simply recomputes the same value.
            int offset = 0;
            int largestAlignment = 16;

            final Matcher memberMatcher = MEMBER.matcher(body);
            while (memberMatcher.find()) {
                final String type = memberMatcher.group(1);
                final String name = memberMatcher.group(2);

                final int alignment = alignment(type);
                final int size = size(type);
                largestAlignment = Math.max(largestAlignment, alignment);
                offset = align(offset, alignment);

                this.uniforms.put(name, new ShaderUniform(blockName, offset, components(type)));
                offset += size;
            }

            final int blockSize = align(offset, largestAlignment);
            final Integer known = this.blockSizes.get(blockName);
            this.blockSizes.put(blockName, known == null ? blockSize : Math.max(known, blockSize));
        }
    }

    /** @return {@code true} if {@code name} is a sampler declared by either stage. */
    public boolean hasSampler(final String name) {
        return this.samplers.contains(name);
    }

    /** @return the uniform with the given name, or {@code null} when it is not declared. */
    public ShaderUniform getUniform(final String name) {
        return this.uniforms.get(name);
    }

    public Set<String> samplerNames() {
        return Set.copyOf(this.samplers);
    }

    public Set<String> uniformNames() {
        return Set.copyOf(this.uniforms.keySet());
    }

    /**
     * Stages every uniform into freshly allocated std140 blocks, keyed by block name.
     * The caller uploads each buffer with
     * {@link com.mojang.renderpearl.api.commands.CommandEncoder#writeToBuffer}.
     */
    public Map<String, ByteBuffer> packUniformBlocks() {
        final Map<String, ByteBuffer> packed = new HashMap<>();
        for (final Map.Entry<String, Integer> entry : this.blockSizes.entrySet()) {
            packed.put(entry.getKey(), ByteBuffer.allocate(entry.getValue()));
        }

        for (final ShaderUniform uniform : this.uniforms.values()) {
            final ByteBuffer buffer = packed.get(uniform.block());
            if (buffer != null) {
                uniform.write(buffer);
            }
        }

        return packed;
    }

    public Identifier location() {
        return this.location;
    }

    private static int align(final int value, final int alignment) {
        final int remainder = value % alignment;
        return remainder == 0 ? value : value + alignment - remainder;
    }

    private static int alignment(final String type) {
        return switch (type) {
            case "float", "int", "bool" -> 4;
            case "vec2", "ivec2", "bvec2" -> 8;
            case "vec3", "ivec3", "bvec3", "vec4", "ivec4", "bvec4",
                    "mat2", "mat3", "mat4", "mat2x2", "mat2x3", "mat2x4",
                    "mat3x2", "mat3x3", "mat3x4", "mat4x2", "mat4x3", "mat4x4" -> 16;
            default -> throw new IllegalArgumentException("Unsupported std140 type: " + type);
        };
    }

    private static int size(final String type) {
        return switch (type) {
            case "float", "int", "bool" -> 4;
            case "vec2", "ivec2", "bvec2" -> 8;
            case "vec3", "ivec3", "bvec3" -> 12;
            case "vec4", "ivec4", "bvec4" -> 16;
            case "mat2", "mat2x2" -> 32;
            case "mat2x3", "mat3x2" -> 48;
            case "mat2x4", "mat4x2" -> 64;
            case "mat3", "mat3x3" -> 48;
            case "mat3x4", "mat4x3" -> 64;
            case "mat4", "mat4x4" -> 64;
            default -> throw new IllegalArgumentException("Unsupported std140 type: " + type);
        };
    }

    private static int components(final String type) {
        return switch (type) {
            case "float", "int", "bool" -> 1;
            case "vec2", "ivec2", "bvec2" -> 2;
            case "vec3", "ivec3", "bvec3" -> 3;
            case "vec4", "ivec4", "bvec4" -> 4;
            default -> 4;
        };
    }
}
