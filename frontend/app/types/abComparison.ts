export interface AbComparisonRow {
    traceId: string
    content: string
    mlCategory: string
    llmCategory: string
    mlConfidence: number
    llmConfidence: number
    isMatch: number
}