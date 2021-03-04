import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Todo } from './todo';
import { map } from 'rxjs/operators';

@Injectable()
export class TodoService {
  readonly todoUrl: string = environment.apiUrl + 'todos';

  constructor(private httpClient: HttpClient) {
  }

  getTodos(filters?: { _id?: string; owner?: string; category?: string }): Observable<Todo[]> {
    let httpParams: HttpParams = new HttpParams();
    if (filters) {
      if (filters._id) {
        filters._id = filters._id.toLowerCase();
        httpParams = httpParams.set('id', filters._id);
      }
      if (filters.owner) {
        filters.owner = filters.owner.toLowerCase();
        httpParams = httpParams.set('owner', filters.owner);
      }
      if (filters.category) {
        filters.category = filters.category.toLowerCase();
        httpParams = httpParams.set('category', filters.category);
      }
    }
    return this.httpClient.get<Todo[]>(this.todoUrl, {
      params: httpParams,
    });
  }

  getTodoById(id: string): Observable<Todo> {
    return this.httpClient.get<Todo>(this.todoUrl + '/' + id);
  }

  filterTodos(todos: Todo[], filters: { _id?: string; owner?: string; category?: string }): Todo[] {

    let filteredTodos = todos;

    // Filter by name
    if (filters._id) {
      filteredTodos = filteredTodos.filter(todo => todo._id.indexOf(filters._id) !== -1);
    }

    // Filter by company
    if (filters.owner) {
      filteredTodos = filteredTodos.filter(todo => todo.owner.indexOf(filters.owner) !== -1);
    }

    // Filter by category
    if (filters.category) {
      filteredTodos = filteredTodos.filter(todo => todo.category.indexOf(filters.category) !== -1);
    }

    return filteredTodos;
  }

  addTodo(newTodo: Todo): Observable<string> {
    // Send post request to add a new todo with the todo data as the body.
    return this.httpClient.post<{id: string}>(this.todoUrl, newTodo).pipe(map(res => res.id));
  }
}
