package adris.altoclef.util.serialization;

import adris.altoclef.Debug;
import adris.altoclef.util.helpers.ItemHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockDeserializer extends StdDeserializer<Object> {
    public BlockDeserializer() {
        this(null);
    }

    public BlockDeserializer(Class<Object> vc) {
        super(vc);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        List<Block> result = new ArrayList<>();

        if (p.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new JsonParseException(p, "Start array expected");
        }

        while (p.nextToken() != JsonToken.END_ARRAY) {
            Block block = null;

            // Translation key (the proper way)
            String blockKey = p.getText();
            blockKey = ItemHelper.trimItemName(blockKey);
            Identifier identifier = new Identifier(blockKey);
            if (Registries.BLOCK.containsId(identifier)) {
                block = Registries.BLOCK.get(identifier);
            } else {
                Debug.logWarning("Invalid block name:" + blockKey + " at " + p.getCurrentLocation().toString());
            }

            if (block != null) {
                result.add(block);
            }
        }

        return result;
    }
}
