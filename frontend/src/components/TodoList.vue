<script setup lang="ts">
import type { Todo } from '../api/todos';

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
    class="rounded-lg border border-[#dde3ea] bg-white p-2 shadow-[0_10px_30px_rgb(31_41_55_/_8%)]"
    aria-label="Todo list"
  >
    <p v-if="loading" class="m-3 text-[#687581]">Loading todos...</p>
    <p v-else-if="todos.length === 0" class="m-3 text-[#687581]">No todos yet.</p>

    <ul v-else class="grid list-none gap-1 p-0">
      <li
        v-for="todo in todos"
        :key="todo.id"
        class="flex min-h-[52px] flex-col items-start justify-between gap-3 rounded-md px-2.5 py-2 hover:bg-[#f1f5f8] sm:flex-row sm:items-center"
      >
        <label class="flex min-w-0 items-center gap-2.5">
          <input
            type="checkbox"
            :checked="todo.completed"
            @change="emit('toggle', todo)"
          />
          <span
            class="[overflow-wrap:anywhere]"
            :class="{ 'text-[#687581] line-through': todo.completed }"
          >
            {{ todo.title }}
          </span>
        </label>
        <button
          type="button"
          class="min-h-9 cursor-pointer rounded-md bg-transparent px-2 text-[#9b2c2c]"
          @click="emit('remove', todo.id)"
        >
          Delete
        </button>
      </li>
    </ul>
  </section>
</template>
