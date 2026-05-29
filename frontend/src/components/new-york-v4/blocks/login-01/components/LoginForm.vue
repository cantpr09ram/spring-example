<script setup lang="ts">
import type { HTMLAttributes } from "vue"
import { computed, ref, watch } from "vue"
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'

const props = defineProps<{
  class?: HTMLAttributes["class"]
  error?: string
  loading?: boolean
  resetKey?: number
}>()

const emit = defineEmits<{
  submit: [payload: { email: string; password: string }]
}>()

const email = ref("")
const password = ref("")

const title = computed(() => "Login to your account")
const description = computed(() => "Enter your email below to login to your account")
const submitLabel = computed(() => {
  if (props.loading) {
    return "Please wait..."
  }
  return "Login"
})

function handleSubmit() {
  emit("submit", {
    email: email.value,
    password: password.value,
  })
}

watch(() => props.resetKey, () => {
  email.value = ""
  password.value = ""
})
</script>

<template>
  <div :class="cn('flex flex-col gap-6', props.class)">
    <Card>
      <CardHeader>
        <CardTitle>{{ title }}</CardTitle>
        <CardDescription>
          {{ description }}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form @submit.prevent="handleSubmit">
          <FieldGroup>
            <Field>
              <FieldLabel for="email">
                Email
              </FieldLabel>
              <Input
                id="email"
                v-model="email"
                type="email"
                placeholder="m@example.com"
                autocomplete="email"
                maxlength="254"
                required
              />
            </Field>
            <Field>
              <FieldLabel for="password">
                Password
              </FieldLabel>
              <Input
                id="password"
                v-model="password"
                autocomplete="current-password"
                type="password"
                minlength="8"
                maxlength="72"
                required
              />
            </Field>
            <p v-if="error" class="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {{ error }}
            </p>
            <Field>
              <Button type="submit" :disabled="loading">
                {{ submitLabel }}
              </Button>
              <FieldDescription class="text-center">
                Ask an admin to create an account for you.
              </FieldDescription>
            </Field>
          </FieldGroup>
        </form>
      </CardContent>
    </Card>
  </div>
</template>
