<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { Plus } from '@lucide/vue';

import { ApiError, clearAuthToken, getAuthToken } from './api/client';
import { getCurrentUser, listUsers, login, type User } from './api/auth';
import { createTodo, deleteTodo, listTodos, updateTodo, type Todo } from './api/todos';
import AppSidebar from './components/AppSidebar.vue';
import LoginForm from './components/LoginForm.vue';
import TodoList from './components/TodoList.vue';
import UserTable from './components/UserTable.vue';
import { Button } from './components/ui/button';
import { Card, CardContent } from './components/ui/card';
import { Input } from './components/ui/input';
import { Label } from './components/ui/label';
import { Separator } from './components/ui/separator';
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from './components/ui/sidebar';

const todos = ref<Todo[]>([]);
const accounts = ref<User[]>([]);
const title = ref('');
const loading = ref(false);
const usersLoading = ref(false);
const saving = ref(false);
const error = ref('');
const usersError = ref('');
const authError = ref('');
const authLoading = ref(false);
const user = ref<User | null>(null);
const checkingSession = ref(true);
const authResetKey = ref(0);
const currentPath = ref(window.location.pathname);

const isAuthenticated = computed(() => Boolean(user.value));
const isAdmin = computed(() => user.value?.role === 'ADMIN');
const isUsersPage = computed(() => currentPath.value === '/user');
const currentTab = computed<'todos' | 'users'>(() => (isUsersPage.value ? 'users' : 'todos'));

function goToLogin(message = '') {
  clearAuthToken();
  user.value = null;
  todos.value = [];
  accounts.value = [];
  authError.value = message;
}

function navigateTo(path: string) {
  if (currentPath.value === path) {
    return;
  }

  window.history.pushState({}, '', path);
  currentPath.value = path;
  void loadCurrentPage();
}

async function loadCurrentPage() {
  if (isUsersPage.value) {
    await loadUsers();
    return;
  }

  await loadTodos();
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

async function loadUsers() {
  accounts.value = [];
  usersError.value = '';

  if (!isAdmin.value) {
    return;
  }

  usersLoading.value = true;
  try {
    accounts.value = await listUsers();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
    usersError.value = err instanceof Error ? err.message : 'Unable to load users.';
  } finally {
    usersLoading.value = false;
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
    await loadCurrentPage();
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
    await loadCurrentPage();
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

function handlePopState() {
  currentPath.value = window.location.pathname;
  if (isAuthenticated.value) {
    void loadCurrentPage();
  }
}

onMounted(() => {
  window.addEventListener('auth:expired', handleAuthExpired);
  window.addEventListener('popstate', handlePopState);
  void restoreSession();
});

onBeforeUnmount(() => {
  window.removeEventListener('auth:expired', handleAuthExpired);
  window.removeEventListener('popstate', handlePopState);
});
</script>

<template>
  <main class="min-h-screen bg-background text-foreground">
    <div v-if="checkingSession" class="mx-auto max-w-[760px] text-sm text-muted-foreground">
      Loading...
    </div>

    <section v-else-if="!isAuthenticated" class="mx-auto grid min-h-screen max-w-[420px] place-items-center px-3.5 py-8 sm:px-5">
      <LoginForm
        class="w-full"
        :error="authError"
        :loading="authLoading"
        :reset-key="authResetKey"
        @submit="submitAuth"
      />
    </section>

    <SidebarProvider v-else>
      <AppSidebar
        v-if="user"
        :current-tab="currentTab"
        :is-admin="isAdmin"
        :user="user"
        @navigate="navigateTo"
        @sign-out="signOut"
      />

      <SidebarInset>
        <header class="flex h-14 shrink-0 items-center gap-2 border-b px-4 transition-[width,height] ease-linear lg:px-6">
          <SidebarTrigger class="-ml-1" />
          <Separator
            orientation="vertical"
            class="mx-2 data-[orientation=vertical]:h-4"
          />
          <div class="min-w-0">
            <h1 class="m-0 text-base font-medium leading-tight">
              {{ isUsersPage ? 'Users' : 'Todos' }}
            </h1>
          </div>
        </header>

        <div class="px-3.5 py-6 sm:px-5 sm:py-8">
          <div class="mx-auto max-w-[980px]">
            <header class="mb-7">
              <div class="min-w-0">
                <h2 class="m-0 text-3xl font-semibold leading-tight sm:text-4xl">
                  {{ isUsersPage ? 'Users' : 'Todos' }}
                </h2>
                <p class="mt-1 text-sm text-muted-foreground">
                  {{ isUsersPage ? 'Manage application accounts.' : 'Track backend-backed tasks.' }}
                </p>
              </div>
            </header>

            <template v-if="isUsersPage">
              <p v-if="!isAdmin" class="m-3 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                Admin access required.
              </p>

              <p v-if="usersError" class="m-3 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                {{ usersError }}
              </p>

              <UserTable v-if="isAdmin" :users="accounts" :loading="usersLoading" />
            </template>

            <template v-else>
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
            </template>
          </div>
        </div>
      </SidebarInset>
    </SidebarProvider>
  </main>
</template>
