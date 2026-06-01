<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import { ApiError, clearAuthToken, getAuthToken } from './api/client';
import { getCurrentUser, listUsers, login, type User } from './api/auth';
import LoginForm from './components/auth/LoginForm.vue';
import AppSidebar from './components/layout/AppSidebar.vue';
import ProductPage from './components/product/ProductPage.vue';
import UserTable from './components/user/UserTable.vue';
import { Separator } from './components/ui/separator';
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from './components/ui/sidebar';

const accounts = ref<User[]>([]);
const usersLoading = ref(false);
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
const currentTab = computed<'products' | 'users'>(() => (isUsersPage.value ? 'users' : 'products'));

function goToLogin(message = '') {
  clearAuthToken();
  user.value = null;
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

async function submitAuth(payload: { email: string; password: string }) {
  authLoading.value = true;
  authError.value = '';
  try {
    const response = await login({
      email: payload.email.trim(),
      password: payload.password,
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
              {{ isUsersPage ? 'Users' : 'Products' }}
            </h1>
          </div>
        </header>

        <div class="px-3.5 py-4 sm:px-5 sm:py-5">
          <div class="mx-auto max-w-[1120px]">
            <template v-if="isUsersPage">
              <header class="mb-4">
                <h2 class="m-0 text-2xl font-semibold leading-tight">Users</h2>
                <p class="mt-1 text-sm text-muted-foreground">Manage application accounts.</p>
              </header>

              <p v-if="!isAdmin" class="m-3 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                Admin access required.
              </p>

              <p v-if="usersError" class="m-3 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
                {{ usersError }}
              </p>

              <UserTable v-if="isAdmin" :users="accounts" :loading="usersLoading" />
            </template>

            <ProductPage v-else />
          </div>
        </div>
      </SidebarInset>
    </SidebarProvider>
  </main>
</template>
