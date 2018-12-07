package com.buaa.cloud_evaluation.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class ItemScoreGetter {
  public ItemScoreGetter() {

    try {
      Reader reader = new FileReader("src/main/resources/item/items.json");
      Gson gson = new GsonBuilder().create();
      JsonArray ja = gson.fromJson(reader, JsonArray.class);

      items = new HashMap<>();

      ja.forEach(child -> {
        JsonObject jo = child.getAsJsonObject();
        int id = jo.get("id").getAsInt();
        int type = jo.get("type").getAsInt();
        String tableName = jo.get("table_name").getAsString();
        String itemName = jo.get("item_name").getAsString();

        Item item;
        switch (type) {
          case 0: {
            double max = jo.get("max").getAsDouble();
            double min = jo.get("min").getAsDouble();
            int direction = jo.get("direction").getAsInt();
            item = new ContinuousItem(id, type, itemName, tableName, max, min, direction);
            break;
          }
          case 1:
            item = new DispersedItem(id, type, itemName, tableName);
            break;
          case 2: {
            double max = jo.get("max").getAsDouble();
            int windowSize = jo.get("window_size").getAsInt();
            item = new WindowItem(id, type, itemName, tableName, max, windowSize);
            break;
          }
          default:
            throw new IllegalStateException("Unknown type: " + type);
        }

        items.put(id, item);
      });
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private Map<Integer, Item> items;

  public  double getScore(int itemId, int timestamp){
    Item curItem = items.get(itemId);

    if (curItem == null) {
      throw new RuntimeException("Can't find item with id: " + itemId);
    }

    return curItem.getScore(timestamp);
  }

  public static void main(String[] args) {
    ItemScoreGetter getter = new ItemScoreGetter();
  }
}
