package umm3601.todos;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

/**
 * Controller that manages requests for info about todos.
 */
public class TodosController {

  private static final String OWNER_KEY = "owner";
  private static final String CATEGORY_KEY = "category";
  private static final String BODY_KEY = "body";
  private static final String STATUS_KEY = "status";


  private final JacksonMongoCollection<Todos> todosCollection;

  /**
   * Construct a controller for todos.
   *
   * @param database the database containing todos data
   */
  public TodosController(MongoDatabase database) {
    todosCollection = JacksonMongoCollection.builder().build(database, "todos", Todos.class);
  }

  /**
   * Get the single todo specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodo(Context ctx) {
    String id = ctx.pathParam("id");
    Todos todo;

    try {
      todo = todosCollection.find(eq("_id", new ObjectId(id))).first();
    } catch(IllegalArgumentException e) {
      throw new BadRequestResponse("The requested todo id wasn't a legal Mongo Object ID.");
    }
    if (todo == null) {
      throw new NotFoundResponse("The requested todo was not found");
    } else {
      ctx.json(todo);
    }
  }

  /**
   * Delete the todo specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void deleteTodo(Context ctx) {
    String id = ctx.pathParam("id");
    todosCollection.deleteOne(eq("_id", new ObjectId(id)));
  }

  /**
   * Get a JSON response with a list of all the todos.
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodos(Context ctx) {

    List<Bson> filters = new ArrayList<>(); // start with a blank document

    if (ctx.queryParamMap().containsKey(OWNER_KEY)) {
        filters.add(regex(OWNER_KEY, Pattern.quote(ctx.queryParam(OWNER_KEY)),"i"));
    }

    if (ctx.queryParamMap().containsKey(BODY_KEY)) {
      filters.add(regex(BODY_KEY,  Pattern.quote(ctx.queryParam(BODY_KEY)), "cillum"));
    }

    if (ctx.queryParamMap().containsKey(CATEGORY_KEY)) {
      filters.add(eq(CATEGORY_KEY, ctx.queryParam(CATEGORY_KEY)));
    }

    if (ctx.queryParamMap().containsKey(STATUS_KEY)) {
      boolean targetBoolean = ctx.queryParam(STATUS_KEY,Boolean.class).get();
      filters.add(eq(STATUS_KEY,  targetBoolean));
    }

    String sortBy = ctx.queryParam("sortby", "owner"); //Sort by sort query param, default is owner
    String sortOrder = ctx.queryParam("sortorder", "asc");

    ctx.json(todosCollection.find(filters.isEmpty() ? new Document() : and(filters))
      .sort(sortOrder.equals("desc") ?  Sorts.descending(sortBy) : Sorts.ascending(sortBy))
      .into(new ArrayList<>()));
  }

  /**
   * Add a new todo
   *
   * @param ctx a Javalin HTTP context
   */
  public void addNewTodo(Context ctx) {

    Todos newTodo = ctx.bodyValidator(Todos.class)
      .check(todo -> todo.owner != null && todo.owner.length() > 0) //Verify that the todo has an owner that is not blank
      .check(todo -> todo.status == true || todo.status == false) // Verify that the todo has a status that is either true or false
      .check(todo -> todo.category != null && todo.category.length() > 0) // Verify that the todo has a category that is not blank
      .check(todo -> todo.body != null && todo.body.length() > 0) // Verify that the todo has a body that is not blank

      .get();

    todosCollection.insertOne(newTodo);
    ctx.status(201);
    ctx.json(ImmutableMap.of("id", newTodo._id));
  }
}
