<script setup lang="ts">
import { Trash2 } from '@lucide/vue';

import type { Todo } from '../api/todos';
import { Button } from './ui/button';

defineProps<{
  todos: Todo[];
  loading: boolean;
}>();

const emit = defineEmits<{
  toggle: [todo: Todo];
  remove: [id: number];
}>();
</script>

<template>
  <section
    class="rounded-lg border bg-card p-2 text-card-foreground shadow-sm"
    aria-label="Todo list"
  >
    <p v-if="loading" class="m-3 text-muted-foreground">Loading todos...</p>
    <p v-else-if="todos.length === 0" class="m-3 text-muted-foreground">No todos yet.</p>

    <ul v-else class="grid list-none gap-1 p-0">
      <li
        v-for="todo in todos"
        :key="todo.id"
        class="flex min-h-[52px] flex-col items-start justify-between gap-3 rounded-md px-2.5 py-2 hover:bg-muted sm:flex-row sm:items-center"
      >
        <label class="flex min-w-0 items-center gap-2.5">
          <input
            type="checkbox"
            :checked="todo.completed"
            @change="emit('toggle', todo)"
          />
          <span
            class="[overflow-wrap:anywhere]"
            :class="{ 'text-muted-foreground line-through': todo.completed }"
          >
            {{ todo.title }}
          </span>
        </label>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          aria-label="Delete todo"
          @click="emit('remove', todo.id)"
        >
          <Trash2 />
        </Button>
      </li>
    </ul>
  </section>
</template>
