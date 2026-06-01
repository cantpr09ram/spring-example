<script setup lang="ts">
import type { User } from '../api/auth';

defineProps<{
  users: User[];
  loading: boolean;
}>();

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short'
});

function formatDate(value: string) {
  return dateFormatter.format(new Date(value));
}
</script>

<template>
  <section class="overflow-hidden rounded-lg border bg-card text-card-foreground shadow-sm" aria-label="Users">
    <p v-if="loading" class="m-3 text-sm text-muted-foreground">Loading users...</p>
    <p v-else-if="users.length === 0" class="m-3 text-sm text-muted-foreground">No users found.</p>

    <div v-else class="overflow-x-auto">
      <table class="w-full min-w-[680px] border-collapse text-left text-sm">
        <thead class="bg-muted text-xs font-semibold uppercase text-muted-foreground">
          <tr>
            <th class="px-4 py-3">ID</th>
            <th class="px-4 py-3">Name</th>
            <th class="px-4 py-3">Email</th>
            <th class="px-4 py-3">Role</th>
            <th class="px-4 py-3">Created</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="account in users" :key="account.id" class="border-t hover:bg-muted/50">
            <td class="px-4 py-3 font-mono text-xs text-muted-foreground">{{ account.id }}</td>
            <td class="px-4 py-3 font-medium">{{ account.displayName }}</td>
            <td class="px-4 py-3 text-muted-foreground">{{ account.email }}</td>
            <td class="px-4 py-3">
              <span class="rounded-md border px-2 py-1 text-xs font-semibold">
                {{ account.role }}
              </span>
            </td>
            <td class="px-4 py-3 text-muted-foreground">{{ formatDate(account.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
