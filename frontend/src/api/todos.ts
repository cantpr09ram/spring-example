import { request } from './client';

export interface Todo {
  id: number;
  title: string;
  completed: boolean;
  createdAt: string;
}

export interface TodoPayload {
  title: string;
  completed?: boolean;
}

export function listTodos(): Promise<Todo[]> {
  return request<Todo[]>('/api/todos');
}

export function createTodo(payload: TodoPayload): Promise<Todo> {
  return request<Todo>('/api/todos', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateTodo(todo: Todo): Promise<Todo> {
  return request<Todo>(`/api/todos/${todo.id}`, {
    method: 'PATCH',
    body: JSON.stringify({
      title: todo.title,
      completed: todo.completed
    })
  });
}

export function deleteTodo(id: number): Promise<void> {
  return request<void>(`/api/todos/${id}`, {
    method: 'DELETE'
  });
}
