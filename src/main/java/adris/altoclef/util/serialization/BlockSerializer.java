package adris.altoclef.util.serialization;

import adris.altoclef.util.helpers.ItemHelper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.apache.commons.compress.compressors.lz77support.LZ77Compressor;

import java.io.IOException;
import java.util.List;

public class BlockSerializer extends StdSerializer<Object> {
    public BlockSerializer() {
        this(null);
    }

    public BlockSerializer(Class<Object> vc) {
        super(vc);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Block[] blocks = (Block[]) value;
        gen.writeStartArray();
        for (Block block : blocks) {
            String key = ItemHelper.trimItemName(block.getTranslationKey());
            gen.writeString(key);
        }
        gen.writeEndArray();
    }
}
