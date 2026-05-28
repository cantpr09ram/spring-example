<script setup lang="ts">
import { onMounted, ref } from 'vue';
import TodoList from './components/TodoList.vue';
import { createTodo, deleteTodo, listTodos, updateTodo, type Todo } from './api/todos';

const todos = ref<Todo[]>([]);
const title = ref('');
const loading = ref(false);
const saving = ref(false);
const error = ref('');

async function loadTodos() {
  loading.value = true;
  error.value = '';
  try {
    todos.value = await listTodos();
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load todos.';
  } finally {
    loading.value = false;
  }
}

async function addTodo() {
  const trimmedTitle = title.value.trim();
  if (!trimmedTitle) {
    return;
  }

  saving.value = true;
  error.value = '';
  try {
    const todo = await createTodo({ title: trimmedTitle });
    todos.value = [...todos.value, todo];
    title.value = '';
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to add todo.';
  } finally {
    saving.value = false;
  }
}

async function toggleTodo(todo: Todo) {
  error.value = '';
  try {
    const updated = await updateTodo({ ...todo, completed: !todo.completed });
    todos.value = todos.value.map((item) => (item.id === updated.id ? updated : item));
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to update todo.';
  }
}

async function removeTodo(id: number) {
  error.value = '';
  try {
    await deleteTodo(id);
    todos.value = todos.value.filter((todo) => todo.id !== id);
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to delete todo.';
  }
}

onMounted(loadTodos);
</script>

<template>
  <main class="mx-auto max-w-[760px] px-3.5 py-8 text-[#1b1f24] sm:px-5 sm:py-14">
    <header class="mb-7">
      <h1 class="m-0 text-[clamp(2rem,8vw,4.5rem)] leading-none font-bold">
        Full-stack starter
      </h1>
    </header>

    <form
      class="mb-4 grid gap-2.5 rounded-lg border border-[#dde3ea] bg-white p-[18px] shadow-[0_10px_30px_rgb(31_41_55_/_8%)]"
      @submit.prevent="addTodo"
    >
      <label class="font-extrabold" for="todo-title">New todo</label>
      <div class="grid gap-2.5 sm:grid-cols-[1fr_auto]">
        <input
          id="todo-title"
          v-model="title"
          class="min-h-11 min-w-0 rounded-md border border-[#c9d3dd] px-3"
          type="text"
          maxlength="160"
          placeholder="Add a backend-backed task"
        />
        <button
          class="min-h-11 cursor-pointer rounded-md bg-[#176b87] px-[18px] font-bold text-white disabled:cursor-not-allowed disabled:opacity-55"
          type="submit"
          :disabled="saving || !title.trim()"
        >
          {{ saving ? 'Adding...' : 'Add' }}
        </button>
      </div>
    </form>

    <p v-if="error" class="m-3 rounded-md border border-[#fed7d7] bg-[#fff5f5] p-3 text-[#9b2c2c]">
      {{ error }}
    </p>

    <TodoList
      :todos="todos"
      :loading="loading"
      @toggle="toggleTodo"
      @remove="removeTodo"
    />
  </main>
</template>
