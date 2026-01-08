<template>
    <div class="min-h-screen bg-gray-100 flex">
        <!-- Sidebar -->
        <aside class="w-64 bg-white border-r">
            <div class="p-4 font-bold text-lg">
                Ticket Dashboard
            </div>

            <nav class="p-2 space-y-2">
                <NuxtLink to="/" class="block px-3 py-2 rounded hover:bg-gray-100">
                    Home
                </NuxtLink>

                <NuxtLink to="/tickets" class="block px-3 py-2 rounded hover:bg-gray-100">
                    Tickets
                </NuxtLink>
            </nav>
        </aside>

        <!-- Main -->
        <main class="flex-1 p-6">
            <slot />
        </main>
    </div>
</template>
