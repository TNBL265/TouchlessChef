package app.touchlessChef.dao.recipe;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import app.touchlessChef.model.Ingredient;

/**
 * Reference: https://github.com/aza0092/Cooking-Recipe-Android-App/blob/master/app/src/main/java/dao/IngredientDAO.java
 * Adopting Data Access Object Design Pattern to provide our app an API to talk to the SQLite db
 */
public class IngredientDAO {
    private final SQLiteDatabase db;

    public IngredientDAO(SQLiteDatabase db) {
        this.db = db;
    }

    public void insert(Ingredient ingredient) {
        insert(ingredient.getName(), ingredient.getRecipeId());
    }

    public void insert(String ingredient, long recipeId) {
        ContentValues values = new ContentValues();
        values.put(Config.KEY_NAME, ingredient);
        values.put(Config.KEY_RECIPE_ID, recipeId);
        db.insert(Config.TABLE_NAME, null, values);
    }

    public List<Ingredient> selectAllByRecipeId(long recipeId) {
        List<Ingredient> ingredients = new ArrayList<>();
        try (Cursor cursor = db.query(Config.TABLE_NAME,
                new String[]{Config.KEY_ID, Config.KEY_NAME, Config.KEY_RECIPE_ID},
                Config.KEY_RECIPE_ID + " = ?", new String[]{recipeId + ""},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    ingredients.add(new Ingredient(
                            cursor.getLong(0), cursor.getString(1), cursor.getLong(2)));
                } while (cursor.moveToNext());
            }
        }
        return ingredients;
    }

    public void deleteAllByRecipeId(long recipeId) {
        db.delete(Config.TABLE_NAME, Config.KEY_RECIPE_ID + "=" + recipeId, null);
    }

    public static class Config {
        public static final String TABLE_NAME = "ingredients";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_RECIPE_ID = "recipeId";

        public static final String CREATE_TABLE_STATEMENT =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_NAME + " TEXT NOT NULL, " +
                        KEY_RECIPE_ID + " TEXT NOT NULL, " +
                        "FOREIGN KEY(" + KEY_RECIPE_ID + ") REFERENCES " +
                        RecipeDAO.Config.TABLE_NAME + "(" + RecipeDAO.Config.KEY_ID + "))";
    }
}

