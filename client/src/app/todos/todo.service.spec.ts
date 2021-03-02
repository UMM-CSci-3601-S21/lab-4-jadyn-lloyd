import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Todo } from './todo';
import { TodoService } from './todo.service';

describe('Todo service: ', () => {
  // A small collection of test todos
  const testTodos: Todo[] = [
    {
      _id: 'chris_id',
      owner: 'Chris',
      status: true,
      category: 'video games',
      body: 'play video games too much'
    },
    {
      _id: 'pat_id',
      owner: 'Pat',
      status: false,
      category: 'procrastination',
      body: 'Do not procrastinate'
    },
    {
      _id: 'jamie_id',
      owner: 'Jamie',
      status: false,
      category: 'grades',
      body: 'Get good grades'
    }
  ];
  let todoService: TodoService;
  // These are used to mock the HTTP requests so that we (a) don't have to
  // have the server running and (b) we can check exactly which HTTP
  // requests were made to ensure that we're making the correct requests.
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    // Set up the mock handling of the HTTP requests
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    // Construct an instance of the service with the mock
    // HTTP client.
    todoService = new TodoService(httpClient);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  it('getTodos() calls api/todos', () => {
    // Assert that the todos we get from this call to getTodos()
    // should be our set of test todos. Because we're subscribing
    // to the result of getTodos(), this won't actually get
    // checked until the mocked HTTP request 'returns' a response.
    // This happens when we call req.flush(testTodos) a few lines
    // down.
    todoService.getTodos().subscribe(
      todos => expect(todos).toBe(testTodos)
    );

    // Specify that (exactly) one request will be made to the specified URL.
    const req = httpTestingController.expectOne(todoService.todoUrl);
    // Check that the request made to that URL was a GET request.
    expect(req.request.method).toEqual('GET');
    // Specify the content of the response to that request. This
    // triggers the subscribe above, which leads to that check
    // actually being performed.
    req.flush(testTodos);
  });

  it('getTodos() calls api/todos with filter parameter \'_id\'', () => {

    todoService.getTodos({ _id: 'chris_id' }).subscribe(
      todos => expect(todos).toBe(testTodos)
    );

    // Specify that (exactly) one request will be made to the specified URL with the role parameter.
    const req = httpTestingController.expectOne(
      (request) => request.url.startsWith(todoService.todoUrl) && request.params.has('id')
    );

    // Check that the request made to that URL was a GET request.
    expect(req.request.method).toEqual('GET');

    // Check that the role parameter was 'admin'
    expect(req.request.params.get('id')).toEqual('chris_id');

    req.flush(testTodos);
  });

  it('getTodos() calls api/todos with multiple filter parameters', () => {

    todoService.getTodos({ _id: 'chris_id', owner: 'Chris', category: 'video games' }).subscribe(
      todos => expect(todos).toBe(testTodos)
    );

    // Specify that (exactly) one request will be made to the specified URL with the role parameter.
    const req = httpTestingController.expectOne(
      (request) => request.url.startsWith(todoService.todoUrl)
        && request.params.has('id') && request.params.has('owner') && request.params.has('category')
    );

    // Check that the request made to that URL was a GET request.
    expect(req.request.method).toEqual('GET');

    // Check that the role parameters are correct
    expect(req.request.params.get('id')).toEqual('chris_id');
    expect(req.request.params.get('owner')).toEqual('Chris');
    expect(req.request.params.get('category')).toEqual('video games');

    req.flush(testTodos);
  });

  it('getTodoById() calls api/todos/id', () => {
    const targetTodo: Todo = testTodos[1];
    const targetId: string = targetTodo._id;
    todoService.getTodoById(targetId).subscribe(
      todo => expect(todo).toBe(targetTodo)
    );

    const expectedUrl: string = todoService.todoUrl + '/' + targetId;
    const req = httpTestingController.expectOne(expectedUrl);
    expect(req.request.method).toEqual('GET');
    req.flush(targetTodo);
  });

  it('filterTodos() filters by owner', () => {
    expect(testTodos.length).toBe(3);
    const todoOwner = 'Chris';
    expect(todoService.filterTodos(testTodos, { owner: todoOwner }).length).toBe(1);
  });

  it('filterTodos() filters by category', () => {
    expect(testTodos.length).toBe(3);
    const todoCategory = 'video games';
    expect(todoService.filterTodos(testTodos, { category: todoCategory }).length).toBe(1);
  });

  it('filterTodos() filters by category and owner', () => {
    expect(testTodos.length).toBe(3);
    const todoCategory = 'video games';
    const todoOwner = 'Chris';
    expect(todoService.filterTodos(testTodos, { category: todoCategory, owner: todoOwner }).length).toBe(1);
  });

  it('addTodo() posts to api/todos', () => {

    todoService.addTodo(testTodos[1]).subscribe(
      id => expect(id).toBe('testid')
    );

    const req = httpTestingController.expectOne(todoService.todoUrl);

    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(testTodos[1]);

    req.flush({id: 'testid'});
  });
});
