<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import { ImagePlus, Plus, X } from '@lucide/vue';

import { apiBaseUrl } from '../../api/client';
import { uploadPhoto } from '../../api/photos';
import type { Product, ProductPayload } from '../../api/products';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';

const props = defineProps<{
  open: boolean;
  product: Product | null;
  saving: boolean;
}>();

const emit = defineEmits<{
  close: [];
  save: [payload: ProductPayload];
}>();

const form = reactive({
  name: '',
  category: '',
  price: 0,
  stockQuantity: 0,
  description: '',
  imageUrls: '',
});

const upload = reactive({
  loading: false,
  error: '',
});

const title = computed(() => (props.product ? 'Edit product' : 'Add product'));
const imageUrls = computed(() => parseImageUrls(form.imageUrls));
const canSubmit = computed(() => (
  form.name.trim().length > 0
  && form.category.trim().length > 0
  && Number(form.price) >= 0
  && Number(form.stockQuantity) >= 0
));

watch(
  () => [props.open, props.product] as const,
  () => {
    if (!props.open) {
      return;
    }

    form.name = props.product?.name ?? '';
    form.category = props.product?.category ?? '';
    form.price = Number(props.product?.price ?? 0);
    form.stockQuantity = props.product?.stockQuantity ?? 0;
    form.description = props.product?.description ?? '';
    form.imageUrls = props.product?.imageUrls.join('\n') ?? '';
    upload.error = '';
  },
  { immediate: true }
);

function parseImageUrls(value: string) {
  return value
    .split(/\n/)
    .map((url) => url.trim())
    .filter(Boolean);
}

function resolveImageUrl(url: string) {
  if (/^https?:\/\//i.test(url)) {
    return url;
  }

  return `${apiBaseUrl}${url.startsWith('/') ? url : `/${url}`}`;
}

function setImageUrls(urls: string[]) {
  form.imageUrls = urls.join('\n');
}

function removeImageUrl(index: number) {
  setImageUrls(imageUrls.value.filter((_, currentIndex) => currentIndex !== index));
}

function submit() {
  if (!canSubmit.value) {
    return;
  }

  emit('save', {
    name: form.name.trim(),
    category: form.category.trim(),
    price: Number(form.price),
    stockQuantity: Number(form.stockQuantity),
    description: form.description.trim() || null,
    imageUrls: parseImageUrls(form.imageUrls),
    active: props.product?.active ?? true,
  });
}

async function handlePhotoChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];

  if (!file) {
    return;
  }

  upload.loading = true;
  upload.error = '';

  try {
    const photo = await uploadPhoto(file);
    setImageUrls([...imageUrls.value, photo.url]);
    input.value = '';
  } catch (err) {
    upload.error = err instanceof Error ? err.message : 'Unable to upload photo.';
  } finally {
    upload.loading = false;
  }
}
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 grid place-items-center bg-black/45 px-3 py-5"
    role="presentation"
    @click.self="$emit('close')"
  >
    <section
      aria-modal="true"
      class="w-full max-w-[620px] rounded-md border bg-background shadow-lg"
      role="dialog"
    >
      <header class="flex items-center justify-between border-b px-4 py-3">
        <div>
          <h2 class="m-0 text-base font-semibold">{{ title }}</h2>
          <p class="m-0 mt-0.5 text-xs text-muted-foreground">
            Product data saved to the backend catalog.
          </p>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          class="size-8"
          aria-label="Close product dialog"
          @click="$emit('close')"
        >
          <X class="size-4" />
        </Button>
      </header>

      <form class="grid gap-3 p-4" @submit.prevent="submit">
        <div class="grid gap-3 sm:grid-cols-2">
          <div class="grid gap-1.5">
            <Label for="product-name">Name</Label>
            <Input
              id="product-name"
              v-model="form.name"
              maxlength="160"
              placeholder="Everyday Backpack"
              required
            />
          </div>

          <div class="grid gap-1.5">
            <Label for="product-category">Category</Label>
            <Input
              id="product-category"
              v-model="form.category"
              maxlength="80"
              placeholder="Bags"
              required
            />
          </div>

          <div class="grid gap-1.5">
            <Label for="product-price">Price</Label>
            <Input
              id="product-price"
              v-model.number="form.price"
              min="0"
              step="0.01"
              type="number"
              required
            />
          </div>

          <div class="grid gap-1.5">
            <Label for="product-stock">Stock</Label>
            <Input
              id="product-stock"
              v-model.number="form.stockQuantity"
              min="0"
              step="1"
              type="number"
              required
            />
          </div>
        </div>

        <div class="grid gap-1.5">
          <Label for="product-description">Description</Label>
          <textarea
            id="product-description"
            v-model="form.description"
            class="min-h-20 rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-xs outline-none transition-[color,box-shadow] focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50"
            maxlength="2000"
            placeholder="Short product description"
          />
        </div>

        <div class="grid gap-2">
          <div class="flex items-center justify-between gap-3">
            <Label>Photos</Label>
            <label
              class="inline-flex h-8 cursor-pointer items-center justify-center gap-1.5 rounded-md border bg-background px-3 text-sm font-medium shadow-xs transition-colors hover:bg-accent hover:text-accent-foreground"
              for="product-photo"
            >
              <ImagePlus class="size-4" />
              {{ upload.loading ? 'Uploading...' : 'Upload photo' }}
            </label>
            <input
              id="product-photo"
              class="sr-only"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              :disabled="upload.loading || saving"
              @change="handlePhotoChange"
            >
          </div>
          <p v-if="upload.error" class="m-0 text-xs text-destructive">
            {{ upload.error }}
          </p>
          <div
            v-if="imageUrls.length"
            class="grid grid-cols-3 gap-2 rounded-md border bg-muted/20 p-2 sm:grid-cols-4"
          >
            <div
              v-for="(url, index) in imageUrls"
              :key="url"
              class="group relative grid aspect-square place-items-center overflow-hidden rounded-md border bg-background"
            >
              <img
                :src="resolveImageUrl(url)"
                :alt="`Product photo ${index + 1}`"
                class="h-full w-full object-contain object-center"
              >
              <Button
                type="button"
                variant="destructive"
                size="icon"
                class="absolute right-1 top-1 size-6 opacity-0 shadow-sm transition-opacity group-hover:opacity-100 focus-visible:opacity-100"
                :aria-label="`Remove photo ${index + 1}`"
                :disabled="saving || upload.loading"
                @click="removeImageUrl(index)"
              >
                <X class="size-3.5" />
              </Button>
            </div>
          </div>
          <div
            v-else
            class="grid min-h-24 place-items-center rounded-md border border-dashed bg-muted/20 px-3 text-center text-xs text-muted-foreground"
          >
            Upload JPG, PNG, or WebP photos.
          </div>
        </div>

        <footer class="flex justify-end gap-2 pt-1">
          <Button
            type="button"
            variant="outline"
            :disabled="saving"
            @click="$emit('close')"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            :disabled="saving || !canSubmit"
          >
            <Plus />
            {{ saving ? 'Saving...' : 'Save' }}
          </Button>
        </footer>
      </form>
    </section>
  </div>
</template>
