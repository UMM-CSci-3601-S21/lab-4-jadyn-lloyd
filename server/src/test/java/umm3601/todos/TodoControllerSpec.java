package umm3601.todos;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.util.ContextUtil;
import io.javalin.plugin.json.JavalinJson;


/**
* Tests the logic of the TodosController
*
* @throws IOException
*/
public class TodoControllerSpec {

  MockHttpServletRequest mockReq = new MockHttpServletRequest();
  MockHttpServletResponse mockRes = new MockHttpServletResponse();

  private TodosController todosController;

  private ObjectId fourthId;

  static MongoClient mongoClient;
  static MongoDatabase db;

  static ObjectMapper jsonMapper = new ObjectMapper();

  @BeforeAll
  public static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");

    mongoClient = MongoClients.create(
    MongoClientSettings.builder()
    .applyToClusterSettings(builder ->
    builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
    .build());

    db = mongoClient.getDatabase("test");
  }


  @BeforeEach
  public void setupEach() throws IOException {

    // Reset our mock request and response objects
    mockReq.resetAll();
    mockRes.resetAll();

    // Setup database
    MongoCollection<Document> todoDocuments = db.getCollection("todos");
    todoDocuments.drop();
    List<Document> testTodos = new ArrayList<>();
    testTodos.add(
      new Document()
        .append("owner", "Blanche")
        .append("status", false)
        .append("body", "In sunt ex non tempor cillum commodo amet incididunt anim qui commodo quis. Cillum non labore ex sint esse.")
        .append("category", "software design"));
    testTodos.add(
      new Document()
        .append("owner", "Fry")
        .append("status", false)
        .append("body", "Ipsum esse est ullamco magna tempor anim laborum non officia deserunt veniam commodo. Aute minim incididunt ex commodo.")
        .append("category", "video games"));
    testTodos.add(
      new Document()
        .append("owner", "Fry")
        .append("status", true)
        .append("body", "Ullamco irure laborum magna dolor non. Anim occaecat adipisicing cillum eu magna in.")
        .append("category", "homework"));

    fourthId = new ObjectId();
    Document fourth =
      new Document()
        .append("_id", fourthId)
        .append("owner", "Fry")
        .append("status", true)
        .append("body", "Incididunt enim ea sit qui esse magna eu. Nisi sunt exercitation est Lorem consectetur incididunt cupidatat laboris commodo veniam do ut sint.")
        .append("category", "groceries");


    todoDocuments.insertMany(testTodos);
    todoDocuments.insertOne(fourth);

    todosController = new TodosController(db);
  }

  @AfterAll
  public static void teardown() {
    db.drop();
    mongoClient.close();
  }

  @Test
  public void GetAllTodos() throws IOException {

    // Create our fake Javalin context
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");
    todosController.getTodos(ctx);


    assertEquals(200, mockRes.getStatus());

    String result = ctx.resultString();
    assertEquals(db.getCollection("todos").countDocuments(), JavalinJson.fromJson(result, Todos[].class).length);
  }

  @Test
  public void GetTodosByOwner() throws IOException {

    // Set the query string to test with
    mockReq.setQueryString("owner=Blanche");

    // Create our fake Javalin context
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");

    todosController.getTodos(ctx);

    assertEquals(200, mockRes.getStatus()); // The response status should be 200

    String result = ctx.resultString();
    Todos[] resultTodos = JavalinJson.fromJson(result, Todos[].class);

    assertEquals(1, resultTodos.length); // There should be one todo returned
    for (Todos todo : resultTodos) {
      assertEquals("Blanche", todo.owner); // Every todo should have the owner Blanche
    }
  }

  @Test
  public void GetTodosByCategory() throws IOException {

    mockReq.setQueryString("category=homework");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");
    todosController.getTodos(ctx);

    assertEquals(200, mockRes.getStatus());
    String result = ctx.resultString();

    Todos[] resultTodos = JavalinJson.fromJson(result, Todos[].class);

    assertEquals(1, resultTodos.length); // There should be one todo returned
    for (Todos todo : resultTodos) {
      assertEquals("homework", todo.category);
    }
  }

  @Test
  public void GetTodosByStatus() throws IOException {

    mockReq.setQueryString("status=true");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");
    todosController.getTodos(ctx);

    assertEquals(200, mockRes.getStatus());
    String result = ctx.resultString();

    Todos[] resultTodos = JavalinJson.fromJson(result, Todos[].class);
    assertEquals(2, resultTodos.length); // There should be two todos returned

    for (Todos todo : resultTodos) {
      assertEquals(true, todo.status);
    }
  }

  @Test
  public void GetUsersByCategoryAndOwner() throws IOException {

    mockReq.setQueryString("category=homework&owner=Fry");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");
    todosController.getTodos(ctx);

    assertEquals(200, mockRes.getStatus());
    String result = ctx.resultString();
    Todos[] resultTodos = JavalinJson.fromJson(result, Todos[].class);

    assertEquals(1, resultTodos.length); // There should be one todo returned
    for (Todos todo : resultTodos) {
      assertEquals("homework", todo.category);
      assertEquals("Fry", todo.owner);}
    }

  @Test
  public void GetTodoWithExistentId() throws IOException {

    String testID = fourthId.toHexString();

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos/:id", ImmutableMap.of("id", testID));
    todosController.getTodo(ctx);

    assertEquals(200, mockRes.getStatus());

    String result = ctx.resultString();
    Todos resultTodo = JavalinJson.fromJson(result, Todos.class);

    assertEquals(resultTodo._id, fourthId.toHexString());
    assertEquals(resultTodo.owner, "Fry");
  }

  @Test
  public void GetTodosWithBadId() throws IOException {

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos/:id", ImmutableMap.of("id", "bad"));

    assertThrows(BadRequestResponse.class, () -> {
      todosController.getTodo(ctx);
    });
  }

  @Test
  public void GetTodosWithNonexistentId() throws IOException {

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos/:id", ImmutableMap.of("id", "58af3a600343927e48e87335"));

    assertThrows(NotFoundResponse.class, () -> {
      todosController.getTodo(ctx);
    });
  }

  @Test
  public void AddTodo() throws IOException {

    String testNewTodo = "{"
      + "\"owner\": \"Test Todo\","
      + "\"status\": false,"
      + "\"category\": \"testers\","
      + "\"body\": \"this is a test\""
      + "}";

    mockReq.setBodyContent(testNewTodo);
    mockReq.setMethod("POST");

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");

    todosController.addNewTodo(ctx);

    assertEquals(201, mockRes.getStatus());

    String result = ctx.resultString();
    String id = jsonMapper.readValue(result, ObjectNode.class).get("id").asText();
    assertNotEquals("", id);
    System.out.println(id);

    assertEquals(1, db.getCollection("todos").countDocuments(eq("_id", new ObjectId(id))));

    //verify user was added to the database and the correct ID
    Document addedTodo = db.getCollection("todos").find(eq("_id", new ObjectId(id))).first();
    assertNotNull(addedTodo);
    assertEquals("Test Todo", addedTodo.getString("owner"));
    assertEquals(false, addedTodo.getBoolean("status"));
    assertEquals("testers", addedTodo.getString("category"));
    assertEquals("this is a test", addedTodo.getString("body"));

  }

  @Test
  public void AddInvalidBodyUser() throws IOException {
    String testNewTodos = "{"
      + "\"owner\": \"Test Todo\","
      + "\"status\": false,"
      + "\"category\": \"testers\","
      + "}";
    mockReq.setBodyContent(testNewTodos);
    mockReq.setMethod("POST");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");

    assertThrows(BadRequestResponse.class, () -> {
      todosController.addNewTodo(ctx);
    });
  }

  @Test
  public void AddInvalidOwnerTodos() throws IOException {
    String testNewTodos = "{"
      + "\"category\": \"testers\","
      + "\"body\": \"this is a test\","
      + "\"status\": false,"
      + "}";
    mockReq.setBodyContent(testNewTodos);
    mockReq.setMethod("POST");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");

    assertThrows(BadRequestResponse.class, () -> {
      todosController.addNewTodo(ctx);
    });
  }

  @Test
  public void AddInvalidCategoryTodos() throws IOException {
    String testNewTodos = "{"
      + "\"owner\": \"Test Todos\","
      + "\"status\": false,"
      + "\"body\": \"this is a test\","
      + "}";
    mockReq.setBodyContent(testNewTodos);
    mockReq.setMethod("POST");
    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos");

    assertThrows(BadRequestResponse.class, () -> {
      todosController.addNewTodo(ctx);
    });
  }

  @Test
  public void DeleteTodo() throws IOException {

    String testID = fourthId.toHexString();

    // todo exists before deletion
    assertEquals(1, db.getCollection("todos").countDocuments(eq("_id", new ObjectId(testID))));

    Context ctx = ContextUtil.init(mockReq, mockRes, "api/todos/:id", ImmutableMap.of("id", testID));
    todosController.deleteTodo(ctx);

    assertEquals(200, mockRes.getStatus());

    // todo is no longer in the database
    assertEquals(0, db.getCollection("todos").countDocuments(eq("_id", new ObjectId(testID))));
  }

}
