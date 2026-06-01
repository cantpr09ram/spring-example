<script setup lang="ts">
import {
  IconDotsVertical,
  IconInnerShadowTop,
  IconLogout,
  IconUsers,
} from '@tabler/icons-vue';
import { Package } from '@lucide/vue';

import type { User } from '../../api/auth';
import {
  Avatar,
  AvatarFallback,
} from '../ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  useSidebar,
} from '../ui/sidebar';

const props = defineProps<{
  currentTab: 'products' | 'users';
  isAdmin: boolean;
  user: User;
}>();

const emit = defineEmits<{
  navigate: [path: string];
  signOut: [];
}>();

const { isMobile, setOpenMobile } = useSidebar();

const mainItems = [
  {
    title: 'Products',
    path: '/',
    tab: 'products' as const,
    icon: Package,
  },
  {
    title: 'Users',
    path: '/user',
    tab: 'users' as const,
    icon: IconUsers,
    adminOnly: true,
  },
];

function getInitials(name: string) {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'U';
}

function navigate(path: string) {
  emit('navigate', path);
  if (isMobile.value) {
    setOpenMobile(false);
  }
}

function signOut() {
  emit('signOut');
  if (isMobile.value) {
    setOpenMobile(false);
  }
}
</script>

<template>
  <Sidebar collapsible="icon" variant="inset">
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton
            size="lg"
            class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
          >
            <div class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
              <IconInnerShadowTop class="size-4" />
            </div>
            <div class="grid flex-1 text-left text-sm leading-tight">
              <span class="truncate font-medium">Full-stack starter</span>
              <span class="truncate text-xs text-muted-foreground">Spring + Vue</span>
            </div>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>

    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem
              v-for="item in mainItems.filter((entry) => !entry.adminOnly || props.isAdmin)"
              :key="item.title"
            >
              <SidebarMenuButton
                :is-active="props.currentTab === item.tab"
                :tooltip="item.title"
                @click="navigate(item.path)"
              >
                <component :is="item.icon" />
                <span>{{ item.title }}</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter>
      <SidebarMenu>
        <SidebarMenuItem>
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <SidebarMenuButton
                size="lg"
                class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
              >
                <Avatar class="h-8 w-8 rounded-lg">
                  <AvatarFallback class="rounded-lg">
                    {{ getInitials(props.user.displayName) }}
                  </AvatarFallback>
                </Avatar>
                <div class="grid flex-1 text-left text-sm leading-tight">
                  <span class="truncate font-medium">{{ props.user.displayName }}</span>
                  <span class="truncate text-xs text-muted-foreground">{{ props.user.email }}</span>
                </div>
                <IconDotsVertical class="ml-auto size-4" />
              </SidebarMenuButton>
            </DropdownMenuTrigger>
            <DropdownMenuContent
              class="w-(--reka-dropdown-menu-trigger-width) min-w-56 rounded-lg"
              :side="isMobile ? 'bottom' : 'right'"
              :side-offset="4"
              align="end"
            >
              <DropdownMenuLabel class="p-0 font-normal">
                <div class="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                  <Avatar class="h-8 w-8 rounded-lg">
                    <AvatarFallback class="rounded-lg">
                      {{ getInitials(props.user.displayName) }}
                    </AvatarFallback>
                  </Avatar>
                  <div class="grid flex-1 text-left text-sm leading-tight">
                    <span class="truncate font-medium">{{ props.user.displayName }}</span>
                    <span class="truncate text-xs text-muted-foreground">{{ props.user.email }}</span>
                  </div>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem @select="signOut">
                <IconLogout />
                Log out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarFooter>
    <SidebarRail />
  </Sidebar>
</template>
