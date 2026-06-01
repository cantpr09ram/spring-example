<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Plus } from '@lucide/vue';

import { ApiError } from '../../api/client';
import {
  createProduct,
  deleteProduct,
  listProducts,
  updateProduct,
  type Product,
  type ProductPayload,
} from '../../api/products';
import { Button } from '../ui/button';
import ProductDialog from './ProductDialog.vue';
import ProductGrid from './ProductGrid.vue';

const products = ref<Product[]>([]);
const loading = ref(false);
const saving = ref(false);
const error = ref('');
const dialogOpen = ref(false);
const editingProduct = ref<Product | null>(null);

onMounted(() => {
  void loadProducts();
});

async function loadProducts() {
  products.value = [];
  error.value = '';
  loading.value = true;

  try {
    products.value = await listProducts();
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
    error.value = err instanceof Error ? err.message : 'Unable to load products.';
  } finally {
    loading.value = false;
  }
}

function openAddDialog() {
  editingProduct.value = null;
  dialogOpen.value = true;
}

function openEditDialog(product: Product) {
  editingProduct.value = product;
  dialogOpen.value = true;
}

function closeDialog() {
  if (saving.value) {
    return;
  }

  dialogOpen.value = false;
}

async function saveProduct(payload: ProductPayload) {
  saving.value = true;
  error.value = '';

  try {
    const product = editingProduct.value
      ? await updateProduct(editingProduct.value.id, payload)
      : await createProduct(payload);

    products.value = editingProduct.value
      ? products.value.map((item) => (item.id === product.id ? product : item))
      : [product, ...products.value];
    editingProduct.value = null;
    dialogOpen.value = false;
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
    error.value = err instanceof Error ? err.message : 'Unable to save product.';
  } finally {
    saving.value = false;
  }
}

async function removeProduct(product: Product) {
  if (!window.confirm(`Delete ${product.name}?`)) {
    return;
  }

  error.value = '';

  try {
    await deleteProduct(product.id);
    products.value = products.value.filter((item) => item.id !== product.id);
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      return;
    }
    error.value = err instanceof Error ? err.message : 'Unable to delete product.';
  }
}
</script>

<template>
  <section>
    <header class="mb-4 flex items-start justify-between gap-3">
      <div class="min-w-0">
        <p class="mt-1 text-sm text-muted-foreground">Browse and add catalog products.</p>
      </div>
      <Button
        type="button"
        size="sm"
        @click="openAddDialog"
      >
        <Plus />
        Add
      </Button>
    </header>

    <p v-if="error" class="m-3 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
      {{ error }}
    </p>

    <ProductGrid
      :products="products"
      :loading="loading"
      @edit="openEditDialog"
      @delete="removeProduct"
    />

    <ProductDialog
      :open="dialogOpen"
      :product="editingProduct"
      :saving="saving"
      @close="closeDialog"
      @save="saveProduct"
    />
  </section>
</template>
