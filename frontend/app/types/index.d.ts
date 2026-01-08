declare module 'nuxt/schema' {
    interface RuntimeConfig {
        apiInternalUrl: string
    }
    interface PublicRuntimeConfig {
        apiBaseUrl: string
    }
}

export { }