package umm3601.todos;

import org.mongojack.Id;
import org.mongojack.ObjectId;

public class Todos {

  @ObjectId @Id
  public String _id;

  public String owner;
  public boolean status;
  public String category;
  public String body;
}
