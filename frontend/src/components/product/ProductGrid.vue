<script setup lang="ts">
import { MoreHorizontal, Package, Pencil, Trash2 } from '@lucide/vue';

import type { Product } from '../../api/products';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import { Card, CardContent } from '../ui/card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import { Skeleton } from '../ui/skeleton';
import ProductImageCarousel from './ProductImageCarousel.vue';

defineProps<{
  products: Product[];
  loading: boolean;
}>();

defineEmits<{
  edit: [product: Product];
  delete: [product: Product];
}>();
</script>

<template>
  <div v-if="loading" class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
    <Card v-for="index in 6" :key="index" class="gap-0 overflow-hidden py-0">
      <Skeleton class="aspect-square w-full rounded-none" />
      <CardContent class="grid gap-2 p-3">
        <Skeleton class="h-5 w-2/3" />
        <Skeleton class="h-4 w-1/2" />
        <Skeleton class="h-8 w-full" />
      </CardContent>
    </Card>
  </div>

  <div
    v-else-if="!products.length"
    class="grid min-h-[220px] place-items-center rounded-md border border-dashed bg-muted/20 px-4 text-center"
  >
    <div class="grid gap-2">
      <Package class="mx-auto size-9 text-muted-foreground" />
      <p class="m-0 text-sm font-medium">No products yet</p>
      <p class="m-0 text-sm text-muted-foreground">Add the first product to show it here.</p>
    </div>
  </div>

  <div v-else class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
    <Card
      v-for="product in products"
      :key="product.id"
      class="gap-0 overflow-hidden py-0"
    >
      <ProductImageCarousel
        :image-urls="product.imageUrls"
        :name="product.name"
      />

      <CardContent class="grid gap-2.5 p-3">
        <div class="flex min-w-0 items-start justify-between gap-2">
          <div class="min-w-0">
            <h3 class="m-0 truncate text-sm font-semibold">{{ product.name }}</h3>
            <p class="m-0 text-xs text-muted-foreground">{{ product.category }}</p>
          </div>
          <div class="flex shrink-0 items-center gap-1">
            <Badge :variant="product.active ? 'default' : 'secondary'" class="px-1.5 py-0 text-[11px]">
              {{ product.active ? 'Active' : 'Inactive' }}
            </Badge>
            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  class="size-7"
                  aria-label="Product actions"
                >
                  <MoreHorizontal class="size-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" class="w-32">
                <DropdownMenuItem @select="$emit('edit', product)">
                  <Pencil class="size-4" />
                  Edit
                </DropdownMenuItem>
                <DropdownMenuItem variant="destructive" @select="$emit('delete', product)">
                  <Trash2 class="size-4" />
                  Delete
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <p class="m-0 line-clamp-2 min-h-8 text-xs leading-4 text-muted-foreground">
          {{ product.description || 'No description' }}
        </p>

        <div class="flex items-end justify-between gap-2 border-t pt-2">
          <div>
            <p class="m-0 text-xs text-muted-foreground">Price</p>
            <p class="m-0 text-base font-semibold">${{ Number(product.price).toFixed(2) }}</p>
          </div>
          <p class="m-0 text-xs text-muted-foreground">
            {{ product.stockQuantity }} in stock
          </p>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
