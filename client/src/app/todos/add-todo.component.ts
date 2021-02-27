import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Todo } from './todo';
import { TodoService } from './todo.service';

@Component({
  selector: 'app-add-todo',
  templateUrl: './add-todo.component.html',
  styleUrls: ['./add-todo.component.scss']
})
export class AddTodoComponent implements OnInit {

  addTodoForm: FormGroup;

  todo: Todo;

    // not sure if this name is magical and making it be found or if I'm missing something,
  // but this is where the red text that shows up (when there is invalid input) comes from
  addTodoValidationMessages = {
    _id: [
      {type: 'required', message: 'An id is required'},
      {type: 'minlength', message: 'id must be at least 4 characters long'},
      {type: 'maxlength', message: 'id cannot be more than 50 characters long'},
      {type: 'existingId', message: 'id has already been taken'}
    ],
    owner: [
      {type: 'required', message: 'An owner is required'},
      {type: 'minlength', message: 'Owner name must be at least 2 characters long'},
      {type: 'maxlength', message: 'Owner name cannot be more than 50 characters long'},
    ],
    status: [
      {type: 'required', message: 'A status is required'}
    ],
    category: [
      {type: 'required', message: 'A category is required'},
      {type: 'minlength', message: 'Category must be at least a character long'},
      {type: 'maxlength', message: 'Category cannot be more than 50 characters long'},
    ],
    body: [
      {type: 'required', message: 'A body is required'},
      {type: 'minlength', message: 'Body must be at least 2 characters long'},
      {type: 'maxlength', message: 'Body cannot be more than 50 characters long'},
    ]
  };

  constructor(private fb: FormBuilder, private todoService: TodoService, private snackBar: MatSnackBar, private router: Router) {
  }

  createForms() {

    // add user form validations
    this.addTodoForm = this.fb.group({
      // We allow alphanumeric input and limit the length for name.
      _id: new FormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(50),
        (fc) => {
          if (fc.value.toLowerCase() === 'abc123' || fc.value.toLowerCase() === '123abc') {
            return ({existingId: true});
          } else {
            return null;
          }
        },
      ])),
      owner: new FormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50)
      ])),
      status: new FormControl('', Validators.compose([
        Validators.required
      ])),
      category: new FormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50)
      ])),
      body: new FormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(50)
      ]))
    });
  }

  ngOnInit() {
    this.createForms();
  }


  submitForm() {
    this.todoService.addUser(this.addTodoForm.value).subscribe(newID => {
      this.snackBar.open('Added Todo ' + this.addTodoForm.value.name, null, {
        duration: 2000,
      });
      this.router.navigate(['/todos/', newID]);
    }, err => {
      this.snackBar.open('Failed to add the todo', 'OK', {
        duration: 5000,
      });
    });
  }

}
