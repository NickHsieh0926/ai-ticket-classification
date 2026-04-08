import { defineStore } from 'pinia'

export const useModelStore = defineStore('model', {
  state: () => ({
    modelType: 'ml' as 'ml' | 'llm'
  }),
  actions: {
    setModelType(type: 'ml' | 'llm') {
      this.modelType = type
    }
  }
})