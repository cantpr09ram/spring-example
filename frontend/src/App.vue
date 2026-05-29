<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { LogOut, Plus } from '@lucide/vue';

import { ApiError, clearAuthToken, getAuthToken } from './api/client';
import { getCurrentUser, login, type User } from './api/auth';
import { createTodo, deleteTodo, listTodos, updateTodo, type Todo } from './api/todos';
import LoginForm from './components/new-york-v4/blocks/login-01/components/LoginForm.vue';
import TodoList from './components/TodoList.vue';
import { Button } from './components/ui/button';
import { Card, CardContent } from './components/ui/card';
import { Input } from './components/ui/input';
import { Label } from './components/ui/label';

const todos = ref<Todo[]>([]);
const title = ref('');
const loading = ref(false);
const saving = ref(false);
const error = ref('');
const authError = ref('');
const authLoading = ref(false);
const user = ref<User | null>(null);
const checkingSession = ref(true);
const authResetKey = ref(0);

const isAuthenticated = computed(() => Boolean(user.value));

function goToLogin(message = '') {
  clearAuthToken();
  user.value = null;
  todos.value = [];
  authError.value = message;
}

async function loadTodos() {
  loading.value = true;
  error.value = '';
  try {
    todos.value = await listTodos();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
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
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
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
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
    error.value = err instanceof Error ? err.message : 'Unable to update todo.';
  }
}

async function removeTodo(id: number) {
  error.value = '';
  try {
    await deleteTodo(id);
    todos.value = todos.value.filter((todo) => todo.id !== id);
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
    error.value = err instanceof Error ? err.message : 'Unable to delete todo.';
  }
}

async function submitAuth(payload: { email: string; password: string }) {
  authLoading.value = true;
  authError.value = '';
  try {
    const response = await login({
      email: payload.email.trim(),
      password: payload.password
    });

    user.value = response.user;
    authResetKey.value += 1;
    await loadTodos();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      authError.value = 'Invalid email or password.';
    } else if (err instanceof ApiError && err.status === 409) {
      authError.value = 'Email already registered.';
    } else {
      authError.value = err instanceof Error ? err.message : 'Unable to sign in.';
    }
  } finally {
    authLoading.value = false;
  }
}

function signOut() {
  goToLogin();
}

async function restoreSession() {
  checkingSession.value = true;
  if (!getAuthToken()) {
    checkingSession.value = false;
    return;
  }

  try {
    user.value = await getCurrentUser();
    await loadTodos();
  } catch (err) {
    if (err instanceof ApiError && err.status !== 401) {
      authError.value = err.message;
    }
  } finally {
    checkingSession.value = false;
  }
}

function handleAuthExpired() {
  goToLogin('Session expired. Please sign in again.');
}

onMounted(() => {
  window.addEventListener('auth:expired', handleAuthExpired);
  void restoreSession();
});

onBeforeUnmount(() => {
  window.removeEventListener('auth:expired', handleAuthExpired);
});
</script>

<template>
  <main class="min-h-screen bg-background px-3.5 py-8 text-foreground sm:px-5 sm:py-12">
    <div v-if="checkingSession" class="mx-auto max-w-[760px] text-sm text-muted-foreground">
      Loading...
    </div>

    <section v-else-if="!isAuthenticated" class="mx-auto grid min-h-[calc(100vh-4rem)] max-w-[420px] place-items-center">
      <LoginForm
        class="w-full"
        :error="authError"
        :loading="authLoading"
        :reset-key="authResetKey"
        @submit="submitAuth"
      />
    </section>

    <section v-else class="mx-auto max-w-[760px]">
      <header class="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="m-0 text-4xl leading-none font-bold sm:text-6xl">
            Full-stack starter
          </h1>
          <p class="mt-2 text-sm text-muted-foreground">{{ user?.displayName }}</p>
        </div>
        <Button type="button" variant="outline" @click="signOut">
          <LogOut />
          Sign out
        </Button>
      </header>

      <Card class="mb-4">
        <CardContent class="p-[18px]">
          <form class="grid gap-2.5" @submit.prevent="addTodo">
            <Label class="font-semibold" for="todo-title">New todo</Label>
            <div class="grid gap-2.5 sm:grid-cols-[1fr_auto]">
              <Input
                id="todo-title"
                v-model="title"
                class="min-h-11"
                type="text"
                maxlength="160"
                placeholder="Add a backend-backed task"
              />
              <Button
                class="min-h-11"
                type="submit"
                :disabled="saving || !title.trim()"
              >
                <Plus />
                {{ saving ? 'Adding...' : 'Add' }}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <p v-if="error" class="m-3 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
        {{ error }}
      </p>

      <TodoList
        :todos="todos"
        :loading="loading"
        @toggle="toggleTodo"
        @remove="removeTodo"
      />
    </section>
  </main>
</template>
