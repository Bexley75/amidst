package amidst.seedSearch.search;

import amidst.seedSearch.filter.Filter;
import com.google.gson.*;

import java.lang.reflect.Type;

public class SearchSerializer implements JsonDeserializer<Search>
{
    @Override
    public Search deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get("type");
        String type = "amidst.seedSearch.search." + prim.getAsString() + "Search";

        try
        {
            return context.deserialize(jsonObject, Class.forName(type));
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
