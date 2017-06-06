package amidst.seedSearch.filter;

import com.google.gson.*;

import java.lang.reflect.Type;

public class FilterSerializer implements JsonDeserializer<Filter>
{
    @Override
    public Filter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {

        JsonObject jsonObject =  json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get("type");
        String type = "amidst.seedSearch.filter." + prim.getAsString().toLowerCase() + "." + prim.getAsString() + "Filter";

        try
        {
            return context.deserialize(jsonObject, Class.forName(type));
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
