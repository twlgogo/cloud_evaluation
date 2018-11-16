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
        double max = jo.get("max").getAsDouble();
        double min = jo.get("min").getAsDouble();
        int direction = jo.get("direction").getAsInt();

        Item item;
        switch (type) {
          case 0:
            item = new ContinuousItem(id, direction, type, max, min, itemName, tableName);
            break;
          case 1:
            item = new WindowItem(id, direction, type, max, min, itemName, tableName);
            break;
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
    return curItem.getScore(timestamp);
  }

  public static void main(String[] args) {
    System.out.println(new ItemScoreGetter().items);
  }
}
