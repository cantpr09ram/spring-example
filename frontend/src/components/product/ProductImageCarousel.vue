<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ImageIcon } from '@lucide/vue';

import { apiBaseUrl } from '../../api/client';
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
  type CarouselApi,
} from '../ui/carousel';

const props = defineProps<{
  imageUrls: string[];
  name: string;
}>();

const carouselApi = ref<CarouselApi | null>(null);
const activeIndex = ref(0);
const images = computed(() => props.imageUrls.filter((url) => url.trim().length > 0));
const hasMultipleImages = computed(() => images.value.length > 1);

watch(
  () => props.imageUrls,
  () => {
    activeIndex.value = 0;
    carouselApi.value?.scrollTo(0);
  }
);

function resolveImageUrl(url: string) {
  if (/^https?:\/\//i.test(url)) {
    return url;
  }

  return `${apiBaseUrl}${url.startsWith('/') ? url : `/${url}`}`;
}

function setCarouselApi(api: CarouselApi) {
  if (!api) {
    return;
  }

  carouselApi.value = api;
  activeIndex.value = api.selectedScrollSnap();
  api.on('select', () => {
    activeIndex.value = api.selectedScrollSnap();
  });
}

function scrollToImage(index: number) {
  carouselApi.value?.scrollTo(index);
}
</script>

<template>
  <div class="relative aspect-square overflow-hidden bg-muted">
    <Carousel
      v-if="images.length"
      class="h-full"
      :opts="{ loop: true }"
      @init-api="setCarouselApi"
    >
      <CarouselContent class="h-full -ml-0 [&>div]:h-full">
        <CarouselItem
          v-for="url in images"
          :key="url"
          class="h-full pl-0"
        >
          <img
            :src="resolveImageUrl(url)"
            :alt="name"
            class="h-full w-full object-contain object-center"
          >
        </CarouselItem>
      </CarouselContent>

      <template v-if="hasMultipleImages">
        <CarouselPrevious
          variant="secondary"
          class="left-2 size-7 bg-background/85 shadow-sm"
        />
        <CarouselNext
          variant="secondary"
          class="right-2 size-7 bg-background/85 shadow-sm"
        />

        <div class="absolute bottom-2 left-1/2 flex -translate-x-1/2 gap-1 rounded-full bg-background/85 px-1.5 py-1 shadow-sm">
          <button
            v-for="(_, index) in images"
            :key="index"
            type="button"
            class="size-1.5 rounded-full transition-colors"
            :class="index === activeIndex ? 'bg-foreground' : 'bg-muted-foreground/40'"
            :aria-label="`Show image ${index + 1}`"
            @click="scrollToImage(index)"
          />
        </div>
      </template>
    </Carousel>

    <div v-else class="grid h-full place-items-center">
      <ImageIcon class="size-8 text-muted-foreground" />
    </div>
  </div>
</template>
