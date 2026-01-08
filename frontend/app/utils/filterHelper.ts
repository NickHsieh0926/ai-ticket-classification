import type { PredictionResult } from '@/types/prediction'

export function filterTickets(
    tickets: PredictionResult[],
    filterLabel: string,
    minConfidence: number
) {
    return tickets.filter(ticket => {
        const labelMatch = filterLabel ? ticket.predictedLabel.includes(filterLabel) : true
        const confidenceMatch = ticket.confidence >= minConfidence
        return labelMatch && confidenceMatch
    })
}
